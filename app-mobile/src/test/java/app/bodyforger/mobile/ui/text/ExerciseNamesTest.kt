package app.bodyforger.mobile.ui.text

import app.bodyforger.core.database.data.DefaultExercises
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The resolution table is written by hand next to a seed that grows: this is what stops the
 * two from drifting apart, which would show an English name inside a French catalogue.
 */
class ExerciseNamesTest {

    @Test
    fun `every built-in exercise carries a name key`() {
        val keyless = DefaultExercises.all.filter { it.nameKey.isNullOrBlank() }
        assertTrue("built-in exercises without a key: ${keyless.map { it.id }}", keyless.isEmpty())
    }

    @Test
    fun `every name key resolves to a string resource`() {
        DefaultExercises.all.forEach { exercise ->
            val key = exercise.nameKey!!
            assertNotNull("no resource for $key (${exercise.id})", ExerciseNames.resourceFor(key))
        }
    }

    @Test
    fun `two exercises never share a name key`() {
        val keys = DefaultExercises.all.mapNotNull { it.nameKey }
        assertEquals(keys.size, keys.toSet().size)
    }

    @Test
    fun `an unknown key resolves to nothing rather than to another exercise`() {
        assertNotNull(ExerciseNames.resourceFor("ex_bench_press"))
        assertNull(ExerciseNames.resourceFor("ex_not_a_real_exercise"))
    }
}
