package app.bodyforger.core.ble

import android.util.Log
import app.bodyforger.core.ble.huawei.HuaweiCharacteristic
import app.bodyforger.core.ble.huawei.HuaweiFrameReassembler
import app.bodyforger.core.ble.huawei.HuaweiGattProfile
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.UUID

/**
 * Reassembles incoming frames and exposes whole payloads.
 *
 * One reassembler per characteristic, since several may emit in parallel.
 */
internal class GattFrameInbox(
    private val profile: HuaweiGattProfile,
    private val tag: String
) {

    private val reassemblers = mutableMapOf<HuaweiCharacteristic, HuaweiFrameReassembler>()

    private val notifications = MutableSharedFlow<ScaleNotification>(
        replay = 0,
        extraBufferCapacity = NOTIFICATION_BUFFER,
        onBufferOverflow = BufferOverflow.SUSPEND
    )

    val incoming: Flow<ScaleNotification> = notifications.asSharedFlow()

    fun open(characteristic: HuaweiCharacteristic) {
        reassemblers[characteristic] = HuaweiFrameReassembler()
    }

    fun close(characteristic: HuaweiCharacteristic) {
        reassemblers.remove(characteristic)
    }

    fun clear() = reassemblers.clear()

    /**
     * Feeds one received frame, emitting only once a payload is complete.
     *
     * Emission is non-suspending: this runs inside a Bluetooth stack callback, which must
     * never block.
     */
    fun deliver(uuid: UUID, frame: ByteArray) {
        val characteristic = profile.characteristicOf(uuid) ?: return
        val reassembler = reassemblers.getOrPut(characteristic) { HuaweiFrameReassembler() }

        val payload = reassembler.feed(frame)
        if (payload == null) {
            logRejection(characteristic, reassembler.lastRejection, frame)
            return
        }

        Log.d(tag, "received $characteristic: ${payload.size} B — ${payload.toDiagnosticHex()}")
        notifications.tryEmit(
            ScaleNotification(
                characteristic = characteristic,
                payload = payload,
                encrypted = characteristic.protection != HuaweiCharacteristic.Protection.CLEAR
            )
        )
    }

    private fun logRejection(
        characteristic: HuaweiCharacteristic,
        reason: String?,
        frame: ByteArray
    ) = when {
        reason == null ->
            Log.v(tag, "intermediate frame on $characteristic (${frame.size} B)")
        // This characteristic answers in its own format, outside the framing layer.
        characteristic == HuaweiCharacteristic.CAPABILITIES_RESPONSE ->
            Log.v(tag, "capability answer outside framing (${frame.size} B)")
        else ->
            Log.w(tag, "frame discarded on $characteristic — $reason — ${frame.toDiagnosticHex()}")
    }

    private companion object {
        const val NOTIFICATION_BUFFER = 32
    }
}
