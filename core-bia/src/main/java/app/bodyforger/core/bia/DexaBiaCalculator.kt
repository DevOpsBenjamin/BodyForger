package app.bodyforger.core.bia

import app.bodyforger.core.model.BiaProfile
import app.bodyforger.core.model.BiologicalSex
import app.bodyforger.core.model.BodyCompositionReport
import app.bodyforger.core.model.ImpedancePath
import app.bodyforger.core.model.ImpedanceReading
import app.bodyforger.core.model.RawImpedances
import app.bodyforger.core.model.SegmentalImpedances
import app.bodyforger.core.model.SegmentalMuscleMass

/**
 * Le moteur de composition corporelle bi-fréquence.
 *
 * **Il n'invente aucun chiffre.** Une grandeur que le matériel n'a pas mesurée est absente,
 * jamais remplacée par un défaut : un nombre fabriqué serait indiscernable d'une mesure
 * réelle dans l'historique. Là où l'ancienne version repliait une impédance manquante sur
 * `500,0 Ω`, celle-ci rend `null`.
 *
 * Le taux de masse grasse qu'il produit est **le nôtre**, calculé depuis les résistances
 * brutes. Il vit à côté de celui que la balance envoie, et ne le remplace pas — les deux
 * s'affichent ensemble. Comme il est une fonction pure des ohms conservés, il n'est jamais
 * persisté : le jour où les équations s'améliorent, tout l'historique se recalcule.
 *
 * ⚠️ La comparaison avec les chiffres du constructeur est un **détecteur de grosse erreur,
 * pas une cible de précision** : sa tolérance est le kilogramme. Réduire un écart inférieur
 * au kilo n'est jamais en soi une raison de toucher au modèle.
 */
object DexaBiaCalculator {

    /**
     * Régression de masse maigre bi-fréquence, une ligne par sexe.
     *
     * $FFM = c_1 \frac{H^2}{Z_{50}} + c_2 \frac{H^2}{Z_{250}} + c_3 Z_{50} + c_4 Z_{250}
     *      + c_5 P + c_6 H + c_7 A^2 + c_8 A + c_9$
     *
     * La double fréquence est ce qui distingue l'eau extracellulaire de la masse musculaire
     * active : à 50 kHz le courant contourne les membranes cellulaires, à 250 kHz il les
     * traverse.
     */
    private data class LeanMassModel(
        val lowFreqIndex: Double,
        val highFreqIndex: Double,
        val lowFreqOhms: Double,
        val highFreqOhms: Double,
        val massKg: Double,
        val heightCm: Double,
        val ageSquared: Double,
        val age: Double,
        val bias: Double
    )

    private val MALE = LeanMassModel(
        0.12631, 0.16098, -0.01195, -0.02027, 0.14923, 0.25154, -0.000070, -0.03560, -20.79390
    )
    private val FEMALE = LeanMassModel(
        0.07182, 0.07944, -0.01169, -0.01661, 0.11944, 0.23935, 0.000430, -0.08840, -14.71130
    )

    /**
     * Partage de la masse maigre selon le modèle occidental **Brozek 4C**, et non selon les
     * constantes du constructeur : la balance est un instrument, pas la référence.
     * Voir *Lean Mass Compartments* dans `CONTEXT.md`.
     */
    private const val BROZEK_WATER_FRACTION = 0.732
    private const val BROZEK_PROTEIN_FRACTION = 0.211
    private const val BROZEK_BONE_MINERAL_FRACTION = 0.057

    /** Norme clinique du rapport eau extracellulaire sur eau totale, faute de mieux. */
    private const val DEFAULT_ECW_TBW_RATIO = 0.380

    /** Masse musculaire squelettique dérivée de la masse maigre. */
    private const val SMM_SLOPE = 0.605
    private const val SMM_INTERCEPT = 1.833

/**
     * Part du muscle squelettique logée dans les quatre membres. Le reste est le tronc, que
     * la bio-impédance ne sait pas isoler de façon fiable (son résidu de Kirchhoff est
     * dominé par le bruit).
     */
    private const val APPENDICULAR_MUSCLE_FRACTION = 0.650

    /**
     * Facteur d'étalonnage géométrique entre le haut et le bas du corps : à masse musculaire
     * égale, un bras est plus court qu'une jambe et n'oppose donc pas la même résistance.
     *
     * C'est une **constante de calibration**, pas une mesure : sa valeur est choisie pour
     * qu'une morphologie de référence retrouve les fractions de population usuelles
     * (~17 % bras, ~48 % jambes).
     */
    private const val UPPER_TO_LOWER_GEOMETRY = 0.506

