package app.bodyforger.core.ble

import app.bodyforger.core.model.ImpedancePath
import app.bodyforger.core.model.ImpedanceReading
import app.bodyforger.core.model.RawImpedances
import java.time.LocalDateTime

/**
 * Une trame de télémétrie déchiffrée, telle que la balance l'a émise.
 *
 * Toute grandeur que l'appareil n'a pas mesurée est `null` — jamais un zéro, jamais un
 * défaut. La balance signale l'absence en remplissant le champ de zéros ; c'est le décodeur
 * qui traduit cette convention en absence.
 */
data class BiaTelemetry(
    val massKg: Double,
    val bodyFatPercentage: Double?,
    val heartRateBpm: Int?,
    val measuredAt: LocalDateTime?,
    val rawImpedances: RawImpedances,
    /**
     * Octet 11 de la trame. Sur la Scale 3 Pro c'est le jour ISO de la semaine (1 = lundi),
     * vérifié sur deux jours distincts. La seule capture `M00D` connue y porte `0xa0`, que
     * cette lecture n'explique pas : le champ est donc exposé brut, sans interprétation.
     */
    val statusByte: Int
)

/**
 * Décodeur de la trame de bio-impédance temps réel (`0x97`) de la famille Haige.
 *
 * Disposition (`TECH.md` §6.2), identique sur les deux longueurs de trame :
 * ```
 * 0..2   poids           uint16_le / 100      kg
 * 2..4   masse grasse    uint16_le / 10       %
 * 4..11  horodatage      année, mois, jour, heure, minute, seconde
 * 11     statut          uint8
 * 12..24 six trajets     uint16_le / 10       Ω, à basse fréquence
 * 24..26 rythme cardiaque uint16_le           bpm
 * 26..38 six trajets     uint16_le / 10       Ω, à haute fréquence — absents d'une trame courte
 * ```
 */
object BiaTelemetryDecoder {

    /** Longueur minimale : en deçà, la trame ne porte même pas le bloc basse fréquence. */
    const val MIN_FRAME_BYTES = 26

    /** Longueur à partir de laquelle le bloc haute fréquence est présent. */
    const val DUAL_FREQUENCY_FRAME_BYTES = 38

    private const val LOW_FREQUENCY_BLOCK_OFFSET = 12
    private const val HIGH_FREQUENCY_BLOCK_OFFSET = 26
    private const val HEART_RATE_OFFSET = 24

    /**
     * Le facteur d'échelle des résistances est **fixe** sur cette famille : les compteurs
     * bruts sont des dixièmes d'ohm.
     *
     * openScale désambiguïse par magnitude (`1..3999` lus en ohms, `4000..39999` divisés par
     * dix). Cette heuristique se trompe sur nos relevés : une lecture Pro authentique vaut
     * `3658`, qu'elle rendrait en 3658 Ω au lieu de 365,8 Ω.
     */
    private const val OHM_SCALE = 10.0

    /** Plage plausible d'un rythme cardiaque ; hors d'elle, la valeur est tenue pour absente. */
    private val PLAUSIBLE_HEART_RATE = 1..240

    /**
     * Décode une trame déchiffrée.
     *
     * @return la télémétrie, ou `null` si la trame est trop courte pour être interprétée.
     */
    fun decode(payload: ByteArray): BiaTelemetry? {
        if (payload.size < MIN_FRAME_BYTES) return null

        val hasHighFrequency = payload.size >= DUAL_FREQUENCY_FRAME_BYTES

        val readings = buildMap {
            putBlock(payload, LOW_FREQUENCY_BLOCK_OFFSET, ImpedanceReading.LOW_FREQUENCY_KHZ)
            if (hasHighFrequency) {
                putBlock(payload, HIGH_FREQUENCY_BLOCK_OFFSET, ImpedanceReading.HIGH_FREQUENCY_KHZ)
            }
        }

        return BiaTelemetry(
            massKg = u16(payload, 0) / 100.0,
            bodyFatPercentage = u16(payload, 2).takeIf { it > 0 }?.let { it / 10.0 },
            heartRateBpm = u16(payload, HEART_RATE_OFFSET).takeIf { it in PLAUSIBLE_HEART_RATE },
            measuredAt = readTimestamp(payload),
            rawImpedances = RawImpedances.of(readings),
            statusByte = payload[11].toInt() and 0xFF
        )
    }

    /**
     * Lit les six trajets d'un bloc. Un compteur nul signifie « non mesuré » : l'entrée est
     * omise plutôt que portée à zéro.
     */
    private fun MutableMap<ImpedanceReading, Double>.putBlock(
        payload: ByteArray,
        offset: Int,
        frequencyKHz: Int
    ) {
        for (path in ImpedancePath.BY_WIRE_INDEX) {
            val raw = u16(payload, offset + path.wireIndex * 2)
            if (raw > 0) {
                put(ImpedanceReading(path, frequencyKHz), raw / OHM_SCALE)
            }
        }
    }

    private fun readTimestamp(payload: ByteArray): LocalDateTime? {
        val year = u16(payload, 4)
        if (year < 2000) return null
        return runCatching {
            LocalDateTime.of(
                year,
                payload[6].toInt() and 0xFF,
                payload[7].toInt() and 0xFF,
                payload[8].toInt() and 0xFF,
                payload[9].toInt() and 0xFF,
                payload[10].toInt() and 0xFF
            )
        }.getOrNull()
    }

    private fun u16(payload: ByteArray, offset: Int): Int =
        (payload[offset].toInt() and 0xFF) or ((payload[offset + 1].toInt() and 0xFF) shl 8)
}
