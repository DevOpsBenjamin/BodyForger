package app.bodyforger.mobile.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

/**
 * A figure is rounded where it is printed, never where it is computed.
 *
 * The engine answered 52.0192348320957 litres of water and the card showed all of it. Every
 * digit past the first decimal is arithmetic, not measurement.
 */
class FiguresTest {

    @Test
    fun `a computed figure keeps one decimal at most`() {
        assertEquals("52", formatMeasure(52.0192348320957083257, Locale.US))
        assertEquals("33", formatMeasure(33.0208, Locale.US))
        assertEquals("30.9", formatMeasure(30.94632, Locale.US))
        assertEquals("71.1", formatMeasure(71.11499, Locale.US))
    }

    @Test
    fun `a trailing zero is dropped, and a round figure stays round`() {
        assertEquals("52", formatMeasure(52.0, Locale.US))
        assertEquals("103", formatMeasure(103.0, Locale.US))
    }

    @Test
    fun `the locale decides the decimal separator`() {
        assertEquals("30.9", formatMeasure(30.94, Locale.US))
        assertEquals("30,9", formatMeasure(30.94, Locale.FRANCE))
    }

    @Test
    fun `a figure is never grouped, being a body and not a tonnage`() {
        assertEquals("1052.4", formatMeasure(1052.44, Locale.US))
    }
}
