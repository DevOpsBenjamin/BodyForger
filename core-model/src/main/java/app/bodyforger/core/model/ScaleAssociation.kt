package app.bodyforger.core.model

/**
 * Le lien durable entre l'athlète et une balance donnée.
 *
 * Créée **une seule fois**, par la montre ou par le téléphone, puis partagée entre les deux.
 * Tant qu'elle existe, chaque pesée l'utilise directement : l'appairage n'est jamais rejoué.
 *
 * Porte le **plafond** de capacité du matériel — la couche qui sert avant la mesure. Il vaut
 * `null` tant que le modèle n'a pas été reconnu ; il se révèle alors à la première pesée.
 */
data class ScaleAssociation(
    /** Adresse Bluetooth physique, obtenue du scan natif — jamais saisie à la main. */
    val deviceAddress: String,

    /** Identifiant d'utilisateur gravé dans la mémoire flash de la balance. Un seul par athlète. */
    val huid: String,

    /** Masse de tare relevée pendant l'appairage, en kilogrammes. */
    val tareKg: Double,

    /** Nom annoncé dans l'advertisement BLE, tel que reçu — c'est lui qui porte le modèle. */
    val advertisedName: String,

    /** Plafond du matériel, ou `null` si le modèle n'est pas documenté. */
    val capability: ScaleCapability? = null
)
