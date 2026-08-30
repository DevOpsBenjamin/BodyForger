package app.bodyforger.core.model

/**
 * Un trajet anatomique le long duquel une balance mesure une résistance électrique.
 *
 * Un trajet n'est **pas** une zone du corps : les six traversent à la fois les membres
 * et le tronc. La composition segmentaire s'en déduit par les lois de Kirchhoff, elle ne
 * s'y lit pas directement.
 *
 * [wireIndex] est la position du trajet dans la trame de télémétrie, identique aux deux
 * fréquences. Ordre confirmé par trois sources indépendantes (décodeur de production,
 * `HUAWEI_SCALE_3_BIA_ALGORITHMS.md` §3, base locale de Huawei Health).
 */
enum class ImpedancePath(val wireIndex: Int, val involvesHands: Boolean) {
    LEFT_FOOT_TO_RIGHT_FOOT(0, involvesHands = false),
    LEFT_HAND_TO_RIGHT_HAND(1, involvesHands = true),
    LEFT_HAND_TO_LEFT_FOOT(2, involvesHands = true),
    LEFT_HAND_TO_RIGHT_FOOT(3, involvesHands = true),
    RIGHT_HAND_TO_LEFT_FOOT(4, involvesHands = true),
    RIGHT_HAND_TO_RIGHT_FOOT(5, involvesHands = true);

    companion object {
        /** Les six trajets, dans l'ordre où la trame les transporte. */
        val BY_WIRE_INDEX: List<ImpedancePath> = entries.sortedBy { it.wireIndex }
    }
}

/**
 * Une résistance identifiée par son trajet et sa fréquence d'excitation.
 *
 * La fréquence est portée en kilohertz plutôt que par un drapeau « bi-fréquence » : un
 * appareil tri-fréquence ne doit pas imposer de refonte du modèle.
 */
data class ImpedanceReading(val path: ImpedancePath, val frequencyKHz: Int) {
    init {
        require(frequencyKHz > 0) { "Fréquence invalide : $frequencyKHz kHz" }
    }

    companion object {
        const val LOW_FREQUENCY_KHZ: Int = 50
        const val HIGH_FREQUENCY_KHZ: Int = 250
    }
}
