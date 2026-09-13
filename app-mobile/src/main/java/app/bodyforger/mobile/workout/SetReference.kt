package app.bodyforger.mobile.workout

import app.bodyforger.core.model.UnilateralSide
import app.bodyforger.core.model.WorkoutSet

/**
 * What makes a set the same set from one session to the next.
 *
 * Not its identifier, which is new every session, but its place: which exercise, which set of
 * that exercise, and which side for a unilateral one. The left side of the third set of a
 * squat is comparable only to the left side of the third set of a squat.
 */
data class SetReference(
    val exerciseId: String,
    val setIndex: Int,
    val side: UnilateralSide
) {
    companion object {
        fun of(set: WorkoutSet) = SetReference(set.exerciseId, set.setIndex, set.side)
    }
}
