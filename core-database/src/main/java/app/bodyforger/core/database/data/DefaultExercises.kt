package app.bodyforger.core.database.data

import app.bodyforger.core.database.entity.ExerciseEntity
import app.bodyforger.core.model.EquipmentType
import app.bodyforger.core.model.HealthConnectExerciseType
import app.bodyforger.core.model.MuscleGroup

/** The built-in exercise catalogue, pre-loaded when the database is created. */
object DefaultExercises {

    val all: List<ExerciseEntity> =
        PushExercises.all + PullExercises.all + LegExercises.all + CoreExercises.all

    internal fun exercise(
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
