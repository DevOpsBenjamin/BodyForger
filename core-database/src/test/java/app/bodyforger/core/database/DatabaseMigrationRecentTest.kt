package app.bodyforger.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DatabaseMigrationRecentTest {

    private val testDbName = "migration-recent-test.db"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        BodyForgerDatabase::class.java
    )

    @Test
    fun migrate12To14_addsHeartRateSamplesAndHealthConnectPrompt() {
        val dbV12 = helper.createDatabase(testDbName, 12)
        dbV12.execSQL("INSERT INTO app_settings (id, tourVersionSeen, setupVersionDone) VALUES (1, 1, 1)")
        dbV12.execSQL(
            """
            INSERT INTO workout_sessions (
                id, title, notes, status, startedAtEpochMs, totalVolumeKg, isFinalized
            ) VALUES (
                'sess_1', 'Morning Push', '', 'COMPLETED', 1725000000000, 1200.0, 1
            )
            """.trimIndent()
        )
        dbV12.close()

        val dbV14 = helper.runMigrationsAndValidate(
            testDbName,
            14,
            true,
            BodyForgerDatabases.MIGRATION_12_13,
            BodyForgerDatabases.MIGRATION_13_14
        )

        dbV14.execSQL(
            "INSERT INTO workout_heart_rate_samples (sessionId, timestampEpochMs, bpm) VALUES ('sess_1', 1725000010000, 145)"
        )

        val cursorSample = dbV14.query("SELECT sessionId, bpm FROM workout_heart_rate_samples WHERE sessionId = 'sess_1'")
        assertTrue(cursorSample.moveToFirst())
        assertEquals("sess_1", cursorSample.getString(0))
        assertEquals(145, cursorSample.getInt(1))
        cursorSample.close()

        val cursorSettings = dbV14.query("SELECT healthConnectPromptDismissed FROM app_settings WHERE id = 1")
        assertTrue(cursorSettings.moveToFirst())
        assertEquals(0, cursorSettings.getInt(0))
        cursorSettings.close()
    }

    @Test
    fun migrate14To15_addsExportFlagAndSetTimingFields() {
        val dbV14 = helper.createDatabase(testDbName, 14)
        dbV14.execSQL(
            """
            INSERT INTO workout_sessions (
                id, title, notes, status, startedAtEpochMs, totalVolumeKg, isFinalized
            ) VALUES (
                'sess_14', 'Legacy Workout', '', 'COMPLETED', 1725000000000, 1500.0, 1
            )
            """.trimIndent()
        )
        dbV14.execSQL(
            """
            INSERT INTO workout_sets (
                id, sessionId, exerciseId, exerciseName, primaryMuscle, equipment, activityCategory,
                orderIndex, setIndex, type, weightKg, weightUnit, reps, isCompleted, side, restTimeSeconds
            ) VALUES (
                'set_14', 'sess_14', 'ex_bench', 'Bench Press', 'CHEST', 'BARBELL', 'STRENGTH_TRAINING',
                0, 0, 'NORMAL', 100.0, 'KG', 10, 1, 'NONE', 90
            )
            """.trimIndent()
        )
        dbV14.close()

        val dbV15 = helper.runMigrationsAndValidate(
            testDbName,
            15,
            true,
            BodyForgerDatabases.MIGRATION_14_15
        )

        val cursorSession = dbV15.query(
            "SELECT id, isHealthConnectExported FROM workout_sessions WHERE id = 'sess_14'"
        )
        assertTrue(cursorSession.moveToFirst())
        assertEquals("sess_14", cursorSession.getString(0))
        assertEquals(0, cursorSession.getInt(1))
        cursorSession.close()

        val cursorSet = dbV15.query(
            "SELECT id, startedAtEpochMs, actualRestSeconds FROM workout_sets WHERE id = 'set_14'"
        )
        assertTrue(cursorSet.moveToFirst())
        assertEquals("set_14", cursorSet.getString(0))
        assertTrue(cursorSet.isNull(1))
        assertTrue(cursorSet.isNull(2))
        cursorSet.close()

        dbV15.execSQL(
            """
            INSERT INTO workout_sets (
                id, sessionId, exerciseId, exerciseName, primaryMuscle, equipment, activityCategory,
                orderIndex, setIndex, type, weightKg, weightUnit, reps, isCompleted, side, restTimeSeconds,
                startedAtEpochMs, actualRestSeconds
            ) VALUES (
                'set_15', 'sess_14', 'ex_bench', 'Bench Press', 'CHEST', 'BARBELL', 'STRENGTH_TRAINING',
                0, 1, 'NORMAL', 100.0, 'KG', 10, 1, 'NONE', 90, 1725000090000, 90
            )
            """.trimIndent()
        )

        val cursorNewSet = dbV15.query(
            "SELECT id, startedAtEpochMs, actualRestSeconds FROM workout_sets WHERE id = 'set_15'"
        )
        assertTrue(cursorNewSet.moveToFirst())
        assertEquals("set_15", cursorNewSet.getString(0))
        assertEquals(1725000090000L, cursorNewSet.getLong(1))
        assertEquals(90, cursorNewSet.getInt(2))
        cursorNewSet.close()
    }

    @Test
    fun migrateAll_from5To15_preservesAllExistingData() {
        val dbV5 = helper.createDatabase(testDbName, 5)
        dbV5.execSQL(
            """
            INSERT INTO athlete_identity (
                id, huid, createdAtEpochMs, syncState, name, sex, birthDateIso, heightCm
            ) VALUES (
                1, '9876543210', 1725000000000, 'LOCAL', 'Benjamin', 'MALE', '1990-01-01', 180.0
            )
            """.trimIndent()
        )
        dbV5.execSQL(
            """
            INSERT INTO workout_sessions (
                id, title, notes, status, startedAtEpochMs, totalVolumeKg, isFinalized
            ) VALUES (
                'sess_root', 'Root Push', '', 'COMPLETED', 1725000000000, 2000.0, 1
            )
            """.trimIndent()
        )
        dbV5.close()

        val dbV15 = helper.runMigrationsAndValidate(
            testDbName,
            15,
            true,
            *BodyForgerDatabases.ALL_MIGRATIONS
        )

        val cursorAthlete = dbV15.query("SELECT id, huid, name, heightCm FROM athlete_identity WHERE id = 1")
        assertTrue(cursorAthlete.moveToFirst())
        assertEquals("9876543210", cursorAthlete.getString(1))
        assertEquals("Benjamin", cursorAthlete.getString(2))
        assertEquals(180.0, cursorAthlete.getDouble(3), 0.001)
        cursorAthlete.close()

        val cursorSession = dbV15.query(
            "SELECT id, title, totalVolumeKg, isHealthConnectExported FROM workout_sessions WHERE id = 'sess_root'"
        )
        assertTrue(cursorSession.moveToFirst())
        assertEquals("sess_root", cursorSession.getString(0))
        assertEquals("Root Push", cursorSession.getString(1))
        assertEquals(2000.0, cursorSession.getDouble(2), 0.001)
        assertEquals(0, cursorSession.getInt(3))
        cursorSession.close()
    }
}
