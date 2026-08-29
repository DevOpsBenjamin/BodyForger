package app.bodyforger.core.ble.huawei

import app.bodyforger.core.ble.ScaleTransport
import app.bodyforger.core.ble.SessionFailure
import app.bodyforger.core.ble.WeighInState
import app.bodyforger.core.model.ScaleAssociation
import app.bodyforger.core.model.ScaleUserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeoutOrNull
import java.security.SecureRandom
import java.time.LocalDateTime

/**
 * A weigh-in from waking the scale to the telemetry frame — `docs/BLE_PROTOCOL.md` §6.
 *
 * The flow ends on [WeighInState.Completed] or [WeighInState.Failed], and emits nothing after.
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
        coroutineScope {
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
            return@coroutineScope
        }

        advance() // Handshake
        val sessionKey = HuaweiHandshake(transport, model, random).negotiate(association.deviceAddress)
        if (sessionKey == null) {
            emit(WeighInState.Failed(SessionFailure.REJECTED_BY_DEVICE))
            return@coroutineScope
        }

        advance() // Synchronisation de l'heure
        if (!transport.write(HuaweiCharacteristic.TIME_SYNC, HuaweiPayloads.currentTime(clock()))) {
            emit(WeighInState.Failed(SessionFailure.CONNECTION_LOST))
            return@coroutineScope
        }

        advance() // Profil utilisateur
        val sent = writeSealed(
            sessionKey,
            HuaweiCharacteristic.USER_PROFILE,
            HuaweiPayloads.userProfile(huid, profile)
        )
        if (!sent) {
            emit(WeighInState.Failed(SessionFailure.REJECTED_BY_DEVICE))
            return@coroutineScope
        }

        advance() // Armement du flux de télémétrie
        if (!transport.subscribe(HuaweiCharacteristic.BIA_STREAM)) {
            emit(WeighInState.Failed(SessionFailure.CONNECTION_LOST))
            return@coroutineScope
        }

        advance() // Balance prête : l'athlète peut monter

        //
        val frame = run {
            val awaited = async {
                transport.incoming.first { it.characteristic == HuaweiCharacteristic.BIA_STREAM }
            }
            if (!transport.writeRaw(HuaweiCharacteristic.BIA_STREAM, HuaweiCommands.QUERY)) {
                awaited.cancel()
                return@run null
            }
            withTimeoutOrNull(athleteTimeoutMs) { awaited.await() }.also { if (it == null) awaited.cancel() }
        }
        if (frame == null) {
            emit(WeighInState.Failed(SessionFailure.TIMED_OUT))
            return@coroutineScope
        }

        advance()

        val clear = HuaweiCrypto.decrypt(sessionKey, frame.payload)
        val decoded = clear?.let { HuaweiTelemetryDecoder.decode(it, model) }
        if (decoded == null) {
            emit(WeighInState.Failed(SessionFailure.DEVICE_ERROR))
            return@coroutineScope
        }

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
        /** Undressing and stabilising takes minutes, not protocol milliseconds. */
        const val DEFAULT_ATHLETE_TIMEOUT_MS = 120_000L
    }
}
