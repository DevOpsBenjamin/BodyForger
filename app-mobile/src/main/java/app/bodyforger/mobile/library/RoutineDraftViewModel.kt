package app.bodyforger.mobile.library

import androidx.lifecycle.ViewModel
import app.bodyforger.core.model.Routine
import app.bodyforger.core.model.RoutineExercise
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The routine being edited, held above the editor screen.
 *
 * The draft has to outlive the screen: adding an exercise means leaving for the catalogue and
 * coming back, and a state kept inside the editor would be destroyed on the way out — losing
 * the name typed and the sets configured.
 */
class RoutineDraftViewModel : ViewModel() {

    private val _draft = MutableStateFlow<Routine?>(null)
    val draft: StateFlow<Routine?> = _draft.asStateFlow()

    /** Opens an empty draft, or the routine being edited. */
    fun open(routine: Routine?) {
        _draft.value = routine ?: Routine(name = "")
    }

    fun close() {
        _draft.value = null
    }

    fun rename(name: String) = mutate { it.copy(name = name) }

    fun setNotes(notes: String) = mutate { it.copy(notes = notes) }

    fun setExercises(exercises: List<RoutineExercise>) = mutate { it.copy(exercises = exercises) }

    /** Appends an exercise coming back from the catalogue, or replaces one at [replacing]. */
    fun addExercise(exercise: RoutineExercise, replacing: Int? = null) = mutate { current ->
        val updated = current.exercises.toMutableList()
        if (replacing != null && replacing in updated.indices) {
            updated[replacing] = exercise
        } else {
            updated += exercise
        }
        current.copy(exercises = updated)
    }

    private fun mutate(change: (Routine) -> Routine) {
        _draft.value = _draft.value?.let(change)
    }
}
