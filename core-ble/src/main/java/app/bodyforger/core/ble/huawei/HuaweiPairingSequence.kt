package app.bodyforger.core.ble.huawei

import app.bodyforger.core.ble.AthleteInstruction
import app.bodyforger.core.ble.PairingRequirement
import app.bodyforger.core.ble.SessionPhase
import app.bodyforger.core.model.ElectrodeCount

/** Une étape de la séquence d'appairage, telle que le pilote Haige la découpe. */
data class HuaweiPairingStep(
    val phase: SessionPhase,
    val instruction: AthleteInstruction? = null,
    /** Libellé de diagnostic, jamais destiné à piloter l'interface. */
    val detail: String
)

/**
 * La séquence d'appairage **Mode 1** de la famille Haige : gravure d'un profil dans la
 * mémoire flash de la balance, puis pesée de validation.
 *
 * C'est ici que se voit ce qu'un contrat générique ne peut pas présumer : l'appairage Haige
 * **exige que l'athlète monte sur la balance**, parce que la gravure n'est confirmée que par
 * une pesée réelle. Une balance en diffusion pure, ou un profil Bluetooth SIG standard, n'a
 * rien de tel — d'où [PairingRequirement] porté par le pilote et non par le cœur.
 *
 * Séquence tirée de `TECH.md` §5 et de l'implémentation de référence `scale3.py`.
 */
object HuaweiPairingSequence {

    val requirement: PairingRequirement = PairingRequirement.WEIGH_IN_REQUIRED

    /**
     * Les étapes pour un modèle donné.
     *
     * La saisie de la poignée n'est demandée que si le matériel a huit électrodes : l'exiger
     * d'une balance qui n'en a pas laisserait l'athlète devant une consigne impossible.
     */
    fun stepsFor(model: HuaweiScaleModel): List<HuaweiPairingStep> = buildList {
        add(
            HuaweiPairingStep(
                phase = SessionPhase.DISCOVERING,
                instruction = AthleteInstruction.TAP_SCALE_TO_WAKE,
                detail = "Réveil de la balance et scan ciblé"
            )
        )
        add(
            HuaweiPairingStep(
                phase = SessionPhase.PREPARING,
                instruction = AthleteInstruction.STAY_OFF_PLATFORM,
                detail = "Handshake chiffré (0x21, 0x25, 0x29)"
            )
        )
        add(HuaweiPairingStep(SessionPhase.PREPARING, detail = "Armement de l'association (0x45)"))
        add(HuaweiPairingStep(SessionPhase.PREPARING, detail = "Gravure du HUID en mémoire flash (0x2D)"))
        add(HuaweiPairingStep(SessionPhase.PREPARING, detail = "Capture de la tare renvoyée par la balance"))
        add(HuaweiPairingStep(SessionPhase.PREPARING, detail = "Synchronisation de l'heure (0x52)"))
        add(HuaweiPairingStep(SessionPhase.PREPARING, detail = "Transmission du profil utilisateur (0x31)"))
        add(HuaweiPairingStep(SessionPhase.PREPARING, detail = "Désarmement de l'association (0x45)"))

        // La gravure n'est confirmée que par une pesée réelle : c'est le propre de ce matériel.
        add(
            HuaweiPairingStep(
                phase = SessionPhase.AWAITING_ATHLETE,
                instruction = AthleteInstruction.STEP_ON_BAREFOOT,
                detail = "Pesée de validation : montée sur le plateau"
            )
        )
        if (model.capability?.electrodeCount == ElectrodeCount.EIGHT) {
            add(
                HuaweiPairingStep(
                    phase = SessionPhase.AWAITING_ATHLETE,
                    instruction = AthleteInstruction.GRIP_HANDLE,
                    detail = "Saisie de la poignée rétractable"
                )
            )
        }
        add(HuaweiPairingStep(SessionPhase.MEASURING, detail = "Relevé de validation (0x97)"))
    }
}
