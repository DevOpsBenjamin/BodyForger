package app.bodyforger.core.model

enum class SetPhase {
    WARMUP,
    WORK
}

enum class SetType {
    STRAIGHT,
    DROPSET,
    RESTPAUSE
}

data class DropSubSet(
    val weightKg: Double,
    val reps: Int
)

data class ClusterSubSet(
    val reps: Int,
    val restSeconds: Int = 15
)

data class WorkoutSet(
    val id: String,
    val exerciseId: String,
    val phase: SetPhase = SetPhase.WORK,
    val type: SetType = SetType.STRAIGHT,
    val weightKg: Double,
    val reps: Int,
    val rpe: Double? = null,
    val isCompleted: Boolean = false,
    val drops: List<DropSubSet> = emptyList(),
    val clusters: List<ClusterSubSet> = emptyList(),
    val completedAtEpochMs: Long? = null
)
