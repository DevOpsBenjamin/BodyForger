package app.bodyforger.core.database

import app.bodyforger.core.database.dao.AthleteIdentityDao
import app.bodyforger.core.database.entity.AthleteIdentityEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class AthleteIdentityDaoTest : DatabaseTestBase() {

    private lateinit var identityDao: AthleteIdentityDao

    @Before
    override fun setUp() {
        super.setUp()
        identityDao = database.athleteIdentityDao()
    }

    @Test
    fun huidOrCreate_returnsSameHuidOnSubsequentCalls() = runTest {
        val firstHuid = identityDao.huidOrCreate(nowEpochMs = 1000L)
        assertNotNull(firstHuid)
        assertEquals(AthleteIdentityEntity.HUID_DIGITS, firstHuid.length)

        val secondHuid = identityDao.huidOrCreate(nowEpochMs = 2000L)
        assertEquals(firstHuid, secondHuid)

        val entity = identityDao.find()
        assertNotNull(entity)
        assertEquals(firstHuid, entity!!.huid)
        assertEquals(1000L, entity.createdAtEpochMs)
    }

    @Test
    fun adopt_preservesExistingProfileWhenReplacingHuid() = runTest {
        identityDao.saveProfile(
            name = "Athlete One",
            sex = "MALE",
            birthDateIso = "1995-06-20",
            heightCm = 182.5,
            nowEpochMs = 1000L
        )

        val beforeAdopt = identityDao.find()
        assertNotNull(beforeAdopt)
        assertEquals("Athlete One", beforeAdopt!!.name)
        assertEquals("MALE", beforeAdopt.sex)
        assertEquals("1995-06-20", beforeAdopt.birthDateIso)
        assertEquals(182.5, beforeAdopt.heightCm!!, 0.001)

        val remoteHuid = "0123456789"
        identityDao.adopt(remoteHuid, nowEpochMs = 2000L)

        val afterAdopt = identityDao.find()
        assertNotNull(afterAdopt)
        assertEquals(remoteHuid, afterAdopt!!.huid)
        assertEquals("SYNCED_PEER", afterAdopt.syncState)
        assertEquals("Athlete One", afterAdopt.name)
        assertEquals("MALE", afterAdopt.sex)
        assertEquals("1995-06-20", afterAdopt.birthDateIso)
        assertEquals(182.5, afterAdopt.heightCm!!, 0.001)
        assertEquals(2000L, afterAdopt.createdAtEpochMs)
    }

    @Test
    fun saveProfile_createsHuidAndPersistsProfileFields() = runTest {
        identityDao.saveProfile(
            name = "Athlete Two",
            sex = "FEMALE",
            birthDateIso = "1998-11-03",
            heightCm = 168.0,
            nowEpochMs = 1500L
        )

        val entity = identityDao.find()
        assertNotNull(entity)
        assertNotNull(entity!!.huid)
        assertEquals("Athlete Two", entity.name)
        assertEquals("FEMALE", entity.sex)
        assertEquals("1998-11-03", entity.birthDateIso)
        assertEquals(168.0, entity.heightCm!!, 0.001)
    }
}
