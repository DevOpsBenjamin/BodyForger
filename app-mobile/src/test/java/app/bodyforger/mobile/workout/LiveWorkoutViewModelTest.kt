package app.bodyforger.mobile.workout

import app.bodyforger.core.database.dao.WorkoutDao
import app.bodyforger.core.database.entity.WorkoutSessionEntity
import app.bodyforger.core.database.entity.WorkoutSessionWithSets
import app.bodyforger.core.database.entity.WorkoutSetEntity
import app.bodyforger.core.model.EquipmentType
import app.bodyforger.core.model.MuscleGroup
import app.bodyforger.core.model.Routine
import app.bodyforger.core.model.RoutineExercise
import app.bodyforger.core.model.RoutineSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LiveWorkoutViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class RecordingHaptics : WorkoutHaptics {
        var completedCount = 0
        var warningCount = 0
        var finishedCount = 0

        override fun setCompleted() { completedCount++ }
        override fun restWarning() { warningCount++ }
        override fun restFinished() { finishedCount++ }
    }

    private class FakeWorkoutDao : WorkoutDao {
        val sessions = mutableListOf<WorkoutSessionEntity>()
        val sets = mutableListOf<WorkoutSetEntity>()

        override fun getAllSessionsWithSets(): Flow<List<WorkoutSessionWithSets>> = emptyFlow()
        override fun getCompletedSessions(): Flow<List<WorkoutSessionWithSets>> = emptyFlow()
        override suspend fun getSessionWithSets(sessionId: String): WorkoutSessionWithSets? = null
        override suspend fun getActiveSession(): WorkoutSessionWithSets? = null
        override fun observeActiveSession(): Flow<WorkoutSessionWithSets?> = emptyFlow()
        override suspend fun insertSession(session: WorkoutSessionEntity) { sessions.add(session) }
        override suspend fun updateSession(session: WorkoutSessionEntity) {}
        override suspend fun insertSet(set: WorkoutSetEntity) { sets.add(set) }
        override suspend fun insertSets(sets: List<WorkoutSetEntity>) { this.sets.addAll(sets) }
        override suspend fun updateSet(set: WorkoutSetEntity) {}
        override suspend fun deleteSet(setId: String) { sets.removeAll { it.id == setId } }
        override suspend fun deleteSession(sessionId: String) { sessions.removeAll { it.id == sessionId } }
        override suspend fun getSetById(setId: String): WorkoutSetEntity? = sets.firstOrNull { it.id == setId }
        override suspend fun getSetsForSession(sessionId: String): List<WorkoutSetEntity> = sets.filter { it.sessionId == sessionId }
        override suspend fun getLastPerformance(exerciseId: String, currentSessionId: String): List<WorkoutSetEntity> = emptyList()
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun routineWithExercise(restTimeSeconds: Int = 90) = Routine(
        id = "routine-1",
        name = "Push Day",
        exercises = listOf(
            RoutineExercise(
                exerciseId = "bench-press",
                exerciseName = "Bench Press",
                primaryMuscle = MuscleGroup.CHEST,
                equipment = EquipmentType.BARBELL,
                restTimeSeconds = restTimeSeconds,
                sets = listOf(
                    RoutineSet(setIndex = 1, targetWeightKg = 80.0, reps = 10),
                    RoutineSet(setIndex = 2, targetWeightKg = 80.0, reps = 8)
                )
            )
        )
    )

    @Test
    fun `validating a set triggers haptics and starts rest timer`() = runTest {
        val haptics = RecordingHaptics()
        val dao = FakeWorkoutDao()
        val viewModel = LiveWorkoutViewModel(workoutDao = dao, workoutHaptics = haptics)

        viewModel.begin(routine = routineWithExercise(restTimeSeconds = 60), freeSessionTitle = "Free")
        val activeWorkout = viewModel.active.value
        assertNotNull(activeWorkout)

        val firstSet = activeWorkout!!.sets.first()
        viewModel.toggleSetCompleted(firstSet.id)

        assertEquals(1, haptics.completedCount)
        val restState = viewModel.restTimer.value
        assertNotNull(restState)
        assertEquals(60, restState?.secondsRemaining)
        assertEquals(60, restState?.totalSeconds)
    }

    @Test
    fun `unvalidating a set stops active rest timer for that set`() = runTest {
        val haptics = RecordingHaptics()
        val dao = FakeWorkoutDao()
        val viewModel = LiveWorkoutViewModel(workoutDao = dao, workoutHaptics = haptics)

        viewModel.begin(routine = routineWithExercise(restTimeSeconds = 45), freeSessionTitle = "Free")
        val firstSet = viewModel.active.value!!.sets.first()

        viewModel.toggleSetCompleted(firstSet.id)
        assertNotNull(viewModel.restTimer.value)

        viewModel.toggleSetCompleted(firstSet.id)
        assertNull(viewModel.restTimer.value)
    }

    @Test
    fun `addRestSeconds and skipRest adjust rest timer state`() = runTest {
        val haptics = RecordingHaptics()
        val dao = FakeWorkoutDao()
        val viewModel = LiveWorkoutViewModel(workoutDao = dao, workoutHaptics = haptics)

        viewModel.begin(routine = routineWithExercise(restTimeSeconds = 60), freeSessionTitle = "Free")
        val firstSet = viewModel.active.value!!.sets.first()
        viewModel.toggleSetCompleted(firstSet.id)

        viewModel.addRestSeconds(30)
        assertEquals(90, viewModel.restTimer.value?.secondsRemaining)

        viewModel.skipRest()
        assertNull(viewModel.restTimer.value)
    }

    @Test
    fun `finishing or deleting workout clears active workout and stops rest timer`() = runTest {
        val haptics = RecordingHaptics()
        val dao = FakeWorkoutDao()
        val viewModel = LiveWorkoutViewModel(workoutDao = dao, workoutHaptics = haptics)

        viewModel.begin(routine = routineWithExercise(), freeSessionTitle = "Free")
        val firstSet = viewModel.active.value!!.sets.first()
        viewModel.toggleSetCompleted(firstSet.id)

        assertNotNull(viewModel.restTimer.value)

        viewModel.finish()
        assertNull(viewModel.active.value)
        assertNull(viewModel.restTimer.value)
    }
}
