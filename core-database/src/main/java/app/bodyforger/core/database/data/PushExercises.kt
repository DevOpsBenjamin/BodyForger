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
            name = "Développé Couché",
            type = HealthConnectExerciseType.BENCH_PRESS,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.BARBELL
        ),
        DefaultExercises.exercise(
            id = "bf_chest_002",
            name = "Développé Incliné",
            type = HealthConnectExerciseType.INCLINE_BENCH_PRESS,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.BARBELL
        ),
        DefaultExercises.exercise(
            id = "bf_chest_003",
            name = "Développé Décliné",
            type = HealthConnectExerciseType.BENCH_PRESS,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.BARBELL
        ),
        DefaultExercises.exercise(
            id = "bf_chest_004",
            name = "Développé Couché Prise Serrée",
            type = HealthConnectExerciseType.BENCH_PRESS,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.BARBELL
        ),
        DefaultExercises.exercise(
            id = "bf_chest_005",
            name = "Développé Couché Smith Machine",
            type = HealthConnectExerciseType.BENCH_PRESS,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_chest_006",
            name = "Développé Incliné Smith Machine",
            type = HealthConnectExerciseType.INCLINE_BENCH_PRESS,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_chest_007",
            name = "Développé Couché Haltères",
            type = HealthConnectExerciseType.DUMBBELL_BENCH_PRESS,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.DUMBBELL
        ),
        DefaultExercises.exercise(
            id = "bf_chest_008",
            name = "Développé Incliné Haltères",
            type = HealthConnectExerciseType.DUMBBELL_INCLINE_BENCH_PRESS,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.DUMBBELL
        ),
        DefaultExercises.exercise(
            id = "bf_chest_009",
            name = "Écarté Couché Haltères",
            type = HealthConnectExerciseType.CHEST_FLY,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.DUMBBELL
        ),
        DefaultExercises.exercise(
            id = "bf_chest_010",
            name = "Développé Couché Machine",
            type = HealthConnectExerciseType.CHEST_PRESS,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_chest_011",
            name = "Développé Incliné Machine",
            type = HealthConnectExerciseType.CHEST_PRESS,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_chest_012",
            name = "Développé Couché Convergent",
            type = HealthConnectExerciseType.CHEST_PRESS,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.MACHINE_CONVERGENT
        ),
        DefaultExercises.exercise(
            id = "bf_chest_013",
            name = "Développé Incliné Convergent",
            type = HealthConnectExerciseType.CHEST_PRESS,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.MACHINE_CONVERGENT
        ),
        DefaultExercises.exercise(
            id = "bf_chest_014",
            name = "Pec Deck Machine",
            type = HealthConnectExerciseType.CHEST_FLY,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_chest_015",
            name = "Écartés Poulies Vis-à-Vis",
            type = HealthConnectExerciseType.CHEST_FLY,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.CABLE
        ),
        DefaultExercises.exercise(
            id = "bf_chest_016",
            name = "Écartés Poulie Basse",
            type = HealthConnectExerciseType.CHEST_FLY,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.CABLE
        ),
        DefaultExercises.exercise(
            id = "bf_chest_017",
            name = "Dips Poids du Corps",
            type = HealthConnectExerciseType.DIP,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.BODYWEIGHT
        ),
        DefaultExercises.exercise(
            id = "bf_chest_018",
            name = "Dips Assistés Machine",
            type = HealthConnectExerciseType.DIP,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_chest_019",
            name = "Dips Assis Machine",
            type = HealthConnectExerciseType.TRICEPS_DIP,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_chest_020",
            name = "Pompes Classiques",
            type = HealthConnectExerciseType.PUSH_UP,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.BODYWEIGHT
        ),
        DefaultExercises.exercise(
            id = "bf_chest_021",
            name = "Pompes Déclinées",
            type = HealthConnectExerciseType.PUSH_UP,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.BODYWEIGHT
        ),
        DefaultExercises.exercise(
            id = "bf_sh_001",
            name = "Développé Militaire",
            type = HealthConnectExerciseType.OVERHEAD_PRESS,
            muscle = MuscleGroup.SHOULDERS,
            equipment = EquipmentType.BARBELL
        ),
        DefaultExercises.exercise(
            id = "bf_sh_002",
            name = "Développé Épaules Haltères",
            type = HealthConnectExerciseType.SHOULDER_PRESS,
            muscle = MuscleGroup.SHOULDERS,
            equipment = EquipmentType.DUMBBELL
        ),
        DefaultExercises.exercise(
            id = "bf_sh_003",
            name = "Développé Épaules Machine",
            type = HealthConnectExerciseType.SHOULDER_PRESS,
            muscle = MuscleGroup.SHOULDERS,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_sh_004",
            name = "Développé Épaules Convergent",
            type = HealthConnectExerciseType.SHOULDER_PRESS,
            muscle = MuscleGroup.SHOULDERS,
            equipment = EquipmentType.MACHINE_CONVERGENT
        ),
        DefaultExercises.exercise(
            id = "bf_sh_005",
            name = "Développé Épaules Smith Machine",
            type = HealthConnectExerciseType.SHOULDER_PRESS,
            muscle = MuscleGroup.SHOULDERS,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_sh_006",
            name = "Élévations Latérales Haltères",
            type = HealthConnectExerciseType.LATERAL_RAISE,
            muscle = MuscleGroup.SHOULDERS,
            equipment = EquipmentType.DUMBBELL
        ),
        DefaultExercises.exercise(
            id = "bf_sh_007",
            name = "Élévations Latérales Poulie Unilatérale",
            type = HealthConnectExerciseType.LATERAL_RAISE,
            muscle = MuscleGroup.SHOULDERS,
            equipment = EquipmentType.CABLE,
            isUnilateral = true
        ),
        DefaultExercises.exercise(
            id = "bf_sh_008",
            name = "Élévations Latérales Machine",
            type = HealthConnectExerciseType.LATERAL_RAISE,
            muscle = MuscleGroup.SHOULDERS,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_sh_009",
            name = "Face Pull Poulie",
            type = HealthConnectExerciseType.REVERSE_FLY,
            muscle = MuscleGroup.SHOULDERS,
            equipment = EquipmentType.CABLE
        ),
        DefaultExercises.exercise(
            id = "bf_sh_010",
            name = "Oiseau Machine",
            type = HealthConnectExerciseType.REVERSE_FLY,
            muscle = MuscleGroup.SHOULDERS,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_sh_011",
            name = "Oiseau Haltères Buste Penché",
            type = HealthConnectExerciseType.REVERSE_FLY,
            muscle = MuscleGroup.SHOULDERS,
            equipment = EquipmentType.DUMBBELL
        ),
        DefaultExercises.exercise(
            id = "bf_sh_012",
            name = "Oiseau Poulie Unilatéral",
            type = HealthConnectExerciseType.REVERSE_FLY,
            muscle = MuscleGroup.SHOULDERS,
            equipment = EquipmentType.CABLE,
            isUnilateral = true
        ),
        DefaultExercises.exercise(
            id = "bf_sh_013",
            name = "Élévations Frontales Haltères",
            type = HealthConnectExerciseType.FRONT_RAISE,
            muscle = MuscleGroup.SHOULDERS,
            equipment = EquipmentType.DUMBBELL
        ),
        DefaultExercises.exercise(
            id = "bf_arm_011",
            name = "Barre au Front",
            type = HealthConnectExerciseType.SKULL_CRUSHER,
            muscle = MuscleGroup.TRICEPS,
            equipment = EquipmentType.BARBELL
        ),
        DefaultExercises.exercise(
            id = "bf_arm_012",
            name = "Développé Couché Prise Serrée Triceps",
            type = HealthConnectExerciseType.BENCH_PRESS,
            muscle = MuscleGroup.TRICEPS,
            equipment = EquipmentType.BARBELL
        ),
        DefaultExercises.exercise(
            id = "bf_arm_013",
            name = "Extension Triceps Poulie Corde",
            type = HealthConnectExerciseType.TRICEPS_EXTENSION,
            muscle = MuscleGroup.TRICEPS,
            equipment = EquipmentType.CABLE
        ),
        DefaultExercises.exercise(
            id = "bf_arm_014",
            name = "Extension Triceps Poulie Barre",
            type = HealthConnectExerciseType.TRICEPS_EXTENSION,
            muscle = MuscleGroup.TRICEPS,
            equipment = EquipmentType.CABLE
        ),
        DefaultExercises.exercise(
            id = "bf_arm_015",
            name = "Extension Triceps Overhead Poulie",
            type = HealthConnectExerciseType.TRICEPS_EXTENSION,
            muscle = MuscleGroup.TRICEPS,
            equipment = EquipmentType.CABLE
        ),
        DefaultExercises.exercise(
            id = "bf_arm_016",
            name = "Extension Triceps Poulie Unilatérale",
            type = HealthConnectExerciseType.TRICEPS_EXTENSION,
            muscle = MuscleGroup.TRICEPS,
            equipment = EquipmentType.CABLE,
            isUnilateral = true
        ),
        DefaultExercises.exercise(
            id = "bf_arm_017",
            name = "Dips Triceps Barres Parallèles",
            type = HealthConnectExerciseType.TRICEPS_DIP,
            muscle = MuscleGroup.TRICEPS,
            equipment = EquipmentType.BODYWEIGHT
        ),
    )
}
