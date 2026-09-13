package app.bodyforger.mobile.onboarding

import app.bodyforger.mobile.navigation.Tab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The tour walks the bottom bar. A tab added to the bar and forgotten here would be a screen
 * the athlete is never shown, which is exactly what the tour exists to prevent.
 */
class ScreenTourTest {

    @Test
    fun `every tab of the bottom bar is a stop`() {
        val visited = TourStop.entries.mapNotNull { it.tab }

        assertEquals(Tab.entries, visited)
    }

    @Test
    fun `the stops follow the order of the bar, then settings`() {
        assertEquals(TourStop.SETTINGS, TourStop.entries.last())
        assertNull("Settings is reached by the gear, not by the bar", TourStop.SETTINGS.tab)
    }

    @Test
    fun `next chains every stop and stops at the end`() {
        var stop = TourStop.entries.first()
        var walked = 1

        while (true) {
            val next = stop.next() ?: break
            stop = next
            walked++
        }

        assertEquals(TourStop.entries.size, walked)
        assertTrue(stop.isLast)
    }

    @Test
    fun `only the last stop is the last one`() {
        assertEquals(1, TourStop.entries.count { it.isLast })
    }
}
