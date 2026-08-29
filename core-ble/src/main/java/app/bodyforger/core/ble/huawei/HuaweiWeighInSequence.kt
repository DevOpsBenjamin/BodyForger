package app.bodyforger.core.ble.huawei

import app.bodyforger.core.ble.AthleteInstruction
import app.bodyforger.core.ble.SessionPhase
import app.bodyforger.core.model.ElectrodeCount

/**
 * La séquence de pesée **Mode 2** de la famille Haige, jouée à chaque pesée une fois
 * l'Association établie.
 *
 * Elle est multi-étapes au même titre que l'appairage : la balance doit être réveillée,
 * authentifiée et configurée avant que l'athlète puisse monter, et la consigne de saisir la
 * poignée n'a de sens qu'au dernier moment.
 *
 * Séquence tirée de `TECH.md` §5 et de l'implémentation de référence `scale3.py`.
 */
object HuaweiWeighInSequence {

    /**
     * Les étapes pour un modèle donné.
     *
     * La saisie de la poignée n'est demandée qu'à un matériel à huit électrodes. Sans elle,
     * la pesée aboutit quand même — la balance émet une trame complète où seule la masse est
     * renseignée, puis l'acquitte.
     */
    fun stepsFor(model: HuaweiScaleModel): List<HuaweiSessionStep> = buildList {
        add(
            HuaweiSessionStep(
                phase = SessionPhase.DISCOVERING,
                instruction = AthleteInstruction.TAP_SCALE_TO_WAKE,
                detail = "Réveil de la balance et scan ciblé"
            )
        )
        add(
            HuaweiSessionStep(
                phase = SessionPhase.PREPARING,
                instruction = AthleteInstruction.STAY_OFF_PLATFORM,
                detail = "Handshake chiffré (0x21, 0x25, 0x29)"
            )
        )
        add(HuaweiSessionStep(SessionPhase.PREPARING, detail = "Synchronisation de l'heure (0x52)"))
        add(HuaweiSessionStep(SessionPhase.PREPARING, detail = "Transmission du profil utilisateur (0x31)"))
        add(HuaweiSessionStep(SessionPhase.PREPARING, detail = "Armement du streaming BIA (0x97)"))
        add(
            HuaweiSessionStep(
                phase = SessionPhase.AWAITING_ATHLETE,
                instruction = AthleteInstruction.STEP_ON_BAREFOOT,
                detail = "Balance prête : montée sur le plateau"
            )
        )
        if (model.capability?.electrodeCount == ElectrodeCount.EIGHT) {
            add(
                HuaweiSessionStep(
                    phase = SessionPhase.AWAITING_ATHLETE,
                    instruction = AthleteInstruction.GRIP_HANDLE,
                    detail = "Saisie de la poignée rétractable des deux mains"
                )
            )
        }
        add(HuaweiSessionStep(SessionPhase.MEASURING, detail = "Stabilisation puis relevé (0x97)"))
        add(HuaweiSessionStep(SessionPhase.MEASURING, detail = "Acquittement de la mesure (0x31 type=2)"))
    }
}
