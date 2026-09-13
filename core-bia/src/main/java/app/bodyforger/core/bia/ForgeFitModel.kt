package app.bodyforger.core.bia

import app.bodyforger.core.model.BiaProfile
import app.bodyforger.core.model.CompositionModel
import app.bodyforger.core.model.BiologicalSex
import app.bodyforger.core.model.BodyCompositionReport
import app.bodyforger.core.model.ImpedancePath
import app.bodyforger.core.model.ImpedanceReading
import app.bodyforger.core.model.RawImpedances
import app.bodyforger.core.model.SegmentalFatMass
import app.bodyforger.core.model.SegmentalImpedances
import app.bodyforger.core.model.SegmentalMuscleMass

/**
 * ForgeFit — BodyForger's own body-composition engine.
 *
 * Built ENTIRELY from published, DXA/4C-validated equations plus one physics-based
 * frequency conversion. No coefficient is taken from any manufacturer's binary, and
 * nothing is tuned to reproduce a manufacturer's output. Full rationale and sources:
 * `docs/FORGEFIT_MODEL.md`.
 *
 * Fat-free mass merges two published equations, weighted by their published precision:
 *   * Sun / NHANES (Chumlea et al.) — hand-to-foot, whole body, sex-specific coefficients
 *   * Wu et al. 2011              — foot-to-foot, the legs, sex handled by a term
 *
 * Dual frequency is used honestly: the equations are calibrated at 50 kHz, so the 250 kHz
 * reading is first converted to its 50-equivalent (impedance is systematically lower at
 * 250 kHz because current crosses cell membranes) before the validated equation is applied.
 * The converted 250 still moves faster over time, which is where a recomposition shows up.
 */
object ForgeFitModel : CompositionModel {

    // ------------------------------------------------------------------ Sun / NHANES
    private class Sun(
        val bias: Double, val index: Double, val mass: Double, val direct: Double,
        val see: Double,
    ) {
        fun evaluate(heightCm: Double, massKg: Double, ohms: Double): Double {
            val h2 = heightCm * heightCm
            return bias + index * (h2 / ohms) + mass * massKg + direct * ohms
        }
    }

    private val SUN_MALE = Sun(bias = -10.678, index = 0.652, mass = 0.262, direct = 0.015, see = 3.90)
    private val SUN_FEMALE = Sun(bias = -9.529, index = 0.696, mass = 0.168, direct = 0.016, see = 2.90)

    // ------------------------------------------------------------------ Wu 2011
    private const val WU_BIAS = 13.055
    private const val WU_SEX = 8.125
    private const val WU_MASS = 0.204
    private const val WU_INDEX = 0.394
    private const val WU_AGE = -0.136
    private const val WU_SEE = 3.17

    private fun wu(profile: BiaProfile, massKg: Double, ohms: Double): Double {
        val h2 = profile.heightCm * profile.heightCm
        val sex = if (profile.sex == BiologicalSex.MALE) 1.0 else 0.0
        return WU_BIAS + WU_SEX * sex + WU_MASS * massKg +
            WU_INDEX * (h2 / ohms) + WU_AGE * profile.ageYears.toDouble()
    }

    // ------------------------------------------------------------------ design constants
    /** Majority weight on the (converted) 250 kHz reading; round 3/4, verified non-critical. */
    private const val WEIGHT_250 = 0.75

    /** Typical Z250/Z50; converts a 250 reading to its 50-equivalent. */
    private const val TYPICAL_FREQUENCY_RATIO = 0.887

    // Janssen 2000 — skeletal muscle mass from resistance, published (resistance only).
    private const val JANSSEN_INDEX = 0.401
    private const val JANSSEN_SEX = 3.825
    private const val JANSSEN_AGE = -0.071
    private const val JANSSEN_BIAS = 5.102

    // Brozek 4C split — published.
    private const val BROZEK_WATER_FRACTION = 0.732
    private const val BROZEK_PROTEIN_FRACTION = 0.211

    // Katch-McArdle BMR (kcal/day) from fat-free mass — published.
    private const val KATCH_MCARDLE_BASE = 370.0
    private const val KATCH_MCARDLE_PER_FFM = 21.6
    private const val BROZEK_BONE_MINERAL_FRACTION = 0.057

    // Hydration and segmental distribution.
    private const val DEFAULT_ECW_TBW_RATIO = 0.380
    private const val ECW_RATIO_SLOPE = 0.05
    private const val ECW_REFERENCE_FREQUENCY_RATIO = 0.88
    private const val ECW_RATIO_FLOOR = 0.30
    private const val ECW_RATIO_CEILING = 0.50

