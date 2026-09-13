package app.bodyforger.mobile.ui.components

import androidx.annotation.StringRes
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
 * Playing the first-run guidance again — the screen tour, the setup questions, or both.
 *
 * Neither re-offers itself once it has been answered, and skipping counts as answering, so
 * this is the only way back to them. Someone who skipped the tour on the first evening and
 * wants it a week later has nowhere else to ask.
 *
 * They are replayed apart because they are recorded apart: coming back for the tour is no
 * reason to be asked again for a name and a pair of units already given.
 */
@Composable
fun OnboardingSettingsSection(
    onReplayTour: () -> Unit,
    onReplaySetup: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.settings_onboarding_tour_why),
            color = TextMuted,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        ReplayButton(R.string.settings_onboarding_replay_tour, onReplayTour)
        ReplayButton(R.string.settings_onboarding_replay_setup, onReplaySetup)
    }
}

@Composable
private fun ReplayButton(@StringRes labelRes: Int, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonLime),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Text(text = stringResource(labelRes), fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
