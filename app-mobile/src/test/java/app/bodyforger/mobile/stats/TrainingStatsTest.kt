package app.bodyforger.mobile.stats

import app.bodyforger.core.model.WorkoutSession
import app.bodyforger.core.model.WorkoutSet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrainingStatsTest {

    private fun set(
        exercise: String,
        weightKg: Double,
        reps: Int,
        completed: Boolean = true,
        order: Int = 0
    ) = WorkoutSet(
        exerciseId = exercise.lowercase(),
        exerciseName = exercise,
        weightKg = weightKg,
        reps = reps,
        isCompleted = completed,
        orderIndex = order
    )

    private fun session(
        startedAtEpochMs: Long = 0,
        endedAtEpochMs: Long? = null,
        sets: List<WorkoutSet> = emptyList()
    ) = WorkoutSession(
        title = "séance",
        startedAtEpochMs = startedAtEpochMs,
        endedAtEpochMs = endedAtEpochMs,
        sets = sets
    )

    @Test
    fun `tonnage counts only the sets actually performed`() {
        val sessions = listOf(
            session(sets = listOf(set("Squat", 100.0, 5), set("Squat", 100.0, 5, completed = false)))
        )
        assertEquals(500.0, TrainingStats.totalTonnageKg(sessions), 0.0)
    }

    @Test
    fun `tonnage is reported in tonnes as well`() {
        val sessions = listOf(session(sets = listOf(set("Squat", 100.0, 50))))
        assertEquals(5.0, TrainingStats.totalTonnes(sessions), 0.0)
    }

    @Test
    fun `hours count only the sessions that were closed`() {
        val oneHour = 3_600_000L
        val sessions = listOf(
            session(startedAtEpochMs = 0, endedAtEpochMs = oneHour),
            session(startedAtEpochMs = 0, endedAtEpochMs = null)
        )
        assertEquals(1.0, TrainingStats.totalHours(sessions), 0.0001)
    }

    @Test
    fun `a session that never ended has no duration`() {
        assertEquals(null, TrainingStats.durationMinutes(session(endedAtEpochMs = null)))
    }

    @Test
    fun `tonnage over a window keeps the sessions started inside it`() {
        val sessions = listOf(
            session(startedAtEpochMs = 100, sets = listOf(set("Squat", 100.0, 1))),
            session(startedAtEpochMs = 900, sets = listOf(set("Squat", 200.0, 1)))
        )
        assertEquals(100.0, TrainingStats.tonnageBetween(sessions, 0, 500), 0.0)
    }

    @Test
    fun `the exercises of a session are listed once, in order`() {
        val performed = session(
            sets = listOf(
                set("Squat", 100.0, 5, order = 0),
                set("Squat", 100.0, 5, order = 0),
                set("Presse", 200.0, 8, order = 1)
            )
        )
        assertEquals(listOf("Squat", "Presse"), TrainingStats.exerciseNames(performed))
    }

    @Test
    fun `the record is the best estimate, not the heaviest load`() {
        // 100 kg x 5 estime a 116,7 ; 105 kg x 1 estime a 108,5.
        val sessions = listOf(session(sets = listOf(set("Squat", 100.0, 5), set("Squat", 105.0, 1))))
        val record = TrainingStats.personalRecords(sessions).single()

        assertEquals(100.0, record.bestWeightKg, 0.0)
        assertEquals(5, record.bestReps)
        assertEquals(116.67, record.estimatedOneRepMaxKg, 0.01)
    }

    @Test
    fun `records are listed heaviest first, one per exercise`() {
        val sessions = listOf(
            session(sets = listOf(set("Squat", 140.0, 1), set("Développé", 100.0, 1), set("Squat", 120.0, 1)))
        )
        val records = TrainingStats.personalRecords(sessions)
        assertEquals(listOf("Squat", "Développé"), records.map { it.exerciseName })
    }

    @Test
    fun `a set that was never performed sets no record`() {
        val sessions = listOf(session(sets = listOf(set("Squat", 200.0, 5, completed = false))))
        assertTrue(TrainingStats.personalRecords(sessions).isEmpty())
    }

    @Test
    fun `a bodyweight set sets no record, having no load to compare`() {
        val sessions = listOf(session(sets = listOf(set("Tractions", 0.0, 12))))
        assertTrue(TrainingStats.personalRecords(sessions).isEmpty())
    }

    @Test
    fun `the active days are counted back from today, oldest first`() {
        val day = 86_400_000L
        val today = 100 * day
        val sessions = listOf(
            session(startedAtEpochMs = today),
            session(startedAtEpochMs = today - 2 * day),
            // Hors fenêtre de sept jours : ignorée.
            session(startedAtEpochMs = today - 30 * day)
        )
        // La fenêtre couvre les index 0 (il y a six jours) à 6 (aujourd'hui).
        assertEquals(setOf(6, 4), TrainingStats.activeDayOffsets(sessions, today, days = 7))
    }

    @Test
    fun `two sessions on the same day count as one active day`() {
        val day = 86_400_000L
        val today = 100 * day
        val sessions = listOf(session(startedAtEpochMs = today), session(startedAtEpochMs = today + 3600_000))
        assertEquals(1, TrainingStats.sessionsThisWeek(sessions, today))
    }

    @Test
    fun `an empty history adds up to nothing rather than failing`() {
        assertEquals(0.0, TrainingStats.totalTonnageKg(emptyList()), 0.0)
        assertEquals(0.0, TrainingStats.totalHours(emptyList()), 0.0)
        assertTrue(TrainingStats.personalRecords(emptyList()).isEmpty())
    }
}