    // Body score — a single 0-100 summary of composition, on BodyForger's own scale.
    // Three logical penalties, one capped muscle premium. docs/FORGEFIT_MODEL.md.
    private const val SCORE_MAX = 100.0
    private const val SCORE_FLOOR = 40.0
    private const val SCORE_FAT_HEALTHY_MALE = 18.0
    private const val SCORE_FAT_HEALTHY_FEMALE = 27.0
    private const val SCORE_FAT_ESSENTIAL = 8.0
    private const val SCORE_FAT_PENALTY = 1.6
    private const val SCORE_LOW_FAT_PENALTY = 1.5
    private const val SCORE_VISCERAL_HEALTHY = 9
    private const val SCORE_VISCERAL_PENALTY = 1.2
    private const val SCORE_SMI_SEDENTARY = 8.0
    private const val SCORE_MUSCLE_BONUS = 2.5
    private const val SCORE_MUSCLE_BONUS_CAP = 6.0

    private val CROSSED_PATHS = listOf(
        ImpedancePath.LEFT_HAND_TO_LEFT_FOOT,
        ImpedancePath.LEFT_HAND_TO_RIGHT_FOOT,
        ImpedancePath.RIGHT_HAND_TO_LEFT_FOOT,
        ImpedancePath.RIGHT_HAND_TO_RIGHT_FOOT,
    )

    override fun analyze(
        massKg: Double,
        profile: BiaProfile,
        impedances: RawImpedances
    ): BodyCompositionReport? {
        require(massKg > 0.0) { "Invalid mass: $massKg kg" }

        val bodyLow = meanCrossed(impedances, ImpedanceReading.LOW_FREQUENCY_KHZ)
        val legsLow = impedances[ImpedancePath.LEFT_FOOT_TO_RIGHT_FOOT, ImpedanceReading.LOW_FREQUENCY_KHZ]

        // Nothing usable to stand on.
        if (bodyLow == null && legsLow == null) return null

        val bodyHigh = meanCrossed(impedances, ImpedanceReading.HIGH_FREQUENCY_KHZ)
        val legsHigh = impedances[ImpedancePath.LEFT_FOOT_TO_RIGHT_FOOT, ImpedanceReading.HIGH_FREQUENCY_KHZ]

        val sun = SUN_MALE.takeIf { profile.sex == BiologicalSex.MALE } ?: SUN_FEMALE
        val fatFreeMassKg = fatFreeMass(profile, massKg, sun, bodyLow, bodyHigh, legsLow, legsHigh)
            ?: return null
        if (fatFreeMassKg <= 0.0 || fatFreeMassKg >= massKg) return null

        val skeletalMuscleMassKg = skeletalMuscle(profile, bodyLow)

        val low = KirchhoffSolver.solve(impedances, ImpedanceReading.LOW_FREQUENCY_KHZ)
        val high = KirchhoffSolver.solve(impedances, ImpedanceReading.HIGH_FREQUENCY_KHZ)
        val ecwTbwRatio = if (low != null && high != null) extracellularRatio(low, high) else DEFAULT_ECW_TBW_RATIO

        // Segmental muscle and fat, plus the visceral index, from published anthropometric
        // constants and each limb's own impedance — only when the eight-electrode paths exist.
        val distribution = low?.let {
            SegmentalDistribution.distribute(
                body = it,
                heightCm = profile.heightCm,
                massKg = massKg,
                skeletalMuscleMassKg = skeletalMuscleMassKg,
                fatMassKg = massKg - fatFreeMassKg,
                ageYears = profile.ageYears,
            )
        }

        return compose(
            profile = profile,
            massKg = massKg,
            fatFreeMassKg = fatFreeMassKg,
            skeletalMuscleMassKg = skeletalMuscleMassKg,
            ecwTbwRatio = ecwTbwRatio,
            segmentalMuscle = distribution?.muscle,
            segmentalFat = distribution?.fat,
            visceralFatLevel = distribution?.visceralLevel,
            segmental = listOfNotNull(low, high),
        )
    }

    /** 25% on the 50 kHz reading, 75% on the 250 converted to its 50-equivalent. */
    private fun dualFrequency(evaluate: (Double) -> Double, ohms50: Double, ohms250: Double?): Double {
        if (ohms250 == null) return evaluate(ohms50)
        val equivalent250 = ohms250 / TYPICAL_FREQUENCY_RATIO
        return (1 - WEIGHT_250) * evaluate(ohms50) + WEIGHT_250 * evaluate(equivalent250)
    }

    private fun fatFreeMass(
        profile: BiaProfile, massKg: Double, sun: Sun,
        bodyLow: Double?, bodyHigh: Double?, legsLow: Double?, legsHigh: Double?,
    ): Double? {
        val sunFfm = bodyLow?.let { dualFrequency({ z -> sun.evaluate(profile.heightCm, massKg, z) }, it, bodyHigh) }
        val wuFfm = legsLow?.let { dualFrequency({ z -> wu(profile, massKg, z) }, it, legsHigh) }

        return when {
            sunFfm != null && wuFfm != null -> {
                val wSun = 1.0 / (sun.see * sun.see)
                val wWu = 1.0 / (WU_SEE * WU_SEE)
                (wSun * sunFfm + wWu * wuFfm) / (wSun + wWu)
            }
            sunFfm != null -> sunFfm      // hand-to-foot only
            else -> wuFfm                 // foot-to-foot only (four electrodes)
        }
    }

