package app.bodyforger.mobile.onboarding

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
    fun `only units are unskippable, and only because they already have an answer`() {
        assertNull(SetupStep.UNITS.skipRes)
        SetupStep.entries.filter { it != SetupStep.UNITS }.forEach { step ->
            assertNotNull("${step.name} must offer a way out", step.skipRes)
        }
    }

    @Test
    fun `the scale is skipped in its own words, not in the generic ones`() {
        // Skipping pairing means "not right now", which is not what skipping a name means.
        assertTrue(SetupStep.SCALE.skipRes != SetupStep.NAME.skipRes)
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
