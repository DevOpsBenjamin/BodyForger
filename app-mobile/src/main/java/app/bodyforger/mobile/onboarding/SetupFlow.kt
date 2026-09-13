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
 * filling the profile does not mean the scale is in the room right now.
 */
enum class SetupStep(
    @StringRes val titleRes: Int,
    @StringRes val whyRes: Int,
    /**
     * What the button says when the step is left as it stands, or null for a step that
     * cannot be left undone because it has an answer from the start — units are kilograms
     * and centimetres until said otherwise, and that is already an answer.
     */
    @StringRes val skipRes: Int?
) {
    UNITS(R.string.setup_units_title, R.string.setup_units_why, null),
    NAME(R.string.setup_name_title, R.string.setup_name_why, R.string.setup_skip_step),
    PROFILE(R.string.setup_profile_title, R.string.setup_profile_why, R.string.setup_skip_step),
    SCALE(R.string.setup_scale_title, R.string.setup_scale_why, R.string.setup_no_scale),
    GOALS(R.string.setup_goals_title, R.string.setup_goals_why, R.string.setup_skip_step);

    val isLast: Boolean get() = ordinal == entries.lastIndex

    /** The step after this one, or null when the flow is over. */
    fun next(): SetupStep? = entries.getOrNull(ordinal + 1)
}
