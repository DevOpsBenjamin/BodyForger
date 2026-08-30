package app.bodyforger.mobile.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.core.model.WorkoutSession
import app.bodyforger.mobile.R
import app.bodyforger.mobile.ui.theme.AmberGold
import app.bodyforger.mobile.ui.theme.ElectricCyan
import app.bodyforger.mobile.ui.theme.NeonLime
import app.bodyforger.mobile.ui.theme.SurfaceDark
import app.bodyforger.mobile.ui.theme.TextPrimary
import app.bodyforger.mobile.ui.theme.TextSecondary

/**
 * Offers what to do with a session left open by a previous run.
 *
 * Three ways out, because the sets already logged were really performed: carry on, close the
 * session on what was done, or throw it away. Silently keeping it out of sight would lose the
 * work either way, which is what the second choice exists to prevent.
 */
@Composable
fun ResumeWorkoutDialog(
    session: WorkoutSession,
    onResume: () -> Unit,
    onFinishAsIs: () -> Unit,
    onDelete: () -> Unit
) {
    val completedSets = session.sets.count { it.isCompleted }

    AlertDialog(
        onDismissRequest = {},
        containerColor = SurfaceDark,
        title = {
            Text(
                text = stringResource(R.string.resume_workout_title),
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
                        stringResource(R.string.resume_workout_sets_done, completedSets)
                    } else {
                        stringResource(R.string.resume_workout_no_sets)
                    },
                    color = TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Choice(R.string.resume_workout_resume, ElectricCyan, onResume)
                // Fermer sur ce qui a été fait : ces séries ont bien été effectuées.
                Choice(R.string.resume_workout_finish, NeonLime, onFinishAsIs, enabled = completedSets > 0)
                Choice(R.string.resume_workout_delete, AmberGold, onDelete)
            }
        }
    )
}

@Composable
private fun Choice(textRes: Int, tint: Color, onClick: () -> Unit, enabled: Boolean = true) {
    TextButton(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(textRes),
            color = if (enabled) tint else TextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black
        )
    }
}
