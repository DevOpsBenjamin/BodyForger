package app.bodyforger.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.bodyforger.core.model.EquipmentType
import app.bodyforger.core.model.Exercise
import app.bodyforger.core.model.HealthConnectExerciseType
import app.bodyforger.core.model.MuscleGroup
import app.bodyforger.core.model.WorkoutActivityCategory

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val activityCategory: String = "STRENGTH_TRAINING",
    val healthConnectType: String,
    val primaryMuscleGroup: String,
    val equipment: String,
    val secondaryMuscleGroupsJson: String = "[]",
    val isUnilateral: Boolean = false,
    val isCustom: Boolean = false,
    val instructionsJson: String = "[]"
)

fun ExerciseEntity.toDomain(): Exercise {
    val hcType = try {
        HealthConnectExerciseType.valueOf(healthConnectType)
    } catch (_: Exception) {
        HealthConnectExerciseType.OTHER_WORKOUT
    }

    val category = try {
        WorkoutActivityCategory.valueOf(activityCategory)
    } catch (_: Exception) {
        WorkoutActivityCategory.STRENGTH_TRAINING
    }

    val muscle = try {
        MuscleGroup.valueOf(primaryMuscleGroup)
    } catch (_: Exception) {
        hcType.primaryMuscleGroup
    }

    val equip = try {
        EquipmentType.valueOf(equipment)
    } catch (_: Exception) {
        hcType.defaultEquipment
    }

    return Exercise(
        id = id,
        name = name,
        activityCategory = category,
        healthConnectType = hcType,
        primaryMuscleGroup = muscle,
        equipment = equip,
        isUnilateral = isUnilateral,
        isCustom = isCustom
    )
}

fun Exercise.toEntity(): ExerciseEntity {
    return ExerciseEntity(
        id = id,
        name = name,
        activityCategory = activityCategory.name,
        healthConnectType = healthConnectType.name,
        primaryMuscleGroup = primaryMuscleGroup.name,
        equipment = equipment.name,
        isUnilateral = isUnilateral,
        isCustom = isCustom
    )
}
