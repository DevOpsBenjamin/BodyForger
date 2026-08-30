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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
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
import app.bodyforger.core.model.EquipmentType
import app.bodyforger.core.model.MuscleGroup
import app.bodyforger.mobile.ui.text.label
import app.bodyforger.mobile.ui.theme.AmberGold
import app.bodyforger.mobile.ui.theme.ElectricCyan
import app.bodyforger.mobile.ui.theme.NeonLime
import app.bodyforger.mobile.ui.theme.SurfaceBorder
import app.bodyforger.mobile.ui.theme.SurfaceElevated
import app.bodyforger.mobile.ui.theme.TextPrimary
import app.bodyforger.mobile.ui.theme.TextSecondary

@Composable
fun CatalogFilterBar(
    filterCustomOnly: Boolean,
    onToggleCustomOnly: () -> Unit,
    filterUnilateralOnly: Boolean,
    onToggleUnilateralOnly: () -> Unit,
    selectedMuscles: Set<MuscleGroup>,
    onOpenMuscleDialog: () -> Unit,
    selectedEquipments: Set<EquipmentType>,
    onOpenEquipmentDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        // Filtre 1 : 👤 Perso
        item {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (filterCustomOnly) NeonLime else SurfaceElevated)
                    .border(1.dp, if (filterCustomOnly) NeonLime else SurfaceBorder, RoundedCornerShape(8.dp))
                    .clickable { onToggleCustomOnly() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = if (filterCustomOnly) Color.Black else NeonLime,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Perso",
                        color = if (filterCustomOnly) Color.Black else TextPrimary,
                        fontWeight = if (filterCustomOnly) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 11.sp
                    )
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (filterUnilateralOnly) AmberGold else SurfaceElevated)
                    .border(1.dp, if (filterUnilateralOnly) AmberGold else SurfaceBorder, RoundedCornerShape(8.dp))
                    .clickable { onToggleUnilateralOnly() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = if (filterUnilateralOnly) Color.Black else AmberGold,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "1 Côté",
                        color = if (filterUnilateralOnly) Color.Black else TextPrimary,
                        fontWeight = if (filterUnilateralOnly) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 11.sp
                    )
                }
            }
        }

        item {
            val hasMuscleFilter = selectedMuscles.isNotEmpty()
            val muscleLabel = when {
                selectedMuscles.isEmpty() -> "Muscles : Tous"
                selectedMuscles.size == 1 -> "Muscle : ${selectedMuscles.first().label()}"
                selectedMuscles.size == 2 -> "${selectedMuscles.elementAt(0).label()}, ${selectedMuscles.elementAt(1).label()}"
                else -> "Muscles (${selectedMuscles.size})"
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (hasMuscleFilter) NeonLime.copy(alpha = 0.2f) else SurfaceElevated)
                    .border(1.dp, if (hasMuscleFilter) NeonLime else SurfaceBorder, RoundedCornerShape(8.dp))
                    .clickable { onOpenMuscleDialog() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = muscleLabel,
                        color = if (hasMuscleFilter) NeonLime else TextPrimary,
                        fontWeight = if (hasMuscleFilter) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = if (hasMuscleFilter) NeonLime else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        item {
            val hasEquipFilter = selectedEquipments.isNotEmpty()
            val equipLabel = when {
                selectedEquipments.isEmpty() -> "Matériel : Tout"
                selectedEquipments.size == 1 -> "Matériel : ${selectedEquipments.first().label()}"
                selectedEquipments.size == 2 -> "${selectedEquipments.elementAt(0).label()}, ${selectedEquipments.elementAt(1).label()}"
                else -> "Matériel (${selectedEquipments.size})"
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (hasEquipFilter) ElectricCyan.copy(alpha = 0.2f) else SurfaceElevated)
                    .border(1.dp, if (hasEquipFilter) ElectricCyan else SurfaceBorder, RoundedCornerShape(8.dp))
                    .clickable { onOpenEquipmentDialog() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = equipLabel,
                        color = if (hasEquipFilter) ElectricCyan else TextPrimary,
                        fontWeight = if (hasEquipFilter) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = if (hasEquipFilter) ElectricCyan else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
