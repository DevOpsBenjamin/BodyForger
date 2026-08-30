package app.bodyforger.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.bodyforger.mobile.library.RoutineDraft
import app.bodyforger.mobile.library.RoutineDraft.isSaveable
import app.bodyforger.mobile.library.RoutineDraft.readyToSave
import app.bodyforger.mobile.library.RoutineDraftViewModel
import org.koin.androidx.compose.koinViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import app.bodyforger.core.model.WeightUnit
import app.bodyforger.mobile.R
import app.bodyforger.mobile.ui.components.RestTimePickerDialog
import app.bodyforger.mobile.ui.components.RoutineEditorInfoCard
import app.bodyforger.mobile.ui.components.RoutineEditorTopBar
import app.bodyforger.mobile.ui.components.RoutineExerciseCard
import app.bodyforger.mobile.ui.components.SetTypePickerDialog
import app.bodyforger.mobile.ui.components.WeightUnitPickerDialog
import app.bodyforger.mobile.ui.theme.ElectricCyan
import app.bodyforger.mobile.ui.theme.Obsidian
import app.bodyforger.mobile.ui.theme.TextMuted
import app.bodyforger.mobile.ui.theme.TextSecondary
import java.util.UUID

@Composable
fun RoutineEditorScreen(
    draftViewModel: RoutineDraftViewModel = koinViewModel(),
    onBack: () -> Unit = {},
    onOpenCatalogForAdd: () -> Unit = {},
    onOpenCatalogForReplace: (exerciseIndex: Int) -> Unit = {},
    onSaveRoutine: (Routine) -> Unit = {}
) {
    // The draft lives above this screen: adding an exercise leaves for the catalogue, and a
    // state held here would not survive the round trip.
    val draft = draftViewModel.draft.collectAsState().value ?: return
    val isNewRoutine by draftViewModel.isNew.collectAsState()
    val exercises = draft.exercises

    var activeRestDialogExerciseIndex by remember { mutableStateOf<Int?>(null) }
    var activeSetTypeDialogIndex by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var activeWeightUnitDialogExerciseIndex by remember { mutableStateOf<Int?>(null) }
    var showingReorderScreen by remember { mutableStateOf(false) }

    if (showingReorderScreen) {
        ReorderExercisesScreen(
            initialExercises = exercises,
            onConfirm = { reorderedList ->
                draftViewModel.reorderExercises(reorderedList)
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
            RoutineEditorTopBar(
                isNewRoutine = isNewRoutine,
                routineName = draft.name,
                onBack = onBack,
                onSave = { if (draft.isSaveable()) onSaveRoutine(draft.readyToSave()) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Contenu scrollable
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                item {
                    RoutineEditorInfoCard(
                        routineName = draft.name,
                        onRoutineNameChange = { draftViewModel.rename(it) },
                        routineNotes = draft.notes,
                        onRoutineNotesChange = { draftViewModel.setNotes(it) }
                    )
                }

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
                            onOpenWeightUnitPicker = { activeWeightUnitDialogExerciseIndex = exIndex },
                            onReplace = { onOpenCatalogForReplace(exIndex) },
                            onRemove = { draftViewModel.removeExercise(exIndex) },
                            onOpenRestPicker = { activeRestDialogExerciseIndex = exIndex },
                            onOpenSetTypeDialog = { setIdx -> activeSetTypeDialogIndex = exIndex to setIdx },
                            onUpdateSet = { setIdx, updatedSet ->
                                draftViewModel.updateSet(exIndex, setIdx, updatedSet)
                            },
                            onAddSet = { draftViewModel.addSet(exIndex) },
                            onDeleteSet = { setIdx -> draftViewModel.removeSet(exIndex, setIdx) }
                        )
                    }
                }

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

        // Modales
        activeRestDialogExerciseIndex?.let { exIdx ->
            val currentRest = exercises.getOrNull(exIdx)?.restTimeSeconds ?: RoutineDraft.DEFAULT_REST_SECONDS
            RestTimePickerDialog(
                currentRestSeconds = currentRest,
                onRestSelected = { newRest ->
                    draftViewModel.setRestTime(exIdx, newRest)
                },
                onDismiss = { activeRestDialogExerciseIndex = null }
            )
        }

        activeWeightUnitDialogExerciseIndex?.let { exIdx ->
            val currentUnit = exercises.getOrNull(exIdx)?.weightUnit ?: WeightUnit.KG
            WeightUnitPickerDialog(
                currentUnit = currentUnit,
                onUnitSelected = { newUnit ->
                    draftViewModel.setWeightUnit(exIdx, newUnit)
                },
                onDismiss = { activeWeightUnitDialogExerciseIndex = null }
            )
        }

        activeSetTypeDialogIndex?.let { (exIdx, setIdx) ->
            val currentSet = exercises.getOrNull(exIdx)?.sets?.getOrNull(setIdx)
            if (currentSet != null) {
                SetTypePickerDialog(
                    setIndexDisplay = setIdx + 1,
                    currentSet = currentSet,
                    onTypeSelected = { newType ->
                        draftViewModel.updateSet(exIdx, setIdx, currentSet.copy(type = newType))
                    },
                    onRepsFormatChanged = { isRange ->
                        draftViewModel.updateSet(exIdx, setIdx, currentSet.copy(isRepsRange = isRange))
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
    activityCategory = activityCategory,
    primaryMuscle = primaryMuscleGroup,
    equipment = equipment,
    isUnilateral = isUnilateral,
    weightUnit = WeightUnit.KG,
    orderIndex = 0,
    restTimeSeconds = 90,
    notes = "",
    sets = listOf(
        RoutineSet(setIndex = 1, type = RoutineSetType.NORMAL, reps = 10),
        RoutineSet(setIndex = 2, type = RoutineSetType.NORMAL, reps = 10),
        RoutineSet(setIndex = 3, type = RoutineSetType.NORMAL, reps = 10)
    )
)
