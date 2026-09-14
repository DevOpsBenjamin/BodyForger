package app.bodyforger.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import app.bodyforger.core.model.WorkoutHeartRateSample

@Entity(
    tableName = "workout_heart_rate_samples",
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
        Index("timestampEpochMs")
    ]
)
data class WorkoutHeartRateSampleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: String,
    val timestampEpochMs: Long,
    val bpm: Int
)

fun WorkoutHeartRateSampleEntity.toDomain(): WorkoutHeartRateSample = WorkoutHeartRateSample(
    timestampEpochMs = timestampEpochMs,
    bpm = bpm
)

fun WorkoutHeartRateSample.toEntity(sessionId: String): WorkoutHeartRateSampleEntity = WorkoutHeartRateSampleEntity(
    sessionId = sessionId,
    timestampEpochMs = timestampEpochMs,
    bpm = bpm
)
