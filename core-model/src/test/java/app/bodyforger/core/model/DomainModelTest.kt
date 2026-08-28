package app.bodyforger.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test

class DomainModelTest {

    @Test
    fun testWorkoutSetCreation() {
        val set = WorkoutSet(
            id = "set_1",
            sessionId = "sess_1",
            exerciseId = "ex_bench",
            exerciseName = "Développé Couché",
            primaryMuscle = MuscleGroup.CHEST,
            equipment = EquipmentType.BARBELL,
            activityCategory = WorkoutActivityCategory.STRENGTH_TRAINING,
            type = RoutineSetType.NORMAL,
            weightKg = 100.0,
            weightUnit = WeightUnit.KG,
            reps = 8,
            isCompleted = false,
            side = UnilateralSide.NONE
        )
        assertEquals("set_1", set.id)
        assertEquals("ex_bench", set.exerciseId)
        assertEquals(100.0, set.weightKg, 0.001)
        assertEquals(8, set.reps)
        assertEquals(RoutineSetType.NORMAL, set.type)
        assertEquals(WeightUnit.KG, set.weightUnit)
        assertEquals(UnilateralSide.NONE, set.side)
        assertFalse(set.isCompleted)
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
