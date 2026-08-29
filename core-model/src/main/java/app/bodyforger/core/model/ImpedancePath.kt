package app.bodyforger.core.model

/**
 * An anatomical path along which a scale measures a resistance.
 *
 * A path is **not** a body zone: all six cross both the limbs and the trunk. Segmental
 * composition follows from Kirchhoff's laws — `docs/BIA_ENGINE.md` §2.
 *
 * [wireIndex] is the path's position in the telemetry frame, identical at both frequencies.
 */
enum class ImpedancePath(val wireIndex: Int, val involvesHands: Boolean) {
    LEFT_FOOT_TO_RIGHT_FOOT(0, involvesHands = false),
    LEFT_HAND_TO_RIGHT_HAND(1, involvesHands = true),
    LEFT_HAND_TO_LEFT_FOOT(2, involvesHands = true),
    LEFT_HAND_TO_RIGHT_FOOT(3, involvesHands = true),
    RIGHT_HAND_TO_LEFT_FOOT(4, involvesHands = true),
    RIGHT_HAND_TO_RIGHT_FOOT(5, involvesHands = true);

    companion object {
        /** The six paths, in the order the frame carries them. */
        val BY_WIRE_INDEX: List<ImpedancePath> = entries.sortedBy { it.wireIndex }
    }
}

/**
 * A resistance identified by its path and excitation frequency.
 *
 * Frequency is carried in kilohertz rather than as a dual-frequency flag, so a three-frequency
 * device would not force a redesign.
 */
data class ImpedanceReading(val path: ImpedancePath, val frequencyKHz: Int) {
    init {
        require(frequencyKHz > 0) { "Invalid frequency: $frequencyKHz kHz" }
    }

    companion object {
        const val LOW_FREQUENCY_KHZ: Int = 50
        const val HIGH_FREQUENCY_KHZ: Int = 250
    }
}
