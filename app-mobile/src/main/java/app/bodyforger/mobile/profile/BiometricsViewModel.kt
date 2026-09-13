package app.bodyforger.mobile.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.bodyforger.core.bia.ModelSelector
import app.bodyforger.core.database.dao.AppSettingsDao
import app.bodyforger.core.database.dao.AthleteIdentityDao
import app.bodyforger.core.database.dao.BodyLogDao
import app.bodyforger.core.database.entity.toDomain
import app.bodyforger.core.database.entity.toProfile
import app.bodyforger.core.model.AthleteProfile
import app.bodyforger.core.model.BodyCompositionReport
import app.bodyforger.core.model.BodyLog
import app.bodyforger.core.model.WeightUnit
import java.time.LocalDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * What the biometrics screens show: the athlete's last weigh-in, read back as body composition.
 *
 * Nothing is computed until a real measurement and a complete profile exist. A screen filled
 * with plausible figures would be read as the athlete's own body.
 */
class BiometricsViewModel(
    bodyLogDao: BodyLogDao,
    identityDao: AthleteIdentityDao,
    private val appSettingsDao: AppSettingsDao
) : ViewModel() {

    /** Every weigh-in, most recent first — what the home curve is drawn from. */
    val history: StateFlow<List<BodyLog>> = bodyLogDao.observeAll()
        .map { rows -> rows.map { it.toDomain() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_GRACE_MS), emptyList())

    /** What a body mass is read in. Everything stored and computed stays in kilograms. */
    val weightUnit: StateFlow<WeightUnit> = appSettingsDao.observe()
        .map { entity ->
            entity?.defaultWeightUnit?.let { runCatching { WeightUnit.valueOf(it) }.getOrNull() }
                ?: WeightUnit.KG
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_GRACE_MS), WeightUnit.KG)

    val state: StateFlow<BiometricsState> = combine(
        identityDao.observe().map { it?.toProfile() ?: AthleteProfile() },
        bodyLogDao.observeMostRecent().map { it?.toDomain() },
        appSettingsDao.observe().map { it?.biaEngineId ?: ModelSelector.DEFAULT_ID }
    ) { profile, lastLog, engineId ->
        BiometricsState(
            profile = profile,
            lastLog = lastLog,
            report = reportFor(profile, lastLog, engineId)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_GRACE_MS), BiometricsState())

    /**
     * Our own reading of the raw resistances, kept beside the scale's own figure rather than
     * replacing it — `docs/BIA_ENGINE.md`.
     */
    private fun reportFor(
        profile: AthleteProfile, log: BodyLog?, engineId: String
    ): BodyCompositionReport? {
        val measurement = log ?: return null
        val biaProfile = profile.biaProfileOn(LocalDate.now()) ?: return null
        return ModelSelector.modelFor(engineId).analyze(
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
