package app.bodyforger.core.model

data class SegmentalImpedance(
    val trunkZ50: Double,
    val rightArmZ50: Double,
    val leftArmZ50: Double,
    val rightLegZ50: Double,
    val leftLegZ50: Double
)

data class BodyCompositionReport(
    val bodyFatPercentage: Double,
    val fatFreeMassKg: Double,
    val skeletalMuscleMassKg: Double,
    val totalBodyWaterLiters: Double,
    val extracellularWaterLiters: Double,
    val intracellularWaterLiters: Double,
    val ecwTbwRatio: Double
)

data class BodyLog(
    val id: String,
    val dateIso: String,
    val measuredAtEpochMs: Long,
    val massKg: Double,
    val report: BodyCompositionReport? = null,
    val restingHeartRateBpm: Int? = null
)
