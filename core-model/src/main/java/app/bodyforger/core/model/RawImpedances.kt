package app.bodyforger.core.model

/**
 * La **fidélité obtenue** par une pesée : l'ensemble des résistances qu'elle a réellement
 * relevées.
 *
 * Cette couche vit sur le [BodyLog], pas sur l'Association. Elle sert **après** la mesure —
 * sélectionner la famille d'équations et interdire au moteur de composition de tourner sur
 * du vide. Elle se lit dans la trame, elle ne se déclare pas : la balance remplit de zéros
 * ce qu'elle n'a pas mesuré.
 *
 * À distinguer de [ScaleCapability], qui n'en donne que le majorant.
 */
@JvmInline
value class MeasuredFidelity(val readings: Set<ImpedanceReading>) {

    val paths: Set<ImpedancePath> get() = readings.mapTo(mutableSetOf()) { it.path }

    val frequenciesKHz: List<Int> get() = readings.map { it.frequencyKHz }.distinct().sorted()

    val isEmpty: Boolean get() = readings.isEmpty()

    /**
     * Le nombre d'électrodes que cette pesée a effectivement mis en jeu — déduit des
     * trajets relevés, jamais du modèle de l'appareil.
     */
    val exercisedElectrodeCount: ElectrodeCount
        get() = when {
            readings.any { it.path.involvesHands } -> ElectrodeCount.EIGHT
            readings.isNotEmpty() -> ElectrodeCount.FOUR
            else -> ElectrodeCount.NONE
        }

    companion object {
        /** Une pesée qui n'a relevé aucune impédance : masse seule. */
        val NONE = MeasuredFidelity(emptySet())
    }
}

/**
 * Les résistances brutes d'une pesée, en ohms — la seule grandeur qu'une balance à
 * bio-impédance **mesure** réellement.
 *
 * Conservées telles quelles et pour toujours : c'est ce qui permet de recalculer tout
 * l'historique quand les équations de composition évoluent, et d'agréger une période en
 * analysant la médiane des résistances plutôt qu'une moyenne de résultats.
 *
 * **Une valeur absente n'est jamais remplacée par un défaut.** Un chiffre fabriqué serait
 * indiscernable d'une mesure réelle dans l'historique — une lecture pied ↔ pied authentique
 * vaut 509,8 Ω, soit précisément la plage où tombaient les anciennes valeurs de repli.
 */
@JvmInline
value class RawImpedances private constructor(val ohmsByReading: Map<ImpedanceReading, Double>) {

    val fidelity: MeasuredFidelity get() = MeasuredFidelity(ohmsByReading.keys)

    val isEmpty: Boolean get() = ohmsByReading.isEmpty()

    /** La résistance relevée sur ce trajet à cette fréquence, ou `null` si non mesurée. */
    operator fun get(path: ImpedancePath, frequencyKHz: Int): Double? =
        ohmsByReading[ImpedanceReading(path, frequencyKHz)]

    operator fun get(reading: ImpedanceReading): Double? = ohmsByReading[reading]

    companion object {
        /** Une pesée sans aucune impédance relevée. */
        val NONE = RawImpedances(emptyMap())

        /**
         * Construit un relevé en écartant toute valeur non mesurée.
         *
         * Les entrées nulles ou négatives sont **rejetées, pas conservées** : le zéro est
         * la façon dont l'appareil dit « je n'ai pas mesuré ».
         */
        fun of(ohmsByReading: Map<ImpedanceReading, Double>): RawImpedances =
            RawImpedances(ohmsByReading.filterValues { it > 0.0 }.toMap())
    }
}
