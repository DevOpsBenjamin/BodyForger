package app.bodyforger.mobile.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.core.model.BodyGoal
import app.bodyforger.core.model.WeightUnit
import app.bodyforger.mobile.R
import app.bodyforger.mobile.stats.GoalProgress
import app.bodyforger.mobile.stats.GoalStanding
import app.bodyforger.mobile.ui.theme.AmberGold
import app.bodyforger.mobile.ui.theme.NeonLime
import app.bodyforger.mobile.ui.theme.TextMuted
import app.bodyforger.mobile.ui.theme.TextPrimary
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * The milestones, as the settings screen lists them.
 *
 * A goal shows what it can and says what it cannot. Before a first weigh-in it shows its
 * target alone — there is no direction to report and none is invented. Once anchored it gains
 * its starting point, its progress and, where the trend allows, a projected date.
 */
@Composable
fun GoalsSection(
    standings: List<GoalStanding>,
    unit: WeightUnit,
    onAdd: () -> Unit,
    onRemove: (BodyGoal) -> Unit,
    onToggleValidated: (BodyGoal, Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (standings.isEmpty()) {
            Text(
                text = stringResource(R.string.goals_empty),
                color = TextMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        standings.forEach { standing ->
            GoalRow(
                standing = standing,
                unit = unit,
                onRemove = { onRemove(standing.goal) },
                onToggleValidated = { onToggleValidated(standing.goal, !standing.goal.isValidated) }
            )
        }

        TextButton(onClick = onAdd, modifier = Modifier.padding(top = 4.dp)) {
            Text(
                text = stringResource(R.string.goals_add),
                color = NeonLime,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun formatGoalsSummary(standings: List<GoalStanding>, unit: WeightUnit): String =
    standings.firstOrNull { !it.goal.isValidated }?.let { standing ->
        standing.goal.targetBodyFatPercentage
            ?.let { fat ->
                stringResource(
                    R.string.goals_target_mass_and_fat,
                    unit.formatWithSymbol(standing.goal.targetMassKg),
                    fat
                )
            }
            ?: stringResource(
                R.string.goals_target_mass,
                unit.formatWithSymbol(standing.goal.targetMassKg)
            )
    } ?: stringResource(R.string.settings_goals_none)

@Composable
private fun GoalRow(
    standing: GoalStanding,
    unit: WeightUnit,
    onRemove: () -> Unit,
    onToggleValidated: () -> Unit
) {
    val goal = standing.goal
    val dates = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleValidated)
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = goal.targetBodyFatPercentage
                    ?.let {
                        stringResource(
                            R.string.goals_target_mass_and_fat,
                            unit.formatWithSymbol(goal.targetMassKg),
                            it
                        )
                    }
                    ?: stringResource(R.string.goals_target_mass, unit.formatWithSymbol(goal.targetMassKg)),
                color = if (goal.isValidated) NeonLime else TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = standing.subtitle(dates),
                color = if (standing.hasDriftedBack) AmberGold else TextMuted,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        IconButton(onClick = onRemove) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.goals_remove),
                tint = TextMuted
            )
        }
    }
}

/**
 * One line saying where this goal stands, chosen for what the history can actually support.
 *
 * The order matters: a validated goal reports its date first, a drifting one says so, and an
 * unanchored one admits it has nothing to report rather than showing a progress of zero.
 */
@Composable
private fun GoalStanding.subtitle(dates: DateTimeFormatter): String = when {
    goal.isValidated && hasDriftedBack ->
        stringResource(R.string.goals_validated_but_drifted, goal.validatedOn!!.format(dates))

    goal.isValidated ->
        stringResource(R.string.goals_validated_on, goal.validatedOn!!.format(dates))

    !goal.hasStarted ->
        stringResource(R.string.goals_awaiting_first_weigh_in)

    weighInsThisWeek < GoalProgress.WEIGH_INS_FOR_A_VALID_WEEK ->
        stringResource(
            R.string.goals_week_incomplete,
            weighInsThisWeek,
            GoalProgress.WEIGH_INS_FOR_A_VALID_WEEK
        )

    projectedDate != null && goal.horizonDate != null -> stringResource(
        R.string.goals_projection_against_horizon,
        projectedDate.format(dates),
        goal.horizonDate!!.format(dates)
    )

    projectedDate != null ->
        stringResource(R.string.goals_projection, projectedDate.format(dates))

    else -> stringResource(R.string.goals_no_trend_yet)
}
