package app.bodyforger.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.mobile.R
import app.bodyforger.mobile.ui.theme.AmberGold
import app.bodyforger.mobile.ui.theme.NeonLime
import app.bodyforger.mobile.ui.theme.SurfaceBorder
import app.bodyforger.mobile.ui.theme.SurfaceDark
import app.bodyforger.mobile.ui.theme.TextMuted
import app.bodyforger.mobile.ui.theme.TextPrimary

/**
 * One thing to set up, and whether it is done.
 *
 * [blocking] marks what the app cannot work without. The guide stays only while one of those
 * is missing — a goal is worth suggesting to someone who is setting up, and is no reason to
 * keep a card on the screen of someone who simply does not want one.
 */
data class SetupStep(
    val titleRes: Int,
    val reasonRes: Int,
    val isDone: Boolean,
    val blocking: Boolean,
    val onOpen: () -> Unit
)

/**
 * What to fill in first, and why.
 *
 * The order is not a matter of taste: it is a dependency chain. Weighing is impossible without
 * a measurement profile, because the scale computes its own figures from it. Pairing engraves
 * that profile into the scale. A goal only becomes readable once there is a weigh-in to read
 * it against.
 *
 * Four sections of equal weight tell a new athlete nothing about which one unblocks the rest.
 */
@Composable
fun SetupGuide(steps: List<SetupStep>, modifier: Modifier = Modifier) {
    if (steps.none { it.blocking && !it.isDone }) return

    val done = steps.count { it.isDone }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, AmberGold.copy(alpha = 0.35f), RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.setup_guide_title),
                color = AmberGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(
                text = stringResource(R.string.setup_guide_progress, done, steps.size),
                color = TextMuted,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
            )

            steps.forEach { step -> StepRow(step) }
        }
    }
}

@Composable
private fun StepRow(step: SetupStep) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !step.isDone, onClick = step.onOpen)
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(if (step.isDone) NeonLime.copy(alpha = 0.2f) else SurfaceBorder),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (step.isDone) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = NeonLime,
                    modifier = Modifier.size(13.dp)
                )
            }
        }

        Column {
            Text(
                text = stringResource(step.titleRes),
                color = if (step.isDone) TextMuted else TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            // The reason is what makes the order self-explanatory rather than arbitrary.
            if (!step.isDone) {
                Text(
                    text = stringResource(step.reasonRes),
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}
