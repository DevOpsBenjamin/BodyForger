package app.bodyforger.core.ble.huawei

import app.bodyforger.core.ble.AthleteInstruction
import app.bodyforger.core.ble.SessionPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HuaweiWeighInSequenceTest {

    private val pro = HuaweiWeighInSequence.stepsFor(HuaweiScaleModel.HUAWEI_SCALE_3_PRO)

    @Test
    fun `une pesee est multi-etapes, du reveil a l'acquittement`() {
        assertEquals(SessionPhase.DISCOVERING, pro.first().phase)
        assertEquals(AthleteInstruction.TAP_SCALE_TO_WAKE, pro.first().instruction)
        assertEquals(SessionPhase.MEASURING, pro.last().phase)
        assertTrue(pro.size > 5)
    }

    @Test
    fun `la consigne de monter arrive apres la negociation, jamais avant`() {
        val offPlatform = pro.indexOfFirst { it.instruction == AthleteInstruction.STAY_OFF_PLATFORM }
        val stepOn = pro.indexOfFirst { it.instruction == AthleteInstruction.STEP_ON_BAREFOOT }
        val measuring = pro.indexOfFirst { it.phase == SessionPhase.MEASURING }

        assertTrue(offPlatform in 0 until stepOn)
        assertTrue(stepOn < measuring)
    }

    @Test
    fun `la poignee n'est demandee qu'a un materiel qui en a une`() {
        val plain = HuaweiWeighInSequence.stepsFor(HuaweiScaleModel.HUAWEI_SCALE_3)

        assertTrue(pro.any { it.instruction == AthleteInstruction.GRIP_HANDLE })
        assertFalse(plain.any { it.instruction == AthleteInstruction.GRIP_HANDLE })
    }

    @Test
    fun `la poignee est demandee au dernier moment, juste avant la mesure`() {
        val grip = pro.indexOfFirst { it.instruction == AthleteInstruction.GRIP_HANDLE }
        val measuring = pro.indexOfFirst { it.phase == SessionPhase.MEASURING }

        assertEquals(measuring - 1, grip)
    }

    @Test
    fun `la pesee compte moins d'etapes que l'appairage, qui grave en plus`() {
        val pairing = HuaweiPairingSequence.stepsFor(HuaweiScaleModel.HUAWEI_SCALE_3_PRO)

        assertTrue(pro.size < pairing.size)
    }
}
