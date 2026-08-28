package app.bodyforger.core.sync

import app.bodyforger.core.model.BodyLog
import app.bodyforger.core.model.EquipmentType
import app.bodyforger.core.model.Exercise
import app.bodyforger.core.model.HealthConnectExerciseType
import app.bodyforger.core.model.MuscleGroup
import app.bodyforger.core.model.Routine
import app.bodyforger.core.model.RoutineExercise
import app.bodyforger.core.model.RoutineSet
import app.bodyforger.core.model.RoutineSetType
import app.bodyforger.core.model.UnilateralSide
import app.bodyforger.core.model.WeightUnit
import app.bodyforger.core.model.WorkoutActivityCategory
import app.bodyforger.core.model.WorkoutSession
import app.bodyforger.core.model.WorkoutSessionStatus
import app.bodyforger.core.model.WorkoutSet
import org.json.JSONArray
import org.json.JSONObject

data class BodyForgerBackupPayload(
    val schemaVersion: Int = 1,
    val exportedAtEpochMs: Long = System.currentTimeMillis(),
    val appVersion: String = "0.1.0",
    val routines: List<Routine> = emptyList(),
    val sessions: List<WorkoutSession> = emptyList(),
    val bodyLogs: List<BodyLog> = emptyList(),
    val customExercises: List<Exercise> = emptyList()
)

object JsonBackupManager {

    const val CURRENT_SCHEMA_VERSION = 1

