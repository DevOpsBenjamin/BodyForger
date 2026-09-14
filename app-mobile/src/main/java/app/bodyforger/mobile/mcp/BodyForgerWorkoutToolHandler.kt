package app.bodyforger.mobile.mcp

import app.bodyforger.core.database.BodyForgerDatabase
import app.bodyforger.core.database.entity.WorkoutSessionEntity
import app.bodyforger.core.database.entity.toProfile
import app.bodyforger.core.model.WorkoutSessionStatus
import app.bodyforger.mobile.mcp.McpToolHelpers.arrayProp
import app.bodyforger.mobile.mcp.McpToolHelpers.boolProp
import app.bodyforger.mobile.mcp.McpToolHelpers.errorJson
import app.bodyforger.mobile.mcp.McpToolHelpers.intProp
import app.bodyforger.mobile.mcp.McpToolHelpers.numProp
import app.bodyforger.mobile.mcp.McpToolHelpers.stringProp
import app.bodyforger.mobile.mcp.McpToolHelpers.toolDescriptor
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.UUID

class BodyForgerWorkoutToolHandler(private val database: BodyForgerDatabase?) {

    fun listToolDescriptors(): List<JSONObject> = listOf(
        toolDescriptor(
            TOOL_LOCAL_SUMMARY,
            "Get summary of BodyForger local database (athlete, routines, workouts, goals).",
            JSONObject()
        ),
        toolDescriptor(
            TOOL_INSERT_WORKOUT,
            "Insert a completed or active workout session with sets, reps, weight, RPE and cardio graph.",
            JSONObject().apply {
                put("title", stringProp("Workout title (e.g. Push Hypertrophy, Leg Day)"))
                put("routineId", stringProp("Optional ID of the routine this workout originated from"))
                put("notes", stringProp("Workout session notes"))
                put("status", stringProp("Session status: COMPLETED or ACTIVE (default COMPLETED)"))
                put("startedAtEpochMs", numProp("Start timestamp in milliseconds (epoch ms)"))
                put("endedAtEpochMs", numProp("End timestamp in milliseconds (epoch ms)"))
                put("averageHeartRateBpm", intProp("Average heart rate in BPM (optional)"))
                put("activeCaloriesKcal", intProp("Estimated active calories burned (optional)"))
                put("sets", arrayProp("List of performed sets with exerciseId, weightKg, reps, rpe, type, and optional timing (startedAtEpochMs, completedAtEpochMs, actualRestSeconds)"))
                put("heartRateSamples", arrayProp("Time-series heart rate points: [{timestampEpochMs, bpm}]"))
            },
            required = listOf("title", "startedAtEpochMs", "sets")
        ),
        toolDescriptor(
            TOOL_LIST_WORKOUTS,
            "List past workout sessions with summary tonnage, sets count and cardio stats.",
            JSONObject().apply {
                put("limit", intProp("Max workouts to return (default 30)"))
                put("offset", intProp("Pagination offset (default 0)"))
            }
        ),
        toolDescriptor(
            TOOL_GET_WORKOUT,
            "Get detailed workout session with sets, loads, RPE and cardio graph series.",
            JSONObject().apply {
                put("id", stringProp("Workout session ID"))
                put("includeHeartRateSamples", boolProp("Include raw continuous heart rate curve samples (default false)"))
            },
            required = listOf("id")
        )
    )

    fun canHandle(name: String): Boolean = name in SUPPORTED_TOOLS

    suspend fun execute(name: String, arguments: JSONObject): JSONObject {
        val db = database ?: return errorJson("BodyForger database is not initialized.")
        return when (name) {
            TOOL_LOCAL_SUMMARY -> handleLocalSummary(db)
            TOOL_INSERT_WORKOUT -> handleInsertWorkout(db, arguments)
            TOOL_LIST_WORKOUTS -> handleListWorkouts(db, arguments)
            TOOL_GET_WORKOUT -> handleGetWorkout(db, arguments)
            else -> errorJson("Unsupported tool: $name")
        }
    }

    private suspend fun handleLocalSummary(db: BodyForgerDatabase): JSONObject {
        val athlete = db.athleteIdentityDao().find()
        val routines = db.routineDao().getAllRoutinesWithExercises()
        val goals = db.bodyGoalDao().all()
        val recentSessions = db.workoutDao().getSessionsPaged(limit = 1, offset = 0)
        return JSONObject().apply {
            put("athleteName", athlete?.name ?: "Anonymous")
            put("huid", athlete?.huid ?: "none")
            put("hasMeasurementProfile", athlete?.toProfile()?.isComplete ?: false)
            put("routineCount", routines.size)
            put("activeGoalCount", goals.size)
            put("exerciseCount", db.exerciseDao().getExercisesCount())
            put("hasWorkouts", recentSessions.isNotEmpty())
        }
    }

