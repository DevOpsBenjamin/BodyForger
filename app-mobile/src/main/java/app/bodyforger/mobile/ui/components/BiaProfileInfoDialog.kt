package app.bodyforger.mobile.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.bodyforger.mobile.R
import app.bodyforger.mobile.ui.theme.ElectricCyan
import app.bodyforger.mobile.ui.theme.SurfaceDark
import app.bodyforger.mobile.ui.theme.TextSecondary

/** Explains what the scale does with the profile, and why an empty one blocks a weigh-in. */
@Composable
fun BiaProfileInfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Text(
                text = stringResource(R.string.settings_bia_info_title),
                color = ElectricCyan,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black
            )
        },
        text = {
            Text(
                text = stringResource(R.string.settings_bia_info_body),
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.action_close),
                    color = ElectricCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}
