package app.bodyforger.core.model

/**
 * The profile handed to the scale for its own bio-impedance computations.
 *
 * Distinct from [BiaProfile], which feeds our engine: same physiology, sent to the hardware
 * along with the last known weight that helps it frame its measurement.
 */
data class ScaleUserProfile(
    val physiology: BiaProfile,
    val lastWeightKg: Double? = null,
    /** Guest mode: a one-off reading that occupies no memory slot on the scale. */
    val isGuest: Boolean = false
)
