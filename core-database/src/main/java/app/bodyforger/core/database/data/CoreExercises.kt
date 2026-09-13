package app.bodyforger.core.database.data

import app.bodyforger.core.database.entity.ExerciseEntity
import app.bodyforger.core.model.EquipmentType
import app.bodyforger.core.model.HealthConnectExerciseType
import app.bodyforger.core.model.MuscleGroup

/** Core work and whole-body movements. */
internal object CoreExercises {
    val all: List<ExerciseEntity> = listOf(
        DefaultExercises.exercise(
            id = "bf_abs_001",
            nameKey = "ex_hanging_leg_raise",
            name = "Hanging Leg Raise",
            type = HealthConnectExerciseType.LEG_RAISE,
            muscle = MuscleGroup.ABS,
            equipment = EquipmentType.BODYWEIGHT
        ),
        DefaultExercises.exercise(
            id = "bf_abs_002",
            nameKey = "ex_lying_leg_raise",
            name = "Lying Leg Raise",
            type = HealthConnectExerciseType.LEG_RAISE,
            muscle = MuscleGroup.ABS,
            equipment = EquipmentType.BODYWEIGHT
        ),
        DefaultExercises.exercise(
            id = "bf_abs_003",
            nameKey = "ex_cable_crunch",
            name = "Cable Crunch",
            type = HealthConnectExerciseType.CRUNCH,
            muscle = MuscleGroup.ABS,
            equipment = EquipmentType.CABLE
        ),
        DefaultExercises.exercise(
            id = "bf_abs_004",
            nameKey = "ex_seated_machine_crunch",
            name = "Seated Machine Crunch",
            type = HealthConnectExerciseType.CRUNCH,
            muscle = MuscleGroup.ABS,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_abs_005",
            nameKey = "ex_floor_crunch",
            name = "Floor Crunch",
            type = HealthConnectExerciseType.CRUNCH,
            muscle = MuscleGroup.ABS,
            equipment = EquipmentType.BODYWEIGHT
        ),
        DefaultExercises.exercise(
            id = "bf_abs_006",
            nameKey = "ex_plank",
            name = "Plank",
            type = HealthConnectExerciseType.PLANK,
            muscle = MuscleGroup.ABS,
            equipment = EquipmentType.BODYWEIGHT
        ),
        DefaultExercises.exercise(
            id = "bf_abs_007",
            nameKey = "ex_side_plank",
            name = "Side Plank",
            type = HealthConnectExerciseType.PLANK,
            muscle = MuscleGroup.ABS,
            equipment = EquipmentType.BODYWEIGHT,
            isUnilateral = true
        ),
        DefaultExercises.exercise(
            id = "bf_abs_008",
            nameKey = "ex_cable_pallof_press",
            name = "Cable Pallof Press",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            muscle = MuscleGroup.ABS,
            equipment = EquipmentType.CABLE,
            isUnilateral = true
        ),
        DefaultExercises.exercise(
            id = "bf_abs_009",
            nameKey = "ex_russian_twist",
            name = "Russian Twist",
            type = HealthConnectExerciseType.RUSSIAN_TWIST,
            muscle = MuscleGroup.ABS,
            equipment = EquipmentType.BODYWEIGHT
        ),
        DefaultExercises.exercise(
            id = "bf_abs_010",
            nameKey = "ex_ab_wheel_rollout",
            name = "Ab Wheel Rollout",
            type = HealthConnectExerciseType.AB_WHEEL_ROLLOUT,
            muscle = MuscleGroup.ABS,
            equipment = EquipmentType.BODYWEIGHT
        ),
        DefaultExercises.exercise(
            id = "bf_abs_012",
            nameKey = "ex_burpees",
            name = "Burpees",
            type = HealthConnectExerciseType.BURPEE,
            muscle = MuscleGroup.FULL_BODY,
            equipment = EquipmentType.BODYWEIGHT
        ),
        DefaultExercises.exercise(
            id = "bf_cardio_001",
            nameKey = "ex_elliptical_trainer",
            name = "Elliptical Trainer",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            category = app.bodyforger.core.model.WorkoutActivityCategory.ELLIPTICAL,
            muscle = MuscleGroup.FULL_BODY,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_cardio_002",
            nameKey = "ex_treadmill_incline_walk",
            name = "Treadmill (Incline Walk)",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            category = app.bodyforger.core.model.WorkoutActivityCategory.TREADMILL_WALKING,
            muscle = MuscleGroup.FULL_BODY,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_cardio_003",
            nameKey = "ex_treadmill_run",
            name = "Treadmill (Run)",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            category = app.bodyforger.core.model.WorkoutActivityCategory.TREADMILL_RUNNING,
            muscle = MuscleGroup.FULL_BODY,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_mob_001",
            nameKey = "ex_stretching_and_general_mobility",
            name = "Stretching & General Mobility",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            category = app.bodyforger.core.model.WorkoutActivityCategory.STRETCHING,
            muscle = MuscleGroup.FULL_BODY,
            equipment = EquipmentType.BODYWEIGHT
        ),
        DefaultExercises.exercise(
            id = "bf_mob_002",
            nameKey = "ex_foam_rolling",
            name = "Foam Rolling",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            category = app.bodyforger.core.model.WorkoutActivityCategory.STRETCHING,
            muscle = MuscleGroup.FULL_BODY,
            equipment = EquipmentType.OTHER
        ),
    )
}
