package app.bodyforger.core.ble.huawei

import app.bodyforger.core.ble.BiaTelemetry
import app.bodyforger.core.model.ElectrodeCount
import app.bodyforger.core.model.ImpedancePath
import app.bodyforger.core.model.ImpedanceReading.Companion.HIGH_FREQUENCY_KHZ
import app.bodyforger.core.model.ImpedanceReading.Companion.LOW_FREQUENCY_KHZ
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Vecteurs de non-régression.
 *
 * Les trois trames Pro sont **synthétiques** : elles reproduisent au bit près la structure
 * d'une trame réelle — mêmes offsets, mêmes échelles, mêmes conventions d'absence — mais ne
 * portent la pesée de personne. Un vecteur capturé n'apporterait rien de plus au décodeur,
 * dont le travail est de lire une structure, et exposerait des données de santé.
 *
 * La quatrième vient d'une capture **publiée par openScale** sur une Scale 3 classique : sa
 * valeur est justement de venir d'un autre matériel et d'une autre implémentation.
 */
class HuaweiTelemetryDecoderTest {

    /** Scale 3 Pro, pesée complète avec la poignée saisie. */
    private val proWithHandle = hex(
        "401fb900e9070309071e000736101a1890154a150e15c8144800740e861542130613ca128e12"
    )

    /** Scale 3 Pro, seconde pesée complète. */
    private val proWithHandleSecond = hex(
        "4f1fbb00e9070309072d0c0754104218b81572153615f0144a00920ea41560132413e812ac12"
    )

    /**
      * Scale 3 Pro, pieds nus **sans saisir la poignée**. Reproduit le cas prouvé par
      * capture réelle : trente-huit octets entièrement nuls hormis le poids et la date.
      */
    private val proWithoutHandle = hex(
        "b81f0000e907030f121405060000000000000000000000000000000000000000000000000000"
    )

    /** Scale 3 classique (`M00D`), quatre électrodes — capture openScale du 2026-08-09. */
    private val plainScaleThree = hex("b1212d01ea07080917271da0ea13000000000000000000006100")

    @Test
    fun `une pesee Pro complete livre les douze resistances`() {
        val telemetry = requireNotNull(HuaweiTelemetryDecoder.decode(proWithHandle, HuaweiScaleModel.HUAWEI_SCALE_3_PRO)).telemetry

        assertEquals(80.00, telemetry.massKg, 1e-9)
        assertEquals(18.5, telemetry.bodyFatPercentage!!, 1e-9)
        assertEquals(72, telemetry.heartRateBpm)
        assertEquals(12, telemetry.rawImpedances.ohmsByReading.size)
        assertEquals(ElectrodeCount.EIGHT, telemetry.fidelityElectrodeCount())
        assertEquals(
            listOf(LOW_FREQUENCY_KHZ, HIGH_FREQUENCY_KHZ),
            telemetry.rawImpedances.fidelity.frequenciesKHz
        )
    }

    @Test
    fun `les resistances sont des dixiemes d'ohm, sans heuristique de magnitude`() {
        val telemetry = HuaweiTelemetryDecoder.decode(proWithHandle, HuaweiScaleModel.HUAWEI_SCALE_3_PRO)!!.telemetry

        // Compteur brut 4150 : l'heuristique d'openScale le rendrait en 4150 Ω.
        assertEquals(
            415.0,
            telemetry.rawImpedances[ImpedancePath.LEFT_FOOT_TO_RIGHT_FOOT, LOW_FREQUENCY_KHZ]!!,
            1e-9
        )
        assertEquals(
            617.0,
            telemetry.rawImpedances[ImpedancePath.LEFT_HAND_TO_RIGHT_HAND, LOW_FREQUENCY_KHZ]!!,
            1e-9
        )
    }

    @Test
    fun `la haute frequence est systematiquement inferieure a la basse`() {
        for (frame in listOf(proWithHandle, proWithHandleSecond)) {
            val impedances = HuaweiTelemetryDecoder.decode(frame, HuaweiScaleModel.HUAWEI_SCALE_3_PRO)!!.telemetry.rawImpedances
            for (path in ImpedancePath.entries) {
                val low = impedances[path, LOW_FREQUENCY_KHZ]!!
                val high = impedances[path, HIGH_FREQUENCY_KHZ]!!
                assertTrue("$path : $high devrait être sous $low", high < low)
            }
        }
    }

