package app.bodyforger.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class DomainModelTest {

    @Test
    fun testWorkoutSetCreation() {
        val set = WorkoutSet(
            id = "set_1",
            exerciseId = "ex_bench",
            phase = SetPhase.WORK,
            type = SetType.STRAIGHT,
            weightKg = 100.0,
            reps = 8
        )
        assertEquals("set_1", set.id)
        assertEquals(100.0, set.weightKg, 0.001)
        assertEquals(8, set.reps)
    }

    @Test
    fun testBiaProfileCreation() {
        val profile = BiaProfile(
            sex = BiologicalSex.MALE,
            ageYears = 30,
            heightCm = 180.0
        )
        assertEquals(BiologicalSex.MALE, profile.sex)
        assertEquals(180.0, profile.heightCm, 0.001)
    }
}
