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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.core.model.RoutineSetType
import app.bodyforger.core.model.UnilateralSide
import app.bodyforger.core.model.WeightUnit
import app.bodyforger.core.model.WorkoutSet
import app.bodyforger.mobile.R
import app.bodyforger.mobile.ui.text.badge
import app.bodyforger.mobile.ui.theme.AmberGold
import app.bodyforger.mobile.ui.theme.ElectricCyan
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
    onOpenOptions: () -> Unit,
    lastPerformance: WorkoutSet?,
    onRepeatLastPerformance: () -> Unit,
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
            modifier = Modifier.width(44.dp).clickable(onClick = onOpenOptions)
        ) {
            val labelText = if (set.side != UnilateralSide.NONE) {
                "${set.setIndex}${set.side.badge()}"
            } else {
                set.setIndex.toString()
            }

            Text(
                text = labelText,
                color = if (isCompleted) NeonLime else TextPrimary,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp
            )

            if (set.type != RoutineSetType.NORMAL) {
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = set.type.badge(),
                    color = AmberGold,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 2. What was lifted last time, which one tap copies in here.
        PreviousPerformance(
            previous = lastPerformance,
            weightUnit = weightUnit,
            onRepeat = onRepeatLastPerformance
        )

        // 3. Load entry field (CompactNumberInput, which does not clip)
        // The athlete types what the machine shows; the set stores kilograms. Without the
        // conversion the unit was decorative — 70 on a pound-graduated stack was written as
        // 70 kg and the session tonnage came out more than twice what was lifted.
        val weightText = formatLoad(weightUnit.fromKilograms(set.weightKg))

        CompactNumberInput(
            value = weightText,
            onValueChange = { newVal ->
                val parsed = newVal.replace(',', '.').toDoubleOrNull()
                if (parsed != null) onWeightChange(weightUnit.toKilograms(parsed))
            },
            placeholder = "0",
            modifier = Modifier.width(64.dp)
        )

        CompactNumberInput(
            value = if (set.reps > 0) set.reps.toString() else "",
            onValueChange = { newVal ->
                val parsed = newVal.toIntOrNull()
                if (parsed != null) onRepsChange(parsed)
            },
            placeholder = "0",
            modifier = Modifier.width(52.dp)
        )

        // 4. Validation button / interactive checkbox
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
                    contentDescription = stringResource(R.string.cd_set_validated),
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(text = "✓", color = TextMuted, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * The reference from last time, always in view rather than looked up.
 *
 * A tap carries it into the fields, overwriting whatever they hold — the athlete asking for
 * last session's numbers means they want them, not a merge with what is typed.
 */
@Composable
private fun PreviousPerformance(
    previous: WorkoutSet?,
    weightUnit: WeightUnit,
    onRepeat: () -> Unit
) {
    val label = previous?.let {
        "${formatLoad(weightUnit.fromKilograms(it.weightKg))}${weightUnit.symbol} × ${it.reps}"
    }

    Box(
        modifier = Modifier
            .width(PREVIOUS_COLUMN_WIDTH)
            .then(if (previous != null) Modifier.clickable(onClick = onRepeat) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label ?: NO_PREVIOUS_PERFORMANCE,
            color = if (previous != null) ElectricCyan else TextMuted,
            fontSize = 11.sp,
            fontWeight = if (previous != null) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1
        )
    }
}

/** Trailing zero dropped, and rounded to a tenth: a stack is picked, not measured. */
private fun formatLoad(value: Double): String {
    val rounded = kotlin.math.round(value * 10) / 10
    return if (rounded % 1.0 == 0.0) rounded.toInt().toString() else rounded.toString()
}

/** Shown when the exercise has never been completed before: there is nothing to repeat. */
private const val NO_PREVIOUS_PERFORMANCE = "—"

private val PREVIOUS_COLUMN_WIDTH = 60.dp
