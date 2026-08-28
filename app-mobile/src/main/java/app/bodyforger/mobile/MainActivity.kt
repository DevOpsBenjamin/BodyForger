package app.bodyforger.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.core.database.data.DefaultExercises
import app.bodyforger.core.database.entity.toDomain
import app.bodyforger.core.model.EquipmentType
import app.bodyforger.core.model.Exercise
import app.bodyforger.core.model.MuscleGroup
import app.bodyforger.core.model.Routine
import app.bodyforger.core.model.RoutineExercise
import app.bodyforger.core.model.RoutineSet
import app.bodyforger.core.model.RoutineSetType
import app.bodyforger.mobile.ui.screens.AnalyticsScreen
import app.bodyforger.mobile.ui.screens.CatalogScreen
import app.bodyforger.mobile.ui.screens.CreateExerciseScreen
import app.bodyforger.mobile.ui.screens.HomeScreen
import app.bodyforger.mobile.ui.screens.PlannerScreen
import app.bodyforger.mobile.ui.screens.ProfileScreen
import app.bodyforger.mobile.ui.screens.RoutineEditorScreen
import app.bodyforger.mobile.ui.screens.WorkoutScreen
import app.bodyforger.mobile.ui.screens.toRoutineExercise
import app.bodyforger.mobile.ui.theme.BodyForgerTheme
import app.bodyforger.mobile.ui.theme.CrimsonRed
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

sealed class NavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : NavItem("Accueil", Icons.Filled.Home, Icons.Outlined.Home)
    object Planner : NavItem("Programme", Icons.Filled.FitnessCenter, Icons.Outlined.FitnessCenter)
    object Analytics : NavItem("Stats", Icons.Filled.MonitorWeight, Icons.Outlined.MonitorWeight)
    object Profile : NavItem("Profil", Icons.Filled.Person, Icons.Outlined.Person)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BodyForgerTheme {
                MobileMainScaffold()
            }
        }
    }
}

