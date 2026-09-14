package app.bodyforger.core.database

import app.bodyforger.core.database.dao.WorkoutDao
import app.bodyforger.core.database.entity.WorkoutSessionEntity
import app.bodyforger.core.database.entity.WorkoutSetEntity
import app.bodyforger.core.model.WorkoutSessionStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * The history list reads sessions without their sets, so the exercise names it shows are built
 * by the query rather than by walking a training history in memory.
 */
class WorkoutSessionSummaryTest : DatabaseTestBase() {

    private lateinit var workoutDao: WorkoutDao

    @Before
    override fun setUp() {
        super.setUp()
        workoutDao = database.workoutDao()
    }

    private fun session(id: String, startedAt: Long, status: WorkoutSessionStatus = WorkoutSessionStatus.COMPLETED) =
        WorkoutSessionEntity(
            id = id,
            title = "Session $id",
            status = status.name,
            startedAtEpochMs = startedAt,
            endedAtEpochMs = startedAt + 3_600_000,
            totalVolumeKg = 1000.0
        )

    private fun set(id: String, sessionId: String, exercise: String, order: Int) = WorkoutSetEntity(
        id = id,
        sessionId = sessionId,
        exerciseId = exercise.lowercase(),
        exerciseName = exercise,
        orderIndex = order,
        isCompleted = true
    )

    @Test
    fun exerciseNamesFollowThePerformedOrderWithoutRepeats() = runTest {
        workoutDao.insertSession(session("s1", 1_000_000))
        workoutDao.insertSets(
            listOf(
                set("a", "s1", "Elliptical", order = 0),
                set("b", "s1", "Pull-Up", order = 1),
                set("c", "s1", "Pull-Up", order = 1),
                set("d", "s1", "Leg Press", order = 2)
            )
        )

        val summary = workoutDao.getCompletedSessionSummaries().first().single()

        assertEquals("Elliptical, Pull-Up, Leg Press", summary.exerciseNames)
    }

    @Test
    fun aSessionWithoutSetsReportsNoExerciseRatherThanFailing() = runTest {
        workoutDao.insertSession(session("s1", 1_000_000))

        val summary = workoutDao.getCompletedSessionSummaries().first().single()

        assertNull(summary.exerciseNames)
        assertEquals(1000.0, summary.session.totalVolumeKg, 0.0)
    }

    @Test
    fun onlyCompletedSessionsAreListed_newestFirst() = runTest {
        workoutDao.insertSession(session("old", 1_000_000))
        workoutDao.insertSession(session("new", 2_000_000))
        workoutDao.insertSession(session("running", 3_000_000, WorkoutSessionStatus.ACTIVE))

        val summaries = workoutDao.getCompletedSessionSummaries().first()

        assertEquals(listOf("new", "old"), summaries.map { it.session.id })
    }
}
