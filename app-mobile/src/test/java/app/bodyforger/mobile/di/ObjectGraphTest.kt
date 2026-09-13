package app.bodyforger.mobile.di

import android.app.Application
import android.content.Context
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.verify

/**
 * A missing binding is a crash on the screen that needed it, not a compile error.
 *
 * Walking the graph here turns that runtime surprise back into a failing build.
 */
class ObjectGraphTest {

    /** Android hands these to the graph at startup; nothing declares them. */
    private val providedByAndroid = listOf(Context::class, Application::class)

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `every declared dependency can be resolved`() {
        appModule.verify(extraTypes = providedByAndroid)
    }
}
