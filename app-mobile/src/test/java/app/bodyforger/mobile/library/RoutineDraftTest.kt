package app.bodyforger.mobile.library

import app.bodyforger.core.model.EquipmentType
import app.bodyforger.core.model.MuscleGroup
import app.bodyforger.core.model.Routine
import app.bodyforger.core.model.RoutineExercise
import app.bodyforger.core.model.RoutineSet
import app.bodyforger.mobile.library.RoutineDraft.addExercise
import app.bodyforger.mobile.library.RoutineDraft.addSet
import app.bodyforger.mobile.library.RoutineDraft.isSaveable
import app.bodyforger.mobile.library.RoutineDraft.readyToSave
import app.bodyforger.mobile.library.RoutineDraft.removeExercise
import app.bodyforger.mobile.library.RoutineDraft.removeSet
import app.bodyforger.mobile.library.RoutineDraft.reorderExercises
import app.bodyforger.mobile.library.RoutineDraft.replaceExercise
import app.bodyforger.mobile.library.RoutineDraft.setRestTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutineDraftTest {

    private fun exercise(name: String, sets: Int = 3) = RoutineExercise(
        exerciseId = name.lowercase(),
        exerciseName = name,
        primaryMuscle = MuscleGroup.CHEST,
        equipment = EquipmentType.BARBELL,
        sets = (1..sets).map { RoutineSet(setIndex = it) }
    )

    private fun routine(vararg names: String) = names.fold(Routine(name = "Push")) { draft, name ->
        draft.addExercise(exercise(name))
    }

    @Test
    fun `an added exercise takes the next position`() {
        val draft = routine("Développé", "Écartés")
        assertEquals(listOf(0, 1), draft.exercises.map { it.orderIndex })
    }

    @Test
    fun `removing an exercise renumbers the ones that follow`() {
        val draft = routine("Premier", "Deuxième", "Troisième").removeExercise(0)
        assertEquals(listOf("Deuxième", "Troisième"), draft.exercises.map { it.exerciseName })
        assertEquals(listOf(0, 1), draft.exercises.map { it.orderIndex })
    }

    @Test
    fun `reordering renumbers the exercises to match their new order`() {
        val draft = routine("Premier", "Deuxième")
        val swapped = draft.reorderExercises(draft.exercises.reversed())
        assertEquals(listOf("Deuxième", "Premier"), swapped.exercises.map { it.exerciseName })
        assertEquals(listOf(0, 1), swapped.exercises.map { it.orderIndex })
    }

    @Test
    fun `a replaced exercise keeps the position it took over`() {
        val draft = routine("Premier", "Deuxième").replaceExercise(1, exercise("Remplaçant"))
        assertEquals("Remplaçant", draft.exercises[1].exerciseName)
        assertEquals(1, draft.exercises[1].orderIndex)
    }

    @Test
    fun `an added set carries over what the previous one plans`() {
        val planned = listOf(RoutineSet(setIndex = 1, targetWeightKg = 80.0, reps = 5))
        val draft = Routine(name = "Force").addExercise(exercise("Squat").copy(sets = planned)).addSet(0)

        val added = draft.exercises[0].sets.last()
        assertEquals(80.0, added.targetWeightKg!!, 0.0)
        assertEquals(5, added.reps)
        assertEquals(2, added.setIndex)
    }

    @Test
    fun `deleting a set renumbers the ones that follow`() {
        val draft = routine("Squat").removeSet(0, setIndex = 0)
        assertEquals(listOf(1, 2), draft.exercises[0].sets.map { it.setIndex })
    }

    @Test
    fun `a set added after a deletion does not reuse an index`() {
        val draft = routine("Squat").removeSet(0, setIndex = 0).addSet(0)
        assertEquals(listOf(1, 2, 3), draft.exercises[0].sets.map { it.setIndex })
    }

    @Test
    fun `the last set of an exercise cannot be deleted`() {
        val draft = routine("Squat").let { it.copy(exercises = listOf(it.exercises[0].copy(sets = listOf(RoutineSet(setIndex = 1))))) }
        assertEquals(1, draft.removeSet(0, setIndex = 0).exercises[0].sets.size)
    }

    @Test
    fun `a rest time change reaches only the exercise it targets`() {
        val draft = routine("Premier", "Deuxième").setRestTime(1, 150)
        assertEquals(RoutineDraft.DEFAULT_REST_SECONDS, draft.exercises[0].restTimeSeconds)
        assertEquals(150, draft.exercises[1].restTimeSeconds)
    }

    @Test
    fun `an operation on an exercise that is gone leaves the draft untouched`() {
        val draft = routine("Squat")
        assertEquals(draft, draft.removeExercise(7))
        assertEquals(draft, draft.addSet(7))
        assertEquals(draft, draft.setRestTime(7, 120))
    }

    @Test
    fun `a routine without a name cannot be saved`() {
        assertFalse(Routine(name = "   ").isSaveable())
        assertTrue(Routine(name = "Push").isSaveable())
    }

    @Test
    fun `saving trims the name and the notes`() {
        val ready = Routine(name = "  Push  ", notes = "  jour A  ").readyToSave()
        assertEquals("Push", ready.name)
        assertEquals("jour A", ready.notes)
    }
}
