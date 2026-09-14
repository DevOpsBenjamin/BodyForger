package app.bodyforger.mobile.ui.components

import app.bodyforger.core.bia.ModelSelector
import app.bodyforger.mobile.R

/** Which section is unfolded. Only one at a time: they are steps, not a list to browse. */
enum class SettingsSectionType {
    NONE,
    ATHLETE,
    BIA,
    SCALE,
    GOALS,
    UNIT,
    ENGINE,
    ONBOARDING;

    fun toggled(tapped: SettingsSectionType): SettingsSectionType =
        if (this == tapped) NONE else tapped
}

fun engineLabelRes(id: String): Int = when (id) {
    ModelSelector.FORGEFIT_PRIVATE -> R.string.settings_engine_forgefit_private
    else -> R.string.settings_engine_forgefit_mit
}
