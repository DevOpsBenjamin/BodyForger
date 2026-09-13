package app.bodyforger.core.model

import java.util.UUID

enum class RoutineSetType {
    NORMAL,
    WARMUP,
    DROPSET,
    FAILURE,
    REST_PAUSE
}

/**
 * How a mass is written down, never how it is stored.
 *
 * Every mass in the database is kilograms — a loaded bar, a body, a goal. The composition
 * equations are written in kilograms, the scale reports them, and the profile engraved into it
 * carries them. More decisive still: a stored value whose unit depended on a setting would stop
 * meaning the same thing the day the setting changed, and every past row would silently say
 * something else.
 *
 * So pounds are a way of reading and typing, converted at the edge.
 *
 * [symbol] is written the same in every language; the full name is not.
 */
enum class WeightUnit(val symbol: String) {
    KG("kg"),
    LBS("lbs");

    /** Kilograms as this unit reads them. */
    fun fromKilograms(kilograms: Double): Double = when (this) {
        KG -> kilograms
        LBS -> kilograms / KILOGRAMS_PER_POUND
    }

    /** A value written in this unit, back to the kilograms everything else works in. */
    fun toKilograms(value: Double): Double = when (this) {
        KG -> value
        LBS -> value * KILOGRAMS_PER_POUND
    }

    /** Trailing zeroes dropped: a bar loaded to 100 reads as 100, not 100.0. */
    fun format(kilograms: Double): String {
        val shown = fromKilograms(kilograms)
        return if (shown % 1.0 == 0.0) shown.toInt().toString() else String.format("%.1f", shown)
    }

    /** The value with its symbol, as a load or a body mass is labelled on screen. */
    fun formatWithSymbol(kilograms: Double): String = "${format(kilograms)} $symbol"

    private companion object {
        const val KILOGRAMS_PER_POUND = 0.45359237
    }
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
    val activityCategory: WorkoutActivityCategory = WorkoutActivityCategory.STRENGTH_TRAINING,
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
