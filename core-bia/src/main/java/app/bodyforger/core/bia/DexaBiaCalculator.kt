package app.bodyforger.core.bia

import app.bodyforger.core.model.BiaProfile
import app.bodyforger.core.model.BodyCompositionReport
import app.bodyforger.core.model.ImpedancePath
import app.bodyforger.core.model.ImpedanceReading
import app.bodyforger.core.model.RawImpedances
import app.bodyforger.core.model.SegmentalImpedances
import app.bodyforger.core.model.SegmentalMuscleMass

/**
 * Turns measured resistances into a body composition.
 *
 * Model, coefficients and their provenance: `docs/BIA_ENGINE.md`.
 *
 * A quantity the hardware did not measure is absent, never replaced by a default: a
 * fabricated figure would be indistinguishable from a real measurement in the history.
 */
object DexaBiaCalculator {

    /** Brozek 4C split of fat-free mass — `docs/BIA_ENGINE.md` §4. */
    private const val BROZEK_WATER_FRACTION = 0.732
    private const val BROZEK_PROTEIN_FRACTION = 0.211
    private const val BROZEK_BONE_MINERAL_FRACTION = 0.057

    /** Clinical norm used when a single frequency cannot reveal the hydration balance. */
    private const val DEFAULT_ECW_TBW_RATIO = 0.380
    private const val ECW_RATIO_AT_REFERENCE = 0.380
    private const val ECW_RATIO_SLOPE = 0.05
    private const val ECW_REFERENCE_FREQUENCY_RATIO = 0.88
    private const val ECW_RATIO_FLOOR = 0.30
    private const val ECW_RATIO_CEILING = 0.50

    /** Skeletal muscle from fat-free mass — `docs/BIA_ENGINE.md` §5. */
    private const val SKELETAL_MUSCLE_SLOPE = 0.605
    private const val SKELETAL_MUSCLE_INTERCEPT = 1.833

    /** Share of skeletal muscle held in the four limbs; the remainder is the trunk. */
    private const val APPENDICULAR_MUSCLE_FRACTION = 0.650

    /** Upper-to-lower geometric calibration — `docs/BIA_ENGINE.md` §5. */
    private const val UPPER_TO_LOWER_GEOMETRY = 0.506

    /**
     * The richest analysis the reading allows, or `null` when it carries no usable impedance.
     *
     * Three regimes, from richest to poorest. None fills in what the hardware did not measure:
     * a poorer one returns fewer quantities, never invented ones.
     */
    fun calculate(
        massKg: Double,
        profile: BiaProfile,
        impedances: RawImpedances
    ): BodyCompositionReport? {
        require(massKg > 0.0) { "Invalid mass: $massKg kg" }

        val low = KirchhoffSolver.solve(impedances, ImpedanceReading.LOW_FREQUENCY_KHZ)
        val high = KirchhoffSolver.solve(impedances, ImpedanceReading.HIGH_FREQUENCY_KHZ)

        return when {
            low != null -> eightElectrode(massKg, profile, low, high)
            else -> fourElectrode(massKg, profile, impedances)
        }
    }

    /**
     * Eight electrodes. Without a high frequency the low one stands in for both, and the
     * regression collapses to its single-impedance form — `docs/BIA_ENGINE.md` §3.
     */
    private fun eightElectrode(
        massKg: Double,
        profile: BiaProfile,
        low: SegmentalImpedances,
        high: SegmentalImpedances?
    ): BodyCompositionReport? {
        val fatFreeMassKg = LeanMassModel.of(profile.sex)
            .evaluate(massKg, profile, low.bodyOhms, (high ?: low).bodyOhms)
        if (fatFreeMassKg <= 0.0 || fatFreeMassKg >= massKg) return null

        val skeletalMuscleMassKg = skeletalMuscle(fatFreeMassKg)
        return compose(
            massKg = massKg,
            fatFreeMassKg = fatFreeMassKg,
            skeletalMuscleMassKg = skeletalMuscleMassKg,
            ecwTbwRatio = if (high != null) extracellularRatio(low, high) else DEFAULT_ECW_TBW_RATIO,
            segmentalMuscle = distributeMuscle(skeletalMuscleMassKg, low),
            segmental = listOfNotNull(low, high)
        )
    }

