package app.bodyforger.core.database.data

import app.bodyforger.core.database.entity.ExerciseEntity
import app.bodyforger.core.model.EquipmentType
import app.bodyforger.core.model.HealthConnectExerciseType
import app.bodyforger.core.model.MuscleGroup

/** Quadriceps, hamstrings, glutes and calves. */
internal object LegExercises {
    val all: List<ExerciseEntity> = listOf(
        DefaultExercises.exercise(
            id = "bf_leg_001",
            name = "Back Squat",
            type = HealthConnectExerciseType.SQUAT,
            muscle = MuscleGroup.QUADRICEPS,
            equipment = EquipmentType.BARBELL
        ),
        DefaultExercises.exercise(
            id = "bf_leg_002",
            name = "Front Squat",
            type = HealthConnectExerciseType.FRONT_SQUAT,
            muscle = MuscleGroup.QUADRICEPS,
            equipment = EquipmentType.BARBELL
        ),
        DefaultExercises.exercise(
            id = "bf_leg_003",
            name = "Hack Squat Machine",
            type = HealthConnectExerciseType.SQUAT,
            muscle = MuscleGroup.QUADRICEPS,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_leg_004",
            name = "Squat Smith Machine",
            type = HealthConnectExerciseType.SQUAT,
            muscle = MuscleGroup.QUADRICEPS,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_leg_005",
            name = "Belt Squat Machine",
            type = HealthConnectExerciseType.SQUAT,
            muscle = MuscleGroup.QUADRICEPS,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_leg_006",
            name = "Presse à Cuisses Inclinée 45°",
            type = HealthConnectExerciseType.LEG_PRESS,
            muscle = MuscleGroup.QUADRICEPS,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_leg_007",
            name = "Presse à Cuisses Horizontale",
            type = HealthConnectExerciseType.LEG_PRESS,
            muscle = MuscleGroup.QUADRICEPS,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_leg_008",
            name = "Leg Extension Machine",
            type = HealthConnectExerciseType.LEG_EXTENSION,
            muscle = MuscleGroup.QUADRICEPS,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_leg_009",
            name = "Leg Extension Unilatérale Machine",
            type = HealthConnectExerciseType.LEG_EXTENSION,
            muscle = MuscleGroup.QUADRICEPS,
            equipment = EquipmentType.MACHINE,
            isUnilateral = true
        ),
        DefaultExercises.exercise(
            id = "bf_leg_015",
            name = "Split Squat Bulgare",
            type = HealthConnectExerciseType.LUNGE,
            muscle = MuscleGroup.QUADRICEPS,
            equipment = EquipmentType.DUMBBELL,
            isUnilateral = true
        ),
        DefaultExercises.exercise(
            id = "bf_leg_016",
            name = "Fentes Marchantes",
            type = HealthConnectExerciseType.LUNGE,
            muscle = MuscleGroup.QUADRICEPS,
            equipment = EquipmentType.DUMBBELL,
            isUnilateral = true
        ),
        DefaultExercises.exercise(
            id = "bf_cardio_004",
            name = "Vélo Stationnaire / Biking",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            category = app.bodyforger.core.model.WorkoutActivityCategory.STATIONARY_BIKING,
            muscle = MuscleGroup.QUADRICEPS,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_leg_010",
            name = "Leg Curl Assis Machine",
            type = HealthConnectExerciseType.LEG_CURL,
            muscle = MuscleGroup.HAMSTRINGS,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_leg_011",
            name = "Leg Curl Couché Machine",
            type = HealthConnectExerciseType.LEG_CURL,
            muscle = MuscleGroup.HAMSTRINGS,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_leg_012",
            name = "Leg Curl Unilatéral Machine",
            type = HealthConnectExerciseType.LEG_CURL,
            muscle = MuscleGroup.HAMSTRINGS,
            equipment = EquipmentType.MACHINE,
            isUnilateral = true
        ),
        DefaultExercises.exercise(
            id = "bf_leg_013",
            name = "Soulevé de Terre Roumain Barre",
            type = HealthConnectExerciseType.ROMANIAN_DEADLIFT,
            muscle = MuscleGroup.HAMSTRINGS,
            equipment = EquipmentType.BARBELL
        ),
        DefaultExercises.exercise(
            id = "bf_leg_014",
            name = "Soulevé de Terre Roumain Haltères",
            type = HealthConnectExerciseType.ROMANIAN_DEADLIFT,
            muscle = MuscleGroup.HAMSTRINGS,
            equipment = EquipmentType.DUMBBELL
        ),
        DefaultExercises.exercise(
            id = "bf_leg_017",
            name = "Hip Thrust Barre",
            type = HealthConnectExerciseType.HIP_THRUST,
            muscle = MuscleGroup.GLUTES,
            equipment = EquipmentType.BARBELL
        ),
        DefaultExercises.exercise(
            id = "bf_leg_018",
            name = "Hip Thrust Machine",
            type = HealthConnectExerciseType.HIP_THRUST,
            muscle = MuscleGroup.GLUTES,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_leg_019",
            name = "Abducteurs Machine",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            muscle = MuscleGroup.GLUTES,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_leg_020",
            name = "Adducteurs Machine",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            muscle = MuscleGroup.GLUTES,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_leg_021",
            name = "Kickback Fessier Poulie",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            muscle = MuscleGroup.GLUTES,
            equipment = EquipmentType.CABLE,
            isUnilateral = true
        ),
        DefaultExercises.exercise(
            id = "bf_abs_011",
            name = "Kettlebell Swing",
            type = HealthConnectExerciseType.KETTLEBELL_SWING,
            muscle = MuscleGroup.GLUTES,
            equipment = EquipmentType.KETTLEBELL
        ),
        DefaultExercises.exercise(
            id = "bf_cardio_007",
            name = "StairMaster (Simulateur d'Escaliers)",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            category = app.bodyforger.core.model.WorkoutActivityCategory.STATIONARY_BIKING,
            muscle = MuscleGroup.GLUTES,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_leg_022",
            name = "Mollets Debout Machine",
            type = HealthConnectExerciseType.CALF_RAISE,
            muscle = MuscleGroup.CALVES,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_leg_023",
            name = "Mollets Assis Machine",
            type = HealthConnectExerciseType.CALF_RAISE,
            muscle = MuscleGroup.CALVES,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_leg_024",
            name = "Mollets Presse à Cuisses",
            type = HealthConnectExerciseType.CALF_RAISE,
            muscle = MuscleGroup.CALVES,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_cardio_006",
            name = "Corde à Sauter",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            category = app.bodyforger.core.model.WorkoutActivityCategory.HIIT,
            muscle = MuscleGroup.CALVES,
            equipment = EquipmentType.OTHER
        ),
    )
}
