package app.bodyforger.mobile.mcp

import app.bodyforger.core.database.BodyForgerDatabase
import app.bodyforger.core.database.entity.WorkoutHeartRateSampleEntity
import app.bodyforger.core.database.entity.WorkoutSessionWithSets
import app.bodyforger.core.database.entity.WorkoutSetEntity
import app.bodyforger.core.database.entity.toDomain
import app.bodyforger.core.model.EquipmentType
import app.bodyforger.core.model.MuscleGroup
import app.bodyforger.core.model.RoutineSetType
import app.bodyforger.core.model.UnilateralSide
import app.bodyforger.core.model.WeightUnit
import app.bodyforger.core.model.WorkoutActivityCategory
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.UUID

internal object WorkoutMcpMapper {

    suspend fun parseSetEntities(
        sessionId: String,
        setsJson: JSONArray,
        db: BodyForgerDatabase
    ): Pair<List<WorkoutSetEntity>, Double> {
        val list = mutableListOf<WorkoutSetEntity>()
        var volume = 0.0

        for (i in 0 until setsJson.length()) {
            val sObj = setsJson.getJSONObject(i)
            val exerciseId = sObj.optString("exerciseId", "")
            val dbExercise = db.exerciseDao().findExerciseById(exerciseId)
            val exerciseName = sObj.optString("exerciseName", dbExercise?.name ?: "Exercise")
            val primaryMuscle = dbExercise?.primaryMuscleGroup ?: MuscleGroup.CHEST.name
            val equipment = dbExercise?.equipment ?: EquipmentType.BARBELL.name
            val category = dbExercise?.activityCategory ?: WorkoutActivityCategory.STRENGTH_TRAINING.name

            val weight = sObj.optDouble("weightKg", 0.0)
            val reps = sObj.optInt("reps", 0)
            val isCompleted = sObj.optBoolean("isCompleted", true)
            if (isCompleted) volume += weight * reps

            val rpe = if (sObj.has("rpe") && !sObj.isNull("rpe")) sObj.optDouble("rpe") else null
            val completedAt = if (sObj.has("completedAtEpochMs")) sObj.optLong("completedAtEpochMs") else null
            // An imported session carries its own timing; without these an import would land
            // with the set timing the live workout engine fills in, which is nothing at all.
            val startedAt = if (sObj.has("startedAtEpochMs")) sObj.optLong("startedAtEpochMs") else null
            val actualRest = if (sObj.has("actualRestSeconds")) sObj.optInt("actualRestSeconds") else null

            list.add(
                WorkoutSetEntity(
                    id = UUID.randomUUID().toString(),
                    sessionId = sessionId,
                    exerciseId = exerciseId,
                    exerciseName = exerciseName,
                    primaryMuscle = primaryMuscle,
                    equipment = equipment,
                    activityCategory = category,
                    orderIndex = sObj.optInt("orderIndex", i),
                    setIndex = sObj.optInt("setIndex", 1),
                    type = sObj.optString("type", RoutineSetType.NORMAL.name).uppercase(Locale.ROOT),
                    weightKg = weight,
                    weightUnit = WeightUnit.KG.name,
                    reps = reps,
                    rpe = rpe,
                    isCompleted = isCompleted,
                    side = sObj.optString("side", UnilateralSide.NONE.name).uppercase(Locale.ROOT),
                    restTimeSeconds = sObj.optInt("restTimeSeconds", 90),
                    completedAtEpochMs = completedAt,
                    startedAtEpochMs = startedAt,
                    actualRestSeconds = actualRest
                )
            )
        }
        return list to volume
    }

