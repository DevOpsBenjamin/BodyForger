package app.bodyforger.core.ble

import app.bodyforger.core.model.ScaleAssociation
import app.bodyforger.core.model.ScaleUserProfile
import kotlinx.coroutines.flow.Flow

/**
 * Le composant d'adaptation matérielle dédié à une famille de balances.
 *
 * Le pilote possède **son propre contrat** : sa séquence d'appairage, ses étapes, ses
 * trames, sa cryptographie. Le cœur ne connaît que le vocabulaire commun — [SessionPhase],
 * [AthleteInstruction], [SessionFailure] — et ne présume ni du nombre d'étapes, ni de leur
 * ordre, ni de la nécessité d'une connexion.
 *
 * Rien de propre à un constructeur ne traverse cette interface : ni énumération de modèles,
 * ni décalage de trame, ni identifiant GATT.
 */
interface ScaleDriver : ScaleIdentifier {

    /** Identifiant stable du pilote, par exemple `huawei_haige`. */
    val id: String

    /** Nom lisible de la famille prise en charge. */
    val name: String

    /** Ce que ce matériel exige avant de pouvoir être utilisé au quotidien. */
    val pairingRequirement: PairingRequirement

    /**
     * Examine le nom annoncé dans l'advertisement BLE.
     *
     * @return la balance reconnue, ou `null` si elle n'appartient pas à cette famille.
     */
    override fun identify(advertisedName: String?): RecognisedScale?

    /**
     * Déroule l'appairage initial et produit l'Association.
     *
     * Le flux se termine sur [PairingState.Completed] ou [PairingState.Failed]. Un pilote
     * dont le [pairingRequirement] vaut [PairingRequirement.NONE] peut aboutir sans émettre
     * la moindre étape.
     */
    fun pair(
        deviceAddress: String,
        advertisedName: String,
        profile: ScaleUserProfile
    ): Flow<PairingState>

    /**
     * Déroule une pesée sur la balance associée.
     *
     * Le flux se termine sur [WeighInState.Completed] ou [WeighInState.Failed].
     * L'annulation du flux interrompt la pesée.
     */
    fun weighIn(
        association: ScaleAssociation,
        profile: ScaleUserProfile
    ): Flow<WeighInState>
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
    fun driverFor(association: ScaleAssociation): ScaleDriver? =
        identify(association.advertisedName)?.driver

    data class Match(val driver: ScaleDriver, val scale: RecognisedScale)
}
