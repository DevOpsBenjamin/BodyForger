package app.bodyforger.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "body_logs")
data class BodyLogEntity(
    @PrimaryKey
    val id: String,
    val dateIso: String,
    val measuredAtEpochMs: Long,
    val massKg: Double,
    val bodyFatPercentage: Double?,
    val restingHeartRateBpm: Int?
)
