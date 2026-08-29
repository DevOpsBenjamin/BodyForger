package app.bodyforger.core.ble

import app.bodyforger.core.ble.huawei.HuaweiCharacteristic
import kotlinx.coroutines.flow.Flow

/**
 * Ce qu'une couche de transport doit savoir faire pour qu'un pilote puisse dialoguer avec
 * une balance : se connecter, s'abonner, écrire, et livrer ce qui arrive.
 *
 * Ce contrat existe pour que l'orchestration du protocole — la partie qui compte, et qui se
 * trompe — reste vérifiable sans matériel. Le vrai transport Bluetooth n'a rien à décider :
 * il transporte.
 */
interface ScaleTransport {

    /** Les charges reçues, déjà recollées et rattachées à leur caractéristique. */
    val incoming: Flow<ScaleNotification>

    /** Établit la connexion et découvre les services. `false` si l'un des deux échoue. */
    suspend fun connect(): Boolean

    /**
     * Active les notifications d'une caractéristique.
     *
     * Sans cet abonnement, la balance écrit dans le vide : la plupart de ses réponses
     * arrivent par notification et non en retour d'écriture.
     */
    suspend fun subscribe(characteristic: HuaweiCharacteristic): Boolean

    /** Coupe les notifications d'une caractéristique. */
    suspend fun unsubscribe(characteristic: HuaweiCharacteristic): Boolean

    /**
     * Écrit une charge, en la découpant en trames si nécessaire.
     *
     * @param withResponse `false` pour les caractéristiques en écriture sans réponse.
     */
    suspend fun write(
        characteristic: HuaweiCharacteristic,
        payload: ByteArray,
        withResponse: Boolean = true
    ): Boolean

    /** Ferme la connexion et libère les ressources. Idempotent. */
    fun close()
}

/** Une charge complète reçue d'une caractéristique, telle que la balance l'a émise. */
data class ScaleNotification(
    val characteristic: HuaweiCharacteristic,
    val payload: ByteArray,
    /** Vrai si la charge est chiffrée par la clé de session et reste à déchiffrer. */
    val encrypted: Boolean
) {
    override fun equals(other: Any?): Boolean = this === other || (other is ScaleNotification &&
        characteristic == other.characteristic && encrypted == other.encrypted &&
        payload.contentEquals(other.payload))

    override fun hashCode(): Int = 31 * characteristic.hashCode() + payload.contentHashCode()
}
