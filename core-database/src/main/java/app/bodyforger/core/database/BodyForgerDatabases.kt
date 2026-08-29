package app.bodyforger.core.database

import android.content.Context
import androidx.room.Room

/**
 * L'unique point d'ouverture de la base.
 *
 * ⚠️ **Une seule instance doit exister par processus.** Ouvrir Room deux fois sur le même
 * fichier ne lève aucune erreur : les deux instances tiennent chacune leur cache, et les
 * écritures de l'une restent invisibles à l'autre jusqu'à relecture. Les données semblent
 * alors se perdre par intermittence, ce qui est bien plus coûteux à diagnostiquer qu'un
 * plantage franc.
 *
 * ⚠️ **Migration destructive assumée.** Tant que l'application n'est pas distribuée, une
 * évolution de schéma efface la base plutôt que d'exiger une migration écrite à la main. Ce
 * choix devra être retiré avant la première mise en circulation, faute de quoi une mise à
 * jour effacerait l'historique de l'athlète.
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
