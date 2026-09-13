package app.bodyforger.mobile.stats

import app.bodyforger.core.model.BodyGoal
import app.bodyforger.core.model.BodyLog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

/**
 * The rules that decide when a milestone is reached.
 *
 * Every one of them is a deliberate choice with a way to get it wrong: too lenient and a lean
 * week ticks off a goal, too strict and a goal that was reached never validates. These pin
 * each rule to a case that would break if it moved.
 */
class GoalProgressTest {

    private val zone: ZoneId = ZoneId.systemDefault()

    /** A Wednesday, so a current week exists with days before and after. */
    private val today: LocalDate = LocalDate.of(2026, 9, 9)
    private val thisMonday: LocalDate = today.with(DayOfWeek.MONDAY)

    private fun logsInWeek(
        weeksAgo: Long,
        count: Int,
        massKg: Double,
        bodyFat: Double? = null
    ): List<BodyLog> {
        val monday = thisMonday.minusWeeks(weeksAgo)
        return (0 until count).map { day ->
            BodyLog(
                id = "w$weeksAgo-$day",
                dateIso = monday.plusDays(day.toLong()).toString(),
                measuredAtEpochMs = monday.plusDays(day.toLong())
                    .atStartOfDay(zone).toInstant().toEpochMilli(),
                massKg = massKg,
                bodyFatPercentage = bodyFat
            )
        }
    }

    private fun goal(target: Double = 76.0, from: Double? = 80.0, targetFat: Double? = null, fromFat: Double? = null) =
        BodyGoal(
            targetMassKg = target,
            targetBodyFatPercentage = targetFat,
            startingMassKg = from,
            startingBodyFatPercentage = fromFat,
            createdOn = today.minusWeeks(8)
        )

    // ---------------------------------------------------------------- what makes a week count

    @Test
    fun `two weigh-ins do not make a week`() {
        val logs = logsInWeek(weeksAgo = 1, count = 2, massKg = 75.0) +
            logsInWeek(weeksAgo = 2, count = 3, massKg = 75.0)

        assertEquals(1, GoalProgress.standing(goal(), logs, today).consecutiveWeeksCrossed)
    }

    @Test
    fun `the current week reports how many weigh-ins it holds`() {
        val logs = logsInWeek(weeksAgo = 0, count = 2, massKg = 79.0)

        assertEquals(2, GoalProgress.standing(goal(), logs, today).weighInsThisWeek)
        assertEquals(3, GoalProgress.WEIGH_INS_FOR_A_VALID_WEEK)
    }

    // ------------------------------------------------------------------ what validates a goal

    @Test
    fun `one crossing week is not enough`() {
        val logs = logsInWeek(weeksAgo = 1, count = 3, massKg = 75.0)

        val standing = GoalProgress.standing(goal(), logs, today)
        assertEquals(1, standing.consecutiveWeeksCrossed)
        assertFalse(standing.isReadyToValidate)
    }

    @Test
    fun `two consecutive crossing weeks are`() {
        val logs = logsInWeek(weeksAgo = 1, count = 3, massKg = 75.0) +
            logsInWeek(weeksAgo = 2, count = 3, massKg = 75.5)

        assertTrue(GoalProgress.standing(goal(), logs, today).isReadyToValidate)
    }

    @Test
    fun `two crossing weeks with a gap between them are not`() {
        // A good week, a lapse, another good week: the change is not installed.
        val logs = logsInWeek(weeksAgo = 1, count = 3, massKg = 75.0) +
            logsInWeek(weeksAgo = 2, count = 3, massKg = 79.0) +
            logsInWeek(weeksAgo = 3, count = 3, massKg = 75.0)

        assertEquals(1, GoalProgress.standing(goal(), logs, today).consecutiveWeeksCrossed)
    }

    // ------------------------------------------------------------------------ the recency window

    @Test
    fun `a goal is not validated by weeks older than the window`() {
        // Two perfect weeks, four months ago. Setting a goal today should not mine the past.
        val logs = logsInWeek(weeksAgo = 16, count = 3, massKg = 74.0) +
            logsInWeek(weeksAgo = 17, count = 3, massKg = 74.0)

        assertEquals(0, GoalProgress.standing(goal(), logs, today).consecutiveWeeksCrossed)
    }

