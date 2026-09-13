package app.bodyforger.core.bia

import app.bodyforger.core.model.CompositionModel

/**
 * The body-composition engines available at runtime, and the athlete's chosen one.
 *
 * ForgeFit MIT ([ForgeFitModel]) is always present. ForgeFit Private is added only when its
 * module is compiled into the build, resolved by reflection so that `core-bia` (public) never
 * references that module at compile time — it builds and runs identically whether or not the
 * module is present. When the module IS shipped, keep its object under R8:
 *   `-keep class app.bodyforger.forgefitprivate.ForgeFitPrivateModel { *; }`
 *
 * Which engine is active is a persisted user preference (see the settings screen). When only
 * one engine is compiled in, the choice is moot and the setting hides itself.
 */
object ModelSelector {

    /** Stable ids — persisted in the database, so they must never change. */
    const val FORGEFIT_MIT = "forgefit_mit"
    const val FORGEFIT_PRIVATE = "forgefit_private"

    /** The engine used when the athlete has expressed no preference. */
    const val DEFAULT_ID = FORGEFIT_MIT

    data class Engine(val id: String, val model: CompositionModel)

    /** The engines actually compiled into this build, in display order. */
    val available: List<Engine> by lazy {
        buildList {
            add(Engine(FORGEFIT_MIT, ForgeFitModel))
            resolveSecondary()?.let { add(Engine(FORGEFIT_PRIVATE, it)) }
        }
    }

    /** The chosen engine, falling back to the default for a null or unknown id. */
    fun modelFor(id: String?): CompositionModel =
        available.firstOrNull { it.id == id }?.model ?: ForgeFitModel

    private fun resolveSecondary(): CompositionModel? = runCatching {
        Class.forName("app.bodyforger.forgefitprivate.ForgeFitPrivateModel")
            .getField("INSTANCE").get(null) as CompositionModel
    }.getOrNull()
}
