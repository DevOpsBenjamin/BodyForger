package app.bodyforger.mobile.workout

import app.bodyforger.core.model.RoutineExercise
import app.bodyforger.core.model.UnilateralSide
import app.bodyforger.core.model.WeightUnit
import app.bodyforger.core.model.WorkoutSession
import app.bodyforger.core.model.WorkoutSet
import java.util.UUID

/**
 * A workout as it stands right now: its identity, the exercises on the board, and every set.
 *
 * All of it is a value with no Android in it, so the rules below are unit-tested rather than
 * discovered on the athlete's phone mid-session.
 *
 * A set carries the position of its exercise in [orderIndex]. Every operation that moves the
 * exercise list therefore renumbers the sets with it — see [reindexed].
 */
data class LiveWorkout(
    val session: WorkoutSession,
    val exercises: List<RoutineExercise>,
    val sets: List<WorkoutSet>
) {

    /** The exercise a set belongs to, or null once that exercise has left the board. */
    fun exerciseOf(set: WorkoutSet): RoutineExercise? = exercises.getOrNull(set.orderIndex)

    /** The sets of one exercise, in the order they are to be performed. */
    fun setsOf(exerciseIndex: Int): List<WorkoutSet> =
        sets.filter { it.orderIndex == exerciseIndex }.sortedBy { it.setIndex }

    fun addExercise(exercise: RoutineExercise): LiveWorkout {
        val appended = exercises + exercise.copy(orderIndex = exercises.size)
        return copy(exercises = appended, sets = sets + plannedSetsFor(exercise, appended.lastIndex))
    }

    /**
     * Swaps one exercise for another, dropping the sets of the old one.
     *
     * Keeping them would carry loads that belong to a different movement.
     */
    fun replaceExercise(index: Int, exercise: RoutineExercise): LiveWorkout {
        if (index !in exercises.indices) return this
        val swapped = exercises.toMutableList().apply { this[index] = exercise.copy(orderIndex = index) }
        val kept = sets.filterNot { it.orderIndex == index }
        return copy(exercises = swapped, sets = kept + plannedSetsFor(exercise, index))
    }

    fun removeExercise(index: Int): LiveWorkout {
        if (index !in exercises.indices) return this
        val remaining = exercises.toMutableList().apply { removeAt(index) }
        val kept = sets.filterNot { it.orderIndex == index }
        return copy(exercises = remaining, sets = kept).reindexed(removedIndex = index)
    }

    /**
     * Adds one more set to an exercise, carrying over the load and repetitions of the last one.
     *
     * An athlete adding a set is extending the effort just done, not starting from nothing.
     */
    fun addSet(exerciseIndex: Int): LiveWorkout {
        val exercise = exercises.getOrNull(exerciseIndex) ?: return this
        val previous = setsOf(exerciseIndex).lastOrNull()
        val setIndex = (previous?.setIndex ?: 0) + 1
        val added = sidesOf(exercise).map { side ->
            blankSet(exercise, exerciseIndex, setIndex, side).copy(
                weightKg = previous?.weightKg ?: 0.0,
                reps = previous?.reps ?: DEFAULT_REPS
            )
        }
        return copy(sets = sets + added)
    }

    fun removeSet(setId: String): LiveWorkout = copy(sets = sets.filterNot { it.id == setId })

    fun updateSet(setId: String, change: (WorkoutSet) -> WorkoutSet): LiveWorkout =
        copy(sets = sets.map { if (it.id == setId) change(it) else it })

    fun setRestTime(exerciseIndex: Int, seconds: Int): LiveWorkout =
        mutateExercise(exerciseIndex) { it.copy(restTimeSeconds = seconds) }
            .copy(sets = sets.map { if (it.orderIndex == exerciseIndex) it.copy(restTimeSeconds = seconds) else it })

    /** The unit is a display choice: the sets keep their load, which is always in kilograms. */
    fun setWeightUnit(exerciseIndex: Int, unit: WeightUnit): LiveWorkout =
        mutateExercise(exerciseIndex) { it.copy(weightUnit = unit) }
            .copy(sets = sets.map { if (it.orderIndex == exerciseIndex) it.copy(weightUnit = unit) else it })

    /** The session as it would be recorded, sets included. */
    fun toSession(): WorkoutSession = session.copy(sets = sets, totalVolumeKg = completedVolumeKg())

    /** Tonnage lifted, counting only what the athlete has actually validated. */
    fun completedVolumeKg(): Double =
        sets.filter { it.isCompleted }.sumOf { it.weightKg * it.reps }

    private fun mutateExercise(index: Int, change: (RoutineExercise) -> RoutineExercise): LiveWorkout {
        if (index !in exercises.indices) return this
        return copy(exercises = exercises.toMutableList().apply { this[index] = change(this[index]) })
    }

    /** Closes the gap left by a removed exercise, so index and position agree again. */
    private fun reindexed(removedIndex: Int): LiveWorkout = copy(
        exercises = exercises.mapIndexed { position, exercise -> exercise.copy(orderIndex = position) },
        sets = sets.map { if (it.orderIndex > removedIndex) it.copy(orderIndex = it.orderIndex - 1) else it }
    )

    companion object {

        /** What a set holds when the routine states no target — never a load, only a count. */
        const val DEFAULT_REPS = 10

        /**
         * Opens a workout from a routine, or an empty one when the athlete trains freely.
         */
        fun from(
            exercises: List<RoutineExercise>,
            session: WorkoutSession
        ): LiveWorkout = LiveWorkout(
            session = session,
            exercises = exercises.mapIndexed { position, exercise -> exercise.copy(orderIndex = position) },
            sets = exercises.flatMapIndexed { position, exercise -> plannedSetsFor(exercise, position) }
        )

        /**
         * Turns the sets a routine plans into the sets to be performed.
         *
         * A unilateral exercise yields two: one per side. They share a [WorkoutSet.setIndex],
         * which is what lets the rest timer wait for the second side before starting.
         */
        fun plannedSetsFor(exercise: RoutineExercise, exerciseIndex: Int): List<WorkoutSet> =
            exercise.sets.flatMap { planned ->
                sidesOf(exercise).map { side ->
                    blankSet(exercise, exerciseIndex, planned.setIndex, side).copy(
                        type = planned.type,
                        weightKg = planned.targetWeightKg ?: 0.0,
                        reps = planned.reps ?: planned.minReps ?: DEFAULT_REPS
                    )
                }
            }

        private fun sidesOf(exercise: RoutineExercise): List<UnilateralSide> =
            if (exercise.isUnilateral) listOf(UnilateralSide.LEFT, UnilateralSide.RIGHT)
            else listOf(UnilateralSide.NONE)

        private fun blankSet(
            exercise: RoutineExercise,
            exerciseIndex: Int,
            setIndex: Int,
            side: UnilateralSide
        ) = WorkoutSet(
            id = UUID.randomUUID().toString(),
            exerciseId = exercise.exerciseId,
            exerciseName = exercise.exerciseName,
            primaryMuscle = exercise.primaryMuscle,
            equipment = exercise.equipment,
            activityCategory = exercise.activityCategory,
            orderIndex = exerciseIndex,
            setIndex = setIndex,
            weightKg = 0.0,
            weightUnit = exercise.weightUnit,
            reps = DEFAULT_REPS,
            isCompleted = false,
            side = side,
            restTimeSeconds = exercise.restTimeSeconds
        )
    }
}
