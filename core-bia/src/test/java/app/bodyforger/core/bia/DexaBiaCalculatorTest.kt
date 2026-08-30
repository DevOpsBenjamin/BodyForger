package app.bodyforger.core.bia

import app.bodyforger.core.model.BiaProfile
import app.bodyforger.core.model.BiologicalSex
import app.bodyforger.core.model.ImpedancePath
import app.bodyforger.core.model.ImpedanceReading
import app.bodyforger.core.model.ImpedanceReading.Companion.HIGH_FREQUENCY_KHZ
import app.bodyforger.core.model.ImpedanceReading.Companion.LOW_FREQUENCY_KHZ
import app.bodyforger.core.model.RawImpedances
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * **remonte** les six trajets par les lois de Kirchhoff.
 *
 */
class DexaBiaCalculatorTest {

    private val athlete = BiaProfile(BiologicalSex.MALE, ageYears = 30, heightCm = 180.0)
    private val massKg = 80.0

        private val body50 = Body(rightArm = 302.0, leftArm = 315.0, rightLeg = 204.0, leftLeg = 211.0, trunk = 26.0)

    /** Le même corps à 250 kHz : le courant traverse les membranes, tout baisse. */
    private val body250 = Body(rightArm = 270.0, leftArm = 281.0, rightLeg = 182.0, leftLeg = 188.0, trunk = 22.0)

    private val dualFrequency = impedancesOf(body50, body250)

    @Test
    fun `le solveur retrouve exactement les segments dont on est parti`() {
        val solved = KirchhoffSolver.solve(dualFrequency, LOW_FREQUENCY_KHZ)!!
        assertEquals(body50.rightArm, solved.rightArmOhms, 1e-9)
        assertEquals(body50.leftArm, solved.leftArmOhms, 1e-9)
        assertEquals(body50.rightLeg, solved.rightLegOhms, 1e-9)
        assertEquals(body50.leftLeg, solved.leftLegOhms, 1e-9)
        assertEquals(body50.trunk, solved.trunkOhms, 1e-9)
    }

    @Test
    fun `l'impedance globale ne depend que du pied-a-pied et du main-a-main`() {
        val solved = KirchhoffSolver.solve(dualFrequency, LOW_FREQUENCY_KHZ)!!
        assertEquals((415.0 + 617.0) / 4.0, solved.bodyOhms, 1e-9)
        assertEquals(
            0.25 * (body50.rightArm + body50.leftArm + body50.rightLeg + body50.leftLeg),
            solved.bodyOhms,
            1e-9
        )
    }

    @Test
    fun `la haute frequence traverse mieux que la basse`() {
        val low = KirchhoffSolver.solve(dualFrequency, LOW_FREQUENCY_KHZ)!!
        val high = KirchhoffSolver.solve(dualFrequency, HIGH_FREQUENCY_KHZ)!!
        assertTrue(high.bodyOhms < low.bodyOhms)
        assertTrue(high.trunkOhms < low.trunkOhms)
    }

    @Test
    fun `l'ecart entre deux membres homologues reste dans ce que les mesures autorisent`() {
        // par construction, quelle que soit la source qui l'annonce.
        val bound = KirchhoffSolver.maximumLimbSpread(dualFrequency, LOW_FREQUENCY_KHZ)!!
        val solved = KirchhoffSolver.solve(dualFrequency, LOW_FREQUENCY_KHZ)!!
        val armSpread = kotlin.math.abs(solved.rightArmOhms - solved.leftArmOhms) / 2.0
        assertTrue("écart $armSpread au-delà de la borne $bound", armSpread <= bound + 1e-9)
    }

    // --- Le moteur complet ---

    @Test
    fun `masse maigre et masse grasse referment la masse totale`() {
        val report = DexaBiaCalculator.calculate(massKg, athlete, dualFrequency)!!
        assertEquals(massKg, report.fatFreeMassKg + report.fatMassKg, 1e-9)
        assertEquals(100.0 * report.fatMassKg / massKg, report.bodyFatPercentage, 1e-9)
        assertTrue("gras ${report.bodyFatPercentage} %", report.bodyFatPercentage in 15.0..40.0)
    }

    @Test
    fun `les compartiments Brozek referment la masse maigre a cent pour cent`() {
        val report = DexaBiaCalculator.calculate(massKg, athlete, dualFrequency)!!
        assertEquals(
            report.fatFreeMassKg,
            report.totalBodyWaterKg + report.proteinMassKg + report.boneMineralMassKg,
            1e-9
        )
        assertEquals(
            report.totalBodyWaterKg,
            report.extracellularWaterKg + report.intracellularWaterKg,
            1e-9
        )
        assertTrue("ratio ECW/TBW ${report.ecwTbwRatio}", report.ecwTbwRatio in 0.35..0.45)
    }

