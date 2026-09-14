package app.bodyforger.core.database

import app.bodyforger.core.database.dao.WorkoutDao
import app.bodyforger.core.database.entity.WorkoutSessionEntity
import app.bodyforger.core.database.entity.WorkoutSetEntity
import app.bodyforger.core.model.WorkoutSessionStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WorkoutDaoHealthConnectTest : DatabaseTestBase() {

    private lateinit var workoutDao: WorkoutDao

    @Before
    override fun setUp() {
        super.setUp()
        workoutDao = database.workoutDao()
    }

    @Test
    fun getUnexportedCompletedSessions_returnsOnlyCompletedUnexported() = runTest {
        val completedUnexported1 = WorkoutSessionEntity(
            id = "sess_completed_unexported_1",
            title = "Completed Session 1",
            status = WorkoutSessionStatus.COMPLETED.name,
            startedAtEpochMs = 1000L,
            isFinalized = true,
            isHealthConnectExported = false
        )
        val completedUnexported2 = WorkoutSessionEntity(
            id = "sess_completed_unexported_2",
            title = "Completed Session 2",
            status = WorkoutSessionStatus.COMPLETED.name,
            startedAtEpochMs = 2000L,
            isFinalized = true,
            isHealthConnectExported = false
        )
        val activeUnexported = WorkoutSessionEntity(
            id = "sess_active_unexported",
            title = "Active Session",
            status = WorkoutSessionStatus.ACTIVE.name,
            startedAtEpochMs = 3000L,
            isFinalized = false,
            isHealthConnectExported = false
        )
        val completedExported = WorkoutSessionEntity(
            id = "sess_completed_exported",
            title = "Exported Session",
            status = WorkoutSessionStatus.COMPLETED.name,
            startedAtEpochMs = 4000L,
            isFinalized = true,
            isHealthConnectExported = true
        )

        workoutDao.insertSession(completedUnexported1)
        workoutDao.insertSession(completedUnexported2)
        workoutDao.insertSession(activeUnexported)
        workoutDao.insertSession(completedExported)

        val unexported = workoutDao.getUnexportedCompletedSessions()
        assertEquals(2, unexported.size)
        val unexportedIds = unexported.map { it.session.id }.toSet()
        assertTrue(unexportedIds.contains("sess_completed_unexported_1"))
        assertTrue(unexportedIds.contains("sess_completed_unexported_2"))
    }

    @Test
    fun setHealthConnectExported_updatesFlag() = runTest {
        val session = WorkoutSessionEntity(
            id = "sess_export_toggle",
            title = "Export Toggle Session",
            status = WorkoutSessionStatus.COMPLETED.name,
            startedAtEpochMs = 1000L,
            isHealthConnectExported = false
        )
        workoutDao.insertSession(session)

        val before = workoutDao.getUnexportedCompletedSessions()
        assertEquals(1, before.size)

        workoutDao.setHealthConnectExported("sess_export_toggle", true)

        val after = workoutDao.getUnexportedCompletedSessions()
        assertTrue(after.isEmpty())

        val fetched = workoutDao.getSessionWithSets("sess_export_toggle")?.session
        assertNotNull(fetched)
        assertTrue(fetched!!.isHealthConnectExported)
    }

    @Test
    fun atomicCompleteSet_persistsStartedAtAndActualRestSeconds() = runTest {
        val sessionId = "sess_timing_test"
        val session = WorkoutSessionEntity(
            id = sessionId,
            title = "Timing Session",
            status = WorkoutSessionStatus.ACTIVE.name,
            startedAtEpochMs = 1000L
        )
        workoutDao.insertSession(session)

        val set = WorkoutSetEntity(
            id = "set_timing_1",
            sessionId = sessionId,
            exerciseId = "bench_press",
            exerciseName = "Bench Press",
            setIndex = 1,
            weightKg = 80.0,
            reps = 10,
            isCompleted = false
        )
        workoutDao.insertSets(listOf(set))

        val startedAt = 1750000000000L
        val completedAt = 1750000045000L
        val actualRest = 90

        workoutDao.atomicCompleteSet(
            setId = set.id,
            isCompleted = true,
            completedAtEpochMs = completedAt,
            weightKg = 80.0,
            reps = 10,
            rpe = 8.0,
            startedAtEpochMs = startedAt,
            actualRestSeconds = actualRest
        )

        val updatedSet = workoutDao.getSetById(set.id)
        assertNotNull(updatedSet)
        assertTrue(updatedSet!!.isCompleted)
        assertEquals(startedAt, updatedSet.startedAtEpochMs)
        assertEquals(completedAt, updatedSet.completedAtEpochMs)
        assertEquals(actualRest, updatedSet.actualRestSeconds)
    }
}
