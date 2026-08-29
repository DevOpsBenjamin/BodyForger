package app.bodyforger.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import app.bodyforger.core.database.dao.AthleteIdentityDao
import app.bodyforger.core.database.dao.BodyLogDao
import app.bodyforger.core.database.dao.ScaleAssociationDao
import app.bodyforger.core.database.dao.ExerciseDao
import app.bodyforger.core.database.dao.RoutineDao
import app.bodyforger.core.database.dao.WorkoutDao
import app.bodyforger.core.database.data.DefaultExercises
import app.bodyforger.core.database.entity.AthleteIdentityEntity
import app.bodyforger.core.database.entity.BodyLogEntity
import app.bodyforger.core.database.entity.BodyLogImpedanceEntity
import app.bodyforger.core.database.entity.ExerciseEntity
import app.bodyforger.core.database.entity.RoutineEntity
import app.bodyforger.core.database.entity.ScaleAssociationEntity
import app.bodyforger.core.database.entity.RoutineExerciseEntity
import app.bodyforger.core.database.entity.RoutineSetEntity
import app.bodyforger.core.database.entity.WorkoutSessionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ExerciseEntity::class,
        WorkoutSessionEntity::class,
        AthleteIdentityEntity::class,
        BodyLogEntity::class,
        BodyLogImpedanceEntity::class,
        ScaleAssociationEntity::class,
        RoutineEntity::class,
        RoutineExerciseEntity::class,
        RoutineSetEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class BodyForgerDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun athleteIdentityDao(): AthleteIdentityDao
    abstract fun bodyLogDao(): BodyLogDao
    abstract fun scaleAssociationDao(): ScaleAssociationDao
    abstract fun routineDao(): RoutineDao

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
