package app.bodyforger.core.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import app.bodyforger.core.model.EquipmentType
import app.bodyforger.core.model.MuscleGroup
import app.bodyforger.core.model.Routine
import app.bodyforger.core.model.RoutineExercise
import app.bodyforger.core.model.RoutineSet
import app.bodyforger.core.model.RoutineSetType
import app.bodyforger.core.model.WeightUnit
import java.util.UUID

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val notes: String = "",
    val assignedDaysCsv: String = "", // e.g. "1,3,5"
    val createdAtEpochMs: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "routine_exercises",
    foreignKeys = [
        ForeignKey(
            entity = RoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("routineId")]
)
data class RoutineExerciseEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val routineId: String,
    val exerciseId: String,
    val exerciseName: String,
    val primaryMuscle: String,
    val equipment: String,
    val isUnilateral: Boolean = false,
    val weightUnit: String = "KG",
    val orderIndex: Int = 0,
    val restTimeSeconds: Int = 90,
    val notes: String = "",
    val supersetGroupId: String? = null
)

@Entity(
    tableName = "routine_sets",
    foreignKeys = [
        ForeignKey(
            entity = RoutineExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineExerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("routineExerciseId")]
)
data class RoutineSetEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val routineExerciseId: String,
    val setIndex: Int = 1,
    val type: String = "NORMAL",
    val targetWeightKg: Double? = null,
    val reps: Int? = 10,
    val minReps: Int? = 8,
    val maxReps: Int? = 12,
    val isRepsRange: Boolean = false
)

data class RoutineExerciseWithSets(
    @Embedded val exercise: RoutineExerciseEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "routineExerciseId"
    )
    val sets: List<RoutineSetEntity>
)

data class RoutineWithExercises(
    @Embedded val routine: RoutineEntity,
    @Relation(
        entity = RoutineExerciseEntity::class,
        parentColumn = "id",
        entityColumn = "routineId"
    )
    val exercises: List<RoutineExerciseWithSets>
)

// --- MAPPERS DOMAIN <-> ENTITY ---

fun Routine.toEntity(): RoutineEntity = RoutineEntity(
    id = id,
    name = name,
    notes = notes,
    assignedDaysCsv = assignedDays.sorted().joinToString(","),
    createdAtEpochMs = createdAtEpochMs
)

fun RoutineExercise.toEntity(parentRoutineId: String): RoutineExerciseEntity = RoutineExerciseEntity(
    id = id,
    routineId = parentRoutineId,
    exerciseId = exerciseId,
    exerciseName = exerciseName,
    primaryMuscle = primaryMuscle.name,
    equipment = equipment.name,
    isUnilateral = isUnilateral,
    weightUnit = weightUnit.name,
    orderIndex = orderIndex,
    restTimeSeconds = restTimeSeconds,
    notes = notes,
    supersetGroupId = supersetGroupId
)

fun RoutineSet.toEntity(parentExerciseId: String): RoutineSetEntity = RoutineSetEntity(
    id = id,
    routineExerciseId = parentExerciseId,
    setIndex = setIndex,
    type = type.name,
    targetWeightKg = targetWeightKg,
    reps = reps,
    minReps = minReps,
    maxReps = maxReps,
    isRepsRange = isRepsRange
)

fun RoutineWithExercises.toDomain(): Routine {
    val days = routine.assignedDaysCsv
        .split(",")
        .mapNotNull { it.trim().toIntOrNull() }
        .toSet()

    val domainExercises = exercises
        .sortedBy { it.exercise.orderIndex }
        .map { exerciseWithSets ->
            val ex = exerciseWithSets.exercise
            val domainSets = exerciseWithSets.sets
                .sortedBy { it.setIndex }
                .map { s ->
                    RoutineSet(
                        id = s.id,
                        setIndex = s.setIndex,
                        type = try { RoutineSetType.valueOf(s.type) } catch (e: Exception) { RoutineSetType.NORMAL },
                        targetWeightKg = s.targetWeightKg,
                        reps = s.reps,
                        minReps = s.minReps,
                        maxReps = s.maxReps,
                        isRepsRange = s.isRepsRange
                    )
                }

            RoutineExercise(
                id = ex.id,
                routineId = ex.routineId,
                exerciseId = ex.exerciseId,
                exerciseName = ex.exerciseName,
                primaryMuscle = try { MuscleGroup.valueOf(ex.primaryMuscle) } catch (e: Exception) { MuscleGroup.FULL_BODY },
                equipment = try { EquipmentType.valueOf(ex.equipment) } catch (e: Exception) { EquipmentType.BODYWEIGHT },
                isUnilateral = ex.isUnilateral,
                weightUnit = try { WeightUnit.valueOf(ex.weightUnit) } catch (e: Exception) { WeightUnit.KG },
                orderIndex = ex.orderIndex,
                restTimeSeconds = ex.restTimeSeconds,
                notes = ex.notes,
                sets = if (domainSets.isEmpty()) listOf(RoutineSet(setIndex = 1)) else domainSets,
                supersetGroupId = ex.supersetGroupId
            )
        }

    return Routine(
        id = routine.id,
        name = routine.name,
        notes = routine.notes,
        assignedDays = days,
        exercises = domainExercises,
        createdAtEpochMs = routine.createdAtEpochMs
    )
}
