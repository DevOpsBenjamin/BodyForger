package app.bodyforger.mobile.stats

import app.bodyforger.core.model.BodyLog
import java.time.Instant
import java.time.ZoneId

/**
 * What a weigh-in history adds up to.
 *
 * Every figure here returns `null` when the history cannot support it. A trend needs two
 * points at a known distance; inventing one would put a number on screen that reads exactly
 * like a measurement.
 */
object BodyMetrics {

    const val MEDIAN_WINDOW_DAYS = 7L
    const val MONTH_WINDOW_DAYS = 30L

    /**
     * Median mass over the last [days], or `null` when nothing was weighed in that window.
     *
     * The median rather than the mean because a single bad contact, a heavy meal or a change
     * of clothes moves an average and barely moves a median.
     *
     * One reading in the window is its own median. That is not a fabrication — it is the
     * median of what exists — but it carries none of the noise resistance the median is for.
     */
    fun medianMassKg(logs: List<BodyLog>, nowEpochMs: Long, days: Long = MEDIAN_WINDOW_DAYS): Double? {
        val since = nowEpochMs - days * MILLIS_PER_DAY
        val masses = logs.filter { it.measuredAtEpochMs in since..nowEpochMs }
            .map { it.massKg }
            .sorted()

        if (masses.isEmpty()) return null

        val middle = masses.size / 2
        return if (masses.size % 2 == 1) {
            masses[middle]
        } else {
            (masses[middle - 1] + masses[middle]) / 2.0
        }
    }

    /**
     * Mass gained or lost over the last [days], or `null` when the history does not reach back
     * that far.
     *
     * Measured between the latest reading and the latest one at least [days] old. An athlete
     * who started weighing in a week ago has no monthly trend, and is shown none rather than a
     * difference against their first ever reading dressed up as a month.
     */
    fun massDeltaKg(logs: List<BodyLog>, nowEpochMs: Long, days: Long = MONTH_WINDOW_DAYS): Double? {
        val latest = logs.maxByOrNull { it.measuredAtEpochMs } ?: return null
        val threshold = nowEpochMs - days * MILLIS_PER_DAY
        val reference = logs
            .filter { it.measuredAtEpochMs <= threshold }
            .maxByOrNull { it.measuredAtEpochMs }
            ?: return null

        return latest.massKg - reference.massKg
    }

    /** The most recent reading, whatever it measured. */
    fun latest(logs: List<BodyLog>): BodyLog? = logs.maxByOrNull { it.measuredAtEpochMs }

    private const val MILLIS_PER_DAY = 86_400_000L
}

/** The local date a reading was taken, for grouping by day or week. */
internal fun BodyLog.localDate(zone: ZoneId = ZoneId.systemDefault()) =
    Instant.ofEpochMilli(measuredAtEpochMs).atZone(zone).toLocalDate()
