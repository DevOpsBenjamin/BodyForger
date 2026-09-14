package app.bodyforger.mobile.stats

import app.bodyforger.core.model.WorkoutSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * The activity grid has to be a calendar, not a run of days: its whole point is that a column
 * is a week and a row is a weekday.
 */
class ActivityWeeksTest {

    /** Monday 14 September 2026. */
    private val today: LocalDate = LocalDate.of(2026, 9, 14)

    private fun epochOf(date: LocalDate): Long =
        date.atTime(LocalTime.NOON).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun session(date: LocalDate) =
        WorkoutSession(title = "Session", startedAtEpochMs = epochOf(date))

    @Test
    fun `a column is a week and a row is a weekday`() {
        val weeks = TrainingStats.activityWeeks(listOf(session(today)), epochOf(today), weeks = 4)

        assertEquals(4, weeks.size)
        weeks.forEach { assertEquals(TrainingStats.DAYS_IN_A_WEEK, it.size) }
        // Today is a Monday, so it lands on the first row of the last column.
        assertTrue(weeks.last()[0])
        assertFalse(weeks.last()[1])
    }

    @Test
    fun `the same weekday stays on the same row across weeks`() {
        val wednesdays = listOf(
            LocalDate.of(2026, 9, 2),
            LocalDate.of(2026, 9, 9),
        ).map(::session)

        val weeks = TrainingStats.activityWeeks(wednesdays, epochOf(today), weeks = 3)

        // Wednesday is the third row, in both of the weeks that hold one.
        assertTrue(weeks[0][2])
        assertTrue(weeks[1][2])
        assertFalse(weeks[2][2])
    }

    @Test
    fun `a day outside the window is left out rather than folded in`() {
        val longAgo = session(today.minusWeeks(10))

        val weeks = TrainingStats.activityWeeks(listOf(longAgo), epochOf(today), weeks = 4)

        assertTrue(weeks.all { week -> week.none { it } })
    }

    @Test
    fun `the current week is padded to Sunday so today keeps its weekday`() {
        val friday = LocalDate.of(2026, 9, 11)

        val weeks = TrainingStats.activityWeeks(listOf(session(friday)), epochOf(friday), weeks = 2)

        // Friday is the fifth row, and the two days after it exist but are untrained.
        assertTrue(weeks.last()[4])
        assertFalse(weeks.last()[5])
        assertFalse(weeks.last()[6])
    }
}
