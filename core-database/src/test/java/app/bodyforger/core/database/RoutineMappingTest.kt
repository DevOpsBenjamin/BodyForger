package app.bodyforger.core.database

import app.bodyforger.core.database.entity.RoutineExerciseEntity
import app.bodyforger.core.database.entity.RoutineExerciseWithSets
import app.bodyforger.core.database.entity.RoutineSetEntity
import app.bodyforger.core.database.entity.RoutineWithExercises
import app.bodyforger.core.database.entity.toDomain
import app.bodyforger.core.database.entity.toEntity
import app.bodyforger.core.model.EquipmentType
import app.bodyforger.core.model.MuscleGroup
import app.bodyforger.core.model.Routine
import app.bodyforger.core.model.RoutineExercise
import app.bodyforger.core.model.RoutineSet
import app.bodyforger.core.model.RoutineSetType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutineMappingTest {

    @Test
    fun routineMapping_toDomainAndBackPreservesFullHierarchy() {
        val routine = Routine(
            id = "routine_push_1",
            name = "Push Day Alpha",
            notes = "Focus haut de pec et deltoïdes latéraux",
            assignedDays = setOf(1, 4), // Lundi et Jeudi
            exercises = listOf(
                RoutineExercise(
                    id = "re_1",
                    routineId = "routine_push_1",
                    exerciseId = "bf_bench_press",
                    exerciseName = "Développé Couché",
                    primaryMuscle = MuscleGroup.CHEST,
                    equipment = EquipmentType.BARBELL,
                    isUnilateral = false,
                    orderIndex = 0,
                    restTimeSeconds = 120,
                    notes = "Prise moyenne, rétraction scapulaire",
                    sets = listOf(
                        RoutineSet(
                            id = "rs_1",
                            setIndex = 1,
                            type = RoutineSetType.WARMUP,
                            targetWeightKg = 50.0,
                            reps = 15,
                            isRepsRange = false
                        ),
                        RoutineSet(
                            id = "rs_2",
                            setIndex = 2,
                            type = RoutineSetType.NORMAL,
                            targetWeightKg = 90.0,
                            minReps = 8,
                            maxReps = 12,
                            isRepsRange = true
                        ),
                        RoutineSet(
                            id = "rs_3",
                            setIndex = 3,
                            type = RoutineSetType.FAILURE,
                            targetWeightKg = 90.0,
                            minReps = 8,
                            maxReps = 12,
                            isRepsRange = true
                        )
                    )
                ),
                RoutineExercise(
                    id = "re_2",
                    routineId = "routine_push_1",
                    exerciseId = "bf_lateral_raise_cable",
                    exerciseName = "Élévations Latérales Poulie Unilatérale",
                    primaryMuscle = MuscleGroup.SHOULDERS,
                    equipment = EquipmentType.CABLE,
                    isUnilateral = true,
                    orderIndex = 1,
                    restTimeSeconds = 60,
                    sets = listOf(
                        RoutineSet(
                            id = "rs_4",
                            setIndex = 1,
                            type = RoutineSetType.NORMAL,
                            targetWeightKg = 12.5,
                            reps = 15
                        ),
                        RoutineSet(
                            id = "rs_5",
                            setIndex = 2,
                            type = RoutineSetType.DROPSET,
                            targetWeightKg = 12.5,
                            reps = 15
                        )
                    ),
                    supersetGroupId = "ss_arms_1"
                )
            )
        )

        // Conversion vers Entités
        val routineEntity = routine.toEntity()
        assertEquals("routine_push_1", routineEntity.id)
        assertEquals("Push Day Alpha", routineEntity.name)
        assertEquals("1,4", routineEntity.assignedDaysCsv)

        val exerciseEntities = routine.exercises.map { it.toEntity(routine.id) }
        assertEquals(2, exerciseEntities.size)
        assertEquals("bf_bench_press", exerciseEntities[0].exerciseId)
        assertTrue(exerciseEntities[1].isUnilateral)
        assertEquals("ss_arms_1", exerciseEntities[1].supersetGroupId)

        val setEntities = routine.exercises.flatMap { ex -> ex.sets.map { it.toEntity(ex.id) } }
        assertEquals(5, setEntities.size)
        assertEquals("WARMUP", setEntities[0].type)
        assertEquals("NORMAL", setEntities[1].type)
        assertEquals("FAILURE", setEntities[2].type)
        assertEquals("NORMAL", setEntities[3].type)
        assertEquals("DROPSET", setEntities[4].type)

        // Reconstruction vers Domain via RoutineWithExercises
        val exerciseWithSetsList = exerciseEntities.map { exEnt ->
            RoutineExerciseWithSets(
                exercise = exEnt,
                sets = setEntities.filter { it.routineExerciseId == exEnt.id }
            )
        }

        val routineWithExercises = RoutineWithExercises(
            routine = routineEntity,
            exercises = exerciseWithSetsList
        )

        val restoredDomain = routineWithExercises.toDomain()
        assertEquals(routine.id, restoredDomain.id)
        assertEquals(routine.name, restoredDomain.name)
        assertEquals(routine.notes, restoredDomain.notes)
        assertEquals(routine.assignedDays, restoredDomain.assignedDays)
        assertEquals(2, restoredDomain.exercises.size)

        // Exercice 1 vérification
        val ex1 = restoredDomain.exercises[0]
        assertEquals("Développé Couché", ex1.exerciseName)
        assertEquals(120, ex1.restTimeSeconds)
        assertEquals(3, ex1.sets.size)
        assertEquals(RoutineSetType.WARMUP, ex1.sets[0].type)
        assertEquals(RoutineSetType.NORMAL, ex1.sets[1].type)
        assertEquals(RoutineSetType.FAILURE, ex1.sets[2].type)
        assertTrue(ex1.sets[1].isRepsRange)
        assertEquals(8, ex1.sets[1].minReps)
        assertEquals(12, ex1.sets[1].maxReps)

        // Exercice 2 vérification
        val ex2 = restoredDomain.exercises[1]
        assertTrue(ex2.isUnilateral)
        assertEquals(RoutineSetType.DROPSET, ex2.sets[1].type)
        assertEquals("ss_arms_1", ex2.supersetGroupId)
    }
}
