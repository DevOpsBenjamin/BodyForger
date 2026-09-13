package app.bodyforger.mobile.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.core.model.Routine
import app.bodyforger.mobile.R
import app.bodyforger.mobile.ui.theme.CrimsonRed
import app.bodyforger.mobile.ui.theme.SurfaceDark
import app.bodyforger.mobile.ui.theme.TextPrimary
import app.bodyforger.mobile.ui.theme.TextSecondary

@Composable
fun DeleteRoutineDialog(
    routine: Routine,
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Text(
                text = stringResource(R.string.dialog_delete_routine_title),
                color = CrimsonRed,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )
        },
        text = {
            Text(
                text = stringResource(R.string.dialog_delete_routine_msg),
                color = TextPrimary,
                fontSize = 13.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed, contentColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = stringResource(R.string.action_delete), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_cancel), color = TextSecondary)
            }
        }
    )
}
