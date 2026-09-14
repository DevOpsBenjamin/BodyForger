package app.bodyforger.mobile.mcp

import app.bodyforger.core.database.BodyForgerDatabase
import app.bodyforger.core.database.entity.RoutineEntity
import app.bodyforger.core.database.entity.RoutineExerciseEntity
import app.bodyforger.core.database.entity.RoutineSetEntity
import app.bodyforger.core.database.entity.toDomain
import app.bodyforger.core.model.EquipmentType
import app.bodyforger.core.model.MuscleGroup
import app.bodyforger.core.model.RoutineSetType
import app.bodyforger.mobile.mcp.McpToolHelpers.arrayProp
import app.bodyforger.mobile.mcp.McpToolHelpers.errorJson
import app.bodyforger.mobile.mcp.McpToolHelpers.stringProp
import app.bodyforger.mobile.mcp.McpToolHelpers.toolDescriptor
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.UUID

class BodyForgerRoutineToolHandler(private val database: BodyForgerDatabase?) {

    fun listToolDescriptors(): List<JSONObject> = listOf(
        toolDescriptor(
            TOOL_CREATE_ROUTINE,
            "Create or update a workout routine template with ordered exercises and target sets.",
            JSONObject().apply {
                put("name", stringProp("Routine name (e.g. Push Hypertrophy A)"))
                put("notes", stringProp("Routine notes or instructions"))
                put("assignedDays", arrayProp("Day numbers of the week (1 = Mon, 7 = Sun)"))
                put("exercises", arrayProp("List of exercise objects with exerciseId, orderIndex, restTimeSeconds, sets"))
            },
            required = listOf("name", "exercises")
        ),
        toolDescriptor(
            TOOL_LIST_ROUTINES,
            "List all workout routines with their exercises and planned sets.",
            JSONObject()
        ),
        toolDescriptor(
            TOOL_GET_ROUTINE,
            "Get details of a specific routine by ID or name.",
            JSONObject().apply {
                put("id", stringProp("Routine ID"))
                put("name", stringProp("Routine name (if ID unknown)"))
            }
        )
    )

    fun canHandle(name: String): Boolean = name in SUPPORTED_TOOLS

    suspend fun execute(name: String, arguments: JSONObject): JSONObject {
        val db = database ?: return errorJson("BodyForger database is not initialized.")
        return when (name) {
            TOOL_CREATE_ROUTINE -> handleCreateRoutine(db, arguments)
            TOOL_LIST_ROUTINES -> handleListRoutines(db)
            TOOL_GET_ROUTINE -> handleGetRoutine(db, arguments)
            else -> errorJson("Unsupported tool: $name")
        }
    }

    private suspend fun handleCreateRoutine(db: BodyForgerDatabase, args: JSONObject): JSONObject {
        val routineName = args.optString("name", "").trim()
        if (routineName.isEmpty()) return errorJson("Routine name is required.")

        val routineId = UUID.randomUUID().toString()
        val notes = args.optString("notes", "")
        val daysJson = args.optJSONArray("assignedDays") ?: JSONArray()
        val daysCsv = (0 until daysJson.length()).map { daysJson.getInt(it) }.sorted().joinToString(",")

        val routineEntity = RoutineEntity(
            id = routineId,
            name = routineName,
            notes = notes,
            assignedDaysCsv = daysCsv,
            createdAtEpochMs = System.currentTimeMillis()
        )

        val exercisesJson = args.optJSONArray("exercises") ?: JSONArray()
        val exerciseEntities = mutableListOf<RoutineExerciseEntity>()
        val setEntities = mutableListOf<RoutineSetEntity>()

        for (i in 0 until exercisesJson.length()) {
            val exObj = exercisesJson.getJSONObject(i)
            val exerciseId = exObj.optString("exerciseId", "")
            val dbExercise = db.exerciseDao().findExerciseById(exerciseId)
            val exerciseName = exObj.optString("exerciseName", dbExercise?.name ?: "Exercise")
            val primaryMuscle = dbExercise?.primaryMuscleGroup ?: MuscleGroup.FULL_BODY.name
            val equipment = dbExercise?.equipment ?: EquipmentType.BARBELL.name
            val restSeconds = exObj.optInt("restTimeSeconds", 90)
            val routineExerciseId = UUID.randomUUID().toString()

            exerciseEntities.add(
                RoutineExerciseEntity(
                    id = routineExerciseId,
                    routineId = routineId,
                    exerciseId = exerciseId,
                    exerciseName = exerciseName,
                    primaryMuscle = primaryMuscle,
                    equipment = equipment,
                    orderIndex = exObj.optInt("orderIndex", i),
                    restTimeSeconds = restSeconds,
                    notes = exObj.optString("notes", "")
                )
            )

            val setsJson = exObj.optJSONArray("sets") ?: JSONArray()
            for (s in 0 until setsJson.length()) {
                val sObj = setsJson.getJSONObject(s)
                setEntities.add(
                    RoutineSetEntity(
                        id = UUID.randomUUID().toString(),
                        routineExerciseId = routineExerciseId,
                        setIndex = sObj.optInt("setIndex", s + 1),
                        type = sObj.optString("type", RoutineSetType.NORMAL.name).uppercase(Locale.ROOT),
                        targetWeightKg = if (sObj.has("targetWeightKg")) sObj.getDouble("targetWeightKg") else null,
                        reps = if (sObj.has("reps")) sObj.getInt("reps") else 10,
                        minReps = if (sObj.has("minReps")) sObj.getInt("minReps") else 8,
                        maxReps = if (sObj.has("maxReps")) sObj.getInt("maxReps") else 12,
                        isRepsRange = sObj.optBoolean("isRepsRange", sObj.has("minReps"))
                    )
                )
            }
        }

        db.routineDao().saveFullRoutine(routineEntity, exerciseEntities, setEntities)
        return JSONObject().apply {
            put("success", true)
            put("routineId", routineId)
            put("name", routineName)
            put("exerciseCount", exerciseEntities.size)
            put("setCount", setEntities.size)
        }
    }

