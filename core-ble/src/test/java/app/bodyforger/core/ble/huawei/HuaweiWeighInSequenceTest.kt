package app.bodyforger.core.ble.huawei

import app.bodyforger.core.ble.AthleteInstruction
import app.bodyforger.core.ble.SessionPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HuaweiWeighInSequenceTest {

    private val pro = HuaweiWeighInSequence.stepsFor(HuaweiScaleModel.HUAWEI_SCALE_3_PRO)
    private val plain = HuaweiWeighInSequence.stepsFor(HuaweiScaleModel.HUAWEI_SCALE_3)

    @Test
    fun `a weigh-in is multi-step, from wake-up to acknowledgement`() {
        assertEquals(SessionPhase.DISCOVERING, pro.first().phase)
        assertEquals(listOf(AthleteInstruction.TAP_SCALE_TO_WAKE), pro.first().instructions)
        assertEquals(SessionPhase.MEASURING, pro.last().phase)
    }

    @Test
    fun `stepping on and gripping the handle form a single step`() {
        val stepOn = pro.single { AthleteInstruction.STEP_ON_BAREFOOT in it.instructions }

        assertEquals(
            listOf(AthleteInstruction.STEP_ON_BAREFOOT, AthleteInstruction.GRIP_HANDLE),
            stepOn.instructions
        )
        assertEquals(1, pro.count { AthleteInstruction.GRIP_HANDLE in it.instructions })
    }

    @Test
    fun `the step-on stage immediately precedes the measurement`() {
        val stepOn = pro.indexOfFirst { AthleteInstruction.STEP_ON_BAREFOOT in it.instructions }
        val measuring = pro.indexOfFirst { it.phase == SessionPhase.MEASURING }

        assertEquals(measuring - 1, stepOn)
    }

    @Test
    fun `the handle is not asked of hardware that has none`() {
        assertFalse(plain.any { AthleteInstruction.GRIP_HANDLE in it.instructions })
        assertTrue(plain.any { AthleteInstruction.STEP_ON_BAREFOOT in it.instructions })
        assertEquals(pro.size, plain.size)
    }

    @Test
    fun `the reading and its acknowledgement are one single step for the athlete`() {
        assertEquals(1, pro.count { it.phase == SessionPhase.MEASURING })
    }

    @Test
    fun `the athlete stays off the platform during the negotiation`() {
        val offPlatform = pro.indexOfFirst { AthleteInstruction.STAY_OFF_PLATFORM in it.instructions }
        val stepOn = pro.indexOfFirst { AthleteInstruction.STEP_ON_BAREFOOT in it.instructions }

        assertTrue(offPlatform in 0 until stepOn)
    }

    @Test
    fun `a weigh-in has fewer steps than pairing, which engraves on top`() {
        assertTrue(pro.size < HuaweiPairingSequence.stepsFor(HuaweiScaleModel.HUAWEI_SCALE_3_PRO).size)
    }
}
