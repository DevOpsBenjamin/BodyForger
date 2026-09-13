package app.bodyforger.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import app.bodyforger.core.database.entity.RoutineEntity
import app.bodyforger.core.database.entity.RoutineExerciseEntity
import app.bodyforger.core.database.entity.RoutineSetEntity
import app.bodyforger.core.database.entity.RoutineWithExercises
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface RoutineDao {

    @Transaction
    @Query("SELECT * FROM routines ORDER BY createdAtEpochMs DESC")
    fun getAllRoutinesWithExercisesFlow(): Flow<List<RoutineWithExercises>>

    @Transaction
    @Query("SELECT * FROM routines ORDER BY createdAtEpochMs DESC")
    suspend fun getAllRoutinesWithExercises(): List<RoutineWithExercises>

    @Transaction
    @Query("SELECT * FROM routines WHERE id = :id LIMIT 1")
    suspend fun getRoutineWithExercisesById(id: String): RoutineWithExercises?

    @Transaction
    @Query("SELECT * FROM routines WHERE id = :id LIMIT 1")
    fun getRoutineWithExercisesByIdFlow(id: String): Flow<RoutineWithExercises?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineExercises(exercises: List<RoutineExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineSets(sets: List<RoutineSetEntity>)

    @Query("DELETE FROM routine_exercises WHERE routineId = :routineId")
    suspend fun deleteExercisesForRoutine(routineId: String)

    @Query("DELETE FROM routine_sets WHERE routineExerciseId IN (SELECT id FROM routine_exercises WHERE routineId = :routineId)")
    suspend fun deleteSetsForRoutine(routineId: String)

    @Query("DELETE FROM routines WHERE id = :id")
    suspend fun deleteRoutineById(id: String)

    @Query("UPDATE routines SET assignedDaysCsv = :daysCsv WHERE id = :routineId")
    suspend fun updateAssignedDays(routineId: String, daysCsv: String)

    @Transaction
    suspend fun saveFullRoutine(
        routine: RoutineEntity,
        exercises: List<RoutineExerciseEntity>,
        sets: List<RoutineSetEntity>
    ) {
        insertRoutine(routine)
        deleteSetsForRoutine(routine.id)
        deleteExercisesForRoutine(routine.id)
        insertRoutineExercises(exercises)
        insertRoutineSets(sets)
    }

    @Transaction
    suspend fun duplicateRoutine(routineId: String, newName: String? = null): String? {
        val existing = getRoutineWithExercisesById(routineId) ?: return null
        val newRoutineId = UUID.randomUUID().toString()
        val name = newName ?: "${existing.routine.name} (Copie)"

        val duplicatedRoutine = existing.routine.copy(
            id = newRoutineId,
            name = name,
            createdAtEpochMs = System.currentTimeMillis()
        )

        val newExercises = mutableListOf<RoutineExerciseEntity>()
        val newSets = mutableListOf<RoutineSetEntity>()

        for (exWithSets in existing.exercises) {
            val newExerciseId = UUID.randomUUID().toString()
            newExercises.add(
                exWithSets.exercise.copy(
                    id = newExerciseId,
                    routineId = newRoutineId
                )
            )

            for (s in exWithSets.sets) {
                newSets.add(
                    s.copy(
                        id = UUID.randomUUID().toString(),
                        routineExerciseId = newExerciseId
                    )
                )
            }
        }

        saveFullRoutine(duplicatedRoutine, newExercises, newSets)
        return newRoutineId
    }
}
