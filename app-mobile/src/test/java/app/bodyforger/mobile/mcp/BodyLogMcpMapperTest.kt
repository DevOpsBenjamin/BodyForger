package app.bodyforger.mobile.mcp

import app.bodyforger.core.model.ElectrodeCount
import app.bodyforger.core.model.ImpedancePath
import app.bodyforger.core.model.ImpedanceReading
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId

/**
 * What an imported weigh-in may and may not carry.
 *
 * The resistances are the only quantity a scale truly measures and everything else is
 * recomputed from them, so a wrong path or an invented zero would corrupt the history
 * silently. Each of those is refused here rather than stored.
 */
class BodyLogMcpMapperTest {

    private val zone = ZoneId.of("Europe/Paris")

    /** 2026-08-31 07:05:42+00, a real reading of the Scale 3 Pro with the handle. */
    private val measuredAtEpochMs = 1_788_159_942_000L

    private fun reading(path: ImpedancePath, frequencyKHz: Int, ohms: Double) = JSONObject().apply {
        put("path", path.name)
        put("frequencyKHz", frequencyKHz)
        put("ohms", ohms)
    }

    private fun args(vararg impedances: JSONObject) = JSONObject().apply {
        put("id", "sbg-b5b08a89")
        put("measuredAtEpochMs", measuredAtEpochMs)
        put("massKg", 103.15)
        put("bodyFatPercentage", 31.10)
        put("restingHeartRateBpm", 86)
        put("impedances", JSONArray().apply { impedances.forEach { put(it) } })
    }

    @Test
    fun `a full reading keeps every resistance under its own path`() {
        val json = args(
            reading(ImpedancePath.LEFT_FOOT_TO_RIGHT_FOOT, 50, 3857.0),
            reading(ImpedancePath.LEFT_HAND_TO_RIGHT_HAND, 50, 4942.0),
            reading(ImpedancePath.LEFT_FOOT_TO_RIGHT_FOOT, 250, 3375.0)
        )

        val log = BodyLogMcpMapper.parseBodyLog(json, zone)

        assertEquals("sbg-b5b08a89", log.id)
        assertEquals(103.15, log.massKg, 0.001)
        assertEquals(31.10, log.bodyFatPercentage!!, 0.001)
        assertEquals(86, log.restingHeartRateBpm)
        assertEquals(3, log.rawImpedances.ohmsByReading.size)
        assertEquals(4942.0, log.rawImpedances[ImpedancePath.LEFT_HAND_TO_RIGHT_HAND, 50]!!, 0.001)
        assertEquals(3375.0, log.rawImpedances[ImpedancePath.LEFT_FOOT_TO_RIGHT_FOOT, 250]!!, 0.001)
    }

    @Test
    fun `a hand path is what makes the reading an eight-electrode one`() {
        val feetOnly = BodyLogMcpMapper.parseBodyLog(
            args(reading(ImpedancePath.LEFT_FOOT_TO_RIGHT_FOOT, 50, 3857.0)), zone
        )
        val withHands = BodyLogMcpMapper.parseBodyLog(
            args(
                reading(ImpedancePath.LEFT_FOOT_TO_RIGHT_FOOT, 50, 3857.0),
                reading(ImpedancePath.RIGHT_HAND_TO_LEFT_FOOT, 50, 4640.0)
            ),
            zone
        )

        assertEquals(ElectrodeCount.FOUR, feetOnly.fidelity.exercisedElectrodeCount)
        assertEquals(ElectrodeCount.EIGHT, withHands.fidelity.exercisedElectrodeCount)
    }

    @Test
    fun `the day is derived from the instant, in the zone the phone stands in`() {
        // 2026-08-31 07:05 UTC is still the 31st in Paris; an hour before midnight UTC is not.
        val log = BodyLogMcpMapper.parseBodyLog(args(), zone)
        assertEquals("2026-08-31", log.dateIso)

        val lateNight = args().apply { put("measuredAtEpochMs", 1_788_220_740_000L) } // 23:59 UTC
        assertEquals("2026-09-01", BodyLogMcpMapper.parseBodyLog(lateNight, zone).dateIso)
    }

