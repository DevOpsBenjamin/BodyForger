package app.bodyforger.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.bodyforger.core.model.ElectrodeCount
import app.bodyforger.core.model.ScaleAssociation
import app.bodyforger.core.model.ScaleCapability

/**
 * The lasting link between the athlete and a scale.
 *
 * ⚠️ The HUID is not here: it belongs to the athlete and outlives any association, which only
 * exists once a tare has been read. An association that exists is therefore complete.
 */
@Entity(tableName = "scale_associations")
data class ScaleAssociationEntity(
    /** Physical address from the native scan; also the key. */
    @PrimaryKey
    val deviceAddress: String,
    val tareKg: Double,
    val advertisedName: String,
    /** Electrode count of the hardware ceiling, or `null` when the model is unknown. */
    val electrodeCount: String?,
    /** Frequencies the hardware can read, in kHz, comma separated. */
    val frequenciesKHz: String?,
    val associatedAtEpochMs: Long,
    /** Per ADR 001 §D: `LOCAL_ONLY`, `SYNCED_PEER`, `SYNCED_CLOUD`. */
    val syncState: String = "LOCAL_ONLY"
)

fun ScaleAssociationEntity.toDomain(): ScaleAssociation = ScaleAssociation(
    deviceAddress = deviceAddress,
    huid = "",
    tareKg = tareKg,
    advertisedName = advertisedName,
    capability = electrodeCount?.let { count ->
        val electrodes = runCatching { ElectrodeCount.valueOf(count) }.getOrNull()
            ?: return@let null
        val frequencies = frequenciesKHz
            ?.split(",")
            ?.mapNotNull { it.trim().toIntOrNull() }
            ?: emptyList()
        runCatching { ScaleCapability(electrodes, frequencies) }.getOrNull()
    }
)

fun ScaleAssociation.toEntity(associatedAtEpochMs: Long): ScaleAssociationEntity =
    ScaleAssociationEntity(
        deviceAddress = deviceAddress,
        tareKg = tareKg,
        advertisedName = advertisedName,
        electrodeCount = capability?.electrodeCount?.name,
        frequenciesKHz = capability?.frequenciesKHz?.joinToString(","),
        associatedAtEpochMs = associatedAtEpochMs
    )
