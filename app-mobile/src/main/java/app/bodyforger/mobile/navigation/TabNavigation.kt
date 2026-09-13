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

/**
 * Moves to a screen the way the tour needs to, which is not the way the bar does.
 *
 * [switchTab] saves the stack it unwinds and restores it on the way back, so that a tab
 * remembers where it was. That is right for a tab and wrong for the tour: the tour ends on
 * Settings, reached from whichever tab it was standing on, and saving that stack teaches the
 * bar that the tab in question leads to Settings — tapping Profile afterwards would land on
 * Settings instead of the profile.
 *
 * So the tour unwinds to the start destination and stacks nothing on a tab, saving and
 * restoring nothing.
 */
fun NavHostController.showForTour(destination: Destination) {
    navigate(destination) {
        popUpTo(graph.findStartDestination().id)
        launchSingleTop = true
    }
}

/** Leaves the tour where the app would have opened, with nothing of the tour left behind. */
fun NavHostController.leaveTour() {
    navigate(Destination.Home) {
        popUpTo(graph.findStartDestination().id) { inclusive = true }
        launchSingleTop = true
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
