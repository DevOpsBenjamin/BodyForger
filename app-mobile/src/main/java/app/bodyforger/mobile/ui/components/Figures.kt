package app.bodyforger.mobile.ui.components

import java.text.NumberFormat
import java.util.Locale

/**
 * A measured figure as a screen shows it: one decimal at most, a trailing zero dropped, and
 * the locale's own decimal separator.
 *
 * What the engine computes keeps every digit a double holds — the history is recomputed from
 * the resistances and must not lose precision on the way. Only the printing is rounded: a body
 * holds 52.0 L of water, never 52.0192348320957.
 *
 * Kept apart from [app.bodyforger.core.model.WeightUnit.format], which also converts to the
 * unit a load is read in. A litre and a percentage are not weights and convert to nothing.
 */
internal fun formatMeasure(value: Double, locale: Locale = Locale.getDefault()): String =
    NumberFormat.getNumberInstance(locale).apply {
        maximumFractionDigits = 1
        minimumFractionDigits = 0
        isGroupingUsed = false
    }.format(value)
