package app.bodyforger.mobile.mcp

import app.bodyforger.core.healthconnect.HealthConnectManager
import app.bodyforger.core.healthconnect.HealthConnectPermissions
import app.bodyforger.mobile.mcp.McpToolHelpers.boolProp
import app.bodyforger.mobile.mcp.McpToolHelpers.errorJson
import app.bodyforger.mobile.mcp.McpToolHelpers.intProp
import app.bodyforger.mobile.mcp.McpToolHelpers.toolDescriptor
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.temporal.ChronoUnit

class HealthConnectToolHandler(
    private val healthConnectManager: HealthConnectManager? = null
) {
    private val reader get() = healthConnectManager?.getReaderOrNull()

    fun listToolDescriptors(): List<JSONObject> = listOf(
        toolDescriptor(TOOL_HEALTH_STATUS, "Check Health Connect availability and granted permissions."),
        toolDescriptor(TOOL_HEALTH_INSPECT_SUMMARY, "Scan Health Connect and return an inventory of records and apps.",
            JSONObject().apply { put("monthsBack", intProp("Past months to scan (default 12)")) }),
        toolDescriptor(TOOL_HEALTH_READ_SESSIONS, "Read workout sessions with segment breakdown.",
            JSONObject().apply { put("monthsBack", intProp("Past months to read (default 6)")) }),
        toolDescriptor(TOOL_HEALTH_READ_WEIGHTS, "Read weight and body fat measurements.",
            JSONObject().apply { put("monthsBack", intProp("Past months to read (default 6)")) }),
        toolDescriptor(TOOL_HEALTH_READ_HEART_RATES, "Read continuous heart rate series with summary statistics.",
            JSONObject().apply {
                put("daysBack", intProp("Past days to read (default 1)"))
                put("limit", intProp("Maximum series (default 50)"))
                put("includeSamples", boolProp("Include raw samples (default false)"))
            })
    )

    fun canHandle(name: String): Boolean = name in SUPPORTED_TOOLS

    suspend fun execute(name: String, arguments: JSONObject): JSONObject = when (name) {
        TOOL_HEALTH_STATUS -> handleStatus()
        TOOL_HEALTH_INSPECT_SUMMARY -> handleInspectSummary(arguments)
        TOOL_HEALTH_READ_SESSIONS -> handleReadSessions(arguments)
        TOOL_HEALTH_READ_WEIGHTS -> handleReadWeights(arguments)
        TOOL_HEALTH_READ_HEART_RATES -> handleReadHeartRates(arguments)
        else -> errorJson("Unsupported Health Connect tool: $name")
    }

    private suspend fun handleStatus(): JSONObject {
        val client = healthConnectManager?.getClientOrNull()
            ?: return JSONObject().apply {
                put("available", false)
                put("message", "Health Connect is not available or client not initialized.")
            }
        val granted = HealthConnectPermissions.getGrantedPermissions(client)
        val missing = HealthConnectPermissions.CORE_READ_PERMISSIONS.filterNot { granted.contains(it) }
        return JSONObject().apply {
            put("available", true)
            put("hasHistoryPermission", HealthConnectPermissions.hasHistoryPermission(granted))
            put("grantedPermissions", JSONArray(granted.toList()))
            put("missingPermissions", JSONArray(missing))
        }
    }

    private suspend fun handleInspectSummary(arguments: JSONObject): JSONObject {
        val r = reader ?: return errorJson("Health Connect reader is unavailable.")
        val months = arguments.optInt("monthsBack", 12)
        val overview = r.inspectOverview(monthsBack = months)
        return JSONObject().apply {
            put("monthsScanned", overview.monthsScanned)
            put("hasHistoryPermission", overview.hasHistoryPermission)
            put("totalSessions", overview.totalSessions)
            put("totalHeartRateRecords", overview.totalHeartRateRecords)
            put("totalWeightRecords", overview.totalWeightRecords)
            put("totalBodyFatRecords", overview.totalBodyFatRecords)
            put("totalStepsRecords", overview.totalStepsRecords)
            put("oldestRecord", overview.oldestRecordIso ?: "none")
            put("newestRecord", overview.newestRecordIso ?: "none")
            put("sourcePackages", JSONArray(overview.sourcePackages))
        }
    }

    private suspend fun handleReadSessions(arguments: JSONObject): JSONObject {
        val r = reader ?: return errorJson("Health Connect reader is unavailable.")
        val months = arguments.optInt("monthsBack", 6)
        val now = Instant.now()
        val start = now.minus(months.toLong() * 30L, ChronoUnit.DAYS)
        val sessions = r.readExerciseSessions(start, now)
        val array = JSONArray()
        sessions.forEach { s ->
            array.put(JSONObject().apply {
                put("id", s.id)
                put("title", s.title ?: "")
                put("notes", s.notes ?: "")
                put("startTime", s.startTimeIso)
                put("endTime", s.endTimeIso)
                put("durationMinutes", s.durationMinutes)
                put("exerciseType", s.exerciseTypeName)
                put("sourcePackage", s.sourcePackage)
                val segArray = JSONArray()
                s.segments.forEach { seg ->
                    segArray.put(JSONObject().apply {
                        put("type", seg.segmentTypeName)
                        put("reps", seg.repetitions)
                        put("startTime", seg.startTimeIso)
                        put("endTime", seg.endTimeIso)
                    })
                }
                put("segments", segArray)
            })
        }
        return JSONObject().apply {
            put("count", sessions.size)
            put("sessions", array)
        }
    }

    private suspend fun handleReadWeights(arguments: JSONObject): JSONObject {
        val r = reader ?: return errorJson("Health Connect reader is unavailable.")
        val months = arguments.optInt("monthsBack", 6)
        val now = Instant.now()
        val start = now.minus(months.toLong() * 30L, ChronoUnit.DAYS)
        val weights = r.readWeights(start, now)
        val fats = r.readBodyFats(start, now)
        return JSONObject().apply {
            put("weightCount", weights.size)
            put("bodyFatCount", fats.size)
            put("weights", JSONArray(weights.map {
                JSONObject().apply {
                    put("time", it.timeIso)
                    put("weightKg", it.value)
                    put("source", it.sourcePackage)
                }
            }))
            put("bodyFats", JSONArray(fats.map {
                JSONObject().apply {
                    put("time", it.timeIso)
                    put("bodyFatPercentage", it.value)
                    put("source", it.sourcePackage)
                }
            }))
        }
    }

    private suspend fun handleReadHeartRates(arguments: JSONObject): JSONObject {
        val r = reader ?: return errorJson("Health Connect reader is unavailable.")
        val days = arguments.optInt("daysBack", 1).coerceIn(1, 30)
        val limit = arguments.optInt("limit", 50).coerceIn(1, 200)
        val includeSamples = arguments.optBoolean("includeSamples", false)
        val now = Instant.now()
        val start = now.minus(days.toLong(), ChronoUnit.DAYS)
        val series = r.readHeartRates(start, now, limit, includeSamples)
        val array = JSONArray()
        series.forEach { s ->
            array.put(JSONObject().apply {
                put("startTime", s.startTimeIso)
                put("endTime", s.endTimeIso)
                put("source", s.sourcePackage)
                put("sampleCount", s.sampleCount)
                put("minBpm", s.minBpm)
                put("maxBpm", s.maxBpm)
                put("avgBpm", s.avgBpm)
                if (s.deviceManufacturer != null) put("deviceManufacturer", s.deviceManufacturer)
                if (s.deviceModel != null) put("deviceModel", s.deviceModel)
                if (includeSamples && s.samples.isNotEmpty()) {
                    put("samples", JSONArray(s.samples.map {
                        JSONObject().apply {
                            put("time", it.timeIso)
                            put("bpm", it.bpm)
                        }
                    }))
                }
            })
        }
        return JSONObject().apply {
            put("seriesCount", series.size)
            put("daysBack", days)
            put("series", array)
        }
    }

    companion object {
        const val TOOL_HEALTH_STATUS = "health_connect_status"
        const val TOOL_HEALTH_INSPECT_SUMMARY = "health_connect_inspect_summary"
        const val TOOL_HEALTH_READ_SESSIONS = "health_connect_read_sessions"
        const val TOOL_HEALTH_READ_WEIGHTS = "health_connect_read_weights"
        const val TOOL_HEALTH_READ_HEART_RATES = "health_connect_read_heart_rates"

        private val SUPPORTED_TOOLS = setOf(
            TOOL_HEALTH_STATUS,
            TOOL_HEALTH_INSPECT_SUMMARY,
            TOOL_HEALTH_READ_SESSIONS,
            TOOL_HEALTH_READ_WEIGHTS,
            TOOL_HEALTH_READ_HEART_RATES
        )
    }
}
