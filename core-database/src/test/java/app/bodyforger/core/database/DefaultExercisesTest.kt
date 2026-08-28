package app.bodyforger.core.database

import app.bodyforger.core.database.data.DefaultExercises
import app.bodyforger.core.database.entity.toDomain
import app.bodyforger.core.database.entity.toEntity
import app.bodyforger.core.model.EquipmentType
import app.bodyforger.core.model.HealthConnectExerciseType
import app.bodyforger.core.model.MuscleGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultExercisesTest {

    @Test
    fun defaultExercises_haveValidCountAndUniqueIds() {
        val exercises = DefaultExercises.all

        assertTrue("Le catalogue doit contenir au moins 80 exercices", exercises.size >= 80)

        val ids = exercises.map { it.id }
        assertEquals("Tous les identifiants d'exercices doivent être uniques", ids.size, ids.toSet().size)
    }

    @Test
    fun defaultExercises_areAllNonCustom() {
        DefaultExercises.all.forEach { exercise ->
            assertFalse("L'exercice par défaut ${exercise.name} ne doit pas être custom", exercise.isCustom)
            assertTrue("Le nom de l'exercice ne doit pas être vide", exercise.name.isNotBlank())
            assertTrue("L'identifiant doit commencer par bf_", exercise.id.startsWith("bf_"))
        }
    }

    @Test
    fun defaultExercises_coverAllMajorMuscleGroups() {
        val musclesCovered = DefaultExercises.all.map { it.primaryMuscleGroup }.toSet()

        assertTrue(musclesCovered.contains(MuscleGroup.CHEST.name))
        assertTrue(musclesCovered.contains(MuscleGroup.BACK.name))
        assertTrue(musclesCovered.contains(MuscleGroup.SHOULDERS.name))
        assertTrue(musclesCovered.contains(MuscleGroup.BICEPS.name))
        assertTrue(musclesCovered.contains(MuscleGroup.TRICEPS.name))
        assertTrue(musclesCovered.contains(MuscleGroup.QUADRICEPS.name))
        assertTrue(musclesCovered.contains(MuscleGroup.HAMSTRINGS.name))
        assertTrue(musclesCovered.contains(MuscleGroup.CALVES.name))
        assertTrue(musclesCovered.contains(MuscleGroup.ABS.name))
    }

    @Test
    fun defaultExercises_properlyFlagUnilateralMovements() {
        val unilateralExercises = DefaultExercises.all.filter { it.isUnilateral }

        assertTrue("Des exercices unilatéraux doivent être identifiés", unilateralExercises.isNotEmpty())

        val unilateralNames = unilateralExercises.map { it.name }
        assertTrue(unilateralNames.contains("Split Squat Bulgare"))
        assertTrue(unilateralNames.contains("Fentes Marchantes"))
        assertTrue(unilateralNames.contains("Rowing Haltère Unilatéral"))
        assertTrue(unilateralNames.contains("Tirage Horizontal Poulie Unilatéral"))
        assertTrue(unilateralNames.contains("Élévations Latérales Poulie Unilatérale"))
    }

    @Test
    fun defaultExercises_properlyFlagConvergentMachines() {
        val convergentExercises = DefaultExercises.all.filter { it.equipment == EquipmentType.MACHINE_CONVERGENT.name }

        assertTrue("Des machines convergentes doivent être présentes", convergentExercises.isNotEmpty())

        val convergentNames = convergentExercises.map { it.name }
        assertTrue(convergentNames.contains("Développé Couché Convergent"))
        assertTrue(convergentNames.contains("Développé Épaules Convergent"))
        assertTrue(convergentNames.contains("Tirage Vertical Convergent"))
    }

    @Test
    fun exerciseMapping_toDomainAndBackPreservesIntegrity() {
        DefaultExercises.all.forEach { entity ->
            val domain = entity.toDomain()
            assertEquals(entity.id, domain.id)
            assertEquals(entity.name, domain.name)
            assertEquals(entity.isUnilateral, domain.isUnilateral)
            assertEquals(entity.isCustom, domain.isCustom)

            val backToEntity = domain.toEntity()
            assertEquals(entity.id, backToEntity.id)
            assertEquals(entity.name, backToEntity.name)
            assertEquals(entity.healthConnectType, backToEntity.healthConnectType)
            assertEquals(entity.primaryMuscleGroup, backToEntity.primaryMuscleGroup)
            assertEquals(entity.equipment, backToEntity.equipment)
            assertEquals(entity.isUnilateral, backToEntity.isUnilateral)
            assertEquals(entity.isCustom, backToEntity.isCustom)
        }
    }
}
