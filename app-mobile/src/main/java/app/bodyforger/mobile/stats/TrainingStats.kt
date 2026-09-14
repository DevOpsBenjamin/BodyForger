package app.bodyforger.mobile.stats

import app.bodyforger.core.model.MuscleGroup
import app.bodyforger.core.model.Routine
import app.bodyforger.core.model.WorkoutSession
import app.bodyforger.core.model.WorkoutSet
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.ceil

/**
 * What a training history adds up to.
 *
 * Everything here reads completed sets only: a set that was planned but not performed is not
 * training, and counting it would inflate every figure the athlete uses to judge progress.
 *
 * The estimated one-rep max uses Epley's formula — see `docs/TRAINING_STATS.md`.
 */
object TrainingStats {

    /** Epley: a set of `reps` at `weight` is worth `weight × (1 + reps / 30)` for one rep. */
    private const val EPLEY_DIVISOR = 30.0

    private const val MILLIS_PER_HOUR = 3_600_000.0

    fun completedSets(sessions: List<WorkoutSession>): List<WorkoutSet> =
        sessions.flatMap { it.sets }.filter { it.isCompleted }

    /** Tonnage lifted across every session, in kilograms. */
    fun totalTonnageKg(sessions: List<WorkoutSession>): Double =
        completedSets(sessions).sumOf { it.weightKg * it.reps }


    /** Hours spent training, counting only sessions that were actually closed. */
    fun totalHours(sessions: List<WorkoutSession>): Double = sessions
        .mapNotNull { session -> session.endedAtEpochMs?.minus(session.startedAtEpochMs) }
        .filter { it > 0 }
        .sumOf { it / MILLIS_PER_HOUR }

    /** Tonnage of the sessions started within the given window. */
    fun tonnageBetween(sessions: List<WorkoutSession>, fromEpochMs: Long, toEpochMs: Long): Double =
        totalTonnageKg(sessions.filter { it.startedAtEpochMs in fromEpochMs..toEpochMs })

    fun durationMinutes(session: WorkoutSession): Long? =
        session.endedAtEpochMs?.minus(session.startedAtEpochMs)?.takeIf { it > 0 }?.div(60_000)

    /** The exercises of a session, in the order they were performed, without repeats. */
    fun exerciseNames(session: WorkoutSession): List<String> = session.sets
        .sortedBy { it.orderIndex }
        .map { it.exerciseName }
        .distinct()

    /**
     * The athlete's best set on each exercise, heaviest estimated one-rep max first.
     *
     * The record is the set with the highest estimate, not the heaviest load: five reps at
     * 100 kg is a better performance than one at 105 kg, and only the estimate says so.
     */
    fun personalRecords(sessions: List<WorkoutSession>): List<PersonalRecord> =
        completedSets(sessions)
            .filter { it.weightKg > 0 && it.reps > 0 }
            .groupBy { it.exerciseId }
            .mapNotNull { (_, sets) ->
                val best = sets.maxByOrNull { estimatedOneRepMax(it) } ?: return@mapNotNull null
                PersonalRecord(
                    exerciseName = best.exerciseName,
                    estimatedOneRepMaxKg = estimatedOneRepMax(best),
                    bestWeightKg = best.weightKg,
                    bestReps = best.reps
                )
            }
            .sortedByDescending { it.estimatedOneRepMaxKg }

    /**
     * Which of the last [days] days were trained, counted back from [todayEpochMs].
     *
     * Index 0 is the oldest day of the window and the last index is today, so the grid reads
     * left to right the way a calendar does.
     */
    fun activeDayOffsets(sessions: List<WorkoutSession>, todayEpochMs: Long, days: Int): Set<Int> {
        val today = Instant.ofEpochMilli(todayEpochMs).atZone(ZoneId.systemDefault()).toLocalDate()
        val start = today.minusDays((days - 1).toLong())
        return sessions
            .map { Instant.ofEpochMilli(it.startedAtEpochMs).atZone(ZoneId.systemDefault()).toLocalDate() }
            .filterNot { it.isBefore(start) || it.isAfter(today) }
            .map { ChronoUnit.DAYS.between(start, it).toInt() }
            .toSet()
    }

    /**
     * The activity grid, one column per week, Monday at the top.
     *
     * Weeks rather than runs of days: a column that holds five consecutive days puts Monday on a
     * different row every week, so no habit can show through. Aligned on weekdays, a fortnight of
     * Tuesdays reads as a line, and an untouched weekend as a gap.
     *
     * The last column is the current week, padded to Sunday so today keeps its weekday position
     * rather than sliding to the end of the grid.
     */
    fun activityWeeks(
        sessions: List<WorkoutSession>,
        todayEpochMs: Long,
        weeks: Int
    ): List<List<Boolean>> {
        val zone = ZoneId.systemDefault()
        val today = Instant.ofEpochMilli(todayEpochMs).atZone(zone).toLocalDate()
        val thisMonday = today.with(DayOfWeek.MONDAY)
        val firstMonday = thisMonday.minusWeeks((weeks - 1).toLong())

        val trained = sessions
            .map { Instant.ofEpochMilli(it.startedAtEpochMs).atZone(zone).toLocalDate() }
            .toSet()

        return (0 until weeks).map { week ->
            val monday = firstMonday.plusWeeks(week.toLong())
            (0 until DAYS_IN_A_WEEK).map { day -> monday.plusDays(day.toLong()) in trained }
        }
    }

