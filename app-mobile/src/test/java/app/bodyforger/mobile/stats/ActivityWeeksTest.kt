package app.bodyforger.mobile.stats

import app.bodyforger.core.model.WorkoutSession
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * The activity grid counts sessions per week over a year, so the shade of a cell has to follow
 * how busy that week was, and the last cell has to be the week in progress.
 */
class ActivityWeeksTest {

    /** Monday 14 September 2026. */
    private val today: LocalDate = LocalDate.of(2026, 9, 14)

    private fun epochOf(date: LocalDate): Long =
        date.atTime(LocalTime.NOON).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun session(date: LocalDate) =
        WorkoutSession(title = "Session", startedAtEpochMs = epochOf(date))

    @Test
    fun `the last cell is the week in progress`() {
        val counts = TrainingStats.weeklySessionCounts(listOf(session(today)), epochOf(today), weeks = 52)

        assertEquals(52, counts.size)
        assertEquals(1, counts.last())
    }

    @Test
    fun `every day of one week counts into the same cell`() {
        val week = listOf(
            LocalDate.of(2026, 9, 7),   // Monday
            LocalDate.of(2026, 9, 9),   // Wednesday
            LocalDate.of(2026, 9, 11),  // Friday
            LocalDate.of(2026, 9, 13),  // Sunday
        ).map(::session)

        val counts = TrainingStats.weeklySessionCounts(week, epochOf(today), weeks = 4)

        assertEquals(4, counts[2])
        assertEquals(0, counts[3])
    }

    @Test
    fun `an untrained week is zero rather than missing`() {
        val counts = TrainingStats.weeklySessionCounts(emptyList(), epochOf(today), weeks = 6)

        assertEquals(List(6) { 0 }, counts)
    }

    @Test
    fun `a session older than the window is left out`() {
        val longAgo = session(today.minusWeeks(60))

        val counts = TrainingStats.weeklySessionCounts(listOf(longAgo), epochOf(today), weeks = 52)

        assertEquals(0, counts.sum())
    }
}