    // ----------------------------------------------------------------- body fat eligibility

    @Test
    fun `a goal naming a fat threshold ignores weeks that never measured it`() {
        val withFat = goal(targetFat = 15.0, fromFat = 20.0)
        val logs = logsInWeek(weeksAgo = 1, count = 3, massKg = 75.0, bodyFat = null) +
            logsInWeek(weeksAgo = 2, count = 3, massKg = 75.0, bodyFat = null)

        assertEquals(0, GoalProgress.standing(withFat, logs, today).consecutiveWeeksCrossed)
    }

    @Test
    fun `a goal without a fat threshold counts those same weeks`() {
        val logs = logsInWeek(weeksAgo = 1, count = 3, massKg = 75.0, bodyFat = null) +
            logsInWeek(weeksAgo = 2, count = 3, massKg = 75.0, bodyFat = null)

        assertTrue(GoalProgress.standing(goal(), logs, today).isReadyToValidate)
    }

    @Test
    fun `both thresholds must be crossed in the same week`() {
        val withFat = goal(targetFat = 15.0, fromFat = 20.0)
        val logs = logsInWeek(weeksAgo = 1, count = 3, massKg = 75.0, bodyFat = 17.0) +
            logsInWeek(weeksAgo = 2, count = 3, massKg = 78.0, bodyFat = 14.0)

        assertEquals(0, GoalProgress.standing(withFat, logs, today).consecutiveWeeksCrossed)
    }

    // ------------------------------------------------------------------------- the projection

    @Test
    fun `one valid week is not a trend`() {
        val logs = logsInWeek(weeksAgo = 1, count = 3, massKg = 79.0)

        assertNull(GoalProgress.standing(goal(), logs, today).projectedDate)
    }

    @Test
    fun `a trend moving away from the target projects nothing`() {
        // Gaining while aiming lower: telling the athlete a date would be a fiction.
        val logs = logsInWeek(weeksAgo = 1, count = 3, massKg = 82.0) +
            logsInWeek(weeksAgo = 3, count = 3, massKg = 80.0)

        assertNull(GoalProgress.standing(goal(), logs, today).projectedDate)
    }

    @Test
    fun `a steady loss projects a date beyond today`() {
        val logs = logsInWeek(weeksAgo = 1, count = 3, massKg = 79.0) +
            logsInWeek(weeksAgo = 3, count = 3, massKg = 81.0)

        val projected = GoalProgress.standing(goal(), logs, today).projectedDate
        assertTrue("expected a date after today, got $projected", projected!!.isAfter(today))
    }

    // ------------------------------------------------------------------------ drifting back

    @Test
    fun `a validated goal the median has left is flagged, never un-validated`() {
        val validated = goal().copy(validatedOn = today.minusWeeks(6))
        val logs = (0L..3L).flatMap { logsInWeek(weeksAgo = it, count = 3, massKg = 79.0) }

        val standing = GoalProgress.standing(validated, logs, today)
        assertTrue(standing.hasDriftedBack)
        assertTrue("the goal itself is untouched", standing.goal.isValidated)
    }

    @Test
    fun `a goal still met does not read as drifted`() {
        val validated = goal().copy(validatedOn = today.minusWeeks(6))
        val logs = (0L..3L).flatMap { logsInWeek(weeksAgo = it, count = 3, massKg = 75.0) }

        assertFalse(GoalProgress.standing(validated, logs, today).hasDriftedBack)
    }

    // --------------------------------------------------------------------- before anchoring

    @Test
    fun `a goal set before any weigh-in reports no progress and validates nothing`() {
        val unanchored = goal(from = null)
        val logs = logsInWeek(weeksAgo = 1, count = 3, massKg = 75.0) +
            logsInWeek(weeksAgo = 2, count = 3, massKg = 75.0)

        val standing = GoalProgress.standing(unanchored, logs, today)
        assertEquals(0, standing.consecutiveWeeksCrossed)
        assertNull(standing.massProgress)
    }
}