    /**
     * L'analyse la plus fidèle que la pesée autorise, ou `null` si elle ne porte aucune
     * impédance exploitable.
     *
     * Trois régimes, du plus riche au plus pauvre. Aucun ne comble ce que le matériel n'a
     * pas mesuré : il rend moins de grandeurs, jamais des grandeurs inventées.
     */
    fun calculate(
        massKg: Double,
        profile: BiaProfile,
        impedances: RawImpedances
    ): BodyCompositionReport? {
        require(massKg > 0.0) { "Masse invalide : $massKg kg" }

        val low = KirchhoffSolver.solve(impedances, ImpedanceReading.LOW_FREQUENCY_KHZ)
        val high = KirchhoffSolver.solve(impedances, ImpedanceReading.HIGH_FREQUENCY_KHZ)

        return when {
            // Huit électrodes, deux fréquences : l'analyse complète.
            low != null && high != null -> eightElectrode(massKg, profile, low, high)

            // Huit électrodes, une seule fréquence — la poignée est saisie mais l'appareil
            // ne monte pas en fréquence. Le segmentaire reste calculable.
            low != null -> eightElectrode(massKg, profile, low, null)

            // Quatre électrodes : le courant ne passe que par les jambes. Pas de segment.
            else -> fourElectrode(massKg, profile, impedances)
        }
    }

    /**
     * Huit électrodes. En l'absence de haute fréquence, la basse tient les deux rôles : la
     * régression se réduit alors exactement à sa forme mono-fréquence, les coefficients des
     * deux termes s'additionnant. Rien n'est codé en dur pour ce cas.
     */
    private fun eightElectrode(
        massKg: Double,
        profile: BiaProfile,
        low: SegmentalImpedances,
        high: SegmentalImpedances?
    ): BodyCompositionReport? {
        val fatFreeMassKg = leanMass(massKg, profile, low, high ?: low)
        if (fatFreeMassKg <= 0.0 || fatFreeMassKg >= massKg) return null

        val skeletalMuscleMassKg = SMM_SLOPE * fatFreeMassKg - SMM_INTERCEPT
        return compose(
            massKg = massKg,
            fatFreeMassKg = fatFreeMassKg,
            skeletalMuscleMassKg = skeletalMuscleMassKg,
            // Sans seconde fréquence, l'équilibre intra/extracellulaire ne se lit pas :
            // repli sur la norme clinique plutôt qu'un chiffre tiré d'une seule mesure.
            ecwTbwRatio = if (high != null) extracellularRatio(low, high) else DEFAULT_ECW_TBW_RATIO,
            segmentalMuscle = distributeMuscle(skeletalMuscleMassKg, low),
            segmental = listOfNotNull(low, high)
        )
    }

    /**
     * Quatre électrodes au plateau. Le courant ne traverse que les jambes : la composition
     * globale reste estimable, la répartition par membre **non**, et aucun membre n'est
     * fabriqué pour combler le tableau.
     */
    private fun fourElectrode(
        massKg: Double,
        profile: BiaProfile,
        impedances: RawImpedances
    ): BodyCompositionReport? {
        val footToFoot = impedances[
            ImpedancePath.LEFT_FOOT_TO_RIGHT_FOOT,
            ImpedanceReading.LOW_FREQUENCY_KHZ
        ] ?: return null

        val height = profile.heightCm
        val age = profile.ageYears.toDouble()
        val male = profile.sex == BiologicalSex.MALE
        val fatFreeMassKg = if (male) {
            0.406 * (height * height / footToFoot) + 0.360 * massKg + 0.100 * height - 0.080 * age - 9.10
        } else {
            0.370 * (height * height / footToFoot) + 0.300 * massKg + 0.110 * height - 0.070 * age - 8.20
        }
        if (fatFreeMassKg <= 0.0 || fatFreeMassKg >= massKg) return null

        return compose(
            massKg = massKg,
            fatFreeMassKg = fatFreeMassKg,
            skeletalMuscleMassKg = SMM_SLOPE * fatFreeMassKg - SMM_INTERCEPT,
            ecwTbwRatio = DEFAULT_ECW_TBW_RATIO,
            segmentalMuscle = null,
            segmental = emptyList()
        )
    }

