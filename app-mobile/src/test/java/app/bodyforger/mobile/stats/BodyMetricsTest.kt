package app.bodyforger.mobile.stats

import app.bodyforger.core.model.BodyLog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * The dashboard reads these. Every one of them must be able to say "not enough history"
 * rather than produce a figure that looks measured.
 */
class BodyMetricsTest {

    private val now = 1_757_000_000_000L
    private val day = 86_400_000L

    private fun log(daysAgo: Long, massKg: Double) = BodyLog(
        id = "log-$daysAgo",
        dateIso = "2026-09-01",
        measuredAtEpochMs = now - daysAgo * day,
        massKg = massKg,
        bodyFatPercentage = null
    )

    @Test
    fun `no weigh-in at all yields no median`() {
        assertNull(BodyMetrics.medianMassKg(emptyList(), now))
    }

    @Test
    fun `a weigh-in older than the window does not count`() {
        assertNull(BodyMetrics.medianMassKg(listOf(log(daysAgo = 9, massKg = 80.0)), now))
    }

    @Test
    fun `an odd number of readings takes the middle one`() {
        val logs = listOf(log(1, 79.0), log(2, 83.0), log(3, 81.0))
        assertEquals(81.0, BodyMetrics.medianMassKg(logs, now)!!, 1e-9)
    }

    @Test
    fun `an even number of readings averages the two middle ones`() {
        val logs = listOf(log(1, 79.0), log(2, 80.0), log(3, 82.0), log(4, 90.0))
        assertEquals(81.0, BodyMetrics.medianMassKg(logs, now)!!, 1e-9)
    }

    @Test
    fun `the median ignores an outlier an average would follow`() {
        val logs = listOf(log(1, 80.0), log(2, 80.2), log(3, 95.0))
        // The mean would read 85.07 — a figure the athlete never weighed.
        assertEquals(80.2, BodyMetrics.medianMassKg(logs, now)!!, 1e-9)
    }

    @Test
    fun `a history shorter than the month yields no monthly trend`() {
        val logs = listOf(log(1, 80.0), log(6, 81.0))
        assertNull(BodyMetrics.massDeltaKg(logs, now))
    }

    @Test
    fun `the trend measures against the latest reading old enough to anchor it`() {
        val logs = listOf(log(0, 79.0), log(31, 81.5), log(80, 90.0))
        assertEquals(-2.5, BodyMetrics.massDeltaKg(logs, now)!!, 1e-9)
    }

    @Test
    fun `a single reading anchors nothing`() {
        assertNull(BodyMetrics.massDeltaKg(listOf(log(0, 79.0)), now))
    }
}
