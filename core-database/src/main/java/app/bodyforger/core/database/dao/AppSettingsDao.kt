package app.bodyforger.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.bodyforger.core.database.entity.AppSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSettingsDao {

    @Query("SELECT * FROM app_settings WHERE id = :id LIMIT 1")
    fun observe(id: Int = AppSettingsEntity.SINGLETON_ID): Flow<AppSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: AppSettingsEntity)

    /** Records the chosen BIA engine; the single row is created if it was never written. */
    suspend fun setBiaEngine(engineId: String?) = upsert(AppSettingsEntity(biaEngineId = engineId))
}
