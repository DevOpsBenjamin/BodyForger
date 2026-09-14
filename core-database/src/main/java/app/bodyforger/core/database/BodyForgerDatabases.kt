package app.bodyforger.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import app.bodyforger.core.database.data.DefaultExercises

/**
 * The single place the database is opened.
 *
 * ⚠️ One instance per process. Opening Room twice on the same file raises nothing: each holds
 * its own cache, and writes from one stay invisible to the other, so data seems to vanish
 * intermittently.
 *
 * Each version's schema is exported under `core-database/schemas/`, so migrations are written
 * from a known state and tested with MigrationTestHelper.
 */
object BodyForgerDatabases {

    const val FILE_NAME = "bodyforger.db"

    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `app_settings` (
                    `id` INTEGER NOT NULL,
                    `biaEngineId` TEXT,
                    PRIMARY KEY(`id`)
                )
            """.trimIndent())
        }
    }

    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `exercises` ADD COLUMN `nameKey` TEXT")
            for (exercise in DefaultExercises.all) {
                val key = exercise.nameKey
                if (key != null) {
                    db.execSQL(
                        "UPDATE `exercises` SET `nameKey` = ? WHERE `id` = ?",
                        arrayOf(key, exercise.id)
                    )
                }
            }
        }
    }

    val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `body_logs_new` (
                    `id` TEXT NOT NULL,
                    `dateIso` TEXT NOT NULL,
                    `measuredAtEpochMs` INTEGER NOT NULL,
                    `massKg` REAL NOT NULL,
                    `bodyFatPercentage` REAL,
                    `restingHeartRateBpm` INTEGER,
                    `sourceDeviceAddress` TEXT,
                    PRIMARY KEY(`id`)
                )
            """.trimIndent())
            db.execSQL("""
                INSERT INTO `body_logs_new` (`id`, `dateIso`, `measuredAtEpochMs`, `massKg`, `bodyFatPercentage`, `restingHeartRateBpm`, `sourceDeviceAddress`)
                SELECT `id`, `dateIso`, `measuredAtEpochMs`, `massKg`, `bodyFatPercentage`, `restingHeartRateBpm`, `sourceDeviceAddress` FROM `body_logs`
            """.trimIndent())
            db.execSQL("DROP TABLE `body_logs`")
            db.execSQL("ALTER TABLE `body_logs_new` RENAME TO `body_logs`")
        }
    }

    val MIGRATION_8_9 = object : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `app_settings` ADD COLUMN `defaultWeightUnit` TEXT")
        }
    }

    val MIGRATION_9_10 = object : Migration(9, 10) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `body_goals` (
                    `id` TEXT NOT NULL,
                    `targetMassKg` REAL NOT NULL,
                    `targetBodyFatPercentage` REAL,
                    `startingMassKg` REAL,
                    `startingBodyFatPercentage` REAL,
                    `horizonDateIso` TEXT,
                    `createdOnIso` TEXT NOT NULL,
                    `validatedOnIso` TEXT,
                    PRIMARY KEY(`id`)
                )
            """.trimIndent())
        }
    }

    val MIGRATION_10_11 = object : Migration(10, 11) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `app_settings` ADD COLUMN `defaultHeightUnit` TEXT")
        }
    }

    val MIGRATION_11_12 = object : Migration(11, 12) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `app_settings` ADD COLUMN `tourVersionSeen` INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE `app_settings` ADD COLUMN `setupVersionDone` INTEGER NOT NULL DEFAULT 0")
        }
    }

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

    val MIGRATION_14_15 = object : Migration(14, 15) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `workout_sessions` ADD COLUMN `isHealthConnectExported` INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE `workout_sets` ADD COLUMN `startedAtEpochMs` INTEGER")
            db.execSQL("ALTER TABLE `workout_sets` ADD COLUMN `actualRestSeconds` INTEGER")
        }
    }

    val ALL_MIGRATIONS: Array<Migration> = arrayOf(
        MIGRATION_5_6,
        MIGRATION_6_7,
        MIGRATION_7_8,
        MIGRATION_8_9,
        MIGRATION_9_10,
        MIGRATION_10_11,
        MIGRATION_11_12,
        MIGRATION_12_13,
        MIGRATION_13_14,
        MIGRATION_14_15
    )

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
            .addMigrations(*ALL_MIGRATIONS)
            .build()
        return database
    }
}
