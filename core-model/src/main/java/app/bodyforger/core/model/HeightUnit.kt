package app.bodyforger.core.model

import kotlin.math.roundToInt

/**
 * How a height is written down, never how it is stored.
 *
 * A height lives in centimetres everywhere that matters: [BiaProfile] carries it to the
 * published equations, which are written in centimetres, and it is engraved into the scale's
 * own profile. Inches are a way of reading and typing it, converted at the edge — putting a
 * unit conversion inside the composition engine would be an error nothing could see, since the
 * output would still look plausible.
 *
 * [symbol] is written the same in every language; the full name is not.
 */
enum class HeightUnit(val symbol: String) {
    CM("cm"),
    INCHES("in");

    /** Centimetres as this unit reads them. */
    fun fromCentimetres(centimetres: Double): Double = when (this) {
        CM -> centimetres
        INCHES -> centimetres / CENTIMETRES_PER_INCH
    }

    /** A value written in this unit, back to the centimetres everything else works in. */
    fun toCentimetres(value: Double): Double = when (this) {
        CM -> value
        INCHES -> value * CENTIMETRES_PER_INCH
    }

    /**
     * How the athlete expects to read it: whole centimetres, or inches to one decimal.
     *
     * A centimetre is already finer than anyone measures their own height, and a tenth of an
     * inch is its closest equivalent — 178 cm reads as 70.1 in, not 70.07874015748.
     */
    fun format(centimetres: Double): String = when (this) {
        CM -> centimetres.roundToInt().toString()
        INCHES -> String.format("%.1f", fromCentimetres(centimetres))
    }

    private companion object {
        const val CENTIMETRES_PER_INCH = 2.54
    }
}
