package app.bodyforger.mobile.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.bodyforger.core.database.dao.AppSettingsDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * What the athlete has already been shown, and the record of having shown it.
 *
 * The two counters live in the same settings row as the rest of the preferences, so they are
 * read here together — but they answer separately: the tour can be re-offered without the
 * setup flow following it, and the other way round.
 */
class OnboardingViewModel(
    private val appSettingsDao: AppSettingsDao
) : ViewModel() {

    private val settings = appSettingsDao.observe()

    /**
     * Whether the tour is owed. Null until the row has been read: showing a tour to someone
     * who already went through it, for the instant it takes to load, would be worse than a
     * blank moment.
     */
    val tourDue: StateFlow<Boolean?> = settings
        .map { (it?.tourVersionSeen ?: 0) < TOUR_VERSION }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_GRACE_MS), null)

    /** Whether the setup flow is owed, on the same terms. */
    val setupDue: StateFlow<Boolean?> = settings
        .map { (it?.setupVersionDone ?: 0) < SETUP_VERSION }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_GRACE_MS), null)

    /**
     * Records the tour as seen. Finishing and skipping write the same thing: both are an
     * answer, and the app does not insist on either.
     */
    fun markTourSeen() {
        viewModelScope.launch { appSettingsDao.setTourVersionSeen(TOUR_VERSION) }
    }

    /** Records the setup flow as gone through, skipping included. */
    fun markSetupDone() {
        viewModelScope.launch { appSettingsDao.setSetupVersionDone(SETUP_VERSION) }
    }

    /** Puts the tour back on offer, for the athlete who asks for it again from Settings. */
    fun replayTour() {
        viewModelScope.launch { appSettingsDao.setTourVersionSeen(0) }
    }

    /** Puts the setup flow back on offer. */
    fun replaySetup() {
        viewModelScope.launch { appSettingsDao.setSetupVersionDone(0) }
    }

    private companion object {
        const val SUBSCRIPTION_GRACE_MS = 5_000L
    }
}
