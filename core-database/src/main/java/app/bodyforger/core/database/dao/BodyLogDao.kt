package app.bodyforger.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.bodyforger.core.database.entity.BodyLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyLogDao {
    @Query("SELECT * FROM body_logs ORDER BY measuredAtEpochMs DESC")
    fun getAllLogs(): Flow<List<BodyLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: BodyLogEntity)
}
