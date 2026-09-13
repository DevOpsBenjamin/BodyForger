package app.bodyforger.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.bodyforger.core.model.BodyGoal
import java.time.LocalDate

/**
 * A milestone as it is stored.
 *
 * Dates are held as ISO strings: they are calendar days, not instants, and a goal set on the
 * 1st of June stays the 1st of June whatever timezone the athlete reads it in.
 *
 * The nullable columns are the ones the domain lets an athlete leave empty — a body fat
 * threshold, a horizon, a starting point not yet anchored by a weigh-in — plus the validation
 * date, which is absent until the goal is crossed.
 */
@Entity(tableName = "body_goals")
data class BodyGoalEntity(
    @PrimaryKey
    val id: String,
    val targetMassKg: Double,
    val targetBodyFatPercentage: Double?,
    val startingMassKg: Double?,
    val startingBodyFatPercentage: Double?,
    val horizonDateIso: String?,
    val createdOnIso: String,
    val validatedOnIso: String?
)

fun BodyGoalEntity.toDomain(): BodyGoal = BodyGoal(
    id = id,
    targetMassKg = targetMassKg,
    targetBodyFatPercentage = targetBodyFatPercentage,
    startingMassKg = startingMassKg,
    startingBodyFatPercentage = startingBodyFatPercentage,
    horizonDate = horizonDateIso.toLocalDateOrNull(),
    createdOn = createdOnIso.toLocalDateOrNull() ?: LocalDate.now(),
    validatedOn = validatedOnIso.toLocalDateOrNull()
)

fun BodyGoal.toEntity(): BodyGoalEntity = BodyGoalEntity(
    id = id,
    targetMassKg = targetMassKg,
    targetBodyFatPercentage = targetBodyFatPercentage,
    startingMassKg = startingMassKg,
    startingBodyFatPercentage = startingBodyFatPercentage,
    horizonDateIso = horizonDate?.toString(),
    createdOnIso = createdOn.toString(),
    validatedOnIso = validatedOn?.toString()
)

/** An unreadable stored date reads as absent rather than crashing a screen that lists goals. */
private fun String?.toLocalDateOrNull(): LocalDate? =
    this?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
