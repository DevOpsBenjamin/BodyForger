package app.bodyforger.core.ble

import app.bodyforger.core.model.ElectrodeCount
import app.bodyforger.core.model.ImpedancePath
import app.bodyforger.core.model.ImpedanceReading.Companion.HIGH_FREQUENCY_KHZ
import app.bodyforger.core.model.ImpedanceReading.Companion.LOW_FREQUENCY_KHZ
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Vecteurs de non-régression construits sur des **trames réellement capturées**, pas
 * synthétiques. Trois viennent de la Scale 3 Pro de l'équipe, la quatrième d'une capture
 * publiée par openScale sur une Scale 3 classique.
 */
class BiaTelemetryDecoderTest {

    /** Scale 3 Pro, pesée complète avec la poignée saisie — 2026-08-23. */
    private val proWithHandle = hex(
        "be284901ea070817112e2d074a0e56146712a3119512cb115300ed0c221270109f0fcd10fd0f"
    )

    /** Scale 3 Pro, seconde pesée complète — 2026-08-23. */
    private val proWithHandleSecond = hex(
        "c3284601ea07081711080e07250e3d145c1298117512a7115200c20cfc115a108d0fd210f20f"
    )

    /** Scale 3 Pro, pieds nus **sans saisir la poignée** — 2026-08-29. */
    private val proWithoutHandle = hex(
        "c8280000ea07081d0d062f060000000000000000000000000000000000000000000000000000"
    )

    /** Scale 3 classique (`M00D`), quatre électrodes — capture openScale du 2026-08-09. */
    private val plainScaleThree = hex("b1212d01ea07080917271da0ea13000000000000000000006100")

    @Test
    fun `une pesee Pro complete livre les douze resistances`() {
        val telemetry = requireNotNull(BiaTelemetryDecoder.decode(proWithHandle))

        assertEquals(104.30, telemetry.massKg, 1e-9)
        assertEquals(32.9, telemetry.bodyFatPercentage!!, 1e-9)
        assertEquals(83, telemetry.heartRateBpm)
        assertEquals(12, telemetry.rawImpedances.ohmsByReading.size)
        assertEquals(ElectrodeCount.EIGHT, telemetry.fidelityElectrodeCount())
        assertEquals(
            listOf(LOW_FREQUENCY_KHZ, HIGH_FREQUENCY_KHZ),
            telemetry.rawImpedances.fidelity.frequenciesKHz
        )
    }

    @Test
    fun `les resistances sont des dixiemes d'ohm, sans heuristique de magnitude`() {
        val telemetry = BiaTelemetryDecoder.decode(proWithHandle)!!

        // Compteur brut 3658 : l'heuristique d'openScale le rendrait en 3658 Ω.
        assertEquals(
            365.8,
            telemetry.rawImpedances[ImpedancePath.LEFT_FOOT_TO_RIGHT_FOOT, LOW_FREQUENCY_KHZ]!!,
            1e-9
        )
        assertEquals(
            520.6,
            telemetry.rawImpedances[ImpedancePath.LEFT_HAND_TO_RIGHT_HAND, LOW_FREQUENCY_KHZ]!!,
            1e-9
        )
    }

    @Test
    fun `la haute frequence est systematiquement inferieure a la basse`() {
        for (frame in listOf(proWithHandle, proWithHandleSecond)) {
            val impedances = BiaTelemetryDecoder.decode(frame)!!.rawImpedances
            for (path in ImpedancePath.entries) {
                val low = impedances[path, LOW_FREQUENCY_KHZ]!!
                val high = impedances[path, HIGH_FREQUENCY_KHZ]!!
                assertTrue("$path : $high devrait être sous $low", high < low)
            }
        }
    }

    @Test
    fun `une Pro sans poignee reste une pesee valide, sans aucune impedance`() {
        val telemetry = requireNotNull(BiaTelemetryDecoder.decode(proWithoutHandle))

        // La trame fait bien 38 octets : la longueur ne dit rien de la capacité.
        assertEquals(BiaTelemetryDecoder.DUAL_FREQUENCY_FRAME_BYTES, proWithoutHandle.size)
        assertEquals(104.40, telemetry.massKg, 1e-9)
        assertTrue(telemetry.rawImpedances.isEmpty)
        assertEquals(ElectrodeCount.NONE, telemetry.fidelityElectrodeCount())
    }

    @Test
    fun `masse grasse et rythme cardiaque a zero sont des absences, pas des valeurs`() {
        val telemetry = BiaTelemetryDecoder.decode(proWithoutHandle)!!

        assertNull("un BodyLog à 0 % de masse grasse serait un mensonge", telemetry.bodyFatPercentage)
        assertNull(telemetry.heartRateBpm)
    }

    @Test
    fun `une balance quatre electrodes ne renseigne que le trajet pied a pied`() {
        val telemetry = requireNotNull(BiaTelemetryDecoder.decode(plainScaleThree))

        assertEquals(BiaTelemetryDecoder.MIN_FRAME_BYTES, plainScaleThree.size)
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
        val measured = BiaTelemetryDecoder.decode(plainScaleThree)!!
            .rawImpedances[ImpedancePath.LEFT_FOOT_TO_RIGHT_FOOT, LOW_FREQUENCY_KHZ]!!

        // L'ancien `?: 500.0` serait indiscernable de cette mesure authentique.
        assertTrue(measured in 400.0..600.0)
    }

    @Test
    fun `l'horodatage est lu tel que la balance l'emet`() {
        val telemetry = BiaTelemetryDecoder.decode(proWithoutHandle)!!.measuredAt!!

        assertEquals(2026, telemetry.year)
        assertEquals(8, telemetry.monthValue)
        assertEquals(29, telemetry.dayOfMonth)
        assertEquals(13, telemetry.hour)
        assertEquals(6, telemetry.minute)
        assertEquals(47, telemetry.second)
    }

    @Test
    fun `l'octet de statut est expose brut, sans interpretation`() {
        // Sur la Pro il vaut le jour ISO — 2026-08-23 est un dimanche, 2026-08-29 un samedi.
        assertEquals(7, BiaTelemetryDecoder.decode(proWithHandle)!!.statusByte)
        assertEquals(6, BiaTelemetryDecoder.decode(proWithoutHandle)!!.statusByte)
        // La capture M00D ne suit pas cette lecture : d'où l'absence d'interprétation.
        assertEquals(0xa0, BiaTelemetryDecoder.decode(plainScaleThree)!!.statusByte)
    }

    @Test
    fun `une trame trop courte est refusee`() {
        assertNull(BiaTelemetryDecoder.decode(ByteArray(25)))
        assertNull(BiaTelemetryDecoder.decode(ByteArray(0)))
    }

    private fun BiaTelemetry.fidelityElectrodeCount() =
        rawImpedances.fidelity.exercisedElectrodeCount

    private fun hex(value: String): ByteArray =
        ByteArray(value.length / 2) { value.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
}
