package app.bodyforger.core.database

import app.bodyforger.core.database.entity.AppSettingsEntity
import app.bodyforger.core.model.WeightUnit
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The settings row holds several independent preferences, and the write path replaces the row
 * wholesale. These hold the edit contract without opening a database.
 */
class AppSettingsTest {

    @Test
    fun `editing one preference leaves the others alone`() {
        val stored = AppSettingsEntity(biaEngineId = "forgefit_private", defaultWeightUnit = "LBS")

        val afterEngineChange = stored.copy(biaEngineId = "forgefit_mit")

        assertEquals("LBS", afterEngineChange.defaultWeightUnit)
        assertEquals("forgefit_mit", afterEngineChange.biaEngineId)
    }

    @Test
    fun `a freshly built row would blank every preference — which is why edit reads first`() {
        val stored = AppSettingsEntity(biaEngineId = "forgefit_private", defaultWeightUnit = "LBS")

        // What `upsert(AppSettingsEntity(biaEngineId = …))` used to do.
        val naive = AppSettingsEntity(biaEngineId = "forgefit_mit")

        assertEquals(null, naive.defaultWeightUnit)
        assertEquals("LBS", stored.defaultWeightUnit)
    }

    @Test
    fun `no choice made yet means kilograms`() {
        assertEquals(null, AppSettingsEntity().defaultWeightUnit)
        assertEquals(WeightUnit.KG, WeightUnit.entries.first())
    }

    @Test
    fun `every unit round-trips through its stored name`() {
        WeightUnit.entries.forEach { unit ->
            assertEquals(unit, WeightUnit.valueOf(AppSettingsEntity(defaultWeightUnit = unit.name).defaultWeightUnit!!))
        }
    }

    @Test
    fun `the two onboarding counters answer separately`() {
        val stored = AppSettingsEntity(tourVersionSeen = 1, setupVersionDone = 1)

        // A tour that gains a stop is offered again; the setup flow is not.
        val afterTourRaise = stored.copy(tourVersionSeen = 0)

        assertEquals(0, afterTourRaise.tourVersionSeen)
        assertEquals(1, afterTourRaise.setupVersionDone)
    }

    @Test
    fun `never having seen anything reads as zero, not as done`() {
        assertEquals(0, AppSettingsEntity().tourVersionSeen)
        assertEquals(0, AppSettingsEntity().setupVersionDone)
    }

    @Test
    fun `health connect prompt is not dismissed by default`() {
        assertEquals(false, AppSettingsEntity().healthConnectPromptDismissed)
    }
}