    fun serialize(payload: BodyForgerBackupPayload): String {
        val root = JSONObject().apply {
            put("schemaVersion", payload.schemaVersion)
            put("exportedAtEpochMs", payload.exportedAtEpochMs)
            put("appVersion", payload.appVersion)

            // Routines
            val routinesArr = JSONArray()
            payload.routines.forEach { r ->
                val rObj = JSONObject().apply {
                    put("id", r.id)
                    put("name", r.name)
                    put("notes", r.notes)
                    put("createdAtEpochMs", r.createdAtEpochMs)
                    val daysArr = JSONArray()
                    r.assignedDays.forEach { daysArr.put(it) }
                    put("assignedDays", daysArr)

                    val exArr = JSONArray()
                    r.exercises.forEach { ex ->
                        val exObj = JSONObject().apply {
                            put("id", ex.id)
                            put("routineId", ex.routineId)
                            put("exerciseId", ex.exerciseId)
                            put("exerciseName", ex.exerciseName)
                            put("activityCategory", ex.activityCategory.name)
                            put("primaryMuscle", ex.primaryMuscle.name)
                            put("equipment", ex.equipment.name)
                            put("isUnilateral", ex.isUnilateral)
                            put("weightUnit", ex.weightUnit.name)
                            put("orderIndex", ex.orderIndex)
                            put("restTimeSeconds", ex.restTimeSeconds)
                            put("notes", ex.notes)

                            val setsArr = JSONArray()
                            ex.sets.forEach { s ->
                                val sObj = JSONObject().apply {
                                    put("id", s.id)
                                    put("setIndex", s.setIndex)
                                    put("type", s.type.name)
                                    s.targetWeightKg?.let { put("targetWeightKg", it) }
                                    s.reps?.let { put("reps", it) }
                                    s.minReps?.let { put("minReps", it) }
                                    s.maxReps?.let { put("maxReps", it) }
                                    put("isRepsRange", s.isRepsRange)
                                }
                                setsArr.put(sObj)
                            }
                            put("sets", setsArr)
                        }
                        exArr.put(exObj)
                    }
                    put("exercises", exArr)
                }
                routinesArr.put(rObj)
            }
            put("routines", routinesArr)

            // Sessions
            val sessionsArr = JSONArray()
            payload.sessions.forEach { s ->
                val sObj = JSONObject().apply {
                    put("id", s.id)
                    s.routineId?.let { put("routineId", it) }
                    put("title", s.title)
                    put("notes", s.notes)
                    put("status", s.status.name)
                    put("startedAtEpochMs", s.startedAtEpochMs)
                    s.endedAtEpochMs?.let { put("endedAtEpochMs", it) }
                    s.averageHeartRateBpm?.let { put("averageHeartRateBpm", it) }
                    s.activeCaloriesKcal?.let { put("activeCaloriesKcal", it) }
                    put("totalVolumeKg", s.totalVolumeKg)
                    put("isFinalized", s.isFinalized)

                    val setsArr = JSONArray()
                    s.sets.forEach { setItem ->
                        val setObj = JSONObject().apply {
                            put("id", setItem.id)
                            put("sessionId", setItem.sessionId)
                            put("exerciseId", setItem.exerciseId)
                            put("exerciseName", setItem.exerciseName)
                            put("primaryMuscle", setItem.primaryMuscle.name)
                            put("equipment", setItem.equipment.name)
                            put("activityCategory", setItem.activityCategory.name)
                            put("orderIndex", setItem.orderIndex)
                            put("setIndex", setItem.setIndex)
                            put("type", setItem.type.name)
                            put("weightKg", setItem.weightKg)
                            put("weightUnit", setItem.weightUnit.name)
                            put("reps", setItem.reps)
                            setItem.rpe?.let { put("rpe", it) }
                            put("isCompleted", setItem.isCompleted)
                            put("side", setItem.side.name)
                            put("restTimeSeconds", setItem.restTimeSeconds)
                            setItem.completedAtEpochMs?.let { put("completedAtEpochMs", it) }
                        }
                        setsArr.put(setObj)
                    }
                    put("sets", setsArr)
                }
                sessionsArr.put(sObj)
            }
            put("sessions", sessionsArr)

            // Custom Exercises
            val customExArr = JSONArray()
            payload.customExercises.forEach { cEx ->
                val cObj = JSONObject().apply {
                    put("id", cEx.id)
                    put("name", cEx.name)
                    put("healthConnectType", cEx.healthConnectType.name)
                    put("primaryMuscleGroup", cEx.primaryMuscleGroup.name)
                    val secMusclesArr = JSONArray()
                    cEx.secondaryMuscleGroups.forEach { secMusclesArr.put(it.name) }
                    put("secondaryMuscleGroups", secMusclesArr)
                    put("equipment", cEx.equipment.name)
                    put("isUnilateral", cEx.isUnilateral)
                    put("isCustom", cEx.isCustom)
                }
                customExArr.put(cObj)
            }
            put("customExercises", customExArr)
        }
        return root.toString(2)
    }

