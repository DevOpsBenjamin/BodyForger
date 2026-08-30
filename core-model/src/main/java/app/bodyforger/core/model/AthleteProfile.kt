package app.bodyforger.core.model

import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeParseException

/**
 * Who the athlete is, as far as a body composition measurement is concerned.
 *
 * Every field is optional because a fresh installation knows none of them, and none may be
 * invented: sex, age and height are inputs to the scale's own body fat computation, so a
 * guessed value comes back as a plausible, wrong measurement that is then stored for good.
 *
 * The date of birth is stored rather than the age. An age is only true for a year, and a
 * profile that silently rots would shift every later measurement without anyone noticing.
 */
data class AthleteProfile(
    val sex: BiologicalSex? = null,
    val birthDateIso: String? = null,
    val heightCm: Double? = null
) {

    /** The birth date, or null while it is unset or unreadable. */
    val birthDate: LocalDate?
        get() = birthDateIso?.let {
            try {
                LocalDate.parse(it)
            } catch (_: DateTimeParseException) {
                null
            }
        }

    fun ageYearsOn(today: LocalDate): Int? =
        birthDate?.takeIf { it.isBefore(today) }?.let { Period.between(it, today).years }

    /**
     * The profile a measurement needs, or null while anything is missing.
     *
     * A null is what blocks a weigh-in: better no measurement than one framed by invented
     * numbers.
     */
    fun biaProfileOn(today: LocalDate): BiaProfile? {
        val sex = sex ?: return null
        val age = ageYearsOn(today) ?: return null
        val height = heightCm?.takeIf { it in MINIMUM_HEIGHT_CM..MAXIMUM_HEIGHT_CM } ?: return null
        return BiaProfile(sex = sex, ageYears = age, heightCm = height)
    }

    val isComplete: Boolean
        get() = biaProfileOn(LocalDate.now()) != null

    companion object {
        /** Bounds a typed height must fall within to be a height at all, not a typo. */
        const val MINIMUM_HEIGHT_CM = 100.0
        const val MAXIMUM_HEIGHT_CM = 250.0
    }
}
