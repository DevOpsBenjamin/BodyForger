package app.bodyforger.core.ble.huawei

import app.bodyforger.core.model.ElectrodeCount
import app.bodyforger.core.model.ImpedancePath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HuaweiScaleModelTest {

    /** Nom réellement annoncé par la balance de l'équipe, relevé au scan BLE. */
    private val realAdvertisedName = "HUAWEI Scale 3 Pro-467"

    @Test
    fun `le nom annonce identifie la Pro, suffixe d'exemplaire compris`() {
        assertEquals(HuaweiScaleModel.HUAWEI_SCALE_3_PRO, HuaweiScaleModel.identify(realAdvertisedName))
    }

    @Test
    fun `la Pro est reconnue avant la Scale 3, dont elle contient le libelle`() {
        assertEquals(HuaweiScaleModel.HUAWEI_SCALE_3_PRO, HuaweiScaleModel.identify("HUAWEI Scale 3 Pro"))
        assertEquals(HuaweiScaleModel.HUAWEI_SCALE_3, HuaweiScaleModel.identify("HUAWEI Scale 3"))
    }

    @Test
    fun `la correspondance ignore la casse`() {
        assertEquals(HuaweiScaleModel.HUAWEI_SCALE_3_PRO, HuaweiScaleModel.identify("huawei scale 3 pro-467"))
        assertEquals(HuaweiScaleModel.HUAWEI_SCALE_3_PRO, HuaweiScaleModel.identify("HUAWEI SCALE 3 PRO"))
    }

    @Test
    fun `le nom GAP ne designe que la famille, sans plafond`() {
        val model = requireNotNull(HuaweiScaleModel.identify("HaigeBLE"))

        assertEquals(HuaweiScaleModel.HAIGE_FAMILY, model)
        assertNull("un plafond inventé vaudrait moins que pas de plafond", model.capability)
    }

    @Test
    fun `un appareil etranger n'est pas reconnu`() {
        assertNull(HuaweiScaleModel.identify("Poseidon D80 BLE"))
        assertNull(HuaweiScaleModel.identify("[TV] Samsung 7 Series (50)"))
        assertNull(HuaweiScaleModel.identify(null))
        assertNull(HuaweiScaleModel.identify(""))
    }

    @Test
    fun `le plafond de la Pro couvre les six trajets aux deux frequences`() {
        val capability = HuaweiScaleModel.HUAWEI_SCALE_3_PRO.capability!!

        assertEquals(ElectrodeCount.EIGHT, capability.electrodeCount)
        assertEquals(listOf(50, 250), capability.frequenciesKHz)
        assertEquals(6, capability.measurablePaths.size)
        assertEquals(12, capability.measurableReadings.size)
        assertTrue(capability.supportsBodyComposition)
    }

    @Test
    fun `le plafond de la Scale 3 se limite au trajet pied a pied`() {
        val capability = HuaweiScaleModel.HUAWEI_SCALE_3.capability!!

        assertEquals(ElectrodeCount.FOUR, capability.electrodeCount)
        assertEquals(listOf(50), capability.frequenciesKHz)
        assertEquals(setOf(ImpedancePath.LEFT_FOOT_TO_RIGHT_FOOT), capability.measurablePaths)
        assertEquals(1, capability.measurableReadings.size)
    }

    @Test
    fun `le facteur d'echelle des resistances est porte par le modele`() {
        // l'ajuste sans toucher au protocole.
        for (model in HuaweiScaleModel.entries) {
            assertEquals(HuaweiScaleModel.HAIGE_OHM_DIVISOR, model.impedanceOhmDivisor, 1e-9)
        }
        assertEquals(10.0, HuaweiScaleModel.HAIGE_OHM_DIVISOR, 1e-9)
    }
}
