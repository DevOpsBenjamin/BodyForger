package app.bodyforger.mobile.data

import app.bodyforger.core.model.ImpedancePath
import app.bodyforger.core.model.ImpedanceReading
import app.bodyforger.core.model.RawImpedances

/**
 * Une pesée bi-fréquence **fictive**, pour faire vivre les écrans tant qu'aucune balance
 * n'est appairée.
 *
 * Les résistances ne sont copiées de personne : elles sont reconstruites par les lois des
 * mailles à partir d'un corps théorique — quatre membres longs et étroits, un tronc de large
 * section. Elles sont donc physiquement cohérentes sans être la mesure de quiconque.
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
