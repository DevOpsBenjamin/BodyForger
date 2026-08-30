package app.bodyforger.core.ble

import app.bodyforger.core.model.ScaleAssociation
import app.bodyforger.core.model.ScaleUserProfile
import kotlinx.coroutines.flow.Flow

/**
 * The hardware adapter for a family of scales.
 *
 * A driver owns its own sequences, frames and cryptography. The core knows only the shared
 * vocabulary and assumes nothing about step count, order, or even that a connection exists.
 * Nothing vendor-specific crosses this interface.
 */
interface ScaleDriver : ScaleIdentifier {

    /** Stable driver id, such as `huawei_haige`. */
    val id: String

    /** Readable name of the supported family. */
    val name: String

    /** What this hardware requires before day-to-day use. */
    val pairingRequirement: PairingRequirement

    /** Recognises a scale from its advertised name, or `null` when it is not ours. */
    override fun identify(advertisedName: String?): RecognisedScale?

    /** Runs the initial pairing and produces the association. */
    fun pair(
        deviceAddress: String,
        advertisedName: String,
        profile: ScaleUserProfile
    ): Flow<PairingState>

    /** Runs one weigh-in against an established association. */
    fun weighIn(
        association: ScaleAssociation,
        profile: ScaleUserProfile
    ): Flow<WeighInState>
}

/**
 * The driver registry: picks the one able to talk to a given scale.
 *
 * List order decides overlaps — a specific driver must precede a generic one, such as the
 * standard Bluetooth SIG profile.
 */
class ScaleDriverRegistry(private val drivers: List<ScaleDriver>) {

    /** Le premier pilote qui reconnaît ce nom annoncé, avec ce qu'il en a déduit. */
    fun identify(advertisedName: String?): Match? =
        drivers.firstNotNullOfOrNull { driver ->
            driver.identify(advertisedName)?.let { Match(driver, it) }
        }

    /** Le pilote capable de piloter cette Association. */
    fun driverFor(association: ScaleAssociation): ScaleDriver? =
        identify(association.advertisedName)?.driver

    data class Match(val driver: ScaleDriver, val scale: RecognisedScale)
}