    /** Sessions started in the last seven days, today included. */
    fun sessionsThisWeek(sessions: List<WorkoutSession>, todayEpochMs: Long): Int =
        activeDayOffsets(sessions, todayEpochMs, DAYS_IN_A_WEEK).size

    /**
     * Weeks trained in an unbroken run, counting back from the current one.
     *
     * A week counts when it holds at least one session. The current week is forgiving: an
     * athlete who has not trained yet this Monday keeps the run their previous weeks earned,
     * because a streak that collapses every Monday morning measures the calendar, not them.
     *
     * Weeks start on Monday, as ISO has it.
     */
    fun consecutiveTrainingWeeks(sessions: List<WorkoutSession>, todayEpochMs: Long): Int {
        if (sessions.isEmpty()) return 0

        val zone = ZoneId.systemDefault()
        val thisWeek = Instant.ofEpochMilli(todayEpochMs).atZone(zone).toLocalDate()
            .with(DayOfWeek.MONDAY)
        val trained = sessions
            .map { Instant.ofEpochMilli(it.startedAtEpochMs).atZone(zone).toLocalDate().with(DayOfWeek.MONDAY) }
            .toSet()

        var week = if (thisWeek in trained) thisWeek else thisWeek.minusWeeks(1)
        var run = 0
        while (week in trained) {
            run++
            week = week.minusWeeks(1)
        }
        return run
    }

    const val DAYS_IN_A_WEEK = 7

    /**
     * How long a routine should take, from the rest the athlete actually set.
     *
     * Rest is real data — their own value, per exercise. The work itself is not measured
     * anywhere, so a set is counted at [SECONDS_PER_SET]: long enough to cover a working set
     * and the walk to the rack, short enough not to dominate the total. The last rest of the
     * routine is dropped, since nobody rests after the final set before leaving.
     *
     * The result is an estimate and is labelled as one on screen.
     */
    fun estimatedRoutineMinutes(routine: Routine): Int? {
        val sets = routine.exercises.sumOf { it.sets.size }
        if (sets == 0) return null

        val restSeconds = routine.exercises.sumOf { it.restTimeSeconds * it.sets.size }
        val lastRest = routine.exercises.lastOrNull()?.restTimeSeconds ?: 0
        val totalSeconds = restSeconds - lastRest + sets * SECONDS_PER_SET

        return ceil(totalSeconds / SECONDS_PER_MINUTE).toInt().coerceAtLeast(1)
    }

    /** A working set, rack walk included. Not measured — see `docs/TRAINING_STATS.md`. */
    const val SECONDS_PER_SET = 45

    private const val SECONDS_PER_MINUTE = 60.0

    /**
     * What the week's plan asks for: the routines the planner assigned to a weekday.
     *
     * A routine assigned to three days is three sessions, and its sets count three times. A
     * week with nothing assigned asks for nothing, which is a legitimate answer rather than a
     * figure to invent.
     */
    fun plannedThisWeek(routines: List<Routine>): PlannedWeek {
        val assigned = routines.flatMap { routine ->
            routine.assignedDays.map { routine }
        }

        return PlannedWeek(
            sessions = assigned.size,
            sets = assigned.sumOf { routine -> routine.exercises.sumOf { it.sets.size } },
            setsByMuscle = assigned
                .flatMap { routine -> routine.exercises }
                .groupBy { it.primaryMuscle }
                .mapValues { (_, exercises) -> exercises.sumOf { it.sets.size } }
        )
    }

    /** Validated sets in the last seven days, counted per muscle the exercise works first. */
    fun completedSetsByMuscle(sessions: List<WorkoutSession>, todayEpochMs: Long): Map<MuscleGroup, Int> {
        val since = todayEpochMs - DAYS_IN_A_WEEK * MILLIS_PER_DAY
        return sessions
            .filter { it.startedAtEpochMs in since..todayEpochMs }
            .flatMap { it.sets }
            .filter { it.isCompleted }
            .groupingBy { it.primaryMuscle }
            .eachCount()
    }

    /** Validated sets in the last seven days, all muscles together. */
    fun completedSetsThisWeek(sessions: List<WorkoutSession>, todayEpochMs: Long): Int {
        val since = todayEpochMs - DAYS_IN_A_WEEK * MILLIS_PER_DAY
        return sessions
            .filter { it.startedAtEpochMs in since..todayEpochMs }
            .flatMap { it.sets }
            .count { it.isCompleted }
    }

    private const val MILLIS_PER_DAY = 86_400_000L

    fun estimatedOneRepMax(set: WorkoutSet): Double =
        set.weightKg * (1.0 + set.reps / EPLEY_DIVISOR)
}

/** The best a single exercise has been lifted, and what it was estimated from. */
data class PersonalRecord(
    val exerciseName: String,
    val estimatedOneRepMaxKg: Double,
    val bestWeightKg: Double,
    val bestReps: Int
)

/** What a week's assigned routines add up to. Zero everywhere when nothing is planned. */
data class PlannedWeek(
    val sessions: Int,
    val sets: Int,
    val setsByMuscle: Map<MuscleGroup, Int>
) {
    val isEmpty: Boolean get() = sessions == 0
}
