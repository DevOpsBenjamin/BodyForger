package app.bodyforger.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import app.bodyforger.core.database.dao.BodyLogDao
import app.bodyforger.core.database.dao.WorkoutDao
import app.bodyforger.core.database.entity.BodyLogEntity
import app.bodyforger.core.database.entity.WorkoutSessionEntity

@Database(
    entities = [
        WorkoutSessionEntity::class,
        BodyLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BodyForgerDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun bodyLogDao(): BodyLogDao
}