@Composable
fun MobileMainScaffold() {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var isLiveWorkoutRunning by remember { mutableStateOf(false) }
    var showingLiveWorkoutScreen by remember { mutableStateOf(false) }
    var showingCatalogScreen by remember { mutableStateOf(false) }
    var showingCreateExerciseScreen by remember { mutableStateOf(false) }
    var showingRoutineEditor by remember { mutableStateOf(false) }
    var editingRoutine by remember { mutableStateOf<Routine?>(null) }

    // État de sélection d'exercice depuis le catalogue pour l'éditeur de routine
    var isCatalogForRoutineSelection by remember { mutableStateOf(false) }
    var catalogReplaceExerciseIndex by remember { mutableStateOf<Int?>(null) }

    // Liste partagée des exercices personnalisés créés
    val customExercises = remember { mutableStateListOf<Exercise>() }

    // Liste partagée des routines créées
    val routines = remember {
        mutableStateListOf(
            Routine(
                id = "r_push_1",
                name = "Push Hypertrophie",
                notes = "Focus pectoraux et triceps en surcharge progressive",
                assignedDays = setOf(1, 4), // Lundi et Jeudi
                exercises = listOf(
                    RoutineExercise(
                        id = UUID.randomUUID().toString(),
                        routineId = "r_push_1",
                        exerciseId = "bf_bench_press",
                        exerciseName = "Développé Couché",
                        primaryMuscle = MuscleGroup.CHEST,
                        equipment = EquipmentType.BARBELL,
                        orderIndex = 0,
                        restTimeSeconds = 120,
                        sets = listOf(
                            RoutineSet(setIndex = 1, type = RoutineSetType.WARMUP, reps = 15, targetWeightKg = 50.0),
                            RoutineSet(setIndex = 2, type = RoutineSetType.NORMAL, minReps = 8, maxReps = 12, isRepsRange = true, targetWeightKg = 85.0),
                            RoutineSet(setIndex = 3, type = RoutineSetType.NORMAL, minReps = 8, maxReps = 12, isRepsRange = true, targetWeightKg = 85.0)
                        )
                    ),
                    RoutineExercise(
                        id = UUID.randomUUID().toString(),
                        routineId = "r_push_1",
                        exerciseId = "bf_incline_dumbbell_press",
                        exerciseName = "Développé Incliné Haltères",
                        primaryMuscle = MuscleGroup.CHEST,
                        equipment = EquipmentType.DUMBBELL,
                        orderIndex = 1,
                        restTimeSeconds = 90,
                        sets = listOf(
                            RoutineSet(setIndex = 1, type = RoutineSetType.NORMAL, reps = 10, targetWeightKg = 30.0),
                            RoutineSet(setIndex = 2, type = RoutineSetType.NORMAL, reps = 10, targetWeightKg = 30.0),
                            RoutineSet(setIndex = 3, type = RoutineSetType.FAILURE, reps = 10, targetWeightKg = 30.0)
                        )
                    ),
                    RoutineExercise(
                        id = UUID.randomUUID().toString(),
                        routineId = "r_push_1",
                        exerciseId = "bf_lateral_raise_cable",
                        exerciseName = "Élévations Latérales Poulie Unilatérale",
                        primaryMuscle = MuscleGroup.SHOULDERS,
                        equipment = EquipmentType.CABLE,
                        isUnilateral = true,
                        orderIndex = 2,
                        restTimeSeconds = 60,
                        sets = listOf(
                            RoutineSet(setIndex = 1, type = RoutineSetType.NORMAL, reps = 15, targetWeightKg = 12.0),
                            RoutineSet(setIndex = 2, type = RoutineSetType.NORMAL, reps = 15, targetWeightKg = 12.0),
                            RoutineSet(setIndex = 3, type = RoutineSetType.DROPSET, reps = 15, targetWeightKg = 12.0)
                        )
                    )
                )
            ),
            Routine(
                id = "r_pull_1",
                name = "Pull Dos & Bras",
                notes = "Largeur de dos et travail des fléchisseurs de bras",
                assignedDays = setOf(2, 5), // Mardi et Vendredi
                exercises = listOf(
                    RoutineExercise(
                        id = UUID.randomUUID().toString(),
                        routineId = "r_pull_1",
                        exerciseId = "bf_lat_pulldown",
                        exerciseName = "Tirage Vertical Poitrine",
                        primaryMuscle = MuscleGroup.BACK,
                        equipment = EquipmentType.CABLE,
                        orderIndex = 0,
                        restTimeSeconds = 90,
                        sets = listOf(
                            RoutineSet(setIndex = 1, type = RoutineSetType.NORMAL, reps = 10, targetWeightKg = 70.0),
                            RoutineSet(setIndex = 2, type = RoutineSetType.NORMAL, reps = 10, targetWeightKg = 70.0),
                            RoutineSet(setIndex = 3, type = RoutineSetType.NORMAL, reps = 10, targetWeightKg = 70.0)
                        )
                    ),
                    RoutineExercise(
                        id = UUID.randomUUID().toString(),
                        routineId = "r_pull_1",
                        exerciseId = "bf_cable_row_seated",
                        exerciseName = "Tirage Horizontal Poulie Basse",
                        primaryMuscle = MuscleGroup.BACK,
                        equipment = EquipmentType.CABLE,
                        orderIndex = 1,
                        restTimeSeconds = 90,
                        sets = listOf(
                            RoutineSet(setIndex = 1, type = RoutineSetType.NORMAL, reps = 12, targetWeightKg = 65.0),
                            RoutineSet(setIndex = 2, type = RoutineSetType.NORMAL, reps = 12, targetWeightKg = 65.0)
                        )
                    ),
                    RoutineExercise(
                        id = UUID.randomUUID().toString(),
                        routineId = "r_pull_1",
                        exerciseId = "bf_incline_dumbbell_curl",
                        exerciseName = "Curl Biceps Incliné",
                        primaryMuscle = MuscleGroup.BICEPS,
                        equipment = EquipmentType.DUMBBELL,
                        orderIndex = 2,
                        restTimeSeconds = 75,
                        sets = listOf(
                            RoutineSet(setIndex = 1, type = RoutineSetType.NORMAL, reps = 10, targetWeightKg = 16.0),
                            RoutineSet(setIndex = 2, type = RoutineSetType.NORMAL, reps = 10, targetWeightKg = 16.0)
                        )
                    )
                )
            ),
            Routine(
                id = "r_legs_1",
                name = "Legs & Abdos",
                notes = "Chaîne antérieure et postérieure",
                assignedDays = setOf(3), // Mercredi
                exercises = listOf(
                    RoutineExercise(
                        id = UUID.randomUUID().toString(),
                        routineId = "r_legs_1",
                        exerciseId = "bf_squat_barbell",
                        exerciseName = "Squat Barre Arrière",
                        primaryMuscle = MuscleGroup.QUADRICEPS,
                        equipment = EquipmentType.BARBELL,
                        orderIndex = 0,
                        restTimeSeconds = 180,
                        sets = listOf(
                            RoutineSet(setIndex = 1, type = RoutineSetType.WARMUP, reps = 12, targetWeightKg = 60.0),
                            RoutineSet(setIndex = 2, type = RoutineSetType.NORMAL, minReps = 6, maxReps = 10, isRepsRange = true, targetWeightKg = 110.0),
                            RoutineSet(setIndex = 3, type = RoutineSetType.NORMAL, minReps = 6, maxReps = 10, isRepsRange = true, targetWeightKg = 110.0)
                        )
                    )
                )
            ),
            Routine(
                id = "r_upper_1",
                name = "Upper Body Express (45 min)",
                notes = "Séance rapide haut du corps pour les journées chargées",
                assignedDays = emptySet(),
                exercises = listOf(
                    RoutineExercise(
                        id = UUID.randomUUID().toString(),
                        routineId = "r_upper_1",
                        exerciseId = "bf_bench_press",
                        exerciseName = "Développé Couché",
                        primaryMuscle = MuscleGroup.CHEST,
                        equipment = EquipmentType.BARBELL,
                        orderIndex = 0,
                        restTimeSeconds = 90,
                        sets = listOf(
                            RoutineSet(setIndex = 1, type = RoutineSetType.NORMAL, reps = 8, targetWeightKg = 80.0),
                            RoutineSet(setIndex = 2, type = RoutineSetType.NORMAL, reps = 8, targetWeightKg = 80.0)
                        )
                    ),
                    RoutineExercise(
                        id = UUID.randomUUID().toString(),
                        routineId = "r_upper_1",
                        exerciseId = "bf_lat_pulldown",
                        exerciseName = "Tirage Vertical Poitrine",
                        primaryMuscle = MuscleGroup.BACK,
                        equipment = EquipmentType.CABLE,
                        orderIndex = 1,
                        restTimeSeconds = 90,
                        sets = listOf(
                            RoutineSet(setIndex = 1, type = RoutineSetType.NORMAL, reps = 10, targetWeightKg = 65.0),
                            RoutineSet(setIndex = 2, type = RoutineSetType.NORMAL, reps = 10, targetWeightKg = 65.0)
                        )
                    )
                )
            ),
            Routine(
                id = "r_lower_iso_1",
                name = "Lower Body Focus Ischios",
                notes = "Travail ciblé chaîne postérieure et fessiers",
                assignedDays = emptySet(),
                exercises = listOf(
                    RoutineExercise(
                        id = UUID.randomUUID().toString(),
                        routineId = "r_lower_iso_1",
                        exerciseId = "bf_deadlift_romanian",
                        exerciseName = "Soulevé de Terre Roumain",
                        primaryMuscle = MuscleGroup.HAMSTRINGS,
                        equipment = EquipmentType.BARBELL,
                        orderIndex = 0,
                        restTimeSeconds = 120,
                        sets = listOf(
                            RoutineSet(setIndex = 1, type = RoutineSetType.NORMAL, reps = 10, targetWeightKg = 90.0),
                            RoutineSet(setIndex = 2, type = RoutineSetType.NORMAL, reps = 10, targetWeightKg = 90.0)
                        )
                    )
                )
            ),
            Routine(
                id = "r_arms_1",
                name = "Épaules & Bras (Arm Day)",
                notes = "Supersets biceps / triceps et deltoïdes latéraux",
                assignedDays = setOf(6), // Samedi
                exercises = listOf(
                    RoutineExercise(
                        id = UUID.randomUUID().toString(),
                        routineId = "r_arms_1",
                        exerciseId = "bf_lateral_raise_cable",
                        exerciseName = "Élévations Latérales Poulie",
                        primaryMuscle = MuscleGroup.SHOULDERS,
                        equipment = EquipmentType.CABLE,
                        orderIndex = 0,
                        restTimeSeconds = 60,
                        sets = listOf(
                            RoutineSet(setIndex = 1, type = RoutineSetType.NORMAL, reps = 15, targetWeightKg = 10.0),
                            RoutineSet(setIndex = 2, type = RoutineSetType.NORMAL, reps = 15, targetWeightKg = 10.0)
                        )
                    ),
                    RoutineExercise(
                        id = UUID.randomUUID().toString(),
                        routineId = "r_arms_1",
                        exerciseId = "bf_incline_dumbbell_curl",
                        exerciseName = "Curl Biceps Incliné",
                        primaryMuscle = MuscleGroup.BICEPS,
                        equipment = EquipmentType.DUMBBELL,
                        orderIndex = 1,
                        restTimeSeconds = 60,
                        sets = listOf(
                            RoutineSet(setIndex = 1, type = RoutineSetType.NORMAL, reps = 12, targetWeightKg = 14.0),
                            RoutineSet(setIndex = 2, type = RoutineSetType.DROPSET, reps = 12, targetWeightKg = 14.0)
                        )
                    )
                )
            ),
            Routine(
                id = "r_full_body_1",
                name = "Full Body Athlétique",
                notes = "Conditioning général et mouvements polyarticulaires",
                assignedDays = emptySet(),
                exercises = listOf(
                    RoutineExercise(
                        id = UUID.randomUUID().toString(),
                        routineId = "r_full_body_1",
                        exerciseId = "bf_squat_barbell",
                        exerciseName = "Squat Barre Arrière",
                        primaryMuscle = MuscleGroup.QUADRICEPS,
                        equipment = EquipmentType.BARBELL,
                        orderIndex = 0,
                        restTimeSeconds = 120,
                        sets = listOf(
                            RoutineSet(setIndex = 1, type = RoutineSetType.NORMAL, reps = 8, targetWeightKg = 100.0)
                        )
                    ),
                    RoutineExercise(
                        id = UUID.randomUUID().toString(),
                        routineId = "r_full_body_1",
                        exerciseId = "bf_bench_press",
                        exerciseName = "Développé Couché",
                        primaryMuscle = MuscleGroup.CHEST,
                        equipment = EquipmentType.BARBELL,
                        orderIndex = 1,
                        restTimeSeconds = 120,
                        sets = listOf(
                            RoutineSet(setIndex = 1, type = RoutineSetType.NORMAL, reps = 8, targetWeightKg = 80.0)
                        )
                    )
                )
            ),
            Routine(
                id = "r_cardio_core_1",
                name = "Cardio & Core Hiit",
                notes = "Gainage abdominal, obliques et mobilité active",
                assignedDays = emptySet(),
                exercises = listOf(
                    RoutineExercise(
                        id = UUID.randomUUID().toString(),
                        routineId = "r_cardio_core_1",
                        exerciseId = "bf_plank",
                        exerciseName = "Gainage Planche Ventrale",
                        primaryMuscle = MuscleGroup.ABS,
                        equipment = EquipmentType.BODYWEIGHT,
                        orderIndex = 0,
                        restTimeSeconds = 45,
                        sets = listOf(
                            RoutineSet(setIndex = 1, type = RoutineSetType.NORMAL, reps = 60),
                            RoutineSet(setIndex = 2, type = RoutineSetType.NORMAL, reps = 60)
                        )
                    )
                )
            )
        )
    }

    val navItems = listOf(NavItem.Home, NavItem.Planner, NavItem.Analytics, NavItem.Profile)

    if (showingCreateExerciseScreen) {
        // Vue plein écran de création d'exercice
        CreateExerciseScreen(
            onBack = { showingCreateExerciseScreen = false },
            onExerciseCreated = { newExercise ->
                customExercises.add(0, newExercise)
                showingCreateExerciseScreen = false
            }
        )
    } else if (showingCatalogScreen) {
        // Vue plein écran du catalogue d'exercices
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
        // Vue plein écran de l'éditeur de routine
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
        // Vue plein écran de la séance active
        WorkoutScreen(
            onMinimize = {
                showingLiveWorkoutScreen = false
            },
            onFinishWorkout = {
                isLiveWorkoutRunning = false
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
                    // Mini-barre flottante de séance active si une séance tourne en fond
                    AnimatedVisibility(
                        visible = isLiveWorkoutRunning,
                        enter = slideInVertically(initialOffsetY = { it }),
                        exit = slideOutVertically(targetOffsetY = { it })
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(SurfaceElevated)
                                .border(1.dp, NeonLime.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                .clickable { showingLiveWorkoutScreen = true }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(NeonLime)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "SÉANCE EN COURS",
                                        color = NeonLime,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "Push Hypertrophie • 42:15",
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "REPRENDRE",
                                    color = NeonLime,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = NeonLime,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    NavigationBar(
                        containerColor = SurfaceDark,
                        tonalElevation = 0.dp,
                        modifier = Modifier.border(width = 1.dp, color = SurfaceBorder)
                    ) {
                        navItems.forEachIndexed { index, item ->
                            val isSelected = selectedTabIndex == index
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { selectedTabIndex = index },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.title,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.title,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = NeonLime,
                                    indicatorColor = NeonLime,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextMuted
                                )
                            )
                        }
                    }
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
                            isLiveWorkoutRunning = true
                            showingLiveWorkoutScreen = true
                        },
                        onNavigateToBiometrics = { selectedTabIndex = 2 },
                        onOpenSettings = { selectedTabIndex = 3 }
                    )
                    1 -> PlannerScreen(
                        routines = routines,
                        onStartWorkout = {
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
                    3 -> ProfileScreen()
                }
            }
        }
    }
}
