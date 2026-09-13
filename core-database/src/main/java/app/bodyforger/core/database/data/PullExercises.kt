package app.bodyforger.core.database.data

import app.bodyforger.core.database.entity.ExerciseEntity
import app.bodyforger.core.model.EquipmentType
import app.bodyforger.core.model.HealthConnectExerciseType
import app.bodyforger.core.model.MuscleGroup

/** Back and biceps: the pulling chain. */
internal object PullExercises {
    val all: List<ExerciseEntity> = listOf(
        DefaultExercises.exercise(
            id = "bf_back_001",
            nameKey = "ex_pull_up_pronated_grip",
            name = "Pull-Up (Pronated Grip)",
            type = HealthConnectExerciseType.PULL_UP,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.BODYWEIGHT
        ),
        DefaultExercises.exercise(
            id = "bf_back_002",
            nameKey = "ex_chin_up_supinated_grip",
            name = "Chin-Up (Supinated Grip)",
            type = HealthConnectExerciseType.CHIN_UP,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.BODYWEIGHT
        ),
        DefaultExercises.exercise(
            id = "bf_back_003",
            nameKey = "ex_pull_up_neutral_grip",
            name = "Pull-Up (Neutral Grip)",
            type = HealthConnectExerciseType.PULL_UP,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.BODYWEIGHT
        ),
        DefaultExercises.exercise(
            id = "bf_back_004",
            nameKey = "ex_dead_hang_pronated_grip",
            name = "Dead Hang (Pronated Grip)",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.BODYWEIGHT
        ),
        DefaultExercises.exercise(
            id = "bf_back_005",
            nameKey = "ex_dead_hang_supinated_grip",
            name = "Dead Hang (Supinated Grip)",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.BODYWEIGHT
        ),
        DefaultExercises.exercise(
            id = "bf_back_006",
            nameKey = "ex_dead_hang_neutral_grip",
            name = "Dead Hang (Neutral Grip)",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.BODYWEIGHT
        ),
        DefaultExercises.exercise(
            id = "bf_back_007",
            nameKey = "ex_assisted_pull_up_machine_pronated_grip",
            name = "Assisted Pull-Up Machine (Pronated Grip)",
            type = HealthConnectExerciseType.PULL_UP,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_back_008",
            nameKey = "ex_assisted_chin_up_machine_supinated_grip",
            name = "Assisted Chin-Up Machine (Supinated Grip)",
            type = HealthConnectExerciseType.CHIN_UP,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_back_009",
            nameKey = "ex_assisted_pull_up_machine_neutral_grip",
            name = "Assisted Pull-Up Machine (Neutral Grip)",
            type = HealthConnectExerciseType.PULL_UP,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_back_010",
            nameKey = "ex_deadlift",
            name = "Deadlift",
            type = HealthConnectExerciseType.DEADLIFT,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.BARBELL
        ),
        DefaultExercises.exercise(
            id = "bf_back_011",
            nameKey = "ex_trap_bar_deadlift",
            name = "Trap Bar Deadlift",
            type = HealthConnectExerciseType.DEADLIFT,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.BARBELL
        ),
        DefaultExercises.exercise(
            id = "bf_back_012",
            nameKey = "ex_wide_grip_lat_pulldown",
            name = "Wide-Grip Lat Pulldown",
            type = HealthConnectExerciseType.LAT_PULLDOWN,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.CABLE
        ),
        DefaultExercises.exercise(
            id = "bf_back_013",
            nameKey = "ex_neutral_grip_lat_pulldown",
            name = "Neutral-Grip Lat Pulldown",
            type = HealthConnectExerciseType.LAT_PULLDOWN,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.CABLE
        ),
        DefaultExercises.exercise(
            id = "bf_back_014",
            nameKey = "ex_lat_pulldown_machine",
            name = "Lat Pulldown Machine",
            type = HealthConnectExerciseType.LAT_PULLDOWN,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_back_015",
            nameKey = "ex_converging_lat_pulldown",
            name = "Converging Lat Pulldown",
            type = HealthConnectExerciseType.LAT_PULLDOWN,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.MACHINE_CONVERGENT
        ),
        DefaultExercises.exercise(
            id = "bf_back_016",
            nameKey = "ex_single_arm_cable_pulldown",
            name = "Single-Arm Cable Pulldown",
            type = HealthConnectExerciseType.LAT_PULLDOWN,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.CABLE,
            isUnilateral = true
        ),
        DefaultExercises.exercise(
            id = "bf_back_017",
            nameKey = "ex_pull_over_poulie_haute",
            name = "Pull-Over Poulie Haute",
            type = HealthConnectExerciseType.LAT_PULLDOWN,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.CABLE
        ),
        DefaultExercises.exercise(
            id = "bf_back_018",
            nameKey = "ex_seated_cable_row",
            name = "Seated Cable Row",
            type = HealthConnectExerciseType.SEATED_CABLE_ROW,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.CABLE
        ),
        DefaultExercises.exercise(
            id = "bf_back_019",
            nameKey = "ex_wide_grip_seated_row",
            name = "Wide-Grip Seated Row",
            type = HealthConnectExerciseType.SEATED_CABLE_ROW,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.CABLE
        ),
        DefaultExercises.exercise(
            id = "bf_back_020",
            nameKey = "ex_single_arm_cable_row",
            name = "Single-Arm Cable Row",
            type = HealthConnectExerciseType.SEATED_CABLE_ROW,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.CABLE,
            isUnilateral = true
        ),
        DefaultExercises.exercise(
            id = "bf_back_021",
            nameKey = "ex_bent_over_barbell_row",
            name = "Bent-Over Barbell Row",
            type = HealthConnectExerciseType.BENT_OVER_ROW,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.BARBELL
        ),
        DefaultExercises.exercise(
            id = "bf_back_022",
            nameKey = "ex_single_arm_dumbbell_row",
            name = "Single-Arm Dumbbell Row",
            type = HealthConnectExerciseType.BENT_OVER_ROW,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.DUMBBELL,
            isUnilateral = true
        ),
        DefaultExercises.exercise(
            id = "bf_back_023",
            nameKey = "ex_t_bar_row",
            name = "T-Bar Row",
            type = HealthConnectExerciseType.BENT_OVER_ROW,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.BARBELL
        ),
        DefaultExercises.exercise(
            id = "bf_back_024",
            nameKey = "ex_seated_row_machine",
            name = "Seated Row Machine",
            type = HealthConnectExerciseType.BENT_OVER_ROW,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_back_025",
            nameKey = "ex_converging_row_machine",
            name = "Converging Row Machine",
            type = HealthConnectExerciseType.BENT_OVER_ROW,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.MACHINE_CONVERGENT
        ),
        DefaultExercises.exercise(
            id = "bf_back_026",
            nameKey = "ex_45_back_extension",
            name = "45° Back Extension",
            type = HealthConnectExerciseType.BACK_EXTENSION,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.BODYWEIGHT
        ),
        DefaultExercises.exercise(
            id = "bf_back_027",
            nameKey = "ex_seated_lower_back_machine",
            name = "Seated Lower Back Machine",
            type = HealthConnectExerciseType.BACK_EXTENSION,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_cardio_005",
            nameKey = "ex_rowing_machine",
            name = "Rowing Machine",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            category = app.bodyforger.core.model.WorkoutActivityCategory.ROWING_MACHINE,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_arm_001",
            nameKey = "ex_barbell_biceps_curl",
            name = "Barbell Biceps Curl",
            type = HealthConnectExerciseType.BARBELL_CURL,
            muscle = MuscleGroup.BICEPS,
            equipment = EquipmentType.BARBELL
        ),
        DefaultExercises.exercise(
            id = "bf_arm_002",
            nameKey = "ex_ez_bar_preacher_curl",
            name = "EZ-Bar Preacher Curl",
            type = HealthConnectExerciseType.BARBELL_CURL,
            muscle = MuscleGroup.BICEPS,
            equipment = EquipmentType.BARBELL
        ),
        DefaultExercises.exercise(
            id = "bf_arm_003",
            nameKey = "ex_dumbbell_biceps_curl",
            name = "Dumbbell Biceps Curl",
            type = HealthConnectExerciseType.DUMBBELL_CURL,
            muscle = MuscleGroup.BICEPS,
            equipment = EquipmentType.DUMBBELL
        ),
        DefaultExercises.exercise(
            id = "bf_arm_004",
            nameKey = "ex_incline_dumbbell_curl",
            name = "Incline Dumbbell Curl",
            type = HealthConnectExerciseType.DUMBBELL_CURL,
            muscle = MuscleGroup.BICEPS,
            equipment = EquipmentType.DUMBBELL
        ),
        DefaultExercises.exercise(
            id = "bf_arm_005",
            nameKey = "ex_dumbbell_hammer_curl",
            name = "Dumbbell Hammer Curl",
            type = HealthConnectExerciseType.HAMMER_CURL,
            muscle = MuscleGroup.BICEPS,
            equipment = EquipmentType.DUMBBELL
        ),
        DefaultExercises.exercise(
            id = "bf_arm_006",
            nameKey = "ex_single_arm_dumbbell_preacher_curl",
            name = "Single-Arm Dumbbell Preacher Curl",
            type = HealthConnectExerciseType.DUMBBELL_CURL,
            muscle = MuscleGroup.BICEPS,
            equipment = EquipmentType.DUMBBELL,
            isUnilateral = true
        ),
        DefaultExercises.exercise(
            id = "bf_arm_007",
            nameKey = "ex_rope_cable_curl",
            name = "Rope Cable Curl",
            type = HealthConnectExerciseType.HAMMER_CURL,
            muscle = MuscleGroup.BICEPS,
            equipment = EquipmentType.CABLE
        ),
        DefaultExercises.exercise(
            id = "bf_arm_008",
            nameKey = "ex_single_arm_low_cable_curl",
            name = "Single-Arm Low Cable Curl",
            type = HealthConnectExerciseType.BARBELL_CURL,
            muscle = MuscleGroup.BICEPS,
            equipment = EquipmentType.CABLE,
            isUnilateral = true
        ),
        DefaultExercises.exercise(
            id = "bf_arm_009",
            nameKey = "ex_biceps_curl_machine",
            name = "Biceps Curl Machine",
            type = HealthConnectExerciseType.BARBELL_CURL,
            muscle = MuscleGroup.BICEPS,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_arm_010",
            nameKey = "ex_converging_biceps_curl",
            name = "Converging Biceps Curl",
            type = HealthConnectExerciseType.BARBELL_CURL,
            muscle = MuscleGroup.BICEPS,
            equipment = EquipmentType.MACHINE_CONVERGENT
        ),
        DefaultExercises.exercise(
            id = "bf_arm_018",
            nameKey = "ex_barbell_reverse_curl",
            name = "Barbell Reverse Curl",
            type = HealthConnectExerciseType.BARBELL_CURL,
            muscle = MuscleGroup.BICEPS,
            equipment = EquipmentType.BARBELL
        ),
    )
}
