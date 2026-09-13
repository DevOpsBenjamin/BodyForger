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
import app.bodyforger.core.bia.ModelSelector
import app.bodyforger.mobile.R
import app.bodyforger.mobile.ui.theme.NeonLime
import app.bodyforger.mobile.ui.theme.TextMuted
import app.bodyforger.mobile.ui.theme.TextPrimary

/**
 * Picks which engine reads the raw resistances into a body composition.
 *
 * Shown only when the build carries more than one engine; with a single one there is nothing
 * to choose. Labels name the engines themselves — ForgeFit MIT and ForgeFit Private.
 */
@Composable
fun BiaEngineSection(
    engineIds: List<String>,
    selectedId: String,
    onSelect: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        engineIds.forEach { id ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(id) }
                    .padding(vertical = 6.dp)
            ) {
                RadioButton(
                    selected = id == selectedId,
                    onClick = { onSelect(id) },
                    colors = RadioButtonDefaults.colors(selectedColor = NeonLime, unselectedColor = TextMuted)
                )
                Column(modifier = Modifier.padding(start = 4.dp)) {
                    Text(
                        text = stringResource(engineLabel(id)),
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(engineDescription(id)),
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

private fun engineLabel(id: String): Int = when (id) {
    ModelSelector.FORGEFIT_PRIVATE -> R.string.settings_engine_forgefit_private
    else -> R.string.settings_engine_forgefit_mit
}

private fun engineDescription(id: String): Int = when (id) {
    ModelSelector.FORGEFIT_PRIVATE -> R.string.settings_engine_forgefit_private_desc
    else -> R.string.settings_engine_forgefit_mit_desc
}