    @Test
    fun `le muscle squelettique est une part de la masse maigre, pas son tout`() {
        val report = DexaBiaCalculator.calculate(massKg, athlete, dualFrequency)!!
        assertTrue(report.skeletalMuscleMassKg > 0.0)
        assertTrue(report.skeletalMuscleMassKg < report.fatFreeMassKg)
    }

    @Test
    fun `l'analyse porte les deux frequences relevees`() {
        val report = DexaBiaCalculator.calculate(massKg, athlete, dualFrequency)!!
        assertEquals(
            listOf(LOW_FREQUENCY_KHZ, HIGH_FREQUENCY_KHZ),
            report.segmental.map { it.frequencyKHz }
        )
    }

    @Test
    fun `une femme et un homme ne partagent pas la meme equation`() {
        val woman = BiaProfile(BiologicalSex.FEMALE, ageYears = 30, heightCm = 180.0)
        val forMan = DexaBiaCalculator.calculate(massKg, athlete, dualFrequency)!!
        val forWoman = DexaBiaCalculator.calculate(massKg, woman, dualFrequency)!!
        assertTrue(forWoman.fatFreeMassKg < forMan.fatFreeMassKg)
    }

    @Test
    fun `une pesee sans impedance ne produit aucune analyse`() {
        assertNull(DexaBiaCalculator.calculate(massKg, athlete, RawImpedances.NONE))
    }

    @Test
    fun `en mono-frequence la regression se reduit exactement a sa forme a une seule impedance`() {
        val flat = DexaBiaCalculator.calculate(massKg, athlete, impedancesOf(body50, body50))!!
        val monoFrequency = DexaBiaCalculator.calculate(massKg, athlete, impedancesOf(body50, null))!!
        assertEquals(flat.fatFreeMassKg, monoFrequency.fatFreeMassKg, 1e-9)
    }

    @Test
    fun `huit electrodes en mono-frequence gardent le segmentaire mais perdent l'hydratation`() {
        val report = DexaBiaCalculator.calculate(massKg, athlete, impedancesOf(body50, null))!!
        assertNotNull(report.segmentalMuscle)
        assertEquals(listOf(LOW_FREQUENCY_KHZ), report.segmental.map { it.frequencyKHz })
        assertEquals(0.380, report.ecwTbwRatio, 1e-9)
    }

    @Test
    fun `quatre electrodes livrent la composition globale et aucun membre`() {
        val feetOnly = RawImpedances.of(
            mapOf(
                ImpedanceReading(ImpedancePath.LEFT_FOOT_TO_RIGHT_FOOT, LOW_FREQUENCY_KHZ) to
                    (body50.leftLeg + body50.rightLeg)
            )
        )
        val report = DexaBiaCalculator.calculate(massKg, athlete, feetOnly)!!
        assertTrue("gras ${report.bodyFatPercentage} %", report.bodyFatPercentage in 15.0..40.0)
        assertEquals(
            report.fatFreeMassKg,
            report.totalBodyWaterKg + report.proteinMassKg + report.boneMineralMassKg,
            1e-9
        )
        assertNull(report.segmentalMuscle)
        assertTrue(report.segmental.isEmpty())
    }

    @Test
    fun `un homme et une femme n'ont pas la meme equation a quatre electrodes`() {
        val feetOnly = RawImpedances.of(
            mapOf(
                ImpedanceReading(ImpedancePath.LEFT_FOOT_TO_RIGHT_FOOT, LOW_FREQUENCY_KHZ) to 415.0
            )
        )
        val woman = BiaProfile(BiologicalSex.FEMALE, ageYears = 30, heightCm = 180.0)
        val forMan = DexaBiaCalculator.calculate(massKg, athlete, feetOnly)!!
        val forWoman = DexaBiaCalculator.calculate(massKg, woman, feetOnly)!!
        assertTrue(forWoman.fatFreeMassKg < forMan.fatFreeMassKg)
    }

    @Test
    fun `un trajet de bras manquant fait retomber sur les quatre electrodes, sans membre`() {
        val incomplete = RawImpedances.of(
            impedancesOf(body50, body250).ohmsByReading
                .filterKeys { it.path != ImpedancePath.RIGHT_HAND_TO_RIGHT_FOOT }
        )
        val report = DexaBiaCalculator.calculate(massKg, athlete, incomplete)!!
        assertNull(report.segmentalMuscle)
    }

