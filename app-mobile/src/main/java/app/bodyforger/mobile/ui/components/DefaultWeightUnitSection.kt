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
import app.bodyforger.core.model.WeightUnit
import app.bodyforger.mobile.R
import app.bodyforger.mobile.ui.text.label
import app.bodyforger.mobile.ui.theme.NeonLime
import app.bodyforger.mobile.ui.theme.TextMuted
import app.bodyforger.mobile.ui.theme.TextPrimary

/**
 * The unit a new exercise starts from.
 *
 * A starting point, not a constraint: an exercise keeps its own unit, so a machine labelled in
 * pounds stays in pounds inside a routine counted in kilograms. Changing this rewrites nothing
 * already logged.
 */
@Composable
fun DefaultWeightUnitSection(
    selected: WeightUnit,
    onSelect: (WeightUnit) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        WeightUnit.entries.forEach { unit ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(unit) }
                    .padding(vertical = 6.dp)
            ) {
                RadioButton(
                    selected = unit == selected,
                    onClick = { onSelect(unit) },
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
            text = stringResource(R.string.settings_default_unit_hint),
            color = TextMuted,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}