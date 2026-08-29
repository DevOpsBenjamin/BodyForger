package app.bodyforger.core.bia

import app.bodyforger.core.model.ImpedancePath
import app.bodyforger.core.model.RawImpedances
import app.bodyforger.core.model.SegmentalImpedances

/**
 * Isole les cinq segments du corps à partir des six trajets mesurés, par résolution des
 * lois de Kirchhoff.
 *
 * Le corps est modélisé en cinq conducteurs reliés à un nœud central : quatre membres longs
 * et étroits (résistance élevée) et un tronc de large section (résistance faible, ~20 Ω).
 * Une balance ne mesure jamais un membre seul — le courant doit entrer par un point et
 * sortir par un autre — d'où six boucles fermées pour cinq inconnues :
 *
 * ```
 * R_lfrf = Z_LF + Z_RF                     R_lhlf = Z_LH + Z_tronc + Z_LF
 * R_lhrh = Z_LH + Z_RH                     R_lhrf = Z_LH + Z_tronc + Z_RF
 *                                          R_rhlf = Z_RH + Z_tronc + Z_LF
 *                                          R_rhrf = Z_RH + Z_tronc + Z_RF
 * ```
 *
 * Les membres s'isolent par différenciation croisée : la somme des trajets passant par le
 * bras droit moins celle des trajets passant par le bras gauche vaut `2·(Z_RH − Z_LH)`, ce
 * qui élimine le tronc. Le tronc s'obtient ensuite en sommant les quatre trajets croisés,
 * où il apparaît quatre fois.
 */
object KirchhoffSolver {

    /**
     * Résout le réseau à une fréquence, ou rend `null` si les six trajets n'y sont pas
     * tous relevés — une valeur manquante n'est jamais remplacée par un défaut.
     */
    fun solve(impedances: RawImpedances, frequencyKHz: Int): SegmentalImpedances? {
        val lfrf = impedances[ImpedancePath.LEFT_FOOT_TO_RIGHT_FOOT, frequencyKHz] ?: return null
        val lhrh = impedances[ImpedancePath.LEFT_HAND_TO_RIGHT_HAND, frequencyKHz] ?: return null
        val lhlf = impedances[ImpedancePath.LEFT_HAND_TO_LEFT_FOOT, frequencyKHz] ?: return null
        val lhrf = impedances[ImpedancePath.LEFT_HAND_TO_RIGHT_FOOT, frequencyKHz] ?: return null
        val rhlf = impedances[ImpedancePath.RIGHT_HAND_TO_LEFT_FOOT, frequencyKHz] ?: return null
        val rhrf = impedances[ImpedancePath.RIGHT_HAND_TO_RIGHT_FOOT, frequencyKHz] ?: return null

        val armDelta = 0.25 * ((rhlf + rhrf) - (lhlf + lhrf))
        val legDelta = 0.25 * ((lhrf + rhrf) - (lhlf + rhlf))

        val rightArm = 0.5 * lhrh + armDelta
        val leftArm = 0.5 * lhrh - armDelta
        val rightLeg = 0.5 * lfrf + legDelta
        val leftLeg = 0.5 * lfrf - legDelta

        return SegmentalImpedances(
            frequencyKHz = frequencyKHz,
            rightArmOhms = rightArm,
            leftArmOhms = leftArm,
            rightLegOhms = rightLeg,
            leftLegOhms = leftLeg,
            trunkOhms = (lhlf + lhrf + rhlf + rhrf - 2.0 * (lfrf + lhrh)) / 4.0,
            // Les quatre trajets croisés s'annulent : Z_corps se réduit à (R_pieds + R_mains) / 4.
            bodyOhms = (lfrf + lhrh) / 4.0
        )
    }

    /**
     * L'écart maximal que les mesures autorisent entre deux membres homologues, en ohms.
     *
     * Une fois pied↔pied et main↔main fixés, la différenciation croisée ne peut produire
     * qu'un écart borné par l'arrangement des quatre trajets croisés. **Tout écart
     * segmentaire annoncé au-delà est faux par construction** — c'est un test de
     * recevabilité utile face à une source externe.
     */
    fun maximumLimbSpread(impedances: RawImpedances, frequencyKHz: Int): Double? {
        val crossed = ImpedancePath.entries
            .filter { it.involvesHands && it != ImpedancePath.LEFT_HAND_TO_RIGHT_HAND }
            .map { impedances[it, frequencyKHz] ?: return null }
        val total = crossed.sum()
        return crossed.indices.flatMap { i -> (i + 1 until crossed.size).map { j -> i to j } }
            .maxOf { (i, j) ->
                val pair = crossed[i] + crossed[j]
                kotlin.math.abs(pair - (total - pair)) / 4.0
            }
    }
}
