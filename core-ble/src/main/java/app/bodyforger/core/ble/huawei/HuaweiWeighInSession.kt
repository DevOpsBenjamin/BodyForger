package app.bodyforger.core.ble.huawei

import app.bodyforger.core.ble.ScaleTransport
import app.bodyforger.core.ble.SessionFailure
import app.bodyforger.core.ble.WeighInState
import app.bodyforger.core.model.ScaleAssociation
import app.bodyforger.core.model.ScaleUserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeoutOrNull
import java.security.SecureRandom
import java.time.LocalDateTime

/**
 * Une pesée de bout en bout, du réveil de la balance à la trame de télémétrie.
 *
 * Le flux émet la progression pas à pas, puis se termine sur [WeighInState.Completed] ou
 * [WeighInState.Failed]. Il n'émet jamais rien après l'un des deux.
 *
 * L'attente de l'athlète est bien plus longue que les autres : il doit se déshabiller,
 * monter, se stabiliser. Un délai calqué sur les échanges protocolaires échouerait sur une
 * personne simplement lente.
 */
class HuaweiWeighInSession(
    private val transport: ScaleTransport,
    private val model: HuaweiScaleModel,
    private val clock: () -> LocalDateTime = LocalDateTime::now,
    private val random: SecureRandom = SecureRandom(),
    private val athleteTimeoutMs: Long = DEFAULT_ATHLETE_TIMEOUT_MS
) {

    fun run(
        association: ScaleAssociation,
        huid: String,
        profile: ScaleUserProfile
    ): Flow<WeighInState> = flow {
        val steps = HuaweiWeighInSequence.stepsFor(model)
        var index = 0
        suspend fun advance() {
            val step = steps[index]
            emit(WeighInState.Progress(index, steps.size, step.phase, step.instructions, step.detail))
            index++
        }

        advance() // Réveil et scan ciblé
        if (!transport.connect()) {
            emit(WeighInState.Failed(SessionFailure.CONNECTION_LOST))
            return@flow
        }

        advance() // Handshake
        val sessionKey = HuaweiHandshake(transport, model, random).negotiate(association.deviceAddress)
        if (sessionKey == null) {
            // Un refus n'est pas diagnosticable : clé racine fausse, appareil usurpé ou
            // liaison perdue se ressemblent tous.
            emit(WeighInState.Failed(SessionFailure.REJECTED_BY_DEVICE))
            return@flow
        }

        advance() // Synchronisation de l'heure
        if (!transport.write(HuaweiCharacteristic.TIME_SYNC, HuaweiPayloads.currentTime(clock()))) {
            emit(WeighInState.Failed(SessionFailure.CONNECTION_LOST))
            return@flow
        }

        advance() // Profil utilisateur
        val sent = writeSealed(
            sessionKey,
            HuaweiCharacteristic.USER_PROFILE,
            HuaweiPayloads.userProfile(huid, profile)
        )
        if (!sent) {
            emit(WeighInState.Failed(SessionFailure.REJECTED_BY_DEVICE))
            return@flow
        }

        advance() // Armement du flux de télémétrie
        if (!transport.subscribe(HuaweiCharacteristic.BIA_STREAM)) {
            emit(WeighInState.Failed(SessionFailure.CONNECTION_LOST))
            return@flow
        }
        // S'abonner ne suffit pas : la balance n'émet rien tant que le flux n'est pas armé.
        if (!transport.writeRaw(HuaweiCharacteristic.BIA_STREAM, HuaweiCommands.QUERY)) {
            emit(WeighInState.Failed(SessionFailure.CONNECTION_LOST))
            return@flow
        }

        advance() // Balance prête : l'athlète peut monter

        advance() // Stabilisation et relevé
        val frame = withTimeoutOrNull(athleteTimeoutMs) {
            transport.incoming.first { it.characteristic == HuaweiCharacteristic.BIA_STREAM }
        }
        if (frame == null) {
            emit(WeighInState.Failed(SessionFailure.TIMED_OUT))
            return@flow
        }

        val clear = HuaweiCrypto.decrypt(sessionKey, frame.payload)
        val decoded = clear?.let { HuaweiTelemetryDecoder.decode(it, model) }
        if (decoded == null) {
            emit(WeighInState.Failed(SessionFailure.DEVICE_ERROR))
            return@flow
        }

        // La balance attend son acquittement pour clore la session ; sans lui elle reste
        // armée et l'athlète devra attendre son extinction avant la pesée suivante.
        writeSealed(
            sessionKey,
            HuaweiCharacteristic.USER_PROFILE,
            HuaweiPayloads.userProfile(
                huid = huid,
                profile = profile,
                kind = HuaweiPayloads.ProfileKind.MEASUREMENT_COMMIT,
                weightKg = decoded.telemetry.massKg
            )
        )

        emit(WeighInState.Completed(decoded.telemetry))
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
        /**
         * Se déshabiller, monter, se stabiliser : deux minutes ne sont pas de trop. Un délai
         * calqué sur les échanges protocolaires échouerait sur une personne simplement lente.
         */
        const val DEFAULT_ATHLETE_TIMEOUT_MS = 120_000L
    }
}
