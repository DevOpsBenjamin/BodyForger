package app.bodyforger.core.model

/**
 * The universal exercise model, shared by mobile, Wear OS and the database.
 *
 * Every exercise carries its canonical Google Health Connect type and its overall activity
 * category.
 */
data class Exercise(
    val id: String,
    /**
     * Resource key for a built-in exercise, so its name follows the athlete's locale.
     * `null` on an exercise the athlete created: that name is theirs, in their own words.
     */
    val nameKey: String? = null,
    /** English fallback, and the only name a custom exercise has. */
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
