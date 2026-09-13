package app.bodyforger.core.ble.huawei

import app.bodyforger.core.model.ElectrodeCount
import app.bodyforger.core.model.ImpedancePath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HuaweiScaleModelTest {

    /** The name the team's scale actually advertises, read off a BLE scan. */
    private val realAdvertisedName = "HUAWEI Scale 3 Pro-467"

    @Test
    fun `the advertised name identifies the Pro, unit suffix included`() {
        assertEquals(HuaweiScaleModel.HUAWEI_SCALE_3_PRO, HuaweiScaleModel.identify(realAdvertisedName))
    }

    @Test
    fun `the Pro is matched before the Scale 3, whose label it contains`() {
        assertEquals(HuaweiScaleModel.HUAWEI_SCALE_3_PRO, HuaweiScaleModel.identify("HUAWEI Scale 3 Pro"))
        assertEquals(HuaweiScaleModel.HUAWEI_SCALE_3, HuaweiScaleModel.identify("HUAWEI Scale 3"))
    }

    @Test
    fun `matching ignores case`() {
        assertEquals(HuaweiScaleModel.HUAWEI_SCALE_3_PRO, HuaweiScaleModel.identify("huawei scale 3 pro-467"))
        assertEquals(HuaweiScaleModel.HUAWEI_SCALE_3_PRO, HuaweiScaleModel.identify("HUAWEI SCALE 3 PRO"))
    }

    @Test
    fun `the GAP name designates the family alone, with no ceiling`() {
        val model = requireNotNull(HuaweiScaleModel.identify("HaigeBLE"))

        assertEquals(HuaweiScaleModel.HAIGE_FAMILY, model)
        assertNull("an invented ceiling would be worth less than nas de plafond", model.capability)
    }

    @Test
    fun `a foreign device is not recognised`() {
        assertNull(HuaweiScaleModel.identify("Poseidon D80 BLE"))
        assertNull(HuaweiScaleModel.identify("[TV] Samsung 7 Series (50)"))
        assertNull(HuaweiScaleModel.identify(null))
        assertNull(HuaweiScaleModel.identify(""))
    }

    @Test
    fun `the Pro's ceiling covers the six paths at both frequencies`() {
        val capability = HuaweiScaleModel.HUAWEI_SCALE_3_PRO.capability!!

        assertEquals(ElectrodeCount.EIGHT, capability.electrodeCount)
        assertEquals(listOf(50, 250), capability.frequenciesKHz)
        assertEquals(6, capability.measurablePaths.size)
        assertEquals(12, capability.measurableReadings.size)
        assertTrue(capability.supportsBodyComposition)
    }

    @Test
    fun `the Scale 3's ceiling is limited to the foot-to-foot path`() {
        val capability = HuaweiScaleModel.HUAWEI_SCALE_3.capability!!

        assertEquals(ElectrodeCount.FOUR, capability.electrodeCount)
        assertEquals(listOf(50), capability.frequenciesKHz)
        assertEquals(setOf(ImpedancePath.LEFT_FOOT_TO_RIGHT_FOOT), capability.measurablePaths)
        assertEquals(1, capability.measurableReadings.size)
    }

    @Test
    fun `the resistance scale factor is carried by the model`() {
        // The divisor belongs to the hardware, not to the frame: a family that counts in
        // other units is adjusted here, without touching the protocol.
        for (model in HuaweiScaleModel.entries) {
            assertEquals(HuaweiScaleModel.HAIGE_OHM_DIVISOR, model.impedanceOhmDivisor, 1e-9)
        }
        assertEquals(10.0, HuaweiScaleModel.HAIGE_OHM_DIVISOR, 1e-9)
    }
}