    fun parseHeartRateEntities(
        sessionId: String,
        hrJson: JSONArray,
        defaultTimestamp: Long
    ): Pair<List<WorkoutHeartRateSampleEntity>, Long> {
        val list = mutableListOf<WorkoutHeartRateSampleEntity>()
        var sumBpm = 0L
        for (h in 0 until hrJson.length()) {
            val hObj = hrJson.getJSONObject(h)
            val bpm = hObj.optInt("bpm", 0)
            val ts = hObj.optLong("timestampEpochMs", defaultTimestamp)
            if (bpm > 0) {
                list.add(WorkoutHeartRateSampleEntity(sessionId = sessionId, timestampEpochMs = ts, bpm = bpm))
                sumBpm += bpm
            }
        }
        return list to sumBpm
    }

    fun toWorkoutSummaryJson(sws: WorkoutSessionWithSets, hrCount: Int): JSONObject {
        val s = sws.session
        val ended = s.endedAtEpochMs
        val durationMin = if (ended != null && ended > s.startedAtEpochMs) {
            (ended - s.startedAtEpochMs) / 60_000L
        } else 0L

        return JSONObject().apply {
            put("id", s.id)
            put("title", s.title)
            put("status", s.status)
            put("startedAtEpochMs", s.startedAtEpochMs)
            if (ended != null) put("endedAtEpochMs", ended)
            put("durationMinutes", durationMin)
            put("totalVolumeKg", s.totalVolumeKg)
            put("setCount", sws.sets.size)
            if (s.averageHeartRateBpm != null) put("averageHeartRateBpm", s.averageHeartRateBpm)
            put("heartRateSampleCount", hrCount)
        }
    }

    fun toWorkoutDetailJson(
        sws: WorkoutSessionWithSets,
        hrSamples: List<WorkoutHeartRateSampleEntity>,
        includeSamples: Boolean
    ): JSONObject {
        val s = sws.session
        val domain = sws.toDomain()

        val cardio = JSONObject().apply {
            put("sampleCount", hrSamples.size)
            if (hrSamples.isNotEmpty()) {
                put("minBpm", hrSamples.minOf { it.bpm })
                put("maxBpm", hrSamples.maxOf { it.bpm })
                put("avgBpm", hrSamples.map { it.bpm }.average().toInt())
                if (includeSamples) {
                    val arr = JSONArray()
                    hrSamples.forEach {
                        arr.put(JSONObject().apply {
                            put("timestampEpochMs", it.timestampEpochMs)
                            put("bpm", it.bpm)
                        })
                    }
                    put("samples", arr)
                }
            }
        }

        val setsArr = JSONArray()
        domain.sets.forEach { setItem ->
            setsArr.put(JSONObject().apply {
                put("id", setItem.id)
                put("exerciseId", setItem.exerciseId)
                put("exerciseName", setItem.exerciseName)
                put("setIndex", setItem.setIndex)
                put("orderIndex", setItem.orderIndex)
                put("type", setItem.type.name)
                put("weightKg", setItem.weightKg)
                put("reps", setItem.reps)
                if (setItem.rpe != null) put("rpe", setItem.rpe)
                put("isCompleted", setItem.isCompleted)
                put("side", setItem.side.name)
                if (setItem.completedAtEpochMs != null) put("completedAtEpochMs", setItem.completedAtEpochMs)
                // Set timing is what makes a session readable as a chronology rather than a list.
                if (setItem.startedAtEpochMs != null) put("startedAtEpochMs", setItem.startedAtEpochMs)
                if (setItem.actualRestSeconds != null) put("actualRestSeconds", setItem.actualRestSeconds)
                put("restTimeSeconds", setItem.restTimeSeconds)
            })
        }

        return JSONObject().apply {
            put("id", s.id)
            put("title", s.title)
            put("notes", s.notes)
            put("status", s.status)
            put("startedAtEpochMs", s.startedAtEpochMs)
            s.endedAtEpochMs?.let { put("endedAtEpochMs", it) }
            put("totalVolumeKg", s.totalVolumeKg)
            if (s.averageHeartRateBpm != null) put("averageHeartRateBpm", s.averageHeartRateBpm)
            if (s.activeCaloriesKcal != null) put("activeCaloriesKcal", s.activeCaloriesKcal)
            put("sets", setsArr)
            put("cardio", cardio)
        }
    }
}
