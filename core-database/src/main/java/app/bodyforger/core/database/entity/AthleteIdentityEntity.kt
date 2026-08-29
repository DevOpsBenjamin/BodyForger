package app.bodyforger.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * L'identité de l'athlète vis-à-vis des balances : une seule ligne, un seul HUID.
 *
 * Le HUID est **une propriété de la personne, pas du lien à une balance** (#19). Il est
 * généré une seule fois, puis répliqué vers l'autre appareil ; il survit à tout échec
 * d'appairage, là où une Association n'existe qu'une fois la tare relevée.
 *
 * ⚠️ **Un seul HUID par athlète, quel que soit l'appareil qui appaire.** Deux identifiants
 * distincts occuperaient deux emplacements dans la mémoire flash de la balance et
 * scinderaient l'historique en deux personnes. C'est pourquoi il ne se régénère jamais quand
 * il existe déjà — reçu du pair ou créé localement.
 */
@Entity(tableName = "athlete_identity")
data class AthleteIdentityEntity(
    /** Ligne unique : l'athlète est singulier sur une installation. */
    @PrimaryKey
    val id: Int = SINGLETON_ID,
    val huid: String,
    val createdAtEpochMs: Long,
    /** Suit l'ADR 001 §D : `LOCAL_ONLY`, `SYNCED_PEER`, `SYNCED_CLOUD`. */
    val syncState: String = "LOCAL_ONLY"
) {
    companion object {
        const val SINGLETON_ID = 1

        /**
         * Longueur du HUID transmis à la balance : trente octets ASCII au plus, complétés de
         * zéros. Dix-sept chiffres suivent la forme observée sur le matériel.
         */
        const val HUID_DIGITS = 17
    }
}
