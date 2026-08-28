package app.bodyforger.core.model

import java.util.UUID

data class WorkoutSession(
    val id: String = UUID.randomUUID().toString(),
    val routineId: String? = null,
    val title: String,
    val notes: String = "",
    val status: WorkoutSessionStatus = WorkoutSessionStatus.ACTIVE,
    val startedAtEpochMs: Long = System.currentTimeMillis(),
    val endedAtEpochMs: Long? = null,
    val sets: List<WorkoutSet> = emptyList(),
    val averageHeartRateBpm: Int? = null,
    val activeCaloriesKcal: Int? = null,
    val totalVolumeKg: Double = 0.0,
    val isFinalized: Boolean = false
)
