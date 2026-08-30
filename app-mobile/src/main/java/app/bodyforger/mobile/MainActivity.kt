package app.bodyforger.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.setValue
import app.bodyforger.mobile.library.LibraryViewModel
import app.bodyforger.mobile.library.RoutineDraftViewModel
import app.bodyforger.mobile.ui.components.ResumeWorkoutDialog
import app.bodyforger.mobile.workout.LiveWorkoutViewModel
import org.koin.androidx.compose.koinViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.bodyforger.core.model.Exercise
import app.bodyforger.core.model.Routine
import app.bodyforger.core.model.WorkoutSession
import app.bodyforger.mobile.navigation.NavItem
import app.bodyforger.mobile.ui.components.ActiveWorkoutMiniBar
import app.bodyforger.mobile.ui.components.BodyForgerBottomNav
import app.bodyforger.mobile.ui.screens.AnalyticsScreen
import app.bodyforger.mobile.ui.screens.CatalogScreen
import app.bodyforger.mobile.ui.screens.CreateExerciseScreen
import app.bodyforger.mobile.ui.screens.HomeScreen
import app.bodyforger.mobile.ui.screens.PlannerScreen
import app.bodyforger.mobile.ui.screens.SettingsScreen
import app.bodyforger.core.model.BiaProfile
import app.bodyforger.core.model.BiologicalSex
import app.bodyforger.mobile.ui.screens.ProfileScreen
import app.bodyforger.mobile.ui.screens.RoutineEditorScreen
import app.bodyforger.mobile.ui.screens.WorkoutScreen
import app.bodyforger.mobile.ui.screens.toRoutineExercise
import app.bodyforger.mobile.ui.theme.BodyForgerTheme
import app.bodyforger.mobile.ui.theme.Obsidian
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BodyForgerTheme {
                BodyForgerApp()
            }
        }
    }
}

