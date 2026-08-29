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

/** Reads a backup payload from JSON. Field names live in [BackupKeys]. */
internal object BackupDeserializer {

    fun read(jsonString: String): BodyForgerBackupPayload {
        val root = JSONObject(jsonString)
        val schemaVersion = root.optInt(BackupKeys.SCHEMA_VERSION, JsonBackupManager.CURRENT_SCHEMA_VERSION)
        val exportedAtEpochMs = root.optLong(BackupKeys.EXPORTED_AT, System.currentTimeMillis())
        val appVersion = root.optString(BackupKeys.APP_VERSION, "0.1.0")

        // Parse Routines
        val routines = mutableListOf<Routine>()
        val routinesArr = root.optJSONArray(BackupKeys.ROUTINES)
        if (routinesArr != null) {
            for (i in 0 until routinesArr.length()) {
                val rObj = routinesArr.getJSONObject(i)
                val assignedDays = mutableSetOf<Int>()
                val daysArr = rObj.optJSONArray(BackupKeys.ASSIGNED_DAYS)
                if (daysArr != null) {
                    for (d in 0 until daysArr.length()) {
                        assignedDays.add(daysArr.getInt(d))
                    }
                }

                val exercises = mutableListOf<RoutineExercise>()
                val exArr = rObj.optJSONArray(BackupKeys.EXERCISES)
                if (exArr != null) {
                    for (e in 0 until exArr.length()) {
                        val exObj = exArr.getJSONObject(e)
                        val sets = mutableListOf<RoutineSet>()
                        val setsArr = exObj.optJSONArray(BackupKeys.SETS)
                        if (setsArr != null) {
                            for (s in 0 until setsArr.length()) {
                                val sObj = setsArr.getJSONObject(s)
                                sets.add(
                                    RoutineSet(
                                        id = sObj.optString(BackupKeys.ID),
                                        setIndex = sObj.optInt(BackupKeys.SET_INDEX, 1),
                                        type = try { RoutineSetType.valueOf(sObj.optString(BackupKeys.TYPE)) } catch (e: Exception) { RoutineSetType.NORMAL },
                                        targetWeightKg = if (sObj.has(BackupKeys.TARGET_WEIGHT_KG)) sObj.getDouble(BackupKeys.TARGET_WEIGHT_KG) else null,
                                        reps = if (sObj.has(BackupKeys.REPS)) sObj.getInt(BackupKeys.REPS) else null,
                                        minReps = if (sObj.has(BackupKeys.MIN_REPS)) sObj.getInt(BackupKeys.MIN_REPS) else null,
                                        maxReps = if (sObj.has(BackupKeys.MAX_REPS)) sObj.getInt(BackupKeys.MAX_REPS) else null,
                                        isRepsRange = sObj.optBoolean(BackupKeys.IS_REPS_RANGE, false)
                                    )
                                )
                            }
                        }

                        exercises.add(
                            RoutineExercise(
                                id = exObj.optString(BackupKeys.ID),
                                routineId = exObj.optString(BackupKeys.ROUTINE_ID),
                                exerciseId = exObj.optString(BackupKeys.EXERCISE_ID),
                                exerciseName = exObj.optString(BackupKeys.EXERCISE_NAME),
                                activityCategory = try { WorkoutActivityCategory.valueOf(exObj.optString(BackupKeys.ACTIVITY_CATEGORY)) } catch (e: Exception) { WorkoutActivityCategory.STRENGTH_TRAINING },
                                primaryMuscle = try { MuscleGroup.valueOf(exObj.optString(BackupKeys.PRIMARY_MUSCLE)) } catch (e: Exception) { MuscleGroup.CHEST },
                                equipment = try { EquipmentType.valueOf(exObj.optString(BackupKeys.EQUIPMENT)) } catch (e: Exception) { EquipmentType.BARBELL },
                                isUnilateral = exObj.optBoolean(BackupKeys.IS_UNILATERAL, false),
                                weightUnit = try { WeightUnit.valueOf(exObj.optString(BackupKeys.WEIGHT_UNIT)) } catch (e: Exception) { WeightUnit.KG },
                                orderIndex = exObj.optInt(BackupKeys.ORDER_INDEX, 0),
                                restTimeSeconds = exObj.optInt(BackupKeys.REST_TIME_SECONDS, 90),
                                notes = exObj.optString(BackupKeys.NOTES, ""),
                                sets = sets
                            )
                        )
                    }
                }

                routines.add(
                    Routine(
                        id = rObj.optString(BackupKeys.ID),
                        name = rObj.optString(BackupKeys.NAME),
                        notes = rObj.optString(BackupKeys.NOTES, ""),
                        assignedDays = assignedDays,
                        exercises = exercises,
                        createdAtEpochMs = rObj.optLong(BackupKeys.CREATED_AT, System.currentTimeMillis())
                    )
                )
            }
        }

        // Parse Sessions
        val sessions = mutableListOf<WorkoutSession>()
        val sessionsArr = root.optJSONArray(BackupKeys.SESSIONS)
        if (sessionsArr != null) {
            for (i in 0 until sessionsArr.length()) {
                val sObj = sessionsArr.getJSONObject(i)
                val sets = mutableListOf<WorkoutSet>()
                val setsArr = sObj.optJSONArray(BackupKeys.SETS)
                if (setsArr != null) {
                    for (k in 0 until setsArr.length()) {
                        val setObj = setsArr.getJSONObject(k)
                        sets.add(
                            WorkoutSet(
                                id = setObj.optString(BackupKeys.ID),
                                sessionId = setObj.optString(BackupKeys.SESSION_ID),
                                exerciseId = setObj.optString(BackupKeys.EXERCISE_ID),
                                exerciseName = setObj.optString(BackupKeys.EXERCISE_NAME),
                                primaryMuscle = try { MuscleGroup.valueOf(setObj.optString(BackupKeys.PRIMARY_MUSCLE)) } catch (e: Exception) { MuscleGroup.CHEST },
                                equipment = try { EquipmentType.valueOf(setObj.optString(BackupKeys.EQUIPMENT)) } catch (e: Exception) { EquipmentType.BARBELL },
                                activityCategory = try { WorkoutActivityCategory.valueOf(setObj.optString(BackupKeys.ACTIVITY_CATEGORY)) } catch (e: Exception) { WorkoutActivityCategory.STRENGTH_TRAINING },
                                orderIndex = setObj.optInt(BackupKeys.ORDER_INDEX, 0),
                                setIndex = setObj.optInt(BackupKeys.SET_INDEX, 1),
                                type = try { RoutineSetType.valueOf(setObj.optString(BackupKeys.TYPE)) } catch (e: Exception) { RoutineSetType.NORMAL },
                                weightKg = setObj.optDouble(BackupKeys.WEIGHT_KG, 0.0),
                                weightUnit = try { WeightUnit.valueOf(setObj.optString(BackupKeys.WEIGHT_UNIT)) } catch (e: Exception) { WeightUnit.KG },
                                reps = setObj.optInt(BackupKeys.REPS, 0),
                                rpe = if (setObj.has(BackupKeys.RPE)) setObj.getDouble(BackupKeys.RPE) else null,
                                isCompleted = setObj.optBoolean(BackupKeys.IS_COMPLETED, false),
                                side = try { UnilateralSide.valueOf(setObj.optString(BackupKeys.SIDE)) } catch (e: Exception) { UnilateralSide.NONE },
                                restTimeSeconds = setObj.optInt(BackupKeys.REST_TIME_SECONDS, 90),
                                completedAtEpochMs = if (setObj.has(BackupKeys.COMPLETED_AT)) setObj.getLong(BackupKeys.COMPLETED_AT) else null
                            )
                        )
                    }
                }

                sessions.add(
                    WorkoutSession(
                        id = sObj.optString(BackupKeys.ID),
                        routineId = if (sObj.has(BackupKeys.ROUTINE_ID)) sObj.getString(BackupKeys.ROUTINE_ID) else null,
                        title = sObj.optString(BackupKeys.TITLE),
                        notes = sObj.optString(BackupKeys.NOTES, ""),
                        status = try { WorkoutSessionStatus.valueOf(sObj.optString(BackupKeys.STATUS)) } catch (e: Exception) { WorkoutSessionStatus.COMPLETED },
                        startedAtEpochMs = sObj.optLong(BackupKeys.STARTED_AT, System.currentTimeMillis()),
                        endedAtEpochMs = if (sObj.has(BackupKeys.ENDED_AT)) sObj.getLong(BackupKeys.ENDED_AT) else null,
                        sets = sets,
                        averageHeartRateBpm = if (sObj.has(BackupKeys.AVERAGE_HEART_RATE_BPM)) sObj.getInt(BackupKeys.AVERAGE_HEART_RATE_BPM) else null,
                        activeCaloriesKcal = if (sObj.has(BackupKeys.ACTIVE_CALORIES_KCAL)) sObj.getInt(BackupKeys.ACTIVE_CALORIES_KCAL) else null,
                        totalVolumeKg = sObj.optDouble(BackupKeys.TOTAL_VOLUME_KG, 0.0),
                        isFinalized = sObj.optBoolean(BackupKeys.IS_FINALIZED, true)
                    )
                )
            }
        }

        // Parse Custom Exercises
        val customExercises = mutableListOf<Exercise>()
        val customExArr = root.optJSONArray(BackupKeys.CUSTOM_EXERCISES)
        if (customExArr != null) {
            for (i in 0 until customExArr.length()) {
                val cObj = customExArr.getJSONObject(i)
                val secMuscles = mutableListOf<MuscleGroup>()
                val secMusclesArr = cObj.optJSONArray(BackupKeys.SECONDARY_MUSCLE_GROUPS)
                if (secMusclesArr != null) {
                    for (m in 0 until secMusclesArr.length()) {
                        try {
                            secMuscles.add(MuscleGroup.valueOf(secMusclesArr.getString(m)))
                        } catch (_: Exception) {}
                    }
                }

                customExercises.add(
                    Exercise(
                        id = cObj.optString(BackupKeys.ID),
                        name = cObj.optString(BackupKeys.NAME),
                        healthConnectType = try { HealthConnectExerciseType.valueOf(cObj.optString(BackupKeys.HEALTH_CONNECT_TYPE)) } catch (e: Exception) { HealthConnectExerciseType.OTHER_WORKOUT },
                        primaryMuscleGroup = try { MuscleGroup.valueOf(cObj.optString(BackupKeys.PRIMARY_MUSCLE_GROUP)) } catch (e: Exception) { MuscleGroup.CHEST },
                        secondaryMuscleGroups = secMuscles,
                        equipment = try { EquipmentType.valueOf(cObj.optString(BackupKeys.EQUIPMENT)) } catch (e: Exception) { EquipmentType.BARBELL },
                        isUnilateral = cObj.optBoolean(BackupKeys.IS_UNILATERAL, false),
                        isCustom = cObj.optBoolean(BackupKeys.IS_CUSTOM, true)
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
