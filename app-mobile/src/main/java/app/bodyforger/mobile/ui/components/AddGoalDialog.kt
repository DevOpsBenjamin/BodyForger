package app.bodyforger.mobile.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import app.bodyforger.mobile.R
import app.bodyforger.mobile.stats.GoalProgress
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
@Composable
fun AddGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (massKg: Double, bodyFat: Double?, horizon: LocalDate?) -> Unit
) {
    var massText by remember { mutableStateOf("") }
    var fatText by remember { mutableStateOf("") }
    var horizonMonths by remember { mutableStateOf<Int?>(null) }

    val massKg = massText.replace(',', '.').toDoubleOrNull()
    val bodyFat = fatText.replace(',', '.').toDoubleOrNull()

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
            Column {
                OutlinedTextField(
                    value = massText,
                    onValueChange = { massText = it },
                    label = { Text(stringResource(R.string.goals_dialog_mass)) },
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

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    HorizonChip(null, horizonMonths) { horizonMonths = null }
                    listOf(6, 12, 24).forEach { months ->
                        HorizonChip(months, horizonMonths) { horizonMonths = months }
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
                        horizonMonths?.let { LocalDate.now().plusMonths(it.toLong()) }
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

@Composable
private fun HorizonChip(months: Int?, selected: Int?, onClick: () -> Unit) {
    FilterChip(
        selected = months == selected,
        onClick = onClick,
        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NeonLime.copy(alpha = 0.2f)),
        label = {
            Text(
                text = months?.let { stringResource(R.string.goals_dialog_horizon_months, it) }
                    ?: stringResource(R.string.goals_dialog_horizon_none),
                fontSize = 11.sp
            )
        }
    )
}
