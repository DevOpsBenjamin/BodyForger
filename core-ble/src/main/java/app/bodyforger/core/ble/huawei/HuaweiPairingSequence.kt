package app.bodyforger.core.ble.huawei

import app.bodyforger.core.ble.AthleteInstruction
import app.bodyforger.core.ble.PairingRequirement
import app.bodyforger.core.ble.SessionPhase
import app.bodyforger.core.model.ElectrodeCount

/**
 * The Haige pairing sequence, mode 1 — `docs/BLE_PROTOCOL.md` §5.
 */
object HuaweiPairingSequence {

    val requirement: PairingRequirement = PairingRequirement.WEIGH_IN_REQUIRED

    /** Steps for a given model. */
    fun stepsFor(model: HuaweiScaleModel): List<HuaweiSessionStep> = buildList {
        add(HuaweiSessionStep(SessionPhase.DISCOVERING, detail = "Connexion à la balance repérée"))
        add(
            HuaweiSessionStep(
                phase = SessionPhase.PREPARING,
                instructions = listOf(AthleteInstruction.STAY_OFF_PLATFORM),
                detail = "Handshake chiffré (0x21, 0x25, 0x29)"
            )
        )
        add(HuaweiSessionStep(SessionPhase.PREPARING, detail = "Armement de l'association (0x45)"))
        add(HuaweiSessionStep(SessionPhase.PREPARING, detail = "Gravure du HUID en mémoire flash (0x2D)"))
        //
        add(
            HuaweiSessionStep(
                phase = SessionPhase.AWAITING_ATHLETE,
                instructions = listOf(AthleteInstruction.STEP_ON),
                detail = "Capture de la tare renvoyée par la balance"
            )
        )
        add(HuaweiSessionStep(SessionPhase.PREPARING, detail = "Synchronisation de l'heure (0x52)"))
        add(HuaweiSessionStep(SessionPhase.PREPARING, detail = "Transmission du profil utilisateur (0x31)"))
        add(HuaweiSessionStep(SessionPhase.PREPARING, detail = "Désarmement de l'association (0x45)"))

        add(HuaweiSessionStep(SessionPhase.MEASURING, detail = "Armement du flux BIA (0x97)"))
        add(HuaweiSessionStep(SessionPhase.MEASURING, detail = "Relevé de validation (0x97) puis acquittement (0x31 type=2)"))
    }
}
