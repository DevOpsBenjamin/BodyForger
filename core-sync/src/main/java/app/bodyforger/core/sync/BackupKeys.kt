package app.bodyforger.core.sync

/**
 * Field names of the backup format.
 *
 * Held in one place because serialisation and deserialisation must agree on every one of
 * them: a key typed twice can drift, and a backup written with one spelling and read with
 * another loses the field silently.
 */
internal object BackupKeys {

    const val SCHEMA_VERSION = "schemaVersion"
    const val EXPORTED_AT = "exportedAtEpochMs"
    const val APP_VERSION = "appVersion"
    const val ROUTINES = "routines"
    const val SESSIONS = "sessions"
    const val BODY_LOGS = "bodyLogs"
    const val CUSTOM_EXERCISES = "customExercises"

    const val ID = "id"
    const val NAME = "name"
    const val NOTES = "notes"
    const val TITLE = "title"
    const val TYPE = "type"
    const val STATUS = "status"
    const val CREATED_AT = "createdAtEpochMs"

    const val ASSIGNED_DAYS = "assignedDays"
    const val EXERCISES = "exercises"
    const val EXERCISE_ID = "exerciseId"
    const val EXERCISE_NAME = "exerciseName"
    const val ROUTINE_ID = "routineId"
    const val ORDER_INDEX = "orderIndex"
    const val REST_TIME_SECONDS = "restTimeSeconds"

    const val SETS = "sets"
    const val SET_INDEX = "setIndex"
    const val REPS = "reps"
    const val MIN_REPS = "minReps"
    const val MAX_REPS = "maxReps"
    const val IS_REPS_RANGE = "isRepsRange"
    const val TARGET_WEIGHT_KG = "targetWeightKg"
    const val WEIGHT_KG = "weightKg"
    const val WEIGHT_UNIT = "weightUnit"
    const val SIDE = "side"
    const val RPE = "rpe"
    const val IS_COMPLETED = "isCompleted"
    const val COMPLETED_AT = "completedAtEpochMs"
    const val SESSION_ID = "sessionId"

    const val STARTED_AT = "startedAtEpochMs"
    const val ENDED_AT = "endedAtEpochMs"
    const val IS_FINALIZED = "isFinalized"
    const val TOTAL_VOLUME_KG = "totalVolumeKg"
    const val AVERAGE_HEART_RATE_BPM = "averageHeartRateBpm"
    const val ACTIVE_CALORIES_KCAL = "activeCaloriesKcal"

    const val ACTIVITY_CATEGORY = "activityCategory"
    const val HEALTH_CONNECT_TYPE = "healthConnectType"
    const val PRIMARY_MUSCLE = "primaryMuscle"
    const val PRIMARY_MUSCLE_GROUP = "primaryMuscleGroup"
    const val SECONDARY_MUSCLE_GROUPS = "secondaryMuscleGroups"
    const val EQUIPMENT = "equipment"
    const val IS_UNILATERAL = "isUnilateral"
    const val IS_CUSTOM = "isCustom"
}
