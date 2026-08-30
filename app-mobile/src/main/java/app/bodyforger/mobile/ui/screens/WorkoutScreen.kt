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
import app.bodyforger.mobile.workout.SetReference
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import app.bodyforger.mobile.ui.components.LiveWorkoutSetOptionsDialog
import app.bodyforger.mobile.ui.components.LiveWorkoutTopBar
import app.bodyforger.mobile.ui.components.RestTimePickerDialog
import app.bodyforger.mobile.ui.components.WeightUnitPickerDialog
import app.bodyforger.mobile.ui.components.WorkoutSummaryDialog
import app.bodyforger.mobile.ui.text.label
import app.bodyforger.mobile.ui.theme.CrimsonRed
import app.bodyforger.mobile.ui.theme.ElectricCyan
import app.bodyforger.mobile.ui.theme.NeonLime
import app.bodyforger.mobile.ui.theme.Obsidian
import app.bodyforger.mobile.workout.LiveWorkoutViewModel
import java.util.UUID
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
fun WorkoutScreen(
    workoutViewModel: LiveWorkoutViewModel = koinViewModel(),
    onMinimize: () -> Unit = {},
    onOpenCatalogForAdd: () -> Unit = {},
    onOpenCatalogForReplace: (exerciseIndex: Int) -> Unit = {},
    onFinishWorkout: (WorkoutSession) -> Unit = {}
) {
    // The workout belongs to the ViewModel: minimising the session leaves this composition,
    // and anything held here would go with it.
    val workout = workoutViewModel.active.collectAsState().value ?: return
    val lastPerformance by workoutViewModel.lastPerformance.collectAsState()

    var sessionSeconds by remember { mutableIntStateOf(0) }
    var currentHeartRate by remember { mutableIntStateOf(138) }

    // Chrono de repos
    var isResting by remember { mutableStateOf(false) }
    var restSecondsRemaining by remember { mutableIntStateOf(0) }
    var currentRestTotalSeconds by remember { mutableIntStateOf(1) }

    // Modales de configuration
    var activeRestPickerExerciseIndex by remember { mutableStateOf<Int?>(null) }
    var activeWeightUnitPickerExerciseIndex by remember { mutableStateOf<Int?>(null) }
    var showingSummaryDialog by remember { mutableStateOf(false) }
    var setOptionsTarget by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    // Compte depuis l'heure de depart plutot qu'en incrementant: reduire la seance
    // detruit ce composable, et un compteur repartirait de zero au retour.
    LaunchedEffect(workout.session.startedAtEpochMs) {
        while (true) {
            sessionSeconds = ((System.currentTimeMillis() - workout.session.startedAtEpochMs) / MILLIS_PER_SECOND).toInt()
            delay(TIMER_TICK_MS)
        }
    }

    // Ticker chrono de repos
    LaunchedEffect(isResting, restSecondsRemaining) {
        if (isResting && restSecondsRemaining > 0) {
            delay(TIMER_TICK_MS)
            restSecondsRemaining--
            if (restSecondsRemaining <= 0) {
                isResting = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
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
            itemsIndexed(workout.exercises, key = { _, ex -> ex.id }) { exIdx, exItem ->
                val exSets = workout.setsOf(exIdx)

                LiveWorkoutExerciseCard(
                    exerciseName = exItem.exerciseName,
                    primaryMuscleName = exItem.primaryMuscle.label(),
                    equipmentName = exItem.equipment.label(),
                    activityCategoryName = exItem.activityCategory.label(),
                    isUnilateral = exItem.isUnilateral,
                    weightUnit = exItem.weightUnit,
                    restTimeSeconds = exItem.restTimeSeconds,
                    sets = exSets,
                    onOpenWeightUnitPicker = { activeWeightUnitPickerExerciseIndex = exIdx },
                    onOpenRestPicker = { activeRestPickerExerciseIndex = exIdx },
                    onReplaceExercise = { onOpenCatalogForReplace(exIdx) },
                    onRemoveExercise = { workoutViewModel.removeExercise(exIdx) },
                    onToggleSetCompleted = { targetSet ->
                        workoutViewModel.toggleSetCompleted(targetSet.id)

                        if (!targetSet.isCompleted && shouldRestAfter(targetSet, exSets)) {
                            currentRestTotalSeconds = exItem.restTimeSeconds
                            restSecondsRemaining = exItem.restTimeSeconds
                            isResting = true
                        }
                    },
                    onUpdateSetWeight = { targetSet, newWeight ->
                        workoutViewModel.setWeight(targetSet.id, newWeight)
                    },
                    onUpdateSetReps = { targetSet, newReps ->
                        workoutViewModel.setReps(targetSet.id, newReps)
                    },
                    onAddSet = { workoutViewModel.addSet(exIdx) },
                    onOpenSetOptions = { targetSet -> setOptionsTarget = exIdx to targetSet.setIndex },
                    lastPerformanceOf = { targetSet -> lastPerformance[SetReference.of(targetSet)] },
                    onRepeatLastPerformance = { targetSet ->
                        workoutViewModel.repeatLastPerformance(targetSet.id)
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

    if (showingSummaryDialog) {
        WorkoutSummaryDialog(
            session = workout.toSession().copy(averageHeartRateBpm = currentHeartRate),
            onConfirmSave = {
                showingSummaryDialog = false
                workoutViewModel.finish()?.let(onFinishWorkout)
            }
        )
    }

    setOptionsTarget?.let { (exIdx, setIdx) ->
        val target = workout.setsOf(exIdx).firstOrNull { it.setIndex == setIdx }
        if (target != null) {
            LiveWorkoutSetOptionsDialog(
                setIndexDisplay = setIdx,
                currentType = target.type,
                canDelete = workout.canRemoveSet(exIdx),
                onTypeSelected = { newType -> workoutViewModel.setType(exIdx, setIdx, newType) },
                onDeleteSet = { workoutViewModel.removeSetAt(exIdx, setIdx) },
                onDismiss = { setOptionsTarget = null }
            )
        }
    }

    activeWeightUnitPickerExerciseIndex?.let { exIdx ->
        val currentUnit = workout.exercises.getOrNull(exIdx)?.weightUnit ?: WeightUnit.KG
        WeightUnitPickerDialog(
            currentUnit = currentUnit,
            onUnitSelected = { newUnit -> workoutViewModel.setWeightUnit(exIdx, newUnit) },
            onDismiss = { activeWeightUnitPickerExerciseIndex = null }
        )
    }

    // Modale Temps de Repos
    activeRestPickerExerciseIndex?.let { exIdx ->
        val currentRest = workout.exercises.getOrNull(exIdx)?.restTimeSeconds ?: DEFAULT_REST_SECONDS
        RestTimePickerDialog(
            currentRestSeconds = currentRest,
            onRestSelected = { newRest -> workoutViewModel.setRestTime(exIdx, newRest) },
            onDismiss = { activeRestPickerExerciseIndex = null }
        )
    }
}

/** One tick of the session and rest clocks. */
private const val TIMER_TICK_MS = 1_000L
private const val MILLIS_PER_SECOND = 1_000L

/** Shown by the rest picker when an exercise somehow carries no rest of its own. */
private const val DEFAULT_REST_SECONDS = 90

/**
 * Whether validating this set ends the effort, and so opens the rest.
 *
 * A unilateral exercise is only done once both sides are: resting after the left side would
 * cut the exercise in half.
 */
private fun shouldRestAfter(validated: WorkoutSet, exerciseSets: List<WorkoutSet>): Boolean {
    if (validated.side != UnilateralSide.LEFT) return true
    return exerciseSets.any {
        it.setIndex == validated.setIndex && it.side == UnilateralSide.RIGHT && it.isCompleted
    }
}
