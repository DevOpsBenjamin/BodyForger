package app.bodyforger.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * The single place the database is opened.
 *
 * ⚠️ One instance per process. Opening Room twice on the same file raises nothing: each holds
 * its own cache, and writes from one stay invisible to the other, so data seems to vanish
 * intermittently.
 *
 * ⚠️ Destructive migration is accepted until the app ships; it must be removed before then —
 * `docs/DATABASE_MIGRATIONS.md`. Each version's schema is already exported under
 * `core-database/schemas/`, so the migrations can be written from a known state.
 */
object BodyForgerDatabases {

    const val FILE_NAME = "bodyforger.db"

    val MIGRATION_12_13 = object : Migration(12, 13) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `workout_heart_rate_samples` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `sessionId` TEXT NOT NULL,
                    `timestampEpochMs` INTEGER NOT NULL,
                    `bpm` INTEGER NOT NULL,
                    FOREIGN KEY(`sessionId`) REFERENCES `workout_sessions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_heart_rate_samples_sessionId` ON `workout_heart_rate_samples` (`sessionId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_heart_rate_samples_timestampEpochMs` ON `workout_heart_rate_samples` (`timestampEpochMs`)")
        }
    }

    val MIGRATION_13_14 = object : Migration(13, 14) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `app_settings` ADD COLUMN `healthConnectPromptDismissed` INTEGER NOT NULL DEFAULT 0")
        }
    }

    @Volatile
    private var instance: BodyForgerDatabase? = null

    fun get(context: Context): BodyForgerDatabase =
        instance ?: synchronized(this) {
            instance ?: build(context.applicationContext).also { instance = it }
        }

    private fun build(context: Context): BodyForgerDatabase {
        lateinit var database: BodyForgerDatabase
        database = Room.databaseBuilder(context, BodyForgerDatabase::class.java, FILE_NAME)
            .addCallback(BodyForgerDatabase.createPrepopulateCallback { database })
            .addMigrations(MIGRATION_12_13, MIGRATION_13_14)
            .fallbackToDestructiveMigration()
            .build()
        return database
    }
}
