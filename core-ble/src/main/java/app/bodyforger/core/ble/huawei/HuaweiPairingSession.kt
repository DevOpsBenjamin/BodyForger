package app.bodyforger.core.ble.huawei

import android.util.Log
import app.bodyforger.core.ble.PairingState
import app.bodyforger.core.ble.ScaleNotification
import app.bodyforger.core.ble.ScaleTransport
import app.bodyforger.core.ble.SessionFailure
import app.bodyforger.core.model.ScaleAssociation
import app.bodyforger.core.model.ScaleCapability
import app.bodyforger.core.model.ScaleUserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeoutOrNull
import java.security.SecureRandom
import java.time.LocalDateTime

/**
 * L'appairage **Mode 1** : graver le profil de l'athlète dans la mémoire flash de la
 * balance, puis relever la tare.
 *
 * ⚠️ **La gravure précède la pesée et consomme un emplacement, définitivement.** L'ordre
 * n'est pas un détail : quand l'athlète est invité à monter, le slot est déjà pris. C'est ce
 * qui rend un abandon bénin — il n'y a plus rien à préserver — mais aussi ce qui interdit de
 * lancer cette séquence à la légère.
 *
 * Le HUID est fourni par l'appelant et **jamais généré ici** : il appartient à l'athlète, a
 * été créé une seule fois à l'ouverture de la base, et rejouer un appairage doit réécrire le
 * **même** emplacement plutôt que d'en consommer un second (#19).
 */
