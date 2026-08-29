package app.bodyforger.core.model

/**
 * The five body segments isolated at **one** frequency, in ohms.
 *
 * Entirely derived and never persisted — `docs/BIA_ENGINE.md` §2, which also explains why
 * [trunkOhms] is an indicator rather than a fine measurement.
 */
data class SegmentalImpedances(
    val frequencyKHz: Int,
    val rightArmOhms: Double,
    val leftArmOhms: Double,
    val rightLegOhms: Double,
    val leftLegOhms: Double,
    val trunkOhms: Double,
    val bodyOhms: Double
) {
    /** Body impedance index: height squared over resistance, the regression's input. */
    fun bodyImpedanceIndex(heightCm: Double): Double = (heightCm * heightCm) / bodyOhms
}
