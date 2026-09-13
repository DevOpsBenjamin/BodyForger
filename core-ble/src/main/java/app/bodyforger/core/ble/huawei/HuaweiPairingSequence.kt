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
        add(HuaweiSessionStep(SessionPhase.DISCOVERING, detail = "Connecting to the spotted scale"))
        add(
            HuaweiSessionStep(
                phase = SessionPhase.PREPARING,
                instructions = listOf(AthleteInstruction.STAY_OFF_PLATFORM),
                detail = "Encrypted handshake (0x21, 0x25, 0x29)"
            )
        )
        add(HuaweiSessionStep(SessionPhase.PREPARING, detail = "Arming association mode (0x45)"))
        add(HuaweiSessionStep(SessionPhase.PREPARING, detail = "Engraving the HUID into flash (0x2D)"))
        //
        add(
            HuaweiSessionStep(
                phase = SessionPhase.AWAITING_ATHLETE,
                instructions = listOf(AthleteInstruction.STEP_ON),
                detail = "Capturing the tare the scale returns"
            )
        )
        add(HuaweiSessionStep(SessionPhase.PREPARING, detail = "Clock synchronisation (0x52)"))
        add(HuaweiSessionStep(SessionPhase.PREPARING, detail = "Sending the user profile (0x31)"))
        add(HuaweiSessionStep(SessionPhase.PREPARING, detail = "Disarming association mode (0x45)"))

        add(HuaweiSessionStep(SessionPhase.MEASURING, detail = "Arming the BIA stream (0x97)"))
        add(HuaweiSessionStep(SessionPhase.MEASURING, detail = "Confirmation reading (0x97) then acknowledgement (0x31 type=2)"))
    }
}
