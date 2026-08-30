package app.bodyforger.core.ble

import app.bodyforger.core.model.RawImpedances
import java.time.LocalDateTime

/**
 * Ce qu'une pesée a livré, indépendamment du matériel qui l'a produite.
 *
 * Toute grandeur que l'appareil n'a pas mesurée est `null` — jamais un zéro, jamais un
 * défaut. Les pilotes traduisent les conventions d'absence propres à leur famille (champ
 * à zéro, valeur sentinelle, champ absent de la trame) en `null` avant d'arriver ici.
 */
data class BiaTelemetry(
    val massKg: Double,
    val bodyFatPercentage: Double?,
    val heartRateBpm: Int?,
    val measuredAt: LocalDateTime?,
    val rawImpedances: RawImpedances
)
