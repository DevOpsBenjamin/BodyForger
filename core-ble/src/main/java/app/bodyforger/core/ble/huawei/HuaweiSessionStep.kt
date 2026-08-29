package app.bodyforger.core.ble.huawei

import app.bodyforger.core.ble.AthleteInstruction
import app.bodyforger.core.ble.SessionPhase

/**
 * Une étape d'une séquence Haige, telle que le pilote la découpe.
 *
 * L'appairage comme la pesée sont multi-étapes ; seule leur composition diffère. Le cœur ne
 * voit jamais ce type — il reçoit la phase, la consigne et la progression.
 */
data class HuaweiSessionStep(
    val phase: SessionPhase,
    val instruction: AthleteInstruction? = null,
    /** Libellé de diagnostic, jamais destiné à l'interface : non traduit. */
    val detail: String
)
