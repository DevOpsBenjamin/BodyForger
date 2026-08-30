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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.mobile.workout.LiveWorkoutViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import app.bodyforger.core.model.Routine
import app.bodyforger.core.model.RoutineExercise
import app.bodyforger.core.model.RoutineSetType
import app.bodyforger.core.model.UnilateralSide
import app.bodyforger.core.model.WeightUnit
import app.bodyforger.core.model.WorkoutSession
import app.bodyforger.core.model.WorkoutSessionStatus
import app.bodyforger.core.model.WorkoutSet
import app.bodyforger.mobile.R
import app.bodyforger.mobile.ui.components.LiveWorkoutExerciseCard
import app.bodyforger.mobile.ui.components.LiveWorkoutRestTimerOverlay
import app.bodyforger.mobile.ui.components.LiveWorkoutTopBar
import app.bodyforger.mobile.ui.components.RestTimePickerDialog
import app.bodyforger.mobile.ui.components.WeightUnitPickerDialog
import app.bodyforger.mobile.ui.components.WorkoutSummaryDialog
import app.bodyforger.mobile.ui.theme.CrimsonRed
import app.bodyforger.mobile.ui.theme.ElectricCyan
import app.bodyforger.mobile.ui.theme.NeonLime
import app.bodyforger.mobile.ui.theme.Obsidian
import kotlinx.coroutines.delay
import java.util.UUID

