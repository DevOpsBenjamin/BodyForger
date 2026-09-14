package app.bodyforger.mobile.mcp

import app.bodyforger.core.database.BodyForgerDatabase
import app.bodyforger.core.healthconnect.HealthConnectManager
import org.json.JSONArray
import org.json.JSONObject

class McpToolRegistry(
    healthConnectManager: HealthConnectManager? = null,
    database: BodyForgerDatabase? = null
) {
    var consentOverride: Boolean? = null
    private val healthConnectHandler = HealthConnectToolHandler(healthConnectManager) { consentOverride }
    private val exerciseHandler = BodyForgerExerciseToolHandler(database)
    private val routineHandler = BodyForgerRoutineToolHandler(database)
    private val workoutHandler = BodyForgerWorkoutToolHandler(database)
    private val bodyLogHandler = BodyForgerBodyLogToolHandler(database)

    suspend fun isHealthConsentGranted(): Boolean = healthConnectHandler.isConsentGranted()

    suspend fun listToolsJson(): JSONArray = JSONArray().apply {
        healthConnectHandler.listToolDescriptors().forEach { put(it) }
        exerciseHandler.listToolDescriptors().forEach { put(it) }
        routineHandler.listToolDescriptors().forEach { put(it) }
        workoutHandler.listToolDescriptors().forEach { put(it) }
        bodyLogHandler.listToolDescriptors().forEach { put(it) }
    }

    suspend fun executeTool(name: String, arguments: JSONObject): JSONObject {
        return try {
            when {
                healthConnectHandler.canHandle(name) -> healthConnectHandler.execute(name, arguments)
                exerciseHandler.canHandle(name) -> exerciseHandler.execute(name, arguments)
                routineHandler.canHandle(name) -> routineHandler.execute(name, arguments)
                workoutHandler.canHandle(name) -> workoutHandler.execute(name, arguments)
                bodyLogHandler.canHandle(name) -> bodyLogHandler.execute(name, arguments)
                else -> McpToolHelpers.errorJson("Unknown tool: $name")
            }
        } catch (t: Throwable) {
            McpToolHelpers.errorJson("Tool execution error: ${t.message}")
        }
    }

    companion object {
        const val TOOL_HEALTH_STATUS = HealthConnectToolHandler.TOOL_HEALTH_STATUS
        const val TOOL_HEALTH_INSPECT_SUMMARY = HealthConnectToolHandler.TOOL_HEALTH_INSPECT_SUMMARY
        const val TOOL_HEALTH_READ_SESSIONS = HealthConnectToolHandler.TOOL_HEALTH_READ_SESSIONS
        const val TOOL_HEALTH_READ_WEIGHTS = HealthConnectToolHandler.TOOL_HEALTH_READ_WEIGHTS
        const val TOOL_HEALTH_READ_HEART_RATES = HealthConnectToolHandler.TOOL_HEALTH_READ_HEART_RATES
        const val TOOL_BODYFORGER_LOCAL_SUMMARY = BodyForgerWorkoutToolHandler.TOOL_LOCAL_SUMMARY
        const val TOOL_SEARCH_EXERCISES = BodyForgerExerciseToolHandler.TOOL_SEARCH_EXERCISES
        const val TOOL_LIST_EXERCISES = BodyForgerExerciseToolHandler.TOOL_LIST_EXERCISES
        const val TOOL_CREATE_EXERCISE = BodyForgerExerciseToolHandler.TOOL_CREATE_EXERCISE
        const val TOOL_CREATE_ROUTINE = BodyForgerRoutineToolHandler.TOOL_CREATE_ROUTINE
        const val TOOL_LIST_ROUTINES = BodyForgerRoutineToolHandler.TOOL_LIST_ROUTINES
        const val TOOL_GET_ROUTINE = BodyForgerRoutineToolHandler.TOOL_GET_ROUTINE
        const val TOOL_INSERT_WORKOUT = BodyForgerWorkoutToolHandler.TOOL_INSERT_WORKOUT
        const val TOOL_LIST_WORKOUTS = BodyForgerWorkoutToolHandler.TOOL_LIST_WORKOUTS
        const val TOOL_GET_WORKOUT = BodyForgerWorkoutToolHandler.TOOL_GET_WORKOUT
        const val TOOL_INSERT_BODY_LOG = BodyForgerBodyLogToolHandler.TOOL_INSERT_BODY_LOG
        const val TOOL_LIST_BODY_LOGS = BodyForgerBodyLogToolHandler.TOOL_LIST_BODY_LOGS
    }
}