    private suspend fun handleListRoutines(db: BodyForgerDatabase): JSONObject {
        val routines = db.routineDao().getAllRoutinesWithExercises()
        val array = JSONArray()
        routines.forEach { rwe ->
            val domain = rwe.toDomain()
            array.put(JSONObject().apply {
                put("id", domain.id)
                put("name", domain.name)
                put("notes", domain.notes)
                put("assignedDays", JSONArray(domain.assignedDays.toList()))
                put("exerciseCount", domain.exercises.size)
                val exArray = JSONArray()
                domain.exercises.forEach { ex ->
                    exArray.put(JSONObject().apply {
                        put("id", ex.id)
                        put("exerciseId", ex.exerciseId)
                        put("exerciseName", ex.exerciseName)
                        put("primaryMuscle", ex.primaryMuscle.name)
                        put("equipment", ex.equipment.name)
                        put("restTimeSeconds", ex.restTimeSeconds)
                        put("setCount", ex.sets.size)
                    })
                }
                put("exercises", exArray)
            })
        }
        return JSONObject().apply {
            put("count", routines.size)
            put("routines", array)
        }
    }

    private suspend fun handleGetRoutine(db: BodyForgerDatabase, args: JSONObject): JSONObject {
        val id = args.optString("id", "")
        val name = args.optString("name", "")
        val rwe = when {
            id.isNotEmpty() -> db.routineDao().getRoutineWithExercisesById(id)
            name.isNotEmpty() -> db.routineDao().findRoutineByName(name)
            else -> return errorJson("Either routine id or name must be provided.")
        } ?: return errorJson("Routine not found.")

        val domain = rwe.toDomain()
        return JSONObject().apply {
            put("id", domain.id)
            put("name", domain.name)
            put("notes", domain.notes)
            put("assignedDays", JSONArray(domain.assignedDays.toList()))
            val exArray = JSONArray()
            domain.exercises.forEach { ex ->
                exArray.put(JSONObject().apply {
                    put("id", ex.id)
                    put("exerciseId", ex.exerciseId)
                    put("exerciseName", ex.exerciseName)
                    put("primaryMuscle", ex.primaryMuscle.name)
                    put("equipment", ex.equipment.name)
                    put("restTimeSeconds", ex.restTimeSeconds)
                    val setsArray = JSONArray()
                    ex.sets.forEach { s ->
                        setsArray.put(JSONObject().apply {
                            put("setIndex", s.setIndex)
                            put("type", s.type.name)
                            if (s.targetWeightKg != null) put("targetWeightKg", s.targetWeightKg)
                            if (s.reps != null) put("reps", s.reps)
                            if (s.minReps != null) put("minReps", s.minReps)
                            if (s.maxReps != null) put("maxReps", s.maxReps)
                            put("isRepsRange", s.isRepsRange)
                        })
                    }
                    put("sets", setsArray)
                })
            }
            put("exercises", exArray)
        }
    }

    companion object {
        const val TOOL_CREATE_ROUTINE = "bodyforger_create_routine"
        const val TOOL_LIST_ROUTINES = "bodyforger_list_routines"
        const val TOOL_GET_ROUTINE = "bodyforger_get_routine"

        private val SUPPORTED_TOOLS = setOf(
            TOOL_CREATE_ROUTINE,
            TOOL_LIST_ROUTINES,
            TOOL_GET_ROUTINE
        )
    }
}
