package app.bodyforger.core.ble.huawei

import app.bodyforger.core.ble.AthleteInstruction
import app.bodyforger.core.ble.SessionPhase
import app.bodyforger.core.model.ElectrodeCount

/**
 * La séquence de pesée **Mode 2** de la famille Haige, jouée à chaque pesée une fois
 * l'Association établie.
 *
 * Multi-étapes au même titre que l'appairage : la balance doit être réveillée, authentifiée
 * et configurée avant que l'athlète puisse monter.
 *
 * Séquence tirée de `TECH.md` §5 et de l'implémentation de référence `scale3.py`.
 */
object HuaweiWeighInSequence {

    /**
     * Les étapes pour un modèle donné.
     *
     * Monter et saisir la poignée forment **une seule étape** : ce sont deux gestes
     * simultanés, et les séparer ferait relâcher la poignée avant la mesure. La poignée n'est
     * demandée qu'à un matériel à huit électrodes ; sans elle la pesée aboutit quand même,
     * avec la seule masse.
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

    /**
     * Ce que l'athlète doit faire au moment de monter — la poignée n'ayant de sens que sur un
     * matériel qui en possède une.
     */
    internal fun stepOnInstructions(model: HuaweiScaleModel): List<AthleteInstruction> = buildList {
        add(AthleteInstruction.STEP_ON_BAREFOOT)
        if (model.capability?.electrodeCount == ElectrodeCount.EIGHT) {
            add(AthleteInstruction.GRIP_HANDLE)
        }
    }
}
