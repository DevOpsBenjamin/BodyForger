package app.bodyforger.mobile.scale

import app.bodyforger.core.ble.AthleteInstruction
import app.bodyforger.core.ble.DiscoveredScale
import app.bodyforger.core.ble.SessionFailure
import app.bodyforger.core.ble.WeighInState
import app.bodyforger.core.model.BodyLog
import app.bodyforger.core.model.ScaleAssociation

/**
 * What the scale screen shows at a given moment.
 *
 * A device is discovered before being associated: the address comes from the native scan,
 * never from typing.
 */
data class ScaleUiState(
    val isScanning: Boolean = false,
    val discovered: List<DiscoveredScale> = emptyList(),
    val association: ScaleAssociation? = null,
    val huid: String? = null,
    val progress: WeighInState.Progress? = null,
    val lastLog: BodyLog? = null,
    /**
     * A mass read without a body fat percentage to go with it.
     *
     * Nothing is stored while that percentage is missing; the reading waits for an entry.
     */
    val weightAwaitingBodyFat: Double? = null,
    val failure: SessionFailure? = null,
    /** Message from a system-refused scan, distinct from finding nothing. */
    val scanError: String? = null,
    val isWeighing: Boolean = false,
    val isPairing: Boolean = false,
    /** Current pairing step, over its total. */
    val pairingStep: Pair<Int, Int>? = null,
    val pairingInstructions: List<AthleteInstruction> = emptyList()
) {
    val isAssociated: Boolean get() = association != null
}
