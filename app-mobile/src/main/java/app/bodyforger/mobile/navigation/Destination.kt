package app.bodyforger.mobile.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector
import app.bodyforger.mobile.R
import kotlinx.serialization.Serializable

/**
 * Every place the athlete can be, named rather than described by a set of flags.
 *
 * The catalogue is the reason this exists. It used to be reached by raising two booleans, and
 * the screen that had asked for an exercise was guessed from a third — which is how adding an
 * exercise mid-workout came to do nothing at all. A caller now says who it is by choosing a
 * route, so answering the wrong one is no longer expressible.
 */
@Serializable
sealed interface Destination {

    /** The four tabs, which share the bottom bar and the mini bar of a running workout. */
    @Serializable
    data object Home : Destination

    @Serializable
    data object Planner : Destination

    @Serializable
    data object Analytics : Destination

    @Serializable
    data object Profile : Destination

    /**
     * Settings. [expandScale] opens the scale section on arrival, for a caller that needed a
     * scale and did not find one configured.
     */
    @Serializable
    data class Settings(val expandScale: Boolean = false) : Destination

    @Serializable
    data object RoutineEditor : Destination

    @Serializable
    data object LiveWorkout : Destination

    @Serializable
    data object CreateExercise : Destination

    /** The catalogue, opened to look around: choosing an exercise leads nowhere. */
    @Serializable
    data object Catalogue : Destination

    /**
     * The catalogue, opened to pick an exercise for someone.
     *
     * Adding and replacing are separate routes rather than one route with an optional
     * position: they are different intentions, and a route argument cannot be null anyway.
     */
    @Serializable
    data object AddToRoutine : Destination

    @Serializable
    data class ReplaceInRoutine(val index: Int) : Destination

    @Serializable
    data object AddToWorkout : Destination

    @Serializable
    data class ReplaceInWorkout(val index: Int) : Destination
}

/**
 * The four tabs of the bottom bar, in the order it shows them.
 *
 * A tab is a destination plus the way it is drawn. Keeping them apart meant two lists to hold
 * in the same order, and the label was written in French inside the code.
 */
enum class Tab(
    val destination: Destination,
    @StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME(Destination.Home, R.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home),
    PLANNER(Destination.Planner, R.string.nav_planner, Icons.Filled.FitnessCenter, Icons.Outlined.FitnessCenter),
    ANALYTICS(Destination.Analytics, R.string.nav_analytics, Icons.Filled.MonitorWeight, Icons.Outlined.MonitorWeight),
    PROFILE(Destination.Profile, R.string.nav_profile, Icons.Filled.Person, Icons.Outlined.Person)
}