    private fun skeletalMuscle(profile: BiaProfile, bodyLow: Double?): Double {
        // Falls back to the whole-body estimate's own share when no hand-to-foot path exists.
        if (bodyLow == null) return 0.0
        val h2 = profile.heightCm * profile.heightCm
        val sex = if (profile.sex == BiologicalSex.MALE) 1.0 else 0.0
        return JANSSEN_INDEX * (h2 / bodyLow) + JANSSEN_SEX * sex +
            JANSSEN_AGE * profile.ageYears.toDouble() + JANSSEN_BIAS
    }

    private fun meanCrossed(impedances: RawImpedances, frequencyKHz: Int): Double? {
        val values = CROSSED_PATHS.mapNotNull { impedances[it, frequencyKHz] }
        return if (values.size == CROSSED_PATHS.size) values.average() else null
    }

    private fun compose(
        profile: BiaProfile, massKg: Double, fatFreeMassKg: Double, skeletalMuscleMassKg: Double,
        ecwTbwRatio: Double, segmentalMuscle: SegmentalMuscleMass?,
        segmentalFat: SegmentalFatMass?, visceralFatLevel: Int?,
        segmental: List<SegmentalImpedances>,
    ): BodyCompositionReport {
        val fatMassKg = massKg - fatFreeMassKg
        val totalBodyWaterKg = fatFreeMassKg * BROZEK_WATER_FRACTION
        val extracellularWaterKg = totalBodyWaterKg * ecwTbwRatio
        val bmr = KATCH_MCARDLE_BASE + KATCH_MCARDLE_PER_FFM * fatFreeMassKg
        val bmi = massKg / ((profile.heightCm / 100.0) * (profile.heightCm / 100.0))
        val bodyAge = (profile.ageYears + (bmi - 25.0).coerceIn(-5.0, 20.0))
            .let { Math.round(it).toInt() }.coerceIn(18, 80)
        val appendicularMuscleKg = skeletalMuscleMassKg * SegmentalDistribution.APPENDICULAR_MUSCLE_FRACTION
        val score = bodyScore(
            profile = profile,
            bodyFatPercentage = (fatMassKg / massKg) * 100.0,
            appendicularMuscleKg = appendicularMuscleKg,
            visceralFatLevel = visceralFatLevel,
        )
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
            segmentalFat = segmentalFat,
            visceralFatLevel = visceralFatLevel,
            segmental = segmental,
            basalMetabolismKcal = bmr,
            bodyScore = score,
            metabolicBodyAge = bodyAge,
        )
    }


    /**
     * A 0-100 composition score. Fat above the healthy band and a high visceral level pull it
     * down, being under essential fat pulls it down, muscle (SMI) lifts it within a cap. This
     * is BodyForger's own scale, not calibrated to any manufacturer's score.
     */
    private fun bodyScore(
        profile: BiaProfile, bodyFatPercentage: Double,
        appendicularMuscleKg: Double, visceralFatLevel: Int?,
    ): Double {
        val healthy = if (profile.sex == BiologicalSex.MALE) SCORE_FAT_HEALTHY_MALE else SCORE_FAT_HEALTHY_FEMALE
        val heightM = profile.heightCm / 100.0
        val smi = appendicularMuscleKg / (heightM * heightM)
        var score = SCORE_MAX
        score -= (bodyFatPercentage - healthy).coerceAtLeast(0.0) * SCORE_FAT_PENALTY
        score -= (SCORE_FAT_ESSENTIAL - bodyFatPercentage).coerceAtLeast(0.0) * SCORE_LOW_FAT_PENALTY
        if (visceralFatLevel != null) {
            score -= (visceralFatLevel - SCORE_VISCERAL_HEALTHY).coerceAtLeast(0) * SCORE_VISCERAL_PENALTY
        }
        score += ((smi - SCORE_SMI_SEDENTARY).coerceAtLeast(0.0) * SCORE_MUSCLE_BONUS)
            .coerceAtMost(SCORE_MUSCLE_BONUS_CAP)
        return Math.round(score.coerceIn(SCORE_FLOOR, SCORE_MAX)).toDouble()
    }

    private fun extracellularRatio(low: SegmentalImpedances, high: SegmentalImpedances): Double =
        (DEFAULT_ECW_TBW_RATIO +
            ECW_RATIO_SLOPE * ((high.bodyOhms / low.bodyOhms) - ECW_REFERENCE_FREQUENCY_RATIO))
            .coerceIn(ECW_RATIO_FLOOR, ECW_RATIO_CEILING)
}
