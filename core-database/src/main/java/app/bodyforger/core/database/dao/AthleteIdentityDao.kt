package app.bodyforger.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import app.bodyforger.core.database.entity.AthleteIdentityEntity
import java.security.SecureRandom

@Dao
interface AthleteIdentityDao {

    @Query("SELECT * FROM athlete_identity WHERE id = :id LIMIT 1")
    suspend fun find(id: Int = AthleteIdentityEntity.SINGLETON_ID): AthleteIdentityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(identity: AthleteIdentityEntity)

    /**
     * Returns the athlete's HUID, creating one only if none exists.
     *
     * ⚠️ Never regenerates an existing HUID: each new one consumes a slot in the scale's flash
     * memory for good.
     */
    @Transaction
    suspend fun huidOrCreate(nowEpochMs: Long): String {
        find()?.let { return it.huid }
        val generated = generateHuid()
        upsert(AthleteIdentityEntity(huid = generated, createdAtEpochMs = nowEpochMs))
        return generated
    }

    /** Adopts the HUID received from the peer device; the first one wins. */
    @Transaction
    suspend fun adopt(huid: String, nowEpochMs: Long) {
        upsert(
            AthleteIdentityEntity(
                huid = huid,
                createdAtEpochMs = nowEpochMs,
                syncState = "SYNCED_PEER"
            )
        )
    }

    private fun generateHuid(): String {
        val random = SecureRandom()
        return buildString(AthleteIdentityEntity.HUID_DIGITS) {
            repeat(AthleteIdentityEntity.HUID_DIGITS) { append(random.nextInt(10)) }
        }
    }
}
