package app.bodyforger.core.model

/**
 * Modèle d'exercice universel partagé entre Mobile, Wear OS et Database.
 * Chaque exercice est nativement lié à son type canonique Google Health Connect
 * et à sa catégorie d'activité globale (Musculation, Elliptique, Rameur, etc.).
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