@Composable
fun BodyForgerApp() {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showingRoutineEditor by remember { mutableStateOf(false) }
    var showingCatalogScreen by remember { mutableStateOf(false) }
    var showingCreateExerciseScreen by remember { mutableStateOf(false) }
    var isCatalogForRoutineSelection by remember { mutableStateOf(false) }
    var catalogReplaceExerciseIndex by remember { mutableStateOf<Int?>(null) }

    var showingLiveWorkoutScreen by remember { mutableStateOf(false) }
    var showingSettingsScreen by remember { mutableStateOf(false) }

    val library: LibraryViewModel = koinViewModel()
    val workoutViewModel: LiveWorkoutViewModel = koinViewModel()
    val routineDraft: RoutineDraftViewModel = koinViewModel()
    val interruptedSession by workoutViewModel.resumable.collectAsState()
    // La séance en cours appartient au ViewModel: réduire l'écran ne doit rien lui coûter.
    val liveWorkout by workoutViewModel.active.collectAsState()
    val freeSessionTitle = stringResource(R.string.workout_live_free_session_title)
    val routines by library.routines.collectAsState()
    val customExercises by library.exercises.collectAsState()
    val completedSessions by library.completedSessions.collectAsState()

    val navItems = listOf(NavItem.Home, NavItem.Planner, NavItem.Analytics, NavItem.Profile)

    // A session left open by a previous run is offered before anything else: the athlete
    // should not discover mid-workout that the previous one was never closed.
    interruptedSession?.let { session ->
        ResumeWorkoutDialog(
            session = session,
            onResume = {
                workoutViewModel.resume(session)
                showingLiveWorkoutScreen = true
            },
            onDiscard = { workoutViewModel.discard(session) }
        )
    }

    if (showingCreateExerciseScreen) {
        CreateExerciseScreen(
            onBack = { showingCreateExerciseScreen = false },
            onExerciseCreated = { newExercise ->
                library.addCustomExercise(newExercise)
                showingCreateExerciseScreen = false
            }
        )
    } else if (showingSettingsScreen) {
        SettingsScreen(
            profile = BiaProfile(BiologicalSex.MALE, ageYears = 30, heightCm = 180.0),
            onBack = { showingSettingsScreen = false }
        )
    } else if (showingCatalogScreen) {
        CatalogScreen(
            exercises = customExercises,
            isSelectionMode = isCatalogForRoutineSelection,
            onBack = {
                showingCatalogScreen = false
                isCatalogForRoutineSelection = false
                catalogReplaceExerciseIndex = null
            },
            onOpenCreateExercise = { showingCreateExerciseScreen = true },
            onSelectExercise = { selectedExercise ->
                if (isCatalogForRoutineSelection) {
                    val replacedIndex = catalogReplaceExerciseIndex
                    if (showingRoutineEditor) {
                        val currentDraft = routineDraft.draft.value ?: Routine(name = "")
                        val chosen = selectedExercise.toRoutineExercise(currentDraft.id)
                        routineDraft.addExercise(chosen, replacing = replacedIndex)
                    } else {
                        // Ajout en pleine séance: l'exercice ne rejoint aucune routine.
                        val chosen = selectedExercise.toRoutineExercise(routineId = "")
                        if (replacedIndex != null) workoutViewModel.replaceExercise(replacedIndex, chosen)
                        else workoutViewModel.addExercise(chosen)
                    }
                    showingCatalogScreen = false
                    isCatalogForRoutineSelection = false
                    catalogReplaceExerciseIndex = null
                }
            }
        )
    } else if (showingRoutineEditor) {
        RoutineEditorScreen(
            draftViewModel = routineDraft,
            onBack = {
                showingRoutineEditor = false
                routineDraft.close()
            },
            onOpenCatalogForAdd = {
                isCatalogForRoutineSelection = true
                catalogReplaceExerciseIndex = null
                showingCatalogScreen = true
            },
            onOpenCatalogForReplace = { exIndex ->
                isCatalogForRoutineSelection = true
                catalogReplaceExerciseIndex = exIndex
                showingCatalogScreen = true
            },
            onSaveRoutine = { savedRoutine ->
                library.saveRoutine(savedRoutine)
                showingRoutineEditor = false
                routineDraft.close()
            }
        )
    } else if (showingLiveWorkoutScreen) {
        WorkoutScreen(
            workoutViewModel = workoutViewModel,
            onMinimize = { showingLiveWorkoutScreen = false },
            onOpenCatalogForAdd = {
                isCatalogForRoutineSelection = true
                catalogReplaceExerciseIndex = null
                showingCatalogScreen = true
            },
            onOpenCatalogForReplace = { exIndex ->
                isCatalogForRoutineSelection = true
                catalogReplaceExerciseIndex = exIndex
                showingCatalogScreen = true
            },
            onFinishWorkout = {
                showingLiveWorkoutScreen = false
                selectedTabIndex = 1 // Retour au planner
            }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Obsidian,
            bottomBar = {
                Column {
                    ActiveWorkoutMiniBar(
                        isVisible = liveWorkout != null,
                        workoutTitle = liveWorkout?.session?.title.orEmpty(),
                        onClick = { showingLiveWorkoutScreen = true }
                    )

                    BodyForgerBottomNav(
                        selectedTabIndex = selectedTabIndex,
                        navItems = navItems,
                        onTabSelected = { selectedTabIndex = it }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTabIndex) {
                    0 -> HomeScreen(
                        onNavigateToWorkout = {
                            workoutViewModel.begin(routine = null, freeSessionTitle = freeSessionTitle)
                            showingLiveWorkoutScreen = true
                        },
                        onNavigateToBiometrics = { selectedTabIndex = 2 },
                        onOpenSettings = { showingSettingsScreen = true }
                    )
                    1 -> PlannerScreen(
                        routines = routines,
                        onStartWorkout = { routineId ->
                            workoutViewModel.begin(
                                routine = routines.firstOrNull { it.id == routineId },
                                freeSessionTitle = freeSessionTitle
                            )
                            showingLiveWorkoutScreen = true
                        },
                        onCreateNewRoutine = {
                                        routineDraft.open(null)
                            showingRoutineEditor = true
                        },
                        onEditRoutine = { routineToEdit ->
                            routineDraft.open(routineToEdit)
                            showingRoutineEditor = true
                        },
                        onDuplicateRoutine = { routineToDup ->
                            library.duplicateRoutine(routineToDup.id, "${routineToDup.name} (Copie)")
                        },
                        onDeleteRoutine = { routineToDel ->
                            library.deleteRoutine(routineToDel.id)
                        },
                        onToggleRoutineDay = { routineId, dayInt ->
                            library.toggleRoutineDay(routineId, dayInt)
                        },
                        onOpenCatalog = {
                            isCatalogForRoutineSelection = false
                            showingCatalogScreen = true
                        }
                    )
                    2 -> AnalyticsScreen()
                    3 -> ProfileScreen(onOpenSettings = { showingSettingsScreen = true })
                }
            }
        }
    }
}
