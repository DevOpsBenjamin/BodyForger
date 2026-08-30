package app.bodyforger.mobile.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.bodyforger.core.database.BodyForgerDatabases
import app.bodyforger.core.database.data.DefaultExercises
import app.bodyforger.core.database.entity.toDomain
import app.bodyforger.core.database.entity.toEntity
import app.bodyforger.core.model.Exercise
import app.bodyforger.core.model.Routine
import app.bodyforger.core.model.WorkoutSession
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Routines, the exercise catalogue and past sessions, read from the database.
 *
 * Everything here outlives the process. The screens observe these flows rather than holding
 * their own lists, so a routine saved on one screen is visible on every other without being
 * passed around.
 */
class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val database = BodyForgerDatabases.get(application)
    private val routineDao = database.routineDao()
    private val exerciseDao = database.exerciseDao()
    private val workoutDao = database.workoutDao()

    init {
        viewModelScope.launch { seedCatalogueIfNeeded() }
    }

    /**
     * Makes sure the built-in catalogue is present.
     *
     * The database seeds itself when created, but asynchronously, so a first read can arrive
     * before the insert lands. And a catalogue enriched in a later version would never reach
     * an existing installation. Inserting ignores conflicts, so only what is missing is added
     * and anything the athlete edited is left alone.
     */
    private suspend fun seedCatalogueIfNeeded() {
        if (exerciseDao.getExercisesCount() < DefaultExercises.all.size) {
            exerciseDao.insertAll(DefaultExercises.all)
        }
    }

    val routines: StateFlow<List<Routine>> = routineDao.getAllRoutinesWithExercisesFlow()
        .map { rows -> rows.map { it.toDomain() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_GRACE_MS), emptyList())

    /** The whole catalogue: the built-in exercises and those the athlete added. */
    val exercises: StateFlow<List<Exercise>> = exerciseDao.getAllExercises()
        .map { rows -> rows.map { it.toDomain() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_GRACE_MS), emptyList())

    val completedSessions: StateFlow<List<WorkoutSession>> = workoutDao.getCompletedSessions()
        .map { rows -> rows.map { it.toDomain() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_GRACE_MS), emptyList())

    /** Writes a routine with its exercises and sets in one transaction. */
    fun saveRoutine(routine: Routine) {
        viewModelScope.launch {
            routineDao.saveFullRoutine(
                routine = routine.toEntity(),
                exercises = routine.exercises.map { it.toEntity(routine.id) },
                sets = routine.exercises.flatMap { exercise ->
                    exercise.sets.map { it.toEntity(exercise.id) }
                }
            )
        }
    }

    fun deleteRoutine(routineId: String) {
        viewModelScope.launch { routineDao.deleteRoutineById(routineId) }
    }

    fun duplicateRoutine(routineId: String, newName: String) {
        viewModelScope.launch { routineDao.duplicateRoutine(routineId, newName) }
    }

    /**
     * Adds or removes a training day.
     *
     * Written through its own query rather than by rewriting the routine: toggling a day must
     * not touch the exercises, which a full save would delete and reinsert.
     */
    fun toggleRoutineDay(routineId: String, day: Int) {
        viewModelScope.launch {
            val current = routineDao.getRoutineWithExercisesById(routineId)?.toDomain() ?: return@launch
            val days = if (day in current.assignedDays) current.assignedDays - day else current.assignedDays + day
            routineDao.updateAssignedDays(routineId, days.sorted().joinToString(","))
        }
    }

    fun addCustomExercise(exercise: Exercise) {
        viewModelScope.launch {
            exerciseDao.insertExercise(exercise.copy(isCustom = true).toEntity())
        }
    }

    /** Only an exercise the athlete added can be removed; the built-in catalogue stays. */
    fun deleteCustomExercise(exerciseId: String) {
        viewModelScope.launch { exerciseDao.deleteCustomExercise(exerciseId) }
    }

    fun newRoutineId(): String = UUID.randomUUID().toString()

    private companion object {
        /** Keeps the flows alive across a configuration change instead of restarting them. */
        const val SUBSCRIPTION_GRACE_MS = 5_000L
    }
}
