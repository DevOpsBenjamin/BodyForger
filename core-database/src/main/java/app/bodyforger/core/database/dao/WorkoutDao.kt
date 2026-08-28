package app.bodyforger.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import app.bodyforger.core.database.entity.WorkoutSessionEntity
import app.bodyforger.core.database.entity.WorkoutSessionWithSets
import app.bodyforger.core.database.entity.WorkoutSetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    @Transaction
    @Query("SELECT * FROM workout_sessions ORDER BY startedAtEpochMs DESC")
    fun getAllSessionsWithSets(): Flow<List<WorkoutSessionWithSets>>

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE status = 'COMPLETED' ORDER BY startedAtEpochMs DESC")
    fun getCompletedSessions(): Flow<List<WorkoutSessionWithSets>>

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    suspend fun getSessionWithSets(sessionId: String): WorkoutSessionWithSets?

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE status = 'ACTIVE' ORDER BY startedAtEpochMs DESC LIMIT 1")
    suspend fun getActiveSession(): WorkoutSessionWithSets?

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE status = 'ACTIVE' ORDER BY startedAtEpochMs DESC LIMIT 1")
    fun observeActiveSession(): Flow<WorkoutSessionWithSets?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSessionEntity)

    @Update
    suspend fun updateSession(session: WorkoutSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: WorkoutSetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSets(sets: List<WorkoutSetEntity>)

    @Update
    suspend fun updateSet(set: WorkoutSetEntity)

    @Query("DELETE FROM workout_sets WHERE id = :setId")
    suspend fun deleteSet(setId: String)

    @Query("DELETE FROM workout_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: String)

    @Query("SELECT * FROM workout_sets WHERE id = :setId")
    suspend fun getSetById(setId: String): WorkoutSetEntity?

    @Query("SELECT * FROM workout_sets WHERE sessionId = :sessionId")
    suspend fun getSetsForSession(sessionId: String): List<WorkoutSetEntity>

    /**
     * Validation atomique immédiate d'une série avec recalcul du tonnage total de la séance
     * pour assurer une résilience absolue aux crashs et à l'extinction de batterie.
     */
    @Transaction
    suspend fun atomicCompleteSet(
        setId: String,
        isCompleted: Boolean,
        completedAtEpochMs: Long?,
        weightKg: Double,
        reps: Int,
        rpe: Double? = null
    ) {
        val currentSet = getSetById(setId) ?: return
        val updatedSet = currentSet.copy(
            isCompleted = isCompleted,
            completedAtEpochMs = completedAtEpochMs,
            weightKg = weightKg,
            reps = reps,
            rpe = rpe
        )
        updateSet(updatedSet)

        // Recalcul du tonnage cumulé de la séance
        val allSets = getSetsForSession(currentSet.sessionId)
        val totalVolume = allSets.filter { it.isCompleted }.sumOf { it.weightKg * it.reps }
        
        val session = getSessionWithSets(currentSet.sessionId)?.session
        if (session != null) {
            updateSession(session.copy(totalVolumeKg = totalVolume))
        }
    }
}
