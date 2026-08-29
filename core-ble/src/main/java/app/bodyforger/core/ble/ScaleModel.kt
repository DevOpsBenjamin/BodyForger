package app.bodyforger.core.ble

import app.bodyforger.core.model.ElectrodeCount
import app.bodyforger.core.model.ImpedanceReading
import app.bodyforger.core.model.ScaleCapability

/**
 * Un modèle de balance reconnu, et le plafond de capacité qu'il déclare.
 *
 * Le modèle se lit dans le **nom annoncé** de l'advertisement BLE, avant toute connexion —
 * pas dans le nom GAP. Les deux diffèrent sur cette famille : le nom GAP vaut `HaigeBLE`
 * pour toute la gamme, tandis que l'advertisement porte `HUAWEI Scale 3 Pro-467`.
 *
 * Seuls les modèles dont les deux axes sont **documentés** portent une [capability]. Les
 * autres membres de la famille sont reconnus — même protocole, même handshake, même trame —
 * mais leur capacité est déduite de ce que la trame livre réellement, jamais supposée.
 */
enum class ScaleModel(
    private val nameFragments: List<String>,
    val displayName: String,
    val capability: ScaleCapability?
) {
    /**
     * `M00F` / `HAGRID-B29` — poignée rétractable, huit électrodes, bande haute fréquence.
     * Trame de 38 octets. Documenté dans `TECH.md`.
     */
    HUAWEI_SCALE_3_PRO(
        nameFragments = listOf("scale 3 pro"),
        displayName = "HUAWEI Scale 3 Pro",
        capability = ScaleCapability(
            electrodeCount = ElectrodeCount.EIGHT,
            frequenciesKHz = listOf(
                ImpedanceReading.LOW_FREQUENCY_KHZ,
                ImpedanceReading.HIGH_FREQUENCY_KHZ
            )
        )
    ),

    /**
     * `M00D` / `HEM-B19` — quatre électrodes au plateau, basse fréquence seule.
     * Trame de 26 octets. Documenté dans `TECH.md`.
     */
    HUAWEI_SCALE_3(
        nameFragments = listOf("scale 3"),
        displayName = "HUAWEI Scale 3",
        capability = ScaleCapability(
            electrodeCount = ElectrodeCount.FOUR,
            frequenciesKHz = listOf(ImpedanceReading.LOW_FREQUENCY_KHZ)
        )
    ),

    /**
     * Membre reconnu de la famille Haige dont les axes ne sont pas documentés — Scale 2 Pro,
     * HONOR Scale 2, ou un modèle non répertorié.
     *
     * Volontairement sans plafond : le nombre d'électrodes de ces modèles n'est établi par
     * aucune source en notre possession. La capacité se révèle à la première pesée.
     */
    HAIGE_FAMILY(
        nameFragments = listOf("haigeble", "hagrid", "huawei scale", "honor scale"),
        displayName = "Balance Haige",
        capability = null
    );

    private fun matches(advertisedName: String): Boolean =
        nameFragments.any { advertisedName.contains(it, ignoreCase = true) }

    companion object {
        /**
         * Identifie un modèle depuis le nom annoncé dans l'advertisement BLE.
         *
         * L'ordre de déclaration fait foi : `Scale 3 Pro` est testé avant `Scale 3`, dont il
         * contient le libellé. La correspondance est faite par **sous-chaîne** — le suffixe
         * d'un nom annoncé est propre à l'exemplaire (`-467`) et l'athlète peut renommer sa
         * balance.
         *
         * @return le modèle reconnu, ou `null` si l'appareil n'appartient pas à la famille.
         */
        fun identify(advertisedName: String?): ScaleModel? {
            if (advertisedName.isNullOrBlank()) return null
            return entries.firstOrNull { it.matches(advertisedName) }
        }
    }
}
