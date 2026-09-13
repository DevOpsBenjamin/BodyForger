package app.bodyforger.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class AthleteProfileTest {

    private val today = LocalDate.of(2026, 8, 30)

    private fun complete(birthDateIso: String = "1996-08-30") =
        AthleteProfile(sex = BiologicalSex.MALE, birthDateIso = birthDateIso, heightCm = 180.0)

    @Test
    fun `age counts whole years elapsed`() {
        assertEquals(30, complete("1996-08-30").ageYearsOn(today))
        assertEquals(29, complete("1996-08-31").ageYearsOn(today))
    }

    @Test
    fun `a profile missing any field yields no measurement profile`() {
        assertNull(complete().copy(sex = null).biaProfileOn(today))
        assertNull(complete().copy(birthDateIso = null).biaProfileOn(today))
        assertNull(complete().copy(heightCm = null).biaProfileOn(today))
    }

    @Test
    fun `a height outside human range is refused rather than measured`() {
        assertNull(complete().copy(heightCm = 18.0).biaProfileOn(today))
        assertNull(complete().copy(heightCm = 1800.0).biaProfileOn(today))
    }

    @Test
    fun `an unreadable stored date reads as unset`() {
        assertNull(AthleteProfile(birthDateIso = "pas une date").birthDate)
        assertFalse(complete("30-08-1996").isComplete)
    }

    @Test
    fun `a birth date in the future yields no age`() {
        assertNull(complete("2030-01-01").ageYearsOn(today))
    }

    @Test
    fun `a complete profile carries the derived age`() {
        val measurement = complete("1996-08-30").biaProfileOn(today)!!
        assertEquals(BiologicalSex.MALE, measurement.sex)
        assertEquals(30, measurement.ageYears)
        assertEquals(180.0, measurement.heightCm, 0.0)
    }
}
