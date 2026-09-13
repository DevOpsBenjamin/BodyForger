package app.bodyforger.core.ble

import app.bodyforger.core.model.RawImpedances
import java.time.LocalDateTime

/**
 * What a reading delivered, whatever hardware produced it.
 *
 * Anything the device did not measure is `null` — never a zero, never a default.
 */
data class BiaTelemetry(
    val massKg: Double,
    val bodyFatPercentage: Double?,
    val heartRateBpm: Int?,
    val measuredAt: LocalDateTime?,
    val rawImpedances: RawImpedances
)
