package app.bodyforger.mobile.mcp

import app.bodyforger.core.database.BodyForgerDatabase
import app.bodyforger.core.database.entity.ExerciseEntity
import app.bodyforger.core.model.EquipmentType
import app.bodyforger.core.model.HealthConnectExerciseType
import app.bodyforger.core.model.MuscleGroup
import app.bodyforger.core.model.WorkoutActivityCategory
import app.bodyforger.mobile.mcp.McpToolHelpers.arrayProp
import app.bodyforger.mobile.mcp.McpToolHelpers.boolProp
import app.bodyforger.mobile.mcp.McpToolHelpers.errorJson
import app.bodyforger.mobile.mcp.McpToolHelpers.intProp
import app.bodyforger.mobile.mcp.McpToolHelpers.stringProp
import app.bodyforger.mobile.mcp.McpToolHelpers.toolDescriptor
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

class BodyForgerExerciseToolHandler(private val database: BodyForgerDatabase?) {

    fun listToolDescriptors(): List<JSONObject> = listOf(
        toolDescriptor(
            TOOL_SEARCH_EXERCISES,
            "Search exercises in BodyForger database by name, muscle, or equipment.",
            JSONObject().apply {
                put("query", stringProp("Search keyword matching exercise name or ID"))
                put("muscleGroup", stringProp("Filter by primary muscle (e.g. CHEST, BACK, LEGS)"))
                put("equipment", stringProp("Filter by equipment (e.g. BARBELL, DUMBBELL, CABLE, MACHINE)"))
                put("limit", intProp("Max results to return (default 30)"))
            }
        ),
        toolDescriptor(
            TOOL_LIST_EXERCISES,
            "List exercises from the BodyForger catalogue with pagination.",
            JSONObject().apply {
                put("limit", intProp("Number of exercises to return (default 50)"))
                put("offset", intProp("Pagination offset (default 0)"))
                put("onlyCustom", boolProp("Return only athlete-created custom exercises (default false)"))
            }
        ),
        toolDescriptor(
            TOOL_CREATE_EXERCISE,
            "Create a new custom exercise in the BodyForger database.",
            JSONObject().apply {
                put("name", stringProp("Exercise name (e.g. Incline Dumbbell Hammer Curl)"))
                put("primaryMuscleGroup", stringProp("Primary muscle group (e.g. BICEPS, CHEST, BACK, LEGS, SHOULDERS)"))
                put("equipment", stringProp("Equipment type (e.g. DUMBBELL, BARBELL, CABLE, MACHINE, BODYWEIGHT)"))
                put("activityCategory", stringProp("Activity category (default STRENGTH_TRAINING)"))
                put("isUnilateral", boolProp("Whether the exercise is performed one side at a time (default false)"))
                put("secondaryMuscleGroups", arrayProp("List of secondary muscle groups"))
            },
            required = listOf("name", "primaryMuscleGroup", "equipment")
        )
    )

    fun canHandle(name: String): Boolean = name in SUPPORTED_TOOLS

    suspend fun execute(name: String, arguments: JSONObject): JSONObject {
        val db = database ?: return errorJson("BodyForger database is not initialized.")
        return when (name) {
            TOOL_SEARCH_EXERCISES -> handleSearchExercises(db, arguments)
            TOOL_LIST_EXERCISES -> handleListExercises(db, arguments)
            TOOL_CREATE_EXERCISE -> handleCreateExercise(db, arguments)
            else -> errorJson("Unsupported tool: $name")
        }
    }

    private suspend fun handleSearchExercises(db: BodyForgerDatabase, args: JSONObject): JSONObject {
        val query = args.optString("query", "").trim()
        val muscleFilter = args.optString("muscleGroup", "").trim().uppercase(Locale.ROOT)
        val equipFilter = args.optString("equipment", "").trim().uppercase(Locale.ROOT)
        val limit = args.optInt("limit", 30).coerceIn(1, 100)

        val baseResults = if (query.isNotEmpty()) {
            db.exerciseDao().searchExercises(query, limit = 100)
        } else {
            db.exerciseDao().getExercisesPaged(limit = 100, offset = 0)
        }

        val filtered = baseResults.filter { ex ->
            (muscleFilter.isEmpty() || ex.primaryMuscleGroup.equals(muscleFilter, ignoreCase = true)) &&
            (equipFilter.isEmpty() || ex.equipment.equals(equipFilter, ignoreCase = true))
        }.take(limit)

        val array = JSONArray()
        filtered.forEach { ex ->
            array.put(JSONObject().apply {
                put("id", ex.id)
                put("name", ex.name)
                put("activityCategory", ex.activityCategory)
                put("primaryMuscleGroup", ex.primaryMuscleGroup)
                put("equipment", ex.equipment)
                put("isUnilateral", ex.isUnilateral)
                put("isCustom", ex.isCustom)
            })
        }
        return JSONObject().apply {
            put("count", filtered.size)
            put("exercises", array)
        }
    }

