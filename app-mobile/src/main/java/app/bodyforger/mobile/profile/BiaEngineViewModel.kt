package app.bodyforger.mobile.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.bodyforger.core.bia.ModelSelector
import app.bodyforger.core.database.dao.AppSettingsDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * The BIA engine choice, for the settings screen.
 *
 * The list of engines is fixed at build time — whichever modules are compiled in — and only
 * the selection is persisted. With a single engine the section has nothing to offer and the
 * screen hides it.
 */
class BiaEngineViewModel(
    private val appSettingsDao: AppSettingsDao
) : ViewModel() {

    /** The engine ids compiled into this build, in display order. */
    val engineIds: List<String> = ModelSelector.available.map { it.id }

    val selectedId: StateFlow<String> = appSettingsDao.observe()
        .map { it?.biaEngineId ?: ModelSelector.DEFAULT_ID }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_GRACE_MS), ModelSelector.DEFAULT_ID)

    fun select(engineId: String) {
        viewModelScope.launch { appSettingsDao.setBiaEngine(engineId) }
    }

    private companion object {
        const val SUBSCRIPTION_GRACE_MS = 5_000L
    }
}
