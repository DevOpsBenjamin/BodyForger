package app.bodyforger.core.ble.huawei

import app.bodyforger.core.ble.AthleteInstruction
import app.bodyforger.core.ble.PairingRequirement
import app.bodyforger.core.ble.SessionPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HuaweiPairingSequenceTest {

    @Test
    fun `Haige pairing requires a weigh-in, unlike the generic contract`() {
        assertEquals(PairingRequirement.WEIGH_IN_REQUIRED, HuaweiPairingSequence.requirement)

        val steps = HuaweiPairingSequence.stepsFor(HuaweiScaleModel.HUAWEI_SCALE_3_PRO)
        assertTrue(steps.any { AthleteInstruction.STEP_ON in it.instructions })
        assertEquals(SessionPhase.MEASURING, steps.last().phase)
    }

    @Test
    fun `no model demands the handle in order to pair`() {
        // L'appairage ne cherche qu'une tare : une masse de calibration, pas une mesure
        // d'impedance. La poignee n'y sert a rien, meme sur un materiel qui en a une.
        for (model in HuaweiScaleModel.entries) {
            val steps = HuaweiPairingSequence.stepsFor(model)
            assertFalse("$model", steps.any { AthleteInstruction.GRIP_HANDLE in it.instructions })
            assertFalse("$model", steps.any { AthleteInstruction.STEP_ON_BAREFOOT in it.instructions })
            assertTrue("$model", steps.any { AthleteInstruction.STEP_ON in it.instructions })
        }
    }

    @Test
    fun `a model with no known ceiling pairs like the others`() {
        // sait rendre une masse.
        val family = HuaweiPairingSequence.stepsFor(HuaweiScaleModel.HAIGE_FAMILY)

        assertFalse(family.any { AthleteInstruction.GRIP_HANDLE in it.instructions })
        assertTrue(family.any { AthleteInstruction.STEP_ON in it.instructions })
    }

    @Test
    fun `the athlete stays off the platform during the negotiation`() {
        val steps = HuaweiPairingSequence.stepsFor(HuaweiScaleModel.HUAWEI_SCALE_3_PRO)

        val offPlatform = steps.indexOfFirst { AthleteInstruction.STAY_OFF_PLATFORM in it.instructions }
        val stepOn = steps.indexOfFirst { AthleteInstruction.STEP_ON in it.instructions }
        assertTrue(offPlatform in 0 until stepOn)
    }
}
