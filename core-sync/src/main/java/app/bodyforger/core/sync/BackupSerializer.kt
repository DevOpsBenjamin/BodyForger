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

/** Writes a backup payload as JSON. Field names live in [BackupKeys]. */
internal object BackupSerializer {

    fun write(payload: BodyForgerBackupPayload): String {
        val root = JSONObject().apply {
            put(BackupKeys.SCHEMA_VERSION, payload.schemaVersion)
            put(BackupKeys.EXPORTED_AT, payload.exportedAtEpochMs)
            put(BackupKeys.APP_VERSION, payload.appVersion)

            // Routines
            val routinesArr = JSONArray()
            payload.routines.forEach { r ->
                val rObj = JSONObject().apply {
                    put(BackupKeys.ID, r.id)
                    put(BackupKeys.NAME, r.name)
                    put(BackupKeys.NOTES, r.notes)
                    put(BackupKeys.CREATED_AT, r.createdAtEpochMs)
                    val daysArr = JSONArray()
                    r.assignedDays.forEach { daysArr.put(it) }
                    put(BackupKeys.ASSIGNED_DAYS, daysArr)

                    val exArr = JSONArray()
                    r.exercises.forEach { ex ->
                        val exObj = JSONObject().apply {
                            put(BackupKeys.ID, ex.id)
                            put(BackupKeys.ROUTINE_ID, ex.routineId)
                            put(BackupKeys.EXERCISE_ID, ex.exerciseId)
                            put(BackupKeys.EXERCISE_NAME, ex.exerciseName)
                            put(BackupKeys.ACTIVITY_CATEGORY, ex.activityCategory.name)
                            put(BackupKeys.PRIMARY_MUSCLE, ex.primaryMuscle.name)
                            put(BackupKeys.EQUIPMENT, ex.equipment.name)
                            put(BackupKeys.IS_UNILATERAL, ex.isUnilateral)
                            put(BackupKeys.WEIGHT_UNIT, ex.weightUnit.name)
                            put(BackupKeys.ORDER_INDEX, ex.orderIndex)
                            put(BackupKeys.REST_TIME_SECONDS, ex.restTimeSeconds)
                            put(BackupKeys.NOTES, ex.notes)

                            val setsArr = JSONArray()
                            ex.sets.forEach { s ->
                                val sObj = JSONObject().apply {
                                    put(BackupKeys.ID, s.id)
                                    put(BackupKeys.SET_INDEX, s.setIndex)
                                    put(BackupKeys.TYPE, s.type.name)
                                    s.targetWeightKg?.let { put(BackupKeys.TARGET_WEIGHT_KG, it) }
                                    s.reps?.let { put(BackupKeys.REPS, it) }
                                    s.minReps?.let { put(BackupKeys.MIN_REPS, it) }
                                    s.maxReps?.let { put(BackupKeys.MAX_REPS, it) }
                                    put(BackupKeys.IS_REPS_RANGE, s.isRepsRange)
                                }
                                setsArr.put(sObj)
                            }
                            put(BackupKeys.SETS, setsArr)
                        }
                        exArr.put(exObj)
                    }
                    put(BackupKeys.EXERCISES, exArr)
                }
                routinesArr.put(rObj)
            }
            put(BackupKeys.ROUTINES, routinesArr)

            // Sessions
            val sessionsArr = JSONArray()
            payload.sessions.forEach { s ->
                val sObj = JSONObject().apply {
                    put(BackupKeys.ID, s.id)
                    s.routineId?.let { put(BackupKeys.ROUTINE_ID, it) }
                    put(BackupKeys.TITLE, s.title)
                    put(BackupKeys.NOTES, s.notes)
                    put(BackupKeys.STATUS, s.status.name)
                    put(BackupKeys.STARTED_AT, s.startedAtEpochMs)
                    s.endedAtEpochMs?.let { put(BackupKeys.ENDED_AT, it) }
                    s.averageHeartRateBpm?.let { put(BackupKeys.AVERAGE_HEART_RATE_BPM, it) }
                    s.activeCaloriesKcal?.let { put(BackupKeys.ACTIVE_CALORIES_KCAL, it) }
                    put(BackupKeys.TOTAL_VOLUME_KG, s.totalVolumeKg)
                    put(BackupKeys.IS_FINALIZED, s.isFinalized)

                    val setsArr = JSONArray()
                    s.sets.forEach { setItem ->
                        val setObj = JSONObject().apply {
                            put(BackupKeys.ID, setItem.id)
                            put(BackupKeys.SESSION_ID, setItem.sessionId)
                            put(BackupKeys.EXERCISE_ID, setItem.exerciseId)
                            put(BackupKeys.EXERCISE_NAME, setItem.exerciseName)
                            put(BackupKeys.PRIMARY_MUSCLE, setItem.primaryMuscle.name)
                            put(BackupKeys.EQUIPMENT, setItem.equipment.name)
                            put(BackupKeys.ACTIVITY_CATEGORY, setItem.activityCategory.name)
                            put(BackupKeys.ORDER_INDEX, setItem.orderIndex)
                            put(BackupKeys.SET_INDEX, setItem.setIndex)
                            put(BackupKeys.TYPE, setItem.type.name)
                            put(BackupKeys.WEIGHT_KG, setItem.weightKg)
                            put(BackupKeys.WEIGHT_UNIT, setItem.weightUnit.name)
                            put(BackupKeys.REPS, setItem.reps)
                            setItem.rpe?.let { put(BackupKeys.RPE, it) }
                            put(BackupKeys.IS_COMPLETED, setItem.isCompleted)
                            put(BackupKeys.SIDE, setItem.side.name)
                            put(BackupKeys.REST_TIME_SECONDS, setItem.restTimeSeconds)
                            setItem.completedAtEpochMs?.let { put(BackupKeys.COMPLETED_AT, it) }
                        }
                        setsArr.put(setObj)
                    }
                    put(BackupKeys.SETS, setsArr)
                }
                sessionsArr.put(sObj)
            }
            put(BackupKeys.SESSIONS, sessionsArr)

            // Custom Exercises
            val customExArr = JSONArray()
            payload.customExercises.forEach { cEx ->
                val cObj = JSONObject().apply {
                    put(BackupKeys.ID, cEx.id)
                    put(BackupKeys.NAME, cEx.name)
                    put(BackupKeys.HEALTH_CONNECT_TYPE, cEx.healthConnectType.name)
                    put(BackupKeys.PRIMARY_MUSCLE_GROUP, cEx.primaryMuscleGroup.name)
                    val secMusclesArr = JSONArray()
                    cEx.secondaryMuscleGroups.forEach { secMusclesArr.put(it.name) }
                    put(BackupKeys.SECONDARY_MUSCLE_GROUPS, secMusclesArr)
                    put(BackupKeys.EQUIPMENT, cEx.equipment.name)
                    put(BackupKeys.IS_UNILATERAL, cEx.isUnilateral)
                    put(BackupKeys.IS_CUSTOM, cEx.isCustom)
                }
                customExArr.put(cObj)
            }
            put(BackupKeys.CUSTOM_EXERCISES, customExArr)
        }
        return root.toString(2)
    }
}
