package app.bodyforger.core.bia

import app.bodyforger.core.model.BiaProfile
import app.bodyforger.core.model.BiologicalSex
import app.bodyforger.core.model.ImpedancePath
import app.bodyforger.core.model.ImpedanceReading
import app.bodyforger.core.model.RawImpedances
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pins ForgeFit to the figures verified in the Python reference (`bodyforger-bia-private`)
 * on a real weigh-in: 6 Sept 2026, the athlete's low outlier reading.
 */
class ForgeFitModelTest {

    private val profile = BiaProfile(sex = BiologicalSex.MALE, ageYears = 34, heightCm = 175.0)

    // Raw ohms of the 6 Sept 2026 weigh-in (50 kHz then 250 kHz).
    private fun weighIn(): RawImpedances = RawImpedances.of(
        buildMap {
            fun put(path: ImpedancePath, low: Double, high: Double) {
                put(ImpedanceReading(path, ImpedanceReading.LOW_FREQUENCY_KHZ), low)
                put(ImpedanceReading(path, ImpedanceReading.HIGH_FREQUENCY_KHZ), high)
            }
            put(ImpedancePath.LEFT_FOOT_TO_RIGHT_FOOT, 381.1, 332.1)
            put(ImpedancePath.LEFT_HAND_TO_RIGHT_HAND, 419.3, 321.9)
            put(ImpedancePath.LEFT_HAND_TO_LEFT_FOOT, 436.9, 366.9)
            put(ImpedancePath.LEFT_HAND_TO_RIGHT_FOOT, 434.7, 364.2)
            put(ImpedancePath.RIGHT_HAND_TO_LEFT_FOOT, 443.4, 380.4)
            put(ImpedancePath.RIGHT_HAND_TO_RIGHT_FOOT, 440.6, 377.9)
        }
    )

    @Test
    fun `reproduces the verified fat percentage on a real weigh-in`() {
        val report = ForgeFitModel.analyze(massKg = 102.6, profile = profile, impedances = weighIn())
        assertNotNull(report)
        // Python reference (bodyforger_bia.py) on the exact ohms: 32.17 %.
        assertEquals(32.17, report!!.bodyFatPercentage, 0.05)
        assertTrue("FFM must be plausible", report.fatFreeMassKg in 68.0..76.0)
    }

    @Test
    fun `four electrodes yields a report without segmental detail`() {
        val footToFootOnly = RawImpedances.of(
            mapOf(
                ImpedanceReading(ImpedancePath.LEFT_FOOT_TO_RIGHT_FOOT, ImpedanceReading.LOW_FREQUENCY_KHZ) to 381.1
            )
        )
        val report = ForgeFitModel.analyze(102.6, profile, footToFootOnly)
        assertNotNull(report)
        // No hand path: no Kirchhoff decomposition, hence no segmental detail.
        assertEquals(null, report!!.segmentalMuscle)
        assertEquals(null, report.segmentalFat)
        assertEquals(null, report.visceralFatLevel)
    }

    @Test
    fun `distributes muscle and fat over the segments and derives visceral`() {
        val report = ForgeFitModel.analyze(massKg = 102.6, profile = profile, impedances = weighIn())!!

        val muscle = report.segmentalMuscle
        assertNotNull(muscle)
        // Segments partition the whole, by construction.
        assertEquals(report.skeletalMuscleMassKg, muscle!!.totalKg, 1e-6)
        // Legs carry far more muscle than arms; the trunk is the remainder.
        assertTrue("legs > arms", muscle.rightLegKg > muscle.rightArmKg)
        assertEquals(3.82, muscle.rightArmKg, 0.05)
        assertEquals(9.11, muscle.rightLegKg, 0.05)

        val fat = report.segmentalFat
        assertNotNull(fat)
        assertEquals(report.fatMassKg, fat!!.totalKg, 1e-6)
        // Non-negative everywhere, and the trunk dominates.
        assertTrue("arm fat non-negative", fat.rightArmKg >= 0.0)
        assertTrue("trunk holds the most fat", fat.trunkKg > fat.limbsKg / 2.0)
        assertEquals(20.68, fat.trunkKg, 0.1)

        // BodyForger's own visceral index (not a medical VAT measurement).
        assertEquals(15, report.visceralFatLevel)

        // Composition score: fat and visceral pull down, muscle lifts within a cap.
        assertNotNull(report.bodyScore)
        assertEquals(71.0, report.bodyScore!!, 0.5)
    }

    @Test
    fun `fills BMR and metabolic body age`() {
        val report = ForgeFitModel.analyze(massKg = 102.6, profile = profile, impedances = weighIn())!!
        // Katch-McArdle: 370 + 21.6 * FFM ; body age = age + (BMI - 25).
        assertEquals(370.0 + 21.6 * report.fatFreeMassKg, report.basalMetabolismKcal!!, 0.01)
        assertEquals(43, report.metabolicBodyAge)   // BMI 33.5 -> 34 + 8.5 = 43
    }
}
