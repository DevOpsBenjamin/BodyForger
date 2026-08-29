package app.bodyforger.core.ble

import app.bodyforger.core.model.ScaleAssociation
import kotlinx.coroutines.flow.Flow

/**
 * Le composant d'adaptation matérielle dédié à une famille de balances.
 *
 * Encapsule le chiffrement, les étapes d'appairage et le décodage des trames, et expose la
 * pesée comme une **suite d'états observables** — de sorte que l'interface reste générique et
 * ignorante du matériel.
 *
 * La signature ne rend pas un résultat atomique : une pesée dure plusieurs dizaines de
 * secondes et l'athlète doit en suivre la progression.
 */
interface ScaleDriver {

    /** Nom lisible de la famille prise en charge. */
    val name: String

    /** Vrai si ce pilote sait dialoguer avec ce modèle. */
    fun supports(model: ScaleModel): Boolean

    /**
     * Déroule une pesée sur la balance associée.
     *
     * Le flux émet la progression et se termine sur [WeighInState.Completed] ou
     * [WeighInState.Failed]. L'annulation du flux interrompt la pesée.
     */
    fun weighIn(association: ScaleAssociation): Flow<WeighInState>
}
