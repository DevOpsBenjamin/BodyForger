package app.bodyforger.core.model

/**
 * Activity category of a Google Health Connect session.
 *
 * Lets the workout engine split and export cardio warm-up blocks separately from strength
 * work.
 */
enum class WorkoutActivityCategory(
    val googleSessionTypeId: Int
) {
    STRENGTH_TRAINING(56), // EXERCISE_TYPE_STRENGTH_TRAINING
    ELLIPTICAL(25),        // EXERCISE_TYPE_ELLIPTICAL
    STATIONARY_BIKING(8),  // EXERCISE_TYPE_BIKING_STATIONARY
    TREADMILL_RUNNING(57), // EXERCISE_TYPE_RUNNING_TREADMILL
    TREADMILL_WALKING(79), // EXERCISE_TYPE_WALKING
    ROWING_MACHINE(53),    // EXERCISE_TYPE_ROWING_MACHINE
    STRETCHING(64),        // EXERCISE_TYPE_STRETCHING
    HIIT(36),              // EXERCISE_TYPE_HIGH_INTENSITY_INTERVAL_TRAINING
    CALISTHENICS(13),      // EXERCISE_TYPE_CALISTHENICS
    OTHER(0)               // EXERCISE_TYPE_OTHER_WORKOUT
}
