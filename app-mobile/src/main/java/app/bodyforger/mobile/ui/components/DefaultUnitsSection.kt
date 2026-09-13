package app.bodyforger.mobile.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.core.model.HeightUnit
import app.bodyforger.core.model.WeightUnit
import app.bodyforger.mobile.R
import app.bodyforger.mobile.ui.text.label
import app.bodyforger.mobile.ui.theme.NeonLime
import app.bodyforger.mobile.ui.theme.TextMuted
import app.bodyforger.mobile.ui.theme.TextPrimary

/**
 * The units the athlete reads and writes in.
 *
 * Weight is a starting point, not a constraint: an exercise keeps its own unit, so a machine
 * labelled in pounds stays in pounds inside a routine counted in kilograms, and changing this
 * rewrites nothing already logged.
 *
 * Height is purely a matter of reading and typing — it is stored in centimetres whatever is
 * chosen here, because that is what the composition equations and the scale both expect.
 */
@Composable
fun DefaultUnitsSection(
    weightUnit: WeightUnit,
    heightUnit: HeightUnit,
    onSelectWeight: (WeightUnit) -> Unit,
    onSelectHeight: (HeightUnit) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.settings_units_weight),
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        WeightUnit.entries.forEach { unit ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectWeight(unit) }
                    .padding(vertical = 6.dp)
            ) {
                RadioButton(
                    selected = unit == weightUnit,
                    onClick = { onSelectWeight(unit) },
                    colors = RadioButtonDefaults.colors(selectedColor = NeonLime, unselectedColor = TextMuted)
                )
                Text(
                    text = unit.label(),
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
        Text(
            text = stringResource(R.string.settings_units_height),
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 12.dp)
        )
        HeightUnit.entries.forEach { unit ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectHeight(unit) }
                    .padding(vertical = 6.dp)
            ) {
                RadioButton(
                    selected = unit == heightUnit,
                    onClick = { onSelectHeight(unit) },
                    colors = RadioButtonDefaults.colors(selectedColor = NeonLime, unselectedColor = TextMuted)
                )
                Text(
                    text = unit.label(),
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        Text(
            text = stringResource(R.string.settings_units_hint),
            color = TextMuted,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}