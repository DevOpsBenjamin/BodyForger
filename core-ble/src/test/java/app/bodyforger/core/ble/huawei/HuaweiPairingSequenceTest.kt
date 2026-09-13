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
        // Pairing only wants a tare: a calibration mass, not an impedance reading. The
        // handle is of no use to it, even on hardware that has one.
        for (model in HuaweiScaleModel.entries) {
            val steps = HuaweiPairingSequence.stepsFor(model)
            assertFalse("$model", steps.any { AthleteInstruction.GRIP_HANDLE in it.instructions })
            assertFalse("$model", steps.any { AthleteInstruction.STEP_ON_BAREFOOT in it.instructions })
            assertTrue("$model", steps.any { AthleteInstruction.STEP_ON in it.instructions })
        }
    }

    @Test
    fun `a model with no known ceiling pairs like the others`() {
        // An unknown model is assumed to do no more than return a mass: the handle is never
        // demanded of hardware whose capability is not established.
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
