package app.bodyforger.mobile

import android.app.Application
import app.bodyforger.mobile.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * Builds the object graph once, before the first screen exists.
 *
 * Screens ask the graph for what they need instead of receiving it from their parent, so a
 * screen leaving the composition no longer takes its state with it.
 */
class BodyForgerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@BodyForgerApplication)
            modules(appModule)
        }
    }
}
