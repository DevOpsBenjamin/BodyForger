package app.bodyforger.mobile.stats

import app.bodyforger.core.model.WorkoutSession
import app.bodyforger.core.model.WorkoutSessionStatus
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

/**
 * The streak on the profile screen.
 *
 * A run of weeks is easy to compute and easy to compute wrongly: the interesting cases are the
 * empty history, the gap, and the Monday morning where nothing has happened yet.
 */
class ConsecutiveWeeksTest {

    private val zone: ZoneId = ZoneId.systemDefault()

    /** A Wednesday, so that "earlier this week" and "later this week" both exist. */
    private val today: LocalDate = LocalDate.of(2026, 9, 9)
    private val todayEpochMs = today.atStartOfDay(zone).toInstant().toEpochMilli()

    private fun sessionWeeksAgo(weeks: Long): WorkoutSession {
        val date = today.with(DayOfWeek.MONDAY).minusWeeks(weeks).plusDays(1)
        return WorkoutSession(
            id = "s-$weeks",
            title = "session",
            startedAtEpochMs = date.atStartOfDay(zone).toInstant().toEpochMilli(),
            status = WorkoutSessionStatus.COMPLETED
        )
    }

    @Test
    fun `no session at all is no streak`() {
        assertEquals(0, TrainingStats.consecutiveTrainingWeeks(emptyList(), todayEpochMs))
    }

    @Test
    fun `three unbroken weeks count three`() {
        val sessions = listOf(sessionWeeksAgo(0), sessionWeeksAgo(1), sessionWeeksAgo(2))
        assertEquals(3, TrainingStats.consecutiveTrainingWeeks(sessions, todayEpochMs))
    }

    @Test
    fun `a missed week ends the run`() {
        val sessions = listOf(sessionWeeksAgo(0), sessionWeeksAgo(1), sessionWeeksAgo(3))
        assertEquals(2, TrainingStats.consecutiveTrainingWeeks(sessions, todayEpochMs))
    }

    @Test
    fun `nothing trained yet this week keeps what the previous ones earned`() {
        // The forgiving case: a streak that collapses every Monday would measure the calendar.
        val sessions = listOf(sessionWeeksAgo(1), sessionWeeksAgo(2))
        assertEquals(2, TrainingStats.consecutiveTrainingWeeks(sessions, todayEpochMs))
    }

    @Test
    fun `several sessions in one week still count that week once`() {
        val monday = today.with(DayOfWeek.MONDAY)
        val sessions = listOf(0L, 1L, 2L).map { offset ->
            WorkoutSession(
                id = "s-$offset",
                title = "session",
                startedAtEpochMs = monday.plusDays(offset).atStartOfDay(zone).toInstant().toEpochMilli(),
                status = WorkoutSessionStatus.COMPLETED
            )
        }
        assertEquals(1, TrainingStats.consecutiveTrainingWeeks(sessions, todayEpochMs))
    }

    @Test
    fun `a run that stopped long ago is not a current streak`() {
        val sessions = listOf(sessionWeeksAgo(5), sessionWeeksAgo(6))
        assertEquals(0, TrainingStats.consecutiveTrainingWeeks(sessions, todayEpochMs))
    }
}
