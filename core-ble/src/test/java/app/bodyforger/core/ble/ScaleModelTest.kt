package app.bodyforger.core.ble

import app.bodyforger.core.model.ElectrodeCount
import app.bodyforger.core.model.ImpedancePath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScaleModelTest {

    /** Nom réellement annoncé par la balance de l'équipe, relevé au scan BLE. */
    private val realAdvertisedName = "HUAWEI Scale 3 Pro-467"

    @Test
    fun `le nom annonce identifie la Pro, suffixe d'exemplaire compris`() {
        assertEquals(ScaleModel.HUAWEI_SCALE_3_PRO, ScaleModel.identify(realAdvertisedName))
    }

    @Test
    fun `la Pro est reconnue avant la Scale 3, dont elle contient le libelle`() {
        assertEquals(ScaleModel.HUAWEI_SCALE_3_PRO, ScaleModel.identify("HUAWEI Scale 3 Pro"))
        assertEquals(ScaleModel.HUAWEI_SCALE_3, ScaleModel.identify("HUAWEI Scale 3"))
    }

    @Test
    fun `la correspondance ignore la casse`() {
        assertEquals(ScaleModel.HUAWEI_SCALE_3_PRO, ScaleModel.identify("huawei scale 3 pro-467"))
        assertEquals(ScaleModel.HUAWEI_SCALE_3_PRO, ScaleModel.identify("HUAWEI SCALE 3 PRO"))
    }

    @Test
    fun `le nom GAP ne designe que la famille, sans plafond`() {
        // `HaigeBLE` est ce que remonte CoreBluetooth ; il ne distingue aucun modèle.
        val model = requireNotNull(ScaleModel.identify("HaigeBLE"))

        assertEquals(ScaleModel.HAIGE_FAMILY, model)
        assertNull("un plafond inventé vaudrait moins que pas de plafond", model.capability)
    }

    @Test
    fun `un appareil etranger n'est pas reconnu`() {
        assertNull(ScaleModel.identify("Poseidon D80 BLE"))
        assertNull(ScaleModel.identify("[TV] Samsung 7 Series (50)"))
        assertNull(ScaleModel.identify(null))
        assertNull(ScaleModel.identify(""))
    }

    @Test
    fun `le plafond de la Pro couvre les six trajets aux deux frequences`() {
        val capability = ScaleModel.HUAWEI_SCALE_3_PRO.capability!!

        assertEquals(ElectrodeCount.EIGHT, capability.electrodeCount)
        assertEquals(listOf(50, 250), capability.frequenciesKHz)
        assertEquals(6, capability.measurablePaths.size)
        assertEquals(12, capability.measurableReadings.size)
        assertTrue(capability.supportsBodyComposition)
    }

    @Test
    fun `le plafond de la Scale 3 se limite au trajet pied a pied`() {
        val capability = ScaleModel.HUAWEI_SCALE_3.capability!!

        assertEquals(ElectrodeCount.FOUR, capability.electrodeCount)
        assertEquals(listOf(50), capability.frequenciesKHz)
        assertEquals(setOf(ImpedancePath.LEFT_FOOT_TO_RIGHT_FOOT), capability.measurablePaths)
        assertEquals(1, capability.measurableReadings.size)
    }

    @Test
    fun `le facteur d'echelle des resistances est porte par le modele`() {
        // TECH.md §6.2 : le dixième d'ohm n'est pas universel dans la gamme Huawei.
        // Le facteur appartient donc au matériel, pas au décodeur — un modèle futur
        // l'ajuste sans toucher au protocole.
        for (model in ScaleModel.entries) {
            assertEquals(ScaleModel.HAIGE_OHM_DIVISOR, model.impedanceOhmDivisor, 1e-9)
        }
        assertEquals(10.0, ScaleModel.HAIGE_OHM_DIVISOR, 1e-9)
    }
}
