package app.bodyforger.core.sync

import app.bodyforger.core.model.EquipmentType
import app.bodyforger.core.model.Exercise
import app.bodyforger.core.model.HealthConnectExerciseType
import app.bodyforger.core.model.MuscleGroup
import app.bodyforger.core.model.Routine
import app.bodyforger.core.model.RoutineExercise
import app.bodyforger.core.model.RoutineSet
import app.bodyforger.core.model.RoutineSetType
import app.bodyforger.core.model.UnilateralSide
import app.bodyforger.core.model.WeightUnit
import app.bodyforger.core.model.WorkoutActivityCategory
import app.bodyforger.core.model.WorkoutSession
import app.bodyforger.core.model.WorkoutSessionStatus
import app.bodyforger.core.model.WorkoutSet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JsonBackupManagerTest {

    @Test
    fun testSerializeAndDeserializeFullBackup() {
        val routine = Routine(
            id = "r_push",
            name = "Push Day Alpha",
            notes = "Focus pecs claviculaire",
            assignedDays = setOf(1, 4),
            exercises = listOf(
                RoutineExercise(
                    id = "re_1",
                    routineId = "r_push",
                    exerciseId = "bf_bench_press",
                    exerciseName = "Développé Couché",
                    activityCategory = WorkoutActivityCategory.STRENGTH_TRAINING,
                    primaryMuscle = MuscleGroup.CHEST,
                    equipment = EquipmentType.BARBELL,
                    weightUnit = WeightUnit.KG,
                    restTimeSeconds = 120,
                    sets = listOf(
                        RoutineSet(id = "s_1", setIndex = 1, type = RoutineSetType.WARMUP, targetWeightKg = 60.0, reps = 12),
                        RoutineSet(id = "s_2", setIndex = 2, type = RoutineSetType.NORMAL, targetWeightKg = 100.0, reps = 8)
                    )
                )
            )
        )

        val session = WorkoutSession(
            id = "sess_001",
            routineId = "r_push",
            title = "Push Day Alpha",
            status = WorkoutSessionStatus.COMPLETED,
            startedAtEpochMs = 1756400000000L,
            endedAtEpochMs = 1756403600000L,
            totalVolumeKg = 4500.0,
            sets = listOf(
                WorkoutSet(
                    id = "wset_1",
                    sessionId = "sess_001",
                    exerciseId = "bf_bench_press",
                    exerciseName = "Développé Couché",
                    primaryMuscle = MuscleGroup.CHEST,
                    equipment = EquipmentType.BARBELL,
                    orderIndex = 0,
                    setIndex = 1,
                    weightKg = 100.0,
                    reps = 8,
                    isCompleted = true,
                    side = UnilateralSide.NONE
                )
            )
        )

        val customEx = Exercise(
            id = "c_1",
            name = "Poulie Vis-à-Vis Basse",
            healthConnectType = HealthConnectExerciseType.OTHER_WORKOUT,
            primaryMuscleGroup = MuscleGroup.CHEST,
            equipment = EquipmentType.CABLE,
            isCustom = true
        )

        val payload = BodyForgerBackupPayload(
            schemaVersion = 1,
            exportedAtEpochMs = 1756404000000L,
            appVersion = "0.1.0-alpha",
            routines = listOf(routine),
            sessions = listOf(session),
            customExercises = listOf(customEx)
        )

        val json = JsonBackupManager.serialize(payload)
        assertTrue(json.contains("Push Day Alpha"))
        assertTrue(json.contains("bf_bench_press"))
        assertTrue(json.contains("Poulie Vis-à-Vis Basse"))

        val restored = JsonBackupManager.deserialize(json)
        assertEquals(1, restored.schemaVersion)
        assertEquals(1, restored.routines.size)
        assertEquals("Push Day Alpha", restored.routines[0].name)
        assertEquals(setOf(1, 4), restored.routines[0].assignedDays)
        assertEquals(1, restored.routines[0].exercises.size)
        assertEquals(2, restored.routines[0].exercises[0].sets.size)

        assertEquals(1, restored.sessions.size)
        assertEquals("sess_001", restored.sessions[0].id)
        assertEquals(4500.0, restored.sessions[0].totalVolumeKg, 0.01)
        assertEquals(1, restored.sessions[0].sets.size)
        assertTrue(restored.sessions[0].sets[0].isCompleted)

        assertEquals(1, restored.customExercises.size)
        assertEquals("Poulie Vis-à-Vis Basse", restored.customExercises[0].name)
    }
}
