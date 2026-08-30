package app.bodyforger.mobile.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.bodyforger.core.database.dao.WorkoutDao
import app.bodyforger.core.database.entity.toDomain
import app.bodyforger.core.database.entity.toEntity
import app.bodyforger.core.database.entity.toSetEntities
import app.bodyforger.core.model.Routine
import app.bodyforger.core.model.RoutineExercise
import app.bodyforger.core.model.RoutineSetType
import app.bodyforger.core.model.WeightUnit
import app.bodyforger.core.model.WorkoutSession
import app.bodyforger.core.model.WorkoutSessionStatus
import app.bodyforger.core.model.WorkoutSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Owns the workout in progress and writes every change as it is made.
 *
 * The workout lives here rather than in the screen because the athlete can minimise the
 * session to consult a routine or the catalogue: a state held by the screen would leave with
 * it. Each change is also persisted on its own, so an interrupted session — a crash, a flat
 * battery — keeps everything already done.
 */
class LiveWorkoutViewModel(private val workoutDao: WorkoutDao) : ViewModel() {

    private val _active = MutableStateFlow<LiveWorkout?>(null)

    /** The workout in progress, or null when the athlete is not training. */
    val active: StateFlow<LiveWorkout?> = _active.asStateFlow()

    private val _resumable = MutableStateFlow<WorkoutSession?>(null)

    /** A session left open by a previous run, offered for resumption. */
    val resumable: StateFlow<WorkoutSession?> = _resumable.asStateFlow()

    init {
        viewModelScope.launch {
            _resumable.value = workoutDao.getActiveSession()?.toDomain()
        }
    }

    /**
     * Opens a workout and writes it before the first set, so that a crash cannot leave sets
     * without the session they belong to.
     *
     * [freeSessionTitle] names a session started without a routine.
     *
     * A workout already in progress is kept: opening a second one would leave the first
     * active in the database, to resurface later as a session to resume.
     */
    fun begin(routine: Routine?, freeSessionTitle: String) {
        if (_active.value != null) return
        val session = WorkoutSession(
            routineId = routine?.id,
            title = routine?.name ?: freeSessionTitle,
            startedAtEpochMs = System.currentTimeMillis()
        )
        val workout = LiveWorkout.from(routine?.exercises.orEmpty(), session)
        _active.value = workout
        _resumable.value = null
        viewModelScope.launch {
            workoutDao.insertSession(session.toEntity())
            if (workout.sets.isNotEmpty()) {
                workoutDao.insertSets(workout.sets.toSetEntities(session.id))
            }
        }
    }

    /** Picks a session back up where it stopped, board and loads included. */
    fun resume(session: WorkoutSession) {
        _active.value = LiveWorkout.resumed(session)
        _resumable.value = null
    }

    /** Drops the resumption offer without touching the session it pointed at. */
    fun clearResumable() {
        _resumable.value = null
    }

    fun addExercise(exercise: RoutineExercise) = mutate { workout ->
        workout.addExercise(exercise).also { insertSetsOf(it, exerciseIndex = it.exercises.lastIndex) }
    }

    fun replaceExercise(index: Int, exercise: RoutineExercise) = mutate { workout ->
        workout.setsOf(index).forEach { dropped -> viewModelScope.launch { workoutDao.deleteSet(dropped.id) } }
        workout.replaceExercise(index, exercise).also { insertSetsOf(it, exerciseIndex = index) }
    }

    /**
     * Takes an exercise off the board.
     *
     * Its sets are deleted rather than kept unfinished: they were never performed, so leaving
     * them would count planned work as done in the history.
     */
    fun removeExercise(index: Int) = mutate { workout ->
        val dropped = workout.setsOf(index)
        val without = workout.removeExercise(index)
        viewModelScope.launch {
            dropped.forEach { workoutDao.deleteSet(it.id) }
            // The remaining sets moved up a position; the stored rows must follow.
            workoutDao.insertSets(without.sets.toSetEntities(without.session.id))
        }
        without
    }

    fun addSet(exerciseIndex: Int) = mutate { workout ->
        val extended = workout.addSet(exerciseIndex)
        val added = extended.sets.filterNot { set -> workout.sets.any { it.id == set.id } }
        viewModelScope.launch { workoutDao.insertSets(added.toSetEntities(workout.session.id)) }
        extended
    }

