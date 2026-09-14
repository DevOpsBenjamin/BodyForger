package app.bodyforger.mobile.mcp

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * The window rules that let a past workout be imported hour by hour, without letting a wide
 * request drag the whole history into memory.
 */
class HeartRateWindowTest {

    private val now: Instant = Instant.parse("2026-09-14T12:00:00Z")

    @Test
    fun `explicit window is honoured`() {
        val window = HeartRateWindow.resolve(
            JSONObject().apply {
                put("startTime", "2026-09-08T05:40:00Z")
                put("endTime", "2026-09-08T06:30:00Z")
            },
            now
        )

        assertEquals(Instant.parse("2026-09-08T05:40:00Z"), window?.start)
        assertEquals(Instant.parse("2026-09-08T06:30:00Z"), window?.end)
    }

    @Test
    fun `daysBack remains the shorthand when no start is given`() {
        val window = HeartRateWindow.resolve(JSONObject().apply { put("daysBack", 3) }, now)

        assertEquals(now.minus(3, ChronoUnit.DAYS), window?.start)
        assertEquals(now, window?.end)
    }

    @Test
    fun `a start without an end reads up to now`() {
        val window = HeartRateWindow.resolve(
            JSONObject().apply { put("startTime", "2026-09-13T08:00:00Z") },
            now
        )

        assertEquals(Instant.parse("2026-09-13T08:00:00Z"), window?.start)
        assertEquals(now, window?.end)
    }

    @Test
    fun `a window wider than the cap is clamped to the most recent slice`() {
        val window = HeartRateWindow.resolve(
            JSONObject().apply { put("startTime", "2025-01-01T00:00:00Z") },
            now
        )

        assertEquals(
            now.minus(HealthConnectToolHandler.MAX_WINDOW_DAYS.toLong(), ChronoUnit.DAYS),
            window?.start
        )
    }

    @Test
    fun `an inverted window is refused rather than silently swapped`() {
        assertNull(
            HeartRateWindow.resolve(
                JSONObject().apply {
                    put("startTime", "2026-09-08T06:30:00Z")
                    put("endTime", "2026-09-08T05:40:00Z")
                },
                now
            )
        )
    }

    @Test
    fun `an empty window is refused`() {
        assertNull(
            HeartRateWindow.resolve(
                JSONObject().apply {
                    put("startTime", "2026-09-08T06:00:00Z")
                    put("endTime", "2026-09-08T06:00:00Z")
                },
                now
            )
        )
    }

    @Test
    fun `an unparsable instant falls back to the daysBack shorthand`() {
        val window = HeartRateWindow.resolve(
            JSONObject().apply {
                put("startTime", "8 sept. 2026, 07:44")
                put("daysBack", 2)
            },
            now
        )

        assertEquals(now.minus(2, ChronoUnit.DAYS), window?.start)
    }
}
