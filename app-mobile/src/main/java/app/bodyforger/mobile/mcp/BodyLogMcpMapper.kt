package app.bodyforger.mobile.mcp

import app.bodyforger.core.model.BodyLog
import app.bodyforger.core.model.ImpedancePath
import app.bodyforger.core.model.ImpedanceReading
import app.bodyforger.core.model.RawImpedances
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

/**
 * Between a weigh-in as MCP carries it and a weigh-in as the app holds it.
 *
 * Kept apart from the handler so it can be tested without a database: the resistances are the
 * one thing an import must never distort, since the whole history is recomputed from them.
 */
internal object BodyLogMcpMapper {

    /**
     * Reads a weigh-in, or throws with what is wrong.
     *
     * A path is named, never given by its index in the scale's frame: that index has already
     * changed once, and a silent shift would rewrite the anatomy of the whole history.
     */
    fun parseBodyLog(args: JSONObject, zone: ZoneId = ZoneId.systemDefault()): BodyLog {
        val massKg = args.optDouble("massKg", Double.NaN)
        require(!massKg.isNaN() && massKg > 0.0) { "massKg is required and must be positive." }

        require(args.has("measuredAtEpochMs")) { "measuredAtEpochMs is required." }
        val measuredAtEpochMs = args.optLong("measuredAtEpochMs")
        require(measuredAtEpochMs > 0L) { "measuredAtEpochMs must be a positive epoch in milliseconds." }

        val bodyFat = if (args.isNull("bodyFatPercentage")) null else {
            args.optDouble("bodyFatPercentage").takeUnless { it.isNaN() }
        }
        require(bodyFat == null || bodyFat in 1.0..75.0) {
            "bodyFatPercentage must fall between 1 and 75, or be absent."
        }

        val heartRate = if (args.isNull("restingHeartRateBpm")) null else {
            args.optInt("restingHeartRateBpm").takeIf { it > 0 }
        }

        // The day is never sent, only derived: it is the calendar face of the instant, and a
        // caller free to send both could store a reading whose day contradicts its own time.
        val dateIso = Instant.ofEpochMilli(measuredAtEpochMs).atZone(zone).toLocalDate().toString()

        return BodyLog(
            id = args.optString("id").takeIf { it.isNotEmpty() } ?: UUID.randomUUID().toString(),
            dateIso = dateIso,
            measuredAtEpochMs = measuredAtEpochMs,
            massKg = massKg,
            bodyFatPercentage = bodyFat,
            rawImpedances = parseImpedances(args.optJSONArray("impedances")),
            restingHeartRateBpm = heartRate
        )
    }

    private fun parseImpedances(array: JSONArray?): RawImpedances {
        if (array == null || array.length() == 0) return RawImpedances.NONE

        val readings = mutableMapOf<ImpedanceReading, Double>()
        for (index in 0 until array.length()) {
            val row = array.optJSONObject(index)
                ?: throw IllegalArgumentException("impedances[$index] is not an object.")

            val pathName = row.optString("path")
            val path = ImpedancePath.entries.firstOrNull { it.name == pathName }
                ?: throw IllegalArgumentException(
                    "impedances[$index]: unknown path '$pathName'. One of: " +
                        ImpedancePath.entries.joinToString { it.name }
                )

            val frequencyKHz = row.optInt("frequencyKHz")
            require(frequencyKHz > 0) { "impedances[$index]: frequencyKHz must be positive." }

            val ohms = row.optDouble("ohms", Double.NaN)
            require(!ohms.isNaN() && ohms > 0.0) {
                // The scale fills what it did not measure with zeros; over MCP an unmeasured
                // path is an absent entry, so that "not measured" never arrives as a number.
                "impedances[$index]: ohms must be positive, or the reading must be left out."
            }

            val reading = ImpedanceReading(path, frequencyKHz)
            require(readings.put(reading, ohms) == null) {
                "impedances[$index]: ${path.name} at $frequencyKHz kHz is given twice."
            }
        }
        return RawImpedances.of(readings)
    }

    /** A stored weigh-in, as a reader gets it back. */
    fun toBodyLogJson(log: BodyLog, sourceDeviceAddress: String?): JSONObject = JSONObject().apply {
        put("id", log.id)
        put("dateIso", log.dateIso)
        put("measuredAtEpochMs", log.measuredAtEpochMs)
        put("massKg", log.massKg)
        if (log.bodyFatPercentage != null) put("bodyFatPercentage", log.bodyFatPercentage)
        if (log.restingHeartRateBpm != null) put("restingHeartRateBpm", log.restingHeartRateBpm)
        if (sourceDeviceAddress != null) put("sourceDeviceAddress", sourceDeviceAddress)
        put("impedanceCount", log.rawImpedances.ohmsByReading.size)
        put("impedances", JSONArray().apply {
            log.rawImpedances.ohmsByReading
                .toList()
                .sortedWith(compareBy({ it.first.frequencyKHz }, { it.first.path.wireIndex }))
                .forEach { (reading, ohms) ->
                    put(JSONObject().apply {
                        put("path", reading.path.name)
                        put("frequencyKHz", reading.frequencyKHz)
                        put("ohms", ohms)
                    })
                }
        })
    }
}
