package app.bodyforger.mobile.mcp

import app.bodyforger.core.database.entity.WorkoutHeartRateSampleEntity
import app.bodyforger.core.database.entity.WorkoutSessionEntity
import app.bodyforger.core.database.entity.WorkoutSessionWithSets
import app.bodyforger.core.database.entity.WorkoutSetEntity
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutMcpMapperTest {

    @Test
    fun parseHeartRateEntitiesComputesSumAndListCorrectly() {
        val hrJson = JSONArray().apply {
            put(JSONObject().apply {
                put("timestampEpochMs", 1000L)
                put("bpm", 120)
            })
            put(JSONObject().apply {
                put("timestampEpochMs", 2000L)
                put("bpm", 140)
            })
            put(JSONObject().apply {
                put("timestampEpochMs", 3000L)
                put("bpm", 0) // Should be ignored
            })
        }

        val (entities, sumBpm) = WorkoutMcpMapper.parseHeartRateEntities("sess_1", hrJson, defaultTimestamp = 500L)
        assertEquals(2, entities.size)
        assertEquals(260L, sumBpm)
        assertEquals("sess_1", entities[0].sessionId)
        assertEquals(1000L, entities[0].timestampEpochMs)
        assertEquals(120, entities[0].bpm)
        assertEquals(140, entities[1].bpm)
    }

    @Test
    fun toWorkoutSummaryJsonFormatsFieldsCorrectly() {
        val session = WorkoutSessionEntity(
            id = "s_1",
            routineId = "r_1",
            title = "Push Day",
            startedAtEpochMs = 1000L,
            endedAtEpochMs = 1000L + 3600_000L,
            averageHeartRateBpm = 135,
            totalVolumeKg = 5400.0
        )
        val sws = WorkoutSessionWithSets(session = session, sets = emptyList())
        val json = WorkoutMcpMapper.toWorkoutSummaryJson(sws, hrCount = 120)

        assertEquals("s_1", json.getString("id"))
        assertEquals("Push Day", json.getString("title"))
        assertEquals(60L, json.getLong("durationMinutes"))
        assertEquals(5400.0, json.getDouble("totalVolumeKg"), 0.01)
        assertEquals(135, json.getInt("averageHeartRateBpm"))
        assertEquals(120, json.getInt("heartRateSampleCount"))
    }

    @Test
    fun toWorkoutDetailJsonComputesCardioStatsAndRespectsSamplesFlag() {
        val session = WorkoutSessionEntity(
            id = "s_2",
            title = "Legs Hypertrophy",
            startedAtEpochMs = 2000L,
            totalVolumeKg = 8000.0
        )
        val sets = listOf(
            WorkoutSetEntity(
                id = "set_1",
                sessionId = "s_2",
                exerciseId = "squat",
                exerciseName = "Barbell Back Squat",
                setIndex = 1,
                weightKg = 100.0,
                reps = 10,
                isCompleted = true
            )
        )
        val sws = WorkoutSessionWithSets(session = session, sets = sets)
        val hrSamples = listOf(
            WorkoutHeartRateSampleEntity(sessionId = "s_2", timestampEpochMs = 2100L, bpm = 110),
            WorkoutHeartRateSampleEntity(sessionId = "s_2", timestampEpochMs = 2200L, bpm = 150),
            WorkoutHeartRateSampleEntity(sessionId = "s_2", timestampEpochMs = 2300L, bpm = 130)
        )

        // Without raw samples
        val jsonWithout = WorkoutMcpMapper.toWorkoutDetailJson(sws, hrSamples, includeSamples = false)
        val cardioWithout = jsonWithout.getJSONObject("cardio")
        assertEquals(3, cardioWithout.getInt("sampleCount"))
        assertEquals(110, cardioWithout.getInt("minBpm"))
        assertEquals(150, cardioWithout.getInt("maxBpm"))
        assertEquals(130, cardioWithout.getInt("avgBpm"))
        assertFalse(cardioWithout.has("samples"))

        // With raw samples
        val jsonWith = WorkoutMcpMapper.toWorkoutDetailJson(sws, hrSamples, includeSamples = true)
        val cardioWith = jsonWith.getJSONObject("cardio")
        assertTrue(cardioWith.has("samples"))
        assertEquals(3, cardioWith.getJSONArray("samples").length())
    }

    @Test
    fun allRegisteredMcpToolsHaveValidSchemas() = kotlinx.coroutines.runBlocking {
        val registry = McpToolRegistry()
        val tools = registry.listToolsJson()
        assertEquals(17, tools.length())

        for (i in 0 until tools.length()) {
            val tool = tools.getJSONObject(i)
            assertTrue(tool.has("name"))
            assertTrue(tool.getString("name").isNotEmpty())
            assertTrue(tool.has("description"))
            assertTrue(tool.has("inputSchema"))
            assertEquals("object", tool.getJSONObject("inputSchema").getString("type"))
        }
    }
}
