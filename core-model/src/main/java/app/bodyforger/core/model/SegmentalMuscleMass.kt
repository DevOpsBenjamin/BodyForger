package app.bodyforger.core.model

/**
 * Muscle mass over the five body segments, in kilograms.
 *
 * Left/right and upper/lower splits are both measured; only the share of muscle held in the
 * limbs is a population constant, the trunk being the remainder — `docs/BIA_ENGINE.md` §5.
 */
data class SegmentalMuscleMass(
    val rightArmKg: Double,
    val leftArmKg: Double,
    val rightLegKg: Double,
    val leftLegKg: Double,
    val trunkKg: Double
) {
    /** Limb muscle: the ASMM of the clinical literature. */
    val appendicularKg: Double get() = rightArmKg + leftArmKg + rightLegKg + leftLegKg

    /** The five segments summed, equal by construction to skeletal muscle mass. */
    val totalKg: Double get() = appendicularKg + trunkKg

    /**
     * The Baumgartner skeletal muscle index, in kg/m² — the one the clinical grid applies to.
     *
     * Men: < 7.0 sarcopenia · 7.0–8.5 sedentary · 8.5–10.0 athletic · > 10.0 elite.
     */
    fun baumgartnerIndex(heightCm: Double): Double =
        appendicularKg / ((heightCm / 100.0) * (heightCm / 100.0))

    /** Difference between the arms, as a percentage of the stronger one. */
    val armAsymmetryPercent: Double
        get() = asymmetry(rightArmKg, leftArmKg)

    /** Difference between the legs, as a percentage of the stronger one. */
    val legAsymmetryPercent: Double
        get() = asymmetry(rightLegKg, leftLegKg)

    private fun asymmetry(right: Double, left: Double): Double {
        val stronger = maxOf(right, left)
        return if (stronger <= 0.0) 0.0 else (kotlin.math.abs(right - left) / stronger) * 100.0
    }
}
