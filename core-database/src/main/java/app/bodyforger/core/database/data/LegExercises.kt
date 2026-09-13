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
            nameKey = "ex_back_squat",
            name = "Back Squat",
            type = HealthConnectExerciseType.SQUAT,
            muscle = MuscleGroup.QUADRICEPS,
            equipment = EquipmentType.BARBELL
        ),
        DefaultExercises.exercise(
            id = "bf_leg_002",
            nameKey = "ex_front_squat",
            name = "Front Squat",
            type = HealthConnectExerciseType.FRONT_SQUAT,
            muscle = MuscleGroup.QUADRICEPS,
            equipment = EquipmentType.BARBELL
        ),
        DefaultExercises.exercise(
            id = "bf_leg_003",
            nameKey = "ex_hack_squat_machine",
            name = "Hack Squat Machine",
            type = HealthConnectExerciseType.SQUAT,
            muscle = MuscleGroup.QUADRICEPS,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_leg_004",
            nameKey = "ex_smith_machine_squat",
            name = "Smith Machine Squat",
            type = HealthConnectExerciseType.SQUAT,
            muscle = MuscleGroup.QUADRICEPS,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_leg_005",
            nameKey = "ex_belt_squat_machine",
            name = "Belt Squat Machine",
            type = HealthConnectExerciseType.SQUAT,
            muscle = MuscleGroup.QUADRICEPS,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_leg_006",
            nameKey = "ex_45_incline_leg_press",
            name = "45° Incline Leg Press",
            type = HealthConnectExerciseType.LEG_PRESS,
            muscle = MuscleGroup.QUADRICEPS,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_leg_007",
            nameKey = "ex_horizontal_leg_press",
            name = "Horizontal Leg Press",
            type = HealthConnectExerciseType.LEG_PRESS,
            muscle = MuscleGroup.QUADRICEPS,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_leg_008",
            nameKey = "ex_leg_extension_machine",
            name = "Leg Extension Machine",
            type = HealthConnectExerciseType.LEG_EXTENSION,
            muscle = MuscleGroup.QUADRICEPS,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_leg_009",
            nameKey = "ex_single_leg_extension_machine",
            name = "Single-Leg Extension Machine",
            type = HealthConnectExerciseType.LEG_EXTENSION,
            muscle = MuscleGroup.QUADRICEPS,
            equipment = EquipmentType.MACHINE,
            isUnilateral = true
        ),
        DefaultExercises.exercise(
            id = "bf_leg_015",
            nameKey = "ex_bulgarian_split_squat",
            name = "Bulgarian Split Squat",
            type = HealthConnectExerciseType.LUNGE,
            muscle = MuscleGroup.QUADRICEPS,
            equipment = EquipmentType.DUMBBELL,
            isUnilateral = true
        ),
        DefaultExercises.exercise(
            id = "bf_leg_016",
            nameKey = "ex_walking_lunges",
            name = "Walking Lunges",
            type = HealthConnectExerciseType.LUNGE,
            muscle = MuscleGroup.QUADRICEPS,
            equipment = EquipmentType.DUMBBELL,
            isUnilateral = true
        ),
        DefaultExercises.exercise(
            id = "bf_cardio_004",
            nameKey = "ex_stationary_bike_cycling",
            name = "Stationary Bike / Cycling",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            category = app.bodyforger.core.model.WorkoutActivityCategory.STATIONARY_BIKING,
            muscle = MuscleGroup.QUADRICEPS,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_leg_010",
            nameKey = "ex_seated_leg_curl_machine",
            name = "Seated Leg Curl Machine",
            type = HealthConnectExerciseType.LEG_CURL,
            muscle = MuscleGroup.HAMSTRINGS,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_leg_011",
            nameKey = "ex_lying_leg_curl_machine",
            name = "Lying Leg Curl Machine",
            type = HealthConnectExerciseType.LEG_CURL,
            muscle = MuscleGroup.HAMSTRINGS,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_leg_012",
            nameKey = "ex_single_leg_curl_machine",
            name = "Single-Leg Curl Machine",
            type = HealthConnectExerciseType.LEG_CURL,
            muscle = MuscleGroup.HAMSTRINGS,
            equipment = EquipmentType.MACHINE,
            isUnilateral = true
        ),
        DefaultExercises.exercise(
            id = "bf_leg_013",
            nameKey = "ex_barbell_romanian_deadlift",
            name = "Barbell Romanian Deadlift",
            type = HealthConnectExerciseType.ROMANIAN_DEADLIFT,
            muscle = MuscleGroup.HAMSTRINGS,
            equipment = EquipmentType.BARBELL
        ),
        DefaultExercises.exercise(
            id = "bf_leg_014",
            nameKey = "ex_dumbbell_romanian_deadlift",
            name = "Dumbbell Romanian Deadlift",
            type = HealthConnectExerciseType.ROMANIAN_DEADLIFT,
            muscle = MuscleGroup.HAMSTRINGS,
            equipment = EquipmentType.DUMBBELL
        ),
        DefaultExercises.exercise(
            id = "bf_leg_017",
            nameKey = "ex_barbell_hip_thrust",
            name = "Barbell Hip Thrust",
            type = HealthConnectExerciseType.HIP_THRUST,
            muscle = MuscleGroup.GLUTES,
            equipment = EquipmentType.BARBELL
        ),
        DefaultExercises.exercise(
            id = "bf_leg_018",
            nameKey = "ex_hip_thrust_machine",
            name = "Hip Thrust Machine",
            type = HealthConnectExerciseType.HIP_THRUST,
            muscle = MuscleGroup.GLUTES,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_leg_019",
            nameKey = "ex_hip_abduction_machine",
            name = "Hip Abduction Machine",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            muscle = MuscleGroup.GLUTES,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_leg_020",
            nameKey = "ex_hip_adduction_machine",
            name = "Hip Adduction Machine",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            muscle = MuscleGroup.GLUTES,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_leg_021",
            nameKey = "ex_cable_glute_kickback",
            name = "Cable Glute Kickback",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            muscle = MuscleGroup.GLUTES,
            equipment = EquipmentType.CABLE,
            isUnilateral = true
        ),
        DefaultExercises.exercise(
            id = "bf_abs_011",
            nameKey = "ex_kettlebell_swing",
            name = "Kettlebell Swing",
            type = HealthConnectExerciseType.KETTLEBELL_SWING,
            muscle = MuscleGroup.GLUTES,
            equipment = EquipmentType.KETTLEBELL
        ),
        DefaultExercises.exercise(
            id = "bf_cardio_007",
            nameKey = "ex_stairmaster_stair_climber",
            name = "StairMaster (Stair Climber)",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            category = app.bodyforger.core.model.WorkoutActivityCategory.STATIONARY_BIKING,
            muscle = MuscleGroup.GLUTES,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_leg_022",
            nameKey = "ex_standing_calf_raise_machine",
            name = "Standing Calf Raise Machine",
            type = HealthConnectExerciseType.CALF_RAISE,
            muscle = MuscleGroup.CALVES,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_leg_023",
            nameKey = "ex_seated_calf_raise_machine",
            name = "Seated Calf Raise Machine",
            type = HealthConnectExerciseType.CALF_RAISE,
            muscle = MuscleGroup.CALVES,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_leg_024",
            nameKey = "ex_leg_press_calf_raise",
            name = "Leg Press Calf Raise",
            type = HealthConnectExerciseType.CALF_RAISE,
            muscle = MuscleGroup.CALVES,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_cardio_006",
            nameKey = "ex_jump_rope",
            name = "Jump Rope",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            category = app.bodyforger.core.model.WorkoutActivityCategory.HIIT,
            muscle = MuscleGroup.CALVES,
            equipment = EquipmentType.OTHER
        ),
    )
}
