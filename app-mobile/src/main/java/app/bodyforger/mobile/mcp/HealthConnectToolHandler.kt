package app.bodyforger.mobile.mcp

import app.bodyforger.core.healthconnect.HealthConnectManager
import app.bodyforger.core.healthconnect.HealthConnectPermissions
import app.bodyforger.mobile.mcp.McpToolHelpers.boolProp
import app.bodyforger.mobile.mcp.McpToolHelpers.errorJson
import app.bodyforger.mobile.mcp.McpToolHelpers.intProp
import app.bodyforger.mobile.mcp.McpToolHelpers.stringProp
import app.bodyforger.mobile.mcp.McpToolHelpers.toolDescriptor
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.temporal.ChronoUnit

class HealthConnectToolHandler(
    private val healthConnectManager: HealthConnectManager? = null,
    private val consentOverrideProvider: (() -> Boolean?)? = null
) {
    private val reader get() = healthConnectManager?.getReaderOrNull()

    suspend fun isConsentGranted(): Boolean {
        val override = consentOverrideProvider?.invoke()
        if (override != null) return override
        if (healthConnectManager == null) return true
        if (!healthConnectManager.isAvailable()) return false
        val client = healthConnectManager.getClientOrNull() ?: return false
        return try {
            val granted = HealthConnectPermissions.getGrantedPermissions(client)
            granted.isNotEmpty() && HealthConnectPermissions.hasExercisePermission(granted)
        } catch (_: Exception) {
            false
        }
    }

    suspend fun listToolDescriptors(): List<JSONObject> {
        val statusTool = toolDescriptor(
            TOOL_HEALTH_STATUS,
            "Check Health Connect availability, consent status, and granted permissions."
        )
        if (!isConsentGranted()) {
            return listOf(statusTool)
        }
        return listOf(
            statusTool,
            toolDescriptor(TOOL_HEALTH_INSPECT_SUMMARY, "Scan Health Connect and return an inventory of records and apps.",
                JSONObject().apply { put("monthsBack", intProp("Past months to scan (default 12)")) }),
            toolDescriptor(TOOL_HEALTH_READ_SESSIONS, "Read workout sessions with segment breakdown.",
                JSONObject().apply { put("monthsBack", intProp("Past months to read (default 6)")) }),
            toolDescriptor(TOOL_HEALTH_READ_WEIGHTS, "Read weight and body fat measurements.",
                JSONObject().apply { put("monthsBack", intProp("Past months to read (default 6)")) }),
            toolDescriptor(TOOL_HEALTH_READ_HEART_RATES, "Read continuous heart rate series with summary statistics.",
                JSONObject().apply {
                    put("startTime", stringProp("Window start, ISO-8601 instant (e.g. 2026-09-08T05:40:00Z). Defaults to daysBack before endTime"))
                    put("endTime", stringProp("Window end, ISO-8601 instant. Defaults to now"))
                    put("daysBack", intProp("Past days to read when startTime is absent (default 1, max 31)"))
                    put("limit", intProp("Maximum series (default 50, max 200)"))
                    put("includeSamples", boolProp("Include raw samples (default false)"))
                    put("maxSamples", intProp("Budget of raw samples to serialise (default 5000, max 20000)"))
                })
        )
    }

    fun canHandle(name: String): Boolean = name in SUPPORTED_TOOLS

    suspend fun execute(name: String, arguments: JSONObject): JSONObject {
        if (name != TOOL_HEALTH_STATUS && !isConsentGranted()) {
            return errorJson(
                "Google Health Connect consent has not been granted by the athlete. Only bodyforger_* tools are active."
            )
        }
        return when (name) {
            TOOL_HEALTH_STATUS -> handleStatus()
            TOOL_HEALTH_INSPECT_SUMMARY -> handleInspectSummary(arguments)
            TOOL_HEALTH_READ_SESSIONS -> handleReadSessions(arguments)
            TOOL_HEALTH_READ_WEIGHTS -> handleReadWeights(arguments)
            TOOL_HEALTH_READ_HEART_RATES -> handleReadHeartRates(arguments)
            else -> errorJson("Unsupported Health Connect tool: $name")
        }
    }

    private suspend fun handleStatus(): JSONObject {
        val consent = isConsentGranted()
        val client = healthConnectManager?.getClientOrNull()
            ?: return JSONObject().apply {
                put("available", false)
                put("consentGranted", false)
                put("activeTools", JSONArray(listOf("bodyforger_*", TOOL_HEALTH_STATUS)))
                put("message", "Health Connect is not available or consent not granted. Only bodyforger_* tools are active.")
            }
        val granted = HealthConnectPermissions.getGrantedPermissions(client)
        val missing = HealthConnectPermissions.CORE_READ_PERMISSIONS.filterNot { granted.contains(it) }
        return JSONObject().apply {
            put("available", true)
            put("consentGranted", consent)
            put("hasHistoryPermission", HealthConnectPermissions.hasHistoryPermission(granted))
            put("grantedPermissions", JSONArray(granted.toList()))
            put("missingPermissions", JSONArray(missing))
            if (!consent) {
                put("activeTools", JSONArray(listOf("bodyforger_*", TOOL_HEALTH_STATUS)))
                put("message", "Google Health Connect consent not granted. Only bodyforger_* tools are active.")
            } else {
                put("activeTools", JSONArray(listOf("all")))
            }
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

    /**
     * Reads a heart rate window.
     *
     * The window is explicit — importing one past workout means asking for that hour, not for
     * "the last N days" truncated at an arbitrary series. [daysBack] stays as the shorthand for
     * recent data.
     *
     * Two budgets keep a wide window from exhausting memory: [limit] caps the series, and
     * `maxSamples` caps the raw points actually serialised. A single series can hold thousands
     * of samples, so capping series alone never bounded the response. When the budget runs out
     * the response says so and names the instant to resume from, rather than silently returning
     * a partial curve.
     */
    private suspend fun handleReadHeartRates(arguments: JSONObject): JSONObject {
        val r = reader ?: return errorJson("Health Connect reader is unavailable.")
        val limit = arguments.optInt("limit", 50).coerceIn(1, MAX_SERIES)
        val includeSamples = arguments.optBoolean("includeSamples", false)
        val sampleBudget = arguments.optInt("maxSamples", DEFAULT_MAX_SAMPLES).coerceIn(1, MAX_SAMPLES)

        val window = HeartRateWindow.resolve(arguments, Instant.now())
            ?: return errorJson("startTime must be before endTime.")
        val (start, end, days) = window

        val series = r.readHeartRates(start, end, limit, includeSamples)
        val array = JSONArray()
        var spent = 0
        var truncatedAt: String? = null
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
                    if (spent >= sampleBudget) {
                        // Summary statistics still stand; only the raw curve stops here.
                        if (truncatedAt == null) truncatedAt = s.startTimeIso
                    } else {
                        val room = sampleBudget - spent
                        val emitted = s.samples.take(room)
                        if (emitted.size < s.samples.size && truncatedAt == null) {
                            truncatedAt = s.startTimeIso
                        }
                        spent += emitted.size
                        put("samples", JSONArray(emitted.map {
                            JSONObject().apply {
                                put("time", it.timeIso)
                                put("bpm", it.bpm)
                            }
                        }))
                    }
                }
            })
        }
        return JSONObject().apply {
            put("seriesCount", series.size)
            put("startTime", start.toString())
            put("endTime", end.toString())
            put("daysBack", days)
            put("seriesTruncated", series.size >= limit)
            if (includeSamples) {
                put("sampleCount", spent)
                put("samplesTruncated", truncatedAt != null)
                // Resume here to page through a window whose curve did not fit in one response.
                truncatedAt?.let { put("resumeFromTime", it) }
            }
            put("series", array)
        }
    }

    companion object {
        const val TOOL_HEALTH_STATUS = "health_connect_status"
        const val TOOL_HEALTH_INSPECT_SUMMARY = "health_connect_inspect_summary"
        const val TOOL_HEALTH_READ_SESSIONS = "health_connect_read_sessions"
        const val TOOL_HEALTH_READ_WEIGHTS = "health_connect_read_weights"
        const val TOOL_HEALTH_READ_HEART_RATES = "health_connect_read_heart_rates"

        /** Series cap, unchanged: Health Connect paging is the expensive part. */
        const val MAX_SERIES = 200

        /**
         * Raw sample budget. A Fitbit minute holds ~30 samples, so 5000 covers a ~2.5 hour
         * workout curve, while the hard cap keeps the largest response well away from the
         * out-of-memory failures a sample-unbounded read used to cause.
         */
        const val DEFAULT_MAX_SAMPLES = 5000
        const val MAX_SAMPLES = 20000
        const val MAX_WINDOW_DAYS = 31

        private val SUPPORTED_TOOLS = setOf(
            TOOL_HEALTH_STATUS,
            TOOL_HEALTH_INSPECT_SUMMARY,
            TOOL_HEALTH_READ_SESSIONS,
            TOOL_HEALTH_READ_WEIGHTS,
            TOOL_HEALTH_READ_HEART_RATES
        )
    }
}
