package app.bodyforger.core.ble.huawei

import app.bodyforger.core.model.BiologicalSex
import app.bodyforger.core.model.ScaleUserProfile
import java.time.LocalDateTime

/**
 * The payloads the scale expects, built byte by byte.
 *
 * Layouts: `docs/BLE_PROTOCOL.md` §9.
 */
object HuaweiPayloads {

    const val PROFILE_BYTES = 69
    private const val HUID_BYTES = 30
    private const val UID_BYTES = 32
    private const val UID_OFFSET = 30

    /** Profile kind: routine configuration, or acknowledgement of a completed reading. */
    enum class ProfileKind(val code: Int) {
        ROUTINE(0),

        MEASUREMENT_COMMIT(2)
    }

    /**
     * The user profile, 69 bytes in the clear.
     *
     * [weightKg] is the last known weight, left at zero when there is none.
     */
    fun userProfile(
        huid: String,
        profile: ScaleUserProfile,
        kind: ProfileKind = ProfileKind.ROUTINE,
        weightKg: Double? = profile.lastWeightKg
    ): ByteArray {
        val huidBytes = huid.toByteArray(Charsets.US_ASCII)
        require(huidBytes.size <= HUID_BYTES) { "HUID of ${huidBytes.size} bytes, $HUID_BYTES at most" }

        val physiology = profile.physiology
        require(physiology.ageYears in 0..255) { "Age out of range: ${physiology.ageYears}" }
        require(physiology.heightCm > 0) { "Invalid height: ${physiology.heightCm}" }

        val payload = ByteArray(PROFILE_BYTES)
        huidBytes.copyInto(payload)
        payload[UID_OFFSET + UID_BYTES] = if (physiology.sex == BiologicalSex.MALE) 1 else 0
        payload[63] = physiology.ageYears.toByte()
        payload.putUInt16LittleEndian(64, physiology.heightCm.toInt())
        payload.putUInt16LittleEndian(66, ((weightKg ?: 0.0) * 100).toInt().coerceIn(0, 0xFFFF))
        payload[68] = kind.code.toByte()
        return payload
    }

    /** Current time in the standard Bluetooth SIG format. */
    fun currentTime(now: LocalDateTime): ByteArray {
        val payload = ByteArray(10)
        payload.putUInt16LittleEndian(0, now.year)
        payload[2] = now.monthValue.toByte()
        payload[3] = now.dayOfMonth.toByte()
        payload[4] = now.hour.toByte()
        payload[5] = now.minute.toByte()
        payload[6] = now.second.toByte()
        payload[7] = now.dayOfWeek.value.toByte()
        payload[8] = 0 // fraction de seconde en 256e, sans objet ici
        payload[9] = 0 // raison de l'ajustement : aucune
        return payload
    }

    /** Arms or disarms binding mode. */
    fun bindingControl(armed: Boolean): ByteArray = byteArrayOf(if (armed) 1 else 0)

    private fun ByteArray.putUInt16LittleEndian(offset: Int, value: Int) {
        this[offset] = (value and 0xFF).toByte()
        this[offset + 1] = ((value shr 8) and 0xFF).toByte()
    }
}
