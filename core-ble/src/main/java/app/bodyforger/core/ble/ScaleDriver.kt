package app.bodyforger.core.ble

import app.bodyforger.core.model.ScaleAssociation
import kotlinx.coroutines.flow.Flow

/**
 * Le composant d'adaptation matérielle dédié à une famille de balances.
 *
 * Encapsule l'identification, le chiffrement, les étapes d'appairage et le décodage des
 * trames, et expose la pesée comme une **suite d'états observables** — de sorte que
 * l'application reste générique et ignorante du matériel.
 *
 * Rien de propre à un constructeur ne doit traverser cette interface : ni énumération de
 * modèles, ni décalage de trame, ni identifiant GATT.
 */
interface ScaleDriver {

    /** Nom lisible de la famille prise en charge. */
    val name: String

    /**
     * Examine le nom annoncé dans l'advertisement BLE.
     *
     * @return la balance reconnue, ou `null` si elle n'appartient pas à cette famille.
     */
    fun identify(advertisedName: String?): RecognisedScale?

    /**
     * Déroule une pesée sur la balance associée.
     *
     * Le flux émet la progression et se termine sur [WeighInState.Completed] ou
     * [WeighInState.Failed]. L'annulation du flux interrompt la pesée.
     */
    fun weighIn(association: ScaleAssociation): Flow<WeighInState>
}

/**
 * Le registre des pilotes : choisit celui qui sait dialoguer avec une balance donnée.
 *
 * L'ordre de la liste fait foi en cas de recouvrement — un pilote spécifique doit précéder
 * un pilote générique, tel que le profil Bluetooth SIG standard.
 */
class ScaleDriverRegistry(private val drivers: List<ScaleDriver>) {

    /** Le premier pilote qui reconnaît ce nom annoncé, avec ce qu'il en a déduit. */
    fun identify(advertisedName: String?): Match? =
        drivers.firstNotNullOfOrNull { driver ->
            driver.identify(advertisedName)?.let { Match(driver, it) }
        }

    /** Le pilote capable de piloter cette Association. */
    fun driverFor(advertisedName: String): ScaleDriver? = identify(advertisedName)?.driver

    data class Match(val driver: ScaleDriver, val scale: RecognisedScale)
}