    @Test
    fun `a weigh-in without composition keeps its mass and no percentage`() {
        val json = args().apply {
            remove("bodyFatPercentage")
            remove("impedances")
        }

        val log = BodyLogMcpMapper.parseBodyLog(json, zone)

        assertEquals(103.15, log.massKg, 0.001)
        assertNull(log.bodyFatPercentage)
        assertTrue(log.rawImpedances.isEmpty)
        assertEquals(ElectrodeCount.NONE, log.fidelity.exercisedElectrodeCount)
    }

    @Test
    fun `an unknown path is refused rather than dropped`() {
        val json = args(JSONObject().apply {
            put("path", "FEET")
            put("frequencyKHz", 50)
            put("ohms", 3857.0)
        })

        val error = assertThrows(IllegalArgumentException::class.java) {
            BodyLogMcpMapper.parseBodyLog(json, zone)
        }
        assertTrue(error.message!!.contains("unknown path 'FEET'"))
    }

    @Test
    fun `a zero resistance is refused, since it means not measured`() {
        val json = args(reading(ImpedancePath.LEFT_HAND_TO_RIGHT_HAND, 50, 0.0))

        val error = assertThrows(IllegalArgumentException::class.java) {
            BodyLogMcpMapper.parseBodyLog(json, zone)
        }
        assertTrue(error.message!!.contains("ohms must be positive"))
    }

    @Test
    fun `the same path at the same frequency cannot be given twice`() {
        val json = args(
            reading(ImpedancePath.LEFT_FOOT_TO_RIGHT_FOOT, 50, 3857.0),
            reading(ImpedancePath.LEFT_FOOT_TO_RIGHT_FOOT, 50, 3860.0)
        )

        assertThrows(IllegalArgumentException::class.java) {
            BodyLogMcpMapper.parseBodyLog(json, zone)
        }
    }

    @Test
    fun `mass and instant are both required`() {
        assertThrows(IllegalArgumentException::class.java) {
            BodyLogMcpMapper.parseBodyLog(args().apply { remove("massKg") }, zone)
        }
        assertThrows(IllegalArgumentException::class.java) {
            BodyLogMcpMapper.parseBodyLog(args().apply { remove("measuredAtEpochMs") }, zone)
        }
    }

    @Test
    fun `a percentage outside what a body can hold is refused`() {
        assertThrows(IllegalArgumentException::class.java) {
            BodyLogMcpMapper.parseBodyLog(args().apply { put("bodyFatPercentage", 0.4) }, zone)
        }
    }

    @Test
    fun `what is read back carries the resistances by frequency then by path`() {
        val log = BodyLogMcpMapper.parseBodyLog(
            args(
                reading(ImpedancePath.LEFT_HAND_TO_RIGHT_HAND, 250, 4298.0),
                reading(ImpedancePath.LEFT_FOOT_TO_RIGHT_FOOT, 50, 3857.0),
                reading(ImpedancePath.LEFT_FOOT_TO_RIGHT_FOOT, 250, 3375.0)
            ),
            zone
        )

        val json = BodyLogMcpMapper.toBodyLogJson(log, "50:FB:19:F8:0C:21")

        assertEquals(3, json.getInt("impedanceCount"))
        assertEquals("50:FB:19:F8:0C:21", json.getString("sourceDeviceAddress"))
        val rows = json.getJSONArray("impedances")
        assertEquals(50, rows.getJSONObject(0).getInt("frequencyKHz"))
        assertEquals(ImpedancePath.LEFT_FOOT_TO_RIGHT_FOOT.name, rows.getJSONObject(1).getString("path"))
        assertEquals(250, rows.getJSONObject(2).getInt("frequencyKHz"))

        // And it goes back in unchanged.
        val reparsed = BodyLogMcpMapper.parseBodyLog(json, zone)
        assertEquals(log.rawImpedances.ohmsByReading, reparsed.rawImpedances.ohmsByReading)
        assertEquals(
            4298.0,
            reparsed.rawImpedances[ImpedanceReading(ImpedancePath.LEFT_HAND_TO_RIGHT_HAND, 250)]!!,
            0.001
        )
    }
}
