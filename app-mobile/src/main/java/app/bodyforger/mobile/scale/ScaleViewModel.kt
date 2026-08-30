package app.bodyforger.mobile.scale

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.bodyforger.core.ble.AndroidGattTransport
import app.bodyforger.core.ble.AndroidScaleScanner
import app.bodyforger.core.ble.DiscoveredScale
import app.bodyforger.core.ble.ScanRejected
import app.bodyforger.core.ble.SessionFailure
import app.bodyforger.core.ble.WeighInState
import app.bodyforger.core.ble.ScaleIdentifier
import app.bodyforger.core.ble.huawei.HuaweiScaleModel
import app.bodyforger.core.ble.PairingState
import app.bodyforger.core.ble.huawei.HuaweiPairingSession
import app.bodyforger.core.ble.huawei.HuaweiWeighInSession
import app.bodyforger.core.database.dao.AthleteIdentityDao
import app.bodyforger.core.database.dao.BodyLogDao
import app.bodyforger.core.database.dao.ScaleAssociationDao
import app.bodyforger.core.database.entity.impedanceRows
import app.bodyforger.core.database.entity.toDomain
import app.bodyforger.core.database.entity.toEntity
import app.bodyforger.core.model.BiaProfile
import app.bodyforger.core.model.BodyLog
import app.bodyforger.core.model.ScaleAssociation
import app.bodyforger.core.model.ScaleUserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

/**
 * Scale state and the course of a weigh-in, from scan to database write.
 *
 * The driver and protocol live in `core-ble`; this only chains them and exposes progress.
 */
