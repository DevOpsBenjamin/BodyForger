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
 * A reading's round trip through the entities, without opening a database.
 *
 * What these hold is the mapping, which is where the mistakes are: SQLite is not the part
 * that gets a column wrong.
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
    fun `a reading round-trips without losing anything`() {
        val restored = BodyLogWithImpedances(log.toEntity(), log.impedanceRows()).toDomain()

        assertEquals(log.massKg, restored.massKg, 1e-9)
        assertEquals(log.bodyFatPercentage, restored.bodyFatPercentage, 1e-9)
        assertEquals(log.restingHeartRateBpm, restored.restingHeartRateBpm)
        assertEquals(log.rawImpedances.ohmsByReading, restored.rawImpedances.ohmsByReading)
    }

    @Test
    fun `every resistance is a row, never a blob of text`() {
        assertEquals(12, log.impedanceRows().size)
        assertTrue(log.impedanceRows().all { it.bodyLogId == log.id })
    }

    @Test
    fun `an unmeasured quantity is a missing row, not a zero`() {
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
    fun `a weigh-in without impedance is still a valid reading`() {
        val weightOnly = log.copy(rawImpedances = RawImpedances.NONE)
        assertTrue(weightOnly.impedanceRows().isEmpty())

        val restored = BodyLogWithImpedances(weightOnly.toEntity(), emptyList()).toDomain()
        assertEquals(80.0, restored.massKg, 1e-9)
        assertTrue(restored.rawImpedances.isEmpty)
    }

    @Test
    fun `a path is stored by its name, never by its rank`() {
        // Stored by name, never by ordinal: reordering the enum would otherwise rewrite the
        // history against a convention that moved.
        val rows = log.impedanceRows()
        assertTrue(rows.all { row -> ImpedancePath.entries.any { it.name == row.path } })
    }

    @Test
    fun `a path that became unknown is ignored, not guessed`() {
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
    fun `the scale that produced the reading is kept`() {
        val entity = log.toEntity(sourceDeviceAddress = "AA:BB:CC:DD:EE:FF")
        assertEquals("AA:BB:CC:DD:EE:FF", entity.sourceDeviceAddress)
        // A manual entry has no source device, and that absence is itself information.
        assertEquals(null, log.toEntity().sourceDeviceAddress)
    }
}
