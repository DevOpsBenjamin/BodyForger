package app.bodyforger.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.bodyforger.core.database.entity.ScaleAssociationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScaleAssociationDao {

    /** Associated scales, most recently paired first. */
    @Query("SELECT * FROM scale_associations ORDER BY associatedAtEpochMs DESC")
    fun observeAll(): Flow<List<ScaleAssociationEntity>>

    @Query("SELECT * FROM scale_associations ORDER BY associatedAtEpochMs DESC LIMIT 1")
    suspend fun mostRecent(): ScaleAssociationEntity?

    @Query("SELECT * FROM scale_associations WHERE deviceAddress = :address LIMIT 1")
    suspend fun findByAddress(address: String): ScaleAssociationEntity?

    /** Idempotent by address: re-pairing replaces rather than duplicating. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(association: ScaleAssociationEntity)

    @Query("DELETE FROM scale_associations WHERE deviceAddress = :address")
    suspend fun forget(address: String)
}
