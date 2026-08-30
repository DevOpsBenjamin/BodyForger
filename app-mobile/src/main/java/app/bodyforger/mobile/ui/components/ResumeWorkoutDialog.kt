package app.bodyforger.mobile.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.core.model.WorkoutSession
import app.bodyforger.mobile.ui.theme.ElectricCyan
import app.bodyforger.mobile.ui.theme.SurfaceDark
import app.bodyforger.mobile.ui.theme.TextPrimary
import app.bodyforger.mobile.ui.theme.TextSecondary

/**
 * Offers to pick up a session left open by a previous run.
 *
 * Discarding marks it as such rather than deleting it: an abandoned session is information,
 * and the sets already done stay in the history.
 */
@Composable
fun ResumeWorkoutDialog(
    session: WorkoutSession,
    onResume: () -> Unit,
    onDiscard: () -> Unit
) {
    val completedSets = session.sets.count { it.isCompleted }

    AlertDialog(
        onDismissRequest = {},
        containerColor = SurfaceDark,
        title = {
            Text(
                text = "Séance interrompue",
                color = TextPrimary,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                Text(session.title, color = ElectricCyan, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    text = if (completedSets > 0) {
                        "$completedSets série(s) déjà validée(s) ont été conservées."
                    } else {
                        "Aucune série n'avait encore été validée."
                    },
                    color = TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onResume) {
                Text("Reprendre", color = ElectricCyan, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDiscard) {
                Text("Abandonner", color = TextSecondary)
            }
        }
    )
}
