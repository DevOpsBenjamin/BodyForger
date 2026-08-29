package app.bodyforger.core.ble.huawei

import app.bodyforger.core.ble.AthleteInstruction
import app.bodyforger.core.ble.SessionPhase
import app.bodyforger.core.model.ElectrodeCount

/**
 * The Haige weigh-in sequence, mode 2 — `docs/BLE_PROTOCOL.md` §6.
 */
object HuaweiWeighInSequence {

    /**
     * Steps for a given model.
     *
     * Stepping on and gripping the handle form one step: separating them would have the
     * handle released before the reading.
     */
    fun stepsFor(model: HuaweiScaleModel): List<HuaweiSessionStep> = buildList {
        add(
            HuaweiSessionStep(
                phase = SessionPhase.DISCOVERING,
                instructions = listOf(AthleteInstruction.TAP_SCALE_TO_WAKE),
                detail = "Réveil de la balance et scan ciblé"
            )
        )
        add(
            HuaweiSessionStep(
                phase = SessionPhase.PREPARING,
                instructions = listOf(AthleteInstruction.STAY_OFF_PLATFORM),
                detail = "Handshake chiffré (0x21, 0x25, 0x29)"
            )
        )
        add(HuaweiSessionStep(SessionPhase.PREPARING, detail = "Synchronisation de l'heure (0x52)"))
        add(HuaweiSessionStep(SessionPhase.PREPARING, detail = "Transmission du profil utilisateur (0x31)"))
        add(HuaweiSessionStep(SessionPhase.PREPARING, detail = "Armement du streaming BIA (0x97)"))
        add(HuaweiSessionStep(SessionPhase.AWAITING_ATHLETE, stepOnInstructions(model), "Balance prête"))
        add(
            HuaweiSessionStep(
                phase = SessionPhase.MEASURING,
                detail = "Stabilisation, relevé (0x97) puis acquittement (0x31 type=2)"
            )
        )
    }

    /** What the athlete does when stepping on; the handle only where there is one. */
    fun stepOnInstructions(model: HuaweiScaleModel): List<AthleteInstruction> = buildList {
        add(AthleteInstruction.STEP_ON_BAREFOOT)
        if (model.capability?.electrodeCount == ElectrodeCount.EIGHT) {
            add(AthleteInstruction.GRIP_HANDLE)
        }
    }
}
