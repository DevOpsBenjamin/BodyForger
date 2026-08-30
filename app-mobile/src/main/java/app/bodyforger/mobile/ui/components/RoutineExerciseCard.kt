package app.bodyforger.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.core.model.RoutineExercise
import app.bodyforger.core.model.RoutineSet
import app.bodyforger.mobile.R
import app.bodyforger.mobile.ui.theme.AmberGold
import app.bodyforger.mobile.ui.theme.CrimsonRed
import app.bodyforger.mobile.ui.theme.ElectricCyan
import app.bodyforger.mobile.ui.theme.NeonLime
import app.bodyforger.mobile.ui.theme.SurfaceBorder
import app.bodyforger.mobile.ui.theme.SurfaceDark
import app.bodyforger.mobile.ui.theme.SurfaceElevated
import app.bodyforger.mobile.ui.theme.TextMuted
import app.bodyforger.mobile.ui.theme.TextPrimary
import app.bodyforger.mobile.ui.theme.TextSecondary

@Composable
fun RoutineExerciseCard(
    exerciseIndex: Int,
    totalExercises: Int,
    exercise: RoutineExercise,
    onOpenReorder: () -> Unit,
    onOpenWeightUnitPicker: () -> Unit,
    onReplace: () -> Unit,
    onRemove: () -> Unit,
    onOpenRestPicker: () -> Unit,
    onOpenSetTypeDialog: (setIndex: Int) -> Unit,
    onUpdateSet: (setIndex: Int, updatedSet: RoutineSet) -> Unit,
    onAddSet: () -> Unit,
    onDeleteSet: (setIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = exercise.primaryMuscle.displayName,
                            color = ElectricCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(text = "•", color = TextMuted, fontSize = 11.sp)
                        Text(
                            text = exercise.equipment.displayName,
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                        if (exercise.isUnilateral) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(AmberGold.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = null,
                                    tint = AmberGold,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = exercise.exerciseName,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = TextSecondary
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(SurfaceElevated)
                    ) {
                        DropdownMenuItem(
                            text = { Text("⇅ Réorganiser les exercices", color = TextPrimary) },
                            onClick = {
                                menuExpanded = false
                                onOpenReorder()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("⚖️ Unité : ${exercise.weightUnit.symbol.uppercase()}", color = ElectricCyan) },
                            onClick = {
                                menuExpanded = false
                                onOpenWeightUnitPicker()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.routine_editor_replace_exercise), color = ElectricCyan) },
                            onClick = {
                                menuExpanded = false
                                onReplace()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.routine_editor_remove_exercise), color = CrimsonRed) },
                            onClick = {
                                menuExpanded = false
                                onRemove()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceElevated)
                    .clickable { onOpenRestPicker() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                val restMin = exercise.restTimeSeconds / 60
                val restSec = exercise.restTimeSeconds % 60
                val restLabel = if (restSec == 0) "${restMin}min" else "${restMin}min ${restSec}s"
                Text(
                    text = "${stringResource(R.string.routine_editor_rest_prefix)}$restLabel",
                    color = ElectricCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.routine_editor_col_set),
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(34.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(SurfaceElevated.copy(alpha = 0.5f))
                        .clickable { onOpenWeightUnitPicker() }
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = exercise.weightUnit.symbol.uppercase(),
                        color = ElectricCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.routine_editor_col_reps),
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1.5f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(32.dp))
            }

            Spacer(modifier = Modifier.height(4.dp))

            exercise.sets.forEachIndexed { setIdx, setItem ->
                RoutineSetRow(
                    setIndex = setIdx,
                    routineSet = setItem,
                    canDelete = exercise.sets.size > 1,
                    onOpenSetTypeDialog = { onOpenSetTypeDialog(setIdx) },
                    onWeightChanged = { w -> onUpdateSet(setIdx, setItem.copy(targetWeightKg = w)) },
                    onRepsChanged = { r -> onUpdateSet(setIdx, setItem.copy(reps = r)) },
                    onMinRepsChanged = { min -> onUpdateSet(setIdx, setItem.copy(minReps = min)) },
                    onMaxRepsChanged = { max -> onUpdateSet(setIdx, setItem.copy(maxReps = max)) },
                    onDeleteSet = { onDeleteSet(setIdx) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 5. Bouton + AJOUTER UNE SÉRIE
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceElevated)
                    .clickable { onAddSet() }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = NeonLime,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.action_add_set),
                    color = NeonLime,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
