package app.bodyforger.core.model

/**
 * Activity category of a Google Health Connect session.
 *
 * Lets the workout engine split and export cardio warm-up blocks separately from strength
 * work.
 */
enum class WorkoutActivityCategory(
    val displayName: String,
    val googleSessionTypeId: Int
) {
    STRENGTH_TRAINING("Musculation", 56),        // EXERCISE_TYPE_STRENGTH_TRAINING
    ELLIPTICAL("Vélo Elliptique", 25),            // EXERCISE_TYPE_ELLIPTICAL
    STATIONARY_BIKING("Vélo Stationnaire", 8),    // EXERCISE_TYPE_BIKING_STATIONARY
    TREADMILL_RUNNING("Course sur Tapis", 57),    // EXERCISE_TYPE_RUNNING_TREADMILL
    TREADMILL_WALKING("Marche sur Tapis", 79),    // EXERCISE_TYPE_WALKING
    ROWING_MACHINE("Rameur", 53),                 // EXERCISE_TYPE_ROWING_MACHINE
    STRETCHING("Étirements & Mobilité", 64),      // EXERCISE_TYPE_STRETCHING
    HIIT("HIIT & Circuit", 36),                   // EXERCISE_TYPE_HIGH_INTENSITY_INTERVAL_TRAINING
    CALISTHENICS("Poids du corps", 13),           // EXERCISE_TYPE_CALISTHENICS
    OTHER("Autre", 0)                             // EXERCISE_TYPE_OTHER_WORKOUT
}
