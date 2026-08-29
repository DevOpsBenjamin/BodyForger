package app.bodyforger.mobile.scale

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.bodyforger.core.ble.AndroidGattTransport
import app.bodyforger.core.ble.AndroidScaleScanner
import app.bodyforger.core.ble.DiscoveredScale
import app.bodyforger.core.ble.SessionFailure
import app.bodyforger.core.ble.WeighInState
import app.bodyforger.core.ble.ScaleIdentifier
import app.bodyforger.core.ble.huawei.HuaweiScaleModel
import app.bodyforger.core.ble.huawei.HuaweiWeighInSession
import app.bodyforger.core.database.BodyForgerDatabases
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
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

/**
 * Ce que l'écran Balance montre à un instant donné.
 *
 * L'appareil est **découvert** avant d'être associé : l'adresse vient du scan natif, jamais
 * d'une saisie (#19).
 */
data class ScaleUiState(
    val isScanning: Boolean = false,
    val discovered: List<DiscoveredScale> = emptyList(),
    val association: ScaleAssociation? = null,
    val huid: String? = null,
    val progress: WeighInState.Progress? = null,
    val lastLog: BodyLog? = null,
    val failure: SessionFailure? = null,
    val isWeighing: Boolean = false
) {
    val isAssociated: Boolean get() = association != null
}

/**
 * L'état de la balance et le déroulé d'une pesée, du scan à l'écriture en base.
 *
 * Le pilote et le protocole vivent dans `core-ble` ; ce modèle ne fait que les enchaîner et
 * exposer leur progression à l'interface.
 */
class ScaleViewModel(application: Application) : AndroidViewModel(application) {

    private val database = BodyForgerDatabases.get(application)
    private val scanner = AndroidScaleScanner(application)
    /** Le scan n'a besoin que de reconnaître un nom annoncé. */
    private val identifier = ScaleIdentifier(HuaweiScaleModel::recognise)

    private val _state = MutableStateFlow(ScaleUiState())
    val state: StateFlow<ScaleUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            // Le HUID existe dès l'ouverture de la base, avant tout appairage : il appartient
            // à l'athlète et survit à l'échec d'une association (#19).
            val huid = database.athleteIdentityDao().huidOrCreate(System.currentTimeMillis())
            val association = database.scaleAssociationDao().mostRecent()?.toDomain()
            _state.value = _state.value.copy(
                huid = huid,
                association = association?.copy(huid = huid)
            )
        }
    }

    /** Balaie les alentours. La balance ne s'annonce qu'après un tapotement. */
    fun startScan() {
        if (_state.value.isScanning) return
        _state.value = _state.value.copy(isScanning = true, discovered = emptyList(), failure = null)
        viewModelScope.launch {
            scanner.scan(identifier).collect { found ->
                val known = _state.value.discovered
                // Une balance s'annonce en boucle : on tient une entrée par adresse.
                _state.value = _state.value.copy(
                    discovered = (known.filterNot { it.deviceAddress == found.deviceAddress } + found)
                        .sortedByDescending { it.signalStrengthDbm }
                )
            }
        }
    }

    fun stopScan() {
        _state.value = _state.value.copy(isScanning = false)
    }

    /**
     * Retient une balance découverte.
     *
     * L'Association reste incomplète tant qu'aucune tare n'a été relevée — la première pesée
     * la fournira. Rien de fabriqué n'est écrit en attendant.
     */
    fun associate(scale: DiscoveredScale) {
        val huid = _state.value.huid ?: return
        viewModelScope.launch {
            val association = ScaleAssociation(
                deviceAddress = scale.deviceAddress,
                huid = huid,
                tareKg = 0.0,
                advertisedName = scale.advertisedName,
                capability = scale.recognised.capability
            )
            database.scaleAssociationDao().upsert(association.toEntity(System.currentTimeMillis()))
            _state.value = _state.value.copy(
                association = association,
                isScanning = false,
                discovered = emptyList()
            )
        }
    }

    fun forgetScale() {
        val address = _state.value.association?.deviceAddress ?: return
        viewModelScope.launch {
            database.scaleAssociationDao().forget(address)
            _state.value = _state.value.copy(association = null, lastLog = null, progress = null)
        }
    }

    /** Déroule une pesée et enregistre son relevé. */
    fun weighIn(profile: BiaProfile) {
        val association = _state.value.association ?: return
        val huid = _state.value.huid ?: return
        if (_state.value.isWeighing) return

        _state.value = _state.value.copy(isWeighing = true, failure = null, progress = null)
        viewModelScope.launch {
            val device = bluetoothDevice(association.deviceAddress)
            if (device == null) {
                _state.value = _state.value.copy(isWeighing = false, failure = SessionFailure.DEVICE_NOT_FOUND)
                return@launch
            }

            val model = HuaweiScaleModel.identify(association.advertisedName)
            if (model == null) {
                // Le nom annoncé n'est plus reconnu : mieux vaut le dire que deviner un modèle.
                _state.value = _state.value.copy(isWeighing = false, failure = SessionFailure.DEVICE_ERROR)
                return@launch
            }
            val transport = AndroidGattTransport(getApplication(), device, model.gattProfile)
            try {
                HuaweiWeighInSession(transport, model).run(
                    association = association,
                    huid = huid,
                    profile = ScaleUserProfile(profile, lastWeightKg = _state.value.lastLog?.massKg)
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

                // Le taux de la balance fait référence ; sans lui, le relevé attend une
                // saisie plutôt que d'être enregistré avec un chiffre inventé (#20).
                val bodyFat = telemetry.bodyFatPercentage
                if (bodyFat == null) {
                    _state.value = _state.value.copy(failure = SessionFailure.DEVICE_ERROR, progress = null)
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
                database.bodyLogDao().save(log.toEntity(address), log.impedanceRows())
                _state.value = _state.value.copy(lastLog = log, progress = null)
            }
        }
    }

    private fun bluetoothDevice(address: String) = runCatching {
        getApplication<Application>()
            .getSystemService(android.bluetooth.BluetoothManager::class.java)
            ?.adapter
            ?.getRemoteDevice(address)
    }.getOrNull()
}
