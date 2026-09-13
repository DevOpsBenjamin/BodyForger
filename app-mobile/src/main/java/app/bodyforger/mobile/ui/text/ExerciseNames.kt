package app.bodyforger.mobile.ui.text

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.bodyforger.core.model.Exercise
import app.bodyforger.mobile.R

/**
 * Resolves a built-in exercise's name key to the athlete's locale.
 *
 * The table is generated from the seed in `core-database/.../data`, one entry per
 * `nameKey`. A `Map` rather than `Resources.getIdentifier`: the lookup is compile-checked,
 * and resource shrinking cannot strip what the map references by name.
 */
object ExerciseNames {

    private val BY_KEY: Map<String, Int> = mapOf(
        "ex_hanging_leg_raise" to R.string.ex_hanging_leg_raise,
        "ex_lying_leg_raise" to R.string.ex_lying_leg_raise,
        "ex_cable_crunch" to R.string.ex_cable_crunch,
        "ex_seated_machine_crunch" to R.string.ex_seated_machine_crunch,
        "ex_floor_crunch" to R.string.ex_floor_crunch,
        "ex_plank" to R.string.ex_plank,
        "ex_side_plank" to R.string.ex_side_plank,
        "ex_cable_pallof_press" to R.string.ex_cable_pallof_press,
        "ex_russian_twist" to R.string.ex_russian_twist,
        "ex_ab_wheel_rollout" to R.string.ex_ab_wheel_rollout,
        "ex_burpees" to R.string.ex_burpees,
        "ex_elliptical_trainer" to R.string.ex_elliptical_trainer,
        "ex_treadmill_incline_walk" to R.string.ex_treadmill_incline_walk,
        "ex_treadmill_run" to R.string.ex_treadmill_run,
        "ex_stretching_and_general_mobility" to R.string.ex_stretching_and_general_mobility,
        "ex_foam_rolling" to R.string.ex_foam_rolling,
        "ex_back_squat" to R.string.ex_back_squat,
        "ex_front_squat" to R.string.ex_front_squat,
        "ex_hack_squat_machine" to R.string.ex_hack_squat_machine,
        "ex_smith_machine_squat" to R.string.ex_smith_machine_squat,
        "ex_belt_squat_machine" to R.string.ex_belt_squat_machine,
        "ex_45_incline_leg_press" to R.string.ex_45_incline_leg_press,
        "ex_horizontal_leg_press" to R.string.ex_horizontal_leg_press,
        "ex_leg_extension_machine" to R.string.ex_leg_extension_machine,
        "ex_single_leg_extension_machine" to R.string.ex_single_leg_extension_machine,
        "ex_bulgarian_split_squat" to R.string.ex_bulgarian_split_squat,
        "ex_walking_lunges" to R.string.ex_walking_lunges,
        "ex_stationary_bike_cycling" to R.string.ex_stationary_bike_cycling,
        "ex_seated_leg_curl_machine" to R.string.ex_seated_leg_curl_machine,
        "ex_lying_leg_curl_machine" to R.string.ex_lying_leg_curl_machine,
        "ex_single_leg_curl_machine" to R.string.ex_single_leg_curl_machine,
        "ex_barbell_romanian_deadlift" to R.string.ex_barbell_romanian_deadlift,
        "ex_dumbbell_romanian_deadlift" to R.string.ex_dumbbell_romanian_deadlift,
        "ex_barbell_hip_thrust" to R.string.ex_barbell_hip_thrust,
        "ex_hip_thrust_machine" to R.string.ex_hip_thrust_machine,
        "ex_hip_abduction_machine" to R.string.ex_hip_abduction_machine,
        "ex_hip_adduction_machine" to R.string.ex_hip_adduction_machine,
        "ex_cable_glute_kickback" to R.string.ex_cable_glute_kickback,
        "ex_kettlebell_swing" to R.string.ex_kettlebell_swing,
        "ex_stairmaster_stair_climber" to R.string.ex_stairmaster_stair_climber,
        "ex_standing_calf_raise_machine" to R.string.ex_standing_calf_raise_machine,
        "ex_seated_calf_raise_machine" to R.string.ex_seated_calf_raise_machine,
        "ex_leg_press_calf_raise" to R.string.ex_leg_press_calf_raise,
        "ex_jump_rope" to R.string.ex_jump_rope,
        "ex_pull_up_pronated_grip" to R.string.ex_pull_up_pronated_grip,
        "ex_chin_up_supinated_grip" to R.string.ex_chin_up_supinated_grip,
        "ex_pull_up_neutral_grip" to R.string.ex_pull_up_neutral_grip,
        "ex_dead_hang_pronated_grip" to R.string.ex_dead_hang_pronated_grip,
        "ex_dead_hang_supinated_grip" to R.string.ex_dead_hang_supinated_grip,
        "ex_dead_hang_neutral_grip" to R.string.ex_dead_hang_neutral_grip,
        "ex_assisted_pull_up_machine_pronated_grip" to R.string.ex_assisted_pull_up_machine_pronated_grip,
        "ex_assisted_chin_up_machine_supinated_grip" to R.string.ex_assisted_chin_up_machine_supinated_grip,
        "ex_assisted_pull_up_machine_neutral_grip" to R.string.ex_assisted_pull_up_machine_neutral_grip,
        "ex_deadlift" to R.string.ex_deadlift,
        "ex_trap_bar_deadlift" to R.string.ex_trap_bar_deadlift,
        "ex_wide_grip_lat_pulldown" to R.string.ex_wide_grip_lat_pulldown,
        "ex_neutral_grip_lat_pulldown" to R.string.ex_neutral_grip_lat_pulldown,
        "ex_lat_pulldown_machine" to R.string.ex_lat_pulldown_machine,
        "ex_converging_lat_pulldown" to R.string.ex_converging_lat_pulldown,
        "ex_single_arm_cable_pulldown" to R.string.ex_single_arm_cable_pulldown,
        "ex_pull_over_poulie_haute" to R.string.ex_pull_over_poulie_haute,
        "ex_seated_cable_row" to R.string.ex_seated_cable_row,
        "ex_wide_grip_seated_row" to R.string.ex_wide_grip_seated_row,
        "ex_single_arm_cable_row" to R.string.ex_single_arm_cable_row,
        "ex_bent_over_barbell_row" to R.string.ex_bent_over_barbell_row,
        "ex_single_arm_dumbbell_row" to R.string.ex_single_arm_dumbbell_row,
        "ex_t_bar_row" to R.string.ex_t_bar_row,
        "ex_seated_row_machine" to R.string.ex_seated_row_machine,
        "ex_converging_row_machine" to R.string.ex_converging_row_machine,
        "ex_45_back_extension" to R.string.ex_45_back_extension,
        "ex_seated_lower_back_machine" to R.string.ex_seated_lower_back_machine,
        "ex_rowing_machine" to R.string.ex_rowing_machine,
        "ex_barbell_biceps_curl" to R.string.ex_barbell_biceps_curl,
        "ex_ez_bar_preacher_curl" to R.string.ex_ez_bar_preacher_curl,
        "ex_dumbbell_biceps_curl" to R.string.ex_dumbbell_biceps_curl,
        "ex_incline_dumbbell_curl" to R.string.ex_incline_dumbbell_curl,
        "ex_dumbbell_hammer_curl" to R.string.ex_dumbbell_hammer_curl,
        "ex_single_arm_dumbbell_preacher_curl" to R.string.ex_single_arm_dumbbell_preacher_curl,
        "ex_rope_cable_curl" to R.string.ex_rope_cable_curl,
        "ex_single_arm_low_cable_curl" to R.string.ex_single_arm_low_cable_curl,
        "ex_biceps_curl_machine" to R.string.ex_biceps_curl_machine,
        "ex_converging_biceps_curl" to R.string.ex_converging_biceps_curl,
        "ex_barbell_reverse_curl" to R.string.ex_barbell_reverse_curl,
        "ex_bench_press" to R.string.ex_bench_press,
        "ex_incline_bench_press" to R.string.ex_incline_bench_press,
        "ex_decline_bench_press" to R.string.ex_decline_bench_press,
        "ex_close_grip_bench_press" to R.string.ex_close_grip_bench_press,
        "ex_smith_machine_bench_press" to R.string.ex_smith_machine_bench_press,
        "ex_smith_machine_incline_press" to R.string.ex_smith_machine_incline_press,
        "ex_dumbbell_bench_press" to R.string.ex_dumbbell_bench_press,
        "ex_incline_dumbbell_press" to R.string.ex_incline_dumbbell_press,
        "ex_dumbbell_fly" to R.string.ex_dumbbell_fly,
        "ex_chest_press_machine" to R.string.ex_chest_press_machine,
        "ex_incline_press_machine" to R.string.ex_incline_press_machine,
        "ex_converging_chest_press" to R.string.ex_converging_chest_press,
        "ex_converging_incline_press" to R.string.ex_converging_incline_press,
        "ex_pec_deck_machine" to R.string.ex_pec_deck_machine,
        "ex_cable_crossover" to R.string.ex_cable_crossover,
        "ex_low_cable_fly" to R.string.ex_low_cable_fly,
        "ex_bodyweight_dips" to R.string.ex_bodyweight_dips,
        "ex_assisted_dips_machine" to R.string.ex_assisted_dips_machine,
        "ex_seated_dips_machine" to R.string.ex_seated_dips_machine,
        "ex_push_ups" to R.string.ex_push_ups,
        "ex_decline_push_ups" to R.string.ex_decline_push_ups,
        "ex_overhead_press" to R.string.ex_overhead_press,
        "ex_dumbbell_shoulder_press" to R.string.ex_dumbbell_shoulder_press,
        "ex_shoulder_press_machine" to R.string.ex_shoulder_press_machine,
        "ex_converging_shoulder_press" to R.string.ex_converging_shoulder_press,
        "ex_smith_machine_shoulder_press" to R.string.ex_smith_machine_shoulder_press,
        "ex_dumbbell_lateral_raise" to R.string.ex_dumbbell_lateral_raise,
        "ex_single_arm_cable_lateral_raise" to R.string.ex_single_arm_cable_lateral_raise,
        "ex_lateral_raise_machine" to R.string.ex_lateral_raise_machine,
        "ex_cable_face_pull" to R.string.ex_cable_face_pull,
        "ex_reverse_pec_deck" to R.string.ex_reverse_pec_deck,
        "ex_bent_over_dumbbell_reverse_fly" to R.string.ex_bent_over_dumbbell_reverse_fly,
        "ex_single_arm_cable_reverse_fly" to R.string.ex_single_arm_cable_reverse_fly,
        "ex_dumbbell_front_raise" to R.string.ex_dumbbell_front_raise,
        "ex_skull_crusher" to R.string.ex_skull_crusher,
        "ex_close_grip_bench_press_triceps" to R.string.ex_close_grip_bench_press_triceps,
        "ex_rope_triceps_pushdown" to R.string.ex_rope_triceps_pushdown,
        "ex_bar_triceps_pushdown" to R.string.ex_bar_triceps_pushdown,
        "ex_overhead_cable_triceps_extension" to R.string.ex_overhead_cable_triceps_extension,
        "ex_single_arm_cable_triceps_extension" to R.string.ex_single_arm_cable_triceps_extension,
        "ex_parallel_bar_triceps_dips" to R.string.ex_parallel_bar_triceps_dips,
    )

    @StringRes
    fun resourceFor(nameKey: String): Int? = BY_KEY[nameKey]
}

/**
 * The name to show: localised for a built-in exercise, verbatim for one the athlete created.
 *
 * Falls back to the stored English name whenever the key is unknown — a catalogue entry
 * removed since, or a backup restored from a newer version.
 */
@Composable
fun Exercise.displayName(): String =
    nameKey?.let { key -> ExerciseNames.resourceFor(key)?.let { stringResource(it) } } ?: name

/** Same resolution outside composition, for sorting and filtering. */
fun Exercise.displayName(context: Context): String =
    nameKey?.let { key -> ExerciseNames.resourceFor(key)?.let { context.getString(it) } } ?: name
