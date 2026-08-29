package app.bodyforger.core.model

/**
 * La masse musculaire répartie sur les cinq segments du corps, en kilogrammes.
 *
 * Le muscle conduit mieux le courant que la graisse : un segment moins résistant en porte
 * davantage. Deux partages en découlent, tous deux **mesurés** :
 *
 * * **Gauche / droite**, par la conductance relative des deux membres d'une paire. Un bras
 *   qui prend du retard sur l'autre se voit.
 * * **Bras / jambes**, par la conductance des deux bras face à celle des deux jambes. Un
 *   cycle de squat fait baisser la résistance des jambes et remonte leur part : le rapport
 *   suit l'entraînement.
 *
 * ⚠️ Une seule constante subsiste : la **part des quatre membres dans le muscle total**
 * (~65 %), le reste revenant au tronc. La bio-impédance n'isole pas le tronc de façon
 * fiable — son résidu de Kirchhoff est dominé par le bruit. Le [trunkKg] est donc un
 * complément à un total, pas une mesure du tronc.
 */
data class SegmentalMuscleMass(
    val rightArmKg: Double,
    val leftArmKg: Double,
    val rightLegKg: Double,
    val leftLegKg: Double,
    val trunkKg: Double
) {
    /** Muscle des quatre membres — l'ASMM de la littérature clinique. */
    val appendicularKg: Double get() = rightArmKg + leftArmKg + rightLegKg + leftLegKg

    /** Le total des cinq segments, égal par construction à la masse musculaire squelettique. */
    val totalKg: Double get() = appendicularKg + trunkKg

    /**
     * L'indice de masse musculaire squelettique de **Baumgartner**, en kg/m² — celui auquel
     * la grille clinique s'applique, parce qu'il porte sur les seuls membres.
     *
     * Hommes : < 7,0 sarcopénie · 7,0–8,5 sédentaire · 8,5–10,0 athlétique · > 10,0 haut niveau.
     */
    fun baumgartnerIndex(heightCm: Double): Double =
        appendicularKg / ((heightCm / 100.0) * (heightCm / 100.0))

    /**
     * L'écart entre les deux bras, en pourcentage du plus fort. C'est ce que la mesure
     * établit vraiment, et donc ce qui mérite d'être suivi.
     */
    val armAsymmetryPercent: Double
        get() = asymmetry(rightArmKg, leftArmKg)

    /** L'écart entre les deux jambes, en pourcentage de la plus forte. */
    val legAsymmetryPercent: Double
        get() = asymmetry(rightLegKg, leftLegKg)

    private fun asymmetry(right: Double, left: Double): Double {
        val stronger = maxOf(right, left)
        return if (stronger <= 0.0) 0.0 else (kotlin.math.abs(right - left) / stronger) * 100.0
    }
}
