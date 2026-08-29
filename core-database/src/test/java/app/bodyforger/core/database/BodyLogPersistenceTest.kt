package app.bodyforger.core.database

import app.bodyforger.core.database.entity.BodyLogImpedanceEntity
import app.bodyforger.core.database.entity.BodyLogWithImpedances
import app.bodyforger.core.database.entity.impedanceRows
import app.bodyforger.core.database.entity.toDomain
import app.bodyforger.core.database.entity.toEntity
import app.bodyforger.core.model.BodyLog
import app.bodyforger.core.model.ImpedancePath
import app.bodyforger.core.model.ImpedanceReading
import app.bodyforger.core.model.RawImpedances
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * la correspondance qui se trompe, pas SQLite.
 */
class BodyLogPersistenceTest {

    private val impedances = RawImpedances.of(
        ImpedancePath.entries.flatMap { path ->
            listOf(
                ImpedanceReading(path, 50) to 300.0 + path.ordinal,
                ImpedanceReading(path, 250) to 270.0 + path.ordinal
            )
        }.toMap()
    )

    private val log = BodyLog(
        id = "log-1",
        dateIso = "2025-03-09",
        measuredAtEpochMs = 1_741_500_000_000,
        massKg = 80.0,
        bodyFatPercentage = 18.5,
        rawImpedances = impedances,
        restingHeartRateBpm = 58
    )

    @Test
    fun `un releve fait l'aller-retour sans rien perdre`() {
        val restored = BodyLogWithImpedances(log.toEntity(), log.impedanceRows()).toDomain()

        assertEquals(log.massKg, restored.massKg, 1e-9)
        assertEquals(log.bodyFatPercentage, restored.bodyFatPercentage, 1e-9)
        assertEquals(log.restingHeartRateBpm, restored.restingHeartRateBpm)
        assertEquals(log.rawImpedances.ohmsByReading, restored.rawImpedances.ohmsByReading)
    }

    @Test
    fun `chaque resistance est une ligne, jamais un texte`() {
        assertEquals(12, log.impedanceRows().size)
        assertTrue(log.impedanceRows().all { it.bodyLogId == log.id })
    }

    @Test
    fun `une grandeur non mesuree est une ligne absente, pas un zero`() {
        val partial = log.copy(
            rawImpedances = RawImpedances.of(
                mapOf(ImpedanceReading(ImpedancePath.LEFT_FOOT_TO_RIGHT_FOOT, 50) to 415.0)
            )
        )
        assertEquals(1, partial.impedanceRows().size)

        val restored = BodyLogWithImpedances(partial.toEntity(), partial.impedanceRows()).toDomain()
        assertEquals(1, restored.rawImpedances.ohmsByReading.size)
        assertEquals(null, restored.rawImpedances[ImpedancePath.LEFT_HAND_TO_RIGHT_HAND, 50])
    }

    @Test
    fun `une pesee sans impedance reste un releve valide`() {
        val weightOnly = log.copy(rawImpedances = RawImpedances.NONE)
        assertTrue(weightOnly.impedanceRows().isEmpty())

        val restored = BodyLogWithImpedances(weightOnly.toEntity(), emptyList()).toDomain()
        assertEquals(80.0, restored.massKg, 1e-9)
        assertTrue(restored.rawImpedances.isEmpty)
    }

    @Test
    fun `le trajet est stocke par son nom, jamais par son rang`() {
        // historique sur une convention qui bouge.
        val rows = log.impedanceRows()
        assertTrue(rows.all { row -> ImpedancePath.entries.any { it.name == row.path } })
    }

    @Test
    fun `un trajet devenu inconnu est ignore, pas devine`() {
        val rows = log.impedanceRows() + BodyLogImpedanceEntity(
            bodyLogId = log.id,
            path = "TRAJET_D_UNE_VERSION_FUTURE",
            frequencyKHz = 50,
            ohms = 123.0
        )
        val restored = BodyLogWithImpedances(log.toEntity(), rows).toDomain()
        assertEquals(12, restored.rawImpedances.ohmsByReading.size)
    }

    @Test
    fun `la balance qui a produit le releve est conservee`() {
        val entity = log.toEntity(sourceDeviceAddress = "AA:BB:CC:DD:EE:FF")
        assertEquals("AA:BB:CC:DD:EE:FF", entity.sourceDeviceAddress)
        // Une saisie manuelle n'a pas de source, et c'est une information en soi.
        assertEquals(null, log.toEntity().sourceDeviceAddress)
    }
}
