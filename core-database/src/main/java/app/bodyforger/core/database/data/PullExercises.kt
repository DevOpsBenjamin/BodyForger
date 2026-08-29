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
            name = "Tractions Pronation",
            type = HealthConnectExerciseType.PULL_UP,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.BODYWEIGHT
        ),
        DefaultExercises.exercise(
            id = "bf_back_002",
            name = "Tractions Supination",
            type = HealthConnectExerciseType.CHIN_UP,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.BODYWEIGHT
        ),
        DefaultExercises.exercise(
            id = "bf_back_003",
            name = "Tractions Prise Neutre",
            type = HealthConnectExerciseType.PULL_UP,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.BODYWEIGHT
        ),
        DefaultExercises.exercise(
            id = "bf_back_004",
            name = "Dead Hang Pronation",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.BODYWEIGHT
        ),
        DefaultExercises.exercise(
            id = "bf_back_005",
            name = "Dead Hang Supination",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.BODYWEIGHT
        ),
        DefaultExercises.exercise(
            id = "bf_back_006",
            name = "Dead Hang Prise Neutre",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.BODYWEIGHT
        ),
        DefaultExercises.exercise(
            id = "bf_back_007",
            name = "Tractions Assistées Machine Pronation",
            type = HealthConnectExerciseType.PULL_UP,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_back_008",
            name = "Tractions Assistées Machine Supination",
            type = HealthConnectExerciseType.CHIN_UP,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_back_009",
            name = "Tractions Assistées Machine Prise Neutre",
            type = HealthConnectExerciseType.PULL_UP,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_back_010",
            name = "Soulevé de Terre",
            type = HealthConnectExerciseType.DEADLIFT,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.BARBELL
        ),
        DefaultExercises.exercise(
            id = "bf_back_011",
            name = "Soulevé de Terre Trap Bar",
            type = HealthConnectExerciseType.DEADLIFT,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.BARBELL
        ),
        DefaultExercises.exercise(
            id = "bf_back_012",
            name = "Tirage Vertical Prise Large",
            type = HealthConnectExerciseType.LAT_PULLDOWN,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.CABLE
        ),
        DefaultExercises.exercise(
            id = "bf_back_013",
            name = "Tirage Vertical Prise Neutre",
            type = HealthConnectExerciseType.LAT_PULLDOWN,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.CABLE
        ),
        DefaultExercises.exercise(
            id = "bf_back_014",
            name = "Tirage Vertical Machine",
            type = HealthConnectExerciseType.LAT_PULLDOWN,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_back_015",
            name = "Tirage Vertical Convergent",
            type = HealthConnectExerciseType.LAT_PULLDOWN,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.MACHINE_CONVERGENT
        ),
        DefaultExercises.exercise(
            id = "bf_back_016",
            name = "Tirage Vertical Poulie Unilatéral",
            type = HealthConnectExerciseType.LAT_PULLDOWN,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.CABLE,
            isUnilateral = true
        ),
        DefaultExercises.exercise(
            id = "bf_back_017",
            name = "Pull-Over Poulie Haute",
            type = HealthConnectExerciseType.LAT_PULLDOWN,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.CABLE
        ),
        DefaultExercises.exercise(
            id = "bf_back_018",
            name = "Tirage Horizontal Poulie Basse",
            type = HealthConnectExerciseType.SEATED_CABLE_ROW,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.CABLE
        ),
        DefaultExercises.exercise(
            id = "bf_back_019",
            name = "Tirage Horizontal Prise Large",
            type = HealthConnectExerciseType.SEATED_CABLE_ROW,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.CABLE
        ),
        DefaultExercises.exercise(
            id = "bf_back_020",
            name = "Tirage Horizontal Poulie Unilatéral",
            type = HealthConnectExerciseType.SEATED_CABLE_ROW,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.CABLE,
            isUnilateral = true
        ),
        DefaultExercises.exercise(
            id = "bf_back_021",
            name = "Rowing Barre Buste Penché",
            type = HealthConnectExerciseType.BENT_OVER_ROW,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.BARBELL
        ),
        DefaultExercises.exercise(
            id = "bf_back_022",
            name = "Rowing Haltère Unilatéral",
            type = HealthConnectExerciseType.BENT_OVER_ROW,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.DUMBBELL,
            isUnilateral = true
        ),
        DefaultExercises.exercise(
            id = "bf_back_023",
            name = "T-Bar Row",
            type = HealthConnectExerciseType.BENT_OVER_ROW,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.BARBELL
        ),
        DefaultExercises.exercise(
            id = "bf_back_024",
            name = "Rowing Assis Machine",
            type = HealthConnectExerciseType.BENT_OVER_ROW,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_back_025",
            name = "Rowing Convergent Machine",
            type = HealthConnectExerciseType.BENT_OVER_ROW,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.MACHINE_CONVERGENT
        ),
        DefaultExercises.exercise(
            id = "bf_back_026",
            name = "Extensions Lombaires Banc 45°",
            type = HealthConnectExerciseType.BACK_EXTENSION,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.BODYWEIGHT
        ),
        DefaultExercises.exercise(
            id = "bf_back_027",
            name = "Machine à Lombaires Assise",
            type = HealthConnectExerciseType.BACK_EXTENSION,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_cardio_005",
            name = "Rameur (Rowing)",
            type = HealthConnectExerciseType.OTHER_WORKOUT,
            category = app.bodyforger.core.model.WorkoutActivityCategory.ROWING_MACHINE,
            muscle = MuscleGroup.BACK,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_arm_001",
            name = "Curl Biceps Barre",
            type = HealthConnectExerciseType.BARBELL_CURL,
            muscle = MuscleGroup.BICEPS,
            equipment = EquipmentType.BARBELL
        ),
        DefaultExercises.exercise(
            id = "bf_arm_002",
            name = "Curl Pupitre Barre EZ",
            type = HealthConnectExerciseType.BARBELL_CURL,
            muscle = MuscleGroup.BICEPS,
            equipment = EquipmentType.BARBELL
        ),
        DefaultExercises.exercise(
            id = "bf_arm_003",
            name = "Curl Biceps Haltères",
            type = HealthConnectExerciseType.DUMBBELL_CURL,
            muscle = MuscleGroup.BICEPS,
            equipment = EquipmentType.DUMBBELL
        ),
        DefaultExercises.exercise(
            id = "bf_arm_004",
            name = "Curl Biceps Incliné",
            type = HealthConnectExerciseType.DUMBBELL_CURL,
            muscle = MuscleGroup.BICEPS,
            equipment = EquipmentType.DUMBBELL
        ),
        DefaultExercises.exercise(
            id = "bf_arm_005",
            name = "Curl Marteau Haltères",
            type = HealthConnectExerciseType.HAMMER_CURL,
            muscle = MuscleGroup.BICEPS,
            equipment = EquipmentType.DUMBBELL
        ),
        DefaultExercises.exercise(
            id = "bf_arm_006",
            name = "Curl Pupitre Haltère Unilatéral",
            type = HealthConnectExerciseType.DUMBBELL_CURL,
            muscle = MuscleGroup.BICEPS,
            equipment = EquipmentType.DUMBBELL,
            isUnilateral = true
        ),
        DefaultExercises.exercise(
            id = "bf_arm_007",
            name = "Curl Corde Poulie Basse",
            type = HealthConnectExerciseType.HAMMER_CURL,
            muscle = MuscleGroup.BICEPS,
            equipment = EquipmentType.CABLE
        ),
        DefaultExercises.exercise(
            id = "bf_arm_008",
            name = "Curl Biceps Poulie Basse Unilatéral",
            type = HealthConnectExerciseType.BARBELL_CURL,
            muscle = MuscleGroup.BICEPS,
            equipment = EquipmentType.CABLE,
            isUnilateral = true
        ),
        DefaultExercises.exercise(
            id = "bf_arm_009",
            name = "Curl Biceps Machine",
            type = HealthConnectExerciseType.BARBELL_CURL,
            muscle = MuscleGroup.BICEPS,
            equipment = EquipmentType.MACHINE
        ),
        DefaultExercises.exercise(
            id = "bf_arm_010",
            name = "Curl Biceps Convergent",
            type = HealthConnectExerciseType.BARBELL_CURL,
            muscle = MuscleGroup.BICEPS,
            equipment = EquipmentType.MACHINE_CONVERGENT
        ),
        DefaultExercises.exercise(
            id = "bf_arm_018",
            name = "Curl Inversé Barre",
            type = HealthConnectExerciseType.BARBELL_CURL,
            muscle = MuscleGroup.BICEPS,
            equipment = EquipmentType.BARBELL
        ),
    )
}