    @Test
    fun `un zero de la trame ne devient jamais une resistance`() {
        // d'absence, jamais une valeur.
        val zeroed = RawImpedances.of(
            impedancesOf(body50, body250).ohmsByReading.mapValues { 0.0 }
        )
        assertTrue(zeroed.isEmpty)
        assertNull(DexaBiaCalculator.calculate(massKg, athlete, zeroed))
    }

    @Test
    fun `les jambes portent nettement plus de muscle que les bras`() {
        val muscle = DexaBiaCalculator.calculate(massKg, athlete, dualFrequency)!!.segmentalMuscle!!
        assertTrue(muscle.rightLegKg > 2.0 * muscle.rightArmKg)
        assertTrue(muscle.leftLegKg > 2.0 * muscle.leftArmKg)
    }

    @Test
    fun `les cinq segments referment la masse musculaire totale`() {
        val report = DexaBiaCalculator.calculate(massKg, athlete, dualFrequency)!!
        assertEquals(report.skeletalMuscleMassKg, report.segmentalMuscle!!.totalKg, 1e-9)
    }

    @Test
    fun `le membre le moins resistant porte la plus grosse part de muscle`() {
        val muscle = DexaBiaCalculator.calculate(massKg, athlete, dualFrequency)!!.segmentalMuscle!!
        assertTrue(body50.rightArm < body50.leftArm)
        assertTrue(muscle.rightArmKg > muscle.leftArmKg)
        assertTrue(body50.rightLeg < body50.leftLeg)
        assertTrue(muscle.rightLegKg > muscle.leftLegKg)
    }

    @Test
    fun `ponderer par la resistance opposee revient a ponderer par la conductance`() {
        val right = body50.rightArm
        val left = body50.leftArm
        assertEquals(
            left / (right + left),
            (1.0 / right) / ((1.0 / right) + (1.0 / left)),
            1e-12
        )
    }

    @Test
    fun `un corps parfaitement symetrique ne montre aucune asymetrie`() {
        val even = Body(rightArm = 308.5, leftArm = 308.5, rightLeg = 207.5, leftLeg = 207.5, trunk = 26.0)
        val muscle = DexaBiaCalculator.calculate(
            massKg, athlete, impedancesOf(even, body250)
        )!!.segmentalMuscle!!
        assertEquals(0.0, muscle.armAsymmetryPercent, 1e-9)
        assertEquals(0.0, muscle.legAsymmetryPercent, 1e-9)
        assertEquals(muscle.rightArmKg, muscle.leftArmKg, 1e-9)
    }

    @Test
    fun `l'indice de Baumgartner situe l'athlete sur sa grille clinique`() {
        val muscle = DexaBiaCalculator.calculate(massKg, athlete, dualFrequency)!!.segmentalMuscle!!
        // Sujet de test : 80 kg pour 180 cm. La grille masculine place le seuil de
        assertTrue("ASMM ${muscle.appendicularKg} kg", muscle.appendicularKg in 20.0..30.0)
        val smi = muscle.baumgartnerIndex(180.0)
        assertTrue("SMI $smi kg/m² hors de la grille", smi in 7.0..10.0)
    }

    @Test
    fun `un cycle de travail sur les bras remonte leur part de muscle`() {
        val trained = body50.copy(rightArm = 287.0, leftArm = 299.0)
        val before = DexaBiaCalculator.calculate(massKg, athlete, dualFrequency)!!.segmentalMuscle!!
        val after = DexaBiaCalculator.calculate(
            massKg, athlete, impedancesOf(trained, body250)
        )!!.segmentalMuscle!!

        val armShareBefore = (before.rightArmKg + before.leftArmKg) / before.appendicularKg
        val armShareAfter = (after.rightArmKg + after.leftArmKg) / after.appendicularKg
        assertTrue("part des bras $armShareBefore -> $armShareAfter", armShareAfter > armShareBefore)
    }

