package app.bodyforger.mobile.navigation

import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.NavGraph.Companion.findStartDestination

/**
 * Moves to a tab the way a bottom bar should.
 *
 * The back stack is unwound to the start destination rather than piled up, so leaving and
 * coming back to a tab does not stack it, and the system back button leaves the app from any
 * tab instead of walking through the ones already visited.
 */
fun NavHostController.switchTab(tab: Tab) {
    navigate(tab.destination) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** The tab currently shown, or null on a destination that is not one. */
fun NavDestination?.currentTab(): Tab? = this?.let { destination ->
    Tab.entries.firstOrNull { tab ->
        when (tab.destination) {
            Destination.Home -> destination.hasRoute<Destination.Home>()
            Destination.Planner -> destination.hasRoute<Destination.Planner>()
            Destination.Analytics -> destination.hasRoute<Destination.Analytics>()
            Destination.Profile -> destination.hasRoute<Destination.Profile>()
            else -> false
        }
    }
}
