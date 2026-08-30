package app.bodyforger.core.model

/** Nombre d'électrodes en contact avec le corps pendant la mesure. */
enum class ElectrodeCount(val electrodes: Int) {
    /** Aucune électrode : balance à poids seul, ou saisie manuelle. */
    NONE(0),

    /** Quatre électrodes au plateau : seul le trajet pied ↔ pied existe. */
    FOUR(4),

    /** Quatre au plateau et quatre sur la poignée rétractable : les six trajets existent. */
    EIGHT(8)
}

/**
 * Le **plafond** d'un matériel : ce qu'il est capable de mesurer.
 *
 * Cette couche vit sur l'Association, pas sur la pesée. Elle sert **avant** la mesure —
 * décider si proposer une analyse de composition a un sens, et inviter l'athlète à sortir
 * la poignée. Elle ne dit jamais ce qu'une pesée donnée a réellement obtenu : pour cela,
 * voir [MeasuredFidelity].
 *
 * Deux axes indépendants. Les fusionner en une énumération plate écraserait deux
 * dimensions orthogonales : le nombre d'électrodes et le nombre de fréquences varient
 * séparément.
 */
data class ScaleCapability(
    val electrodeCount: ElectrodeCount,
    val frequenciesKHz: List<Int>
) {
    init {
        require(frequenciesKHz.all { it > 0 }) { "Fréquence invalide dans $frequenciesKHz" }
        require(frequenciesKHz.distinct().size == frequenciesKHz.size) {
            "Fréquences dupliquées dans $frequenciesKHz"
        }
        require(electrodeCount == ElectrodeCount.NONE || frequenciesKHz.isNotEmpty()) {
            "Un appareil à électrodes doit déclarer au moins une fréquence"
        }
        require(electrodeCount != ElectrodeCount.NONE || frequenciesKHz.isEmpty()) {
            "Un appareil sans électrode ne peut relever aucune fréquence"
        }
    }

    /**
     * Les trajets que ce matériel peut relever. À quatre électrodes, seul le trajet
     * pied ↔ pied existe : les cinq autres passent par les mains.
     */
    val measurablePaths: Set<ImpedancePath> = when (electrodeCount) {
        ElectrodeCount.NONE -> emptySet()
        ElectrodeCount.FOUR -> ImpedancePath.entries.filterNot { it.involvesHands }.toSet()
        ElectrodeCount.EIGHT -> ImpedancePath.entries.toSet()
    }

    /**
     * Le produit des deux axes : toute résistance que ce matériel peut relever.
     *
     * C'est un **majorant**, jamais un constat. Une balance huit électrodes dont l'athlète
     * ne saisit pas la poignée produit légitimement l'ensemble vide.
     */
    val measurableReadings: Set<ImpedanceReading> =
        measurablePaths.flatMap { path -> frequenciesKHz.map { ImpedanceReading(path, it) } }.toSet()

    /** Vrai si le matériel peut produire une analyse de composition corporelle. */
    val supportsBodyComposition: Boolean get() = measurableReadings.isNotEmpty()

    companion object {
        /** Balance sans électrode : elle ne remonte qu'une masse. */
        val WEIGHT_ONLY = ScaleCapability(ElectrodeCount.NONE, emptyList())

        /** Saisie manuelle par l'athlète : aucun capteur. */
        val MANUAL = WEIGHT_ONLY
    }
}
