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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import app.bodyforger.core.model.Exercise
import app.bodyforger.core.model.Routine
import app.bodyforger.core.model.WorkoutSession
import app.bodyforger.mobile.data.DebugSampleRoutines
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
    var editingRoutine by remember { mutableStateOf<Routine?>(null) }
    var showingCatalogScreen by remember { mutableStateOf(false) }
    var showingCreateExerciseScreen by remember { mutableStateOf(false) }
    var isCatalogForRoutineSelection by remember { mutableStateOf(false) }
    var catalogReplaceExerciseIndex by remember { mutableStateOf<Int?>(null) }

    var isLiveWorkoutRunning by remember { mutableStateOf(false) }
    var showingLiveWorkoutScreen by remember { mutableStateOf(false) }
    var activeWorkoutRoutine by remember { mutableStateOf<Routine?>(null) }
    var showingSettingsScreen by remember { mutableStateOf(false) }

    val customExercises = remember { mutableStateListOf<Exercise>() }
    val routines = remember {
        mutableStateListOf<Routine>().apply {
            addAll(DebugSampleRoutines.list)
        }
    }
    val completedSessions = remember { mutableStateListOf<WorkoutSession>() }

    val navItems = listOf(NavItem.Home, NavItem.Planner, NavItem.Analytics, NavItem.Profile)

    if (showingCreateExerciseScreen) {
        CreateExerciseScreen(
            onBack = { showingCreateExerciseScreen = false },
            onExerciseCreated = { newExercise ->
                customExercises.add(0, newExercise)
                showingCreateExerciseScreen = false
            }
        )
    } else if (showingSettingsScreen) {
        SettingsScreen(
            // Profil de démonstration : la saisie du profil athlète reste à câbler.
            profile = BiaProfile(BiologicalSex.MALE, ageYears = 30, heightCm = 180.0),
            onBack = { showingSettingsScreen = false }
        )
    } else if (showingCatalogScreen) {
        CatalogScreen(
            customExercises = customExercises,
            isSelectionMode = isCatalogForRoutineSelection,
            onBack = {
                showingCatalogScreen = false
                isCatalogForRoutineSelection = false
                catalogReplaceExerciseIndex = null
            },
            onOpenCreateExercise = { showingCreateExerciseScreen = true },
            onSelectExercise = { selectedExercise ->
                if (isCatalogForRoutineSelection && showingRoutineEditor) {
                    val currentDraft = editingRoutine ?: Routine(name = "")
                    val newRoutineEx = selectedExercise.toRoutineExercise(currentDraft.id)
                    val updatedExercises = currentDraft.exercises.toMutableList()

                    if (catalogReplaceExerciseIndex != null && catalogReplaceExerciseIndex in updatedExercises.indices) {
                        updatedExercises[catalogReplaceExerciseIndex!!] = newRoutineEx
                    } else {
                        updatedExercises.add(newRoutineEx)
                    }

                    editingRoutine = currentDraft.copy(exercises = updatedExercises)
                    showingCatalogScreen = false
                    isCatalogForRoutineSelection = false
                    catalogReplaceExerciseIndex = null
                }
            }
        )
    } else if (showingRoutineEditor) {
        RoutineEditorScreen(
            initialRoutine = editingRoutine,
            onBack = {
                showingRoutineEditor = false
                editingRoutine = null
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
                val existingIndex = routines.indexOfFirst { it.id == savedRoutine.id }
                if (existingIndex != -1) {
                    routines[existingIndex] = savedRoutine
                } else {
                    routines.add(0, savedRoutine)
                }
                showingRoutineEditor = false
                editingRoutine = null
            }
        )
    } else if (showingLiveWorkoutScreen) {
        WorkoutScreen(
            initialRoutine = activeWorkoutRoutine,
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
            onFinishWorkout = { finishedSession ->
                completedSessions.add(0, finishedSession)
                isLiveWorkoutRunning = false
                showingLiveWorkoutScreen = false
                activeWorkoutRoutine = null
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
                        isVisible = isLiveWorkoutRunning,
                        workoutTitle = "${activeWorkoutRoutine?.name ?: "Séance Active"}",
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
                            activeWorkoutRoutine = routines.firstOrNull()
                            isLiveWorkoutRunning = true
                            showingLiveWorkoutScreen = true
                        },
                        onNavigateToBiometrics = { selectedTabIndex = 2 },
                        onOpenSettings = { showingSettingsScreen = true }
                    )
                    1 -> PlannerScreen(
                        routines = routines,
                        onStartWorkout = {
                            activeWorkoutRoutine = routines.firstOrNull()
                            isLiveWorkoutRunning = true
                            showingLiveWorkoutScreen = true
                        },
                        onCreateNewRoutine = {
                            editingRoutine = null
                            showingRoutineEditor = true
                        },
                        onEditRoutine = { routineToEdit ->
                            editingRoutine = routineToEdit
                            showingRoutineEditor = true
                        },
                        onDuplicateRoutine = { routineToDup ->
                            val duplicated = routineToDup.copy(
                                id = UUID.randomUUID().toString(),
                                name = "${routineToDup.name} (Copie)",
                                createdAtEpochMs = System.currentTimeMillis()
                            )
                            routines.add(0, duplicated)
                        },
                        onDeleteRoutine = { routineToDel ->
                            routines.remove(routineToDel)
                        },
                        onToggleRoutineDay = { routineId, dayInt ->
                            val routineIndex = routines.indexOfFirst { it.id == routineId }
                            if (routineIndex != -1) {
                                val current = routines[routineIndex]
                                val updatedDays = if (current.assignedDays.contains(dayInt)) {
                                    current.assignedDays - dayInt
                                } else {
                                    current.assignedDays + dayInt
                                }
                                routines[routineIndex] = current.copy(assignedDays = updatedDays)
                            }
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
