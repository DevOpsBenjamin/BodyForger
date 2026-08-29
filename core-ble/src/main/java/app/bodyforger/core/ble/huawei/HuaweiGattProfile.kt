package app.bodyforger.core.ble.huawei

import java.util.UUID

/**
 * Une caractéristique GATT du protocole, et le chiffrement qu'elle exige.
 *
 * Trois régimes cohabitent sur la même balance : certaines caractéristiques sont en clair,
 * une seule est protégée par la clé racine — celle qui convoie la clé de session, et pour
 * cause — et les autres par la clé de session elle-même.
 */
enum class HuaweiCharacteristic(val protection: Protection) {
    /** Demande d'authentification : la balance renvoie son aléa. */
    AUTH_REQUEST(Protection.CLEAR),

    /** Échange des jetons : aléa client et jeton client contre jeton balance. */
    AUTH_TOKENS(Protection.CLEAR),

    /** Transport de la clé de session, protégé par la clé racine. */
    SESSION_KEY(Protection.ROOT_KEY),

    /** Armement et désarmement du mode association (`0x01` / `0x00`). */
    BINDING_CONTROL(Protection.CLEAR),

    /** Gravure du HUID en mémoire flash, et retour de la tare. */
    HUID_REGISTRATION(Protection.SESSION_KEY),

    /** Profil utilisateur et validation de mesure. */
    USER_PROFILE(Protection.SESSION_KEY),

    /** Synchronisation d'horloge — caractéristique **standard** Bluetooth SIG. */
    TIME_SYNC(Protection.CLEAR),

    /** Numéro de série. */
    SERIAL_NUMBER(Protection.CLEAR),

    /** État de configuration de l'appareil. */
    DEVICE_CONFIGURATION(Protection.CLEAR),

    /** Modèle matériel — c'est ici que se lit `M00F` ou `M00D`. */
    HARDWARE_MODEL(Protection.CLEAR),

    /** Historique des pesées conservées hors ligne par la balance. */
    OFFLINE_HISTORY(Protection.CLEAR),

    /** Flux de télémétrie BIA en temps réel. */
    BIA_STREAM(Protection.SESSION_KEY),

    /** Annonce des capacités de l'hôte — écriture sans réponse. */
    CAPABILITIES_REQUEST(Protection.CLEAR),

    /** Réponse de capacités de la balance. */
    CAPABILITIES_RESPONSE(Protection.CLEAR),

    /** Sentinelle d'état : la balance y pousse ses événements. */
    STATUS_SENTINEL(Protection.CLEAR);

    enum class Protection { CLEAR, ROOT_KEY, SESSION_KEY }
}

/**
 * La carte des caractéristiques GATT d'un modèle.
 *
 * ⚠️ **Relevée sur la Scale 3 Pro seule.** Douze de ces quinze UUID sont des identifiants
 * 128 bits **propriétaires** — tirés au hasard par le fabricant, ils n'ont de sens que pour
 * ce firmware. Rien ne garantit qu'un autre modèle les partage, même s'il est probable que
 * toute la famille Haige les ait en commun : c'est le même fabricant derrière les balances
 * Huawei et HONOR.
 *
 * Trois exceptions, reconnaissables au motif `0000XXXX-0000-1000-8000-00805f9b34fb` :
 * la synchronisation d'horloge est la caractéristique **standard** Bluetooth SIG *Current
 * Time*, et les deux caractéristiques de capacités occupent la plage 16 bits allouée à un
 * membre du SIG. Celles-là ne dépendent pas du modèle.
 *
 * Comme pour [HuaweiKeyMaterial], les autres modèles reçoivent ce profil par défaut : une
 * hypothèse testable vaut mieux qu'un blocage. Une caractéristique absente à la découverte
 * des services est alors la réfutation — et elle, au moins, se diagnostique.
 */
data class HuaweiGattProfile(val characteristics: Map<HuaweiCharacteristic, UUID>) {

    operator fun get(characteristic: HuaweiCharacteristic): UUID? = characteristics[characteristic]

    /** Retrouve la caractéristique correspondant à un UUID reçu, ou `null` si inconnue. */
    fun characteristicOf(uuid: UUID): HuaweiCharacteristic? =
        characteristics.entries.firstOrNull { it.value == uuid }?.key

    companion object {
        /** Profil relevé sur la Scale 3 Pro (`M00F`), et vérifié sur elle seule. */
        val SCALE_3_PRO = HuaweiGattProfile(
            mapOf(
                HuaweiCharacteristic.AUTH_REQUEST to uuid("02b2a08e-f8b0-4047-b1fd-f4e0efeee679"),
                HuaweiCharacteristic.AUTH_TOKENS to uuid("32330a04-15d9-421a-91c5-2a2d5c7525c9"),
                HuaweiCharacteristic.SESSION_KEY to uuid("a3d330f8-b84f-4f48-a78c-f8d1e33b597a"),
                HuaweiCharacteristic.BINDING_CONTROL to uuid("4338c65e-ed8e-4085-bbea-a25e33ca6b54"),
                HuaweiCharacteristic.HUID_REGISTRATION to uuid("42596cbe-d291-4da3-8ca6-d1ae5d1c9174"),
                HuaweiCharacteristic.USER_PROFILE to uuid("8cc61d7d-66c0-4802-89c3-38c5a163592e"),
                HuaweiCharacteristic.TIME_SYNC to uuid("00002a2b-0000-1000-8000-00805f9b34fb"),
                HuaweiCharacteristic.SERIAL_NUMBER to uuid("11872f15-a91d-49da-ac89-5107284f3425"),
                HuaweiCharacteristic.DEVICE_CONFIGURATION to uuid("bfc36f6e-4150-4a4b-9052-3d359e52962e"),
                HuaweiCharacteristic.HARDWARE_MODEL to uuid("426f058d-8211-413e-8320-397a890a08bf"),
                HuaweiCharacteristic.OFFLINE_HISTORY to uuid("0212f42a-5f19-4bc1-ba52-d7ec7ccb71a4"),
                HuaweiCharacteristic.BIA_STREAM to uuid("46797c17-d639-488d-9476-4789e8472878"),
                HuaweiCharacteristic.CAPABILITIES_REQUEST to uuid("0000fe01-0000-1000-8000-00805f9b34fb"),
                HuaweiCharacteristic.CAPABILITIES_RESPONSE to uuid("0000fe02-0000-1000-8000-00805f9b34fb"),
                HuaweiCharacteristic.STATUS_SENTINEL to uuid("ba216311-1787-472b-bef6-3eb29e62293e")
            )
        )

        /** Descripteur standard d'activation des notifications, commun à tout le BLE. */
        val CLIENT_CONFIG_DESCRIPTOR: UUID = uuid("00002902-0000-1000-8000-00805f9b34fb")

        private fun uuid(value: String): UUID = UUID.fromString(value)
    }
}
