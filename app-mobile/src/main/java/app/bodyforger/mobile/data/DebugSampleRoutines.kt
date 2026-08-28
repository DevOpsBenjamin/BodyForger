package app.bodyforger.mobile.data

import app.bodyforger.core.model.EquipmentType
import app.bodyforger.core.model.MuscleGroup
import app.bodyforger.core.model.Routine
import app.bodyforger.core.model.RoutineExercise
import app.bodyforger.core.model.RoutineSet
import app.bodyforger.core.model.RoutineSetType
import app.bodyforger.core.model.WeightUnit
import app.bodyforger.core.model.WorkoutActivityCategory
import java.util.UUID

object DebugSampleRoutines {
    val list: List<Routine> = listOf(
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
                    weightUnit = WeightUnit.KG,
                    activityCategory = WorkoutActivityCategory.STRENGTH_TRAINING,
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
                    weightUnit = WeightUnit.KG,
                    activityCategory = WorkoutActivityCategory.STRENGTH_TRAINING,
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
                    weightUnit = WeightUnit.LBS,
                    activityCategory = WorkoutActivityCategory.STRENGTH_TRAINING,
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
                    weightUnit = WeightUnit.LBS,
                    activityCategory = WorkoutActivityCategory.STRENGTH_TRAINING,
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
                    weightUnit = WeightUnit.LBS,
                    activityCategory = WorkoutActivityCategory.STRENGTH_TRAINING,
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
                    weightUnit = WeightUnit.KG,
                    activityCategory = WorkoutActivityCategory.STRENGTH_TRAINING,
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
                    weightUnit = WeightUnit.KG,
                    activityCategory = WorkoutActivityCategory.STRENGTH_TRAINING,
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
                    weightUnit = WeightUnit.KG,
                    activityCategory = WorkoutActivityCategory.STRENGTH_TRAINING,
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
                    weightUnit = WeightUnit.LBS,
                    activityCategory = WorkoutActivityCategory.STRENGTH_TRAINING,
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
                    weightUnit = WeightUnit.KG,
                    activityCategory = WorkoutActivityCategory.STRENGTH_TRAINING,
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
                    weightUnit = WeightUnit.LBS,
                    activityCategory = WorkoutActivityCategory.STRENGTH_TRAINING,
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
                    weightUnit = WeightUnit.KG,
                    activityCategory = WorkoutActivityCategory.STRENGTH_TRAINING,
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
                    weightUnit = WeightUnit.KG,
                    activityCategory = WorkoutActivityCategory.STRENGTH_TRAINING,
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
                    weightUnit = WeightUnit.KG,
                    activityCategory = WorkoutActivityCategory.STRENGTH_TRAINING,
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
                    weightUnit = WeightUnit.KG,
                    activityCategory = WorkoutActivityCategory.CALISTHENICS,
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