    @Test
    fun `un cycle de squat remonte la part des jambes`() {
        val trained = body50.copy(rightLeg = 194.0, leftLeg = 200.0)
        val before = DexaBiaCalculator.calculate(massKg, athlete, dualFrequency)!!.segmentalMuscle!!
        val after = DexaBiaCalculator.calculate(
            massKg, athlete, impedancesOf(trained, body250)
        )!!.segmentalMuscle!!

        val legShareBefore = (before.rightLegKg + before.leftLegKg) / before.appendicularKg
        val legShareAfter = (after.rightLegKg + after.leftLegKg) / after.appendicularKg
        assertTrue("part des jambes $legShareBefore -> $legShareAfter", legShareAfter > legShareBefore)
    }

    @Test
    fun `une morphologie de reference retrouve les fractions de population`() {
        val report = DexaBiaCalculator.calculate(massKg, athlete, dualFrequency)!!
        val muscle = report.segmentalMuscle!!
        val armFraction = (muscle.rightArmKg + muscle.leftArmKg) / report.skeletalMuscleMassKg
        val legFraction = (muscle.rightLegKg + muscle.leftLegKg) / report.skeletalMuscleMassKg
        assertEquals(0.170, armFraction, 0.02)
        assertEquals(0.480, legFraction, 0.02)
    }

    @Test
    fun `un bloc haute frequence entierement nul fait basculer en mono-frequence`() {
        // La trame porte toujours douze emplacements. Une balance qui ne monte pas en
        // c'est l'absence de mesure.
        val report = fromWireSlots(
            low = listOf(415.0, 617.0, 552.0, 545.0, 539.0, 532.0),
            high = listOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        )!!
        assertEquals(listOf(LOW_FREQUENCY_KHZ), report.segmental.map { it.frequencyKHz })
        assertNotNull(report.segmentalMuscle)
        assertEquals(0.380, report.ecwTbwRatio, 1e-9)
    }

    @Test
    fun `un seul emplacement renseigne fait basculer en quatre electrodes`() {
        val report = fromWireSlots(
            low = listOf(415.0, 0.0, 0.0, 0.0, 0.0, 0.0),
            high = listOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        )!!
        assertTrue("gras ${report.bodyFatPercentage} %", report.bodyFatPercentage in 15.0..40.0)
        assertNull(report.segmentalMuscle)
        assertTrue(report.segmental.isEmpty())
    }

    @Test
    fun `une trame entierement nulle ne produit aucune analyse`() {
        assertNull(
            fromWireSlots(
                low = listOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                high = listOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
            )
        )
    }

    /** Construit une pesée comme la trame la livre : douze emplacements, zéro compris. */
    private fun fromWireSlots(low: List<Double>, high: List<Double>) =
        DexaBiaCalculator.calculate(
            massKg,
            athlete,
            RawImpedances.of(
                buildMap {
                    ImpedancePath.BY_WIRE_INDEX.forEachIndexed { slot, path ->
                        put(ImpedanceReading(path, LOW_FREQUENCY_KHZ), low[slot])
                        put(ImpedanceReading(path, HIGH_FREQUENCY_KHZ), high[slot])
                    }
                }
            )
        )

    /** Un corps théorique : cinq conducteurs reliés à un nœud central. */
    private data class Body(
        val rightArm: Double,
        val leftArm: Double,
        val rightLeg: Double,
        val leftLeg: Double,
        val trunk: Double
    ) {
        /** Les six trajets qu'une balance mesurerait sur ce corps, par les lois des mailles. */
        fun measuredPaths(): Map<ImpedancePath, Double> = mapOf(
            ImpedancePath.LEFT_FOOT_TO_RIGHT_FOOT to leftLeg + rightLeg,
            ImpedancePath.LEFT_HAND_TO_RIGHT_HAND to leftArm + rightArm,
            ImpedancePath.LEFT_HAND_TO_LEFT_FOOT to leftArm + trunk + leftLeg,
            ImpedancePath.LEFT_HAND_TO_RIGHT_FOOT to leftArm + trunk + rightLeg,
            ImpedancePath.RIGHT_HAND_TO_LEFT_FOOT to rightArm + trunk + leftLeg,
            ImpedancePath.RIGHT_HAND_TO_RIGHT_FOOT to rightArm + trunk + rightLeg
        )
    }

    private fun impedancesOf(low: Body, high: Body?): RawImpedances {
        val readings = mutableMapOf<ImpedanceReading, Double>()
        low.measuredPaths().forEach { (path, ohms) ->
            readings[ImpedanceReading(path, LOW_FREQUENCY_KHZ)] = ohms
        }
        high?.measuredPaths()?.forEach { (path, ohms) ->
            readings[ImpedanceReading(path, HIGH_FREQUENCY_KHZ)] = ohms
        }
        return RawImpedances.of(readings)
    }
}
