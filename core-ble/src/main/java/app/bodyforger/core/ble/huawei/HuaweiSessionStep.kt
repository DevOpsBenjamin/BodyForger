package app.bodyforger.core.ble.huawei

import app.bodyforger.core.ble.AthleteInstruction
import app.bodyforger.core.ble.SessionPhase

/**
 * Une étape d'une séquence Haige, telle que le pilote la découpe.
 *
 * Une étape correspond à un écran, et peut donc demander plusieurs gestes simultanés. Le
 * cœur ne voit jamais ce type — il reçoit la phase, les consignes et la progression.
 */
data class HuaweiSessionStep(
    val phase: SessionPhase,
    val instructions: List<AthleteInstruction> = emptyList(),
    /** Libellé de diagnostic, jamais destiné à l'interface : non traduit. */
    val detail: String
)
