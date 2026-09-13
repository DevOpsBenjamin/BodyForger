package app.bodyforger.core.ble

import app.bodyforger.core.model.ScaleCapability

/**
 * A scale a driver recognised from its advertised name, before any connection.
 *
 * [capability] is `null` when the family is known but the model is not: an invented ceiling
 * would be worth less than none.
 */
data class RecognisedScale(
    val displayName: String,
    val capability: ScaleCapability?
)
