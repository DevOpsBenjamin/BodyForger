package app.bodyforger.mobile.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import app.bodyforger.core.model.Exercise
import app.bodyforger.core.model.Routine
import app.bodyforger.mobile.R
import app.bodyforger.mobile.library.LibraryViewModel
import app.bodyforger.mobile.library.RoutineDraftViewModel
import app.bodyforger.mobile.ui.screens.AnalyticsScreen
import app.bodyforger.mobile.ui.screens.CatalogScreen
import app.bodyforger.mobile.ui.screens.CreateExerciseScreen
import app.bodyforger.mobile.ui.screens.HomeScreen
import app.bodyforger.mobile.ui.screens.PlannerScreen
import app.bodyforger.mobile.ui.screens.ProfileScreen
import app.bodyforger.mobile.ui.screens.RoutineEditorScreen
import app.bodyforger.mobile.ui.screens.SettingsScreen
import app.bodyforger.mobile.ui.screens.WorkoutScreen
import app.bodyforger.mobile.ui.screens.toRoutineExercise
import app.bodyforger.mobile.workout.LiveWorkoutViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * The whole graph: every destination, and what each one does on the way out.
 *
 * The screens themselves know nothing of navigation — they raise an intent, the graph decides
 * where it leads.
 */
@Composable
fun BodyForgerNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    library: LibraryViewModel = koinViewModel(),
    workout: LiveWorkoutViewModel = koinViewModel(),
    routineDraft: RoutineDraftViewModel = koinViewModel()
) {
    val routines by library.routines.collectAsState()
    val exercises by library.exercises.collectAsState()
    val freeSessionTitle = stringResource(R.string.workout_live_free_session_title)

    NavHost(
        navController = navController,
        startDestination = Destination.Home,
        modifier = modifier
    ) {
        composable<Destination.Home> {
            HomeScreen(
                onNavigateToWorkout = {
                    workout.begin(routine = null, freeSessionTitle = freeSessionTitle)
                    navController.navigate(Destination.LiveWorkout)
                },
                onNavigateToBiometrics = { navController.switchTab(Tab.ANALYTICS) },
                onOpenSettings = { navController.navigate(Destination.Settings) }
            )
        }

        composable<Destination.Planner> {
            PlannerScreen(
                routines = routines,
                onStartWorkout = { routineId ->
                    workout.begin(
                        routine = routines.firstOrNull { it.id == routineId },
                        freeSessionTitle = freeSessionTitle
                    )
                    navController.navigate(Destination.LiveWorkout)
                },
                onCreateNewRoutine = {
                    routineDraft.open(null)
                    navController.navigate(Destination.RoutineEditor)
                },
                onEditRoutine = { routine ->
                    routineDraft.open(routine)
                    navController.navigate(Destination.RoutineEditor)
                },
                onDuplicateRoutine = { library.duplicateRoutine(it.id, "${it.name} (Copie)") },
                onDeleteRoutine = { library.deleteRoutine(it.id) },
                onToggleRoutineDay = library::toggleRoutineDay,
                onOpenCatalog = { navController.navigate(Destination.Catalogue) }
            )
        }

        composable<Destination.Analytics> {
            AnalyticsScreen(onOpenScale = { navController.navigate(Destination.Settings) })
        }

        composable<Destination.Profile> {
            ProfileScreen(onOpenSettings = { navController.navigate(Destination.Settings) })
        }

        composable<Destination.Settings> {
            SettingsScreen(onBack = navController::navigateUp)
        }

        composable<Destination.RoutineEditor> {
            RoutineEditorScreen(
                draftViewModel = routineDraft,
                onBack = {
                    routineDraft.close()
                    navController.navigateUp()
                },
                onOpenCatalogForAdd = { navController.navigate(Destination.AddToRoutine) },
                onOpenCatalogForReplace = { navController.navigate(Destination.ReplaceInRoutine(it)) },
                onSaveRoutine = { saved ->
                    library.saveRoutine(saved)
                    routineDraft.close()
                    navController.navigateUp()
                }
            )
        }

        composable<Destination.LiveWorkout> {
            val active by workout.active.collectAsState()

            // Une séance terminée ou supprimée ne laisse rien à afficher. Sans cette sortie,
            // l'écran restait vide et l'athlète n'avait plus de chemin.
            LaunchedEffect(active) {
                if (active == null) navController.switchTab(Tab.HOME)
            }

            if (active != null) {
                WorkoutScreen(
                    workoutViewModel = workout,
                    onMinimize = navController::navigateUp,
                    onOpenCatalogForAdd = { navController.navigate(Destination.AddToWorkout) },
                    onOpenCatalogForReplace = { navController.navigate(Destination.ReplaceInWorkout(it)) },
                    onFinishWorkout = { navController.switchTab(Tab.HOME) },
                    onLeaveWorkout = { navController.switchTab(Tab.HOME) }
                )
            }
        }

        composable<Destination.CreateExercise> {
            CreateExerciseScreen(
                onBack = navController::navigateUp,
                onExerciseCreated = {
                    library.addCustomExercise(it)
                    navController.navigateUp()
                }
            )
        }

        // Le catalogue sert trois intentions ; chacune a sa route, donc sa réponse.
        composable<Destination.Catalogue> {
            CatalogScreen(
                exercises = exercises,
                isSelectionMode = false,
                onBack = navController::navigateUp,
                onOpenCreateExercise = { navController.navigate(Destination.CreateExercise) },
                onSelectExercise = {}
            )
        }

        composable<Destination.AddToRoutine> {
            ExercisePicker(navController, exercises) { chosen ->
                routineDraft.addExercise(chosen.toRoutineExercise(routineDraft.draftId()))
            }
        }

        composable<Destination.ReplaceInRoutine> { entry ->
            val index = entry.toRoute<Destination.ReplaceInRoutine>().index
            ExercisePicker(navController, exercises) { chosen ->
                routineDraft.addExercise(chosen.toRoutineExercise(routineDraft.draftId()), replacing = index)
            }
        }

        // Un exercice ajouté en séance ne rejoint aucune routine.
        composable<Destination.AddToWorkout> {
            ExercisePicker(navController, exercises) { chosen ->
                workout.addExercise(chosen.toRoutineExercise(routineId = ""))
            }
        }

        composable<Destination.ReplaceInWorkout> { entry ->
            val index = entry.toRoute<Destination.ReplaceInWorkout>().index
            ExercisePicker(navController, exercises) { chosen ->
                workout.replaceExercise(index, chosen.toRoutineExercise(routineId = ""))
            }
        }
    }
}

/**
 * The catalogue in picking mode: the four callers differ only in what they do with the choice.
 */
@Composable
private fun ExercisePicker(
    navController: NavHostController,
    exercises: List<Exercise>,
    onChosen: (Exercise) -> Unit
) {
    CatalogScreen(
        exercises = exercises,
        isSelectionMode = true,
        onBack = navController::navigateUp,
        onOpenCreateExercise = { navController.navigate(Destination.CreateExercise) },
        onSelectExercise = { chosen ->
            onChosen(chosen)
            navController.navigateUp()
        }
    )
}

/** The draft's identifier, which the exercises being added belong to. */
private fun RoutineDraftViewModel.draftId(): String = draft.value?.id ?: Routine(name = "").id
