package app.bodyforger.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import app.bodyforger.core.database.dao.BodyLogDao
import app.bodyforger.core.database.dao.ExerciseDao
import app.bodyforger.core.database.dao.WorkoutDao
import app.bodyforger.core.database.data.DefaultExercises
import app.bodyforger.core.database.entity.BodyLogEntity
import app.bodyforger.core.database.entity.ExerciseEntity
import app.bodyforger.core.database.entity.WorkoutSessionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ExerciseEntity::class,
        WorkoutSessionEntity::class,
        BodyLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BodyForgerDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun bodyLogDao(): BodyLogDao

    companion object {
        fun createPrepopulateCallback(
            scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
            provideDatabase: () -> BodyForgerDatabase
        ): Callback {
            return object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    scope.launch {
                        val database = provideDatabase()
                        database.exerciseDao().insertAll(DefaultExercises.all)
                    }
                }
            }
        }
    }
}