    private suspend fun handleListExercises(db: BodyForgerDatabase, args: JSONObject): JSONObject {
        val limit = args.optInt("limit", 50).coerceIn(1, 200)
        val offset = args.optInt("offset", 0).coerceAtLeast(0)
        val onlyCustom = args.optBoolean("onlyCustom", false)

        val paged = db.exerciseDao().getExercisesPaged(limit = limit, offset = offset)
        val filtered = if (onlyCustom) paged.filter { it.isCustom } else paged

        val array = JSONArray()
        filtered.forEach { ex ->
            array.put(JSONObject().apply {
                put("id", ex.id)
                put("name", ex.name)
                put("primaryMuscleGroup", ex.primaryMuscleGroup)
                put("equipment", ex.equipment)
                put("isUnilateral", ex.isUnilateral)
                put("isCustom", ex.isCustom)
            })
        }
        return JSONObject().apply {
            put("totalCount", db.exerciseDao().getExercisesCount())
            put("count", filtered.size)
            put("exercises", array)
        }
    }

    private suspend fun handleCreateExercise(db: BodyForgerDatabase, args: JSONObject): JSONObject {
        val name = args.optString("name", "").trim()
        if (name.isEmpty()) return errorJson("Exercise name is required.")

        val existing = db.exerciseDao().findExerciseByName(name)
        if (existing != null) {
            return JSONObject().apply {
                put("success", true)
                put("message", "Exercise already exists.")
                put("id", existing.id)
                put("name", existing.name)
            }
        }

        // Stored as text, so an unknown name would sit in the database until something read it
        // back and quietly fell to a default. Refusing here says which value was wrong, while
        // the exercise can still be created with a corrected one.
        val muscleStr = args.optString("primaryMuscleGroup", MuscleGroup.CHEST.name).uppercase(Locale.ROOT)
        if (enumOrNull<MuscleGroup>(muscleStr) == null) {
            return errorJson("Unknown primaryMuscleGroup '$muscleStr'. One of: ${names<MuscleGroup>()}")
        }
        val equipStr = args.optString("equipment", EquipmentType.BARBELL.name).uppercase(Locale.ROOT)
        if (enumOrNull<EquipmentType>(equipStr) == null) {
            return errorJson("Unknown equipment '$equipStr'. One of: ${names<EquipmentType>()}")
        }
        val catStr = args.optString("activityCategory", WorkoutActivityCategory.STRENGTH_TRAINING.name).uppercase(Locale.ROOT)
        if (enumOrNull<WorkoutActivityCategory>(catStr) == null) {
            return errorJson("Unknown activityCategory '$catStr'. One of: ${names<WorkoutActivityCategory>()}")
        }
        val isUnilateral = args.optBoolean("isUnilateral", false)

        // Declared by the tool and, until now, dropped on the floor.
        val secondaryJson = args.optJSONArray("secondaryMuscleGroups") ?: JSONArray()
        val secondary = (0 until secondaryJson.length()).map { secondaryJson.getString(it).trim().uppercase(Locale.ROOT) }
        secondary.firstOrNull { enumOrNull<MuscleGroup>(it) == null }?.let {
            return errorJson("Unknown secondary muscle group '$it'. One of: ${names<MuscleGroup>()}")
        }

        val slug = name.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9]+"), "_").trim('_')
        val id = "custom_$slug"

        val entity = ExerciseEntity(
            id = id,
            nameKey = null,
            name = name,
            activityCategory = catStr,
            healthConnectType = HealthConnectExerciseType.OTHER_WORKOUT.name,
            primaryMuscleGroup = muscleStr,
            equipment = equipStr,
            secondaryMuscleGroupsJson = JSONArray(secondary).toString(),
            isUnilateral = isUnilateral,
            isCustom = true
        )
        db.exerciseDao().insertExercise(entity)

        return JSONObject().apply {
            put("success", true)
            put("id", id)
            put("name", name)
            put("primaryMuscleGroup", muscleStr)
            put("equipment", equipStr)
            put("secondaryMuscleGroups", JSONArray(secondary))
        }
    }

    private inline fun <reified T : Enum<T>> enumOrNull(raw: String): T? =
        enumValues<T>().firstOrNull { it.name == raw }

    private inline fun <reified T : Enum<T>> names(): String =
        enumValues<T>().joinToString(", ") { it.name }

    companion object {
        const val TOOL_SEARCH_EXERCISES = "bodyforger_search_exercises"
        const val TOOL_LIST_EXERCISES = "bodyforger_list_exercises"
        const val TOOL_CREATE_EXERCISE = "bodyforger_create_exercise"

        private val SUPPORTED_TOOLS = setOf(
            TOOL_SEARCH_EXERCISES,
            TOOL_LIST_EXERCISES,
            TOOL_CREATE_EXERCISE
        )
    }
}
