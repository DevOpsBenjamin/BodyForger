package app.bodyforger.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
abstract class DatabaseTestBase {

    protected lateinit var database: BodyForgerDatabase

    @Before
    open fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, BodyForgerDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    open fun tearDown() {
        database.close()
    }
}
