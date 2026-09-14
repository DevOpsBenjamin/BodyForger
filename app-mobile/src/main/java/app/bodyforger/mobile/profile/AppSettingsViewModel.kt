package app.bodyforger.mobile.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.bodyforger.core.bia.ModelSelector
import app.bodyforger.core.database.dao.AppSettingsDao
import app.bodyforger.core.database.entity.AppSettingsEntity
import app.bodyforger.core.model.HeightUnit
import app.bodyforger.core.model.WeightUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Installation-wide preferences, for the settings screen.
 *
 * They share one database row, so they share one view model: two would each hold their own
 * cache of the same line and drift apart on a write.
 */
class AppSettingsViewModel(
    private val appSettingsDao: AppSettingsDao
) : ViewModel() {

    /** The engine ids compiled into this build, in display order. */
    val engineIds: List<String> = ModelSelector.available.map { it.id }

    private val settings: Flow<AppSettingsEntity?> = appSettingsDao.observe()

    val selectedEngineId: StateFlow<String> = settings
        .map { it?.biaEngineId ?: ModelSelector.DEFAULT_ID }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_GRACE_MS), ModelSelector.DEFAULT_ID)

    /** The unit a new exercise starts from. Kilograms until the athlete says otherwise. */
    val defaultWeightUnit: StateFlow<WeightUnit> = settings
        .map { entity -> entity?.defaultWeightUnit.toWeightUnit() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_GRACE_MS), DEFAULT_UNIT)

    /** The unit a height is written and read in. Centimetres until the athlete says otherwise. */
    val defaultHeightUnit: StateFlow<HeightUnit> = settings
        .map { entity -> entity?.defaultHeightUnit.toHeightUnit() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_GRACE_MS), DEFAULT_HEIGHT_UNIT)

    /** Whether the athlete has dismissed or handled the Google Health Connect prompt. */
    val healthConnectPromptDismissed: StateFlow<Boolean?> = settings
        .map { it?.healthConnectPromptDismissed ?: false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_GRACE_MS), null)

    fun selectDefaultHeightUnit(unit: HeightUnit) {
        viewModelScope.launch { appSettingsDao.setDefaultHeightUnit(unit.name) }
    }

    fun selectEngine(engineId: String) {
        viewModelScope.launch { appSettingsDao.setBiaEngine(engineId) }
    }

    fun selectDefaultWeightUnit(unit: WeightUnit) {
        viewModelScope.launch { appSettingsDao.setDefaultWeightUnit(unit.name) }
    }

    fun setHealthConnectPromptDismissed(dismissed: Boolean) {
        viewModelScope.launch { appSettingsDao.setHealthConnectPromptDismissed(dismissed) }
    }

    private companion object {
        const val SUBSCRIPTION_GRACE_MS = 5_000L
        val DEFAULT_UNIT = WeightUnit.KG

        val DEFAULT_HEIGHT_UNIT = HeightUnit.CM

        /** An unreadable stored name falls back rather than crashing an app that starts up. */
        fun String?.toHeightUnit(): HeightUnit =
            this?.let { runCatching { HeightUnit.valueOf(it) }.getOrNull() } ?: DEFAULT_HEIGHT_UNIT

        fun String?.toWeightUnit(): WeightUnit =
            this?.let { runCatching { WeightUnit.valueOf(it) }.getOrNull() } ?: DEFAULT_UNIT
    }
}
