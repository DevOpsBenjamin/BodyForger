package app.bodyforger.mobile.mcp

import org.json.JSONObject
import java.time.Instant
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

/**
 * The time window a heart rate read covers.
 *
 * Importing a past workout means asking for that hour by name, so an explicit `startTime` /
 * `endTime` pair wins; `daysBack` remains the shorthand for recent data. The span is capped:
 * a request wider than [HealthConnectToolHandler.MAX_WINDOW_DAYS] is clamped to the most recent
 * slice rather than refused, and the caller is told which window was actually read.
 */
internal data class HeartRateWindow(
    val start: Instant,
    val end: Instant,
    val daysBack: Int
) {
    companion object {
        /** Returns null when the requested window is empty or inverted. */
        fun resolve(arguments: JSONObject, now: Instant): HeartRateWindow? {
            val end = parseInstant(arguments.optString("endTime", "")) ?: now
            val days = arguments.optInt("daysBack", 1)
                .coerceIn(1, HealthConnectToolHandler.MAX_WINDOW_DAYS)
            val requestedStart = parseInstant(arguments.optString("startTime", ""))
                ?: end.minus(days.toLong(), ChronoUnit.DAYS)
            if (!requestedStart.isBefore(end)) return null

            val cap = end.minus(HealthConnectToolHandler.MAX_WINDOW_DAYS.toLong(), ChronoUnit.DAYS)
            val start = if (requestedStart.isBefore(cap)) cap else requestedStart
            return HeartRateWindow(start, end, days)
        }

        private fun parseInstant(raw: String): Instant? {
            val trimmed = raw.trim()
            if (trimmed.isEmpty()) return null
            return try {
                Instant.parse(trimmed)
            } catch (_: DateTimeParseException) {
                null
            }
        }
    }
}
