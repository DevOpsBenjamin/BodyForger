package app.bodyforger.core.ble

import kotlinx.coroutines.flow.Flow

/**
 * Une balance repérée pendant un scan, avant toute connexion.
 *
 * [deviceAddress] vient du **scan natif**, jamais d'une saisie : c'est ce qui rend
 * l'appairage réalisable depuis la montre, et c'est aussi la graine de la clé racine.
 */
data class DiscoveredScale(
    val deviceAddress: String,
    val advertisedName: String,
    val recognised: RecognisedScale,
    /** Puissance reçue, en dBm — utile pour proposer la plus proche quand plusieurs répondent. */
    val signalStrengthDbm: Int
)

/**
 * La découverte des balances alentour.
 *
 * ⚠️ Le nom examiné est celui **annoncé dans l'advertisement**, pas le nom GAP de l'appareil.
 * Les deux diffèrent : la famille Haige annonce son modèle (`HUAWEI Scale 3 Pro-467`) tout en
 * portant un nom GAP générique (`HaigeBLE`). Se fier au second empêcherait de reconnaître le
 * modèle — c'est ce qui a été établi en #24.
 */
interface ScaleScanner {

    /**
     * Émet les balances reconnues par [driver] tant que le flux est collecté.
     *
     * Le même appareil peut être émis plusieurs fois : une balance s'annonce en boucle, et sa
     * puissance reçue varie. C'est à l'appelant de dédupliquer par adresse.
     */
    fun scan(driver: ScaleDriver): Flow<DiscoveredScale>
}