@Composable
fun WorkoutScreen(
    initialRoutine: Routine? = null,
    workoutViewModel: LiveWorkoutViewModel = viewModel(),
    onMinimize: () -> Unit = {},
    onOpenCatalogForAdd: () -> Unit = {},
    onOpenCatalogForReplace: (exerciseIndex: Int) -> Unit = {},
    onFinishWorkout: (WorkoutSession) -> Unit = {}
) {
    val sessionStartedAtEpochMs = remember { System.currentTimeMillis() }
    var sessionSeconds by remember { mutableIntStateOf(0) }
    var currentHeartRate by remember { mutableIntStateOf(138) }

    // Chrono de repos
    var isResting by remember { mutableStateOf(false) }
    var restSecondsRemaining by remember { mutableIntStateOf(60) }
    var currentRestTotalSeconds by remember { mutableIntStateOf(60) }

    // Modales de configuration
    var activeRestPickerExerciseIndex by remember { mutableStateOf<Int?>(null) }
    var activeWeightUnitPickerExerciseIndex by remember { mutableStateOf<Int?>(null) }
    var showingSummaryDialog by remember { mutableStateOf(false) }
    val sessionId = remember { UUID.randomUUID().toString() }
    val sessionTitle = stringResource(R.string.workout_live_free_session_title)

    // Liste des exercices et séries actives
    val workoutExercises = remember {
        mutableStateListOf<RoutineExercise>().apply {
            addAll(initialRoutine?.exercises ?: emptyList())
        }
    }

    val liveSets = remember {
        mutableStateListOf<WorkoutSet>().apply {
            initialRoutine?.exercises?.forEachIndexed { exIdx, ex ->
                ex.sets.forEach { setItem ->
                    if (ex.isUnilateral) {
                        add(
                            WorkoutSet(
                                id = UUID.randomUUID().toString(),
                                exerciseId = ex.exerciseId,
                                exerciseName = ex.exerciseName,
                                primaryMuscle = ex.primaryMuscle,
                                equipment = ex.equipment,
                                activityCategory = ex.activityCategory,
                                orderIndex = exIdx,
                                setIndex = setItem.setIndex,
                                type = setItem.type,
                                weightKg = setItem.targetWeightKg ?: 0.0,
                                weightUnit = ex.weightUnit,
                                reps = setItem.reps ?: setItem.minReps ?: 10,
                                isCompleted = false,
                                side = UnilateralSide.LEFT,
                                restTimeSeconds = ex.restTimeSeconds
                            )
                        )
                        add(
                            WorkoutSet(
                                id = UUID.randomUUID().toString(),
                                exerciseId = ex.exerciseId,
                                exerciseName = ex.exerciseName,
                                primaryMuscle = ex.primaryMuscle,
                                equipment = ex.equipment,
                                activityCategory = ex.activityCategory,
                                orderIndex = exIdx,
                                setIndex = setItem.setIndex,
                                type = setItem.type,
                                weightKg = setItem.targetWeightKg ?: 0.0,
                                weightUnit = ex.weightUnit,
                                reps = setItem.reps ?: setItem.minReps ?: 10,
                                isCompleted = false,
                                side = UnilateralSide.RIGHT,
                                restTimeSeconds = ex.restTimeSeconds
                            )
                        )
                    } else {
                        add(
                            WorkoutSet(
                                id = UUID.randomUUID().toString(),
                                exerciseId = ex.exerciseId,
                                exerciseName = ex.exerciseName,
                                primaryMuscle = ex.primaryMuscle,
                                equipment = ex.equipment,
                                activityCategory = ex.activityCategory,
                                orderIndex = exIdx,
                                setIndex = setItem.setIndex,
                                type = setItem.type,
                                weightKg = setItem.targetWeightKg ?: 0.0,
                                weightUnit = ex.weightUnit,
                                reps = setItem.reps ?: setItem.minReps ?: 10,
                                isCompleted = false,
                                side = UnilateralSide.NONE,
                                restTimeSeconds = ex.restTimeSeconds
                            )
                        )
                    }
                }
            }
        }
    }

    // Ticker durée de séance
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            sessionSeconds++
        }
    }

    // Ticker chrono de repos
    LaunchedEffect(isResting, restSecondsRemaining) {
        if (isResting && restSecondsRemaining > 0) {
            delay(1000)
            restSecondsRemaining--
            if (restSecondsRemaining <= 0) {
                isResting = false
            }
        }
    }

    LaunchedEffect(sessionId) {
        workoutViewModel.start(
            session = WorkoutSession(
                id = sessionId,
                routineId = initialRoutine?.id,
                title = initialRoutine?.name ?: sessionTitle,
                startedAtEpochMs = sessionStartedAtEpochMs
            ),
            plannedSets = liveSets.toList()
        )
    }

    val liveSession = remember(workoutExercises.size, liveSets.toList(), sessionSeconds) {
        WorkoutSession(
            id = sessionId,
            routineId = initialRoutine?.id,
            title = initialRoutine?.name ?: sessionTitle,
            startedAtEpochMs = sessionStartedAtEpochMs,
            endedAtEpochMs = if (showingSummaryDialog) System.currentTimeMillis() else null,
            status = if (showingSummaryDialog) WorkoutSessionStatus.COMPLETED else WorkoutSessionStatus.ACTIVE,
            sets = liveSets.toList(),
            averageHeartRateBpm = currentHeartRate,
            isFinalized = showingSummaryDialog
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // 1. Barre supérieure
        LiveWorkoutTopBar(
            currentHeartRate = currentHeartRate,
            sessionSeconds = sessionSeconds,
            onMinimize = onMinimize
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Liste des exercices scrollable
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            itemsIndexed(workoutExercises, key = { _, ex -> ex.id }) { exIdx, exItem ->
                val exSets = liveSets.filter { it.orderIndex == exIdx }

                LiveWorkoutExerciseCard(
                    exerciseName = exItem.exerciseName,
                    primaryMuscleName = exItem.primaryMuscle.displayName,
                    equipmentName = exItem.equipment.displayName,
                    activityCategoryName = exItem.activityCategory.displayName,
                    isUnilateral = exItem.isUnilateral,
                    weightUnit = exItem.weightUnit,
                    restTimeSeconds = exItem.restTimeSeconds,
                    sets = exSets,
                    onOpenWeightUnitPicker = { activeWeightUnitPickerExerciseIndex = exIdx },
                    onOpenRestPicker = { activeRestPickerExerciseIndex = exIdx },
                    onReplaceExercise = { onOpenCatalogForReplace(exIdx) },
                    onRemoveExercise = {
                        workoutExercises.removeAt(exIdx)
                        liveSets.removeAll { it.orderIndex == exIdx }
                    },
                    onToggleSetCompleted = { targetSet ->
                        val setIdxInList = liveSets.indexOfFirst { it.id == targetSet.id }
                        if (setIdxInList != -1) {
                            val newCompleted = !targetSet.isCompleted
                            val recorded = targetSet.copy(
                                isCompleted = newCompleted,
                                completedAtEpochMs = if (newCompleted) System.currentTimeMillis() else null
                            )
                            liveSets[setIdxInList] = recorded
                            workoutViewModel.recordSet(recorded)

                            // Déclenche le chrono de repos si validé
                            if (newCompleted) {
                                val shouldTriggerRest = if (targetSet.side == UnilateralSide.LEFT) {
                                    // Attend que le côté droit soit fait aussi
                                    val rightSide = liveSets.find { it.orderIndex == exIdx && it.setIndex == targetSet.setIndex && it.side == UnilateralSide.RIGHT }
                                    rightSide?.isCompleted == true
                                } else {
                                    true
                                }

                                if (shouldTriggerRest) {
                                    currentRestTotalSeconds = exItem.restTimeSeconds
                                    restSecondsRemaining = exItem.restTimeSeconds
                                    isResting = true
                                }
                            }
                        }
                    },
                    onUpdateSetWeight = { targetSet, newWeight ->
                        val setIdxInList = liveSets.indexOfFirst { it.id == targetSet.id }
                        if (setIdxInList != -1) {
                            liveSets[setIdxInList] = targetSet.copy(weightKg = newWeight)
                        }
                    },
                    onUpdateSetReps = { targetSet, newReps ->
                        val setIdxInList = liveSets.indexOfFirst { it.id == targetSet.id }
                        if (setIdxInList != -1) {
                            liveSets[setIdxInList] = targetSet.copy(reps = newReps)
                        }
                    },
                    onAddSet = {
                        val lastSet = exSets.lastOrNull()
                        val newSetIdx = (lastSet?.setIndex ?: 0) + 1
                        if (exItem.isUnilateral) {
                            liveSets.add(
                                WorkoutSet(
                                    exerciseId = exItem.exerciseId,
                                    exerciseName = exItem.exerciseName,
                                    primaryMuscle = exItem.primaryMuscle,
                                    equipment = exItem.equipment,
                                    activityCategory = exItem.activityCategory,
                                    orderIndex = exIdx,
                                    setIndex = newSetIdx,
                                    weightKg = lastSet?.weightKg ?: 0.0,
                                    weightUnit = exItem.weightUnit,
                                    reps = lastSet?.reps ?: 10,
                                    side = UnilateralSide.LEFT,
                                    restTimeSeconds = exItem.restTimeSeconds
                                )
                            )
                            liveSets.add(
                                WorkoutSet(
                                    exerciseId = exItem.exerciseId,
                                    exerciseName = exItem.exerciseName,
                                    primaryMuscle = exItem.primaryMuscle,
                                    equipment = exItem.equipment,
                                    activityCategory = exItem.activityCategory,
                                    orderIndex = exIdx,
                                    setIndex = newSetIdx,
                                    weightKg = lastSet?.weightKg ?: 0.0,
                                    weightUnit = exItem.weightUnit,
                                    reps = lastSet?.reps ?: 10,
                                    side = UnilateralSide.RIGHT,
                                    restTimeSeconds = exItem.restTimeSeconds
                                )
                            )
                        } else {
                            liveSets.add(
                                WorkoutSet(
                                    exerciseId = exItem.exerciseId,
                                    exerciseName = exItem.exerciseName,
                                    primaryMuscle = exItem.primaryMuscle,
                                    equipment = exItem.equipment,
                                    activityCategory = exItem.activityCategory,
                                    orderIndex = exIdx,
                                    setIndex = newSetIdx,
                                    weightKg = lastSet?.weightKg ?: 0.0,
                                    weightUnit = exItem.weightUnit,
                                    reps = lastSet?.reps ?: 10,
                                    side = UnilateralSide.NONE,
                                    restTimeSeconds = exItem.restTimeSeconds
                                )
                            )
                        }
                    },
                    onDeleteSet = { targetSet ->
                        liveSets.removeAll { it.id == targetSet.id }
                    }
                )
            }

            // Bouton + AJOUTER UN EXERCICE PENDANT LA SÉANCE
            item {
                Button(
                    onClick = onOpenCatalogForAdd,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = androidx.compose.ui.graphics.Color.Black),
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
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Chronomètre de Repos Interactif
        LiveWorkoutRestTimerOverlay(
            isVisible = isResting,
            secondsRemaining = restSecondsRemaining,
            totalSeconds = currentRestTotalSeconds,
            onAddSeconds = { added ->
                restSecondsRemaining = (restSecondsRemaining + added).coerceAtLeast(0)
                currentRestTotalSeconds = (currentRestTotalSeconds + added).coerceAtLeast(1)
            },
            onSkipRest = { isResting = false }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 4. Bouton TERMINER LA SÉANCE
        Button(
            onClick = { showingSummaryDialog = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonLime,
                contentColor = androidx.compose.ui.graphics.Color.Black
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = stringResource(R.string.workout_live_finish_btn),
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                letterSpacing = 0.5.sp
            )
        }
    }

    // Dialogue Récapitulatif Bilan de Fin de Séance
    if (showingSummaryDialog) {
        WorkoutSummaryDialog(
            session = liveSession,
            onConfirmSave = {
                showingSummaryDialog = false
                workoutViewModel.finish(liveSession)
                onFinishWorkout(liveSession)
            }
        )
    }

    // Modale Unité de Poids
    activeWeightUnitPickerExerciseIndex?.let { exIdx ->
        val currentUnit = workoutExercises.getOrNull(exIdx)?.weightUnit ?: WeightUnit.KG
        WeightUnitPickerDialog(
            currentUnit = currentUnit,
            onUnitSelected = { newUnit ->
                val currentEx = workoutExercises[exIdx]
                workoutExercises[exIdx] = currentEx.copy(weightUnit = newUnit)
                // Met à jour les sets associés
                liveSets.replaceAll {
                    if (it.orderIndex == exIdx) it.copy(weightUnit = newUnit) else it
                }
            },
            onDismiss = { activeWeightUnitPickerExerciseIndex = null }
        )
    }

    // Modale Temps de Repos
    activeRestPickerExerciseIndex?.let { exIdx ->
        val currentRest = workoutExercises.getOrNull(exIdx)?.restTimeSeconds ?: 90
        RestTimePickerDialog(
            currentRestSeconds = currentRest,
            onRestSelected = { newRest ->
                val currentEx = workoutExercises[exIdx]
                workoutExercises[exIdx] = currentEx.copy(restTimeSeconds = newRest)
            },
            onDismiss = { activeRestPickerExerciseIndex = null }
        )
    }
}
