package app.bodyforger.core.ble

import app.bodyforger.core.model.ScaleAssociation

/**
 * How a weigh-in is progressing.
 *
 * A weigh-in is a sequence of tens of seconds, which the athlete follows and can interrupt —
 * `docs/BLE_PROTOCOL.md` §6.
 */
sealed interface WeighInState {

    /**
     * Step [index] of [totalSteps], as the driver cuts its sequence.
     *
     * A step may ask for several simultaneous actions: stepping on and gripping the handle
     * happen together, and separating them would have the handle released before the reading.
     *
     * [detail] is a driver-owned label for logs and diagnosis, never for driving the
     * interface, which reads [phase] and [instructions].
     */
    data class Progress(
        val index: Int,
        val totalSteps: Int,
        val phase: SessionPhase,
        val instructions: List<AthleteInstruction> = emptyList(),
        val detail: String? = null
    ) : WeighInState

    /** Mass settling, where the hardware streams it live. */
    data class LiveWeight(val massKg: Double) : WeighInState

    /**
     * The scale delivered its reading.
     *
     * A success even with no impedance at all: an eight-electrode scale whose handle was not
     * gripped emits a complete frame carrying mass alone, and acknowledges the weigh-in.
     */
    data class Completed(val telemetry: BiaTelemetry) : WeighInState

    data class Failed(val reason: SessionFailure, val cause: Throwable? = null) : WeighInState
}

/**
 * How a pairing is progressing.
 *
 * The course depends entirely on the hardware — see [PairingRequirement] and
 * `docs/BLE_PROTOCOL.md` §5.
 */
sealed interface PairingState {

    data class Progress(
        val index: Int,
        val totalSteps: Int,
        val phase: SessionPhase,
        val instructions: List<AthleteInstruction> = emptyList(),
        val detail: String? = null
    ) : PairingState

    /**
     * The association is established and can be shared between watch and phone.
     *
     * [validation] carries the reading taken during pairing where the hardware produces one:
     * the athlete already stepped on, so keeping it spares asking again.
     */
    data class Completed(
        val association: ScaleAssociation,
        val validation: BiaTelemetry? = null
    ) : PairingState

    data class Failed(val reason: SessionFailure, val cause: Throwable? = null) : PairingState
}

/** What a piece of hardware requires before day-to-day use. */
enum class PairingRequirement {
    /** Nothing to pair: the scale broadcasts or simply answers. */
    NONE,
    /** An association suffices, with no physical action from the athlete. */
    HANDSHAKE_ONLY,
    /** The hardware engraves a profile and requires a validation weigh-in. */
    WEIGH_IN_REQUIRED
}
