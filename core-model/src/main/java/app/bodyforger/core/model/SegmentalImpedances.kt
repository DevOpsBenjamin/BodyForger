package app.bodyforger.core.model

/**
 * Les cinq segments du corps isolés à **une** fréquence, en ohms.
 *
 * Couche **entièrement dérivée** : ces valeurs se recalculent à la demande depuis les
 * [RawImpedances] et ne sont **jamais persistées**. C'est ce qui permet de refaire tout
 * l'historique le jour où les équations évoluent — voir *Measurement Trueness vs
 * Repeatability* dans `CONTEXT.md`.
 *
 * ⚠️ [trunkOhms] est un **résidu de différence de grands nombres** : à ±1 % d'erreur de
 * contact il varie de ±9 Ω autour d'une valeur nominale de ~20 Ω, soit près de 100 %
 * d'incertitude relative, contre 2 % pour [bodyOhms]. À traiter comme un indicateur, jamais
 * comme une mesure fine.
 */
data class SegmentalImpedances(
    val frequencyKHz: Int,
    val rightArmOhms: Double,
    val leftArmOhms: Double,
    val rightLegOhms: Double,
    val leftLegOhms: Double,
    val trunkOhms: Double,
    val bodyOhms: Double
) {
    /** L'index d'impédance corporelle : taille² / résistance, l'entrée des régressions DEXA. */
    fun bodyImpedanceIndex(heightCm: Double): Double = (heightCm * heightCm) / bodyOhms
}
