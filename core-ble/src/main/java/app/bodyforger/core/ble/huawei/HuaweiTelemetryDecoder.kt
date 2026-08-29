package app.bodyforger.core.ble.huawei

import app.bodyforger.core.ble.BiaTelemetry
import app.bodyforger.core.model.ImpedancePath
import app.bodyforger.core.model.ImpedanceReading
import app.bodyforger.core.model.RawImpedances
import java.time.LocalDateTime

/** A decoded Haige telemetry frame, plus the family-specific field the generic model omits. */
data class HuaweiTelemetryFrame(
    val telemetry: BiaTelemetry,
    /** Raw status byte, exposed without interpretation — `docs/BLE_PROTOCOL.md` §9. */
    val statusByte: Int
)

/**
 * Decoder for the Haige real-time bio-impedance frame.
 *
 * Layout: `docs/BLE_PROTOCOL.md` §9.
 */
object HuaweiTelemetryDecoder {

    /** Below this, the frame does not even carry the low-frequency block. */
    const val MIN_FRAME_BYTES = 26

    /** From this length on, the high-frequency block is present. */
    const val DUAL_FREQUENCY_FRAME_BYTES = 38

    private const val MASS_OFFSET = 0
    private const val BODY_FAT_OFFSET = 2
    private const val YEAR_OFFSET = 4
    private const val LOW_FREQUENCY_BLOCK_OFFSET = 12
    private const val HIGH_FREQUENCY_BLOCK_OFFSET = 26
    private const val HEART_RATE_OFFSET = 24
    private const val STATUS_BYTE_OFFSET = 11

    /** Outside this range a heart rate is treated as absent. */
    private const val MASS_SCALE = 100.0
    private const val BODY_FAT_SCALE = 10.0

    private val PLAUSIBLE_HEART_RATE = 1..240

    /**
     * Decodes a decrypted frame, or `null` when it is too short to interpret.
     *
     * [model] is required because the ohm scale factor is a property of the hardware, not of
     * the protocol.
     */
    fun decode(payload: ByteArray, model: HuaweiScaleModel): HuaweiTelemetryFrame? {
        if (payload.size < MIN_FRAME_BYTES) return null

        val readings = buildMap {
            putBlock(payload, LOW_FREQUENCY_BLOCK_OFFSET, ImpedanceReading.LOW_FREQUENCY_KHZ, model)
            if (payload.size >= DUAL_FREQUENCY_FRAME_BYTES) {
                putBlock(payload, HIGH_FREQUENCY_BLOCK_OFFSET, ImpedanceReading.HIGH_FREQUENCY_KHZ, model)
            }
        }

        return HuaweiTelemetryFrame(
            telemetry = BiaTelemetry(
                massKg = u16(payload, MASS_OFFSET) / MASS_SCALE,
                bodyFatPercentage = u16(payload, BODY_FAT_OFFSET).takeIf { it > 0 }?.let { it / BODY_FAT_SCALE },
                heartRateBpm = u16(payload, HEART_RATE_OFFSET).takeIf { it in PLAUSIBLE_HEART_RATE },
                measuredAt = readTimestamp(payload),
                rawImpedances = RawImpedances.of(readings)
            ),
            statusByte = payload[STATUS_BYTE_OFFSET].toInt() and 0xFF
        )
    }

    /** Reads one frequency block; a zero counter means not measured and is omitted. */
    private fun MutableMap<ImpedanceReading, Double>.putBlock(
        payload: ByteArray,
        offset: Int,
        frequencyKHz: Int,
        model: HuaweiScaleModel
    ) {
        for (path in ImpedancePath.BY_WIRE_INDEX) {
            val raw = u16(payload, offset + path.wireIndex * 2)
            if (raw > 0) {
                put(ImpedanceReading(path, frequencyKHz), raw / model.impedanceOhmDivisor)
            }
        }
    }

    private fun readTimestamp(payload: ByteArray): LocalDateTime? {
        val year = u16(payload, YEAR_OFFSET)
        if (year < 2000) return null
        return runCatching {
            LocalDateTime.of(
                year,
                payload[6].toInt() and 0xFF,
                payload[7].toInt() and 0xFF,
                payload[8].toInt() and 0xFF,
                payload[9].toInt() and 0xFF,
                payload[10].toInt() and 0xFF
            )
        }.getOrNull()
    }

    private fun u16(payload: ByteArray, offset: Int): Int =
        (payload[offset].toInt() and 0xFF) or ((payload[offset + 1].toInt() and 0xFF) shl 8)
}
