package app.bodyforger.mobile.data

import app.bodyforger.core.model.ImpedancePath
import app.bodyforger.core.model.ImpedanceReading
import app.bodyforger.core.model.RawImpedances

/**
 * A **fictional** dual-frequency reading, to keep the screens alive until a scale is paired.
 *
 * The resistances are copied from nobody: they are rebuilt from a theoretical body by the
 * loop laws, so they are physically coherent without being anyone's measurement.
 */
object DebugSampleBia {

    private const val RIGHT_ARM_50 = 260.0
    private const val LEFT_ARM_50 = 270.0
    private const val RIGHT_LEG_50 = 180.0
    private const val LEFT_LEG_50 = 185.0
    private const val TRUNK_50 = 22.0

    // À 250 kHz le courant traverse les membranes cellulaires : tout baisse.
    private const val RIGHT_ARM_250 = 232.0
    private const val LEFT_ARM_250 = 241.0
    private const val RIGHT_LEG_250 = 161.0
    private const val LEFT_LEG_250 = 165.0
    private const val TRUNK_250 = 19.0

    val dualFrequencyReading: RawImpedances = RawImpedances.of(
        paths(RIGHT_ARM_50, LEFT_ARM_50, RIGHT_LEG_50, LEFT_LEG_50, TRUNK_50, ImpedanceReading.LOW_FREQUENCY_KHZ) +
            paths(RIGHT_ARM_250, LEFT_ARM_250, RIGHT_LEG_250, LEFT_LEG_250, TRUNK_250, ImpedanceReading.HIGH_FREQUENCY_KHZ)
    )

    private fun paths(
        rightArm: Double,
        leftArm: Double,
        rightLeg: Double,
        leftLeg: Double,
        trunk: Double,
        frequencyKHz: Int
    ): Map<ImpedanceReading, Double> = mapOf(
        ImpedancePath.LEFT_FOOT_TO_RIGHT_FOOT to leftLeg + rightLeg,
        ImpedancePath.LEFT_HAND_TO_RIGHT_HAND to leftArm + rightArm,
        ImpedancePath.LEFT_HAND_TO_LEFT_FOOT to leftArm + trunk + leftLeg,
        ImpedancePath.LEFT_HAND_TO_RIGHT_FOOT to leftArm + trunk + rightLeg,
        ImpedancePath.RIGHT_HAND_TO_LEFT_FOOT to rightArm + trunk + leftLeg,
        ImpedancePath.RIGHT_HAND_TO_RIGHT_FOOT to rightArm + trunk + rightLeg
    ).mapKeys { (path, _) -> ImpedanceReading(path, frequencyKHz) }
}
