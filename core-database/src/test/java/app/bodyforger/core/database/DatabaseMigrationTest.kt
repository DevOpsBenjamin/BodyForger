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
class DatabaseMigrationTest {

    private val testDbName = "migration-test.db"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        BodyForgerDatabase::class.java
    )

    @Test
    fun migrate5To6_createsAppSettingsTable() {
        helper.createDatabase(testDbName, 5).close()

        val db = helper.runMigrationsAndValidate(
            testDbName,
            6,
            true,
            BodyForgerDatabases.MIGRATION_5_6
        )

        db.execSQL("INSERT INTO app_settings (id, biaEngineId) VALUES (1, 'dexa_v1')")
        val cursor = db.query("SELECT id, biaEngineId FROM app_settings WHERE id = 1")
        assertTrue(cursor.moveToFirst())
        assertEquals(1, cursor.getInt(0))
        assertEquals("dexa_v1", cursor.getString(1))
        cursor.close()
    }

    @Test
    fun migrate6To7_addsNameKeyAndBackfillsSeededExercises() {
        val dbV6 = helper.createDatabase(testDbName, 6)
        dbV6.execSQL(
            """
            INSERT INTO exercises (
                id, name, activityCategory, healthConnectType, primaryMuscleGroup,
                equipment, secondaryMuscleGroupsJson, isUnilateral, isCustom, instructionsJson
            ) VALUES (
                'bf_chest_001', 'Bench Press', 'STRENGTH_TRAINING', 'BENCH_PRESS', 'CHEST',
                'BARBELL', '[]', 0, 0, '[]'
            )
            """.trimIndent()
        )
        dbV6.close()

        val dbV7 = helper.runMigrationsAndValidate(
            testDbName,
            7,
            true,
            BodyForgerDatabases.MIGRATION_6_7
        )

        val cursor = dbV7.query("SELECT id, nameKey, name FROM exercises WHERE id = 'bf_chest_001'")
        assertTrue(cursor.moveToFirst())
        assertEquals("bf_chest_001", cursor.getString(0))
        assertEquals("ex_bench_press", cursor.getString(1))
        assertEquals("Bench Press", cursor.getString(2))
        cursor.close()
    }

    @Test
    fun migrate7To8_preservesDataAndAllowsNullableBodyFat() {
        val dbV7 = helper.createDatabase(testDbName, 7)
        dbV7.execSQL(
            """
            INSERT INTO body_logs (
                id, dateIso, measuredAtEpochMs, massKg, bodyFatPercentage, restingHeartRateBpm, sourceDeviceAddress
            ) VALUES (
                'log_v7', '2026-09-01', 1725148800000, 78.4, 15.2, 58, 'AA:BB:CC:DD:EE:FF'
            )
            """.trimIndent()
        )
        dbV7.close()

        val dbV8 = helper.runMigrationsAndValidate(
            testDbName,
            8,
            true,
            BodyForgerDatabases.MIGRATION_7_8
        )

        val cursorExisting = dbV8.query("SELECT id, massKg, bodyFatPercentage FROM body_logs WHERE id = 'log_v7'")
        assertTrue(cursorExisting.moveToFirst())
        assertEquals(78.4, cursorExisting.getDouble(1), 0.001)
        assertEquals(15.2, cursorExisting.getDouble(2), 0.001)
        cursorExisting.close()

        dbV8.execSQL(
            """
            INSERT INTO body_logs (
                id, dateIso, measuredAtEpochMs, massKg, bodyFatPercentage
            ) VALUES (
                'log_mass_only', '2026-09-02', 1725235200000, 78.0, NULL
            )
            """.trimIndent()
        )

        val cursorNull = dbV8.query("SELECT id, massKg, bodyFatPercentage FROM body_logs WHERE id = 'log_mass_only'")
        assertTrue(cursorNull.moveToFirst())
        assertEquals(78.0, cursorNull.getDouble(1), 0.001)
        assertTrue(cursorNull.isNull(2))
        cursorNull.close()
    }

    @Test
    fun migrate8To9_addsDefaultWeightUnit() {
        val dbV8 = helper.createDatabase(testDbName, 8)
        dbV8.execSQL("INSERT INTO app_settings (id, biaEngineId) VALUES (1, 'default_engine')")
        dbV8.close()

        val dbV9 = helper.runMigrationsAndValidate(
            testDbName,
            9,
            true,
            BodyForgerDatabases.MIGRATION_8_9
        )

        val cursor = dbV9.query("SELECT id, biaEngineId, defaultWeightUnit FROM app_settings WHERE id = 1")
        assertTrue(cursor.moveToFirst())
        assertEquals("default_engine", cursor.getString(1))
        assertNull(cursor.getString(2))
        cursor.close()
    }

    @Test
    fun migrate9To10_createsBodyGoalsTable() {
        helper.createDatabase(testDbName, 9).close()

        val dbV10 = helper.runMigrationsAndValidate(
            testDbName,
            10,
            true,
            BodyForgerDatabases.MIGRATION_9_10
        )

        dbV10.execSQL(
            """
            INSERT INTO body_goals (
                id, targetMassKg, targetBodyFatPercentage, createdOnIso
            ) VALUES (
                'goal_1', 75.0, 12.0, '2026-09-14'
            )
            """.trimIndent()
        )

        val cursor = dbV10.query("SELECT id, targetMassKg, targetBodyFatPercentage FROM body_goals WHERE id = 'goal_1'")
        assertTrue(cursor.moveToFirst())
        assertEquals(75.0, cursor.getDouble(1), 0.001)
        assertEquals(12.0, cursor.getDouble(2), 0.001)
        cursor.close()
    }

    @Test
    fun migrate10To12_addsHeightUnitAndOnboardingVersions() {
        val dbV10 = helper.createDatabase(testDbName, 10)
        dbV10.execSQL("INSERT INTO app_settings (id, biaEngineId) VALUES (1, 'engine')")
        dbV10.close()

        val dbV12 = helper.runMigrationsAndValidate(
            testDbName,
            12,
            true,
            BodyForgerDatabases.MIGRATION_10_11,
            BodyForgerDatabases.MIGRATION_11_12
        )

        val cursor = dbV12.query(
            "SELECT id, defaultHeightUnit, tourVersionSeen, setupVersionDone FROM app_settings WHERE id = 1"
        )
        assertTrue(cursor.moveToFirst())
        assertNull(cursor.getString(1))
        assertEquals(0, cursor.getInt(2))
        assertEquals(0, cursor.getInt(3))
        cursor.close()
    }

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
    fun migrateAll_from5To14_preservesAllExistingData() {
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

        val dbV14 = helper.runMigrationsAndValidate(
            testDbName,
            14,
            true,
            *BodyForgerDatabases.ALL_MIGRATIONS
        )

        val cursorAthlete = dbV14.query("SELECT id, huid, name, heightCm FROM athlete_identity WHERE id = 1")
        assertTrue(cursorAthlete.moveToFirst())
        assertEquals("9876543210", cursorAthlete.getString(1))
        assertEquals("Benjamin", cursorAthlete.getString(2))
        assertEquals(180.0, cursorAthlete.getDouble(3), 0.001)
        cursorAthlete.close()

        val cursorSession = dbV14.query("SELECT id, title, totalVolumeKg FROM workout_sessions WHERE id = 'sess_root'")
        assertTrue(cursorSession.moveToFirst())
        assertEquals("sess_root", cursorSession.getString(0))
        assertEquals("Root Push", cursorSession.getString(1))
        assertEquals(2000.0, cursorSession.getDouble(2), 0.001)
        cursorSession.close()
    }
}
