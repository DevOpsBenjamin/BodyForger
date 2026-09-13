package app.bodyforger.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pounds are a way of writing a mass down. Everything that adds one up — tonnage, a goal
 * threshold, a composition equation — reads kilograms, so the conversion has to be exact in
 * both directions.
 */
class WeightUnitTest {

    @Test
    fun `kilograms pass through untouched`() {
        assertEquals(100.0, WeightUnit.KG.toKilograms(100.0), 1e-9)
        assertEquals(100.0, WeightUnit.KG.fromKilograms(100.0), 1e-9)
    }

    @Test
    fun `a pound is the international avoirdupois pound`() {
        assertEquals(0.45359237, WeightUnit.LBS.toKilograms(1.0), 1e-12)
        assertEquals(220.46226218, WeightUnit.LBS.fromKilograms(100.0), 1e-6)
    }

    @Test
    fun `a mass survives the round trip in either unit`() {
        WeightUnit.entries.forEach { unit ->
            val written = unit.fromKilograms(82.4)
            assertEquals("$unit", 82.4, unit.toKilograms(written), 1e-9)
        }
    }

    @Test
    fun `a whole number keeps no trailing zero`() {
        assertEquals("100", WeightUnit.KG.format(100.0))
        assertEquals("82.4", WeightUnit.KG.format(82.4))
    }

    @Test
    fun `the symbol follows the value`() {
        assertEquals("100 kg", WeightUnit.KG.formatWithSymbol(100.0))
        assertEquals("220.5 lbs", WeightUnit.LBS.formatWithSymbol(100.0))
    }

    @Test
    fun `typing a hundred in pounds is not a hundred kilograms`() {
        // The fault this replaces: the figure went into weightKg untouched, so a bar logged in
        // pounds inflated the tonnage by a factor of two point two.
        assertEquals(45.359237, WeightUnit.LBS.toKilograms(100.0), 1e-9)
    }
}
