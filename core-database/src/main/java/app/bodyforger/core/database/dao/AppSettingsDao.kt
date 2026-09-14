package app.bodyforger.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import app.bodyforger.core.database.entity.AppSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSettingsDao {

    @Query("SELECT * FROM app_settings WHERE id = :id LIMIT 1")
    fun observe(id: Int = AppSettingsEntity.SINGLETON_ID): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = :id LIMIT 1")
    suspend fun current(id: Int = AppSettingsEntity.SINGLETON_ID): AppSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: AppSettingsEntity)

    /**
     * Writes one preference, leaving the others as they were.
     *
     * The row is replaced wholesale on write, so it has to be read first: passing a freshly
     * built entity would blank every preference the caller did not mention.
     */
    @Transaction
    suspend fun edit(change: (AppSettingsEntity) -> AppSettingsEntity) =
        upsert(change(current() ?: AppSettingsEntity()))

    /** Records the chosen BIA engine; the single row is created if it was never written. */
    suspend fun setBiaEngine(engineId: String?) = edit { it.copy(biaEngineId = engineId) }

    /** Records the weight unit new exercises start from. */
    suspend fun setDefaultWeightUnit(unit: String?) = edit { it.copy(defaultWeightUnit = unit) }

    /** Records the unit a height is written in. Storage stays in centimetres regardless. */
    suspend fun setDefaultHeightUnit(unit: String?) = edit { it.copy(defaultHeightUnit = unit) }

    /** Records that the screen tour was seen through to the end, or skipped, at [version]. */
    suspend fun setTourVersionSeen(version: Int) = edit { it.copy(tourVersionSeen = version) }

    /** Records that the setup flow was gone through, or skipped, at [version]. */
    suspend fun setSetupVersionDone(version: Int) = edit { it.copy(setupVersionDone = version) }

    /** Records that the Google Health Connect prompt was dismissed or resolved. */
    suspend fun setHealthConnectPromptDismissed(dismissed: Boolean) =
        edit { it.copy(healthConnectPromptDismissed = dismissed) }
}
