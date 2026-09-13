package app.bodyforger.mobile.onboarding

import androidx.annotation.StringRes
import app.bodyforger.mobile.R

/**
 * The version of the setup flow as written here — see [TOUR_VERSION] for what the number is
 * for. A step added to the flow raises it; the tour's own counter is left alone.
 */
const val SETUP_VERSION = 1

/**
 * The questions a fresh install asks, in the order it asks them.
 *
 * The order is a dependency chain rather than a matter of taste. Units come first because
 * every figure asked for afterwards is written in them — a height typed before the athlete
 * said inches is a height in the wrong unit. The measurement profile gates weighing: the
 * scale computes its own figures from it. Pairing engraves that profile into the scale, so it
 * cannot come before. A goal is only readable against a weigh-in, so it comes last.
 *
 * Every step is skippable, [SCALE] independently of [PROFILE]: saying you own a scale and
 * filling the profile does not mean the scale is in the room right now. And every step can be
 * gone back to, since the answer to one of them is sometimes only obvious after seeing the
 * next.
 */
enum class SetupStep(
    @StringRes val titleRes: Int,
    @StringRes val whyRes: Int
) {
    UNITS(R.string.setup_units_title, R.string.setup_units_why),
    NAME(R.string.setup_name_title, R.string.setup_name_why),
    PROFILE(R.string.setup_profile_title, R.string.setup_profile_why),
    SCALE(R.string.setup_scale_title, R.string.setup_scale_why),
    GOALS(R.string.setup_goals_title, R.string.setup_goals_why);

    val isFirst: Boolean get() = ordinal == 0

    val isLast: Boolean get() = ordinal == entries.lastIndex

    /** The step after this one, or null when the flow is over. */
    fun next(): SetupStep? = entries.getOrNull(ordinal + 1)

    /** The step before this one, or null at the start of the flow. */
    fun previous(): SetupStep? = entries.getOrNull(ordinal - 1)
}
