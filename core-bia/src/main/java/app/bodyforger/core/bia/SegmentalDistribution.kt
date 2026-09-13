package app.bodyforger.core.bia

import app.bodyforger.core.model.SegmentalFatMass
import app.bodyforger.core.model.SegmentalImpedances
import app.bodyforger.core.model.SegmentalMuscleMass
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Distributes whole-body muscle and fat over the five segments, and derives a visceral
 * index — entirely from published anthropometric constants and each limb's own impedance.
 *
 * No manufacturer coefficient is involved. The chain, in order:
 *   * a limb's muscle follows its segmental resistance index L²/Z (segmental BIA), but only
 *     to set the left/right balance; the upper/lower split is a population constant, because
 *     limb lengths alone do not carry the true leg-to-arm muscle ratio;
 *   * a limb's fat is its non-muscular mass — the published mass fraction minus its muscle —
 *     which keeps every limb's fat non-negative by construction;
 *   * the trunk is the remainder, the only honest way to place trunk fat, whose impedance
 *     BIA measures poorly (`docs/FORGEFIT_MODEL.md`).
 *
 * Sources: de Leva 1996 / Winter *Biomechanics* (segment masses and lengths), Kim et al.
 * 2002 (appendicular muscle share), the population upper/lower limb muscle distribution.
 *
 * The visceral level is BodyForger's own index, not a medical visceral-adipose-tissue
 * measurement — every consumer scale defines its own arbitrary scale. Here it rises with
 * trunk fat and, past middle age, with age.
 */
internal object SegmentalDistribution {

    // Segment length as a fraction of stature — Winter, Biomechanics and Motor Control.
    private const val ARM_LENGTH_FRACTION = 0.44
    private const val LEG_LENGTH_FRACTION = 0.53

    // Segment mass as a fraction of body mass, per single limb — de Leva 1996 / Winter.
    private const val ARM_MASS_FRACTION = 0.050
    private const val LEG_MASS_FRACTION = 0.200

    // Appendicular skeletal muscle as a share of total skeletal muscle — Kim et al. 2002.
    const val APPENDICULAR_MUSCLE_FRACTION = 0.75

    // Of appendicular muscle, the share held by the two lower limbs — population constant.
    private const val LOWER_LIMB_MUSCLE_SHARE = 0.70

    // Visceral index: level rises with trunk fat and, past the pivot age, with age.
    private const val VISCERAL_TRUNK_COEFFICIENT = 0.70
    private const val VISCERAL_AGE_COEFFICIENT = 0.10
    private const val VISCERAL_AGE_PIVOT = 30
    private const val VISCERAL_MIN = 1
    private const val VISCERAL_MAX = 59

    data class Result(
        val muscle: SegmentalMuscleMass,
        val fat: SegmentalFatMass,
        val visceralLevel: Int,
    )

    /**
     * @param body the five segments isolated at 50 kHz — the frequency the equations are
     *   calibrated at.
     */
    fun distribute(
        body: SegmentalImpedances,
        heightCm: Double,
        massKg: Double,
        skeletalMuscleMassKg: Double,
        fatMassKg: Double,
        ageYears: Int,
    ): Result {
        val armLengthSq = (heightCm * ARM_LENGTH_FRACTION).let { it * it }
        val legLengthSq = (heightCm * LEG_LENGTH_FRACTION).let { it * it }

        // Segmental resistance index L²/Z — higher means a more conductive, more muscular limb.
        val riRightArm = armLengthSq / body.rightArmOhms
        val riLeftArm = armLengthSq / body.leftArmOhms
        val riRightLeg = legLengthSq / body.rightLegOhms
        val riLeftLeg = legLengthSq / body.leftLegOhms

        // Muscle: the upper/lower split is fixed; impedance sets only the left/right balance,
        // which is exactly the asymmetry a lifter wants to read.
        val appendicular = skeletalMuscleMassKg * APPENDICULAR_MUSCLE_FRACTION
        val legMuscle = appendicular * LOWER_LIMB_MUSCLE_SHARE
        val armMuscle = appendicular - legMuscle
        val rightArmMuscle = armMuscle * riRightArm / (riRightArm + riLeftArm)
        val leftArmMuscle = armMuscle - rightArmMuscle
        val rightLegMuscle = legMuscle * riRightLeg / (riRightLeg + riLeftLeg)
        val leftLegMuscle = legMuscle - rightLegMuscle
        val trunkMuscle = skeletalMuscleMassKg - appendicular

        val muscle = SegmentalMuscleMass(
            rightArmKg = rightArmMuscle,
            leftArmKg = leftArmMuscle,
            rightLegKg = rightLegMuscle,
            leftLegKg = leftLegMuscle,
            trunkKg = trunkMuscle,
        )

        // Fat: spread over each segment's non-muscular mass, the trunk taking the remainder.
        val rightArmMass = massKg * ARM_MASS_FRACTION
        val leftArmMass = massKg * ARM_MASS_FRACTION
        val rightLegMass = massKg * LEG_MASS_FRACTION
        val leftLegMass = massKg * LEG_MASS_FRACTION
        val trunkMass = massKg - (rightArmMass + leftArmMass + rightLegMass + leftLegMass)

        val nonMuscleRightArm = max(0.0, rightArmMass - rightArmMuscle)
        val nonMuscleLeftArm = max(0.0, leftArmMass - leftArmMuscle)
        val nonMuscleRightLeg = max(0.0, rightLegMass - rightLegMuscle)
        val nonMuscleLeftLeg = max(0.0, leftLegMass - leftLegMuscle)
        val nonMuscleTrunk = max(0.0, trunkMass - trunkMuscle)
        val nonMuscleTotal = nonMuscleRightArm + nonMuscleLeftArm +
            nonMuscleRightLeg + nonMuscleLeftLeg + nonMuscleTrunk

        val fat = if (nonMuscleTotal <= 0.0) {
            SegmentalFatMass(0.0, 0.0, 0.0, 0.0, fatMassKg)
        } else {
            SegmentalFatMass(
                rightArmKg = fatMassKg * nonMuscleRightArm / nonMuscleTotal,
                leftArmKg = fatMassKg * nonMuscleLeftArm / nonMuscleTotal,
                rightLegKg = fatMassKg * nonMuscleRightLeg / nonMuscleTotal,
                leftLegKg = fatMassKg * nonMuscleLeftLeg / nonMuscleTotal,
                trunkKg = fatMassKg * nonMuscleTrunk / nonMuscleTotal,
            )
        }

        val visceralLevel = (
            fat.trunkKg * VISCERAL_TRUNK_COEFFICIENT +
                max(0, ageYears - VISCERAL_AGE_PIVOT) * VISCERAL_AGE_COEFFICIENT
            ).roundToInt().coerceIn(VISCERAL_MIN, VISCERAL_MAX)

        return Result(muscle, fat, visceralLevel)
    }
}
