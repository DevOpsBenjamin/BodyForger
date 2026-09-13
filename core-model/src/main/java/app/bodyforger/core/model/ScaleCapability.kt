package app.bodyforger.core.model

/** Electrodes in contact with the body during a reading. */
enum class ElectrodeCount(val electrodes: Int) {
    /** No electrodes: weight-only scale, or manual entry. */
    NONE(0),

    /** Four plate electrodes: only the foot-to-foot path exists. */
    FOUR(4),

    /** Four on the plate and four on the handle: all six paths exist. */
    EIGHT(8)
}

/**
 * A device's **ceiling**: what it is able to measure.
 *
 * Lives on the association, not on the reading. It serves before a measurement — deciding
 * whether offering a composition makes sense — and never says what a given reading obtained.
 * For that, see [MeasuredFidelity].
 *
 * Two independent axes: electrode count and frequency count vary separately.
 */
data class ScaleCapability(
    val electrodeCount: ElectrodeCount,
    val frequenciesKHz: List<Int>
) {
    init {
        require(frequenciesKHz.all { it > 0 }) { "Invalid frequency in $frequenciesKHz" }
        require(frequenciesKHz.distinct().size == frequenciesKHz.size) {
            "Duplicate frequencies in $frequenciesKHz"
        }
        require(electrodeCount == ElectrodeCount.NONE || frequenciesKHz.isNotEmpty()) {
            "A device with electrodes must declare at least one frequency"
        }
        require(electrodeCount != ElectrodeCount.NONE || frequenciesKHz.isEmpty()) {
            "A device without electrodes can read no frequency"
        }
    }

    /** Paths this device can read; with four electrodes only foot-to-foot exists. */
    val measurablePaths: Set<ImpedancePath> = when (electrodeCount) {
        ElectrodeCount.NONE -> emptySet()
        ElectrodeCount.FOUR -> ImpedancePath.entries.filterNot { it.involvesHands }.toSet()
        ElectrodeCount.EIGHT -> ImpedancePath.entries.toSet()
    }

    /** Every resistance this device could read: an upper bound, never an observation. */
    val measurableReadings: Set<ImpedanceReading> =
        measurablePaths.flatMap { path -> frequenciesKHz.map { ImpedanceReading(path, it) } }.toSet()

    /** True when the device can produce a body composition. */
    val supportsBodyComposition: Boolean get() = measurableReadings.isNotEmpty()

    companion object {
        /** A scale without electrodes reports mass only. */
        val WEIGHT_ONLY = ScaleCapability(ElectrodeCount.NONE, emptyList())

        /** Manual entry by the athlete: no sensor at all. */
        val MANUAL = WEIGHT_ONLY
    }
}
