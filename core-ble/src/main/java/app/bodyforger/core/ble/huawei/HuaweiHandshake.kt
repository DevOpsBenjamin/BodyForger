package app.bodyforger.core.ble.huawei

import app.bodyforger.core.ble.ScaleNotification
import app.bodyforger.core.ble.ScaleTransport
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import java.security.SecureRandom

/**
 * La négociation commune à toute session Haige, appairage comme pesée.
 *
 * Elle établit trois choses dans l'ordre : que la balance est bien celle qu'elle prétend,
 * que nous sommes autorisés à lui parler, et une clé de session pour la suite.
 *
 * L'authentification est **mutuelle et vérifiée dans les deux sens**. L'implémentation de
 * référence calcule le jeton attendu de la balance sans jamais le comparer à celui qu'elle
 * envoie : n'importe quel appareil annonçant le bon nom aurait alors été accepté, et aurait
 * reçu le profil de l'athlète. Ici, un jeton qui ne correspond pas interrompt la session.
 */
class HuaweiHandshake(
    private val transport: ScaleTransport,
    private val model: HuaweiScaleModel,
    private val random: SecureRandom = SecureRandom()
) {

    /**
     * Déroule la négociation et rend la clé de session, ou `null` si elle échoue.
     *
     * L'échec n'est pas diagnosticable finement : la balance refuse sans dire pourquoi. Une
     * clé racine fausse — matériel dont les constantes n'ont pas été relevées — ressemble
     * exactement à un appareil hors de portée.
     */
    suspend fun negotiate(macAddress: String): ByteArray? {
        val keys = model.keyMaterial
        val rootKey = HuaweiCrypto.deriveRootKey(keys, macAddress)

        // S'abonner d'abord : les réponses arrivent par notification, jamais en retour
        // d'écriture. Écrire avant de s'être abonné revient à parler dans le vide.
        for (characteristic in NEGOTIATION_CHARACTERISTICS) {
            if (!transport.subscribe(characteristic)) return null
        }

        // 1. La balance tire son aléa et nous l'envoie.
        if (!transport.write(HuaweiCharacteristic.AUTH_REQUEST, AUTH_REQUEST_PAYLOAD)) return null
        val scaleNonce = awaitPayload(HuaweiCharacteristic.AUTH_REQUEST)
            ?.takeIf { it.size >= HuaweiCrypto.NONCE_BYTES }
            ?.copyOf(HuaweiCrypto.NONCE_BYTES)
            ?: return null

        // 2. Nous tirons le nôtre et prouvons que nous connaissons le secret.
        val clientNonce = ByteArray(HuaweiCrypto.NONCE_BYTES).also(random::nextBytes)
        val clientToken = HuaweiCrypto.clientToken(keys, scaleNonce, clientNonce)
        if (!transport.write(HuaweiCharacteristic.AUTH_TOKENS, clientNonce + clientToken)) return null

        // 3. La balance prouve à son tour, et c'est là que la référence s'arrêtait.
        val scaleToken = awaitPayload(HuaweiCharacteristic.AUTH_TOKENS) ?: return null
        val expected = HuaweiCrypto.expectedScaleToken(keys, scaleNonce, clientNonce)
        if (!scaleToken.startsWithBytes(expected)) return null

        // 4. La clé de session voyage sous la clé racine — elle ne peut pas se protéger
        //    elle-même.
        val sessionKey = ByteArray(HuaweiCrypto.KEY_BYTES).also(random::nextBytes)
        val iv = ByteArray(HuaweiCrypto.IV_BYTES).also(random::nextBytes)
        val sealed = HuaweiCrypto.encrypt(rootKey, iv, sessionKey)
        if (!transport.write(HuaweiCharacteristic.SESSION_KEY, sealed)) return null
        if (awaitPayload(HuaweiCharacteristic.SESSION_KEY) == null) return null

        // 5. Annonce des capacités de l'hôte, en écriture sans réponse.
        transport.write(
            HuaweiCharacteristic.CAPABILITIES_REQUEST,
            HOST_CAPABILITIES,
            withResponse = false
        )

        return sessionKey
    }

    private suspend fun awaitPayload(characteristic: HuaweiCharacteristic): ByteArray? =
        withTimeoutOrNull(RESPONSE_TIMEOUT_MS) {
            transport.incoming.first { it.characteristic == characteristic }
        }?.let(ScaleNotification::payload)

    /** Comparaison à temps constant sur le préfixe attendu, pour ne rien laisser fuir. */
    private fun ByteArray.startsWithBytes(expected: ByteArray): Boolean {
        if (size < expected.size) return false
        var difference = 0
        for (i in expected.indices) difference = difference or (this[i].toInt() xor expected[i].toInt())
        return difference == 0
    }

    companion object {
        private const val RESPONSE_TIMEOUT_MS = 5_000L

        private val NEGOTIATION_CHARACTERISTICS = listOf(
            HuaweiCharacteristic.STATUS_SENTINEL,
            HuaweiCharacteristic.CAPABILITIES_RESPONSE,
            HuaweiCharacteristic.AUTH_REQUEST,
            HuaweiCharacteristic.AUTH_TOKENS,
            HuaweiCharacteristic.SESSION_KEY
        )

        /** Charge fixe de la demande d'authentification, relevée sur le protocole. */
        private val AUTH_REQUEST_PAYLOAD = byteArrayOf(
            0xDB.toByte(), 0x03, 0x00, 0xC1.toByte(), 0x40
        )

        /** Capacités annoncées par l'hôte, telles que la balance les attend. */
        private val HOST_CAPABILITIES = byteArrayOf(
            0x5A, 0x00, 0x05, 0x00, 0x01, 0x37, 0x01, 0x00, 0x1C, 0xA9.toByte()
        )
    }
}
