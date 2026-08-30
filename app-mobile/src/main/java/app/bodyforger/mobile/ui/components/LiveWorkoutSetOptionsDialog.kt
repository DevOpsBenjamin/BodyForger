package app.bodyforger.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.core.model.RoutineSetType
import app.bodyforger.mobile.ui.text.label
import app.bodyforger.mobile.ui.theme.AmberGold
import app.bodyforger.mobile.ui.theme.ElectricCyan
import app.bodyforger.mobile.ui.theme.NeonLime
import app.bodyforger.mobile.ui.theme.SurfaceBorder
import app.bodyforger.mobile.ui.theme.SurfaceDark
import app.bodyforger.mobile.ui.theme.SurfaceElevated
import app.bodyforger.mobile.ui.theme.TextMuted
import app.bodyforger.mobile.ui.theme.TextPrimary

/**
 * What can be done to one set mid-workout: change what it is, or drop it.
 *
 * Reached by tapping the set number, the same gesture as in the routine editor.
 */
@Composable
fun LiveWorkoutSetOptionsDialog(
    setIndexDisplay: Int,
    currentType: RoutineSetType,
    canDelete: Boolean,
    onTypeSelected: (RoutineSetType) -> Unit,
    onDeleteSet: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Text(
                text = "SÉRIE $setIndexDisplay",
                color = NeonLime,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )
        },
        text = {
            Column {
                RoutineSetType.entries.forEach { type ->
                    SetTypeChoice(
                        type = type,
                        isSelected = type == currentType,
                        onClick = {
                            onTypeSelected(type)
                            onDismiss()
                        }
                    )
                    Spacer(Modifier.height(6.dp))
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDeleteSet()
                    onDismiss()
                },
                enabled = canDelete
            ) {
                Text(
                    text = if (canDelete) "SUPPRIMER LA SÉRIE" else "DERNIÈRE SÉRIE",
                    color = if (canDelete) AmberGold else TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "FERMER", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun SetTypeChoice(type: RoutineSetType, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceElevated, RoundedCornerShape(10.dp))
            .border(
                width = 1.dp,
                color = if (isSelected) ElectricCyan else SurfaceBorder,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            text = type.label(),
            color = if (isSelected) ElectricCyan else TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
