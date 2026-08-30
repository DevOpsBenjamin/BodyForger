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
     * The sets performed the last time this exercise was trained, before the current session.
     *
     * The whole of that session's sets for the exercise, not the heaviest ever: the athlete
     * wants to know what they lifted last time, in order, to decide what to lift now.
     */
    @Query(
        """
        SELECT * FROM workout_sets
        WHERE exerciseId = :exerciseId
          AND isCompleted = 1
          AND sessionId = (
            SELECT s.id FROM workout_sessions s
            JOIN workout_sets w ON w.sessionId = s.id
            WHERE w.exerciseId = :exerciseId
              AND w.isCompleted = 1
              AND s.status = 'COMPLETED'
              AND s.id != :currentSessionId
            ORDER BY s.startedAtEpochMs DESC
            LIMIT 1
          )
        """
    )
    suspend fun getLastPerformance(exerciseId: String, currentSessionId: String): List<WorkoutSetEntity>

    /**
     * Records one validated set and the session tonnage that follows, in one transaction.
     *
     * Recomputing the tonnage here rather than storing it separately keeps the aggregate and
     * its rows from disagreeing after a partial write.
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

        val allSets = getSetsForSession(currentSet.sessionId)
        val totalVolume = allSets.filter { it.isCompleted }.sumOf { it.weightKg * it.reps }
        
        val session = getSessionWithSets(currentSet.sessionId)?.session
        if (session != null) {
            updateSession(session.copy(totalVolumeKg = totalVolume))
        }
    }
}
