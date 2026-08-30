package app.bodyforger.mobile.library

import androidx.lifecycle.ViewModel
import app.bodyforger.core.model.Routine
import app.bodyforger.core.model.RoutineExercise
import app.bodyforger.core.model.RoutineSet
import app.bodyforger.core.model.WeightUnit
import app.bodyforger.mobile.library.RoutineDraft.addExercise
import app.bodyforger.mobile.library.RoutineDraft.addSet
import app.bodyforger.mobile.library.RoutineDraft.removeExercise
import app.bodyforger.mobile.library.RoutineDraft.removeSet
import app.bodyforger.mobile.library.RoutineDraft.reorderExercises
import app.bodyforger.mobile.library.RoutineDraft.replaceExercise
import app.bodyforger.mobile.library.RoutineDraft.setRestTime
import app.bodyforger.mobile.library.RoutineDraft.setWeightUnit
import app.bodyforger.mobile.library.RoutineDraft.updateSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The routine being edited, held above the editor screen.
 *
 * The draft has to outlive the screen: adding an exercise means leaving for the catalogue and
 * coming back, and a state kept inside the editor would be destroyed on the way out — losing
 * the name typed and the sets configured. The rules themselves live in [RoutineDraft].
 */
class RoutineDraftViewModel : ViewModel() {

    private val _draft = MutableStateFlow<Routine?>(null)
    val draft: StateFlow<Routine?> = _draft.asStateFlow()

    private val _isNew = MutableStateFlow(true)

    /** Whether the draft is a routine being created rather than one being edited. */
    val isNew: StateFlow<Boolean> = _isNew.asStateFlow()

    /** Opens an empty draft, or the routine being edited. */
    fun open(routine: Routine?) {
        _draft.value = routine ?: Routine(name = "")
        _isNew.value = routine == null
    }

    fun close() {
        _draft.value = null
    }

    fun rename(name: String) = mutate { it.copy(name = name) }

    fun setNotes(notes: String) = mutate { it.copy(notes = notes) }

    /** Adds an exercise coming back from the catalogue, or replaces the one at [replacing]. */
    fun addExercise(exercise: RoutineExercise, replacing: Int? = null) = mutate { draft ->
        if (replacing != null) draft.replaceExercise(replacing, exercise) else draft.addExercise(exercise)
    }

    fun removeExercise(index: Int) = mutate { it.removeExercise(index) }

    fun reorderExercises(reordered: List<RoutineExercise>) = mutate { it.reorderExercises(reordered) }

    fun setRestTime(index: Int, seconds: Int) = mutate { it.setRestTime(index, seconds) }

    fun setWeightUnit(index: Int, unit: WeightUnit) = mutate { it.setWeightUnit(index, unit) }

    fun updateSet(exerciseIndex: Int, setIndex: Int, set: RoutineSet) =
        mutate { it.updateSet(exerciseIndex, setIndex, set) }

    fun addSet(exerciseIndex: Int) = mutate { it.addSet(exerciseIndex) }

    fun removeSet(exerciseIndex: Int, setIndex: Int) = mutate { it.removeSet(exerciseIndex, setIndex) }

    private fun mutate(change: (Routine) -> Routine) {
        _draft.value = _draft.value?.let(change)
    }
}
