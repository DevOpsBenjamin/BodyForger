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
    @Query("SELECT * FROM body_logs WHERE dateIso = :dateIso LIMIT 1")
    suspend fun findByDate(dateIso: String): BodyLogWithImpedances?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: BodyLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImpedances(impedances: List<BodyLogImpedanceEntity>)

    @Query("DELETE FROM body_log_impedances WHERE bodyLogId = :bodyLogId")
    suspend fun deleteImpedancesFor(bodyLogId: String)

    /**
     * Enregistre un relevé et ses résistances **d'un seul tenant**.
     *
     * Sans transaction, un arrêt entre les deux écritures laisserait un relevé sans ses
     * impédances — indiscernable d'une pesée qui n'en aurait pas mesuré, alors qu'elles ont
     * bien été relevées. Les anciennes lignes sont effacées d'abord : réenregistrer un même
     * relevé le remplace au lieu d'accumuler des résistances en double.
     */
    @Transaction
    suspend fun save(log: BodyLogEntity, impedances: List<BodyLogImpedanceEntity>) {
        insertLog(log)
        deleteImpedancesFor(log.id)
        if (impedances.isNotEmpty()) insertImpedances(impedances)
    }

    /**
     * Les résistances d'un trajet et d'une fréquence sur une période, pour en tirer une
     * médiane.
     *
     * C'est la requête que la table fille rend possible : agréger une période sur la médiane
     * des **résistances** plutôt que sur une moyenne de résultats (`CONTEXT.md`).
     */
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