class ScaleViewModel(
    application: Application,
    private val athleteIdentityDao: AthleteIdentityDao,
    private val bodyLogDao: BodyLogDao,
    private val scaleAssociationDao: ScaleAssociationDao
) : AndroidViewModel(application) {

    private val scanner = AndroidScaleScanner(application)
    /** Le scan n'a besoin que de reconnaître un nom annoncé. */
    private val identifier = ScaleIdentifier(HuaweiScaleModel::recognise)

    /**
     * The running scan.
     *
     * ⚠️ Android cannot open a GATT connection while a scan runs, so it must be genuinely
     * cancelled — not merely hidden in the state.
     */
    private var scanJob: Job? = null

    private val _state = MutableStateFlow(ScaleUiState())
    val state: StateFlow<ScaleUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val huid = athleteIdentityDao.huidOrCreate(System.currentTimeMillis())
            val association = scaleAssociationDao.mostRecent()?.toDomain()
            val lastLog = bodyLogDao.mostRecent()?.toDomain()
            _state.value = _state.value.copy(
                huid = huid,
                association = association?.copy(huid = huid),
                lastLog = lastLog
            )
        }
    }

    /** Scans for nearby scales. */
    fun startScan() {
        if (_state.value.isScanning) return
        _state.value = _state.value.copy(
            isScanning = true,
            discovered = emptyList(),
            failure = null,
            scanError = null
        )
        scanJob = viewModelScope.launch {
            scanner.scan(identifier).catch { cause ->
                _state.value = _state.value.copy(
                    isScanning = false,
                    scanError = (cause as? ScanRejected)?.message ?: "La recherche a échoué."
                )
            }.collect { found ->
                val known = _state.value.discovered
                _state.value = _state.value.copy(
                    // Les compatibles d'abord, puis les plus proches : c'est l'ordre dans
                    // lequel on cherche sa balance des yeux.
                    discovered = (known.filterNot { it.deviceAddress == found.deviceAddress } + found)
                        .sortedWith(
                            compareByDescending<DiscoveredScale> { it.isCompatible }
                                .thenByDescending { it.signalStrengthDbm }
                        )
                )
            }
        }
    }

    fun stopScan() {
        scanJob?.cancel()
        scanJob = null
        _state.value = _state.value.copy(isScanning = false)
    }

    /**
     * Runs pairing: HUID engraving, then the tare.
     *
     * ⚠️ Engraving consumes a flash slot for good, and happens **before** the athlete steps
     * on. Replaying overwrites the same slot, the HUID never changing.
     */
    fun associate(scale: DiscoveredScale, profile: BiaProfile) {
        // Un appareil non reconnu n'a pas de pilote : graver dessus échouerait sans message.
        if (!scale.isCompatible) return
        val huid = _state.value.huid ?: return
        if (_state.value.isPairing) return

        stopScan()
        _state.value = _state.value.copy(isPairing = true, failure = null, progress = null)
        viewModelScope.launch {
            val device = bluetoothDevice(scale.deviceAddress)
            val model = HuaweiScaleModel.identify(scale.advertisedName)
            if (device == null || model == null) {
                _state.value = _state.value.copy(isPairing = false, failure = SessionFailure.DEVICE_NOT_FOUND)
                return@launch
            }

            val transport = AndroidGattTransport(getApplication(), device, model.gattProfile)
            try {
                HuaweiPairingSession(transport, model).run(
                    deviceAddress = scale.deviceAddress,
                    advertisedName = scale.advertisedName,
                    huid = huid,
                    profile = ScaleUserProfile(profile, lastWeightKg = lastKnownWeightKg())
                ).collect { state ->
                    when (state) {
                        is PairingState.Progress -> _state.value = _state.value.copy(
                            pairingStep = state.index to state.totalSteps,
                            pairingInstructions = state.instructions
                        )
                        is PairingState.Failed -> _state.value = _state.value.copy(
                            failure = state.reason,
                            pairingStep = null,
                            pairingInstructions = emptyList()
                        )
                        is PairingState.Completed -> {
                            scaleAssociationDao
                                .upsert(state.association.toEntity(System.currentTimeMillis()))
                            _state.value = _state.value.copy(
                                association = state.association,
                                discovered = emptyList(),
                                pairingStep = null,
                                pairingInstructions = emptyList()
                            )
                            state.validation?.let { handle(WeighInState.Completed(it)) }
                        }
                    }
                }
            } finally {
                transport.close()
                _state.value = _state.value.copy(isPairing = false)
            }
        }
    }

    /** Clears what the last weigh-in left on screen, once the athlete has read it. */
    fun clearWeighInFeedback() {
        _state.value = _state.value.copy(failure = null, weightAwaitingBodyFat = null, progress = null)
    }

    fun forgetScale() {
        val address = _state.value.association?.deviceAddress ?: return
        viewModelScope.launch {
            scaleAssociationDao.forget(address)
            _state.value = _state.value.copy(association = null, lastLog = null, progress = null)
        }
    }

    /** Runs a weigh-in and stores its reading. */
    fun weighIn(profile: BiaProfile) {
        val association = _state.value.association ?: return
        val huid = _state.value.huid ?: return
        if (_state.value.isWeighing) return

        stopScan()
        _state.value = _state.value.copy(
            isWeighing = true,
            failure = null,
            progress = null,
            weightAwaitingBodyFat = null
        )
        viewModelScope.launch {
            val device = bluetoothDevice(association.deviceAddress)
            if (device == null) {
                _state.value = _state.value.copy(isWeighing = false, failure = SessionFailure.DEVICE_NOT_FOUND)
                return@launch
            }

            val model = HuaweiScaleModel.identify(association.advertisedName)
            if (model == null) {
                _state.value = _state.value.copy(isWeighing = false, failure = SessionFailure.DEVICE_ERROR)
                return@launch
            }
            val transport = AndroidGattTransport(getApplication(), device, model.gattProfile)
            try {
                HuaweiWeighInSession(transport, model).run(
                    association = association,
                    huid = huid,
                    profile = ScaleUserProfile(profile, lastWeightKg = lastKnownWeightKg())
                ).collect { state -> handle(state) }
            } finally {
                transport.close()
                _state.value = _state.value.copy(isWeighing = false)
            }
        }
    }

    private suspend fun handle(state: WeighInState) {
        when (state) {
            is WeighInState.Progress -> _state.value = _state.value.copy(progress = state)
            is WeighInState.LiveWeight -> Unit
            is WeighInState.Failed -> _state.value = _state.value.copy(failure = state.reason, progress = null)
            is WeighInState.Completed -> {
                val telemetry = state.telemetry
                val measuredAt = telemetry.measuredAt
                    ?.atZone(ZoneId.systemDefault())?.toInstant()
                    ?: Instant.now()

                // Une pesée sans BIA — pieds nus manquants, contact incomplet — rend un poids
                // seul : on l'annonce plutôt que de le taire (#24).
                val bodyFat = telemetry.bodyFatPercentage
                if (bodyFat == null) {
                    _state.value = _state.value.copy(
                        weightAwaitingBodyFat = telemetry.massKg,
                        progress = null
                    )
                    return
                }

                val log = BodyLog(
                    id = UUID.randomUUID().toString(),
                    dateIso = measuredAt.atZone(ZoneId.systemDefault()).toLocalDate().toString(),
                    measuredAtEpochMs = measuredAt.toEpochMilli(),
                    massKg = telemetry.massKg,
                    bodyFatPercentage = bodyFat,
                    rawImpedances = telemetry.rawImpedances,
                    restingHeartRateBpm = telemetry.heartRateBpm
                )
                val address = _state.value.association?.deviceAddress
                bodyLogDao.save(log.toEntity(address), log.impedanceRows())
                _state.value = _state.value.copy(lastLog = log, progress = null)
            }
        }
    }

    /**
     * The best known weight to announce, which the scale uses to frame its measurement.
     *
     * Last reading first, pairing tare next; failing both, nothing is announced rather than
     * an invented figure.
     */
    private fun lastKnownWeightKg(): Double? =
        _state.value.lastLog?.massKg ?: _state.value.association?.tareKg?.takeIf { it > 0.0 }

    private fun bluetoothDevice(address: String) = runCatching {
        getApplication<Application>()
            .getSystemService(android.bluetooth.BluetoothManager::class.java)
            ?.adapter
            ?.getRemoteDevice(address)
    }.getOrNull()
}
