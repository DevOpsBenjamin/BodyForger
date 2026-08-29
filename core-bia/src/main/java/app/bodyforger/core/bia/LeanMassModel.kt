package app.bodyforger.core.bia

import app.bodyforger.core.model.BiaProfile
import app.bodyforger.core.model.BiologicalSex

/**
 * Coefficients of the dual-frequency fat-free mass regression.
 *
 * Names match `docs/BIA_ENGINE.md` §3, where each row is tabulated.
 */
internal data class LeanMassModel(
    val lowFrequencyIndex: Double,
    val highFrequencyIndex: Double,
    val lowFrequencyOhms: Double,
    val highFrequencyOhms: Double,
    val mass: Double,
    val height: Double,
    val ageSquared: Double,
    val age: Double,
    val bias: Double
) {
    fun evaluate(
        massKg: Double,
        profile: BiaProfile,
        lowFrequencyOhms: Double,
        highFrequencyOhms: Double
    ): Double {
        val height = profile.heightCm
        val heightSquared = height * height
        val age = profile.ageYears.toDouble()
        return lowFrequencyIndex * (heightSquared / lowFrequencyOhms) +
            highFrequencyIndex * (heightSquared / highFrequencyOhms) +
            this.lowFrequencyOhms * lowFrequencyOhms +
            this.highFrequencyOhms * highFrequencyOhms +
            mass * massKg +
            this.height * height +
            ageSquared * age * age +
            this.age * age +
            bias
    }

    companion object {
        val MALE = LeanMassModel(
            lowFrequencyIndex = 0.12631,
            highFrequencyIndex = 0.16098,
            lowFrequencyOhms = -0.01195,
            highFrequencyOhms = -0.02027,
            mass = 0.14923,
            height = 0.25154,
            ageSquared = -0.000070,
            age = -0.03560,
            bias = -20.79390
        )

        val FEMALE = LeanMassModel(
            lowFrequencyIndex = 0.07182,
            highFrequencyIndex = 0.07944,
            lowFrequencyOhms = -0.01169,
            highFrequencyOhms = -0.01661,
            mass = 0.11944,
            height = 0.23935,
            ageSquared = 0.000430,
            age = -0.08840,
            bias = -14.71130
        )

        fun of(sex: BiologicalSex): LeanMassModel = if (sex == BiologicalSex.MALE) MALE else FEMALE
    }
}

/**
 * Single-frequency, four-electrode fall-back, used when only the foot-to-foot path exists.
 *
 * `docs/BIA_ENGINE.md` §3 — the only coefficients in the engine without independent
 * corroboration.
 */
internal data class FootToFootModel(
    val impedanceIndex: Double,
    val mass: Double,
    val height: Double,
    val age: Double,
    val bias: Double
) {
    fun evaluate(massKg: Double, profile: BiaProfile, footToFootOhms: Double): Double {
        val height = profile.heightCm
        return impedanceIndex * (height * height / footToFootOhms) +
            mass * massKg +
            this.height * height +
            age * profile.ageYears.toDouble() +
            bias
    }

    companion object {
        val MALE = FootToFootModel(
            impedanceIndex = 0.406,
            mass = 0.360,
            height = 0.100,
            age = -0.080,
            bias = -9.10
        )

        val FEMALE = FootToFootModel(
            impedanceIndex = 0.370,
            mass = 0.300,
            height = 0.110,
            age = -0.070,
            bias = -8.20
        )

        fun of(sex: BiologicalSex): FootToFootModel = if (sex == BiologicalSex.MALE) MALE else FEMALE
    }
}