    /** Les compartiments Brozek, communs aux trois régimes. */
    private fun compose(
        massKg: Double,
        fatFreeMassKg: Double,
        skeletalMuscleMassKg: Double,
        ecwTbwRatio: Double,
        segmentalMuscle: SegmentalMuscleMass?,
        segmental: List<SegmentalImpedances>
    ): BodyCompositionReport {
        val fatMassKg = massKg - fatFreeMassKg
        val totalBodyWaterKg = fatFreeMassKg * BROZEK_WATER_FRACTION
        val extracellularWaterKg = totalBodyWaterKg * ecwTbwRatio
        return BodyCompositionReport(
            fatFreeMassKg = fatFreeMassKg,
            fatMassKg = fatMassKg,
            bodyFatPercentage = (fatMassKg / massKg) * 100.0,
            skeletalMuscleMassKg = skeletalMuscleMassKg,
            totalBodyWaterKg = totalBodyWaterKg,
            extracellularWaterKg = extracellularWaterKg,
            intracellularWaterKg = totalBodyWaterKg - extracellularWaterKg,
            proteinMassKg = fatFreeMassKg * BROZEK_PROTEIN_FRACTION,
            boneMineralMassKg = fatFreeMassKg * BROZEK_BONE_MINERAL_FRACTION,
            ecwTbwRatio = ecwTbwRatio,
            segmentalMuscle = segmentalMuscle,
            segmental = segmental
        )
    }

    private fun leanMass(
        massKg: Double,
        profile: BiaProfile,
        low: SegmentalImpedances,
        high: SegmentalImpedances
    ): Double {
        val m = if (profile.sex == BiologicalSex.MALE) MALE else FEMALE
        val height = profile.heightCm
        val age = profile.ageYears.toDouble()
        return m.lowFreqIndex * low.bodyImpedanceIndex(height) +
            m.highFreqIndex * high.bodyImpedanceIndex(height) +
            m.lowFreqOhms * low.bodyOhms +
            m.highFreqOhms * high.bodyOhms +
            m.massKg * massKg +
            m.heightCm * height +
            m.ageSquared * age * age +
            m.age * age +
            m.bias
    }

    /**
     * Rapport eau extracellulaire sur eau totale, lu dans l'écart entre les deux fréquences.
     *
     * ⚠️ Cette calibration affine est **héritée de l'implémentation de référence et jamais
     * validée indépendamment**. Le principe physique, lui, est standard : plus la haute
     * fréquence se rapproche de la basse, plus l'eau est retenue hors des cellules.
     */
    /**
     * Répartit le muscle squelettique sur les cinq segments.
     *
     * Le partage gauche/droite suit la **conductance relative** : le muscle conduit mieux
     * le courant que la graisse, donc le membre le moins résistant porte la plus grosse
     * part. Pondérer un membre par la résistance de **l'autre** est exactement cette
     * conductance relative — `Z_LH / (Z_RH + Z_LH)` et `(1/Z_RH) / (1/Z_RH + 1/Z_LH)` sont
     * la même expression.
     *
     * Le partage **haut/bas** obéit à la même logique, un cran plus haut : la conductance
     * des deux bras face à celle des deux jambes, corrigée du facteur géométrique qui les
     * sépare. Un cycle de travail sur les bras fait baisser leur résistance et remonte donc
     * leur part — le rapport bras/jambes suit l'entraînement au lieu d'être figé.
     *
     * Seule la part des membres dans le muscle total reste une constante de population : le
     * tronc n'est pas isolable de façon fiable par bio-impédance.
     */
    private fun distributeMuscle(
        skeletalMuscleMassKg: Double,
        low: SegmentalImpedances
    ): SegmentalMuscleMass {
        val appendicularPool = skeletalMuscleMassKg * APPENDICULAR_MUSCLE_FRACTION

        // Haut contre bas : la conductance des deux bras face à celle des deux jambes.
        val armConductance = UPPER_TO_LOWER_GEOMETRY / (low.rightArmOhms + low.leftArmOhms)
        val legConductance = 1.0 / (low.rightLegOhms + low.leftLegOhms)
        val armShare = armConductance / (armConductance + legConductance)

        val armPool = appendicularPool * armShare
        val legPool = appendicularPool - armPool

        // Droite contre gauche, au sein de chaque paire.
        val rightArmKg = armPool * (low.leftArmOhms / (low.rightArmOhms + low.leftArmOhms))
        val rightLegKg = legPool * (low.leftLegOhms / (low.rightLegOhms + low.leftLegOhms))

        return SegmentalMuscleMass(
            rightArmKg = rightArmKg,
            leftArmKg = armPool - rightArmKg,
            rightLegKg = rightLegKg,
            leftLegKg = legPool - rightLegKg,
            trunkKg = skeletalMuscleMassKg - appendicularPool
        )
    }

    private fun extracellularRatio(low: SegmentalImpedances, high: SegmentalImpedances): Double =
        (0.380 + 0.05 * ((high.bodyOhms / low.bodyOhms) - 0.88)).coerceIn(0.30, 0.50)
}
