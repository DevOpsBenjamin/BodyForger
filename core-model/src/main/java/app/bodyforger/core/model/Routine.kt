package app.bodyforger.core.model

import java.util.UUID

enum class RoutineSetType(val displayName: String, val shortBadge: String) {
    NORMAL("Normale", "1"),
    WARMUP("Échauffement", "ÉCH"),
    DROPSET("Dégressive (Drop Set)", "DROP"),
    FAILURE("À l'échec", "ÉCHEC"),
    REST_PAUSE("Rest-Pause", "RP")
}

enum class WeightUnit(val symbol: String, val displayName: String) {
    KG("kg", "Kilogrammes (kg)"),
    LBS("lbs", "Livres (lbs)")
}

data class RoutineSet(
    val id: String = UUID.randomUUID().toString(),
    val setIndex: Int = 1,
    val type: RoutineSetType = RoutineSetType.NORMAL,
    val targetWeightKg: Double? = null,
    val reps: Int? = 10,
    val minReps: Int? = 8,
    val maxReps: Int? = 12,
    val isRepsRange: Boolean = false
)

data class RoutineExercise(
    val id: String = UUID.randomUUID().toString(),
    val routineId: String = "",
    val exerciseId: String,
    val exerciseName: String,
    val primaryMuscle: MuscleGroup,
    val equipment: EquipmentType,
    val isUnilateral: Boolean = false,
    val weightUnit: WeightUnit = WeightUnit.KG,
    val orderIndex: Int = 0,
    val restTimeSeconds: Int = 90,
    val notes: String = "",
    val sets: List<RoutineSet> = listOf(
        RoutineSet(setIndex = 1),
        RoutineSet(setIndex = 2),
        RoutineSet(setIndex = 3)
    ),
    val supersetGroupId: String? = null
)

data class Routine(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val notes: String = "",
    val assignedDays: Set<Int> = emptySet(), // 1 = Lundi, 2 = Mardi, ..., 7 = Dimanche
    val exercises: List<RoutineExercise> = emptyList(),
    val createdAtEpochMs: Long = System.currentTimeMillis()
)
