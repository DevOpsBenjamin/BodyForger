package app.bodyforger.core.model

import java.time.LocalDate
import java.util.UUID

/**
 * A milestone the athlete is working towards — *palier* in `CONTEXT.md`.
 *
 * Only [targetMassKg] is required. A goal that also names a body fat threshold is stricter in
 * two ways: both thresholds must be crossed, and only weigh-ins that measured body fat count
 * towards it at all. A goal without one accepts every weigh-in, including the mass-only
 * readings a scale returns when the handle stays in its cradle.
 *
 * [horizonDate] is a direction, never a deadline: nothing fails when it passes. It is stored as
 * a date even when the athlete entered a duration — "in six months" fills the field, it does
 * not hold it, or the horizon would retreat as fast as they approached it.
 *
 * A goal is never invalidated automatically. [validatedOn] records the day it was crossed, and
 * that stays true however the trend moves afterwards.
 */
data class BodyGoal(
    val id: String = UUID.randomUUID().toString(),
    val targetMassKg: Double,
    val targetBodyFatPercentage: Double? = null,

    /**
     * Where the athlete stood when this goal started counting, or `null` until they weigh in.
     *
     * A goal can be set before ever standing on a scale — an intention does not wait on a
     * measurement. It simply has no direction yet, and the first weigh-in that follows fills
     * this in through [startingFrom]. Until then the goal shows its target and nothing else:
     * no progress, no direction, no projection.
     *
     * Direction is read from here and never from the current trend. Reading it from the
     * current one would flip the moment the goal is reached — a target below you is a loss
     * until you pass it, at which point it would become a gain and the goal would stop
     * counting as crossed exactly when it was.
     */
    val startingMassKg: Double? = null,
    val startingBodyFatPercentage: Double? = null,

    val horizonDate: LocalDate? = null,
    val createdOn: LocalDate,
    val validatedOn: LocalDate? = null
) {
    init {
        require(targetMassKg > 0.0) { "Invalid target mass: $targetMassKg kg" }
        require(startingMassKg == null || startingMassKg > 0.0) {
            "Invalid starting mass: $startingMassKg kg"
        }
        require(targetBodyFatPercentage == null || targetBodyFatPercentage in 0.0..100.0) {
            "Invalid target body fat: $targetBodyFatPercentage %"
        }
    }

    val isValidated: Boolean get() = validatedOn != null

    /** True once a weigh-in has given this goal a direction to be judged against. */
    val hasStarted: Boolean get() = startingMassKg != null

    /**
     * Anchors a goal set before any weigh-in to the first one that follows.
     *
     * Returns the goal unchanged once anchored: the starting point is captured once, or the
     * direction would drift with every reading.
     */
    fun startingFrom(massKg: Double, bodyFatPercentage: Double?): BodyGoal =
        if (hasStarted) this
        else copy(startingMassKg = massKg, startingBodyFatPercentage = bodyFatPercentage)

    /** True when this goal only counts weigh-ins that measured body composition. */
    val requiresBodyFat: Boolean get() = targetBodyFatPercentage != null

    /**
     * Whether a week's medians sit on the far side of both thresholds.
     *
     * Mass and body fat are judged independently, so gaining mass while losing fat is an
     * ordinary goal rather than a contradiction. A goal that names a body fat threshold is not
     * crossed by a week that never measured it — such a week does not count against the goal
     * either, it simply is not one of its weeks.
     */
    fun isCrossedBy(massKg: Double, bodyFatPercentage: Double?): Boolean {
        val startingMass = startingMassKg ?: return false
        if (!reached(massKg, targetMassKg, startingMass)) return false

        val target = targetBodyFatPercentage ?: return true
        val measured = bodyFatPercentage ?: return false
        val start = startingBodyFatPercentage ?: return false
        return reached(measured, target, start)
    }

    /**
     * How far along, from 0 at the starting point to 1 at the threshold, on mass.
     *
     * `null` when the goal was set at the target it already sat on: there is no distance to
     * cover and no share of it to report. Overshooting is not capped — passing the target is
     * worth knowing.
     */
    fun massProgressFrom(massKg: Double): Double? {
        val start = startingMassKg ?: return null
        val distance = targetMassKg - start
        if (distance == 0.0) return null
        return (massKg - start) / distance
    }

    private fun reached(value: Double, target: Double, from: Double): Boolean =
        if (from >= target) value <= target else value >= target
}
