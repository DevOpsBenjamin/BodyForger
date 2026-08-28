package app.bodyforger.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val startedAtEpochMs: Long,
    val endedAtEpochMs: Long?,
    val averageHeartRateBpm: Int?,
    val activeCaloriesKcal: Int?,
    val isFinalized: Boolean
)
