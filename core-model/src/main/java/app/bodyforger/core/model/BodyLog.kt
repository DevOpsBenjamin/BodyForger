package app.bodyforger.core.model

/**
 * Impédances par zone anatomique.
 *
 * ⚠️ Ce type fusionne le mesuré et le dérivé : les zones segmentaires sont des fonctions
 * pures de Kirchhoff des [RawImpedances], et ne doivent jamais être persistées. Conservé
 * uniquement le temps que `DexaBiaCalculator` soit réécrit — voir #20.
 */
@Deprecated("Couche dérivée, jamais persistée. Remplacé par RawImpedances — voir #20.")
data class SegmentalImpedance(
    val trunkZ50: Double,
    val rightArmZ50: Double,
    val leftArmZ50: Double,
    val rightLegZ50: Double,
    val leftLegZ50: Double
)

/**
 * L'analyse de composition corporelle dérivée des [RawImpedances].
 *
 * N'existe que si des impédances ont été relevées. À ne pas confondre avec le taux de masse
 * grasse du [BodyLog], qui est toujours renseigné.
 */
data class BodyCompositionReport(
    val bodyFatPercentage: Double,
    val fatFreeMassKg: Double,
    val skeletalMuscleMassKg: Double,
    val totalBodyWaterLiters: Double,
    val extracellularWaterLiters: Double,
    val intracellularWaterLiters: Double,
    val ecwTbwRatio: Double
)

/**
 * Un relevé corporel journalier rattaché à l'athlète.
 *
 * Porte toujours une masse, une date et un taux de masse grasse — ce dernier provenant soit
 * de la balance, soit d'une saisie manuelle. C'est ce qui rend l'application utilisable sans
 * balance connectée.
 *
 * Les [rawImpedances] ne sont présentes que si le matériel les a relevées, et leur
 * [MeasuredFidelity] dit exactement ce que cette pesée a obtenu. Une grandeur non mesurée
 * est **absente**, jamais remplacée par un défaut.
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
    /** Ce que cette pesée a réellement relevé. */
    val fidelity: MeasuredFidelity get() = rawImpedances.fidelity

    /** Vrai si une analyse de composition corporelle est calculable à partir de ce relevé. */
    val supportsBodyComposition: Boolean get() = !rawImpedances.isEmpty
}
