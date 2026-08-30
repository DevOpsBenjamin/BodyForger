package app.bodyforger.mobile.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.bodyforger.core.bia.DexaBiaCalculator
import app.bodyforger.core.database.dao.AthleteIdentityDao
import app.bodyforger.core.database.dao.BodyLogDao
import app.bodyforger.core.database.entity.toDomain
import app.bodyforger.core.database.entity.toProfile
import app.bodyforger.core.model.AthleteProfile
import app.bodyforger.core.model.BodyCompositionReport
import app.bodyforger.core.model.BodyLog
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

/**
 * What the biometrics screens show: the athlete's last weigh-in, read back as body composition.
 *
 * Nothing is computed until a real measurement and a complete profile exist. A screen filled
 * with plausible figures would be read as the athlete's own body.
 */
class BiometricsViewModel(
    bodyLogDao: BodyLogDao,
    identityDao: AthleteIdentityDao
) : ViewModel() {

    val state: StateFlow<BiometricsState> = combine(
        identityDao.observe().map { it?.toProfile() ?: AthleteProfile() },
        bodyLogDao.observeMostRecent().map { it?.toDomain() }
    ) { profile, lastLog ->
        BiometricsState(
            profile = profile,
            lastLog = lastLog,
            report = reportFor(profile, lastLog)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_GRACE_MS), BiometricsState())

    /**
     * Our own reading of the raw resistances, kept beside the scale's own figure rather than
     * replacing it — `docs/BIA_ENGINE.md`.
     */
    private fun reportFor(profile: AthleteProfile, log: BodyLog?): BodyCompositionReport? {
        val measurement = log ?: return null
        val biaProfile = profile.biaProfileOn(LocalDate.now()) ?: return null
        return DexaBiaCalculator.calculate(
            massKg = measurement.massKg,
            profile = biaProfile,
            impedances = measurement.rawImpedances
        )
    }

    private companion object {
        const val SUBSCRIPTION_GRACE_MS = 5_000L
    }
}

/**
 * [report] is our own computation; [BodyLog.bodyFatPercentage] is what the scale itself
 * concluded. Both are shown: they do not have to agree.
 */
data class BiometricsState(
    val profile: AthleteProfile = AthleteProfile(),
    val lastLog: BodyLog? = null,
    val report: BodyCompositionReport? = null
)
