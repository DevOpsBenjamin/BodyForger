package app.bodyforger.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import app.bodyforger.core.database.entity.BodyLogEntity
import app.bodyforger.core.database.entity.BodyLogImpedanceEntity
import app.bodyforger.core.database.entity.BodyLogWithImpedances
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyLogDao {

    @Transaction
    @Query("SELECT * FROM body_logs ORDER BY measuredAtEpochMs DESC")
    fun observeAll(): Flow<List<BodyLogWithImpedances>>

    @Transaction
    @Query("SELECT * FROM body_logs ORDER BY measuredAtEpochMs DESC LIMIT 1")
    suspend fun mostRecent(): BodyLogWithImpedances?

    @Transaction
    @Query("SELECT * FROM body_logs ORDER BY measuredAtEpochMs DESC LIMIT 1")
    fun observeMostRecent(): Flow<BodyLogWithImpedances?>

    @Transaction
    @Query("SELECT * FROM body_logs WHERE dateIso = :dateIso LIMIT 1")
    suspend fun findByDate(dateIso: String): BodyLogWithImpedances?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: BodyLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImpedances(impedances: List<BodyLogImpedanceEntity>)

    @Query("DELETE FROM body_log_impedances WHERE bodyLogId = :bodyLogId")
    suspend fun deleteImpedancesFor(bodyLogId: String)

    /**
     * Stores a reading and its resistances in one transaction.
     *
     * Without it, a crash between the two writes would leave a reading with no impedances,
     * indistinguishable from one that measured none.
     */
    @Transaction
    suspend fun save(log: BodyLogEntity, impedances: List<BodyLogImpedanceEntity>) {
        insertLog(log)
        deleteImpedancesFor(log.id)
        if (impedances.isNotEmpty()) insertImpedances(impedances)
    }

    /** Resistances of one path and frequency over a period, for a median. */
    @Query(
        """
        SELECT i.ohms FROM body_log_impedances i
        INNER JOIN body_logs l ON l.id = i.bodyLogId
        WHERE i.path = :path AND i.frequencyKHz = :frequencyKHz
          AND l.measuredAtEpochMs BETWEEN :fromEpochMs AND :toEpochMs
        ORDER BY i.ohms
        """
    )
    suspend fun ohmsOverPeriod(
        path: String,
        frequencyKHz: Int,
        fromEpochMs: Long,
        toEpochMs: Long
    ): List<Double>
}
