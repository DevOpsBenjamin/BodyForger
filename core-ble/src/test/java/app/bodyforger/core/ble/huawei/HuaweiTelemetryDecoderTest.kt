package app.bodyforger.core.ble.huawei

import app.bodyforger.core.ble.BiaTelemetry
import app.bodyforger.core.model.ElectrodeCount
import app.bodyforger.core.model.ImpedancePath
import app.bodyforger.core.model.ImpedanceReading.Companion.HIGH_FREQUENCY_KHZ
import app.bodyforger.core.model.ImpedanceReading.Companion.LOW_FREQUENCY_KHZ
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Decoding of the Haige telemetry frame, driven by real captures rather than fixtures.
 *
 * Layout under test: `docs/BLE_PROTOCOL.md` §9.
 */
class HuaweiTelemetryDecoderTest {

    /** Scale 3 Pro, complete weigh-in with the handle gripped. */
    private val proWithHandle = hex(
        "401fb900e9070309071e000736101a1890154a150e15c8144800740e861542130613ca128e12"
    )

    /** Scale 3 Pro, a second complete weigh-in. */
    private val proWithHandleSecond = hex(
        "4f1fbb00e9070309072d0c0754104218b81572153615f0144a00920ea41560132413e812ac12"
    )

    /** Scale 3 Pro, weighed with the handle left in its cradle. */
    private val proWithoutHandle = hex(
        "b81f0000e907030f121405060000000000000000000000000000000000000000000000000000"
    )

    /** Plain Scale 3 (`M00D`), four electrodes — openScale capture of 2026-08-09. */
    private val plainScaleThree = hex("b1212d01ea07080917271da0ea13000000000000000000006100")

    @Test
    fun `a complete Pro weigh-in yields the twelve resistances`() {
        val telemetry = requireNotNull(HuaweiTelemetryDecoder.decode(proWithHandle, HuaweiScaleModel.HUAWEI_SCALE_3_PRO)).telemetry

        assertEquals(80.00, telemetry.massKg, 1e-9)
        assertEquals(18.5, telemetry.bodyFatPercentage!!, 1e-9)
        assertEquals(72, telemetry.heartRateBpm)
        assertEquals(12, telemetry.rawImpedances.ohmsByReading.size)
        assertEquals(ElectrodeCount.EIGHT, telemetry.fidelityElectrodeCount())
        assertEquals(
            listOf(LOW_FREQUENCY_KHZ, HIGH_FREQUENCY_KHZ),
            telemetry.rawImpedances.fidelity.frequenciesKHz
        )
    }

    @Test
    fun `resistances are tenths of an ohm, with no magnitude heuristic`() {
        val telemetry = HuaweiTelemetryDecoder.decode(proWithHandle, HuaweiScaleModel.HUAWEI_SCALE_3_PRO)!!.telemetry

        // Raw counter 4150: openScale's heuristic would render it as 4150 Ω.
        assertEquals(
            415.0,
            telemetry.rawImpedances[ImpedancePath.LEFT_FOOT_TO_RIGHT_FOOT, LOW_FREQUENCY_KHZ]!!,
            1e-9
        )
        assertEquals(
            617.0,
            telemetry.rawImpedances[ImpedancePath.LEFT_HAND_TO_RIGHT_HAND, LOW_FREQUENCY_KHZ]!!,
            1e-9
        )
    }

    @Test
    fun `the high frequency is systematically below the low one`() {
        for (frame in listOf(proWithHandle, proWithHandleSecond)) {
            val impedances = HuaweiTelemetryDecoder.decode(frame, HuaweiScaleModel.HUAWEI_SCALE_3_PRO)!!.telemetry.rawImpedances
            for (path in ImpedancePath.entries) {
                val low = impedances[path, LOW_FREQUENCY_KHZ]!!
                val high = impedances[path, HIGH_FREQUENCY_KHZ]!!
                assertTrue("$path: $high should sit below $low", high < low)
            }
        }
    }

