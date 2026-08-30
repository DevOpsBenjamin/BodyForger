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
 * Pairing, mode 1: engrave the athlete's profile into the scale's flash, then read the tare.
 *
 * ⚠️ Engraving precedes the weigh-in and consumes a memory slot for good — the HUID comes
 * from the caller and is never generated here. `docs/BLE_PROTOCOL.md` §5.
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
        transport.writeRaw(HuaweiCharacteristic.BIA_STREAM, HuaweiCommands.QUERY)

        advance() // Relevé de validation, puis acquittement
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

    /** Reads the tare from the engraving answer, which opens with a status byte. */
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

        val status = clear[0].toInt() and 0xFF
        if (status != STATUS_OK) {
            Log.w(TAG, "gravure refusée par la balance (statut $status)")
            return null
        }

        val hundredths = (clear[1].toInt() and 0xFF) or ((clear[2].toInt() and 0xFF) shl 8)
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

        /** Status byte; zero means accepted. */
        private const val STATUS_BYTES = 1
        private const val WEIGHT_BYTES = 2
        private const val STATUS_OK = 0

        /** HUID field of the engraving frame, zero-padded. */
        const val HUID_FIELD_BYTES = 30

        /** Undressing and stepping on takes time; a protocol-sized timeout would fail. */
        const val DEFAULT_TARE_TIMEOUT_MS = 120_000L

        /** The validation frame is a bonus, not a condition: a short, separate wait. */
        const val VALIDATION_TIMEOUT_MS = 20_000L
    }
}
