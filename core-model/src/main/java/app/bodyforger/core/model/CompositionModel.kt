package app.bodyforger.core.model

/**
 * A body-composition engine: a black box that turns a weigh-in into a full report.
 *
 * The contract only — no coefficient, no logic. It lives in `core-model` so that any
 * engine, public or private, can implement it while sharing nothing but the shape of the
 * plug. Each implementation owns ALL of its own coefficients and duplicates whatever
 * infrastructure it needs; no implementation code is shared between engines.
 *
 * Implementations:
 *   * ForgeFit MIT     (`core-bia`, in this repository) — built only from published equations.
 *   * ForgeFit Private (a separate module, not in this repository and not MIT-licensed).
 */
interface CompositionModel {

    /**
     * The richest analysis the reading allows, or `null` when it carries no usable
     * impedance. A poorer reading yields fewer quantities, never invented ones.
     */
    fun analyze(
        massKg: Double,
        profile: BiaProfile,
        impedances: RawImpedances
    ): BodyCompositionReport?
}
