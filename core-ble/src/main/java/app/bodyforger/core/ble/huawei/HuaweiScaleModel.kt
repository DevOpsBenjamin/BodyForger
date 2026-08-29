package app.bodyforger.core.ble.huawei

import app.bodyforger.core.ble.RecognisedScale
import app.bodyforger.core.model.ElectrodeCount
import app.bodyforger.core.model.ImpedanceReading
import app.bodyforger.core.model.ScaleCapability

/**
 * A recognised model of the Haige family.
 *
 * Recognition, key material and GATT map: `docs/BLE_PROTOCOL.md`.
 */
enum class HuaweiScaleModel(
    private val nameFragments: List<String>,
    val displayName: String,
    val capability: ScaleCapability?,
    val impedanceOhmDivisor: Double = HAIGE_OHM_DIVISOR,
    /**
     * Le matériel cryptographique employé pour ce modèle.
     *
     * ⚠️ Il n'a été **relevé que sur la Scale 3 Pro**. Les autres modèles reçoivent le même
     * par défaut : c'est une **hypothèse volontairement testable**, pas un constat. Un
     * possesseur d'une autre balance peut ainsi essayer avec du code complet, et un
     * handshake refusé sur ce modèle **est** la réfutation — il faudra alors relever ses
     * propres constantes.
     */
    val keyMaterial: HuaweiKeyMaterial = HuaweiKeyMaterial.SCALE_3_PRO,
    /** GATT map used for this model — `docs/BLE_PROTOCOL.md` §4. */
    val gattProfile: HuaweiGattProfile = HuaweiGattProfile.SCALE_3_PRO
) {
    /** `M00F` — retractable handle, eight electrodes, dual frequency. */
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
        // Seul modèle sur lequel le matériel de clés a été effectivement relevé.
    ),

    /** `M00D` — four plate electrodes, low frequency only. */
    HUAWEI_SCALE_3(
        nameFragments = listOf("scale 3"),
        displayName = "HUAWEI Scale 3",
        capability = ScaleCapability(
            electrodeCount = ElectrodeCount.FOUR,
            frequenciesKHz = listOf(ImpedanceReading.LOW_FREQUENCY_KHZ)
        )
        // Hérite du matériel de la Pro faute d'un relevé propre : à confirmer ou infirmer
        // par un handshake réel sur une M00D.
    ),

    /**
     * A recognised Haige device whose axes are undocumented.
     *
     * Deliberately capped at nothing: an invented ceiling would be worth less than none, and
     * the capability reveals itself on the first reading.
     */
    HAIGE_FAMILY(
        nameFragments = listOf("haigeble", "hagrid", "huawei scale", "honor scale"),
        displayName = "Balance Haige",
        capability = null
    );

    /** Ce que le pilote expose à la couche générique, sans rien laisser filtrer du modèle. */
    fun toRecognisedScale(): RecognisedScale = RecognisedScale(displayName, capability)

    private fun matches(advertisedName: String): Boolean =
        nameFragments.any { advertisedName.contains(it, ignoreCase = true) }

    companion object {
        /**
         * Facteur d'échelle des résistances de la famille Haige : les compteurs bruts sont
         * des **dixièmes d'ohm**.
         *
         * `TECH.md` §6.2 avertit que ce n'est pas universel dans la gamme Huawei — d'où un
         * facteur porté par le modèle plutôt qu'en constante du décodeur. Nos deux captures
         *
         * openScale désambiguïse par magnitude (`1..3999` lus en ohms, `4000..39999` divisés
         * par dix) parce qu'il couvre cinquante-huit balances. Cette heuristique se trompe
         */
        const val HAIGE_OHM_DIVISOR: Double = 10.0

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
        fun identify(advertisedName: String?): HuaweiScaleModel? {
            if (advertisedName.isNullOrBlank()) return null
            return entries.firstOrNull { it.matches(advertisedName) }
        }

        /** Driver entry point: recognises a scale without exposing the enumeration. */
        fun recognise(advertisedName: String?): RecognisedScale? =
            identify(advertisedName)?.toRecognisedScale()
    }
}
