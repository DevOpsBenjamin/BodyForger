package app.bodyforger.core.database.data

import app.bodyforger.core.database.entity.ExerciseEntity
import app.bodyforger.core.model.EquipmentType
import app.bodyforger.core.model.HealthConnectExerciseType
import app.bodyforger.core.model.MuscleGroup

/** Chest, shoulders and triceps: the pushing chain. */
internal object PushExercises {
    val all: List<ExerciseEntity> = listOf(
        DefaultExercises.exercise(
            id = "bf_chest_001",
            nameKey = "ex_bench_press",
            name = "Bench Press",
            type = HealthConnectExerciseType.BENCH_PRESS,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.BARBELL
        ),
        DefaultExercises.exercise(
            id = "bf_chest_002",
            nameKey = "ex_incline_bench_press",
            name = "Incline Bench Press",
            type = HealthConnectExerciseType.INCLINE_BENCH_PRESS,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.BARBELL
        ),
        DefaultExercises.exercise(
            id = "bf_chest_003",
            nameKey = "ex_decline_bench_press",
            name = "Decline Bench Press",
            type = HealthConnectExerciseType.BENCH_PRESS,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.BARBELL
        ),
        DefaultExercises.exercise(
            id = "bf_chest_004",
            nameKey = "ex_close_grip_bench_press",
            name = "Close-Grip Bench Press",
            type = HealthConnectExerciseType.BENCH_PRESS,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.BARBELL
        ),
        DefaultExercises.exercise(
            id = "bf_chest_005",
            nameKey = "ex_smith_machine_bench_press",
            name = "Smith Machine Bench Press",
            type = HealthConnectExerciseType.BENCH_PRESS,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_chest_006",
            nameKey = "ex_smith_machine_incline_press",
            name = "Smith Machine Incline Press",
            type = HealthConnectExerciseType.INCLINE_BENCH_PRESS,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_chest_007",
            nameKey = "ex_dumbbell_bench_press",
            name = "Dumbbell Bench Press",
            type = HealthConnectExerciseType.DUMBBELL_BENCH_PRESS,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.DUMBBELL
        ),
        DefaultExercises.exercise(
            id = "bf_chest_008",
            nameKey = "ex_incline_dumbbell_press",
            name = "Incline Dumbbell Press",
            type = HealthConnectExerciseType.DUMBBELL_INCLINE_BENCH_PRESS,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.DUMBBELL
        ),
        DefaultExercises.exercise(
            id = "bf_chest_009",
            nameKey = "ex_dumbbell_fly",
            name = "Dumbbell Fly",
            type = HealthConnectExerciseType.CHEST_FLY,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.DUMBBELL
        ),
        DefaultExercises.exercise(
            id = "bf_chest_010",
            nameKey = "ex_chest_press_machine",
            name = "Chest Press Machine",
            type = HealthConnectExerciseType.CHEST_PRESS,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_chest_011",
            nameKey = "ex_incline_press_machine",
            name = "Incline Press Machine",
            type = HealthConnectExerciseType.CHEST_PRESS,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_chest_012",
            nameKey = "ex_converging_chest_press",
            name = "Converging Chest Press",
            type = HealthConnectExerciseType.CHEST_PRESS,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.MACHINE_CONVERGENT
        ),
        DefaultExercises.exercise(
            id = "bf_chest_013",
            nameKey = "ex_converging_incline_press",
            name = "Converging Incline Press",
            type = HealthConnectExerciseType.CHEST_PRESS,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.MACHINE_CONVERGENT
        ),
        DefaultExercises.exercise(
            id = "bf_chest_014",
            nameKey = "ex_pec_deck_machine",
            name = "Pec Deck Machine",
            type = HealthConnectExerciseType.CHEST_FLY,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_chest_015",
            nameKey = "ex_cable_crossover",
            name = "Cable Crossover",
            type = HealthConnectExerciseType.CHEST_FLY,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.CABLE
        ),
        DefaultExercises.exercise(
            id = "bf_chest_016",
            nameKey = "ex_low_cable_fly",
            name = "Low Cable Fly",
            type = HealthConnectExerciseType.CHEST_FLY,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.CABLE
        ),
        DefaultExercises.exercise(
            id = "bf_chest_017",
            nameKey = "ex_bodyweight_dips",
            name = "Bodyweight Dips",
            type = HealthConnectExerciseType.DIP,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.BODYWEIGHT
        ),
        DefaultExercises.exercise(
            id = "bf_chest_018",
            nameKey = "ex_assisted_dips_machine",
            name = "Assisted Dips Machine",
            type = HealthConnectExerciseType.DIP,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_chest_019",
            nameKey = "ex_seated_dips_machine",
            name = "Seated Dips Machine",
            type = HealthConnectExerciseType.TRICEPS_DIP,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_chest_020",
            nameKey = "ex_push_ups",
            name = "Push-Ups",
            type = HealthConnectExerciseType.PUSH_UP,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.BODYWEIGHT
        ),
        DefaultExercises.exercise(
            id = "bf_chest_021",
            nameKey = "ex_decline_push_ups",
            name = "Decline Push-Ups",
            type = HealthConnectExerciseType.PUSH_UP,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.BODYWEIGHT
        ),
        DefaultExercises.exercise(
            id = "bf_sh_001",
            nameKey = "ex_overhead_press",
            name = "Overhead Press",
            type = HealthConnectExerciseType.OVERHEAD_PRESS,
            muscle = MuscleGroup.SHOULDERS,
            equipment = EquipmentType.BARBELL
        ),
        DefaultExercises.exercise(
            id = "bf_sh_002",
            nameKey = "ex_dumbbell_shoulder_press",
            name = "Dumbbell Shoulder Press",
            type = HealthConnectExerciseType.SHOULDER_PRESS,
            muscle = MuscleGroup.SHOULDERS,
            equipment = EquipmentType.DUMBBELL
        ),
        DefaultExercises.exercise(
            id = "bf_sh_003",
            nameKey = "ex_shoulder_press_machine",
            name = "Shoulder Press Machine",
            type = HealthConnectExerciseType.SHOULDER_PRESS,
            muscle = MuscleGroup.SHOULDERS,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_sh_004",
            nameKey = "ex_converging_shoulder_press",
            name = "Converging Shoulder Press",
            type = HealthConnectExerciseType.SHOULDER_PRESS,
            muscle = MuscleGroup.SHOULDERS,
            equipment = EquipmentType.MACHINE_CONVERGENT
        ),
        DefaultExercises.exercise(
            id = "bf_sh_005",
            nameKey = "ex_smith_machine_shoulder_press",
            name = "Smith Machine Shoulder Press",
            type = HealthConnectExerciseType.SHOULDER_PRESS,
            muscle = MuscleGroup.SHOULDERS,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_sh_006",
            nameKey = "ex_dumbbell_lateral_raise",
            name = "Dumbbell Lateral Raise",
            type = HealthConnectExerciseType.LATERAL_RAISE,
            muscle = MuscleGroup.SHOULDERS,
            equipment = EquipmentType.DUMBBELL
        ),
        DefaultExercises.exercise(
            id = "bf_sh_007",
            nameKey = "ex_single_arm_cable_lateral_raise",
            name = "Single-Arm Cable Lateral Raise",
            type = HealthConnectExerciseType.LATERAL_RAISE,
            muscle = MuscleGroup.SHOULDERS,
            equipment = EquipmentType.CABLE,
            isUnilateral = true
        ),
        DefaultExercises.exercise(
            id = "bf_sh_008",
            nameKey = "ex_lateral_raise_machine",
            name = "Lateral Raise Machine",
            type = HealthConnectExerciseType.LATERAL_RAISE,
            muscle = MuscleGroup.SHOULDERS,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_sh_009",
            nameKey = "ex_cable_face_pull",
            name = "Cable Face Pull",
            type = HealthConnectExerciseType.REVERSE_FLY,
            muscle = MuscleGroup.SHOULDERS,
            equipment = EquipmentType.CABLE
        ),
        DefaultExercises.exercise(
            id = "bf_sh_010",
            nameKey = "ex_reverse_pec_deck",
            name = "Reverse Pec Deck",
            type = HealthConnectExerciseType.REVERSE_FLY,
            muscle = MuscleGroup.SHOULDERS,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_sh_011",
            nameKey = "ex_bent_over_dumbbell_reverse_fly",
            name = "Bent-Over Dumbbell Reverse Fly",
            type = HealthConnectExerciseType.REVERSE_FLY,
            muscle = MuscleGroup.SHOULDERS,
            equipment = EquipmentType.DUMBBELL
        ),
        DefaultExercises.exercise(
            id = "bf_sh_012",
            nameKey = "ex_single_arm_cable_reverse_fly",
            name = "Single-Arm Cable Reverse Fly",
            type = HealthConnectExerciseType.REVERSE_FLY,
            muscle = MuscleGroup.SHOULDERS,
            equipment = EquipmentType.CABLE,
            isUnilateral = true
        ),
        DefaultExercises.exercise(
            id = "bf_sh_013",
            nameKey = "ex_dumbbell_front_raise",
            name = "Dumbbell Front Raise",
            type = HealthConnectExerciseType.FRONT_RAISE,
            muscle = MuscleGroup.SHOULDERS,
            equipment = EquipmentType.DUMBBELL
        ),
        DefaultExercises.exercise(
            id = "bf_arm_011",
            nameKey = "ex_skull_crusher",
            name = "Skull Crusher",
            type = HealthConnectExerciseType.SKULL_CRUSHER,
            muscle = MuscleGroup.TRICEPS,
            equipment = EquipmentType.BARBELL
        ),
        DefaultExercises.exercise(
            id = "bf_arm_012",
            nameKey = "ex_close_grip_bench_press_triceps",
            name = "Close-Grip Bench Press (Triceps)",
            type = HealthConnectExerciseType.BENCH_PRESS,
            muscle = MuscleGroup.TRICEPS,
            equipment = EquipmentType.BARBELL
        ),
        DefaultExercises.exercise(
            id = "bf_arm_013",
            nameKey = "ex_rope_triceps_pushdown",
            name = "Rope Triceps Pushdown",
            type = HealthConnectExerciseType.TRICEPS_EXTENSION,
            muscle = MuscleGroup.TRICEPS,
            equipment = EquipmentType.CABLE
        ),
        DefaultExercises.exercise(
            id = "bf_arm_014",
            nameKey = "ex_bar_triceps_pushdown",
            name = "Bar Triceps Pushdown",
            type = HealthConnectExerciseType.TRICEPS_EXTENSION,
            muscle = MuscleGroup.TRICEPS,
            equipment = EquipmentType.CABLE
        ),
        DefaultExercises.exercise(
            id = "bf_arm_015",
            nameKey = "ex_overhead_cable_triceps_extension",
            name = "Overhead Cable Triceps Extension",
            type = HealthConnectExerciseType.TRICEPS_EXTENSION,
            muscle = MuscleGroup.TRICEPS,
            equipment = EquipmentType.CABLE
        ),
        DefaultExercises.exercise(
            id = "bf_arm_016",
            nameKey = "ex_single_arm_cable_triceps_extension",
            name = "Single-Arm Cable Triceps Extension",
            type = HealthConnectExerciseType.TRICEPS_EXTENSION,
            muscle = MuscleGroup.TRICEPS,
            equipment = EquipmentType.CABLE,
            isUnilateral = true
        ),
        DefaultExercises.exercise(
            id = "bf_arm_017",
            nameKey = "ex_parallel_bar_triceps_dips",
            name = "Parallel Bar Triceps Dips",
            type = HealthConnectExerciseType.TRICEPS_DIP,
            muscle = MuscleGroup.TRICEPS,
            equipment = EquipmentType.BODYWEIGHT
        ),
    )
}
