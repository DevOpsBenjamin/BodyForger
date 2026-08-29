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
            name = "Relevés de Jambes Suspendu",
            type = HealthConnectExerciseType.LEG_RAISE,
            muscle = MuscleGroup.ABS,
            equipment = EquipmentType.BODYWEIGHT
        ),
        DefaultExercises.exercise(
            id = "bf_abs_002",
            name = "Relevés de Jambes au Sol",
            type = HealthConnectExerciseType.LEG_RAISE,
            muscle = MuscleGroup.ABS,
            equipment = EquipmentType.BODYWEIGHT
        ),
        DefaultExercises.exercise(
            id = "bf_abs_003",
            name = "Crunch Poulie Haute",
            type = HealthConnectExerciseType.CRUNCH,
            muscle = MuscleGroup.ABS,
            equipment = EquipmentType.CABLE
        ),
        DefaultExercises.exercise(
            id = "bf_abs_004",
            name = "Crunch Machine Assise",
            type = HealthConnectExerciseType.CRUNCH,
            muscle = MuscleGroup.ABS,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_abs_005",
            name = "Crunch Sol",
            type = HealthConnectExerciseType.CRUNCH,
            muscle = MuscleGroup.ABS,
            equipment = EquipmentType.BODYWEIGHT
        ),
        DefaultExercises.exercise(
            id = "bf_abs_006",
            name = "Gainage Planche",
            type = HealthConnectExerciseType.PLANK,
            muscle = MuscleGroup.ABS,
            equipment = EquipmentType.BODYWEIGHT
        ),
        DefaultExercises.exercise(
            id = "bf_abs_007",
            name = "Gainage Latéral",
            type = HealthConnectExerciseType.PLANK,
            muscle = MuscleGroup.ABS,
            equipment = EquipmentType.BODYWEIGHT,
            isUnilateral = true
        ),
        DefaultExercises.exercise(
            id = "bf_abs_008",
            name = "Pallof Press Poulie",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            muscle = MuscleGroup.ABS,
            equipment = EquipmentType.CABLE,
            isUnilateral = true
        ),
        DefaultExercises.exercise(
            id = "bf_abs_009",
            name = "Russian Twist",
            type = HealthConnectExerciseType.RUSSIAN_TWIST,
            muscle = MuscleGroup.ABS,
            equipment = EquipmentType.BODYWEIGHT
        ),
        DefaultExercises.exercise(
            id = "bf_abs_010",
            name = "Roue Abdominale",
            type = HealthConnectExerciseType.AB_WHEEL_ROLLOUT,
            muscle = MuscleGroup.ABS,
            equipment = EquipmentType.BODYWEIGHT
        ),
        DefaultExercises.exercise(
            id = "bf_abs_012",
            name = "Burpees",
            type = HealthConnectExerciseType.BURPEE,
            muscle = MuscleGroup.FULL_BODY,
            equipment = EquipmentType.BODYWEIGHT
        ),
        DefaultExercises.exercise(
            id = "bf_cardio_001",
            name = "Vélo Elliptique",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            category = app.bodyforger.core.model.WorkoutActivityCategory.ELLIPTICAL,
            muscle = MuscleGroup.FULL_BODY,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_cardio_002",
            name = "Tapis de Course (Marche Inclinée)",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            category = app.bodyforger.core.model.WorkoutActivityCategory.TREADMILL_WALKING,
            muscle = MuscleGroup.FULL_BODY,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_cardio_003",
            name = "Tapis de Course (Course)",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            category = app.bodyforger.core.model.WorkoutActivityCategory.TREADMILL_RUNNING,
            muscle = MuscleGroup.FULL_BODY,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_mob_001",
            name = "Étirements & Mobilité Globale",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            category = app.bodyforger.core.model.WorkoutActivityCategory.STRETCHING,
            muscle = MuscleGroup.FULL_BODY,
            equipment = EquipmentType.BODYWEIGHT
        ),
        DefaultExercises.exercise(
            id = "bf_mob_002",
            name = "Rouleau de Massage (Foam Roller)",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            category = app.bodyforger.core.model.WorkoutActivityCategory.STRETCHING,
            muscle = MuscleGroup.FULL_BODY,
            equipment = EquipmentType.OTHER
        ),
    )
}
