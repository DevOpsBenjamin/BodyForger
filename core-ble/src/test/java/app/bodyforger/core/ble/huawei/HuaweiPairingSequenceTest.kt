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
    fun `l'appairage Haige exige une pesee, contrairement au contrat generique`() {
        assertEquals(PairingRequirement.WEIGH_IN_REQUIRED, HuaweiPairingSequence.requirement)

        val steps = HuaweiPairingSequence.stepsFor(HuaweiScaleModel.HUAWEI_SCALE_3_PRO)
        assertTrue(steps.any { it.instruction == AthleteInstruction.STEP_ON_BAREFOOT })
        assertEquals(SessionPhase.MEASURING, steps.last().phase)
    }

    @Test
    fun `la poignee n'est demandee qu'a un materiel qui en a une`() {
        val pro = HuaweiPairingSequence.stepsFor(HuaweiScaleModel.HUAWEI_SCALE_3_PRO)
        val plain = HuaweiPairingSequence.stepsFor(HuaweiScaleModel.HUAWEI_SCALE_3)

        assertTrue(pro.any { it.instruction == AthleteInstruction.GRIP_HANDLE })
        assertFalse(plain.any { it.instruction == AthleteInstruction.GRIP_HANDLE })
    }

    @Test
    fun `un modele sans plafond connu ne reclame pas la poignee`() {
        val family = HuaweiPairingSequence.stepsFor(HuaweiScaleModel.HAIGE_FAMILY)

        assertFalse(family.any { it.instruction == AthleteInstruction.GRIP_HANDLE })
        assertTrue(family.any { it.instruction == AthleteInstruction.STEP_ON_BAREFOOT })
    }

    @Test
    fun `l'athlete reste hors du plateau pendant la negociation`() {
        val steps = HuaweiPairingSequence.stepsFor(HuaweiScaleModel.HUAWEI_SCALE_3_PRO)

        val offPlatform = steps.indexOfFirst { it.instruction == AthleteInstruction.STAY_OFF_PLATFORM }
        val stepOn = steps.indexOfFirst { it.instruction == AthleteInstruction.STEP_ON_BAREFOOT }
        assertTrue(offPlatform in 0 until stepOn)
    }
}
