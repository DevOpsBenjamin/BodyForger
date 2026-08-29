package app.bodyforger.core.model

/**
 * What a reading actually captured: the set of resistances it obtained.
 *
 * Lives on the reading, not on the association. It is read from the frame, never declared —
 * the scale fills with zeros what it did not measure. See [ScaleCapability] for the ceiling.
 */
@JvmInline
value class MeasuredFidelity(val readings: Set<ImpedanceReading>) {

    val paths: Set<ImpedancePath> get() = readings.mapTo(mutableSetOf()) { it.path }

    val frequenciesKHz: List<Int> get() = readings.map { it.frequencyKHz }.distinct().sorted()

    val isEmpty: Boolean get() = readings.isEmpty()

    /** Electrode count actually exercised, deduced from the paths read. */
    val exercisedElectrodeCount: ElectrodeCount
        get() = when {
            readings.any { it.path.involvesHands } -> ElectrodeCount.EIGHT
            readings.isNotEmpty() -> ElectrodeCount.FOUR
            else -> ElectrodeCount.NONE
        }

    companion object {
        /** A reading with no impedance at all: mass only. */
        val NONE = MeasuredFidelity(emptySet())
    }
}

/**
 * The raw resistances of a reading, in ohms — the only quantity a scale truly measures.
 *
 * Kept verbatim and forever, which is what allows the whole history to be recomputed when the
 * equations improve. A missing value is never replaced by a default: see `docs/BIA_ENGINE.md`.
 */
@JvmInline
value class RawImpedances private constructor(val ohmsByReading: Map<ImpedanceReading, Double>) {

    val fidelity: MeasuredFidelity get() = MeasuredFidelity(ohmsByReading.keys)

    val isEmpty: Boolean get() = ohmsByReading.isEmpty()

    /** The resistance read on this path at this frequency, or `null` when not measured. */
    operator fun get(path: ImpedancePath, frequencyKHz: Int): Double? =
        ohmsByReading[ImpedanceReading(path, frequencyKHz)]

    operator fun get(reading: ImpedanceReading): Double? = ohmsByReading[reading]

    companion object {
        /** A reading carrying no impedance. */
        val NONE = RawImpedances(emptyMap())

        /** Builds a reading, rejecting anything not measured: zero means "not measured". */
        fun of(ohmsByReading: Map<ImpedanceReading, Double>): RawImpedances =
            RawImpedances(ohmsByReading.filterValues { it > 0.0 }.toMap())
    }
}
