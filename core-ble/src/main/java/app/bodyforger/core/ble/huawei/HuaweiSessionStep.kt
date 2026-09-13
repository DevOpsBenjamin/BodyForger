package app.bodyforger.core.ble.huawei

import app.bodyforger.core.ble.AthleteInstruction
import app.bodyforger.core.ble.SessionPhase

/**
 * One step of a Haige sequence, as the driver cuts it.
 *
 * A step is one screen, so it may ask for several simultaneous actions. The core never sees
 * this type.
 */
data class HuaweiSessionStep(
    val phase: SessionPhase,
    val instructions: List<AthleteInstruction> = emptyList(),
    /** Diagnostic label, never shown to the athlete: untranslated. */
    val detail: String
)