    fun deserialize(jsonString: String): BodyForgerBackupPayload {
        val root = JSONObject(jsonString)
        val schemaVersion = root.optInt("schemaVersion", CURRENT_SCHEMA_VERSION)
        val exportedAtEpochMs = root.optLong("exportedAtEpochMs", System.currentTimeMillis())
        val appVersion = root.optString("appVersion", "0.1.0")

        // Parse Routines
        val routines = mutableListOf<Routine>()
        val routinesArr = root.optJSONArray("routines")
        if (routinesArr != null) {
            for (i in 0 until routinesArr.length()) {
                val rObj = routinesArr.getJSONObject(i)
                val assignedDays = mutableSetOf<Int>()
                val daysArr = rObj.optJSONArray("assignedDays")
                if (daysArr != null) {
                    for (d in 0 until daysArr.length()) {
                        assignedDays.add(daysArr.getInt(d))
                    }
                }

                val exercises = mutableListOf<RoutineExercise>()
                val exArr = rObj.optJSONArray("exercises")
                if (exArr != null) {
                    for (e in 0 until exArr.length()) {
                        val exObj = exArr.getJSONObject(e)
                        val sets = mutableListOf<RoutineSet>()
                        val setsArr = exObj.optJSONArray("sets")
                        if (setsArr != null) {
                            for (s in 0 until setsArr.length()) {
                                val sObj = setsArr.getJSONObject(s)
                                sets.add(
                                    RoutineSet(
                                        id = sObj.optString("id"),
                                        setIndex = sObj.optInt("setIndex", 1),
                                        type = try { RoutineSetType.valueOf(sObj.optString("type")) } catch (e: Exception) { RoutineSetType.NORMAL },
                                        targetWeightKg = if (sObj.has("targetWeightKg")) sObj.getDouble("targetWeightKg") else null,
                                        reps = if (sObj.has("reps")) sObj.getInt("reps") else null,
                                        minReps = if (sObj.has("minReps")) sObj.getInt("minReps") else null,
                                        maxReps = if (sObj.has("maxReps")) sObj.getInt("maxReps") else null,
                                        isRepsRange = sObj.optBoolean("isRepsRange", false)
                                    )
                                )
                            }
                        }

                        exercises.add(
                            RoutineExercise(
                                id = exObj.optString("id"),
                                routineId = exObj.optString("routineId"),
                                exerciseId = exObj.optString("exerciseId"),
                                exerciseName = exObj.optString("exerciseName"),
                                activityCategory = try { WorkoutActivityCategory.valueOf(exObj.optString("activityCategory")) } catch (e: Exception) { WorkoutActivityCategory.STRENGTH_TRAINING },
                                primaryMuscle = try { MuscleGroup.valueOf(exObj.optString("primaryMuscle")) } catch (e: Exception) { MuscleGroup.CHEST },
                                equipment = try { EquipmentType.valueOf(exObj.optString("equipment")) } catch (e: Exception) { EquipmentType.BARBELL },
                                isUnilateral = exObj.optBoolean("isUnilateral", false),
                                weightUnit = try { WeightUnit.valueOf(exObj.optString("weightUnit")) } catch (e: Exception) { WeightUnit.KG },
                                orderIndex = exObj.optInt("orderIndex", 0),
                                restTimeSeconds = exObj.optInt("restTimeSeconds", 90),
                                notes = exObj.optString("notes", ""),
                                sets = sets
                            )
                        )
                    }
                }

                routines.add(
                    Routine(
                        id = rObj.optString("id"),
                        name = rObj.optString("name"),
                        notes = rObj.optString("notes", ""),
                        assignedDays = assignedDays,
                        exercises = exercises,
                        createdAtEpochMs = rObj.optLong("createdAtEpochMs", System.currentTimeMillis())
                    )
                )
            }
        }

        // Parse Sessions
        val sessions = mutableListOf<WorkoutSession>()
        val sessionsArr = root.optJSONArray("sessions")
        if (sessionsArr != null) {
            for (i in 0 until sessionsArr.length()) {
                val sObj = sessionsArr.getJSONObject(i)
                val sets = mutableListOf<WorkoutSet>()
                val setsArr = sObj.optJSONArray("sets")
                if (setsArr != null) {
                    for (k in 0 until setsArr.length()) {
                        val setObj = setsArr.getJSONObject(k)
                        sets.add(
                            WorkoutSet(
                                id = setObj.optString("id"),
                                sessionId = setObj.optString("sessionId"),
                                exerciseId = setObj.optString("exerciseId"),
                                exerciseName = setObj.optString("exerciseName"),
                                primaryMuscle = try { MuscleGroup.valueOf(setObj.optString("primaryMuscle")) } catch (e: Exception) { MuscleGroup.CHEST },
                                equipment = try { EquipmentType.valueOf(setObj.optString("equipment")) } catch (e: Exception) { EquipmentType.BARBELL },
                                activityCategory = try { WorkoutActivityCategory.valueOf(setObj.optString("activityCategory")) } catch (e: Exception) { WorkoutActivityCategory.STRENGTH_TRAINING },
                                orderIndex = setObj.optInt("orderIndex", 0),
                                setIndex = setObj.optInt("setIndex", 1),
                                type = try { RoutineSetType.valueOf(setObj.optString("type")) } catch (e: Exception) { RoutineSetType.NORMAL },
                                weightKg = setObj.optDouble("weightKg", 0.0),
                                weightUnit = try { WeightUnit.valueOf(setObj.optString("weightUnit")) } catch (e: Exception) { WeightUnit.KG },
                                reps = setObj.optInt("reps", 0),
                                rpe = if (setObj.has("rpe")) setObj.getDouble("rpe") else null,
                                isCompleted = setObj.optBoolean("isCompleted", false),
                                side = try { UnilateralSide.valueOf(setObj.optString("side")) } catch (e: Exception) { UnilateralSide.NONE },
                                restTimeSeconds = setObj.optInt("restTimeSeconds", 90),
                                completedAtEpochMs = if (setObj.has("completedAtEpochMs")) setObj.getLong("completedAtEpochMs") else null
                            )
                        )
                    }
                }

                sessions.add(
                    WorkoutSession(
                        id = sObj.optString("id"),
                        routineId = if (sObj.has("routineId")) sObj.getString("routineId") else null,
                        title = sObj.optString("title"),
                        notes = sObj.optString("notes", ""),
                        status = try { WorkoutSessionStatus.valueOf(sObj.optString("status")) } catch (e: Exception) { WorkoutSessionStatus.COMPLETED },
                        startedAtEpochMs = sObj.optLong("startedAtEpochMs", System.currentTimeMillis()),
                        endedAtEpochMs = if (sObj.has("endedAtEpochMs")) sObj.getLong("endedAtEpochMs") else null,
                        sets = sets,
                        averageHeartRateBpm = if (sObj.has("averageHeartRateBpm")) sObj.getInt("averageHeartRateBpm") else null,
                        activeCaloriesKcal = if (sObj.has("activeCaloriesKcal")) sObj.getInt("activeCaloriesKcal") else null,
                        totalVolumeKg = sObj.optDouble("totalVolumeKg", 0.0),
                        isFinalized = sObj.optBoolean("isFinalized", true)
                    )
                )
            }
        }

        // Parse Custom Exercises
        val customExercises = mutableListOf<Exercise>()
        val customExArr = root.optJSONArray("customExercises")
        if (customExArr != null) {
            for (i in 0 until customExArr.length()) {
                val cObj = customExArr.getJSONObject(i)
                val secMuscles = mutableListOf<MuscleGroup>()
                val secMusclesArr = cObj.optJSONArray("secondaryMuscleGroups")
                if (secMusclesArr != null) {
                    for (m in 0 until secMusclesArr.length()) {
                        try {
                            secMuscles.add(MuscleGroup.valueOf(secMusclesArr.getString(m)))
                        } catch (_: Exception) {}
                    }
                }

                customExercises.add(
                    Exercise(
                        id = cObj.optString("id"),
                        name = cObj.optString("name"),
                        healthConnectType = try { HealthConnectExerciseType.valueOf(cObj.optString("healthConnectType")) } catch (e: Exception) { HealthConnectExerciseType.OTHER_WORKOUT },
                        primaryMuscleGroup = try { MuscleGroup.valueOf(cObj.optString("primaryMuscleGroup")) } catch (e: Exception) { MuscleGroup.CHEST },
                        secondaryMuscleGroups = secMuscles,
                        equipment = try { EquipmentType.valueOf(cObj.optString("equipment")) } catch (e: Exception) { EquipmentType.BARBELL },
                        isUnilateral = cObj.optBoolean("isUnilateral", false),
                        isCustom = cObj.optBoolean("isCustom", true)
                    )
                )
            }
        }

        return BodyForgerBackupPayload(
            schemaVersion = schemaVersion,
            exportedAtEpochMs = exportedAtEpochMs,
            appVersion = appVersion,
            routines = routines,
            sessions = sessions,
            bodyLogs = emptyList(),
            customExercises = customExercises
        )
    }
}
