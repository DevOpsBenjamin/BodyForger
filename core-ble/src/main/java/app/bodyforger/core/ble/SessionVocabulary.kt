package app.bodyforger.core.ble

/**
 * Le vocabulaire commun à toutes les balances.
 *
 * Le cœur ne connaît **que** ces termes ; c'est le pilote qui décide de sa séquence, de son
 * nombre d'étapes et de leur ordre. Une balance en diffusion pure n'a ni connexion ni
 * authentification et n'émettra jamais [SessionPhase.PREPARING] ; une balance qui exige une
 * pesée d'appairage émettra [AthleteInstruction.STEP_ON_BAREFOOT] pendant l'appairage, là où
 * une autre n'en aura pas besoin.
 */
enum class SessionPhase {
    /** Recherche de l'appareil parmi ceux qui s'annoncent. */
    DISCOVERING,

    /** Connexion, authentification, transmission du profil — sans intervention de l'athlète. */
    PREPARING,

    /** Le matériel attend un geste physique : voir [AthleteInstruction]. */
    AWAITING_ATHLETE,

    /** Stabilisation de la masse puis relevé des impédances. */
    MEASURING
}

/**
 * Un geste physique demandé à l'athlète.
 *
 * Ensemble volontairement clos et petit : l'interface sait rendre chacun de ces gestes, et
 * les pilotes se limitent à choisir lesquels demander et dans quel ordre. Ajouter un geste
 * est une décision de conception, pas un détail de pilote.
 */
enum class AthleteInstruction {
    /** Tapoter la balance pour qu'elle s'annonce ; sans cela elle reste invisible au scan. */
    TAP_SCALE_TO_WAKE,

    /** Rester hors du plateau pendant la négociation. */
    STAY_OFF_PLATFORM,

    /** Monter pieds nus sur les électrodes. */
    STEP_ON_BAREFOOT,

    /** Saisir la poignée rétractable des deux mains — sans quoi aucune impédance ne sera relevée. */
    GRIP_HANDLE,

    /** Descendre du plateau. */
    STEP_OFF
}

/** Pourquoi une session s'est arrêtée avant d'aboutir. */
enum class SessionFailure {
    /** Aucun appareil correspondant ne s'est annoncé dans le délai imparti. */
    DEVICE_NOT_FOUND,

    /** La liaison a été perdue en cours de séquence. */
    CONNECTION_LOST,

    /** Le matériel a rejeté l'authentification ou le profil utilisateur. */
    REJECTED_BY_DEVICE,

    /** L'athlète n'est pas intervenu, ou la mesure ne s'est jamais stabilisée. */
    TIMED_OUT,

    /** Aucune Association connue : il faut d'abord appairer la balance. */
    NOT_ASSOCIATED,

    /** Le matériel a signalé une condition qu'il est seul à connaître. */
    DEVICE_ERROR
}
