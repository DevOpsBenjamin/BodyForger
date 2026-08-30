package app.bodyforger.mobile.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.bodyforger.mobile.R
import app.bodyforger.mobile.ui.theme.AmberGold
import app.bodyforger.mobile.ui.theme.SurfaceDark
import app.bodyforger.mobile.ui.theme.TextPrimary
import app.bodyforger.mobile.ui.theme.TextSecondary

/**
 * Confirms giving up on a workout, because the gesture cannot be taken back.
 *
 * The body says what survives: the sets stay in the history marked discarded, out of every
 * statistic (ADR 001 §A — the history is append-only, an abandoned session is information).
 */
@Composable
fun DiscardWorkoutDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Text(
                text = stringResource(R.string.workout_live_discard_title),
                color = AmberGold,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black
            )
        },
        text = {
            Text(
                text = stringResource(R.string.workout_live_discard_body),
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.workout_live_discard_confirm),
                    color = AmberGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.action_cancel),
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}
