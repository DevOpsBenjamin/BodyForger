package app.bodyforger.core.database.data

import app.bodyforger.core.database.entity.ExerciseEntity
import app.bodyforger.core.model.EquipmentType
import app.bodyforger.core.model.HealthConnectExerciseType
import app.bodyforger.core.model.MuscleGroup

/**
 * Catalogue officiel des exercices fondamentaux BodyForger (~100 exercices).
 * Ces exercices sont pré-remplis à la création de la base de données et ne peuvent
 * pas être supprimés par l'utilisateur (isCustom = false).
 */
object DefaultExercises {

    val all: List<ExerciseEntity> = listOf(
        // ==========================================
        // 1. PECTORAUX & POUSSÉE (Chest)
        // ==========================================
        exercise(
            id = "bf_chest_001",
            name = "Développé Couché",
            type = HealthConnectExerciseType.BENCH_PRESS,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.BARBELL
        ),
        exercise(
            id = "bf_chest_002",
            name = "Développé Incliné",
            type = HealthConnectExerciseType.INCLINE_BENCH_PRESS,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.BARBELL
        ),
        exercise(
            id = "bf_chest_003",
            name = "Développé Décliné",
            type = HealthConnectExerciseType.BENCH_PRESS,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.BARBELL
        ),
        exercise(
            id = "bf_chest_004",
            name = "Développé Couché Prise Serrée",
            type = HealthConnectExerciseType.BENCH_PRESS,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.BARBELL
        ),
        exercise(
            id = "bf_chest_005",
            name = "Développé Couché Smith Machine",
            type = HealthConnectExerciseType.BENCH_PRESS,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.MACHINE
        ),
        exercise(
            id = "bf_chest_006",
            name = "Développé Incliné Smith Machine",
            type = HealthConnectExerciseType.INCLINE_BENCH_PRESS,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.MACHINE
        ),
        exercise(
            id = "bf_chest_007",
            name = "Développé Couché Haltères",
            type = HealthConnectExerciseType.DUMBBELL_BENCH_PRESS,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.DUMBBELL
        ),
        exercise(
            id = "bf_chest_008",
            name = "Développé Incliné Haltères",
            type = HealthConnectExerciseType.DUMBBELL_INCLINE_BENCH_PRESS,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.DUMBBELL
        ),
        exercise(
            id = "bf_chest_009",
            name = "Écarté Couché Haltères",
            type = HealthConnectExerciseType.CHEST_FLY,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.DUMBBELL
        ),
        exercise(
            id = "bf_chest_010",
            name = "Développé Couché Machine",
            type = HealthConnectExerciseType.CHEST_PRESS,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.MACHINE
        ),
        exercise(
            id = "bf_chest_011",
            name = "Développé Incliné Machine",
            type = HealthConnectExerciseType.CHEST_PRESS,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.MACHINE
        ),
        exercise(
            id = "bf_chest_012",
            name = "Développé Couché Convergent",
            type = HealthConnectExerciseType.CHEST_PRESS,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.MACHINE_CONVERGENT
        ),
        exercise(
            id = "bf_chest_013",
            name = "Développé Incliné Convergent",
            type = HealthConnectExerciseType.CHEST_PRESS,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.MACHINE_CONVERGENT
        ),
        exercise(
            id = "bf_chest_014",
            name = "Pec Deck Machine",
            type = HealthConnectExerciseType.CHEST_FLY,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.MACHINE
        ),
        exercise(
            id = "bf_chest_015",
            name = "Écartés Poulies Vis-à-Vis",
            type = HealthConnectExerciseType.CHEST_FLY,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.CABLE
        ),
        exercise(
            id = "bf_chest_016",
            name = "Écartés Poulie Basse",
            type = HealthConnectExerciseType.CHEST_FLY,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.CABLE
        ),
        exercise(
            id = "bf_chest_017",
            name = "Dips Poids du Corps",
            type = HealthConnectExerciseType.DIP,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.BODYWEIGHT
        ),
        exercise(
            id = "bf_chest_018",
            name = "Dips Assistés Machine",
            type = HealthConnectExerciseType.DIP,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.MACHINE
        ),
        exercise(
            id = "bf_chest_019",
            name = "Dips Assis Machine",
            type = HealthConnectExerciseType.TRICEPS_DIP,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.MACHINE
        ),
        exercise(
            id = "bf_chest_020",
            name = "Pompes Classiques",
            type = HealthConnectExerciseType.PUSH_UP,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.BODYWEIGHT
        ),
        exercise(
            id = "bf_chest_021",
            name = "Pompes Déclinées",
            type = HealthConnectExerciseType.PUSH_UP,
            muscle = MuscleGroup.CHEST,
            equipment = EquipmentType.BODYWEIGHT
        ),

        // ==========================================
        // 2. DOS, GRAND DORSAL & SUSPENSIONS (Back)
        // ==========================================
        exercise(
            id = "bf_back_001",
            name = "Tractions Pronation",
            type = HealthConnectExerciseType.PULL_UP,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.BODYWEIGHT
        ),
        exercise(
            id = "bf_back_002",
            name = "Tractions Supination",
            type = HealthConnectExerciseType.CHIN_UP,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.BODYWEIGHT
        ),
        exercise(
            id = "bf_back_003",
            name = "Tractions Prise Neutre",
            type = HealthConnectExerciseType.PULL_UP,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.BODYWEIGHT
        ),
        exercise(
            id = "bf_back_004",
            name = "Dead Hang Pronation",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.BODYWEIGHT
        ),
        exercise(
            id = "bf_back_005",
            name = "Dead Hang Supination",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.BODYWEIGHT
        ),
        exercise(
            id = "bf_back_006",
            name = "Dead Hang Prise Neutre",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.BODYWEIGHT
        ),
        exercise(
            id = "bf_back_007",
            name = "Tractions Assistées Machine Pronation",
            type = HealthConnectExerciseType.PULL_UP,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.MACHINE
        ),
        exercise(
            id = "bf_back_008",
            name = "Tractions Assistées Machine Supination",
            type = HealthConnectExerciseType.CHIN_UP,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.MACHINE
        ),
        exercise(
            id = "bf_back_009",
            name = "Tractions Assistées Machine Prise Neutre",
            type = HealthConnectExerciseType.PULL_UP,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.MACHINE
        ),
        exercise(
            id = "bf_back_010",
            name = "Soulevé de Terre",
            type = HealthConnectExerciseType.DEADLIFT,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.BARBELL
        ),
        exercise(
            id = "bf_back_011",
            name = "Soulevé de Terre Trap Bar",
            type = HealthConnectExerciseType.DEADLIFT,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.BARBELL
        ),
        exercise(
            id = "bf_back_012",
            name = "Tirage Vertical Prise Large",
            type = HealthConnectExerciseType.LAT_PULLDOWN,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.CABLE
        ),
        exercise(
            id = "bf_back_013",
            name = "Tirage Vertical Prise Neutre",
            type = HealthConnectExerciseType.LAT_PULLDOWN,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.CABLE
        ),
        exercise(
            id = "bf_back_014",
            name = "Tirage Vertical Machine",
            type = HealthConnectExerciseType.LAT_PULLDOWN,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.MACHINE
        ),
        exercise(
            id = "bf_back_015",
            name = "Tirage Vertical Convergent",
            type = HealthConnectExerciseType.LAT_PULLDOWN,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.MACHINE_CONVERGENT
        ),
        exercise(
            id = "bf_back_016",
            name = "Tirage Vertical Poulie Unilatéral",
            type = HealthConnectExerciseType.LAT_PULLDOWN,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.CABLE,
            isUnilateral = true
        ),
        exercise(
            id = "bf_back_017",
            name = "Pull-Over Poulie Haute",
            type = HealthConnectExerciseType.LAT_PULLDOWN,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.CABLE
        ),
        exercise(
            id = "bf_back_018",
            name = "Tirage Horizontal Poulie Basse",
            type = HealthConnectExerciseType.SEATED_CABLE_ROW,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.CABLE
        ),
        exercise(
            id = "bf_back_019",
            name = "Tirage Horizontal Prise Large",
            type = HealthConnectExerciseType.SEATED_CABLE_ROW,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.CABLE
        ),
        exercise(
            id = "bf_back_020",
            name = "Tirage Horizontal Poulie Unilatéral",
            type = HealthConnectExerciseType.SEATED_CABLE_ROW,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.CABLE,
            isUnilateral = true
        ),
        exercise(
            id = "bf_back_021",
            name = "Rowing Barre Buste Penché",
            type = HealthConnectExerciseType.BENT_OVER_ROW,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.BARBELL
        ),
        exercise(
            id = "bf_back_022",
            name = "Rowing Haltère Unilatéral",
            type = HealthConnectExerciseType.BENT_OVER_ROW,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.DUMBBELL,
            isUnilateral = true
        ),
        exercise(
            id = "bf_back_023",
            name = "T-Bar Row",
            type = HealthConnectExerciseType.BENT_OVER_ROW,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.BARBELL
        ),
        exercise(
            id = "bf_back_024",
            name = "Rowing Assis Machine",
            type = HealthConnectExerciseType.BENT_OVER_ROW,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.MACHINE
        ),
        exercise(
            id = "bf_back_025",
            name = "Rowing Convergent Machine",
            type = HealthConnectExerciseType.BENT_OVER_ROW,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.MACHINE_CONVERGENT
        ),
        exercise(
            id = "bf_back_026",
            name = "Extensions Lombaires Banc 45°",
            type = HealthConnectExerciseType.BACK_EXTENSION,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.BODYWEIGHT
        ),
        exercise(
            id = "bf_back_027",
            name = "Machine à Lombaires Assise",
            type = HealthConnectExerciseType.BACK_EXTENSION,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.MACHINE
        ),

        // ==========================================
        // 3. ÉPAULES & DELTOÏDES (Shoulders)
        // ==========================================
        exercise(
            id = "bf_sh_001",
            name = "Développé Militaire",
            type = HealthConnectExerciseType.OVERHEAD_PRESS,
            muscle = MuscleGroup.SHOULDERS,
            equipment = EquipmentType.BARBELL
        ),
        exercise(
            id = "bf_sh_002",
            name = "Développé Épaules Haltères",
            type = HealthConnectExerciseType.SHOULDER_PRESS,
            muscle = MuscleGroup.SHOULDERS,
            equipment = EquipmentType.DUMBBELL
        ),
        exercise(
            id = "bf_sh_003",
            name = "Développé Épaules Machine",
            type = HealthConnectExerciseType.SHOULDER_PRESS,
            muscle = MuscleGroup.SHOULDERS,
            equipment = EquipmentType.MACHINE
        ),
        exercise(
            id = "bf_sh_004",
            name = "Développé Épaules Convergent",
            type = HealthConnectExerciseType.SHOULDER_PRESS,
            muscle = MuscleGroup.SHOULDERS,
            equipment = EquipmentType.MACHINE_CONVERGENT
        ),
        exercise(
            id = "bf_sh_005",
            name = "Développé Épaules Smith Machine",
            type = HealthConnectExerciseType.SHOULDER_PRESS,
            muscle = MuscleGroup.SHOULDERS,
            equipment = EquipmentType.MACHINE
        ),
        exercise(
            id = "bf_sh_006",
            name = "Élévations Latérales Haltères",
            type = HealthConnectExerciseType.LATERAL_RAISE,
            muscle = MuscleGroup.SHOULDERS,
            equipment = EquipmentType.DUMBBELL
        ),
        exercise(
            id = "bf_sh_007",
            name = "Élévations Latérales Poulie Unilatérale",
            type = HealthConnectExerciseType.LATERAL_RAISE,
            muscle = MuscleGroup.SHOULDERS,
            equipment = EquipmentType.CABLE,
            isUnilateral = true
        ),
        exercise(
            id = "bf_sh_008",
            name = "Élévations Latérales Machine",
            type = HealthConnectExerciseType.LATERAL_RAISE,
            muscle = MuscleGroup.SHOULDERS,
            equipment = EquipmentType.MACHINE
        ),
        exercise(
            id = "bf_sh_009",
            name = "Face Pull Poulie",
            type = HealthConnectExerciseType.REVERSE_FLY,
            muscle = MuscleGroup.SHOULDERS,
            equipment = EquipmentType.CABLE
        ),
        exercise(
            id = "bf_sh_010",
            name = "Oiseau Machine",
            type = HealthConnectExerciseType.REVERSE_FLY,
            muscle = MuscleGroup.SHOULDERS,
            equipment = EquipmentType.MACHINE
        ),
        exercise(
            id = "bf_sh_011",
            name = "Oiseau Haltères Buste Penché",
            type = HealthConnectExerciseType.REVERSE_FLY,
            muscle = MuscleGroup.SHOULDERS,
            equipment = EquipmentType.DUMBBELL
        ),
        exercise(
            id = "bf_sh_012",
            name = "Oiseau Poulie Unilatéral",
            type = HealthConnectExerciseType.REVERSE_FLY,
            muscle = MuscleGroup.SHOULDERS,
            equipment = EquipmentType.CABLE,
            isUnilateral = true
        ),
        exercise(
            id = "bf_sh_013",
            name = "Élévations Frontales Haltères",
            type = HealthConnectExerciseType.FRONT_RAISE,
            muscle = MuscleGroup.SHOULDERS,
            equipment = EquipmentType.DUMBBELL
        ),

        // ==========================================
        // 4. BRAS : BICEPS & TRICEPS (Arms)
        // ==========================================
        exercise(
            id = "bf_arm_001",
            name = "Curl Biceps Barre",
            type = HealthConnectExerciseType.BARBELL_CURL,
            muscle = MuscleGroup.BICEPS,
            equipment = EquipmentType.BARBELL
        ),
        exercise(
            id = "bf_arm_002",
            name = "Curl Pupitre Barre EZ",
            type = HealthConnectExerciseType.BARBELL_CURL,
            muscle = MuscleGroup.BICEPS,
            equipment = EquipmentType.BARBELL
        ),
        exercise(
            id = "bf_arm_003",
            name = "Curl Biceps Haltères",
            type = HealthConnectExerciseType.DUMBBELL_CURL,
            muscle = MuscleGroup.BICEPS,
            equipment = EquipmentType.DUMBBELL
        ),
        exercise(
            id = "bf_arm_004",
            name = "Curl Biceps Incliné",
            type = HealthConnectExerciseType.DUMBBELL_CURL,
            muscle = MuscleGroup.BICEPS,
            equipment = EquipmentType.DUMBBELL
        ),
        exercise(
            id = "bf_arm_005",
            name = "Curl Marteau Haltères",
            type = HealthConnectExerciseType.HAMMER_CURL,
            muscle = MuscleGroup.BICEPS,
            equipment = EquipmentType.DUMBBELL
        ),
        exercise(
            id = "bf_arm_006",
            name = "Curl Pupitre Haltère Unilatéral",
            type = HealthConnectExerciseType.DUMBBELL_CURL,
            muscle = MuscleGroup.BICEPS,
            equipment = EquipmentType.DUMBBELL,
            isUnilateral = true
        ),
        exercise(
            id = "bf_arm_007",
            name = "Curl Corde Poulie Basse",
            type = HealthConnectExerciseType.HAMMER_CURL,
            muscle = MuscleGroup.BICEPS,
            equipment = EquipmentType.CABLE
        ),
        exercise(
            id = "bf_arm_008",
            name = "Curl Biceps Poulie Basse Unilatéral",
            type = HealthConnectExerciseType.BARBELL_CURL,
            muscle = MuscleGroup.BICEPS,
            equipment = EquipmentType.CABLE,
            isUnilateral = true
        ),
        exercise(
            id = "bf_arm_009",
            name = "Curl Biceps Machine",
            type = HealthConnectExerciseType.BARBELL_CURL,
            muscle = MuscleGroup.BICEPS,
            equipment = EquipmentType.MACHINE
        ),
        exercise(
            id = "bf_arm_010",
            name = "Curl Biceps Convergent",
            type = HealthConnectExerciseType.BARBELL_CURL,
            muscle = MuscleGroup.BICEPS,
            equipment = EquipmentType.MACHINE_CONVERGENT
        ),
        exercise(
            id = "bf_arm_011",
            name = "Barre au Front",
            type = HealthConnectExerciseType.SKULL_CRUSHER,
            muscle = MuscleGroup.TRICEPS,
            equipment = EquipmentType.BARBELL
        ),
        exercise(
            id = "bf_arm_012",
            name = "Développé Couché Prise Serrée Triceps",
            type = HealthConnectExerciseType.BENCH_PRESS,
            muscle = MuscleGroup.TRICEPS,
            equipment = EquipmentType.BARBELL
        ),
        exercise(
            id = "bf_arm_013",
            name = "Extension Triceps Poulie Corde",
            type = HealthConnectExerciseType.TRICEPS_EXTENSION,
            muscle = MuscleGroup.TRICEPS,
            equipment = EquipmentType.CABLE
        ),
        exercise(
            id = "bf_arm_014",
            name = "Extension Triceps Poulie Barre",
            type = HealthConnectExerciseType.TRICEPS_EXTENSION,
            muscle = MuscleGroup.TRICEPS,
            equipment = EquipmentType.CABLE
        ),
        exercise(
            id = "bf_arm_015",
            name = "Extension Triceps Overhead Poulie",
            type = HealthConnectExerciseType.TRICEPS_EXTENSION,
            muscle = MuscleGroup.TRICEPS,
            equipment = EquipmentType.CABLE
        ),
        exercise(
            id = "bf_arm_016",
            name = "Extension Triceps Poulie Unilatérale",
            type = HealthConnectExerciseType.TRICEPS_EXTENSION,
            muscle = MuscleGroup.TRICEPS,
            equipment = EquipmentType.CABLE,
            isUnilateral = true
        ),
        exercise(
            id = "bf_arm_017",
            name = "Dips Triceps Barres Parallèles",
            type = HealthConnectExerciseType.TRICEPS_DIP,
            muscle = MuscleGroup.TRICEPS,
            equipment = EquipmentType.BODYWEIGHT
        ),
        exercise(
            id = "bf_arm_018",
            name = "Curl Inversé Barre",
            type = HealthConnectExerciseType.BARBELL_CURL,
            muscle = MuscleGroup.BICEPS,
            equipment = EquipmentType.BARBELL
        ),

        // ==========================================
        // 5. JAMBES, FESSIERS & MOLLETS (Legs)
        // ==========================================
        exercise(
            id = "bf_leg_001",
            name = "Back Squat",
            type = HealthConnectExerciseType.SQUAT,
            muscle = MuscleGroup.QUADRICEPS,
            equipment = EquipmentType.BARBELL
        ),
        exercise(
            id = "bf_leg_002",
            name = "Front Squat",
            type = HealthConnectExerciseType.FRONT_SQUAT,
            muscle = MuscleGroup.QUADRICEPS,
            equipment = EquipmentType.BARBELL
        ),
        exercise(
            id = "bf_leg_003",
            name = "Hack Squat Machine",
            type = HealthConnectExerciseType.SQUAT,
            muscle = MuscleGroup.QUADRICEPS,
            equipment = EquipmentType.MACHINE
        ),
        exercise(
            id = "bf_leg_004",
            name = "Squat Smith Machine",
            type = HealthConnectExerciseType.SQUAT,
            muscle = MuscleGroup.QUADRICEPS,
            equipment = EquipmentType.MACHINE
        ),
        exercise(
            id = "bf_leg_005",
            name = "Belt Squat Machine",
            type = HealthConnectExerciseType.SQUAT,
            muscle = MuscleGroup.QUADRICEPS,
            equipment = EquipmentType.MACHINE
        ),
        exercise(
            id = "bf_leg_006",
            name = "Presse à Cuisses Inclinée 45°",
            type = HealthConnectExerciseType.LEG_PRESS,
            muscle = MuscleGroup.QUADRICEPS,
            equipment = EquipmentType.MACHINE
        ),
        exercise(
            id = "bf_leg_007",
            name = "Presse à Cuisses Horizontale",
            type = HealthConnectExerciseType.LEG_PRESS,
            muscle = MuscleGroup.QUADRICEPS,
            equipment = EquipmentType.MACHINE
        ),
        exercise(
            id = "bf_leg_008",
            name = "Leg Extension Machine",
            type = HealthConnectExerciseType.LEG_EXTENSION,
            muscle = MuscleGroup.QUADRICEPS,
            equipment = EquipmentType.MACHINE
        ),
        exercise(
            id = "bf_leg_009",
            name = "Leg Extension Unilatérale Machine",
            type = HealthConnectExerciseType.LEG_EXTENSION,
            muscle = MuscleGroup.QUADRICEPS,
            equipment = EquipmentType.MACHINE,
            isUnilateral = true
        ),
        exercise(
            id = "bf_leg_010",
            name = "Leg Curl Assis Machine",
            type = HealthConnectExerciseType.LEG_CURL,
            muscle = MuscleGroup.HAMSTRINGS,
            equipment = EquipmentType.MACHINE
        ),
        exercise(
            id = "bf_leg_011",
            name = "Leg Curl Couché Machine",
            type = HealthConnectExerciseType.LEG_CURL,
            muscle = MuscleGroup.HAMSTRINGS,
            equipment = EquipmentType.MACHINE
        ),
        exercise(
            id = "bf_leg_012",
            name = "Leg Curl Unilatéral Machine",
            type = HealthConnectExerciseType.LEG_CURL,
            muscle = MuscleGroup.HAMSTRINGS,
            equipment = EquipmentType.MACHINE,
            isUnilateral = true
        ),
        exercise(
            id = "bf_leg_013",
            name = "Soulevé de Terre Roumain Barre",
            type = HealthConnectExerciseType.ROMANIAN_DEADLIFT,
            muscle = MuscleGroup.HAMSTRINGS,
            equipment = EquipmentType.BARBELL
        ),
        exercise(
            id = "bf_leg_014",
            name = "Soulevé de Terre Roumain Haltères",
            type = HealthConnectExerciseType.ROMANIAN_DEADLIFT,
            muscle = MuscleGroup.HAMSTRINGS,
            equipment = EquipmentType.DUMBBELL
        ),
        exercise(
            id = "bf_leg_015",
            name = "Split Squat Bulgare",
            type = HealthConnectExerciseType.LUNGE,
            muscle = MuscleGroup.QUADRICEPS,
            equipment = EquipmentType.DUMBBELL,
            isUnilateral = true
        ),
        exercise(
            id = "bf_leg_016",
            name = "Fentes Marchantes",
            type = HealthConnectExerciseType.LUNGE,
            muscle = MuscleGroup.QUADRICEPS,
            equipment = EquipmentType.DUMBBELL,
            isUnilateral = true
        ),
        exercise(
            id = "bf_leg_017",
            name = "Hip Thrust Barre",
            type = HealthConnectExerciseType.HIP_THRUST,
            muscle = MuscleGroup.GLUTES,
            equipment = EquipmentType.BARBELL
        ),
        exercise(
            id = "bf_leg_018",
            name = "Hip Thrust Machine",
            type = HealthConnectExerciseType.HIP_THRUST,
            muscle = MuscleGroup.GLUTES,
            equipment = EquipmentType.MACHINE
        ),
        exercise(
            id = "bf_leg_019",
            name = "Abducteurs Machine",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            muscle = MuscleGroup.GLUTES,
            equipment = EquipmentType.MACHINE
        ),
        exercise(
            id = "bf_leg_020",
            name = "Adducteurs Machine",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            muscle = MuscleGroup.GLUTES,
            equipment = EquipmentType.MACHINE
        ),
        exercise(
            id = "bf_leg_021",
            name = "Kickback Fessier Poulie",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            muscle = MuscleGroup.GLUTES,
            equipment = EquipmentType.CABLE,
            isUnilateral = true
        ),
        exercise(
            id = "bf_leg_022",
            name = "Mollets Debout Machine",
            type = HealthConnectExerciseType.CALF_RAISE,
            muscle = MuscleGroup.CALVES,
            equipment = EquipmentType.MACHINE
        ),
        exercise(
            id = "bf_leg_023",
            name = "Mollets Assis Machine",
            type = HealthConnectExerciseType.CALF_RAISE,
            muscle = MuscleGroup.CALVES,
            equipment = EquipmentType.MACHINE
        ),
        exercise(
            id = "bf_leg_024",
            name = "Mollets Presse à Cuisses",
            type = HealthConnectExerciseType.CALF_RAISE,
            muscle = MuscleGroup.CALVES,
            equipment = EquipmentType.MACHINE
        ),

        // ==========================================
        // 6. ABDOMINAUX & CORE (Abs)
        // ==========================================
        exercise(
            id = "bf_abs_001",
            name = "Relevés de Jambes Suspendu",
            type = HealthConnectExerciseType.LEG_RAISE,
            muscle = MuscleGroup.ABS,
            equipment = EquipmentType.BODYWEIGHT
        ),
        exercise(
            id = "bf_abs_002",
            name = "Relevés de Jambes au Sol",
            type = HealthConnectExerciseType.LEG_RAISE,
            muscle = MuscleGroup.ABS,
            equipment = EquipmentType.BODYWEIGHT
        ),
        exercise(
            id = "bf_abs_003",
            name = "Crunch Poulie Haute",
            type = HealthConnectExerciseType.CRUNCH,
            muscle = MuscleGroup.ABS,
            equipment = EquipmentType.CABLE
        ),
        exercise(
            id = "bf_abs_004",
            name = "Crunch Machine Assise",
            type = HealthConnectExerciseType.CRUNCH,
            muscle = MuscleGroup.ABS,
            equipment = EquipmentType.MACHINE
        ),
        exercise(
            id = "bf_abs_005",
            name = "Crunch Sol",
            type = HealthConnectExerciseType.CRUNCH,
            muscle = MuscleGroup.ABS,
            equipment = EquipmentType.BODYWEIGHT
        ),
        exercise(
            id = "bf_abs_006",
            name = "Gainage Planche",
            type = HealthConnectExerciseType.PLANK,
            muscle = MuscleGroup.ABS,
            equipment = EquipmentType.BODYWEIGHT
        ),
        exercise(
            id = "bf_abs_007",
            name = "Gainage Latéral",
            type = HealthConnectExerciseType.PLANK,
            muscle = MuscleGroup.ABS,
            equipment = EquipmentType.BODYWEIGHT,
            isUnilateral = true
        ),
        exercise(
            id = "bf_abs_008",
            name = "Pallof Press Poulie",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            muscle = MuscleGroup.ABS,
            equipment = EquipmentType.CABLE,
            isUnilateral = true
        ),
        exercise(
            id = "bf_abs_009",
            name = "Russian Twist",
            type = HealthConnectExerciseType.RUSSIAN_TWIST,
            muscle = MuscleGroup.ABS,
            equipment = EquipmentType.BODYWEIGHT
        ),
        exercise(
            id = "bf_abs_010",
            name = "Roue Abdominale",
            type = HealthConnectExerciseType.AB_WHEEL_ROLLOUT,
            muscle = MuscleGroup.ABS,
            equipment = EquipmentType.BODYWEIGHT
        ),
        exercise(
            id = "bf_abs_011",
            name = "Kettlebell Swing",
            type = HealthConnectExerciseType.KETTLEBELL_SWING,
            muscle = MuscleGroup.GLUTES,
            equipment = EquipmentType.KETTLEBELL
        ),
        exercise(
            id = "bf_abs_012",
            name = "Burpees",
            type = HealthConnectExerciseType.BURPEE,
            muscle = MuscleGroup.FULL_BODY,
            equipment = EquipmentType.BODYWEIGHT
        ),

        // ==========================================
        // 8. CARDIO, ÉCHAUFFEMENT & CONDITIONNEMENT
        // ==========================================
        exercise(
            id = "bf_cardio_001",
            name = "Vélo Elliptique",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            category = app.bodyforger.core.model.WorkoutActivityCategory.ELLIPTICAL,
            muscle = MuscleGroup.FULL_BODY,
            equipment = EquipmentType.MACHINE
        ),
        exercise(
            id = "bf_cardio_002",
            name = "Tapis de Course (Marche Inclinée)",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            category = app.bodyforger.core.model.WorkoutActivityCategory.TREADMILL_WALKING,
            muscle = MuscleGroup.FULL_BODY,
            equipment = EquipmentType.MACHINE
        ),
        exercise(
            id = "bf_cardio_003",
            name = "Tapis de Course (Course)",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            category = app.bodyforger.core.model.WorkoutActivityCategory.TREADMILL_RUNNING,
            muscle = MuscleGroup.FULL_BODY,
            equipment = EquipmentType.MACHINE
        ),
        exercise(
            id = "bf_cardio_004",
            name = "Vélo Stationnaire / Biking",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            category = app.bodyforger.core.model.WorkoutActivityCategory.STATIONARY_BIKING,
            muscle = MuscleGroup.QUADRICEPS,
            equipment = EquipmentType.MACHINE
        ),
        exercise(
            id = "bf_cardio_005",
            name = "Rameur (Rowing)",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            category = app.bodyforger.core.model.WorkoutActivityCategory.ROWING_MACHINE,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.MACHINE
        ),
        exercise(
            id = "bf_cardio_006",
            name = "Corde à Sauter",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            category = app.bodyforger.core.model.WorkoutActivityCategory.HIIT,
            muscle = MuscleGroup.CALVES,
            equipment = EquipmentType.OTHER
        ),
        exercise(
            id = "bf_cardio_007",
            name = "StairMaster (Simulateur d'Escaliers)",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            category = app.bodyforger.core.model.WorkoutActivityCategory.STATIONARY_BIKING,
            muscle = MuscleGroup.GLUTES,
            equipment = EquipmentType.MACHINE
        ),

        // ==========================================
        // 9. MOBILITÉ & RÉCUPÉRATION
        // ==========================================
        exercise(
            id = "bf_mob_001",
            name = "Étirements & Mobilité Globale",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            category = app.bodyforger.core.model.WorkoutActivityCategory.STRETCHING,
            muscle = MuscleGroup.FULL_BODY,
            equipment = EquipmentType.BODYWEIGHT
        ),
        exercise(
            id = "bf_mob_002",
            name = "Rouleau de Massage (Foam Roller)",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            category = app.bodyforger.core.model.WorkoutActivityCategory.STRETCHING,
            muscle = MuscleGroup.FULL_BODY,
            equipment = EquipmentType.OTHER
        )
    )

    private fun exercise(
        id: String,
        name: String,
        type: HealthConnectExerciseType,
        category: app.bodyforger.core.model.WorkoutActivityCategory = app.bodyforger.core.model.WorkoutActivityCategory.STRENGTH_TRAINING,
        muscle: MuscleGroup,
        equipment: EquipmentType,
        isUnilateral: Boolean = false
    ) = ExerciseEntity(
        id = id,
        name = name,
        activityCategory = category.name,
        healthConnectType = type.name,
        primaryMuscleGroup = muscle.name,
        equipment = equipment.name,
        isUnilateral = isUnilateral,
        isCustom = false
    )
}
