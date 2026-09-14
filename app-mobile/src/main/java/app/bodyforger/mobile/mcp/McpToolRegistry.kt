package app.bodyforger.mobile.mcp

import app.bodyforger.core.database.BodyForgerDatabase
import app.bodyforger.core.database.entity.toProfile
import app.bodyforger.core.healthconnect.HealthConnectReader
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.temporal.ChronoUnit

import app.bodyforger.core.healthconnect.HealthConnectManager
import app.bodyforger.core.healthconnect.HealthConnectPermissions

class McpToolRegistry(
    private val healthConnectManager: HealthConnectManager? = null,
    private val database: BodyForgerDatabase? = null
) {

    private val reader get() = healthConnectManager?.getReaderOrNull()
    private val db get() = database

    fun listToolsJson(): JSONArray = JSONArray().apply {
        put(createToolDescriptor(TOOL_HEALTH_STATUS, "Check Health Connect availability and granted permissions.", JSONObject()))
        put(createToolDescriptor(TOOL_HEALTH_INSPECT_SUMMARY, "Scan Health Connect and return an inventory of records and apps.",
            JSONObject().apply { put("monthsBack", intProp("Past months to scan (default 12)")) }))
        put(createToolDescriptor(TOOL_HEALTH_READ_SESSIONS, "Read workout sessions with segment breakdown.",
            JSONObject().apply { put("monthsBack", intProp("Past months to read (default 6)")) }))
        put(createToolDescriptor(TOOL_HEALTH_READ_WEIGHTS, "Read weight and body fat measurements.",
            JSONObject().apply { put("monthsBack", intProp("Past months to read (default 6)")) }))
        put(createToolDescriptor(TOOL_HEALTH_READ_HEART_RATES, "Read continuous heart rate series with summary statistics.",
            JSONObject().apply {
                put("daysBack", intProp("Past days to read (default 1)"))
                put("limit", intProp("Maximum series (default 50)"))
                put("includeSamples", boolProp("Include raw samples (default false)"))
            }))
        put(createToolDescriptor(TOOL_BODYFORGER_LOCAL_SUMMARY, "Get summary of BodyForger local database.", JSONObject()))
    }

    suspend fun executeTool(name: String, arguments: JSONObject): JSONObject {
        return try {
            executeToolInternal(name, arguments)
        } catch (t: Throwable) {
            errorJson("Tool execution error: ${t.message}")
        }
    }

    private suspend fun executeToolInternal(name: String, arguments: JSONObject): JSONObject {
        val reader = reader
        return when (name) {
            TOOL_HEALTH_STATUS -> {
                val client = healthConnectManager?.getClientOrNull()
                if (client == null) {
                    return JSONObject().apply {
                        put("available", false)
                        put("message", "Health Connect is not available or client not initialized.")
                    }
                }
                val granted = HealthConnectPermissions.getGrantedPermissions(client)
                val missing = HealthConnectPermissions.CORE_READ_PERMISSIONS.filterNot { granted.contains(it) }
                JSONObject().apply {
                    put("available", true)
                    put("hasHistoryPermission", HealthConnectPermissions.hasHistoryPermission(granted))
                    put("grantedPermissions", JSONArray(granted.toList()))
                    put("missingPermissions", JSONArray(missing))
                }
            }
            TOOL_HEALTH_INSPECT_SUMMARY -> {
                if (reader == null) return errorJson("Health Connect reader is unavailable.")
                val months = arguments.optInt("monthsBack", 12)
                val overview = reader.inspectOverview(monthsBack = months)
                JSONObject().apply {
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
            TOOL_HEALTH_READ_SESSIONS -> {
                if (reader == null) return errorJson("Health Connect reader is unavailable.")
                val months = arguments.optInt("monthsBack", 6)
                val now = Instant.now()
                val start = now.minus(months.toLong() * 30L, ChronoUnit.DAYS)
                val sessions = reader.readExerciseSessions(start, now)
                val array = JSONArray()
                sessions.forEach { s ->
                    val sJson = JSONObject().apply {
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
                    }
                    array.put(sJson)
                }
                JSONObject().apply {
                    put("count", sessions.size)
                    put("sessions", array)
                }
            }
            TOOL_HEALTH_READ_WEIGHTS -> {
                if (reader == null) return errorJson("Health Connect reader is unavailable.")
                val months = arguments.optInt("monthsBack", 6)
                val now = Instant.now()
                val start = now.minus(months.toLong() * 30L, ChronoUnit.DAYS)
                val weights = reader.readWeights(start, now)
                val fats = reader.readBodyFats(start, now)
                JSONObject().apply {
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
            TOOL_HEALTH_READ_HEART_RATES -> {
                if (reader == null) return errorJson("Health Connect reader is unavailable.")
                val days = arguments.optInt("daysBack", 1).coerceIn(1, 30)
                val limit = arguments.optInt("limit", 50).coerceIn(1, 200)
                val includeSamples = arguments.optBoolean("includeSamples", false)
                val now = Instant.now()
                val start = now.minus(days.toLong(), ChronoUnit.DAYS)
                val series = reader.readHeartRates(start, now, limit, includeSamples)
                val array = JSONArray()
                series.forEach { s ->
                    val sJson = JSONObject().apply {
                        put("startTime", s.startTimeIso)
                        put("endTime", s.endTimeIso)
                        put("source", s.sourcePackage)
                        put("sampleCount", s.sampleCount)
                        put("minBpm", s.minBpm)
                        put("maxBpm", s.maxBpm)
                        put("avgBpm", s.avgBpm)
                        if (includeSamples && s.samples.isNotEmpty()) {
                            put("samples", JSONArray(s.samples.map {
                                JSONObject().apply {
                                    put("time", it.timeIso)
                                    put("bpm", it.bpm)
                                }
                            }))
                        }
                    }
                    array.put(sJson)
                }
                JSONObject().apply {
                    put("seriesCount", series.size)
                    put("daysBack", days)
                    put("series", array)
                }
            }
            TOOL_BODYFORGER_LOCAL_SUMMARY -> {
                val db = db
                if (db == null) return errorJson("Local database is not initialized.")
                val athlete = db.athleteIdentityDao().find()
                val routines = db.routineDao().getAllRoutinesWithExercises()
                val goals = db.bodyGoalDao().all()
                JSONObject().apply {
                    put("athleteName", athlete?.name ?: "Anonymous")
                    put("huid", athlete?.huid ?: "none")
                    put("hasMeasurementProfile", athlete?.toProfile()?.isComplete ?: false)
                    put("routineCount", routines.size)
                    put("activeGoalCount", goals.size)
                }
            }
            else -> errorJson("Unknown tool: $name")
        }
    }

    private fun createToolDescriptor(name: String, description: String, properties: JSONObject): JSONObject {
        return JSONObject().apply {
            put("name", name)
            put("description", description)
            put("inputSchema", JSONObject().apply {
                put("type", "object")
                put("properties", properties)
            })
        }
    }

    private fun errorJson(message: String): JSONObject = JSONObject().apply {
        put("error", true)
        put("message", message)
    }

    private fun intProp(desc: String) = JSONObject().apply {
        put("type", "integer")
        put("description", desc)
    }

    private fun boolProp(desc: String) = JSONObject().apply {
        put("type", "boolean")
        put("description", desc)
    }

    companion object {
        const val TOOL_HEALTH_STATUS = "health_connect_status"
        const val TOOL_HEALTH_INSPECT_SUMMARY = "health_connect_inspect_summary"
        const val TOOL_HEALTH_READ_SESSIONS = "health_connect_read_sessions"
        const val TOOL_HEALTH_READ_WEIGHTS = "health_connect_read_weights"
        const val TOOL_HEALTH_READ_HEART_RATES = "health_connect_read_heart_rates"
        const val TOOL_BODYFORGER_LOCAL_SUMMARY = "bodyforger_local_summary"
    }
}
