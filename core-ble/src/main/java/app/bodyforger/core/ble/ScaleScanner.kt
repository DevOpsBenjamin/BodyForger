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
    /**
     * Ce qu'un pilote a reconnu, ou `null` pour un appareil inconnu.
     *
     * Les inconnus sont montrés plutôt que masqués : voir le nom réellement annoncé est le
     * seul moyen de comprendre qu'une balance est bien là mais que notre filtre la rate. Un
     * scan qui ne montre que ce qu'il reconnaît ne peut jamais dire qu'il s'est trompé.
     */
    val recognised: RecognisedScale?,
    /** Puissance reçue, en dBm — utile pour proposer la plus proche quand plusieurs répondent. */
    val signalStrengthDbm: Int
) {
    val isCompatible: Boolean get() = recognised != null
}

/**
 * La découverte des balances alentour.
 *
 * ⚠️ Le nom examiné est celui **annoncé dans l'advertisement**, pas le nom GAP de l'appareil.
 * Les deux diffèrent : la famille Haige annonce son modèle (`HUAWEI Scale 3 Pro-467`) tout en
 * portant un nom GAP générique (`HaigeBLE`). Se fier au second empêcherait de reconnaître le
 * modèle — c'est ce qui a été établi en #24.
 */
/**
 * Le strict nécessaire pour trier un scan : reconnaître une balance à son nom annoncé.
 *
 * Contrat volontairement plus étroit que [ScaleDriver] : scanner n'exige pas de savoir
 * appairer ni peser, et le demander obligerait à écrire un pilote complet avant d'avoir
 * repéré le premier appareil.
 */
fun interface ScaleIdentifier {
    fun identify(advertisedName: String?): RecognisedScale?
}

interface ScaleScanner {

    /**
     * Émet les appareils qui s'annoncent tant que le flux est collecté, reconnus ou non.
     *
     * Le même appareil peut être émis plusieurs fois : une balance s'annonce en boucle, et sa
     * puissance reçue varie. C'est à l'appelant de dédupliquer par adresse.
     */
    fun scan(identifier: ScaleIdentifier): Flow<DiscoveredScale>
}
