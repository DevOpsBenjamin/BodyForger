package app.bodyforger.core.model

/**
 * The universal exercise model, shared by mobile, Wear OS and the database.
 *
 * Every exercise carries its canonical Google Health Connect type and its overall activity
 * category.
 */
data class Exercise(
    val id: String,
    val name: String,
    val activityCategory: WorkoutActivityCategory = WorkoutActivityCategory.STRENGTH_TRAINING,
    val healthConnectType: HealthConnectExerciseType = HealthConnectExerciseType.OTHER_WORKOUT,
    val primaryMuscleGroup: MuscleGroup = healthConnectType.primaryMuscleGroup,
    val equipment: EquipmentType = healthConnectType.defaultEquipment,
    val secondaryMuscleGroups: List<MuscleGroup> = emptyList(),
    val isUnilateral: Boolean = false,
    val isCustom: Boolean = false,
    val instructions: List<String> = emptyList()
)
