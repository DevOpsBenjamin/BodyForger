package app.bodyforger.mobile.di

import org.koin.dsl.module

/**
 * The whole object graph, named once so startup and tests cannot drift apart.
 *
 * The application starts this and nothing else; `ObjectGraphTest` walks this and nothing else.
 */
val appModule = module {
    includes(dataModule, viewModelModule)
}
