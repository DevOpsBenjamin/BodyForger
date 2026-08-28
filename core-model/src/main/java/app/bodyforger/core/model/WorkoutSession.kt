package app.bodyforger.core.model

data class WorkoutSession(
    val id: String,
    val title: String,
    val startedAtEpochMs: Long,
    val endedAtEpochMs: Long? = null,
    val sets: List<WorkoutSet> = emptyList(),
    val averageHeartRateBpm: Int? = null,
    val activeCaloriesKcal: Int? = null,
    val isFinalized: Boolean = false
)
