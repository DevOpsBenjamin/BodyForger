package app.bodyforger.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.core.model.RoutineSetType
import app.bodyforger.core.model.UnilateralSide
import app.bodyforger.core.model.WeightUnit
import app.bodyforger.core.model.WorkoutSet
import app.bodyforger.mobile.ui.theme.AmberGold
import app.bodyforger.mobile.ui.theme.NeonLime
import app.bodyforger.mobile.ui.theme.SurfaceBorder
import app.bodyforger.mobile.ui.theme.SurfaceElevated
import app.bodyforger.mobile.ui.theme.TextMuted
import app.bodyforger.mobile.ui.theme.TextPrimary

@Composable
fun LiveWorkoutSetRow(
    set: WorkoutSet,
    weightUnit: WeightUnit,
    onToggleCompleted: () -> Unit,
    onWeightChange: (Double) -> Unit,
    onRepsChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isCompleted = set.isCompleted
    val rowBgColor = if (isCompleted) NeonLime.copy(alpha = 0.08f) else SurfaceElevated
    val rowBorderColor = if (isCompleted) NeonLime.copy(alpha = 0.4f) else SurfaceBorder

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(rowBgColor)
            .border(1.dp, rowBorderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.width(44.dp)
        ) {
            val labelText = if (set.side != UnilateralSide.NONE) {
                "${set.setIndex}${set.side.shortBadge}"
            } else {
                set.setIndex.toString()
            }

            Text(
                text = labelText,
                color = if (isCompleted) NeonLime else TextPrimary,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp
            )

            if (set.type == RoutineSetType.WARMUP) {
                Spacer(modifier = Modifier.width(3.dp))
                Text(text = "ÉCH", color = AmberGold, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            } else if (set.type == RoutineSetType.DROPSET) {
                Spacer(modifier = Modifier.width(3.dp))
                Text(text = "DROP", color = AmberGold, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
        }

        // 2. Champ Saisie Charge (CompactNumberInput anti-troncature)
        val weightText = if (set.weightKg % 1.0 == 0.0) {
            set.weightKg.toInt().toString()
        } else {
            set.weightKg.toString()
        }

        CompactNumberInput(
            value = weightText,
            onValueChange = { newVal ->
                val parsed = newVal.replace(',', '.').toDoubleOrNull()
                if (parsed != null) onWeightChange(parsed)
            },
            placeholder = "0",
            modifier = Modifier.width(72.dp)
        )

        CompactNumberInput(
            value = if (set.reps > 0) set.reps.toString() else "",
            onValueChange = { newVal ->
                val parsed = newVal.toIntOrNull()
                if (parsed != null) onRepsChange(parsed)
            },
            placeholder = "0",
            modifier = Modifier.width(60.dp)
        )

        // 4. Bouton de Validation / Checkbox interactif
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isCompleted) NeonLime else SurfaceBorder.copy(alpha = 0.4f))
                .border(1.dp, if (isCompleted) NeonLime else SurfaceBorder, RoundedCornerShape(8.dp))
                .clickable { onToggleCompleted() },
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Validé",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(text = "✓", color = TextMuted, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
