package app.bodyforger.mobile.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.mobile.R
import app.bodyforger.mobile.ui.theme.NeonLime
import app.bodyforger.mobile.ui.theme.TextMuted

/**
 * Playing the first-run guidance again.
 *
 * The tour never re-offers itself once it has been answered — finished or skipped are the
 * same answer — so this is the only way back to it. Someone who skipped it on the first
 * evening and wants it a week later has nowhere else to ask.
 */
@Composable
fun OnboardingSettingsSection(
    onReplayTour: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.settings_onboarding_tour_why),
            color = TextMuted,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        OutlinedButton(
            onClick = onReplayTour,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonLime),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.settings_onboarding_replay_tour),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
