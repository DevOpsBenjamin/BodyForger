package app.bodyforger.mobile.onboarding

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The flow is a dependency chain, not a list: reordering it would ask for a height before the
 * unit it is written in, or offer to pair a scale that has no profile to be engraved with.
 */
class SetupFlowTest {

    @Test
    fun `units are asked first, since every later answer is written in them`() {
        assertEquals(SetupStep.UNITS, SetupStep.entries.first())
    }

    @Test
    fun `the profile comes before the pairing it is engraved into`() {
        assertTrue(SetupStep.PROFILE.ordinal < SetupStep.SCALE.ordinal)
    }

    @Test
    fun `goals come last, having nothing to be read against before a weigh-in`() {
        assertEquals(SetupStep.GOALS, SetupStep.entries.last())
        assertTrue(SetupStep.GOALS.isLast)
    }

    @Test
    fun `previous walks back to the start and stops there`() {
        var step = SetupStep.entries.last()
        var walked = 1

        while (true) {
            step = step.previous() ?: break
            walked++
        }

        assertEquals(SetupStep.entries.size, walked)
        assertTrue(step.isFirst)
        assertNull(SetupStep.entries.first().previous())
    }

    @Test
    fun `next chains every step and stops at the end`() {
        var step = SetupStep.entries.first()
        var walked = 1

        while (true) {
            step = step.next() ?: break
            walked++
        }

        assertEquals(SetupStep.entries.size, walked)
        assertTrue(step.isLast)
    }
}
