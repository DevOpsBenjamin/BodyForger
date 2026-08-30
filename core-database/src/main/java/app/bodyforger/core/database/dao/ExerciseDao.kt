package app.bodyforger.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import app.bodyforger.core.database.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    @Query("SELECT * FROM exercises ORDER BY name ASC")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE id = :id LIMIT 1")
    fun getExerciseById(id: String): Flow<ExerciseEntity?>

    @Query("""
        SELECT * FROM exercises 
        WHERE (:query = '' OR name LIKE '%' || :query || '%')
          AND (:muscle IS NULL OR primaryMuscleGroup = :muscle)
          AND (:equipment IS NULL OR equipment = :equipment)
        ORDER BY isCustom DESC, name ASC
    """)
    fun searchExercises(
        query: String = "",
        muscle: String? = null,
        equipment: String? = null
    ): Flow<List<ExerciseEntity>>

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun getExercisesCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(exercises: List<ExerciseEntity>)

    @Update
    suspend fun updateExercise(exercise: ExerciseEntity)

        @Query("DELETE FROM exercises WHERE id = :id AND isCustom = 1")
    suspend fun deleteCustomExercise(id: String): Int
}
