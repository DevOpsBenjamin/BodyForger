package app.bodyforger.core.ble

import app.bodyforger.core.model.ScaleCapability

/**
 * L'état observable d'une pesée.
 *
 * Une pesée n'est pas une opération instantanée mais une séquence de plusieurs dizaines de
 * secondes, que l'athlète suit et peut interrompre. Elle se déroule en trois temps, dont le
 * premier est physique et doit lui être annoncé : réveil par tapotement, négociation hors du
 * plateau, puis montée sur la balance.
 */
sealed interface WeighInState {

    /** La balance dort. Tant que l'athlète ne la tapote pas, elle reste invisible au scan. */
    data object AwaitingWakeUp : WeighInState

    /** Recherche de la balance associée parmi les appareils qui s'annoncent. */
    data object Scanning : WeighInState

    /** Balance trouvée, connexion GATT en cours. */
    data class Connecting(val displayName: String) : WeighInState

    /** Handshake chiffré et configuration du profil utilisateur, athlète **hors** du plateau. */
    data object Negotiating : WeighInState

    /**
     * La balance est prête : l'athlète peut monter, pieds nus.
     *
     * [capability] porte le plafond du matériel quand il est connu, afin d'inviter
     * explicitement à saisir la poignée sur un appareil huit électrodes. Sans poignée, la
     * pesée aboutira quand même — mais sans aucune impédance.
     */
    data class ReadyForStepOn(val capability: ScaleCapability?) : WeighInState

    /** Stabilisation de la masse puis relevé des impédances, une vingtaine de secondes. */
    data object Measuring : WeighInState

    /**
     * La balance a livré sa mesure et l'a acquittée.
     *
     * **C'est un succès même sans aucune impédance.** Une balance huit électrodes dont
     * l'athlète n'a pas saisi la poignée émet une trame complète où seule la masse est
     * renseignée, puis valide la pesée : la fidélité obtenue est simplement moindre, ce
     * n'est pas un échec.
     */
    data class Completed(val telemetry: BiaTelemetry) : WeighInState

    /** La pesée n'a pas abouti. */
    data class Failed(val reason: Reason, val cause: Throwable? = null) : WeighInState {
        enum class Reason {
            /** Aucune balance associée ne s'est annoncée dans le délai imparti. */
            SCALE_NOT_FOUND,

            /** La connexion GATT a été perdue en cours de séquence. */
            CONNECTION_LOST,

            /** La balance a rejeté le handshake ou le profil utilisateur. */
            AUTHENTICATION_REJECTED,

            /** L'athlète n'est pas monté, ou la masse ne s'est jamais stabilisée. */
            MEASUREMENT_TIMED_OUT,

            /** Aucune Association connue : il faut d'abord appairer la balance. */
            NOT_ASSOCIATED
        }
    }
}
