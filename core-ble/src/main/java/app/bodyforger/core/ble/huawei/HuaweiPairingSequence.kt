package app.bodyforger.core.ble.huawei

import app.bodyforger.core.ble.AthleteInstruction
import app.bodyforger.core.ble.PairingRequirement
import app.bodyforger.core.ble.SessionPhase
import app.bodyforger.core.model.ElectrodeCount

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
     * Monter et saisir la poignée forment une seule étape, deux gestes simultanés. La poignée
     * n'est demandée que si le matériel en a une : l'exiger d'une balance qui n'en a pas
     * laisserait l'athlète devant une consigne impossible.
     */
    fun stepsFor(model: HuaweiScaleModel): List<HuaweiSessionStep> = buildList {
        // Aucun tapotement ici : l'appairage part d'une balance que le scan vient de
        // repérer, donc déjà réveillée. Le tapotement sert à réveiller une balance **déjà
        // appairée** au moment de peser, et n'a de sens que là.
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
        // L'athlète monte ici, et une seule fois : la tare arrive après quelques secondes,
        // puis la trame BIA complète pendant la même montée. L'inviter deux fois le ferait
        // descendre entre les deux et perdrait la mesure.
        add(
            HuaweiSessionStep(
                phase = SessionPhase.AWAITING_ATHLETE,
                instructions = HuaweiWeighInSequence.stepOnInstructions(model),
                detail = "Capture de la tare renvoyée par la balance"
            )
        )
        add(HuaweiSessionStep(SessionPhase.PREPARING, detail = "Synchronisation de l'heure (0x52)"))
        add(HuaweiSessionStep(SessionPhase.PREPARING, detail = "Transmission du profil utilisateur (0x31)"))
        add(HuaweiSessionStep(SessionPhase.PREPARING, detail = "Désarmement de l'association (0x45)"))

        // L'athlète est déjà sur la balance : la trame de validation arrive pendant la même
        // montée que la tare, sans nouvelle consigne.
        add(HuaweiSessionStep(SessionPhase.MEASURING, detail = "Armement du flux BIA (0x97)"))
        add(HuaweiSessionStep(SessionPhase.MEASURING, detail = "Relevé de validation (0x97) puis acquittement (0x31 type=2)"))
    }
}
