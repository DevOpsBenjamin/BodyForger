package app.bodyforger.mobile.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.bodyforger.core.database.dao.WorkoutDao
import app.bodyforger.core.database.entity.toDomain
import app.bodyforger.core.model.WorkoutHeartRateSample
import app.bodyforger.core.model.WorkoutSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * A past session, read whole.
 *
 * Its heart rate curve lives in its own table and is worth loading only when a session is
 * actually opened, which is why this is separate from the history list.
 */
data class WorkoutDetailState(
    val session: WorkoutSession? = null,
    val heartRates: List<WorkoutHeartRateSample> = emptyList(),
    val isLoading: Boolean = true
)

class WorkoutDetailViewModel(private val workoutDao: WorkoutDao) : ViewModel() {

    private val _state = MutableStateFlow(WorkoutDetailState())
    val state: StateFlow<WorkoutDetailState> = _state.asStateFlow()

    fun load(sessionId: String) {
        viewModelScope.launch {
            val session = workoutDao.getSessionWithSets(sessionId)?.toDomain()
            val samples = workoutDao.getHeartRateSamplesForSession(sessionId).map { it.toDomain() }
            _state.value = WorkoutDetailState(session, samples, isLoading = false)
        }
    }
}
