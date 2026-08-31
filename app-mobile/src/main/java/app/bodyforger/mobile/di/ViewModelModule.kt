package app.bodyforger.mobile.di

import app.bodyforger.mobile.library.LibraryViewModel
import app.bodyforger.mobile.library.RoutineDraftViewModel
import app.bodyforger.mobile.profile.AthleteProfileViewModel
import app.bodyforger.mobile.profile.BiometricsViewModel
import app.bodyforger.mobile.scale.ScaleViewModel
import app.bodyforger.mobile.workout.LiveWorkoutViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * The ViewModels, each declaring what it reads and writes in its constructor.
 *
 * [RoutineDraftViewModel] is the exception: it holds no dependency, only the routine being
 * edited. It is declared here so that the editor and the catalogue share one instance —
 * which is what lets a draft survive the trip between them.
 */
val viewModelModule = module {
    viewModel { LibraryViewModel(routineDao = get(), exerciseDao = get(), workoutDao = get()) }
    viewModel { LiveWorkoutViewModel(workoutDao = get(), workoutHaptics = get()) }
    viewModel { RoutineDraftViewModel() }
    viewModel { AthleteProfileViewModel(identityDao = get()) }
    viewModel { BiometricsViewModel(bodyLogDao = get(), identityDao = get()) }
    viewModel {
        ScaleViewModel(
            application = androidApplication(),
            athleteIdentityDao = get(),
            bodyLogDao = get(),
            scaleAssociationDao = get()
        )
    }
}
