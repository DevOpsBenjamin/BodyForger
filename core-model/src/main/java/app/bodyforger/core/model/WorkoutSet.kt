package app.bodyforger.core.model

import java.util.UUID

/** Which side a set is performed on. How it reads on screen belongs to the UI. */
enum class UnilateralSide {
    NONE,
    LEFT,
    RIGHT
}

/**
 * A session is either being trained or it is over.
 *
 * There is no discarded state: a session the athlete gives up on is deleted, because a status
 * that appears in no history and no statistic would only hide a row in the database.
 */
enum class WorkoutSessionStatus {
    ACTIVE,
    COMPLETED
}

data class WorkoutSet(
    val id: String = UUID.randomUUID().toString(),
    val sessionId: String = "",
    val exerciseId: String,
    val exerciseName: String = "",
    val primaryMuscle: MuscleGroup = MuscleGroup.CHEST,
    val equipment: EquipmentType = EquipmentType.BARBELL,
    val activityCategory: WorkoutActivityCategory = WorkoutActivityCategory.STRENGTH_TRAINING,
    val orderIndex: Int = 0,
    val setIndex: Int = 1,
    val type: RoutineSetType = RoutineSetType.NORMAL,
    val weightKg: Double = 0.0,
    val weightUnit: WeightUnit = WeightUnit.KG,
    val reps: Int = 0,
    val rpe: Double? = null,
    val isCompleted: Boolean = false,
    val side: UnilateralSide = UnilateralSide.NONE,
    val restTimeSeconds: Int = 90,
    val completedAtEpochMs: Long? = null
)
