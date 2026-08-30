package app.bodyforger.core.ble.huawei

/**
 * Les commandes fixes du protocole, **déjà encadrées**.
 *
 * Ces suites d'octets sont des trames complètes — octet magique, longueur, séquence et CRC
 * compris — relevées telles quelles sur le protocole. Elles s'écrivent avec `writeRaw` et
 * jamais avec `write` : les encadrer une seconde fois produirait une trame contenant une
 * trame, que la balance rejette silencieusement.
 */
object HuaweiCommands {

    /**
     * La commande générique du protocole : elle demande, elle arme, elle désarme, selon la
     * caractéristique sur laquelle on l'écrit.
     *
     * Sur la caractéristique d'authentification elle réclame l'aléa de la balance ; sur celle
     * du flux BIA elle l'arme ; sur d'autres elle interroge le numéro de série ou le modèle.
     * Le protocole ne distingue pas ces intentions dans la trame, seulement dans sa
     * destination.
     */
    val QUERY = byteArrayOf(0xDB.toByte(), 0x03, 0x00, 0xC1.toByte(), 0x40)

    /** Les capacités annoncées par l'hôte, en écriture sans réponse. */
    val HOST_CAPABILITIES = byteArrayOf(
        0x5A, 0x00, 0x05, 0x00, 0x01, 0x37, 0x01, 0x00, 0x1C, 0xA9.toByte()
    )
}
