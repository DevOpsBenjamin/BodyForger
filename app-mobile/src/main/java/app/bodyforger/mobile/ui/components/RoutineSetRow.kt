package app.bodyforger.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.core.model.RoutineSet
import app.bodyforger.core.model.RoutineSetType
import app.bodyforger.mobile.R
import app.bodyforger.mobile.ui.text.badge
import app.bodyforger.mobile.ui.theme.AmberGold
import app.bodyforger.mobile.ui.theme.CrimsonRed
import app.bodyforger.mobile.ui.theme.ElectricCyan
import app.bodyforger.mobile.ui.theme.NeonLime
import app.bodyforger.mobile.ui.theme.SurfaceBorder
import app.bodyforger.mobile.ui.theme.SurfaceElevated
import app.bodyforger.mobile.ui.theme.TextMuted
import app.bodyforger.mobile.ui.theme.TextPrimary

@Composable
fun RoutineSetRow(
    setIndex: Int,
    routineSet: RoutineSet,
    canDelete: Boolean,
    onOpenSetTypeDialog: () -> Unit,
    onWeightChanged: (Double?) -> Unit,
    onRepsChanged: (Int?) -> Unit,
    onMinRepsChanged: (Int?) -> Unit,
    onMaxRepsChanged: (Int?) -> Unit,
    onDeleteSet: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    when (routineSet.type) {
                        RoutineSetType.NORMAL -> SurfaceElevated
                        RoutineSetType.WARMUP -> AmberGold.copy(alpha = 0.2f)
                        RoutineSetType.DROPSET -> ElectricCyan.copy(alpha = 0.2f)
                        RoutineSetType.FAILURE -> CrimsonRed.copy(alpha = 0.2f)
                        RoutineSetType.REST_PAUSE -> NeonLime.copy(alpha = 0.2f)
                    }
                )
                .border(
                    1.dp,
                    when (routineSet.type) {
                        RoutineSetType.NORMAL -> SurfaceBorder
                        RoutineSetType.WARMUP -> AmberGold
                        RoutineSetType.DROPSET -> ElectricCyan
                        RoutineSetType.FAILURE -> CrimsonRed
                        RoutineSetType.REST_PAUSE -> NeonLime
                    },
                    RoundedCornerShape(8.dp)
                )
                .clickable { onOpenSetTypeDialog() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (routineSet.type == RoutineSetType.NORMAL) "${setIndex + 1}" else routineSet.type.badge(),
                color = when (routineSet.type) {
                    RoutineSetType.NORMAL -> TextPrimary
                    RoutineSetType.WARMUP -> AmberGold
                    RoutineSetType.DROPSET -> ElectricCyan
                    RoutineSetType.FAILURE -> CrimsonRed
                    RoutineSetType.REST_PAUSE -> NeonLime
                },
                fontSize = if (routineSet.type.badge().length > 2) 9.sp else 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        val weightStr = routineSet.targetWeightKg?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: ""
        CompactNumberInput(
            value = weightStr,
            onValueChange = { input -> onWeightChanged(input.toDoubleOrNull()) },
            placeholder = "-",
            isDecimal = true,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        if (!routineSet.isRepsRange) {
            val repsStr = routineSet.reps?.toString() ?: ""
            CompactNumberInput(
                value = repsStr,
                onValueChange = { input -> onRepsChanged(input.toIntOrNull()) },
                placeholder = "10",
                isDecimal = false,
                modifier = Modifier.weight(1.5f)
            )
        } else {
            Row(
                modifier = Modifier.weight(1.5f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val minStr = routineSet.minReps?.toString() ?: ""
                val maxStr = routineSet.maxReps?.toString() ?: ""

                CompactNumberInput(
                    value = minStr,
                    onValueChange = { input -> onMinRepsChanged(input.toIntOrNull()) },
                    placeholder = "8",
                    isDecimal = false,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = stringResource(R.string.routine_editor_reps_to),
                    color = TextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                CompactNumberInput(
                    value = maxStr,
                    onValueChange = { input -> onMaxRepsChanged(input.toIntOrNull()) },
                    placeholder = "12",
                    isDecimal = false,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        IconButton(
            onClick = { if (canDelete) onDeleteSet() },
            enabled = canDelete,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Supprimer série",
                tint = if (canDelete) TextMuted else Color.Transparent,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}
