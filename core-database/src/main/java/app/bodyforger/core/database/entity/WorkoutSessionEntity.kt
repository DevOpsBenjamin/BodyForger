package app.bodyforger.core.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import app.bodyforger.core.model.EquipmentType
import app.bodyforger.core.model.MuscleGroup
import app.bodyforger.core.model.RoutineSetType
import app.bodyforger.core.model.UnilateralSide
import app.bodyforger.core.model.WeightUnit
import app.bodyforger.core.model.WorkoutActivityCategory
import app.bodyforger.core.model.WorkoutSession
import app.bodyforger.core.model.WorkoutSessionStatus
import app.bodyforger.core.model.WorkoutSet

@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey
    val id: String,
    val routineId: String? = null,
    val title: String,
    val notes: String = "",
    val status: String = WorkoutSessionStatus.ACTIVE.name,
    val startedAtEpochMs: Long,
    val endedAtEpochMs: Long? = null,
    val averageHeartRateBpm: Int? = null,
    val activeCaloriesKcal: Int? = null,
    val totalVolumeKg: Double = 0.0,
    val isFinalized: Boolean = false
)

@Entity(
    tableName = "workout_sets",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("sessionId"),
        Index("exerciseId")
    ]
)
data class WorkoutSetEntity(
    @PrimaryKey
    val id: String,
    val sessionId: String,
    val exerciseId: String,
    val exerciseName: String,
    val primaryMuscle: String = MuscleGroup.CHEST.name,
    val equipment: String = EquipmentType.BARBELL.name,
    val activityCategory: String = WorkoutActivityCategory.STRENGTH_TRAINING.name,
    val orderIndex: Int = 0,
    val setIndex: Int = 1,
    val type: String = RoutineSetType.NORMAL.name,
    val weightKg: Double = 0.0,
    val weightUnit: String = WeightUnit.KG.name,
    val reps: Int = 0,
    val rpe: Double? = null,
    val isCompleted: Boolean = false,
    val side: String = UnilateralSide.NONE.name,
    val restTimeSeconds: Int = 90,
    val completedAtEpochMs: Long? = null
)

data class WorkoutSessionWithSets(
    @Embedded
    val session: WorkoutSessionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionId"
    )
    val sets: List<WorkoutSetEntity>
)

fun WorkoutSessionEntity.toDomain(sets: List<WorkoutSet> = emptyList()): WorkoutSession = WorkoutSession(
    id = id,
    routineId = routineId,
    title = title,
    notes = notes,
    status = try { WorkoutSessionStatus.valueOf(status) } catch (e: Exception) { WorkoutSessionStatus.ACTIVE },
    startedAtEpochMs = startedAtEpochMs,
    endedAtEpochMs = endedAtEpochMs,
    sets = sets,
    averageHeartRateBpm = averageHeartRateBpm,
    activeCaloriesKcal = activeCaloriesKcal,
    totalVolumeKg = totalVolumeKg,
    isFinalized = isFinalized
)

fun WorkoutSetEntity.toDomain(): WorkoutSet = WorkoutSet(
    id = id,
    sessionId = sessionId,
    exerciseId = exerciseId,
    exerciseName = exerciseName,
    primaryMuscle = try { MuscleGroup.valueOf(primaryMuscle) } catch (e: Exception) { MuscleGroup.CHEST },
    equipment = try { EquipmentType.valueOf(equipment) } catch (e: Exception) { EquipmentType.BARBELL },
    activityCategory = try { WorkoutActivityCategory.valueOf(activityCategory) } catch (e: Exception) { WorkoutActivityCategory.STRENGTH_TRAINING },
    orderIndex = orderIndex,
    setIndex = setIndex,
    type = try { RoutineSetType.valueOf(type) } catch (e: Exception) { RoutineSetType.NORMAL },
    weightKg = weightKg,
    weightUnit = try { WeightUnit.valueOf(weightUnit) } catch (e: Exception) { WeightUnit.KG },
    reps = reps,
    rpe = rpe,
    isCompleted = isCompleted,
    side = try { UnilateralSide.valueOf(side) } catch (e: Exception) { UnilateralSide.NONE },
    restTimeSeconds = restTimeSeconds,
    completedAtEpochMs = completedAtEpochMs
)

fun WorkoutSessionWithSets.toDomain(): WorkoutSession = session.toDomain(
    sets = sets.sortedWith(compareBy({ it.orderIndex }, { it.setIndex }, { it.side })).map { it.toDomain() }
)

fun WorkoutSession.toEntity(): WorkoutSessionEntity = WorkoutSessionEntity(
    id = id,
    routineId = routineId,
    title = title,
    notes = notes,
    status = status.name,
    startedAtEpochMs = startedAtEpochMs,
    endedAtEpochMs = endedAtEpochMs,
    averageHeartRateBpm = averageHeartRateBpm,
    activeCaloriesKcal = activeCaloriesKcal,
    totalVolumeKg = totalVolumeKg,
    isFinalized = isFinalized
)

fun WorkoutSet.toEntity(sessionId: String): WorkoutSetEntity = WorkoutSetEntity(
    id = id,
    sessionId = sessionId,
    exerciseId = exerciseId,
    exerciseName = exerciseName,
    primaryMuscle = primaryMuscle.name,
    equipment = equipment.name,
    activityCategory = activityCategory.name,
    orderIndex = orderIndex,
    setIndex = setIndex,
    type = type.name,
    weightKg = weightKg,
    weightUnit = weightUnit.name,
    reps = reps,
    rpe = rpe,
    isCompleted = isCompleted,
    side = side.name,
    restTimeSeconds = restTimeSeconds,
    completedAtEpochMs = completedAtEpochMs
)
