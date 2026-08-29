package app.bodyforger.mobile.scale

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.bodyforger.core.ble.AndroidGattTransport
import app.bodyforger.core.ble.AndroidScaleScanner
import app.bodyforger.core.ble.AthleteInstruction
import app.bodyforger.core.ble.DiscoveredScale
import app.bodyforger.core.ble.ScanRejected
import app.bodyforger.core.ble.SessionFailure
import app.bodyforger.core.ble.WeighInState
import app.bodyforger.core.ble.ScaleIdentifier
import app.bodyforger.core.ble.huawei.HuaweiScaleModel
import app.bodyforger.core.ble.PairingState
import app.bodyforger.core.ble.huawei.HuaweiPairingSession
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.Job
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
    /**
     * Une masse relevée que la balance n'a pas pu accompagner d'un taux de masse grasse.
     *
     * Rien n'est enregistré tant que ce taux manque : un relevé sans lui n'existe pas (#20).
     * La mesure n'est pas perdue pour autant — elle attend une saisie.
     */
    val weightAwaitingBodyFat: Double? = null,
    val failure: SessionFailure? = null,
    /** Message d'un scan refusé par le système, à distinguer d'une absence de résultat. */
    val scanError: String? = null,
    val isWeighing: Boolean = false,
    val isPairing: Boolean = false,
    /** Étape courante de l'appairage, sur son total. */
    val pairingStep: Pair<Int, Int>? = null,
    val pairingInstructions: List<AthleteInstruction> = emptyList()
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

    /**
     * Le scan en cours.
     *
     * ⚠️ Android échoue à établir une connexion GATT pendant qu'un scan tourne : le radio
     * est occupé et `connectGatt` retourne un échec sans explication. Le scan doit donc être
     * **réellement annulé** avant toute connexion, et pas seulement masqué dans l'état.
     */
    private var scanJob: Job? = null

    private val _state = MutableStateFlow(ScaleUiState())
    val state: StateFlow<ScaleUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            // Le HUID existe dès l'ouverture de la base, avant tout appairage : il appartient
            // à l'athlète et survit à l'échec d'une association (#19).
            val huid = database.athleteIdentityDao().huidOrCreate(System.currentTimeMillis())
            val association = database.scaleAssociationDao().mostRecent()?.toDomain()
            // Le dernier relevé vient de la base : sans lui, une pesée après redémarrage
            // annoncerait un poids nul à la balance, qui s'en sert pour cadrer sa mesure.
            val lastLog = database.bodyLogDao().mostRecent()?.toDomain()
            _state.value = _state.value.copy(
                huid = huid,
                association = association?.copy(huid = huid),
                lastLog = lastLog
            )
        }
    }

    /** Balaie les alentours. La balance ne s'annonce qu'après un tapotement. */
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
                // Un refus du système n'est pas une absence de balance : le taire laisserait
                // l'écran chercher indéfiniment.
                _state.value = _state.value.copy(
                    isScanning = false,
                    scanError = (cause as? ScanRejected)?.message ?: "La recherche a échoué."
                )
            }.collect { found ->
                val known = _state.value.discovered
                // Une balance s'annonce en boucle : on tient une entrée par adresse.
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
     * Déroule l'appairage **Mode 1** : gravure du HUID, puis relevé de la tare.
     *
     * ⚠️ La gravure consomme un emplacement de la mémoire flash, définitivement, et elle a
     * lieu **avant** que l'athlète ne monte. Rejouer l'appairage réécrit le même emplacement
     * puisque le HUID ne change jamais (#19).
     *
     * Sans tare relevée, aucune Association n'est enregistrée : elle n'existe pas tant que
     * la pesée de validation n'a pas eu lieu.
     */
    fun associate(scale: DiscoveredScale, profile: BiaProfile) {
        // Un appareil non reconnu n'a pas de pilote : tenter une gravure dessus serait
        // écrire au hasard dans la mémoire d'un matériel inconnu.
        if (!scale.isCompatible) return
        val huid = _state.value.huid ?: return
        if (_state.value.isPairing) return

        // Couper le scan **avant** de connecter : le laisser tourner fait échouer la
        // connexion sans message.
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
                            database.scaleAssociationDao()
                                .upsert(state.association.toEntity(System.currentTimeMillis()))
                            _state.value = _state.value.copy(
                                association = state.association,
                                discovered = emptyList(),
                                pairingStep = null,
                                pairingInstructions = emptyList()
                            )
                        }
                    }
                }
            } finally {
                transport.close()
                _state.value = _state.value.copy(isPairing = false)
            }
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
                // Le nom annoncé n'est plus reconnu : mieux vaut le dire que deviner un modèle.
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

                // Le taux de la balance fait référence ; sans lui, le relevé attend une
                // saisie plutôt que d'être enregistré avec un chiffre inventé (#20).
                //
                // Ce n'est pas une panne : une balance huit électrodes dont l'athlète n'a pas
                // saisi la poignée renvoie une trame complète où seule la masse est
                // renseignée, et acquitte la pesée. La fidélité obtenue est simplement
                // moindre (#24).
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
                database.bodyLogDao().save(log.toEntity(address), log.impedanceRows())
                _state.value = _state.value.copy(lastLog = log, progress = null)
            }
        }
    }

    /**
     * Le meilleur poids connu à annoncer à la balance, qui s'en sert pour cadrer sa mesure.
     *
     * Le dernier relevé d'abord, la tare d'appairage ensuite. Aucun des deux n'est fabriqué :
     * à défaut, rien n'est annoncé plutôt qu'un chiffre inventé, que la balance graverait
     * dans sa calibration (#19).
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
