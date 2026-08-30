package app.bodyforger.core.database

import app.bodyforger.core.database.entity.WorkoutSessionEntity
import app.bodyforger.core.database.entity.WorkoutSessionWithSets
import app.bodyforger.core.database.entity.WorkoutSetEntity
import app.bodyforger.core.database.entity.toDomain
import app.bodyforger.core.database.entity.toEntity
import app.bodyforger.core.model.EquipmentType
import app.bodyforger.core.model.MuscleGroup
import app.bodyforger.core.model.RoutineSetType
import app.bodyforger.core.model.UnilateralSide
import app.bodyforger.core.model.WeightUnit
import app.bodyforger.core.model.WorkoutActivityCategory
import app.bodyforger.core.model.WorkoutSession
import app.bodyforger.core.model.WorkoutSessionStatus
import app.bodyforger.core.model.WorkoutSet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutMappingTest {

    @Test
    fun testWorkoutSessionEntityToDomainAndBack() {
        val domainSession = WorkoutSession(
            id = "sess_123",
            routineId = "r_push_1",
            title = "Push Hypertrophie",
            notes = "Focus pecs",
            status = WorkoutSessionStatus.COMPLETED,
            startedAtEpochMs = 1756400000000L,
            endedAtEpochMs = 1756403600000L,
            averageHeartRateBpm = 142,
            activeCaloriesKcal = 420,
            totalVolumeKg = 8500.0,
            isFinalized = true
        )

        val entity = domainSession.toEntity()
        assertEquals("sess_123", entity.id)
        assertEquals("r_push_1", entity.routineId)
        assertEquals("Push Hypertrophie", entity.title)
        assertEquals("COMPLETED", entity.status)
        assertEquals(8500.0, entity.totalVolumeKg, 0.01)
        assertTrue(entity.isFinalized)

        val convertedDomain = entity.toDomain()
        assertEquals(domainSession.id, convertedDomain.id)
        assertEquals(domainSession.title, convertedDomain.title)
        assertEquals(domainSession.status, convertedDomain.status)
        assertEquals(domainSession.totalVolumeKg, convertedDomain.totalVolumeKg, 0.01)
    }

    @Test
    fun testWorkoutSetEntityToDomainAndBack() {
        val domainSet = WorkoutSet(
            id = "set_001",
            sessionId = "sess_123",
            exerciseId = "bf_bench_press",
            exerciseName = "Développé Couché",
            primaryMuscle = MuscleGroup.CHEST,
            equipment = EquipmentType.BARBELL,
            activityCategory = WorkoutActivityCategory.STRENGTH_TRAINING,
            orderIndex = 0,
            setIndex = 1,
            type = RoutineSetType.NORMAL,
            weightKg = 85.0,
            weightUnit = WeightUnit.KG,
            reps = 10,
            rpe = 8.5,
            isCompleted = true,
            side = UnilateralSide.NONE,
            restTimeSeconds = 90,
            completedAtEpochMs = 1756400600000L
        )

        val entity = domainSet.toEntity(sessionId = "sess_123")
        assertEquals("set_001", entity.id)
        assertEquals("sess_123", entity.sessionId)
        assertEquals("bf_bench_press", entity.exerciseId)
        assertEquals("Développé Couché", entity.exerciseName)
        assertEquals("CHEST", entity.primaryMuscle)
        assertEquals("BARBELL", entity.equipment)
        assertEquals("STRENGTH_TRAINING", entity.activityCategory)
        assertEquals(85.0, entity.weightKg, 0.01)
        assertEquals("KG", entity.weightUnit)
        assertEquals(10, entity.reps)
        assertTrue(entity.isCompleted)
        assertEquals("NONE", entity.side)

        val convertedDomain = entity.toDomain()
        assertEquals(domainSet.id, convertedDomain.id)
        assertEquals(domainSet.primaryMuscle, convertedDomain.primaryMuscle)
        assertEquals(domainSet.equipment, convertedDomain.equipment)
        assertEquals(domainSet.activityCategory, convertedDomain.activityCategory)
        assertEquals(domainSet.weightUnit, convertedDomain.weightUnit)
        assertEquals(domainSet.side, convertedDomain.side)
        assertTrue(convertedDomain.isCompleted)
    }

    @Test
    fun testUnilateralWorkoutSetMapping() {
        val leftSet = WorkoutSet(
            id = "set_uni_l",
            sessionId = "sess_uni",
            exerciseId = "bf_lateral_raise_cable",
            exerciseName = "Élévations Latérales Poulie Unilatérale",
            primaryMuscle = MuscleGroup.SHOULDERS,
            equipment = EquipmentType.CABLE,
            activityCategory = WorkoutActivityCategory.STRENGTH_TRAINING,
            orderIndex = 2,
            setIndex = 1,
            type = RoutineSetType.NORMAL,
            weightKg = 15.0,
            weightUnit = WeightUnit.LBS,
            reps = 15,
            isCompleted = true,
            side = UnilateralSide.LEFT
        )

        val entity = leftSet.toEntity("sess_uni")
        assertEquals("LEFT", entity.side)
        assertEquals("LBS", entity.weightUnit)

        val domain = entity.toDomain()
        assertEquals(UnilateralSide.LEFT, domain.side)
        assertEquals(WeightUnit.LBS, domain.weightUnit)
    }

    @Test
    fun testWorkoutSessionWithSetsRelationToDomain() {
        val sessionEntity = WorkoutSessionEntity(
            id = "sess_full",
            routineId = "r_1",
            title = "Full Session",
            startedAtEpochMs = 1000L,
            totalVolumeKg = 1500.0
        )

        val setEntities = listOf(
            WorkoutSetEntity(
                id = "s_2",
                sessionId = "sess_full",
                exerciseId = "bf_squat",
                exerciseName = "Squat",
                orderIndex = 1,
                setIndex = 1,
                weightKg = 100.0,
                reps = 10,
                isCompleted = true
            ),
            WorkoutSetEntity(
                id = "s_1",
                sessionId = "sess_full",
                exerciseId = "bf_bench",
                exerciseName = "Bench Press",
                orderIndex = 0,
                setIndex = 1,
                weightKg = 50.0,
                reps = 10,
                isCompleted = true
            )
        )

        val relation = WorkoutSessionWithSets(
            session = sessionEntity,
            sets = setEntities
        )

        val domain = relation.toDomain()
        assertEquals("sess_full", domain.id)
        assertEquals(2, domain.sets.size)
        assertEquals("bf_bench", domain.sets[0].exerciseId)
        assertEquals("bf_squat", domain.sets[1].exerciseId)
    }
}
