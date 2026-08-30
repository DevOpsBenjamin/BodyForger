package app.bodyforger.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.bodyforger.core.model.ElectrodeCount
import app.bodyforger.core.model.ScaleAssociation
import app.bodyforger.core.model.ScaleCapability

/**
 * Le lien durable entre l'athlète et une balance.
 *
 * ⚠️ **Le HUID n'est pas ici.** Il appartient à l'athlète et non à ce lien : généré une seule
 * fois à l'initialisation de la base, il survit à tout échec d'appairage, là où une
 * Association n'existe qu'une fois la tare relevée (#19). Les deux n'ont ni la même durée de
 * vie ni le même propriétaire.
 *
 * Une Association qui existe est donc, par construction, complète.
 */
@Entity(tableName = "scale_associations")
data class ScaleAssociationEntity(
    /** Adresse physique issue du scan natif, jamais d'une saisie. C'est aussi la clé. */
    @PrimaryKey
    val deviceAddress: String,
    val tareKg: Double,
    val advertisedName: String,
    /** Nombre d'électrodes du plafond matériel, ou `null` si le modèle est inconnu. */
    val electrodeCount: String?,
    /** Fréquences que le matériel sait relever, en kHz, séparées par des virgules. */
    val frequenciesKHz: String?,
    val associatedAtEpochMs: Long,
    /** Suit l'ADR 001 §D : `LOCAL_ONLY`, `SYNCED_PEER`, `SYNCED_CLOUD`. */
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
