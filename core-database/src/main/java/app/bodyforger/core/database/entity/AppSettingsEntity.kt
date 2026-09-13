package app.bodyforger.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Installation-wide preferences: one row.
 *
 * Kept apart from the athlete's identity — these are app choices, not who the athlete is.
 * A null field means "no choice made yet"; the app then applies its own default.
 */
@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    val id: Int = SINGLETON_ID,
    /** Chosen BIA engine id (see `ModelSelector`); null means the default engine. */
    val biaEngineId: String? = null,
    /**
     * Weight unit a new exercise starts from, as a [app.bodyforger.core.model.WeightUnit] name;
     * null means kilograms. An exercise may still carry another one — a machine labelled in
     * pounds stays in pounds — so this is a starting point, not a constraint.
     */
    val defaultWeightUnit: String? = null,
    /**
     * Unit a height is written and read in, as a [app.bodyforger.core.model.HeightUnit] name;
     * null means centimetres. The height itself is stored in centimetres whatever this says.
     */
    val defaultHeightUnit: String? = null
) {
    companion object {
        const val SINGLETON_ID = 1
    }
}
