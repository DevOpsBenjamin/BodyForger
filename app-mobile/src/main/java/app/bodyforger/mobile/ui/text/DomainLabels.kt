package app.bodyforger.mobile.ui.text

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.bodyforger.core.model.EquipmentType
import app.bodyforger.core.model.MuscleGroup
import app.bodyforger.core.model.RoutineSetType
import app.bodyforger.core.model.UnilateralSide
import app.bodyforger.core.model.WeightUnit
import app.bodyforger.core.model.WorkoutActivityCategory
import app.bodyforger.mobile.R

/**
 * How the domain vocabulary reads on screen, in the reader's language.
 *
 * The model carries identity only: `LEFT` is a side, not the letter "G". A label baked into
 * the enum made every screen French, and put a translation decision inside `core-model`,
 * where no screen exists to make it.
 */
@Composable
fun UnilateralSide.label(): String = stringResource(
    when (this) {
        UnilateralSide.NONE -> R.string.unilateral_side_none
        UnilateralSide.LEFT -> R.string.unilateral_side_left
        UnilateralSide.RIGHT -> R.string.unilateral_side_right
    }
)

/** The letter shown against a set number: `L`/`R` in English, `G`/`D` in French. */
@Composable
fun UnilateralSide.badge(): String = stringResource(
    when (this) {
        UnilateralSide.NONE -> R.string.unilateral_side_badge_none
        UnilateralSide.LEFT -> R.string.unilateral_side_badge_left
        UnilateralSide.RIGHT -> R.string.unilateral_side_badge_right
    }
)

@Composable
fun RoutineSetType.label(): String = stringResource(
    when (this) {
        RoutineSetType.NORMAL -> R.string.set_type_normal
        RoutineSetType.WARMUP -> R.string.set_type_warmup
        RoutineSetType.DROPSET -> R.string.set_type_dropset
        RoutineSetType.FAILURE -> R.string.set_type_failure
        RoutineSetType.REST_PAUSE -> R.string.set_type_rest_pause
    }
)

@Composable
fun RoutineSetType.badge(): String = stringResource(
    when (this) {
        RoutineSetType.NORMAL -> R.string.set_type_badge_normal
        RoutineSetType.WARMUP -> R.string.set_type_badge_warmup
        RoutineSetType.DROPSET -> R.string.set_type_badge_dropset
        RoutineSetType.FAILURE -> R.string.set_type_badge_failure
        RoutineSetType.REST_PAUSE -> R.string.set_type_badge_rest_pause
    }
)

@Composable
fun WeightUnit.label(): String = stringResource(
    when (this) {
        WeightUnit.KG -> R.string.weight_unit_kg
        WeightUnit.LBS -> R.string.weight_unit_lbs
    }
)

@Composable
fun MuscleGroup.label(): String = stringResource(
    when (this) {
        MuscleGroup.CHEST -> R.string.muscle_group_chest
        MuscleGroup.BACK -> R.string.muscle_group_back
        MuscleGroup.SHOULDERS -> R.string.muscle_group_shoulders
        MuscleGroup.BICEPS -> R.string.muscle_group_biceps
        MuscleGroup.TRICEPS -> R.string.muscle_group_triceps
        MuscleGroup.QUADRICEPS -> R.string.muscle_group_quadriceps
        MuscleGroup.HAMSTRINGS -> R.string.muscle_group_hamstrings
        MuscleGroup.GLUTES -> R.string.muscle_group_glutes
        MuscleGroup.CALVES -> R.string.muscle_group_calves
        MuscleGroup.ABS -> R.string.muscle_group_abs
        MuscleGroup.FULL_BODY -> R.string.muscle_group_full_body
    }
)

@Composable
fun EquipmentType.label(): String = stringResource(
    when (this) {
        EquipmentType.BARBELL -> R.string.equipment_barbell
        EquipmentType.DUMBBELL -> R.string.equipment_dumbbell
        EquipmentType.CABLE -> R.string.equipment_cable
        EquipmentType.MACHINE -> R.string.equipment_machine
        EquipmentType.MACHINE_CONVERGENT -> R.string.equipment_machine_convergent
        EquipmentType.BODYWEIGHT -> R.string.equipment_bodyweight
        EquipmentType.KETTLEBELL -> R.string.equipment_kettlebell
        EquipmentType.OTHER -> R.string.equipment_other
    }
)

@Composable
fun WorkoutActivityCategory.label(): String = stringResource(
    when (this) {
        WorkoutActivityCategory.STRENGTH_TRAINING -> R.string.activity_category_strength_training
        WorkoutActivityCategory.ELLIPTICAL -> R.string.activity_category_elliptical
        WorkoutActivityCategory.STATIONARY_BIKING -> R.string.activity_category_stationary_biking
        WorkoutActivityCategory.TREADMILL_RUNNING -> R.string.activity_category_treadmill_running
        WorkoutActivityCategory.TREADMILL_WALKING -> R.string.activity_category_treadmill_walking
        WorkoutActivityCategory.ROWING_MACHINE -> R.string.activity_category_rowing_machine
        WorkoutActivityCategory.STRETCHING -> R.string.activity_category_stretching
        WorkoutActivityCategory.HIIT -> R.string.activity_category_hiit
        WorkoutActivityCategory.CALISTHENICS -> R.string.activity_category_calisthenics
        WorkoutActivityCategory.OTHER -> R.string.activity_category_other
    }
)
