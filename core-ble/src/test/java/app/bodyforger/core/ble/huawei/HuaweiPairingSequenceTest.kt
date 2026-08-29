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
        assertTrue(steps.any { AthleteInstruction.STEP_ON in it.instructions })
        assertEquals(SessionPhase.MEASURING, steps.last().phase)
    }

    @Test
    fun `aucun modele ne reclame la poignee pour s'appairer`() {
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
    fun `un modele sans plafond connu s'appaire comme les autres`() {
        // sait rendre une masse.
        val family = HuaweiPairingSequence.stepsFor(HuaweiScaleModel.HAIGE_FAMILY)

        assertFalse(family.any { AthleteInstruction.GRIP_HANDLE in it.instructions })
        assertTrue(family.any { AthleteInstruction.STEP_ON in it.instructions })
    }

    @Test
    fun `l'athlete reste hors du plateau pendant la negociation`() {
        val steps = HuaweiPairingSequence.stepsFor(HuaweiScaleModel.HUAWEI_SCALE_3_PRO)

        val offPlatform = steps.indexOfFirst { AthleteInstruction.STAY_OFF_PLATFORM in it.instructions }
        val stepOn = steps.indexOfFirst { AthleteInstruction.STEP_ON in it.instructions }
        assertTrue(offPlatform in 0 until stepOn)
    }
}
