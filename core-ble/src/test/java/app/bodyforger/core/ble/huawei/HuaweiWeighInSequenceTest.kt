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
    fun `une pesee est multi-etapes, du reveil a l'acquittement`() {
        assertEquals(SessionPhase.DISCOVERING, pro.first().phase)
        assertEquals(listOf(AthleteInstruction.TAP_SCALE_TO_WAKE), pro.first().instructions)
        assertEquals(SessionPhase.MEASURING, pro.last().phase)
    }

    @Test
    fun `monter et saisir la poignee forment une seule etape`() {
        val stepOn = pro.single { AthleteInstruction.STEP_ON_BAREFOOT in it.instructions }

        assertEquals(
            listOf(AthleteInstruction.STEP_ON_BAREFOOT, AthleteInstruction.GRIP_HANDLE),
            stepOn.instructions
        )
        assertEquals(1, pro.count { AthleteInstruction.GRIP_HANDLE in it.instructions })
    }

    @Test
    fun `l'etape de montee precede immediatement la mesure`() {
        val stepOn = pro.indexOfFirst { AthleteInstruction.STEP_ON_BAREFOOT in it.instructions }
        val measuring = pro.indexOfFirst { it.phase == SessionPhase.MEASURING }

        assertEquals(measuring - 1, stepOn)
    }

    @Test
    fun `la poignee n'est pas demandee a un materiel qui n'en a pas`() {
        assertFalse(plain.any { AthleteInstruction.GRIP_HANDLE in it.instructions })
        assertTrue(plain.any { AthleteInstruction.STEP_ON_BAREFOOT in it.instructions })
        assertEquals(pro.size, plain.size)
    }

    @Test
    fun `le relve et son acquittement ne font qu'une etape pour l'athlete`() {
        assertEquals(1, pro.count { it.phase == SessionPhase.MEASURING })
    }

    @Test
    fun `l'athlete reste hors du plateau pendant la negociation`() {
        val offPlatform = pro.indexOfFirst { AthleteInstruction.STAY_OFF_PLATFORM in it.instructions }
        val stepOn = pro.indexOfFirst { AthleteInstruction.STEP_ON_BAREFOOT in it.instructions }

        assertTrue(offPlatform in 0 until stepOn)
    }

    @Test
    fun `la pesee compte moins d'etapes que l'appairage, qui grave en plus`() {
        assertTrue(pro.size < HuaweiPairingSequence.stepsFor(HuaweiScaleModel.HUAWEI_SCALE_3_PRO).size)
    }
}
