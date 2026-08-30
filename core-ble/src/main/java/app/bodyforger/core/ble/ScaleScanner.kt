package app.bodyforger.core.ble

import kotlinx.coroutines.flow.Flow

/**
 * A scale spotted during a scan, before any connection.
 *
 * [recognised] is `null` for an unknown device. Unknown devices are shown rather than hidden:
 * seeing the name actually advertised is the only way to notice a scale the name filter
 * misses — `docs/BLE_PROTOCOL.md` §1.
 */
data class DiscoveredScale(
    val deviceAddress: String,
    val advertisedName: String,
    val recognised: RecognisedScale?,
    val signalStrengthDbm: Int
) {
    val isCompatible: Boolean get() = recognised != null
}

/** The scan was refused by the system, which is not the same as finding nothing. */
class ScanRejected(val errorCode: Int, override val message: String) : Exception(message)

/**
 * All a scan needs in order to sort what it hears: recognise a scale by its advertised name.
 *
 * Narrower than [ScaleDriver] on purpose — scanning does not require knowing how to pair or
 * weigh, and demanding it would mean writing a full driver before spotting a first device.
 */
fun interface ScaleIdentifier {
    fun identify(advertisedName: String?): RecognisedScale?
}

interface ScaleScanner {
    /**
     * Emits advertising devices while collected, recognised or not.
     *
     * A scale advertises repeatedly, so the same device is emitted many times with a varying
     * signal strength; deduplicating by address is the caller's business.
     */
    fun scan(identifier: ScaleIdentifier): Flow<DiscoveredScale>
}
