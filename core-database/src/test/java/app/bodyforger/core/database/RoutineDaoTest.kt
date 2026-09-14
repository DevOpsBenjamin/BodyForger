package app.bodyforger.core.database

import app.bodyforger.core.database.dao.RoutineDao
import app.bodyforger.core.database.entity.RoutineEntity
import app.bodyforger.core.database.entity.RoutineExerciseEntity
import app.bodyforger.core.database.entity.RoutineSetEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RoutineDaoTest : DatabaseTestBase() {

    private lateinit var routineDao: RoutineDao

    @Before
    override fun setUp() {
        super.setUp()
        routineDao = database.routineDao()
    }

    @Test
    fun saveFullRoutine_replacesExercisesAndSetsWithoutOrphans() = runTest {
        val routineId = "routine_push"
        val routine = RoutineEntity(id = routineId, name = "Push Day A")
        val initialExercise = RoutineExerciseEntity(
            id = "re_initial",
            routineId = routineId,
            exerciseId = "bench_press",
            exerciseName = "Bench Press",
            primaryMuscle = "CHEST",
            equipment = "BARBELL"
        )
        val initialSet1 = RoutineSetEntity(
            id = "rs_initial_1",
            routineExerciseId = initialExercise.id,
            setIndex = 1,
            targetWeightKg = 80.0,
            reps = 10
        )
        val initialSet2 = RoutineSetEntity(
            id = "rs_initial_2",
            routineExerciseId = initialExercise.id,
            setIndex = 2,
            targetWeightKg = 80.0,
            reps = 8
        )

        routineDao.saveFullRoutine(
            routine = routine,
            exercises = listOf(initialExercise),
            sets = listOf(initialSet1, initialSet2)
        )

        val retrievedInitial = routineDao.getRoutineWithExercisesById(routineId)
        assertNotNull(retrievedInitial)
        assertEquals(1, retrievedInitial!!.exercises.size)
        assertEquals(2, retrievedInitial.exercises[0].sets.size)

        val updatedRoutine = routine.copy(name = "Push Day A (Updated)")
        val newExercise1 = RoutineExerciseEntity(
            id = "re_new_1",
            routineId = routineId,
            exerciseId = "incline_press",
            exerciseName = "Incline Press",
            primaryMuscle = "CHEST",
            equipment = "DUMBBELL"
        )
        val newExercise2 = RoutineExerciseEntity(
            id = "re_new_2",
            routineId = routineId,
            exerciseId = "lateral_raise",
            exerciseName = "Lateral Raise",
            primaryMuscle = "SHOULDERS",
            equipment = "DUMBBELL"
        )
        val newSet1 = RoutineSetEntity(
            id = "rs_new_1",
            routineExerciseId = newExercise1.id,
            setIndex = 1,
            targetWeightKg = 30.0,
            reps = 12
        )

        routineDao.saveFullRoutine(
            routine = updatedRoutine,
            exercises = listOf(newExercise1, newExercise2),
            sets = listOf(newSet1)
        )

        val retrievedUpdated = routineDao.getRoutineWithExercisesById(routineId)
        assertNotNull(retrievedUpdated)
        assertEquals("Push Day A (Updated)", retrievedUpdated!!.routine.name)
        assertEquals(2, retrievedUpdated.exercises.size)

        val exerciseIds = retrievedUpdated.exercises.map { it.exercise.id }.toSet()
        assertEquals(setOf("re_new_1", "re_new_2"), exerciseIds)
        assertTrue(exerciseIds.none { it == "re_initial" })

        val inclineEx = retrievedUpdated.exercises.first { it.exercise.id == "re_new_1" }
        assertEquals(1, inclineEx.sets.size)
        assertEquals("rs_new_1", inclineEx.sets[0].id)

        val lateralEx = retrievedUpdated.exercises.first { it.exercise.id == "re_new_2" }
        assertTrue(lateralEx.sets.isEmpty())
    }

    @Test
    fun duplicateRoutine_regeneratesIdsInCascade() = runTest {
        val originalRoutineId = "routine_pull"
        val originalRoutine = RoutineEntity(id = originalRoutineId, name = "Pull Day")
        val originalExercise = RoutineExerciseEntity(
            id = "re_pull_1",
            routineId = originalRoutineId,
            exerciseId = "barbell_row",
            exerciseName = "Barbell Row",
            primaryMuscle = "BACK",
            equipment = "BARBELL"
        )
        val originalSet = RoutineSetEntity(
            id = "rs_pull_1",
            routineExerciseId = originalExercise.id,
            setIndex = 1,
            targetWeightKg = 70.0,
            reps = 10
        )

        routineDao.saveFullRoutine(
            routine = originalRoutine,
            exercises = listOf(originalExercise),
            sets = listOf(originalSet)
        )

        val duplicatedId = routineDao.duplicateRoutine(originalRoutineId, "Pull Day Clone")
        assertNotNull(duplicatedId)
        assertNotEquals(originalRoutineId, duplicatedId)

        val originalAfter = routineDao.getRoutineWithExercisesById(originalRoutineId)
        assertNotNull(originalAfter)
        assertEquals("Pull Day", originalAfter!!.routine.name)

        val duplicated = routineDao.getRoutineWithExercisesById(duplicatedId!!)
        assertNotNull(duplicated)
        assertEquals("Pull Day Clone", duplicated!!.routine.name)
        assertEquals(1, duplicated.exercises.size)

        val duplicatedExercise = duplicated.exercises[0].exercise
        assertNotEquals(originalExercise.id, duplicatedExercise.id)
        assertEquals(duplicatedId, duplicatedExercise.routineId)
        assertEquals("barbell_row", duplicatedExercise.exerciseId)

        assertEquals(1, duplicated.exercises[0].sets.size)
        val duplicatedSet = duplicated.exercises[0].sets[0]
        assertNotEquals(originalSet.id, duplicatedSet.id)
        assertEquals(duplicatedExercise.id, duplicatedSet.routineExerciseId)
        assertEquals(70.0, duplicatedSet.targetWeightKg!!, 0.0)
    }
}
