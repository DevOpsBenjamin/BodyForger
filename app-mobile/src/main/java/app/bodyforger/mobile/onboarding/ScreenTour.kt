package app.bodyforger.mobile.onboarding

import androidx.annotation.StringRes
import app.bodyforger.mobile.R
import app.bodyforger.mobile.navigation.Destination
import app.bodyforger.mobile.navigation.Tab

/**
 * The version of the tour as written here.
 *
 * Raising it offers the tour again to everyone, including someone who has already been
 * through an older one — which is the point of storing a number rather than a flag. It is
 * counted apart from [SETUP_VERSION]: gaining a stop is no reason to ask again for a name.
 */
const val TOUR_VERSION = 1

/**
 * One stop of the screen tour: a screen, and a sentence about what it is for.
 *
 * The tour drives the navigation itself — the athlete presses Next and the app changes tab
 * under the card. Asking for the real tap would teach the gesture, but a tap landing beside
 * the target would have to be either swallowed or chased, and neither reads as a tour.
 *
 * The card sits at the centre of the screen rather than against the element it describes.
 * A cut-out anchored on a component's bounds is the prettier of the two and the more
 * fragile: it has to measure a target that a reworked layout may no longer place, and it
 * fails the day someone moves a card. Centre costs nothing and cannot break.
 */
enum class TourStop(
    val destination: Destination,
    val tab: Tab?,
    @StringRes val titleRes: Int,
    @StringRes val bodyRes: Int
) {
    HOME(Destination.Home, Tab.HOME, R.string.tour_home_title, R.string.tour_home_body),
    PLANNER(Destination.Planner, Tab.PLANNER, R.string.tour_planner_title, R.string.tour_planner_body),
    ANALYTICS(Destination.Analytics, Tab.ANALYTICS, R.string.tour_analytics_title, R.string.tour_analytics_body),
    PROFILE(Destination.Profile, Tab.PROFILE, R.string.tour_profile_title, R.string.tour_profile_body),

    /** Settings is reached by the gear, not by the bar, so this stop carries no tab. */
    SETTINGS(Destination.Settings(), null, R.string.tour_settings_title, R.string.tour_settings_body);

    val isFirst: Boolean get() = ordinal == 0

    val isLast: Boolean get() = ordinal == entries.lastIndex

    /** The stop after this one, or null when the tour is over. */
    fun next(): TourStop? = entries.getOrNull(ordinal + 1)

    /** The stop before this one, or null at the start — for reading one again. */
    fun previous(): TourStop? = entries.getOrNull(ordinal - 1)
}
