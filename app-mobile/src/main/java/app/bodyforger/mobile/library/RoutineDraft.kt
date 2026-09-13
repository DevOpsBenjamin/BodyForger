package app.bodyforger.mobile.library

import app.bodyforger.core.model.Routine
import app.bodyforger.core.model.RoutineExercise
import app.bodyforger.core.model.RoutineSet
import app.bodyforger.core.model.RoutineSetType
import app.bodyforger.core.model.WeightUnit

/**
 * The edits an athlete makes to a routine, as operations on a value.
 *
 * Kept out of the screen and out of Android so the rules below are unit-tested. Position is
 * held by [RoutineExercise.orderIndex] and [RoutineSet.setIndex], and both are renumbered
 * whenever the lists move — an index that no longer matches its position reorders the routine
 * behind the athlete's back.
 */
object RoutineDraft {

    /** What a set proposes when the athlete states nothing: a count, never a load. */
    const val DEFAULT_REPS = 10
    const val DEFAULT_MIN_REPS = 8
    const val DEFAULT_MAX_REPS = 12

    /** Rest an exercise falls back to when it carries none of its own. */
    const val DEFAULT_REST_SECONDS = 90

    /** A routine keeps at least one set per exercise: an exercise with none is not planned. */
    const val MINIMUM_SETS_PER_EXERCISE = 1

    fun Routine.addExercise(exercise: RoutineExercise): Routine =
        withExercises(exercises + exercise)

    fun Routine.replaceExercise(index: Int, exercise: RoutineExercise): Routine =
        if (index !in exercises.indices) this
        else withExercises(exercises.toMutableList().apply { this[index] = exercise })

    fun Routine.removeExercise(index: Int): Routine =
        if (index !in exercises.indices) this
        else withExercises(exercises.toMutableList().apply { removeAt(index) })

    fun Routine.reorderExercises(reordered: List<RoutineExercise>): Routine = withExercises(reordered)

    fun Routine.setRestTime(index: Int, seconds: Int): Routine =
        mutateExercise(index) { it.copy(restTimeSeconds = seconds) }

    fun Routine.setWeightUnit(index: Int, unit: WeightUnit): Routine =
        mutateExercise(index) { it.copy(weightUnit = unit) }

    fun Routine.updateSet(exerciseIndex: Int, setIndex: Int, set: RoutineSet): Routine =
        mutateSets(exerciseIndex) { sets ->
            if (setIndex !in sets.indices) sets
            else sets.toMutableList().apply { this[setIndex] = set }
        }

    /**
     * Adds a set carrying over what the previous one plans.
     *
     * An athlete adding a set is extending the same effort, not starting from nothing.
     */
    fun Routine.addSet(exerciseIndex: Int): Routine = mutateSets(exerciseIndex) { sets ->
        val previous = sets.lastOrNull()
        sets + RoutineSet(
            type = RoutineSetType.NORMAL,
            targetWeightKg = previous?.targetWeightKg,
            reps = previous?.reps ?: DEFAULT_REPS,
            minReps = previous?.minReps ?: DEFAULT_MIN_REPS,
            maxReps = previous?.maxReps ?: DEFAULT_MAX_REPS,
            isRepsRange = previous?.isRepsRange ?: false
        )
    }

    fun Routine.removeSet(exerciseIndex: Int, setIndex: Int): Routine = mutateSets(exerciseIndex) { sets ->
        if (sets.size <= MINIMUM_SETS_PER_EXERCISE || setIndex !in sets.indices) sets
        else sets.toMutableList().apply { removeAt(setIndex) }
    }

    /** The routine as it would be saved, once the name has been trimmed. */
    fun Routine.readyToSave(): Routine = copy(name = name.trim(), notes = notes.trim())

    /** Whether the routine can be saved at all — an unnamed routine cannot be found again. */
    fun Routine.isSaveable(): Boolean = name.isNotBlank()

    private fun Routine.mutateExercise(index: Int, change: (RoutineExercise) -> RoutineExercise): Routine =
        if (index !in exercises.indices) this
        else copy(exercises = exercises.toMutableList().apply { this[index] = change(this[index]) })

    private fun Routine.mutateSets(index: Int, change: (List<RoutineSet>) -> List<RoutineSet>): Routine =
        mutateExercise(index) { exercise -> exercise.copy(sets = renumbered(change(exercise.sets))) }

    private fun Routine.withExercises(updated: List<RoutineExercise>): Routine =
        copy(exercises = updated.mapIndexed { position, exercise -> exercise.copy(orderIndex = position) })

    /** Set indices are displayed to the athlete, so they count from one, without gaps. */
    private fun renumbered(sets: List<RoutineSet>): List<RoutineSet> =
        sets.mapIndexed { position, set -> set.copy(setIndex = position + 1) }
}