class HuaweiPairingSession(
    private val transport: ScaleTransport,
    private val model: HuaweiScaleModel,
    private val clock: () -> LocalDateTime = LocalDateTime::now,
    private val random: SecureRandom = SecureRandom(),
    private val athleteTimeoutMs: Long = DEFAULT_TARE_TIMEOUT_MS
) {

    fun run(
        deviceAddress: String,
        advertisedName: String,
        huid: String,
        profile: ScaleUserProfile
    ): Flow<PairingState> = flow {
        // Un scope englobant : écouter une réponse doit pouvoir démarrer **avant** l'écriture
        // qui la provoque, sans quoi une réponse immédiate serait perdue.
        coroutineScope {
        val steps = HuaweiPairingSequence.stepsFor(model)
        var index = 0
        suspend fun advance() {
            val step = steps[index]
            emit(PairingState.Progress(index, steps.size, step.phase, step.instructions, step.detail))
            index++
        }

        advance() // Réveil et scan ciblé
        if (!transport.connect()) {
            emit(PairingState.Failed(SessionFailure.CONNECTION_LOST))
            return@coroutineScope
        }

        advance() // Handshake
        val sessionKey = HuaweiHandshake(transport, model, random).negotiate(deviceAddress)
        if (sessionKey == null) {
            emit(PairingState.Failed(SessionFailure.REJECTED_BY_DEVICE))
            return@coroutineScope
        }

        advance() // Armement du mode association
        if (!transport.subscribe(HuaweiCharacteristic.HUID_REGISTRATION)) {
            emit(PairingState.Failed(SessionFailure.CONNECTION_LOST))
            return@coroutineScope
        }
        if (!transport.write(HuaweiCharacteristic.BINDING_CONTROL, HuaweiPayloads.bindingControl(armed = true))) {
            emit(PairingState.Failed(SessionFailure.REJECTED_BY_DEVICE))
            return@coroutineScope
        }

        advance() // Gravure du HUID — irréversible à partir d'ici
        // La tare répond sur la même caractéristique : on écoute avant d'écrire, faute de
        // quoi une réponse immédiate serait perdue.
        val tareAwaited = async {
            transport.incoming.first { it.characteristic == HuaweiCharacteristic.HUID_REGISTRATION }
        }
        val engraved = writeSealed(
            sessionKey,
            HuaweiCharacteristic.HUID_REGISTRATION,
            huid.toByteArray(Charsets.US_ASCII).copyOf(HUID_FIELD_BYTES)
        )
        if (!engraved) {
            tareAwaited.cancel()
            transport.write(HuaweiCharacteristic.BINDING_CONTROL, HuaweiPayloads.bindingControl(armed = false))
            emit(PairingState.Failed(SessionFailure.REJECTED_BY_DEVICE))
            return@coroutineScope
        }

        advance() // Attente de la tare : l'athlète doit monter
        val tareKg = awaitTare(sessionKey, tareAwaited)
        if (tareKg == null) {
            // La référence poursuit ici avec une tare de zéro, ou un ancien poids, et l'écrit
            // dans la mémoire flash. Nous refusons : une Association sans tare n'existe pas,
            // et l'appairage se rejouera sur le même HUID sans rien coûter (#19).
            transport.write(HuaweiCharacteristic.BINDING_CONTROL, HuaweiPayloads.bindingControl(armed = false))
            emit(PairingState.Failed(SessionFailure.TIMED_OUT))
            return@coroutineScope
        }

        advance() // Synchronisation de l'heure
        transport.write(HuaweiCharacteristic.TIME_SYNC, HuaweiPayloads.currentTime(clock()))

        advance() // Profil utilisateur, avec la tare réellement relevée
        writeSealed(
            sessionKey,
            HuaweiCharacteristic.USER_PROFILE,
            HuaweiPayloads.userProfile(huid, profile, weightKg = tareKg)
        )

        advance() // Désarmement
        transport.write(HuaweiCharacteristic.BINDING_CONTROL, HuaweiPayloads.bindingControl(armed = false))
        transport.unsubscribe(HuaweiCharacteristic.HUID_REGISTRATION)

        advance() // Armement du flux BIA — l'athlète est toujours sur la balance
        transport.subscribe(HuaweiCharacteristic.BIA_STREAM)
        val validationAwaited = async {
            transport.incoming.first { it.characteristic == HuaweiCharacteristic.BIA_STREAM }
        }
        // S'abonner ne suffit pas : le flux doit être armé pour que la balance émette.
        transport.writeRaw(HuaweiCharacteristic.BIA_STREAM, HuaweiCommands.QUERY)

        advance() // Relevé de validation, puis acquittement
        // ⚠️ Attente **courte** et volontairement distincte de celle de la tare : cette trame
        // est un bonus, pas une condition. Rien n'établit que tout matériel en produise une
        // pendant l'appairage — `TECH.md` §5 la montre, l'implémentation de référence s'arrête
        // à la tare. Lui accorder le délai d'attente de l'athlète ferait paraître l'appairage
        // bloqué deux minutes durant, pour une valeur facultative.
        val validation = withTimeoutOrNull(VALIDATION_TIMEOUT_MS) { validationAwaited.await() }
            .also { if (it == null) validationAwaited.cancel() }
        val telemetry = validation
            ?.let { HuaweiCrypto.decrypt(sessionKey, it.payload) }
            ?.let { HuaweiTelemetryDecoder.decode(it, model) }
            ?.telemetry

        if (telemetry != null) {
            writeSealed(
                sessionKey,
                HuaweiCharacteristic.USER_PROFILE,
                HuaweiPayloads.userProfile(
                    huid = huid,
                    profile = profile,
                    kind = HuaweiPayloads.ProfileKind.MEASUREMENT_COMMIT,
                    weightKg = telemetry.massKg
                )
            )
        }

        emit(
            PairingState.Completed(
                ScaleAssociation(
                    deviceAddress = deviceAddress,
                    huid = huid,
                    tareKg = tareKg,
                    advertisedName = advertisedName,
                    capability = model.capability
                ),
                validation = telemetry
            )
        )
        }
    }

    /**
     * La balance renvoie la tare sur la caractéristique de gravure, une fois l'athlète monté.
     *
     * C'est **l'acquittement de la gravure autant que la mesure** : recevoir cette valeur
     * prouve que l'emplacement a bien été écrit.
     */
    private suspend fun awaitTare(
        sessionKey: ByteArray,
        awaited: Deferred<ScaleNotification>
    ): Double? {
        val notification = withTimeoutOrNull(athleteTimeoutMs) { awaited.await() }
        if (notification == null) {
            awaited.cancel()
            return null
        }

        val clear = HuaweiCrypto.decrypt(sessionKey, notification.payload) ?: return null
        if (clear.size < STATUS_BYTES + WEIGHT_BYTES) return null

        // La réponse s'ouvre sur un octet de statut, et le poids ne vient qu'ensuite. Le lire
        // dès le premier octet mêlait le statut à la moitié basse du poids, ce qui donnait une
        // tare absurde sans que rien ne le signale.
        val status = clear[0].toInt() and 0xFF
        if (status != STATUS_OK) {
            Log.w(TAG, "gravure refusée par la balance (statut $status)")
            return null
        }

        val hundredths = (clear[1].toInt() and 0xFF) or ((clear[2].toInt() and 0xFF) shl 8)
        // Un zéro n'est pas une tare : c'est l'absence de pesée.
        return (hundredths / 100.0).takeIf { it > 0.0 }
    }

    private suspend fun writeSealed(
        sessionKey: ByteArray,
        characteristic: HuaweiCharacteristic,
        payload: ByteArray
    ): Boolean {
        val iv = ByteArray(HuaweiCrypto.IV_BYTES).also(random::nextBytes)
        return transport.write(characteristic, HuaweiCrypto.encrypt(sessionKey, iv, payload))
    }

    companion object {
        private const val TAG = "BodyForgerBle"

        /** La réponse s'ouvre sur un octet de statut ; zéro vaut acceptation. */
        private const val STATUS_BYTES = 1
        private const val WEIGHT_BYTES = 2
        private const val STATUS_OK = 0

        /** Champ HUID de la trame de gravure : trente octets, complétés de zéros. */
        const val HUID_FIELD_BYTES = 30

        /** L'athlète doit se déchausser et monter : la référence attend vingt-cinq secondes. */
        const val DEFAULT_TARE_TIMEOUT_MS = 120_000L

        /**
         * Attente de la trame de validation, facultative. L'athlète est déjà sur la balance
         * et la mesure d'impédance suit la stabilisation de quelques secondes ; au-delà, elle
         * ne viendra pas, et l'appairage n'a pas à l'attendre.
         */
        const val VALIDATION_TIMEOUT_MS = 20_000L
    }
}
