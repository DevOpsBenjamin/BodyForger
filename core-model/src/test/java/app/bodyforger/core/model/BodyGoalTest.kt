package app.bodyforger.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * A goal decides what counts as reached. Getting the direction wrong here would validate a
 * milestone the athlete never hit, or refuse one they did.
 */
class BodyGoalTest {

    private val today: LocalDate = LocalDate.of(2026, 9, 13)

    private fun goal(
        target: Double,
        from: Double,
        targetFat: Double? = null,
        fromFat: Double? = null
    ) = BodyGoal(
        targetMassKg = target,
        targetBodyFatPercentage = targetFat,
        startingMassKg = from,
        startingBodyFatPercentage = fromFat,
        createdOn = today
    )

    @Test
    fun `a target below the starting point is reached by going down`() {
        val losing = goal(target = 76.0, from = 80.0)

        assertTrue(losing.isCrossedBy(76.0, null))
        assertTrue(losing.isCrossedBy(74.5, null))
        assertFalse(losing.isCrossedBy(77.0, null))
    }

    @Test
    fun `a target above the starting point is reached by going up`() {
        val gaining = goal(target = 84.0, from = 80.0)

        assertTrue(gaining.isCrossedBy(84.0, null))
        assertTrue(gaining.isCrossedBy(85.5, null))
        assertFalse(gaining.isCrossedBy(83.0, null))
    }

    @Test
    fun `passing the target does not un-reach it`() {
        // Direction read from the current trend would flip here and stop counting the goal as
        // crossed at the exact moment it was.
        val losing = goal(target = 76.0, from = 80.0)

        assertTrue(losing.isCrossedBy(75.0, null))
        assertTrue(losing.isCrossedBy(70.0, null))
    }

    @Test
    fun `mass and body fat are judged independently`() {
        // Gaining mass while losing fat is an ordinary goal, not a contradiction.
        val recomposition = goal(target = 84.0, from = 80.0, targetFat = 14.0, fromFat = 18.0)

        assertTrue(recomposition.isCrossedBy(84.5, 13.5))
        assertFalse("mass reached, fat not", recomposition.isCrossedBy(84.5, 16.0))
        assertFalse("fat reached, mass not", recomposition.isCrossedBy(82.0, 13.5))
    }

    @Test
    fun `a goal naming a fat threshold is not crossed by a week that never measured it`() {
        val withFat = goal(target = 76.0, from = 80.0, targetFat = 15.0, fromFat = 20.0)

        assertFalse(withFat.isCrossedBy(74.0, null))
        assertTrue(withFat.requiresBodyFat)
    }

    @Test
    fun `a goal without a fat threshold accepts a mass-only week`() {
        val massOnly = goal(target = 76.0, from = 80.0)

        assertTrue(massOnly.isCrossedBy(75.0, null))
        assertFalse(massOnly.requiresBodyFat)
    }

    @Test
    fun `progress runs from the starting point to the threshold`() {
        val losing = goal(target = 76.0, from = 80.0)

        assertEquals(0.0, losing.massProgressFrom(80.0)!!, 1e-9)
        assertEquals(0.5, losing.massProgressFrom(78.0)!!, 1e-9)
        assertEquals(1.0, losing.massProgressFrom(76.0)!!, 1e-9)
    }

    @Test
    fun `overshooting reads past one rather than being capped`() {
        val losing = goal(target = 76.0, from = 80.0)

        assertEquals(1.5, losing.massProgressFrom(74.0)!!, 1e-9)
    }

    @Test
    fun `a goal set at the point already stood on reports no progress`() {
        assertNull(goal(target = 80.0, from = 80.0).massProgressFrom(80.0))
    }

    @Test
    fun `a fresh goal is not validated`() {
        assertFalse(goal(target = 76.0, from = 80.0).isValidated)
        assertTrue(goal(target = 76.0, from = 80.0).copy(validatedOn = today).isValidated)
    }

    @Test
    fun `a goal set before any weigh-in waits rather than guessing a direction`() {
        val unanchored = BodyGoal(targetMassKg = 76.0, createdOn = today)

        assertFalse(unanchored.hasStarted)
        assertFalse("no direction, so nothing can be crossed", unanchored.isCrossedBy(70.0, null))
        assertNull(unanchored.massProgressFrom(70.0))
    }

    @Test
    fun `the first weigh-in anchors it, and only the first`() {
        val anchored = BodyGoal(targetMassKg = 76.0, createdOn = today)
            .startingFrom(massKg = 80.0, bodyFatPercentage = 19.0)

        assertTrue(anchored.hasStarted)
        assertEquals(80.0, anchored.startingMassKg!!, 1e-9)
        assertTrue(anchored.isCrossedBy(75.0, null))

        val later = anchored.startingFrom(massKg = 70.0, bodyFatPercentage = 12.0)
        assertEquals("the starting point is captured once", 80.0, later.startingMassKg!!, 1e-9)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `a body fat threshold outside nought to a hundred is refused`() {
        goal(target = 76.0, from = 80.0, targetFat = 140.0, fromFat = 20.0)
    }
}
