package app.bodyforger.core.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import app.bodyforger.core.model.BodyLog
import app.bodyforger.core.model.ImpedancePath
import app.bodyforger.core.model.ImpedanceReading
import app.bodyforger.core.model.RawImpedances

/**
 * A body reading as it is stored.
 *
 * The body fat percentage is the one the scale sent, or the one the athlete entered: received
 * data, not a computation. Ours is never persisted — it recomputes from the resistances.
 */
@Entity(tableName = "body_logs")
data class BodyLogEntity(
    @PrimaryKey
    val id: String,
    val dateIso: String,
    val measuredAtEpochMs: Long,
    val massKg: Double,
    val bodyFatPercentage: Double,
    val restingHeartRateBpm: Int?,
    /** Address of the scale that produced this reading, or `null` for a manual entry. */
    val sourceDeviceAddress: String? = null
)

/**
 * One measured resistance, one row.
 *
 * A child table rather than a JSON column: a period is aggregated on the median of the
 * resistances, which SQL computes over rows, and an unmeasured quantity stays an absent row
 * rather than an encoded zero.
 */
@Entity(
    tableName = "body_log_impedances",
    foreignKeys = [
        ForeignKey(
            entity = BodyLogEntity::class,
            parentColumns = ["id"],
            childColumns = ["bodyLogId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("bodyLogId"), Index(value = ["bodyLogId", "path", "frequencyKHz"], unique = true)]
)
data class BodyLogImpedanceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bodyLogId: String,
    /** Path name, never its index in the frame — that index has already changed once. */
    val path: String,
    val frequencyKHz: Int,
    val ohms: Double
)

/** A reading and its resistances, as they are read together. */
data class BodyLogWithImpedances(
    @Embedded val log: BodyLogEntity,
    @Relation(parentColumn = "id", entityColumn = "bodyLogId")
    val impedances: List<BodyLogImpedanceEntity>
)

fun BodyLogWithImpedances.toDomain(): BodyLog = BodyLog(
    id = log.id,
    dateIso = log.dateIso,
    measuredAtEpochMs = log.measuredAtEpochMs,
    massKg = log.massKg,
    bodyFatPercentage = log.bodyFatPercentage,
    rawImpedances = RawImpedances.of(
        impedances.mapNotNull { row ->
            val path = runCatching { ImpedancePath.valueOf(row.path) }.getOrNull() ?: return@mapNotNull null
            ImpedanceReading(path, row.frequencyKHz) to row.ohms
        }.toMap()
    ),
    restingHeartRateBpm = log.restingHeartRateBpm
)

fun BodyLog.toEntity(sourceDeviceAddress: String? = null): BodyLogEntity = BodyLogEntity(
    id = id,
    dateIso = dateIso,
    measuredAtEpochMs = measuredAtEpochMs,
    massKg = massKg,
    bodyFatPercentage = bodyFatPercentage,
    restingHeartRateBpm = restingHeartRateBpm,
    sourceDeviceAddress = sourceDeviceAddress
)

fun BodyLog.impedanceRows(): List<BodyLogImpedanceEntity> =
    rawImpedances.ohmsByReading.map { (reading, ohms) ->
        BodyLogImpedanceEntity(
            bodyLogId = id,
            path = reading.path.name,
            frequencyKHz = reading.frequencyKHz,
            ohms = ohms
        )
    }
