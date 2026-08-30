package app.bodyforger.mobile.stats

import app.bodyforger.core.model.WorkoutSession
import app.bodyforger.core.model.WorkoutSet
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

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

    private const val KILOGRAMS_PER_TONNE = 1_000.0
    private const val MILLIS_PER_HOUR = 3_600_000.0

    fun completedSets(sessions: List<WorkoutSession>): List<WorkoutSet> =
        sessions.flatMap { it.sets }.filter { it.isCompleted }

    /** Tonnage lifted across every session, in kilograms. */
    fun totalTonnageKg(sessions: List<WorkoutSession>): Double =
        completedSets(sessions).sumOf { it.weightKg * it.reps }

    fun totalTonnes(sessions: List<WorkoutSession>): Double =
        totalTonnageKg(sessions) / KILOGRAMS_PER_TONNE

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

    /** Sessions started in the last seven days, today included. */
    fun sessionsThisWeek(sessions: List<WorkoutSession>, todayEpochMs: Long): Int =
        activeDayOffsets(sessions, todayEpochMs, DAYS_IN_A_WEEK).size

    const val DAYS_IN_A_WEEK = 7

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
