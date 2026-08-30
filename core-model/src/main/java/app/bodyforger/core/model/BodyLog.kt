package app.bodyforger.core.model

/**
 * Body composition derived from the raw impedances.
 *
 * Exists only where impedances were read. Not to be confused with the body fat percentage of
 * a [BodyLog], which is always present. Model: `docs/BIA_ENGINE.md`.
 */
data class BodyCompositionReport(
    /** Fat-free mass: everything that is not fat — muscle, bone, water, organs. */
    val fatFreeMassKg: Double,
    val fatMassKg: Double,
    val bodyFatPercentage: Double,

    /** Skeletal muscle: the share training moves. */
    val skeletalMuscleMassKg: Double,

    // Brozek 4C compartments — docs/BIA_ENGINE.md §4
    val totalBodyWaterKg: Double,
    val extracellularWaterKg: Double,
    val intracellularWaterKg: Double,
    val proteinMassKg: Double,
    val boneMineralMassKg: Double,

    /** Extracellular to total body water. Clinical norm ~0.38–0.40. */
    val ecwTbwRatio: Double,

    /** Muscle over the five segments, or `null` when the reading did not involve the hands. */
    val segmentalMuscle: SegmentalMuscleMass?,

    /** The five segments isolated at each frequency read. Derived, never persisted. */
    val segmental: List<SegmentalImpedances> = emptyList()
) {
    /**
     * Total skeletal muscle over height squared.
     *
     * Not the Baumgartner index: that grid applies to limb muscle only — see
     * [SegmentalMuscleMass.baumgartnerIndex].
     */
    fun totalMuscleIndex(heightCm: Double): Double =
        skeletalMuscleMassKg / ((heightCm / 100.0) * (heightCm / 100.0))
}

/**
 * A timestamped body reading.
 *
 * Identity is the instant, never the day: weighing in several times a day is legitimate and
 * overwrites nothing. See *Body Log* in `CONTEXT.md`.
 */
data class BodyLog(
    val id: String,
    val dateIso: String,
    val measuredAtEpochMs: Long,
    val massKg: Double,
    val bodyFatPercentage: Double,
    val rawImpedances: RawImpedances = RawImpedances.NONE,
    val restingHeartRateBpm: Int? = null
) {
    /** What this reading actually captured. */
    val fidelity: MeasuredFidelity get() = rawImpedances.fidelity

    /** True when a body composition can be computed from this reading. */
    val supportsBodyComposition: Boolean get() = !rawImpedances.isEmpty
}
