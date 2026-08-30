package app.bodyforger.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.core.model.MuscleGroup
import app.bodyforger.mobile.ui.text.label
import app.bodyforger.mobile.ui.theme.ElectricCyan
import app.bodyforger.mobile.ui.theme.SurfaceBorder
import app.bodyforger.mobile.ui.theme.SurfaceElevated
import app.bodyforger.mobile.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SecondaryMuscleSelectorFlow(
    excludedMuscle: MuscleGroup,
    selectedSecondaryMuscles: Set<MuscleGroup>,
    onToggleMuscle: (MuscleGroup) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        MuscleGroup.entries.filter { it != MuscleGroup.FULL_BODY && it != excludedMuscle }.forEach { muscle ->
            val isSelected = selectedSecondaryMuscles.contains(muscle)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) ElectricCyan.copy(alpha = 0.25f) else SurfaceElevated)
                    .border(1.dp, if (isSelected) ElectricCyan else SurfaceBorder, RoundedCornerShape(8.dp))
                    .clickable { onToggleMuscle(muscle) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                    Text(
                        text = muscle.label(),
                        color = if (isSelected) ElectricCyan else TextSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
