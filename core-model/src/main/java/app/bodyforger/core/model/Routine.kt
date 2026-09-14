package app.bodyforger.core.model

import java.text.NumberFormat
import java.util.Locale
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

    /**
     * Trailing zeroes dropped, thousands grouped: a bar reads 100, a season's tonnage 7,894.
     *
     * Grouped because a total is where the digits pile up — 7894 is a figure to decipher,
     * 7,894 is one to read. The locale decides which separator, as it does the decimal one.
     */
    fun format(kilograms: Double, locale: Locale = Locale.getDefault()): String {
        val shown = fromKilograms(kilograms)
        val format = NumberFormat.getNumberInstance(locale).apply {
            maximumFractionDigits = 1
            minimumFractionDigits = 0
            isGroupingUsed = true
        }
        return format.format(shown)
    }

    /** The value with its symbol, as a load, a body mass or a tonnage is labelled on screen. */
    fun formatWithSymbol(kilograms: Double, locale: Locale = Locale.getDefault()): String =
        "${format(kilograms, locale)} $symbol"

    /**
     * A career total, short enough to sit in a stat card.
     *
     * Every set of every session adds up fast: a season reads 233,436.5 kg, which wraps onto two
     * lines and is no more informative than 233.4 t. Past a thousand units the figure switches to
     * thousands — tonnes in kilograms, thousands of pounds otherwise — and keeps one decimal.
     * Below that it is written out in full, because 840 kg is a number to read, not to round.
     */
    fun formatCumulative(kilograms: Double, locale: Locale = Locale.getDefault()): String {
        val shown = fromKilograms(kilograms)
        if (shown < THOUSAND) return formatWithSymbol(kilograms, locale)
        val format = NumberFormat.getNumberInstance(locale).apply {
            maximumFractionDigits = 1
            minimumFractionDigits = 0
            isGroupingUsed = true
        }
        val suffix = when (this) {
            KG -> "t"
            LBS -> "k $symbol"
        }
        return "${format.format(shown / THOUSAND)} $suffix"
    }

    private companion object {
        const val KILOGRAMS_PER_POUND = 0.45359237
        const val THOUSAND = 1000.0
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