    private suspend fun handleInsertWorkout(db: BodyForgerDatabase, args: JSONObject): JSONObject {
        val title = args.optString("title", "").trim()
        if (title.isEmpty()) return errorJson("Workout title is required.")

        val sessionId = args.optString("id", UUID.randomUUID().toString())
        val routineId = if (args.has("routineId")) args.optString("routineId").takeIf { it.isNotEmpty() } else null
        val notes = args.optString("notes", "")
        val statusStr = args.optString("status", WorkoutSessionStatus.COMPLETED.name).uppercase(Locale.ROOT)
        val startedAt = args.optLong("startedAtEpochMs", System.currentTimeMillis())
        val endedAt = if (args.has("endedAtEpochMs")) args.optLong("endedAtEpochMs") else null
        val activeCalories = if (args.has("activeCaloriesKcal")) args.optInt("activeCaloriesKcal") else null

        val setsJson = args.optJSONArray("sets") ?: JSONArray()
        val (setEntities, computedVolume) = WorkoutMcpMapper.parseSetEntities(sessionId, setsJson, db)

        val hrJson = args.optJSONArray("heartRateSamples") ?: JSONArray()
        val (hrEntities, sumBpm) = WorkoutMcpMapper.parseHeartRateEntities(sessionId, hrJson, startedAt)

        val avgBpm = if (args.has("averageHeartRateBpm")) {
            args.getInt("averageHeartRateBpm")
        } else if (hrEntities.isNotEmpty()) {
            (sumBpm / hrEntities.size).toInt()
        } else null

        val sessionEntity = WorkoutSessionEntity(
            id = sessionId,
            routineId = routineId,
            title = title,
            notes = notes,
            status = statusStr,
            startedAtEpochMs = startedAt,
            endedAtEpochMs = endedAt,
            averageHeartRateBpm = avgBpm,
            activeCaloriesKcal = activeCalories,
            totalVolumeKg = computedVolume,
            isFinalized = (statusStr == WorkoutSessionStatus.COMPLETED.name)
        )

        db.workoutDao().insertSession(sessionEntity)
        if (setEntities.isNotEmpty()) db.workoutDao().insertSets(setEntities)
        if (hrEntities.isNotEmpty()) db.workoutDao().insertHeartRateSamples(hrEntities)

        return JSONObject().apply {
            put("success", true)
            put("id", sessionId)
            put("title", title)
            put("totalVolumeKg", computedVolume)
            put("setCount", setEntities.size)
            put("heartRateSampleCount", hrEntities.size)
            if (avgBpm != null) put("averageHeartRateBpm", avgBpm)
        }
    }

    private suspend fun handleListWorkouts(db: BodyForgerDatabase, args: JSONObject): JSONObject {
        val limit = args.optInt("limit", 30).coerceIn(1, 100)
        val offset = args.optInt("offset", 0).coerceAtLeast(0)
        val sessions = db.workoutDao().getSessionsPaged(limit, offset)

        val array = JSONArray()
        sessions.forEach { sws ->
            val hrCount = db.workoutDao().getHeartRateSampleCountForSession(sws.session.id)
            array.put(WorkoutMcpMapper.toWorkoutSummaryJson(sws, hrCount))
        }
        return JSONObject().apply {
            put("count", sessions.size)
            put("workouts", array)
        }
    }

    private suspend fun handleGetWorkout(db: BodyForgerDatabase, args: JSONObject): JSONObject {
        val sessionId = args.optString("id", "")
        if (sessionId.isEmpty()) return errorJson("Workout ID is required.")

        val sws = db.workoutDao().getSessionWithSets(sessionId) ?: return errorJson("Workout not found.")
        val includeSamples = args.optBoolean("includeHeartRateSamples", false)
        val hrSamples = db.workoutDao().getHeartRateSamplesForSession(sessionId)

        return WorkoutMcpMapper.toWorkoutDetailJson(sws, hrSamples, includeSamples)
    }

    companion object {
        const val TOOL_LOCAL_SUMMARY = "bodyforger_local_summary"
        const val TOOL_INSERT_WORKOUT = "bodyforger_insert_workout"
        const val TOOL_LIST_WORKOUTS = "bodyforger_list_workouts"
        const val TOOL_GET_WORKOUT = "bodyforger_get_workout"

        private val SUPPORTED_TOOLS = setOf(
            TOOL_LOCAL_SUMMARY,
            TOOL_INSERT_WORKOUT,
            TOOL_LIST_WORKOUTS,
            TOOL_GET_WORKOUT
        )
    }
}
