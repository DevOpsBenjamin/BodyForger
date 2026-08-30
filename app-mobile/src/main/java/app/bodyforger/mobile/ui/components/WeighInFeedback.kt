package app.bodyforger.mobile.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.mobile.R
import app.bodyforger.mobile.scale.ScaleUiState
import app.bodyforger.mobile.ui.theme.AmberGold
import app.bodyforger.mobile.ui.theme.ElectricCyan
import app.bodyforger.mobile.ui.theme.NeonLime
import app.bodyforger.mobile.ui.theme.SurfaceDark
import app.bodyforger.mobile.ui.theme.TextPrimary
import app.bodyforger.mobile.ui.theme.TextSecondary

/**
 * What the scale is asking for, while it is asking.
 *
 * A weigh-in is a conversation with a device the athlete is standing on: step on, hold the
 * handle, wait. Saying nothing would leave them guessing why nothing happens.
 */
@Composable
fun WeighInFeedback(state: ScaleUiState, onDismiss: () -> Unit) {
    val failure = state.failure
    val awaiting = state.weightAwaitingBodyFat

    when {
        state.isWeighing -> Feedback(
            title = stringResource(R.string.weigh_in_running),
            tint = ElectricCyan,
            showsSpinner = true,
            body = state.progress?.instructions.orEmpty().joinToString("\n") { instructionLabel(it) },
            onDismiss = null
        )

        failure != null -> Feedback(
            title = stringResource(R.string.weigh_in_failed),
            tint = AmberGold,
            body = failureLabel(failure),
            onDismiss = onDismiss
        )

        awaiting != null -> Feedback(
            title = stringResource(R.string.weigh_in_weight_only),
            tint = NeonLime,
            body = stringResource(R.string.weigh_in_weight_only_body, awaiting),
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun Feedback(
    title: String,
    tint: androidx.compose.ui.graphics.Color,
    body: String,
    onDismiss: (() -> Unit)?,
    showsSpinner: Boolean = false
) {
    AlertDialog(
        onDismissRequest = { onDismiss?.invoke() },
        containerColor = SurfaceDark,
        title = { Text(title, color = tint, fontSize = 15.sp, fontWeight = FontWeight.Black) },
        text = {
            Column {
                if (showsSpinner) {
                    CircularProgressIndicator(color = tint, modifier = Modifier.height(24.dp))
                    Spacer(Modifier.height(12.dp))
                }
                if (body.isNotBlank()) {
                    Text(body, color = TextSecondary, fontSize = 13.sp, lineHeight = 19.sp)
                }
            }
        },
        confirmButton = {
            if (onDismiss != null) {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = stringResource(R.string.action_close),
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    )
}
