package app.bodyforger.wear.tile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class BodyForgerTileServiceTest {

    @Test
    fun testTileConstants() {
        assertEquals("EXTRA_NAV_TARGET", BodyForgerTileService.EXTRA_NAV_TARGET)
        assertEquals("workout", BodyForgerTileService.TARGET_WORKOUT)
        assertEquals("weigh_in", BodyForgerTileService.TARGET_WEIGH_IN)
        assertEquals("1", BodyForgerTileService.RESOURCES_VERSION)
    }

    @Test
    fun testTileServiceInstantiation() {
        val service = BodyForgerTileService()
        assertNotNull(service)
    }
}
