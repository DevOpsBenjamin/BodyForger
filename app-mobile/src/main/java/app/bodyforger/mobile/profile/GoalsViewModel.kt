package app.bodyforger.mobile.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.bodyforger.core.database.dao.BodyGoalDao
import app.bodyforger.core.database.dao.BodyLogDao
import app.bodyforger.core.database.entity.toDomain
import app.bodyforger.core.database.entity.toEntity
import app.bodyforger.core.model.BodyGoal
import app.bodyforger.core.model.BodyLog
import app.bodyforger.mobile.stats.GoalProgress
import app.bodyforger.mobile.stats.GoalStanding
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * The milestones, and where each of them stands.
 *
 * Two things happen on their own here, and both are writes, so both are deliberate:
 *
 *  * a goal set before any weigh-in is **anchored** by the first one that follows, which is
 *    what gives it a direction;
 *  * a goal whose thresholds have been crossed for two consecutive valid weeks is **validated**,
 *    dated the day it was noticed.
 *
 * Neither ever runs backwards. Anchoring happens once, and a validated goal is left alone
 * however the trend moves afterwards.
 */
class GoalsViewModel(
    private val bodyGoalDao: BodyGoalDao,
    bodyLogDao: BodyLogDao
) : ViewModel() {

    private val goals = bodyGoalDao.observeAll().map { rows -> rows.map { it.toDomain() } }
    private val logs = bodyLogDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    val standings: StateFlow<List<GoalStanding>> = combine(goals, logs) { goals, logs ->
        settle(goals, logs)
        val today = LocalDate.now()
        goals.map { GoalProgress.standing(it, logs, today) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_GRACE_MS), emptyList())

    /** The one being worked towards: first unvalidated, in the order the DAO returns. */
    val active: StateFlow<GoalStanding?> = standings
        .map { list -> list.firstOrNull { !it.goal.isValidated } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_GRACE_MS), null)

    fun add(
        targetMassKg: Double,
        targetBodyFatPercentage: Double? = null,
        horizonDate: LocalDate? = null
    ) {
        viewModelScope.launch {
            bodyGoalDao.upsert(
                BodyGoal(
                    targetMassKg = targetMassKg,
                    targetBodyFatPercentage = targetBodyFatPercentage,
                    horizonDate = horizonDate,
                    createdOn = LocalDate.now()
                ).toEntity()
            )
        }
    }

    fun remove(goal: BodyGoal) {
        viewModelScope.launch { bodyGoalDao.delete(goal.toEntity()) }
    }

    /** Marks a goal reached, or unmarks one the athlete disagrees with. */
    fun setValidated(goal: BodyGoal, validated: Boolean) {
        viewModelScope.launch {
            bodyGoalDao.upsert(
                goal.copy(validatedOn = if (validated) LocalDate.now() else null).toEntity()
            )
        }
    }

    /**
     * Anchors what has no direction yet, validates what has been earned.
     *
     * Runs off the same emission the screen reads, so the athlete never sees a goal that is
     * ready and not yet marked.
     */
    private fun settle(goals: List<BodyGoal>, logs: List<BodyLog>) {
        if (logs.isEmpty()) return
        val today = LocalDate.now()
        val first = logs.minByOrNull { it.measuredAtEpochMs } ?: return

        val changed = goals.mapNotNull { goal ->
            val anchored = goal.startingFrom(first.massKg, first.bodyFatPercentage)
            val settled =
                if (!anchored.isValidated && GoalProgress.standing(anchored, logs, today).isReadyToValidate) {
                    anchored.copy(validatedOn = today)
                } else {
                    anchored
                }
            settled.takeIf { it != goal }
        }

        if (changed.isNotEmpty()) {
            viewModelScope.launch { bodyGoalDao.upsertAll(changed.map { it.toEntity() }) }
        }
    }

    private companion object {
        const val SUBSCRIPTION_GRACE_MS = 5_000L
    }
}
