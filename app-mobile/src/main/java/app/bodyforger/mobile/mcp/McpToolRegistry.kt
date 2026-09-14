package app.bodyforger.mobile.mcp

import app.bodyforger.core.database.BodyForgerDatabase
import app.bodyforger.core.database.entity.toProfile
import app.bodyforger.core.healthconnect.HealthConnectReader
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.temporal.ChronoUnit

import app.bodyforger.core.healthconnect.HealthConnectManager

class McpToolRegistry(
    private val healthConnectManager: HealthConnectManager? = null,
    private val database: BodyForgerDatabase? = null
) {

    private val reader get() = healthConnectManager?.getReaderOrNull()
    private val db get() = database

    fun listToolsJson(): JSONArray {
        val array = JSONArray()
        array.put(createToolDescriptor(
            name = TOOL_HEALTH_STATUS,
            description = "Check Health Connect availability, status, and granted permissions including historical data access.",
            properties = JSONObject()
        ))
        array.put(createToolDescriptor(
            name = TOOL_HEALTH_INSPECT_SUMMARY,
            description = "Scan Health Connect across multiple months and return an inventory of records (sessions, HR, weights) and originating package names (e.g. Hevy, Samsung Health).",
            properties = JSONObject().apply {
                put("monthsBack", JSONObject().apply {
                    put("type", "integer")
                    put("description", "Number of past months to scan (default 12)")
                })
            }
        ))
        array.put(createToolDescriptor(
            name = TOOL_HEALTH_READ_SESSIONS,
            description = "Read workout sessions from Health Connect with full segment breakdown, exercise types, sets, reps, and originating app.",
            properties = JSONObject().apply {
                put("monthsBack", JSONObject().apply {
                    put("type", "integer")
                    put("description", "Number of past months to read (default 6)")
                })
            }
        ))
        array.put(createToolDescriptor(
            name = TOOL_HEALTH_READ_WEIGHTS,
            description = "Read weight and body fat measurements from Health Connect over past months.",
            properties = JSONObject().apply {
                put("monthsBack", JSONObject().apply {
                    put("type", "integer")
                    put("description", "Number of past months to read (default 6)")
                })
            }
        ))
        array.put(createToolDescriptor(
            name = TOOL_HEALTH_READ_HEART_RATES,
            description = "Read continuous heart rate series from Health Connect.",
            properties = JSONObject().apply {
                put("monthsBack", JSONObject().apply {
                    put("type", "integer")
                    put("description", "Number of past months to read (default 1)")
                })
            }
        ))
        array.put(createToolDescriptor(
            name = TOOL_BODYFORGER_LOCAL_SUMMARY,
            description = "Get summary of BodyForger local Room database (athlete profile, routines, workouts, goals).",
            properties = JSONObject()
        ))
        return array
    }

    suspend fun executeTool(name: String, arguments: JSONObject): JSONObject {
        val reader = reader
        return when (name) {
            TOOL_HEALTH_STATUS -> {
                if (reader == null) {
                    return JSONObject().apply {
                        put("available", false)
                        put("message", "Health Connect is not available or reader not initialized.")
                    }
                }
                val overview = reader.inspectOverview(monthsBack = 1)
                JSONObject().apply {
                    put("available", overview.isAvailable)
                    put("hasHistoryPermission", overview.hasHistoryPermission)
                    put("grantedPermissions", JSONArray(overview.grantedPermissions))
                    put("missingPermissions", JSONArray(overview.missingPermissions))
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
                val months = arguments.optInt("monthsBack", 1)
                val now = Instant.now()
                val start = now.minus(months.toLong() * 30L, ChronoUnit.DAYS)
                val series = reader.readHeartRates(start, now)
                JSONObject().apply {
                    put("seriesCount", series.size)
                    put("series", JSONArray(series.map { s ->
                        JSONObject().apply {
                            put("startTime", s.startTimeIso)
                            put("endTime", s.endTimeIso)
                            put("source", s.sourcePackage)
                            put("sampleCount", s.samples.size)
                            put("samples", JSONArray(s.samples.map { sample ->
                                JSONObject().apply {
                                    put("time", sample.timeIso)
                                    put("bpm", sample.bpm)
                                }
                            }))
                        }
                    }))
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

    companion object {
        const val TOOL_HEALTH_STATUS = "health_connect_status"
        const val TOOL_HEALTH_INSPECT_SUMMARY = "health_connect_inspect_summary"
        const val TOOL_HEALTH_READ_SESSIONS = "health_connect_read_sessions"
        const val TOOL_HEALTH_READ_WEIGHTS = "health_connect_read_weights"
        const val TOOL_HEALTH_READ_HEART_RATES = "health_connect_read_heart_rates"
        const val TOOL_BODYFORGER_LOCAL_SUMMARY = "bodyforger_local_summary"
    }
}
