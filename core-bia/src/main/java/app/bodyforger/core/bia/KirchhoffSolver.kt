package app.bodyforger.core.bia

import app.bodyforger.core.model.ImpedancePath
import app.bodyforger.core.model.RawImpedances
import app.bodyforger.core.model.SegmentalImpedances

/**
 * Isolates the five body segments from the six measured paths.
 *
 * Model and derivations: `docs/BIA_ENGINE.md` §2.
 */
object KirchhoffSolver {

    private const val HALF = 0.5
    private const val QUARTER = 0.25
    private const val TRUNK_DIVISOR = 4.0

    /** Solves one frequency, or `null` when any of the six paths is missing at it. */
    fun solve(impedances: RawImpedances, frequencyKHz: Int): SegmentalImpedances? {
        val footToFoot = impedances[ImpedancePath.LEFT_FOOT_TO_RIGHT_FOOT, frequencyKHz] ?: return null
        val handToHand = impedances[ImpedancePath.LEFT_HAND_TO_RIGHT_HAND, frequencyKHz] ?: return null
        val leftHandLeftFoot = impedances[ImpedancePath.LEFT_HAND_TO_LEFT_FOOT, frequencyKHz] ?: return null
        val leftHandRightFoot = impedances[ImpedancePath.LEFT_HAND_TO_RIGHT_FOOT, frequencyKHz] ?: return null
        val rightHandLeftFoot = impedances[ImpedancePath.RIGHT_HAND_TO_LEFT_FOOT, frequencyKHz] ?: return null
        val rightHandRightFoot = impedances[ImpedancePath.RIGHT_HAND_TO_RIGHT_FOOT, frequencyKHz] ?: return null

        val armDelta = QUARTER *
            ((rightHandLeftFoot + rightHandRightFoot) - (leftHandLeftFoot + leftHandRightFoot))
        val legDelta = QUARTER *
            ((leftHandRightFoot + rightHandRightFoot) - (leftHandLeftFoot + rightHandLeftFoot))

        val crossedPathSum =
            leftHandLeftFoot + leftHandRightFoot + rightHandLeftFoot + rightHandRightFoot

        return SegmentalImpedances(
            frequencyKHz = frequencyKHz,
            rightArmOhms = HALF * handToHand + armDelta,
            leftArmOhms = HALF * handToHand - armDelta,
            rightLegOhms = HALF * footToFoot + legDelta,
            leftLegOhms = HALF * footToFoot - legDelta,
            trunkOhms = (crossedPathSum - 2.0 * (footToFoot + handToHand)) / TRUNK_DIVISOR,
            bodyOhms = (footToFoot + handToHand) / TRUNK_DIVISOR
        )
    }

    /**
     * The widest difference the measurements allow between two matching limbs, in ohms.
     *
     * Any segmental figure beyond it is wrong by construction — `docs/BIA_ENGINE.md` §2.
     */
    fun maximumLimbSpread(impedances: RawImpedances, frequencyKHz: Int): Double? {
        val crossed = ImpedancePath.entries
            .filter { it.involvesHands && it != ImpedancePath.LEFT_HAND_TO_RIGHT_HAND }
            .map { impedances[it, frequencyKHz] ?: return null }
        val total = crossed.sum()
        return crossed.indices
            .flatMap { i -> (i + 1 until crossed.size).map { j -> i to j } }
            .maxOf { (i, j) ->
                val pair = crossed[i] + crossed[j]
                kotlin.math.abs(pair - (total - pair)) / TRUNK_DIVISOR
            }
    }
}
