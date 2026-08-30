package app.bodyforger.mobile.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.bodyforger.core.database.dao.WorkoutDao
import app.bodyforger.core.database.entity.toDomain
import app.bodyforger.core.database.entity.toEntity
import app.bodyforger.core.database.entity.toSetEntities
import app.bodyforger.core.model.Routine
import app.bodyforger.core.model.WorkoutSession
import app.bodyforger.core.model.WorkoutSessionStatus
import app.bodyforger.core.model.WorkoutSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Persists a live workout as it happens.
 *
 * Each validated set is written on its own, so an interrupted session — a crash, a flat
 * battery — keeps every set already done and can be picked up where it stopped. A session
 * written only at the end would lose an hour of work to a moment of bad luck.
 */
class LiveWorkoutViewModel(private val workoutDao: WorkoutDao) : ViewModel() {

    private val _resumable = MutableStateFlow<WorkoutSession?>(null)

    /** A session left open by a previous run, offered for resumption. */
    val resumable: StateFlow<WorkoutSession?> = _resumable.asStateFlow()

    init {
        viewModelScope.launch {
            _resumable.value = workoutDao.getActiveSession()?.toDomain()
        }
    }

    /**
     * Opens a session and writes it before the first set, so that a crash cannot leave sets
     * without the session they belong to.
     */
    fun start(session: WorkoutSession, plannedSets: List<WorkoutSet>) {
        viewModelScope.launch {
            workoutDao.insertSession(session.toEntity())
            if (plannedSets.isNotEmpty()) workoutDao.insertSets(plannedSets.toSetEntities(session.id))
            _resumable.value = null
        }
    }

    /** Records one validated set, and the session tonnage that follows from it. */
    fun recordSet(set: WorkoutSet) {
        viewModelScope.launch {
            workoutDao.atomicCompleteSet(
                setId = set.id,
                isCompleted = set.isCompleted,
                completedAtEpochMs = set.completedAtEpochMs,
                weightKg = set.weightKg,
                reps = set.reps,
                rpe = set.rpe
            )
        }
    }

    /** Keeps a set's load or repetitions as they are edited, before it is validated. */
    fun updateSet(set: WorkoutSet, sessionId: String) {
        viewModelScope.launch { workoutDao.updateSet(set.toEntity(sessionId)) }
    }

    /** Drops the resumption offer once the athlete has taken it. */
    fun clearResumable() {
        _resumable.value = null
    }

    fun finish(session: WorkoutSession) = close(session, WorkoutSessionStatus.COMPLETED)

    /**
     * Marks a session discarded rather than deleting it.
     *
     * An append-only history keeps the trace: an abandoned session is information, and
     * removing rows would break the idempotence the sync relies on (ADR 001 §A).
     */
    fun discard(session: WorkoutSession) = close(session, WorkoutSessionStatus.DISCARDED)

    private fun close(session: WorkoutSession, status: WorkoutSessionStatus) {
        viewModelScope.launch {
            workoutDao.updateSession(
                session.copy(
                    status = status,
                    endedAtEpochMs = System.currentTimeMillis(),
                    isFinalized = true
                ).toEntity()
            )
            _resumable.value = null
        }
    }

    fun startFrom(routine: Routine?, session: WorkoutSession, plannedSets: List<WorkoutSet>) =
        start(session.copy(routineId = routine?.id), plannedSets)
}
