package app.bodyforger.core.ble

import app.bodyforger.core.model.ScaleAssociation

/**
 * L'avancement d'une pesée, exprimé dans le vocabulaire commun.
 *
 * Une pesée n'est pas une opération instantanée mais une séquence de plusieurs dizaines de
 * secondes, que l'athlète suit et peut interrompre. Le nombre d'étapes et leur ordre
 * appartiennent au pilote ; seul le vocabulaire est partagé.
 */
sealed interface WeighInState {

    /**
     * Étape [index] sur [totalSteps], telle que le pilote découpe sa séquence.
     *
     * Une pesée est multi-étapes au même titre qu'un appairage : réveil, handshake, balance
     * prête, consigne à l'athlète, mesure. Le découpage appartient au pilote et sert à
     * afficher une progression, pas à en déduire ce qui se passe.
     *
     * [detail] est un libellé propre au pilote, destiné au journal et au diagnostic — jamais
     * à piloter l'interface, qui se fonde sur [phase] et [instruction].
     */
    data class Progress(
        val index: Int,
        val totalSteps: Int,
        val phase: SessionPhase,
        val instruction: AthleteInstruction? = null,
        val detail: String? = null
    ) : WeighInState

    /** Masse en cours de stabilisation, quand le matériel la diffuse en direct. */
    data class LiveWeight(val massKg: Double) : WeighInState

    /**
     * La balance a livré sa mesure.
     *
     * **C'est un succès même sans aucune impédance.** Une balance huit électrodes dont
     * l'athlète n'a pas saisi la poignée émet une trame complète où seule la masse est
     * renseignée, puis valide la pesée : la fidélité obtenue est simplement moindre.
     */
    data class Completed(val telemetry: BiaTelemetry) : WeighInState

    /** La pesée n'a pas abouti. */
    data class Failed(val reason: SessionFailure, val cause: Throwable? = null) : WeighInState
}

/**
 * L'avancement d'un appairage.
 *
 * Le déroulé dépend entièrement du matériel : certaines balances exigent une pesée de
 * validation pour graver un profil dans leur mémoire flash, d'autres n'ont rien à appairer.
 * Voir [PairingRequirement].
 */
sealed interface PairingState {

    /**
     * Étape [index] sur [totalSteps], telle que le pilote la découpe.
     *
     * Le découpage est propre au pilote : il sert à afficher une progression, pas à en
     * déduire ce qui se passe.
     */
    data class Progress(
        val index: Int,
        val totalSteps: Int,
        val phase: SessionPhase,
        val instruction: AthleteInstruction? = null,
        val detail: String? = null
    ) : PairingState

    /** L'Association est établie et peut être partagée entre la montre et le téléphone. */
    data class Completed(val association: ScaleAssociation) : PairingState

    /** L'appairage n'a pas abouti. */
    data class Failed(val reason: SessionFailure, val cause: Throwable? = null) : PairingState
}

/** Ce qu'un matériel exige avant de pouvoir être utilisé au quotidien. */
enum class PairingRequirement {
    /** Rien à appairer : la balance se contente de diffuser ou de répondre. */
    NONE,

    /** Une Association suffit, sans intervention physique de l'athlète. */
    HANDSHAKE_ONLY,

    /**
     * Le matériel grave un profil dans sa mémoire et exige une **pesée de validation** :
     * l'athlète doit monter sur la balance pendant l'appairage lui-même.
     */
    WEIGH_IN_REQUIRED
}
