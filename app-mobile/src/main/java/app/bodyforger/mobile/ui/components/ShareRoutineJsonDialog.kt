package app.bodyforger.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.core.model.Routine
import app.bodyforger.mobile.R
import app.bodyforger.mobile.ui.theme.AmberGold
import app.bodyforger.mobile.ui.theme.ElectricCyan
import app.bodyforger.mobile.ui.theme.SurfaceDark
import app.bodyforger.mobile.ui.theme.SurfaceElevated
import app.bodyforger.mobile.ui.theme.TextPrimary

@Composable
fun ShareRoutineJsonDialog(
    routine: Routine,
    onDismiss: () -> Unit
) {
    val jsonString = """
        {
          "id": "${routine.id}",
          "name": "${routine.name}",
          "notes": "${routine.notes}",
          "assignedDays": [${routine.assignedDays.joinToString(",")}],
          "exercises": [
            ${routine.exercises.joinToString(",\n    ") { ex ->
        """{"name": "${ex.exerciseName}", "sets": ${ex.sets.size}, "rest": ${ex.restTimeSeconds}}"""
    }}
          ]
        }
    """.trimIndent()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Text(
                text = "📤 PARTAGE DE ROUTINE (JSON)",
                color = AmberGold,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )
        },
        text = {
            Column {
                Text(text = "Routine : ${routine.name}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceElevated)
                        .padding(10.dp)
                ) {
                    Text(
                        text = jsonString,
                        color = ElectricCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = stringResource(R.string.action_close), fontWeight = FontWeight.Bold)
            }
        }
    )
}
