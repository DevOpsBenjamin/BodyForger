package app.bodyforger.core.database

import app.bodyforger.core.database.dao.WorkoutDao
import app.bodyforger.core.database.entity.WorkoutHeartRateSampleEntity
import app.bodyforger.core.database.entity.WorkoutSessionEntity
import app.bodyforger.core.database.entity.WorkoutSetEntity
import app.bodyforger.core.model.WorkoutSessionStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WorkoutDaoTest : DatabaseTestBase() {

    private lateinit var workoutDao: WorkoutDao

    @Before
    override fun setUp() {
        super.setUp()
        workoutDao = database.workoutDao()
    }

    @Test
    fun getLastPerformance_returnsLastCompletedSessionSetsOnly() = runTest {
        val exerciseId = "bench_press"

        val oldCompletedSession = WorkoutSessionEntity(
            id = "session_old",
            title = "Old Push",
            status = WorkoutSessionStatus.COMPLETED.name,
            startedAtEpochMs = 1000L,
            endedAtEpochMs = 2000L
        )
        val recentCompletedSession = WorkoutSessionEntity(
            id = "session_recent",
            title = "Recent Push",
            status = WorkoutSessionStatus.COMPLETED.name,
            startedAtEpochMs = 3000L,
            endedAtEpochMs = 4000L
        )
        val activeSession = WorkoutSessionEntity(
            id = "session_active",
            title = "Active Push",
            status = WorkoutSessionStatus.ACTIVE.name,
            startedAtEpochMs = 5000L
        )
        val currentSessionId = "session_current"
        val currentSession = WorkoutSessionEntity(
            id = currentSessionId,
            title = "Current Push",
            status = WorkoutSessionStatus.ACTIVE.name,
            startedAtEpochMs = 6000L
        )

        workoutDao.insertSession(oldCompletedSession)
        workoutDao.insertSession(recentCompletedSession)
        workoutDao.insertSession(activeSession)
        workoutDao.insertSession(currentSession)

        workoutDao.insertSets(
            listOf(
                WorkoutSetEntity(
                    id = "set_old_1",
                    sessionId = oldCompletedSession.id,
                    exerciseId = exerciseId,
                    exerciseName = "Bench Press",
                    setIndex = 1,
                    weightKg = 80.0,
                    reps = 10,
                    isCompleted = true
                ),
                WorkoutSetEntity(
                    id = "set_recent_1",
                    sessionId = recentCompletedSession.id,
                    exerciseId = exerciseId,
                    exerciseName = "Bench Press",
                    setIndex = 1,
                    weightKg = 85.0,
                    reps = 8,
                    isCompleted = true
                ),
                WorkoutSetEntity(
                    id = "set_recent_2",
                    sessionId = recentCompletedSession.id,
                    exerciseId = exerciseId,
                    exerciseName = "Bench Press",
                    setIndex = 2,
                    weightKg = 85.0,
                    reps = 6,
                    isCompleted = true
                ),
                WorkoutSetEntity(
                    id = "set_active_1",
                    sessionId = activeSession.id,
                    exerciseId = exerciseId,
                    exerciseName = "Bench Press",
                    setIndex = 1,
                    weightKg = 90.0,
                    reps = 5,
                    isCompleted = true
                )
            )
        )

        val lastPerformance = workoutDao.getLastPerformance(exerciseId, currentSessionId)

        assertEquals(2, lastPerformance.size)
        assertEquals("set_recent_1", lastPerformance[0].id)
        assertEquals(85.0, lastPerformance[0].weightKg, 0.0)
        assertEquals("set_recent_2", lastPerformance[1].id)
        assertEquals(85.0, lastPerformance[1].weightKg, 0.0)
    }

    @Test
    fun getLastPerformance_ignoresCompletedSessionWithNoCompletedSets() = runTest {
        val exerciseId = "squat"

        val validPastSession = WorkoutSessionEntity(
            id = "session_valid",
            title = "Legs Valid",
            status = WorkoutSessionStatus.COMPLETED.name,
            startedAtEpochMs = 1000L,
            endedAtEpochMs = 2000L
        )
        val skippedPastSession = WorkoutSessionEntity(
            id = "session_skipped",
            title = "Legs Skipped",
            status = WorkoutSessionStatus.COMPLETED.name,
            startedAtEpochMs = 3000L,
            endedAtEpochMs = 4000L
        )

        workoutDao.insertSession(validPastSession)
        workoutDao.insertSession(skippedPastSession)

        workoutDao.insertSets(
            listOf(
                WorkoutSetEntity(
                    id = "set_valid",
                    sessionId = validPastSession.id,
                    exerciseId = exerciseId,
                    exerciseName = "Squat",
                    setIndex = 1,
                    weightKg = 100.0,
                    reps = 5,
                    isCompleted = true
                ),
                WorkoutSetEntity(
                    id = "set_uncompleted",
                    sessionId = skippedPastSession.id,
                    exerciseId = exerciseId,
                    exerciseName = "Squat",
                    setIndex = 1,
                    weightKg = 110.0,
                    reps = 0,
                    isCompleted = false
                )
            )
        )

        val lastPerformance = workoutDao.getLastPerformance(exerciseId, "session_today")

        assertEquals(1, lastPerformance.size)
        assertEquals("set_valid", lastPerformance[0].id)
        assertEquals(100.0, lastPerformance[0].weightKg, 0.0)
    }

    @Test
    fun atomicCompleteSet_updatesSetAndRecalculatesSessionVolume() = runTest {
        val sessionId = "session_volume_test"
        val session = WorkoutSessionEntity(
            id = sessionId,
            title = "Volume Workout",
            status = WorkoutSessionStatus.ACTIVE.name,
            startedAtEpochMs = 1000L,
            totalVolumeKg = 0.0
        )
        workoutDao.insertSession(session)

        val set1 = WorkoutSetEntity(
            id = "set_v1",
            sessionId = sessionId,
            exerciseId = "deadlift",
            exerciseName = "Deadlift",
            setIndex = 1,
            weightKg = 140.0,
            reps = 5,
            isCompleted = false
        )
        val set2 = WorkoutSetEntity(
            id = "set_v2",
            sessionId = sessionId,
            exerciseId = "deadlift",
            exerciseName = "Deadlift",
            setIndex = 2,
            weightKg = 150.0,
            reps = 3,
            isCompleted = false
        )
        workoutDao.insertSets(listOf(set1, set2))

        workoutDao.atomicCompleteSet(
            setId = set1.id,
            isCompleted = true,
            completedAtEpochMs = 1500L,
            weightKg = 140.0,
            reps = 5,
            rpe = 8.0
        )

        val afterSet1 = workoutDao.getSetById(set1.id)
        assertNotNull(afterSet1)
        assertTrue(afterSet1!!.isCompleted)
        assertEquals(1500L, afterSet1.completedAtEpochMs)
        assertEquals(8.0, afterSet1.rpe)

        val sessionAfter1 = workoutDao.getSessionWithSets(sessionId)?.session
        assertNotNull(sessionAfter1)
        assertEquals(700.0, sessionAfter1!!.totalVolumeKg, 0.001)

        workoutDao.atomicCompleteSet(
            setId = set2.id,
            isCompleted = true,
            completedAtEpochMs = 1800L,
            weightKg = 150.0,
            reps = 3,
            rpe = 9.0
        )

        val sessionAfter2 = workoutDao.getSessionWithSets(sessionId)?.session
        assertNotNull(sessionAfter2)
        assertEquals(1150.0, sessionAfter2!!.totalVolumeKg, 0.001)
    }

    @Test
    fun deleteSession_cascadesToSetsAndHeartRateSamples() = runTest {
        val sessionId = "session_to_delete"
        val session = WorkoutSessionEntity(
            id = sessionId,
            title = "Delete Me",
            status = WorkoutSessionStatus.ACTIVE.name,
            startedAtEpochMs = 1000L
        )
        workoutDao.insertSession(session)

        workoutDao.insertSets(
            listOf(
                WorkoutSetEntity(
                    id = "set_del_1",
                    sessionId = sessionId,
                    exerciseId = "bench",
                    exerciseName = "Bench",
                    setIndex = 1
                ),
                WorkoutSetEntity(
                    id = "set_del_2",
                    sessionId = sessionId,
                    exerciseId = "bench",
                    exerciseName = "Bench",
                    setIndex = 2
                )
            )
        )

        workoutDao.insertHeartRateSamples(
            listOf(
                WorkoutHeartRateSampleEntity(
                    sessionId = sessionId,
                    timestampEpochMs = 1100L,
                    bpm = 135
                ),
                WorkoutHeartRateSampleEntity(
                    sessionId = sessionId,
                    timestampEpochMs = 1200L,
                    bpm = 142
                )
            )
        )

        assertEquals(2, workoutDao.getSetsForSession(sessionId).size)
        assertEquals(2, workoutDao.getHeartRateSampleCountForSession(sessionId))

        workoutDao.deleteSession(sessionId)

        assertNull(workoutDao.getSessionWithSets(sessionId))
        assertTrue(workoutDao.getSetsForSession(sessionId).isEmpty())
        assertEquals(0, workoutDao.getHeartRateSampleCountForSession(sessionId))
    }
}
