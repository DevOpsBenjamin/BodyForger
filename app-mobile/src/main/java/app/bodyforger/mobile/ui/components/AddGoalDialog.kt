package app.bodyforger.mobile.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.core.model.WeightUnit
import app.bodyforger.mobile.R
import app.bodyforger.mobile.stats.GoalProgress
import app.bodyforger.mobile.ui.theme.AmberGold
import app.bodyforger.mobile.ui.theme.NeonLime
import app.bodyforger.mobile.ui.theme.SurfaceDark
import app.bodyforger.mobile.ui.theme.TextMuted
import app.bodyforger.mobile.ui.theme.TextPrimary
import java.time.LocalDate

/**
 * Setting a milestone.
 *
 * Mass is the only thing asked for. Body fat narrows the goal to weigh-ins that measured it,
 * and an horizon is offered as a duration because that is how the athlete thinks — "in six
 * months" — while what gets stored is the date that duration lands on. Kept as a duration it
 * would retreat as fast as they approached it.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddGoalDialog(
    /** What the athlete types in. What gets stored is kilograms, converted here. */
    unit: WeightUnit,
    onDismiss: () -> Unit,
    onConfirm: (massKg: Double, bodyFat: Double?, horizon: LocalDate?) -> Unit
) {
    var massText by remember { mutableStateOf("") }
    var fatText by remember { mutableStateOf("") }
    var horizon by remember { mutableStateOf<HorizonChoice>(HorizonChoice.None) }
    var day by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }

    val massKg = massText.replace(',', '.').toDoubleOrNull()?.let { unit.toKilograms(it) }
    val bodyFat = fatText.replace(',', '.').toDoubleOrNull()
    val horizonDate = when (val choice = horizon) {
        HorizonChoice.None -> null
        HorizonChoice.OnADate -> futureDateFrom(day, month, year)
        is HorizonChoice.InMonths -> LocalDate.now().plusMonths(choice.months.toLong())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Text(
                text = stringResource(R.string.goals_dialog_title),
                color = TextPrimary,
                fontWeight = FontWeight.Black
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = massText,
                    onValueChange = { massText = it },
                    label = { Text(stringResource(R.string.goals_dialog_mass, unit.symbol)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = fatText,
                    onValueChange = { fatText = it },
                    label = { Text(stringResource(R.string.goals_dialog_fat)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )

                Text(
                    text = stringResource(R.string.goals_dialog_horizon),
                    color = TextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                )

                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    HorizonChip(
                        label = stringResource(R.string.goals_dialog_horizon_none),
                        selected = horizon == HorizonChoice.None
                    ) { horizon = HorizonChoice.None }

                    listOf(6, 12, 24).forEach { months ->
                        HorizonChip(
                            label = stringResource(R.string.goals_dialog_horizon_months, months),
                            selected = horizon == HorizonChoice.InMonths(months)
                        ) { horizon = HorizonChoice.InMonths(months) }
                    }

                    HorizonChip(
                        label = stringResource(R.string.goals_dialog_horizon_on_a_date),
                        selected = horizon == HorizonChoice.OnADate
                    ) { horizon = HorizonChoice.OnADate }
                }

                // Day, month, year, as the birth date is entered elsewhere: one date entry
                // style in the app rather than two.
                if (horizon == HorizonChoice.OnADate) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        CompactNumberInput(
                            value = day,
                            onValueChange = { day = it },
                            placeholder = stringResource(R.string.profile_day),
                            maxLength = 2,
                            modifier = Modifier.width(52.dp)
                        )
                        CompactNumberInput(
                            value = month,
                            onValueChange = { month = it },
                            placeholder = stringResource(R.string.profile_month),
                            maxLength = 2,
                            modifier = Modifier.width(52.dp)
                        )
                        CompactNumberInput(
                            value = year,
                            onValueChange = { year = it },
                            placeholder = stringResource(R.string.profile_year),
                            maxLength = 4,
                            modifier = Modifier.width(72.dp)
                        )
                    }

                    if (horizonDate == null && (day + month + year).isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.goals_dialog_horizon_must_be_future),
                            color = AmberGold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Text(
                    text = stringResource(
                        R.string.goals_dialog_hint,
                        GoalProgress.WEIGH_INS_FOR_A_VALID_WEEK,
                        GoalProgress.WEIGH_INS_FOR_A_SOLID_WEEK
                    ),
                    color = TextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = massKg != null && massKg > 0.0,
                onClick = {
                    onConfirm(
                        massKg ?: return@TextButton,
                        bodyFat?.takeIf { it in 0.0..100.0 },
                        horizonDate
                    )
                }
            ) {
                Text(stringResource(R.string.action_save), color = NeonLime, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel), color = TextMuted)
            }
        }
    )
}

/** How the athlete is filling the horizon in. Only the date it lands on is ever stored. */
private sealed interface HorizonChoice {
    data object None : HorizonChoice
    data object OnADate : HorizonChoice
    data class InMonths(val months: Int) : HorizonChoice
}

@Composable
private fun HorizonChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NeonLime.copy(alpha = 0.2f)),
        label = { Text(text = label, fontSize = 11.sp) }
    )
}

/** A horizon lies ahead: a date already past would be a milestone that arrived before it was set. */
private fun futureDateFrom(day: String, month: String, year: String): LocalDate? {
    val d = day.toIntOrNull() ?: return null
    val m = month.toIntOrNull() ?: return null
    val y = year.toIntOrNull() ?: return null
    return runCatching { LocalDate.of(y, m, d) }
        .getOrNull()
        ?.takeIf { it.isAfter(LocalDate.now()) }
}
