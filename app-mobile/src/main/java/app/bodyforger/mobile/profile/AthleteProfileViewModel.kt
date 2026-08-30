package app.bodyforger.mobile.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.bodyforger.core.database.dao.AthleteIdentityDao
import app.bodyforger.core.database.entity.toProfile
import app.bodyforger.core.model.AthleteProfile
import app.bodyforger.core.model.BiologicalSex
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * The athlete's profile, read from and written to the database.
 *
 * One source for the whole app: sex, age and height frame the scale's own body fat
 * computation, so a screen holding its own copy would measure a different person from the
 * one the scale was told about.
 */
class AthleteProfileViewModel(private val identityDao: AthleteIdentityDao) : ViewModel() {

    val profile: StateFlow<AthleteProfile> = identityDao.observe()
        .map { it?.toProfile() ?: AthleteProfile() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_GRACE_MS), AthleteProfile())

    fun save(sex: BiologicalSex?, birthDateIso: String?, heightCm: Double?) {
        viewModelScope.launch {
            identityDao.saveProfile(
                sex = sex?.name,
                birthDateIso = birthDateIso,
                heightCm = heightCm,
                nowEpochMs = System.currentTimeMillis()
            )
        }
    }

    private companion object {
        /** Keeps the flow alive across a configuration change rather than re-querying. */
        const val SUBSCRIPTION_GRACE_MS = 5_000L
    }
}
