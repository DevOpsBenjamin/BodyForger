package app.bodyforger.core.database

import android.content.Context
import androidx.room.Room

/**
 * The single place the database is opened.
 *
 * ⚠️ One instance per process. Opening Room twice on the same file raises nothing: each holds
 * its own cache, and writes from one stay invisible to the other, so data seems to vanish
 * intermittently.
 *
 * ⚠️ Destructive migration is accepted until the app ships; it must be removed before then —
 * `docs/DATABASE_MIGRATIONS.md` and #43. Each version's schema is already exported under
 * `core-database/schemas/`, so the migrations can be written from a known state.
 */
object BodyForgerDatabases {

    const val FILE_NAME = "bodyforger.db"

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
            .fallbackToDestructiveMigration()
            .build()
        return database
    }
}
