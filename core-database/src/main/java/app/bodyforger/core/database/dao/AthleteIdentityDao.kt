package app.bodyforger.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import app.bodyforger.core.database.entity.AthleteIdentityEntity
import kotlinx.coroutines.flow.Flow
import java.security.SecureRandom

@Dao
interface AthleteIdentityDao {

    @Query("SELECT * FROM athlete_identity WHERE id = :id LIMIT 1")
    suspend fun find(id: Int = AthleteIdentityEntity.SINGLETON_ID): AthleteIdentityEntity?

    @Query("SELECT * FROM athlete_identity WHERE id = :id LIMIT 1")
    fun observe(id: Int = AthleteIdentityEntity.SINGLETON_ID): Flow<AthleteIdentityEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(identity: AthleteIdentityEntity)

    @Query(
        """
        UPDATE athlete_identity
        SET name = :name, sex = :sex, birthDateIso = :birthDateIso, heightCm = :heightCm
        WHERE id = :id
        """
    )
    suspend fun updateProfile(
        name: String?,
        sex: String?,
        birthDateIso: String?,
        heightCm: Double?,
        id: Int = AthleteIdentityEntity.SINGLETON_ID
    )

    /**
     * Records the athlete's profile, creating the identity row if the scale was never used.
     *
     * The profile can be filled in before any pairing, so it cannot wait for the HUID.
     */
    @Transaction
    suspend fun saveProfile(
        name: String?,
        sex: String?,
        birthDateIso: String?,
        heightCm: Double?,
        nowEpochMs: Long
    ) {
        huidOrCreate(nowEpochMs)
        updateProfile(name, sex, birthDateIso, heightCm)
    }

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

    /**
     * Adopts the HUID received from the peer device; the first one wins.
     *
     * ⚠️ Replaces the whole row, so the profile already entered here is carried over: it
     * belongs to the athlete, not to the identity the peer sent.
     */
    @Transaction
    suspend fun adopt(huid: String, nowEpochMs: Long) {
        val existing = find()
        upsert(
            AthleteIdentityEntity(
                huid = huid,
                createdAtEpochMs = nowEpochMs,
                syncState = "SYNCED_PEER",
                name = existing?.name,
                sex = existing?.sex,
                birthDateIso = existing?.birthDateIso,
                heightCm = existing?.heightCm
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
