package app.bodyforger.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The athlete's identity towards scales: one row, one HUID.
 *
 * ⚠️ One HUID per athlete, whichever device pairs. Two distinct identifiers would occupy two
 * slots in the scale's flash and split the history into two people, so it is never
 * regenerated once it exists.
 */
@Entity(tableName = "athlete_identity")
data class AthleteIdentityEntity(
    /** Single row: an installation has one athlete. */
    @PrimaryKey
    val id: Int = SINGLETON_ID,
    val huid: String,
    val createdAtEpochMs: Long,
    /** Per ADR 001 §D: `LOCAL_ONLY`, `SYNCED_PEER`, `SYNCED_CLOUD`. */
    val syncState: String = "LOCAL_ONLY"
) {
    companion object {
        const val SINGLETON_ID = 1

        /** Digits in a generated HUID; thirty ASCII bytes at most reach the scale. */
        const val HUID_DIGITS = 17
    }
}
