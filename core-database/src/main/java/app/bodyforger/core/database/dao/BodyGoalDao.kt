package app.bodyforger.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.bodyforger.core.database.entity.BodyGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyGoalDao {

    /**
     * Every goal, ordered the way the athlete reads them: what comes first, first.
     *
     * Dated goals lead, by horizon. Undated ones follow — there is no honest way to rank a
     * goal with no date against one that has one, so they queue behind, oldest first.
     */
    @Query(
        """
        SELECT * FROM body_goals
        ORDER BY horizonDateIso IS NULL, horizonDateIso ASC, createdOnIso ASC
        """
    )
    fun observeAll(): Flow<List<BodyGoalEntity>>

    @Query("SELECT * FROM body_goals")
    suspend fun all(): List<BodyGoalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(goal: BodyGoalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(goals: List<BodyGoalEntity>)

    @Delete
    suspend fun delete(goal: BodyGoalEntity)

    @Query("SELECT COUNT(*) FROM body_goals WHERE validatedOnIso IS NOT NULL")
    fun observeValidatedCount(): Flow<Int>
}
