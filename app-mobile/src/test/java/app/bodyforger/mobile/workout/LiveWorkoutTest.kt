package app.bodyforger.mobile.workout

import app.bodyforger.core.model.EquipmentType
import app.bodyforger.core.model.MuscleGroup
import app.bodyforger.core.model.RoutineExercise
import app.bodyforger.core.model.RoutineSet
import app.bodyforger.core.model.RoutineSetType
import app.bodyforger.core.model.UnilateralSide
import app.bodyforger.core.model.WeightUnit
import app.bodyforger.core.model.WorkoutSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LiveWorkoutTest {

    private fun exercise(
        name: String,
        unilateral: Boolean = false,
        sets: List<RoutineSet> = listOf(RoutineSet(setIndex = 1), RoutineSet(setIndex = 2))
    ) = RoutineExercise(
        exerciseId = name.lowercase(),
        exerciseName = name,
        primaryMuscle = MuscleGroup.CHEST,
        equipment = EquipmentType.BARBELL,
        isUnilateral = unilateral,
        sets = sets
    )

    private fun workoutOf(vararg exercises: RoutineExercise) =
        LiveWorkout.from(exercises.toList(), WorkoutSession(title = "séance"))

    @Test
    fun `a bilateral exercise yields one set per planned set`() {
        val workout = workoutOf(exercise("Développé"))
        assertEquals(2, workout.sets.size)
        assertTrue(workout.sets.all { it.side == UnilateralSide.NONE })
    }

    @Test
    fun `a unilateral exercise yields both sides under one set index`() {
        val workout = workoutOf(exercise("Fentes", unilateral = true))
        assertEquals(4, workout.sets.size)
        val firstSet = workout.sets.filter { it.setIndex == 1 }
        assertEquals(listOf(UnilateralSide.LEFT, UnilateralSide.RIGHT), firstSet.map { it.side })
    }

    @Test
    fun `a planned target load is carried into the set`() {
        val planned = listOf(RoutineSet(setIndex = 1, targetWeightKg = 60.0, reps = 8))
        val workout = workoutOf(exercise("Squat", sets = planned))
        assertEquals(60.0, workout.sets.single().weightKg, 0.0)
        assertEquals(8, workout.sets.single().reps)
    }

    @Test
    fun `removing an exercise renumbers the sets of the ones that follow`() {
        val workout = workoutOf(exercise("Premier"), exercise("Deuxième"), exercise("Troisième"))
            .removeExercise(0)

        assertEquals(listOf("Deuxième", "Troisième"), workout.exercises.map { it.exerciseName })
        assertEquals(listOf(0, 1), workout.exercises.map { it.orderIndex })
        // Sans renumérotation, les séries du troisième pointeraient au-delà de la liste.
        assertEquals(
            listOf("Deuxième", "Deuxième", "Troisième", "Troisième"),
            workout.sets.sortedBy { it.orderIndex }.map { workout.exerciseOf(it)!!.exerciseName }
        )
    }

    @Test
    fun `removing an exercise drops its own sets`() {
        val workout = workoutOf(exercise("Premier"), exercise("Deuxième")).removeExercise(0)
        assertTrue(workout.sets.none { it.exerciseName == "Premier" })
    }

    @Test
    fun `an added exercise lands at the end with its own sets`() {
        val workout = workoutOf(exercise("Premier")).addExercise(exercise("Ajouté"))
        assertEquals(1, workout.exercises.last().orderIndex)
        assertEquals(2, workout.setsOf(1).size)
    }

    @Test
    fun `replacing an exercise drops the loads of the movement it replaces`() {
        val workout = workoutOf(exercise("Squat"))
            .let { it.updateSet(it.sets.first().id) { set -> set.copy(weightKg = 100.0) } }
            .replaceExercise(0, exercise("Presse"))

        assertEquals("Presse", workout.exercises.single().exerciseName)
        assertTrue(workout.sets.none { it.weightKg == 100.0 })
    }

    @Test
    fun `an added set carries over the previous load and repetitions`() {
        val workout = workoutOf(exercise("Squat"))
        val lastSetId = workout.setsOf(0).last().id
        val loaded = workout.updateSet(lastSetId) { it.copy(weightKg = 80.0, reps = 5) }.addSet(0)

        val added = loaded.setsOf(0).last()
        assertEquals(3, added.setIndex)
        assertEquals(80.0, added.weightKg, 0.0)
        assertEquals(5, added.reps)
    }

    @Test
    fun `an added set on a unilateral exercise covers both sides`() {
        val workout = workoutOf(exercise("Fentes", unilateral = true)).addSet(0)
        val added = workout.setsOf(0).filter { it.setIndex == 3 }
        assertEquals(listOf(UnilateralSide.LEFT, UnilateralSide.RIGHT), added.map { it.side })
    }

    @Test
    fun `a rest time change reaches the sets already on the board`() {
        val workout = workoutOf(exercise("Squat")).setRestTime(0, 120)
        assertTrue(workout.sets.all { it.restTimeSeconds == 120 })
    }

    @Test
    fun `a weight unit change reaches the sets already on the board`() {
        val workout = workoutOf(exercise("Squat")).setWeightUnit(0, WeightUnit.LBS)
        assertTrue(workout.sets.all { it.weightUnit == WeightUnit.LBS })
    }

    @Test
    fun `tonnage counts only the sets the athlete validated`() {
        val workout = workoutOf(exercise("Squat"))
        val ids = workout.setsOf(0).map { it.id }
        val done = workout
            .updateSet(ids[0]) { it.copy(weightKg = 100.0, reps = 5, isCompleted = true) }
            .updateSet(ids[1]) { it.copy(weightKg = 100.0, reps = 5, isCompleted = false) }

        assertEquals(500.0, done.completedVolumeKg(), 0.0)
    }

    @Test
    fun `changing a set type reaches both sides of a unilateral set`() {
        val workout = workoutOf(exercise("Fentes", unilateral = true))
            .setType(0, setIndex = 1, type = RoutineSetType.WARMUP)

        val first = workout.setsOf(0).filter { it.setIndex == 1 }
        assertEquals(2, first.size)
        assertTrue(first.all { it.type == RoutineSetType.WARMUP })
        assertTrue(workout.setsOf(0).filter { it.setIndex == 2 }.all { it.type == RoutineSetType.NORMAL })
    }

    @Test
    fun `deleting a set drops both sides and renumbers what remains`() {
        val threeSets = listOf(RoutineSet(setIndex = 1), RoutineSet(setIndex = 2), RoutineSet(setIndex = 3))
        val workout = workoutOf(exercise("Fentes", unilateral = true, sets = threeSets))
            .removeSetAt(0, setIndex = 1)

        assertEquals(listOf(1, 1, 2, 2), workout.setsOf(0).map { it.setIndex })
    }

    @Test
    fun `the last set of an exercise cannot be deleted`() {
        val workout = workoutOf(exercise("Squat", sets = listOf(RoutineSet(setIndex = 1))))
        assertFalse(workout.canRemoveSet(0))
        assertEquals(workout, workout.removeSetAt(0, setIndex = 1))
    }

    @Test
    fun `a set added after a deletion does not reuse an index`() {
        val workout = workoutOf(exercise("Squat")).removeSetAt(0, setIndex = 1).addSet(0)
        assertEquals(listOf(1, 2), workout.setsOf(0).map { it.setIndex })
    }

    @Test
    fun `an operation on an exercise that is gone leaves the workout untouched`() {
        val workout = workoutOf(exercise("Squat"))
        assertEquals(workout, workout.removeExercise(7))
        assertEquals(workout, workout.addSet(7))
        assertEquals(workout, workout.setRestTime(7, 120))
    }

    @Test
    fun `a bilateral set always opens rest on completion`() {
        val workout = workoutOf(exercise("Squat"))
        val firstSet = workout.sets.first()
        assertTrue(workout.shouldRestAfter(firstSet))
    }

    @Test
    fun `a unilateral left set waits for the right side before resting`() {
        val workout = workoutOf(exercise("Fentes", unilateral = true))
        val leftSet = workout.sets.first { it.side == UnilateralSide.LEFT && it.setIndex == 1 }
        val rightSet = workout.sets.first { it.side == UnilateralSide.RIGHT && it.setIndex == 1 }

        assertFalse(workout.shouldRestAfter(leftSet))

        val rightDone = workout.updateSet(rightSet.id) { it.copy(isCompleted = true) }
        assertTrue(rightDone.shouldRestAfter(leftSet))
    }

    @Test
    fun `a unilateral right set opens rest directly`() {
        val workout = workoutOf(exercise("Fentes", unilateral = true))
        val rightSet = workout.sets.first { it.side == UnilateralSide.RIGHT && it.setIndex == 1 }
        assertTrue(workout.shouldRestAfter(rightSet))
    }
}
