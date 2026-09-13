package app.bodyforger.core.model

/**
 * The lasting link between the athlete and a scale.
 *
 * Created once, then shared between watch and phone; every later reading uses it directly and
 * pairing is never replayed. Carries the hardware ceiling, `null` until the model is known.
 */
data class ScaleAssociation(
    /** Physical Bluetooth address, from the native scan — never typed in. */
    val deviceAddress: String,

    /** Athlete identifier engraved in the scale's flash. One per athlete. */
    val huid: String,

    /** Calibration tare read during pairing, in kilograms. */
    val tareKg: Double,

    /** Advertised name, as received: it is what carries the model. */
    val advertisedName: String,

    /** Hardware ceiling, or `null` when the model is undocumented. */
    val capability: ScaleCapability? = null
)