    @Test
    fun `a Pro without the handle is still a valid weigh-in, carrying no impedance`() {
        val telemetry = requireNotNull(HuaweiTelemetryDecoder.decode(proWithoutHandle, HuaweiScaleModel.HUAWEI_SCALE_3_PRO)).telemetry

        assertEquals(HuaweiTelemetryDecoder.DUAL_FREQUENCY_FRAME_BYTES, proWithoutHandle.size)
        assertEquals(81.20, telemetry.massKg, 1e-9)
        assertTrue(telemetry.rawImpedances.isEmpty)
        assertEquals(ElectrodeCount.NONE, telemetry.fidelityElectrodeCount())
    }

    @Test
    fun `body fat and heart rate at zero are absences, not values`() {
        val telemetry = HuaweiTelemetryDecoder.decode(proWithoutHandle, HuaweiScaleModel.HUAWEI_SCALE_3_PRO)!!.telemetry

        assertNull("a BodyLog at 0 % body fat would be a lie", telemetry.bodyFatPercentage)
        assertNull(telemetry.heartRateBpm)
    }

    @Test
    fun `a four-electrode scale fills in the foot-to-foot path alone`() {
        val telemetry = requireNotNull(HuaweiTelemetryDecoder.decode(plainScaleThree, HuaweiScaleModel.HUAWEI_SCALE_3)).telemetry

        assertEquals(HuaweiTelemetryDecoder.MIN_FRAME_BYTES, plainScaleThree.size)
        assertEquals(86.25, telemetry.massKg, 1e-9)
        assertEquals(30.1, telemetry.bodyFatPercentage!!, 1e-9)
        assertEquals(97, telemetry.heartRateBpm)

        assertEquals(
            setOf(ImpedancePath.LEFT_FOOT_TO_RIGHT_FOOT),
            telemetry.rawImpedances.fidelity.paths
        )
        assertEquals(ElectrodeCount.FOUR, telemetry.fidelityElectrodeCount())
        assertEquals(
            509.8,
            telemetry.rawImpedances[ImpedancePath.LEFT_FOOT_TO_RIGHT_FOOT, LOW_FREQUENCY_KHZ]!!,
            1e-9
        )
    }

    @Test
    fun `a real reading lands on the old fallback value — hence its removal`() {
        val measured = HuaweiTelemetryDecoder.decode(plainScaleThree, HuaweiScaleModel.HUAWEI_SCALE_3)!!.telemetry
            .rawImpedances[ImpedancePath.LEFT_FOOT_TO_RIGHT_FOOT, LOW_FREQUENCY_KHZ]!!

        // The old `?: 500.0` would be indistinguishable from this genuine measurement.
        assertTrue(measured in 400.0..600.0)
    }

    @Test
    fun `the timestamp is read exactly as the scale emits it`() {
        val telemetry = HuaweiTelemetryDecoder.decode(proWithoutHandle, HuaweiScaleModel.HUAWEI_SCALE_3_PRO)!!.telemetry.measuredAt!!

        assertEquals(2025, telemetry.year)
        assertEquals(3, telemetry.monthValue)
        assertEquals(15, telemetry.dayOfMonth)
        assertEquals(18, telemetry.hour)
        assertEquals(20, telemetry.minute)
        assertEquals(5, telemetry.second)
    }

    @Test
    fun `the status byte is exposed raw, without interpretation`() {
        // On the Pro it is the ISO weekday — the frames carry a Sunday, then a Saturday.
        assertEquals(7, HuaweiTelemetryDecoder.decode(proWithHandle, HuaweiScaleModel.HUAWEI_SCALE_3_PRO)!!.statusByte)
        assertEquals(6, HuaweiTelemetryDecoder.decode(proWithoutHandle, HuaweiScaleModel.HUAWEI_SCALE_3_PRO)!!.statusByte)
        assertEquals(0xa0, HuaweiTelemetryDecoder.decode(plainScaleThree, HuaweiScaleModel.HUAWEI_SCALE_3)!!.statusByte)
    }

    @Test
    fun `a frame that is too short is refused`() {
        assertNull(HuaweiTelemetryDecoder.decode(ByteArray(25), HuaweiScaleModel.HUAWEI_SCALE_3_PRO))
        assertNull(HuaweiTelemetryDecoder.decode(ByteArray(0), HuaweiScaleModel.HUAWEI_SCALE_3_PRO))
    }

    private fun BiaTelemetry.fidelityElectrodeCount() =
        rawImpedances.fidelity.exercisedElectrodeCount

    private fun hex(value: String): ByteArray =
        ByteArray(value.length / 2) { value.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
}
