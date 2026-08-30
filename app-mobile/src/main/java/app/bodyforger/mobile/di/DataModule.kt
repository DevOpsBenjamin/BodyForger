package app.bodyforger.mobile.di

import app.bodyforger.core.database.BodyForgerDatabase
import app.bodyforger.core.database.BodyForgerDatabases
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * The database and the DAOs cut from it.
 *
 * The database stays a single instance for the whole process: two Room handles on one file
 * each keep their own cache, and writes made through one stay invisible to the other.
 */
val dataModule = module {
    single<BodyForgerDatabase> { BodyForgerDatabases.get(androidContext()) }
    single { get<BodyForgerDatabase>().exerciseDao() }
    single { get<BodyForgerDatabase>().routineDao() }
    single { get<BodyForgerDatabase>().workoutDao() }
    single { get<BodyForgerDatabase>().bodyLogDao() }
    single { get<BodyForgerDatabase>().athleteIdentityDao() }
    single { get<BodyForgerDatabase>().scaleAssociationDao() }
}
