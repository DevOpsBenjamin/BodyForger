package app.bodyforger.core.ble

import app.bodyforger.core.ble.huawei.HuaweiCharacteristic
import kotlinx.coroutines.flow.Flow

/**
 * What a transport must offer for a driver to talk to a scale.
 *
 * This contract exists so the protocol orchestration — the part that matters, and that gets
 * things wrong — stays verifiable without hardware.
 */
interface ScaleTransport {

    /** Incoming payloads, already reassembled and attributed to a characteristic. */
    val incoming: Flow<ScaleNotification>

    /** Connects and discovers services. */
    suspend fun connect(): Boolean

    /**
     * Enables notifications on a characteristic.
     *
     * Most answers arrive as notifications rather than write returns, so writing before
     * subscribing means talking into the void — `docs/BLE_PROTOCOL.md` §7.
     */
    suspend fun subscribe(characteristic: HuaweiCharacteristic): Boolean

    suspend fun unsubscribe(characteristic: HuaweiCharacteristic): Boolean

    /** Writes a payload, splitting it into frames as needed. */
    suspend fun write(
        characteristic: HuaweiCharacteristic,
        payload: ByteArray,
        withResponse: Boolean = true
    ): Boolean

    /**
     * Writes bytes as they are, without framing.
     *
     * Some fixed commands are already complete frames; framing them again produces a frame
     * inside a frame — `docs/BLE_PROTOCOL.md` §3.
     */
    suspend fun writeRaw(
        characteristic: HuaweiCharacteristic,
        frame: ByteArray,
        withResponse: Boolean = true
    ): Boolean

    /** Closes the connection and releases resources. Idempotent. */
    fun close()
}

/** A complete payload received from a characteristic, as the scale emitted it. */
data class ScaleNotification(
    val characteristic: HuaweiCharacteristic,
    val payload: ByteArray,
    /** True when the payload is session-encrypted and still to be decrypted. */
    val encrypted: Boolean
) {
    override fun equals(other: Any?): Boolean = this === other || (other is ScaleNotification &&
        characteristic == other.characteristic && encrypted == other.encrypted &&
        payload.contentEquals(other.payload))

    override fun hashCode(): Int = 31 * characteristic.hashCode() + payload.contentHashCode()
}
