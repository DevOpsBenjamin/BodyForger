package app.bodyforger.mobile.stats

import app.bodyforger.core.model.BodyGoal
import app.bodyforger.core.model.BodyLog
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * One week of weigh-ins, reduced to what a goal needs to judge it.
 *
 * [bodyFatMedian] is `null` when no reading that week measured composition. Such a week still
 * counts for a goal that names no body fat threshold, and is not one of the weeks of a goal
 * that does.
 */
data class WeeklyMedian(
    val weekStart: LocalDate,
    val weighIns: Int,
    val massMedian: Double,
    val bodyFatMedian: Double?
) {
    /** A week too thin to trust a median from — see [GoalProgress.WEIGH_INS_FOR_A_VALID_WEEK]. */
    val isValid: Boolean get() = weighIns >= GoalProgress.WEIGH_INS_FOR_A_VALID_WEEK
}

/**
 * Where a goal stands, and why.
 *
 * Every field is answerable from the history alone; nothing is estimated to fill a gap. A
 * screen that cannot say how far along an athlete is says so rather than guessing.
 */
data class GoalStanding(
    val goal: BodyGoal,
    /** Consecutive valid weeks crossing the thresholds, within the recency window. */
    val consecutiveWeeksCrossed: Int,
    /** Weigh-ins logged in the current week, against what a valid week needs. */
    val weighInsThisWeek: Int,
    /** Share of the way covered on mass, from the starting point. `null` before anchoring. */
    val massProgress: Double?,
    /** Where the trend puts this goal, or `null` when there is no trend to extend. */
    val projectedDate: LocalDate?,
    /** True when the median has sat past a validated goal's threshold for the whole window. */
    val hasDriftedBack: Boolean
) {
    val isReadyToValidate: Boolean
        get() = consecutiveWeeksCrossed >= GoalProgress.CONSECUTIVE_WEEKS_TO_VALIDATE
}

/**
 * Turns a weigh-in history into what a goal screen shows.
 *
 * The rules, all of them deliberate and all of them visible to the athlete:
 *
 *  * a week counts from [WEIGH_INS_FOR_A_VALID_WEEK] weigh-ins — fewer, and its median says
 *    more about which days were chosen than about the week;
 *  * a goal is crossed by [CONSECUTIVE_WEEKS_TO_VALIDATE] consecutive valid weeks, so that a
 *    single lean week does not tick off a milestone;
 *  * only the last [RECENCY_WINDOW_WEEKS] weeks are examined, so that setting a goal today
 *    cannot validate it against a spring nobody remembers;
 *  * a goal naming a body fat threshold only ever looks at weeks that measured body fat.
 */
object GoalProgress {

    const val WEIGH_INS_FOR_A_VALID_WEEK = 3
    const val WEIGH_INS_FOR_A_SOLID_WEEK = 7
    const val CONSECUTIVE_WEEKS_TO_VALIDATE = 2
    const val RECENCY_WINDOW_WEEKS = 4L

    /** Weekly medians, most recent week first, one entry per week that saw a weigh-in. */
    fun weeklyMedians(logs: List<BodyLog>): List<WeeklyMedian> = logs
        .groupBy { it.localDate().with(DayOfWeek.MONDAY) }
        .map { (weekStart, weekLogs) ->
            WeeklyMedian(
                weekStart = weekStart,
                weighIns = weekLogs.size,
                massMedian = median(weekLogs.map { it.massKg })!!,
                bodyFatMedian = median(weekLogs.mapNotNull { it.bodyFatPercentage })
            )
        }
        .sortedByDescending { it.weekStart }

    /**
     * How a goal stands against a history, as of [today].
     *
     * [logs] is the whole history: this trims it to what each rule needs rather than making
     * the caller guess at windows.
     */
    fun standing(goal: BodyGoal, logs: List<BodyLog>, today: LocalDate): GoalStanding {
        val weeks = weeklyMedians(logs)
        val thisWeek = today.with(DayOfWeek.MONDAY)

        val eligible = weeks
            .filter { it.isValid }
            .filter { !it.weekStart.isBefore(thisWeek.minusWeeks(RECENCY_WINDOW_WEEKS - 1)) }
            .filter { !goal.requiresBodyFat || it.bodyFatMedian != null }

        val latestMass = logs.maxByOrNull { it.measuredAtEpochMs }?.massKg

        return GoalStanding(
            goal = goal,
            consecutiveWeeksCrossed = longestRunCrossing(goal, eligible),
            weighInsThisWeek = weeks.firstOrNull { it.weekStart == thisWeek }?.weighIns ?: 0,
            massProgress = latestMass?.let { goal.massProgressFrom(it) },
            projectedDate = projectedDate(goal, weeks, today),
            hasDriftedBack = goal.isValidated &&
                eligible.size >= RECENCY_WINDOW_WEEKS.toInt() &&
                eligible.none { goal.isCrossedBy(it.massMedian, it.bodyFatMedian) }
        )
    }

    /**
     * When the recent trend would reach the goal, or `null` when there is no trend.
     *
     * The slope comes from the oldest and newest valid weeks in the window: two points at a
     * known distance, which is the least that can be called a direction. A slope going the
     * wrong way, or nowhere, projects nothing — an athlete drifting away from a target is not
     * told they will arrive in nine years.
     */
    fun projectedDate(goal: BodyGoal, weeks: List<WeeklyMedian>, today: LocalDate): LocalDate? {
        val valid = weeks.filter { it.isValid }.sortedBy { it.weekStart }
        if (valid.size < 2) return null

        val first = valid.first()
        val last = valid.last()
        val weeksApart = java.time.temporal.ChronoUnit.WEEKS.between(first.weekStart, last.weekStart)
        if (weeksApart <= 0) return null

        val perWeek = (last.massMedian - first.massMedian) / weeksApart
        if (perWeek == 0.0) return null

        val remaining = goal.targetMassKg - last.massMedian
        val weeksLeft = remaining / perWeek
        if (weeksLeft < 0) return null

        return today.plusWeeks(Math.ceil(weeksLeft).toLong())
    }

    /** The longest run of consecutive weeks, among [weeks], whose medians cross the goal. */
    private fun longestRunCrossing(goal: BodyGoal, weeks: List<WeeklyMedian>): Int {
        val ordered = weeks.sortedBy { it.weekStart }
        var best = 0
        var run = 0
        var previousWeek: LocalDate? = null

        for (week in ordered) {
            val crossed = goal.isCrossedBy(week.massMedian, week.bodyFatMedian)
            val followsOn = previousWeek?.plusWeeks(1) == week.weekStart
            run = if (crossed && followsOn) run + 1 else if (crossed) 1 else 0
            best = maxOf(best, run)
            previousWeek = week.weekStart
        }
        return best
    }

    private fun median(values: List<Double>): Double? {
        if (values.isEmpty()) return null
        val sorted = values.sorted()
        val middle = sorted.size / 2
        return if (sorted.size % 2 == 1) sorted[middle] else (sorted[middle - 1] + sorted[middle]) / 2.0
    }
}
