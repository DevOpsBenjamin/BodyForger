package app.bodyforger.core.bia

import app.bodyforger.core.model.BiaProfile
import app.bodyforger.core.model.BiologicalSex
import org.junit.Assert.assertTrue
import org.junit.Test

class DexaBiaCalculatorTest {

    @Test
    fun testDexaBiaCalculation() {
        val profile = BiaProfile(
            sex = BiologicalSex.MALE,
            ageYears = 28,
            heightCm = 182.0
        )
        val report = DexaBiaCalculator.calculate(
            massKg = 80.0,
            profile = profile
        )
        assertTrue(report.bodyFatPercentage in 5.0..35.0)
        assertTrue(report.skeletalMuscleMassKg > 20.0)
        assertTrue(report.totalBodyWaterLiters > 30.0)
        assertTrue(report.ecwTbwRatio in 0.35..0.45)
    }
}
