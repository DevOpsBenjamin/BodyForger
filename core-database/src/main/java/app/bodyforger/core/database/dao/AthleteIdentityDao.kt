package app.bodyforger.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import app.bodyforger.core.database.entity.AthleteIdentityEntity
import java.security.SecureRandom

@Dao
interface AthleteIdentityDao {

    @Query("SELECT * FROM athlete_identity WHERE id = :id LIMIT 1")
    suspend fun find(id: Int = AthleteIdentityEntity.SINGLETON_ID): AthleteIdentityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(identity: AthleteIdentityEntity)

    /**
     * Rend le HUID de l'athlète, en le créant s'il n'en existe aucun.
     *
     * ⚠️ **Ne régénère jamais un HUID existant.** Chaque nouveau HUID consomme un emplacement
     * dans la mémoire flash de la balance, définitivement : en produire un second scinderait
     * l'historique en deux personnes du point de vue du matériel (#19).
     *
     * Ce point d'entrée est aussi celui de la reprise après un appairage interrompu : le
     * HUID ayant survécu, rejouer l'appairage réécrit sur le **même** emplacement.
     */
    @Transaction
    suspend fun huidOrCreate(nowEpochMs: Long): String {
        find()?.let { return it.huid }
        val generated = generateHuid()
        upsert(AthleteIdentityEntity(huid = generated, createdAtEpochMs = nowEpochMs))
        return generated
    }

    /**
     * Adopte le HUID reçu de l'autre appareil.
     *
     * Appelé quand la montre reçoit celui du téléphone, ou l'inverse : le premier arrivé fait
     * foi, et l'appareil qui adopte abandonne le sien s'il en avait déjà généré un.
     */
    @Transaction
    suspend fun adopt(huid: String, nowEpochMs: Long) {
        upsert(
            AthleteIdentityEntity(
                huid = huid,
                createdAtEpochMs = nowEpochMs,
                syncState = "SYNCED_PEER"
            )
        )
    }

    private fun generateHuid(): String {
        val random = SecureRandom()
        // Chiffres uniquement : la balance attend une chaîne ASCII numérique.
        return buildString(AthleteIdentityEntity.HUID_DIGITS) {
            repeat(AthleteIdentityEntity.HUID_DIGITS) { append(random.nextInt(10)) }
        }
    }
}
