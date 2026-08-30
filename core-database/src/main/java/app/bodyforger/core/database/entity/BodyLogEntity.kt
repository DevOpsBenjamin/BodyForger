package app.bodyforger.core.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import app.bodyforger.core.model.BodyLog
import app.bodyforger.core.model.ImpedancePath
import app.bodyforger.core.model.ImpedanceReading
import app.bodyforger.core.model.RawImpedances

/**
 * Un relevé corporel tel qu'il est conservé.
 *
 * Le taux de masse grasse est **celui que la balance a envoyé**, ou celui que l'athlète a
 * saisi : c'est une donnée reçue, pas un calcul. Le nôtre n'est jamais persisté — il se
 * recalcule à la demande depuis les résistances, de sorte que tout l'historique se mette à
 * jour le jour où les équations s'améliorent (#20).
 */
@Entity(tableName = "body_logs")
data class BodyLogEntity(
    @PrimaryKey
    val id: String,
    val dateIso: String,
    val measuredAtEpochMs: Long,
    val massKg: Double,
    val bodyFatPercentage: Double,
    val restingHeartRateBpm: Int?,
    /** Adresse de la balance qui a produit ce relevé, ou `null` pour une saisie manuelle. */
    val sourceDeviceAddress: String? = null
)

/**
 * Une résistance mesurée, une ligne.
 *
 * Une **table fille plutôt qu'une colonne JSON**, pour deux raisons. La première est que
 * l'agrégation d'une période se fait sur la **médiane des résistances** et non sur une
 * moyenne de résultats : c'est un calcul que SQL sait mener sur des lignes, pas sur du texte
 * désérialisé en mémoire. La seconde est qu'une grandeur non mesurée reste ici une **ligne
 * absente**, jamais un zéro encodé — le zéro est la façon dont la balance dit « je n'ai pas
 * mesuré » (#24).
 */
@Entity(
    tableName = "body_log_impedances",
    foreignKeys = [
        ForeignKey(
            entity = BodyLogEntity::class,
            parentColumns = ["id"],
            childColumns = ["bodyLogId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("bodyLogId"), Index(value = ["bodyLogId", "path", "frequencyKHz"], unique = true)]
)
data class BodyLogImpedanceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bodyLogId: String,
    /** Nom du trajet anatomique, jamais son rang dans la trame — celui-ci a déjà changé. */
    val path: String,
    val frequencyKHz: Int,
    val ohms: Double
)

/** Un relevé et ses résistances, tels qu'ils se lisent ensemble. */
data class BodyLogWithImpedances(
    @Embedded val log: BodyLogEntity,
    @Relation(parentColumn = "id", entityColumn = "bodyLogId")
    val impedances: List<BodyLogImpedanceEntity>
)

fun BodyLogWithImpedances.toDomain(): BodyLog = BodyLog(
    id = log.id,
    dateIso = log.dateIso,
    measuredAtEpochMs = log.measuredAtEpochMs,
    massKg = log.massKg,
    bodyFatPercentage = log.bodyFatPercentage,
    rawImpedances = RawImpedances.of(
        impedances.mapNotNull { row ->
            val path = runCatching { ImpedancePath.valueOf(row.path) }.getOrNull() ?: return@mapNotNull null
            ImpedanceReading(path, row.frequencyKHz) to row.ohms
        }.toMap()
    ),
    restingHeartRateBpm = log.restingHeartRateBpm
)

fun BodyLog.toEntity(sourceDeviceAddress: String? = null): BodyLogEntity = BodyLogEntity(
    id = id,
    dateIso = dateIso,
    measuredAtEpochMs = measuredAtEpochMs,
    massKg = massKg,
    bodyFatPercentage = bodyFatPercentage,
    restingHeartRateBpm = restingHeartRateBpm,
    sourceDeviceAddress = sourceDeviceAddress
)

fun BodyLog.impedanceRows(): List<BodyLogImpedanceEntity> =
    rawImpedances.ohmsByReading.map { (reading, ohms) ->
        BodyLogImpedanceEntity(
            bodyLogId = id,
            path = reading.path.name,
            frequencyKHz = reading.frequencyKHz,
            ohms = ohms
        )
    }