    /** Changes what a set is — a warm-up, a drop set — on both sides of a unilateral one. */
    fun setType(exerciseIndex: Int, setIndex: Int, type: RoutineSetType) = mutate { workout ->
        workout.setType(exerciseIndex, setIndex, type).also { persistSetsOf(it) }
    }

    /** Drops a set and renumbers the ones that follow, in the database as on screen. */
    fun removeSetAt(exerciseIndex: Int, setIndex: Int) = mutate { workout ->
        if (!workout.canRemoveSet(exerciseIndex)) return@mutate workout
        val dropped = workout.sets.filter { it.orderIndex == exerciseIndex && it.setIndex == setIndex }
        val without = workout.removeSetAt(exerciseIndex, setIndex)
        viewModelScope.launch {
            dropped.forEach { workoutDao.deleteSet(it.id) }
            persist(without)
        }
        without
    }

    /** Records a set as done, or takes that back — the one write the tonnage depends on. */
    fun toggleSetCompleted(setId: String) = mutate { workout ->
        workout.updateSet(setId) { set ->
            val completed = !set.isCompleted
            set.copy(
                isCompleted = completed,
                completedAtEpochMs = if (completed) System.currentTimeMillis() else null
            )
        }.also { updated ->
            val recorded = updated.sets.first { it.id == setId }
            viewModelScope.launch {
                workoutDao.atomicCompleteSet(
                    setId = recorded.id,
                    isCompleted = recorded.isCompleted,
                    completedAtEpochMs = recorded.completedAtEpochMs,
                    weightKg = recorded.weightKg,
                    reps = recorded.reps,
                    rpe = recorded.rpe
                )
            }
        }
    }

    fun setWeight(setId: String, weightKg: Double) = editSet(setId) { it.copy(weightKg = weightKg) }

    fun setReps(setId: String, reps: Int) = editSet(setId) { it.copy(reps = reps) }

    fun setRestTime(exerciseIndex: Int, seconds: Int) =
        mutate { it.setRestTime(exerciseIndex, seconds).also(::persistSetsOf) }

    fun setWeightUnit(exerciseIndex: Int, unit: WeightUnit) =
        mutate { it.setWeightUnit(exerciseIndex, unit).also(::persistSetsOf) }

    /** Closes the workout as completed and clears the board. */
    fun finish(): WorkoutSession? = close(WorkoutSessionStatus.COMPLETED)

    /**
     * Marks a workout discarded rather than deleting it.
     *
     * An append-only history keeps the trace: an abandoned session is information, and
     * removing rows would break the idempotence the sync relies on (ADR 001 §A).
     */
    fun discard(): WorkoutSession? = close(WorkoutSessionStatus.DISCARDED)

    /** Discards a session offered for resumption that the athlete declined to pick up. */
    fun discard(session: WorkoutSession) {
        _resumable.value = null
        viewModelScope.launch { workoutDao.updateSession(closed(session, WorkoutSessionStatus.DISCARDED).toEntity()) }
    }

    private fun close(status: WorkoutSessionStatus): WorkoutSession? {
        val workout = _active.value ?: return null
        val session = closed(workout.toSession(), status)
        _active.value = null
        _resumable.value = null
        viewModelScope.launch { workoutDao.updateSession(session.toEntity()) }
        return session
    }

    private fun closed(session: WorkoutSession, status: WorkoutSessionStatus) = session.copy(
        status = status,
        endedAtEpochMs = System.currentTimeMillis(),
        isFinalized = true
    )

    private fun editSet(setId: String, change: (WorkoutSet) -> WorkoutSet) = mutate { workout ->
        workout.updateSet(setId, change).also { updated ->
            val edited = updated.sets.first { it.id == setId }
            viewModelScope.launch { workoutDao.updateSet(edited.toEntity(updated.session.id)) }
        }
    }

    private fun insertSetsOf(workout: LiveWorkout, exerciseIndex: Int) {
        val added = workout.setsOf(exerciseIndex)
        viewModelScope.launch { workoutDao.insertSets(added.toSetEntities(workout.session.id)) }
    }

    private fun persistSetsOf(workout: LiveWorkout) {
        viewModelScope.launch { persist(workout) }
    }

    private suspend fun persist(workout: LiveWorkout) {
        workoutDao.insertSets(workout.sets.toSetEntities(workout.session.id))
    }

    private fun mutate(change: (LiveWorkout) -> LiveWorkout) {
        _active.value = _active.value?.let(change)
    }
}
