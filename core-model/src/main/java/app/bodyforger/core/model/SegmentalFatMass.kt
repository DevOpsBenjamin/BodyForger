package app.bodyforger.core.model

/**
 * Fat mass over the five body segments, in kilograms.
 *
 * Parallel to [SegmentalMuscleMass]. Each limb's fat follows from its own segmental model;
 * the trunk carries the bulk of it.
 */
data class SegmentalFatMass(
    val rightArmKg: Double,
    val leftArmKg: Double,
    val rightLegKg: Double,
    val leftLegKg: Double,
    val trunkKg: Double,
) {
    val limbsKg: Double get() = rightArmKg + leftArmKg + rightLegKg + leftLegKg
    val totalKg: Double get() = limbsKg + trunkKg
}
