package app.bodyforger.mobile.library

import app.bodyforger.core.database.entity.WorkoutSessionSummary

/**
 * A completed session as the history list needs it.
 *
 * Everything here is stored on the session itself, so the list renders without its sets: the
 * tonnage is written as the workout runs, the heart rate average when it closes, and the
 * exercises are concatenated by the query.
 */
data class WorkoutSessionHistoryEntry(
    val id: String,
    val title: String,
    val startedAtEpochMs: Long,
    val endedAtEpochMs: Long?,
    val totalVolumeKg: Double,
    val averageHeartRateBpm: Int?,
    val exerciseNames: List<String>
)

fun WorkoutSessionSummary.toHistoryEntry(): WorkoutSessionHistoryEntry = WorkoutSessionHistoryEntry(
    id = session.id,
    title = session.title,
    startedAtEpochMs = session.startedAtEpochMs,
    endedAtEpochMs = session.endedAtEpochMs,
    totalVolumeKg = session.totalVolumeKg,
    averageHeartRateBpm = session.averageHeartRateBpm,
    exerciseNames = exerciseNames?.split(", ")?.filter { it.isNotBlank() }.orEmpty()
)
