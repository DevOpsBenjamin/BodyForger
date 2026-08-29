package app.bodyforger.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class CapabilityModelTest {

    @Test
    fun `le plafond majore, il ne constate pas`() {
        val pro = ScaleCapability(ElectrodeCount.EIGHT, listOf(50, 250))

        val obtained = RawImpedances.NONE

        assertEquals(12, pro.measurableReadings.size)
        assertTrue(obtained.fidelity.isEmpty)
        assertEquals(ElectrodeCount.NONE, obtained.fidelity.exercisedElectrodeCount)
    }

    @Test
    fun `une valeur nulle est ecartee, jamais conservee a zero`() {
        val impedances = RawImpedances.of(
            mapOf(
                ImpedanceReading(ImpedancePath.LEFT_FOOT_TO_RIGHT_FOOT, 50) to 509.8,
                ImpedanceReading(ImpedancePath.LEFT_HAND_TO_RIGHT_HAND, 50) to 0.0,
                ImpedanceReading(ImpedancePath.LEFT_HAND_TO_LEFT_FOOT, 50) to -1.0
            )
        )

        assertEquals(1, impedances.ohmsByReading.size)
        assertNull(impedances[ImpedancePath.LEFT_HAND_TO_RIGHT_HAND, 50])
        assertEquals(ElectrodeCount.FOUR, impedances.fidelity.exercisedElectrodeCount)
    }

    @Test
    fun `la fidelite obtenue se deduit des trajets, pas du modele declare`() {
        val handsInvolved = RawImpedances.of(
            mapOf(ImpedanceReading(ImpedancePath.RIGHT_HAND_TO_LEFT_FOOT, 50) to 475.7)
        )

        assertEquals(ElectrodeCount.EIGHT, handsInvolved.fidelity.exercisedElectrodeCount)
    }

    @Test
    fun `sans electrode, aucune frequence n'est relevable`() {
        assertEquals(emptySet<ImpedanceReading>(), ScaleCapability.WEIGHT_ONLY.measurableReadings)
        assertTrue(!ScaleCapability.WEIGHT_ONLY.supportsBodyComposition)

        assertThrows(IllegalArgumentException::class.java) {
            ScaleCapability(ElectrodeCount.NONE, listOf(50))
        }
        assertThrows(IllegalArgumentException::class.java) {
            ScaleCapability(ElectrodeCount.EIGHT, emptyList())
        }
    }

    @Test
    fun `les six trajets suivent l'ordre de la trame`() {
        assertEquals(
            listOf(0, 1, 2, 3, 4, 5),
            ImpedancePath.BY_WIRE_INDEX.map { it.wireIndex }
        )
        assertEquals(
            ImpedancePath.LEFT_FOOT_TO_RIGHT_FOOT,
            ImpedancePath.BY_WIRE_INDEX.first()
        )
        assertEquals(1, ImpedancePath.entries.count { !it.involvesHands })
    }
}
