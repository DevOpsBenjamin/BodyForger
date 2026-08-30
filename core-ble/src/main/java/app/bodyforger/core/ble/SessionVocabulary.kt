package app.bodyforger.core.ble

/**
 * Terms shared by every scale, whatever its family.
 *
 * The core knows only these; a driver owns its own sequence, its step count and their order.
 */
enum class SessionPhase {
    DISCOVERING,
    /** Connecting, authenticating, configuring — no athlete involvement. */
    PREPARING,
    /** The hardware waits on a physical action: see [AthleteInstruction]. */
    AWAITING_ATHLETE,
    MEASURING
}

/**
 * A physical action asked of the athlete.
 *
 * Deliberately small and closed: the interface knows how to render each one, and drivers only
 * choose which to ask for and when. Adding one is a design decision, not a driver detail.
 */
enum class AthleteInstruction {
    /** Tap the scale so it advertises; without this it stays invisible to the scan. */
    TAP_SCALE_TO_WAKE,
    STAY_OFF_PLATFORM,
    /** Step on for a weight-only reading, where skin contact does not matter. */
    STEP_ON,
    /** Step on barefoot, contact conditioning the impedance reading. */
    STEP_ON_BAREFOOT,
    /** Grip the retractable handle with both hands; without it no impedance is read. */
    GRIP_HANDLE,
    STEP_OFF
}

/** Why a session stopped before completing. */
enum class SessionFailure {
    DEVICE_NOT_FOUND,
    CONNECTION_LOST,
    /** The hardware rejected the authentication or the user profile. */
    REJECTED_BY_DEVICE,
    /** The athlete never acted, or the reading never stabilised. */
    TIMED_OUT,
    NOT_ASSOCIATED,
    DEVICE_ERROR
}