    @Test
    fun `une Pro sans poignee reste une pesee valide, sans aucune impedance`() {
        val telemetry = requireNotNull(HuaweiTelemetryDecoder.decode(proWithoutHandle, HuaweiScaleModel.HUAWEI_SCALE_3_PRO)).telemetry

        // La trame fait bien 38 octets : la longueur ne dit rien de la capacité.
        assertEquals(HuaweiTelemetryDecoder.DUAL_FREQUENCY_FRAME_BYTES, proWithoutHandle.size)
        assertEquals(81.20, telemetry.massKg, 1e-9)
        assertTrue(telemetry.rawImpedances.isEmpty)
        assertEquals(ElectrodeCount.NONE, telemetry.fidelityElectrodeCount())
    }

    @Test
    fun `masse grasse et rythme cardiaque a zero sont des absences, pas des valeurs`() {
        val telemetry = HuaweiTelemetryDecoder.decode(proWithoutHandle, HuaweiScaleModel.HUAWEI_SCALE_3_PRO)!!.telemetry

        assertNull("un BodyLog à 0 % de masse grasse serait un mensonge", telemetry.bodyFatPercentage)
        assertNull(telemetry.heartRateBpm)
    }

    @Test
    fun `une balance quatre electrodes ne renseigne que le trajet pied a pied`() {
        val telemetry = requireNotNull(HuaweiTelemetryDecoder.decode(plainScaleThree, HuaweiScaleModel.HUAWEI_SCALE_3)).telemetry

        assertEquals(HuaweiTelemetryDecoder.MIN_FRAME_BYTES, plainScaleThree.size)
        assertEquals(86.25, telemetry.massKg, 1e-9)
        assertEquals(30.1, telemetry.bodyFatPercentage!!, 1e-9)
        assertEquals(97, telemetry.heartRateBpm)

        assertEquals(
            setOf(ImpedancePath.LEFT_FOOT_TO_RIGHT_FOOT),
            telemetry.rawImpedances.fidelity.paths
        )
        assertEquals(ElectrodeCount.FOUR, telemetry.fidelityElectrodeCount())
        assertEquals(
            509.8,
            telemetry.rawImpedances[ImpedancePath.LEFT_FOOT_TO_RIGHT_FOOT, LOW_FREQUENCY_KHZ]!!,
            1e-9
        )
    }

    @Test
    fun `une lecture reelle tombe sur l'ancienne valeur de repli — d'ou sa suppression`() {
        val measured = HuaweiTelemetryDecoder.decode(plainScaleThree, HuaweiScaleModel.HUAWEI_SCALE_3)!!.telemetry
            .rawImpedances[ImpedancePath.LEFT_FOOT_TO_RIGHT_FOOT, LOW_FREQUENCY_KHZ]!!

        // L'ancien `?: 500.0` serait indiscernable de cette mesure authentique.
        assertTrue(measured in 400.0..600.0)
    }

    @Test
    fun `l'horodatage est lu tel que la balance l'emet`() {
        val telemetry = HuaweiTelemetryDecoder.decode(proWithoutHandle, HuaweiScaleModel.HUAWEI_SCALE_3_PRO)!!.telemetry.measuredAt!!

        assertEquals(2025, telemetry.year)
        assertEquals(3, telemetry.monthValue)
        assertEquals(15, telemetry.dayOfMonth)
        assertEquals(18, telemetry.hour)
        assertEquals(20, telemetry.minute)
        assertEquals(5, telemetry.second)
    }

    @Test
    fun `l'octet de statut est expose brut, sans interpretation`() {
        // Sur la Pro il vaut le jour ISO — les trames portent un dimanche puis un samedi.
        assertEquals(7, HuaweiTelemetryDecoder.decode(proWithHandle, HuaweiScaleModel.HUAWEI_SCALE_3_PRO)!!.statusByte)
        assertEquals(6, HuaweiTelemetryDecoder.decode(proWithoutHandle, HuaweiScaleModel.HUAWEI_SCALE_3_PRO)!!.statusByte)
        // La capture M00D ne suit pas cette lecture : d'où l'absence d'interprétation.
        assertEquals(0xa0, HuaweiTelemetryDecoder.decode(plainScaleThree, HuaweiScaleModel.HUAWEI_SCALE_3)!!.statusByte)
    }

    @Test
    fun `une trame trop courte est refusee`() {
        assertNull(HuaweiTelemetryDecoder.decode(ByteArray(25), HuaweiScaleModel.HUAWEI_SCALE_3_PRO))
        assertNull(HuaweiTelemetryDecoder.decode(ByteArray(0), HuaweiScaleModel.HUAWEI_SCALE_3_PRO))
    }

    private fun BiaTelemetry.fidelityElectrodeCount() =
        rawImpedances.fidelity.exercisedElectrodeCount

    private fun hex(value: String): ByteArray =
        ByteArray(value.length / 2) { value.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
}
