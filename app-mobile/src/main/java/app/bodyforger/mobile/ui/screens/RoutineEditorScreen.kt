package app.bodyforger.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.core.model.Exercise
import app.bodyforger.core.model.Routine
import app.bodyforger.core.model.RoutineExercise
import app.bodyforger.core.model.RoutineSet
import app.bodyforger.core.model.RoutineSetType
import app.bodyforger.mobile.R
import app.bodyforger.mobile.ui.components.RestTimePickerDialog
import app.bodyforger.mobile.ui.components.RoutineExerciseCard
import app.bodyforger.mobile.ui.components.SetTypePickerDialog
import app.bodyforger.mobile.ui.theme.ElectricCyan
import app.bodyforger.mobile.ui.theme.NeonLime
import app.bodyforger.mobile.ui.theme.Obsidian
import app.bodyforger.mobile.ui.theme.SurfaceBorder
import app.bodyforger.mobile.ui.theme.SurfaceDark
import app.bodyforger.mobile.ui.theme.SurfaceElevated
import app.bodyforger.mobile.ui.theme.TextMuted
import app.bodyforger.mobile.ui.theme.TextPrimary
import app.bodyforger.mobile.ui.theme.TextSecondary
import java.util.UUID

@Composable
fun RoutineEditorScreen(
    initialRoutine: Routine? = null,
    onBack: () -> Unit = {},
    onOpenCatalogForAdd: () -> Unit = {},
    onOpenCatalogForReplace: (exerciseIndex: Int) -> Unit = {},
    onSaveRoutine: (Routine) -> Unit = {}
) {
    var routineName by remember { mutableStateOf(initialRoutine?.name ?: "") }
    var routineNotes by remember { mutableStateOf(initialRoutine?.notes ?: "") }
    val exercises = remember {
        mutableStateListOf<RoutineExercise>().apply {
            addAll(initialRoutine?.exercises ?: emptyList())
        }
    }

    var activeRestDialogExerciseIndex by remember { mutableStateOf<Int?>(null) }
    var activeSetTypeDialogIndex by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var showingReorderScreen by remember { mutableStateOf(false) }

    if (showingReorderScreen) {
        // Vue Plein Écran de réorganisation des exercices (Style Hevy avec drag & drop et bouton Terminé)
        ReorderExercisesScreen(
            initialExercises = exercises.toList(),
            onConfirm = { reorderedList ->
                exercises.clear()
                exercises.addAll(reorderedList)
                showingReorderScreen = false
            },
            onCancel = {
                showingReorderScreen = false
            }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Obsidian)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // --- 1. BARRE SUPÉRIEURE ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack, // Retourne sans enregistrer
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SurfaceElevated)
                            .border(1.dp, SurfaceBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_cancel),
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = if (initialRoutine == null) {
                                stringResource(R.string.routine_editor_new_title)
                            } else {
                                stringResource(R.string.routine_editor_edit_title)
                            },
                            color = NeonLime,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (routineName.isBlank()) stringResource(R.string.routine_editor_untitled) else routineName,
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1
                        )
                    }
                }

                // Bouton ENREGISTRER
                Button(
                    onClick = {
                        if (routineName.isNotBlank()) {
                            val updatedRoutine = Routine(
                                id = initialRoutine?.id ?: UUID.randomUUID().toString(),
                                name = routineName.trim(),
                                notes = routineNotes.trim(),
                                assignedDays = initialRoutine?.assignedDays ?: emptySet(),
                                exercises = exercises.mapIndexed { index, ex ->
                                    ex.copy(orderIndex = index)
                                },
                                createdAtEpochMs = initialRoutine?.createdAtEpochMs ?: System.currentTimeMillis()
                            )
                            onSaveRoutine(updatedRoutine)
                        }
                    },
                    enabled = routineName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonLime,
                        contentColor = Color.Black,
                        disabledContainerColor = SurfaceElevated,
                        disabledContentColor = TextMuted
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = stringResource(R.string.action_save), fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // --- 2. CONTENU PRINCIPAL SCROLLABLE ---
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Section Infos Générales de la Routine
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = stringResource(R.string.routine_editor_title_label),
                                color = NeonLime,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = routineName,
                                onValueChange = { routineName = it },
                                placeholder = { Text(stringResource(R.string.routine_editor_title_hint), color = TextMuted, fontSize = 13.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = SurfaceElevated,
                                    unfocusedContainerColor = SurfaceElevated,
                                    focusedBorderColor = NeonLime,
                                    unfocusedBorderColor = SurfaceBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = stringResource(R.string.routine_editor_notes_label),
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = routineNotes,
                                onValueChange = { routineNotes = it },
                                placeholder = { Text(stringResource(R.string.routine_editor_notes_hint), color = TextMuted, fontSize = 12.sp) },
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = SurfaceElevated,
                                    unfocusedContainerColor = SurfaceElevated,
                                    focusedBorderColor = NeonLime,
                                    unfocusedBorderColor = SurfaceBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Section Exercices
                if (exercises.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 30.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = stringResource(R.string.routine_editor_no_exercises),
                                    color = TextSecondary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.routine_editor_no_exercises_hint),
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                } else {
                    itemsIndexed(exercises, key = { _, item -> item.id }) { exIndex, exItem ->
                        RoutineExerciseCard(
                            exerciseIndex = exIndex,
                            totalExercises = exercises.size,
                            exercise = exItem,
                            onOpenReorder = { showingReorderScreen = true },
                            onReplace = { onOpenCatalogForReplace(exIndex) },
                            onRemove = { exercises.removeAt(exIndex) },
                            onOpenRestPicker = { activeRestDialogExerciseIndex = exIndex },
                            onOpenSetTypeDialog = { setIdx -> activeSetTypeDialogIndex = exIndex to setIdx },
                            onUpdateSet = { setIdx, updatedSet ->
                                val updatedSets = exItem.sets.toMutableList()
                                updatedSets[setIdx] = updatedSet
                                exercises[exIndex] = exItem.copy(sets = updatedSets)
                            },
                            onAddSet = {
                                val lastSet = exItem.sets.lastOrNull()
                                val newSet = RoutineSet(
                                    setIndex = exItem.sets.size + 1,
                                    type = RoutineSetType.NORMAL,
                                    targetWeightKg = lastSet?.targetWeightKg,
                                    reps = lastSet?.reps ?: 10,
                                    minReps = lastSet?.minReps ?: 8,
                                    maxReps = lastSet?.maxReps ?: 12,
                                    isRepsRange = lastSet?.isRepsRange ?: false
                                )
                                exercises[exIndex] = exItem.copy(sets = exItem.sets + newSet)
                            },
                            onDeleteSet = { setIdx ->
                                if (exItem.sets.size > 1) {
                                    val updatedSets = exItem.sets.toMutableList()
                                    updatedSets.removeAt(setIdx)
                                    exercises[exIndex] = exItem.copy(sets = updatedSets)
                                }
                            }
                        )
                    }
                }

                // Bouton + AJOUTER UN EXERCICE
                item {
                    Button(
                        onClick = onOpenCatalogForAdd,
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.action_add_exercise),
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }

        // --- MODALE DU CHRONOMÈTRE DE REPOS ---
        activeRestDialogExerciseIndex?.let { exIdx ->
            val currentRest = exercises.getOrNull(exIdx)?.restTimeSeconds ?: 90
            RestTimePickerDialog(
                currentRestSeconds = currentRest,
                onRestSelected = { newRest ->
                    exercises[exIdx] = exercises[exIdx].copy(restTimeSeconds = newRest)
                },
                onDismiss = { activeRestDialogExerciseIndex = null }
            )
        }

        // --- MODALE DU TYPE DE SÉRIE & OPTIONS DE RÉPÉTITIONS ---
        activeSetTypeDialogIndex?.let { (exIdx, setIdx) ->
            val currentSet = exercises.getOrNull(exIdx)?.sets?.getOrNull(setIdx)
            if (currentSet != null) {
                SetTypePickerDialog(
                    setIndexDisplay = setIdx + 1,
                    currentSet = currentSet,
                    onTypeSelected = { newType ->
                        val updatedSets = exercises[exIdx].sets.toMutableList()
                        updatedSets[setIdx] = currentSet.copy(type = newType)
                        exercises[exIdx] = exercises[exIdx].copy(sets = updatedSets)
                    },
                    onRepsFormatChanged = { isRange ->
                        val updatedSets = exercises[exIdx].sets.toMutableList()
                        updatedSets[setIdx] = currentSet.copy(isRepsRange = isRange)
                        exercises[exIdx] = exercises[exIdx].copy(sets = updatedSets)
                    },
                    onDismiss = { activeSetTypeDialogIndex = null }
                )
            }
        }
    }
}

fun Exercise.toRoutineExercise(routineId: String = ""): RoutineExercise = RoutineExercise(
    id = UUID.randomUUID().toString(),
    routineId = routineId,
    exerciseId = id,
    exerciseName = name,
    primaryMuscle = primaryMuscleGroup,
    equipment = equipment,
    isUnilateral = isUnilateral,
    orderIndex = 0,
    restTimeSeconds = 90,
    notes = "",
    sets = listOf(
        RoutineSet(setIndex = 1, type = RoutineSetType.NORMAL, reps = 10),
        RoutineSet(setIndex = 2, type = RoutineSetType.NORMAL, reps = 10),
        RoutineSet(setIndex = 3, type = RoutineSetType.NORMAL, reps = 10)
    )
)
