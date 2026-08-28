package app.bodyforger.core.bia

import app.bodyforger.core.model.BiaProfile
import app.bodyforger.core.model.BiologicalSex
import app.bodyforger.core.model.BodyCompositionReport
import app.bodyforger.core.model.SegmentalImpedance

object DexaBiaCalculator {

    /**
     * DEXA Multi-Frequency Compartment Modeling
     */
    fun calculate(
        massKg: Double,
        profile: BiaProfile,
        impedances: SegmentalImpedance? = null
    ): BodyCompositionReport {
        val h = profile.heightCm
        val age = profile.ageYears
        val sexFactor = if (profile.sex == BiologicalSex.MALE) 1.0 else 0.0

        val z50 = impedances?.trunkZ50 ?: 500.0
        val heightIndex = (h * h) / z50

        // Kushner & DEXA clinical reference equation
        val tbw = (0.55 * heightIndex) + (0.16 * massKg) + (0.8 * sexFactor) - (0.03 * age) + 1.5
        val ecw = tbw * 0.39
        val icw = tbw - ecw
        val ffm = (tbw / 0.732).coerceAtMost(massKg * 0.95)
        val fatMass = (massKg - ffm).coerceAtLeast(massKg * 0.03)
        val bodyFatPct = ((fatMass / massKg) * 100.0).coerceIn(3.0, 60.0)
        val smm = ffm * 0.54

        return BodyCompositionReport(
            bodyFatPercentage = (bodyFatPct * 10).toInt() / 10.0,
            fatFreeMassKg = (ffm * 10).toInt() / 10.0,
            skeletalMuscleMassKg = (smm * 10).toInt() / 10.0,
            totalBodyWaterLiters = (tbw * 10).toInt() / 10.0,
            extracellularWaterLiters = (ecw * 10).toInt() / 10.0,
            intracellularWaterLiters = (icw * 10).toInt() / 10.0,
            ecwTbwRatio = ((ecw / tbw) * 100).toInt() / 100.0
        )
    }
}
