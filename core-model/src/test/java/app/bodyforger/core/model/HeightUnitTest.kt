package app.bodyforger.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Inches are a way of writing a height down. Everything that computes with one reads
 * centimetres, so the conversion has to survive a round trip exactly.
 */
class HeightUnitTest {

    @Test
    fun `centimetres pass through untouched`() {
        assertEquals(178.0, HeightUnit.CM.toCentimetres(178.0), 1e-9)
        assertEquals(178.0, HeightUnit.CM.fromCentimetres(178.0), 1e-9)
    }

    @Test
    fun `an inch is two point five four centimetres`() {
        assertEquals(2.54, HeightUnit.INCHES.toCentimetres(1.0), 1e-9)
        assertEquals(70.0, HeightUnit.INCHES.fromCentimetres(177.8), 1e-9)
    }

    @Test
    fun `a height survives the round trip in either unit`() {
        HeightUnit.entries.forEach { unit ->
            val written = unit.fromCentimetres(178.0)
            assertEquals("$unit", 178.0, unit.toCentimetres(written), 1e-9)
        }
    }

    @Test
    fun `centimetres are shown whole, inches to a tenth`() {
        assertEquals("178", HeightUnit.CM.format(178.0))
        assertEquals("178", HeightUnit.CM.format(177.6))
        assertEquals("70.1", HeightUnit.INCHES.format(178.0))
    }

    @Test
    fun `the symbol is the same in every language`() {
        assertEquals("cm", HeightUnit.CM.symbol)
        assertEquals("in", HeightUnit.INCHES.symbol)
    }
}
