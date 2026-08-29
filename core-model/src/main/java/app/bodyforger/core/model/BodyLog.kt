package app.bodyforger.core.model

/**
 * L'analyse de composition corporelle dérivée des [RawImpedances].
 *
 * N'existe que si des impédances ont été relevées. À ne pas confondre avec le taux de masse
 * grasse du [BodyLog], qui est toujours renseigné.
 */
data class BodyCompositionReport(
    /** Masse maigre : tout ce qui n'est pas du gras — muscle, os, eau, organes. */
    val fatFreeMassKg: Double,
    val fatMassKg: Double,
    val bodyFatPercentage: Double,

    /** Masse musculaire squelettique : la part que l'entraînement fait bouger. */
    val skeletalMuscleMassKg: Double,

    // --- Compartiments de la masse maigre (modèle Brozek 4C) ---
    val totalBodyWaterKg: Double,
    val extracellularWaterKg: Double,
    val intracellularWaterKg: Double,
    val proteinMassKg: Double,
    val boneMineralMassKg: Double,

    /** Rapport eau extracellulaire sur eau totale. Norme clinique ~0,38 – 0,40. */
    val ecwTbwRatio: Double,

    /**
     * Le muscle réparti sur les cinq segments, ou `null` si la pesée n'a pas mis les mains
     * en jeu : sans les bras dans le circuit, aucun membre n'est isolable et aucun ne sera
     * inventé.
     */
    val segmentalMuscle: SegmentalMuscleMass?,

    /** Les cinq segments isolés à chaque fréquence relevée. Dérivés, jamais persistés. */
    val segmental: List<SegmentalImpedances> = emptyList()
) {
    /**
     * Masse musculaire squelettique **totale** rapportée au carré de la taille, en kg/m².
     *
     * ⚠️ Ce n'est pas le SMI de Baumgartner et la grille clinique ne s'y applique pas :
     * celle-ci porte sur le muscle des seuls membres. Pour la lire, passer par
     * [SegmentalMuscleMass.baumgartnerIndex].
     */
    fun totalMuscleIndex(heightCm: Double): Double =
        skeletalMuscleMassKg / ((heightCm / 100.0) * (heightCm / 100.0))
}

/**
 * Un relevé corporel **horodaté** rattaché à l'athlète.
 *
 * Identifié par son instant et non par sa date : se peser plusieurs fois dans une journée est
 * légitime — au réveil, après l'effort — et n'écrase rien. Un relevé par jour est un
 * **objectif de suivi**, pas une contrainte du modèle : la tendance se lit sur une médiane
 * glissante, qui absorbe très bien plusieurs points par jour comme des jours sans mesure.
 *
 * Porte toujours une masse, une date et un taux de masse grasse — ce dernier provenant soit
 * de la balance, soit d'une saisie manuelle. C'est ce qui rend l'application utilisable sans
 * balance connectée.
 *
 * Les [rawImpedances] ne sont présentes que si le matériel les a relevées, et leur
 * [MeasuredFidelity] dit exactement ce que cette pesée a obtenu. Une grandeur non mesurée
 * est **absente**, jamais remplacée par un défaut.
 */
data class BodyLog(
    val id: String,
    val dateIso: String,
    val measuredAtEpochMs: Long,
    val massKg: Double,
    val bodyFatPercentage: Double,
    val rawImpedances: RawImpedances = RawImpedances.NONE,
    val restingHeartRateBpm: Int? = null
) {
    /** Ce que cette pesée a réellement relevé. */
    val fidelity: MeasuredFidelity get() = rawImpedances.fidelity

    /** Vrai si une analyse de composition corporelle est calculable à partir de ce relevé. */
    val supportsBodyComposition: Boolean get() = !rawImpedances.isEmpty
}