    /**
     * Four plate electrodes: current crosses the legs only, so no limb is isolable and none
     * is fabricated to fill the report.
     */
    private fun fourElectrode(
        massKg: Double,
        profile: BiaProfile,
        impedances: RawImpedances
    ): BodyCompositionReport? {
        val footToFoot = impedances[
            ImpedancePath.LEFT_FOOT_TO_RIGHT_FOOT,
            ImpedanceReading.LOW_FREQUENCY_KHZ
        ] ?: return null

        val fatFreeMassKg = FootToFootModel.of(profile.sex).evaluate(massKg, profile, footToFoot)
        if (fatFreeMassKg <= 0.0 || fatFreeMassKg >= massKg) return null

        return compose(
            massKg = massKg,
            fatFreeMassKg = fatFreeMassKg,
            skeletalMuscleMassKg = skeletalMuscle(fatFreeMassKg),
            ecwTbwRatio = DEFAULT_ECW_TBW_RATIO,
            segmentalMuscle = null,
            segmental = emptyList()
        )
    }

    private fun skeletalMuscle(fatFreeMassKg: Double) =
        SKELETAL_MUSCLE_SLOPE * fatFreeMassKg - SKELETAL_MUSCLE_INTERCEPT

    private fun compose(
        massKg: Double,
        fatFreeMassKg: Double,
        skeletalMuscleMassKg: Double,
        ecwTbwRatio: Double,
        segmentalMuscle: SegmentalMuscleMass?,
        segmental: List<SegmentalImpedances>
    ): BodyCompositionReport {
        val fatMassKg = massKg - fatFreeMassKg
        val totalBodyWaterKg = fatFreeMassKg * BROZEK_WATER_FRACTION
        val extracellularWaterKg = totalBodyWaterKg * ecwTbwRatio
        return BodyCompositionReport(
            fatFreeMassKg = fatFreeMassKg,
            fatMassKg = fatMassKg,
            bodyFatPercentage = (fatMassKg / massKg) * 100.0,
            skeletalMuscleMassKg = skeletalMuscleMassKg,
            totalBodyWaterKg = totalBodyWaterKg,
            extracellularWaterKg = extracellularWaterKg,
            intracellularWaterKg = totalBodyWaterKg - extracellularWaterKg,
            proteinMassKg = fatFreeMassKg * BROZEK_PROTEIN_FRACTION,
            boneMineralMassKg = fatFreeMassKg * BROZEK_BONE_MINERAL_FRACTION,
            ecwTbwRatio = ecwTbwRatio,
            segmentalMuscle = segmentalMuscle,
            segmental = segmental
        )
    }

    /** Spreads muscle over the five segments by relative conductance — §5. */
    private fun distributeMuscle(
        skeletalMuscleMassKg: Double,
        low: SegmentalImpedances
    ): SegmentalMuscleMass {
        val appendicularPool = skeletalMuscleMassKg * APPENDICULAR_MUSCLE_FRACTION

        val armConductance = UPPER_TO_LOWER_GEOMETRY / (low.rightArmOhms + low.leftArmOhms)
        val legConductance = 1.0 / (low.rightLegOhms + low.leftLegOhms)
        val armPool = appendicularPool * (armConductance / (armConductance + legConductance))
        val legPool = appendicularPool - armPool

        val rightArmKg = armPool * (low.leftArmOhms / (low.rightArmOhms + low.leftArmOhms))
        val rightLegKg = legPool * (low.leftLegOhms / (low.rightLegOhms + low.leftLegOhms))

        return SegmentalMuscleMass(
            rightArmKg = rightArmKg,
            leftArmKg = armPool - rightArmKg,
            rightLegKg = rightLegKg,
            leftLegKg = legPool - rightLegKg,
            trunkKg = skeletalMuscleMassKg - appendicularPool
        )
    }

    private fun extracellularRatio(low: SegmentalImpedances, high: SegmentalImpedances): Double =
        (
            ECW_RATIO_AT_REFERENCE +
                ECW_RATIO_SLOPE * ((high.bodyOhms / low.bodyOhms) - ECW_REFERENCE_FREQUENCY_RATIO)
            ).coerceIn(ECW_RATIO_FLOOR, ECW_RATIO_CEILING)
}
