package app.bodyforger.core.ble.huawei

import app.bodyforger.core.ble.ScaleNotification
import android.util.Log
import app.bodyforger.core.ble.ScaleTransport
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
        // Les canaux d'événement sont utiles mais non indispensables : leur absence ne doit
        // pas empêcher une authentification qui, elle, ne dépend que des trois suivants.
        for (characteristic in OPTIONAL_CHANNELS) {
            if (!transport.subscribe(characteristic)) {
                Log.w(TAG, "canal facultatif indisponible, on poursuit : $characteristic")
            }
        }
        for (characteristic in REQUIRED_CHANNELS) {
            if (!transport.subscribe(characteristic)) {
                Log.w(TAG, "abonnement impossible : $characteristic")
                return null
            }
        }

        // 1. La balance tire son aléa et nous l'envoie.
        val answer = exchange(HuaweiCharacteristic.AUTH_REQUEST) {
            transport.writeRaw(HuaweiCharacteristic.AUTH_REQUEST, HuaweiCommands.QUERY)
        }
        val scaleNonce = answer
            ?.takeIf { it.size >= HuaweiCrypto.NONCE_BYTES }
            ?.copyOf(HuaweiCrypto.NONCE_BYTES)
        if (scaleNonce == null) {
            Log.w(TAG, "aléa de la balance absent ou trop court (${answer?.size ?: 0} o)")
            return null
        }

        // 2. Nous tirons le nôtre et prouvons que nous connaissons le secret.
        val clientNonce = ByteArray(HuaweiCrypto.NONCE_BYTES).also(random::nextBytes)
        val clientToken = HuaweiCrypto.clientToken(keys, scaleNonce, clientNonce)
        // 3. La balance prouve à son tour, et c'est là que la référence s'arrêtait.
        val scaleToken = exchange(HuaweiCharacteristic.AUTH_TOKENS) {
            transport.write(HuaweiCharacteristic.AUTH_TOKENS, clientNonce + clientToken)
        }
        if (scaleToken == null) {
            Log.w(TAG, "la balance n'a pas répondu son jeton")
            return null
        }
        val expected = HuaweiCrypto.expectedScaleToken(keys, scaleNonce, clientNonce)
        if (!scaleToken.startsWithBytes(expected)) {
            // Clés du modèle inadaptées, ou appareil qui n'est pas celui qu'il prétend.
            Log.w(TAG, "jeton de la balance invalide (${scaleToken.size} o)")
            return null
        }

        // 4. La clé de session voyage sous la clé racine — elle ne peut pas se protéger
        //    elle-même.
        val sessionKey = ByteArray(HuaweiCrypto.KEY_BYTES).also(random::nextBytes)
        val iv = ByteArray(HuaweiCrypto.IV_BYTES).also(random::nextBytes)
        val sealed = HuaweiCrypto.encrypt(rootKey, iv, sessionKey)
        if (exchange(HuaweiCharacteristic.SESSION_KEY) {
                transport.write(HuaweiCharacteristic.SESSION_KEY, sealed)
            } == null
        ) {
            Log.w(TAG, "clé de session non acquittée")
            return null
        }
        Log.d(TAG, "négociation réussie")

        // 5. Annonce des capacités de l'hôte, en écriture sans réponse.
        transport.writeRaw(
            HuaweiCharacteristic.CAPABILITIES_REQUEST,
            HuaweiCommands.HOST_CAPABILITIES,
            withResponse = false
        )

        return sessionKey
    }

    /**
     * Écrit, puis attend la réponse — **en se mettant à écouter d'abord**.
     *
     * ⚠️ Écrire puis écouter perd les réponses immédiates. Le flux des notifications n'a pas
     * de tampon : ce qui est émis avant qu'un collecteur ne s'abonne n'existe pour personne.
     * La balance acquitte parfois en une milliseconde, et l'attente expirait alors sur une
     * réponse déjà arrivée — un échec d'autant plus trompeur que les échanges plus lents,
     * eux, passaient.
     */
    private suspend fun exchange(
        characteristic: HuaweiCharacteristic,
        send: suspend () -> Boolean
    ): ByteArray? = coroutineScope {
        val response = async {
            transport.incoming.first { it.characteristic == characteristic }
        }
        if (!send()) {
            Log.w(TAG, "écriture refusée sur $characteristic")
            response.cancel()
            return@coroutineScope null
        }
        val received = withTimeoutOrNull(RESPONSE_TIMEOUT_MS) { response.await() }
        if (received == null) response.cancel()
        received?.payload
    }

    /** Comparaison à temps constant sur le préfixe attendu, pour ne rien laisser fuir. */
    private fun ByteArray.startsWithBytes(expected: ByteArray): Boolean {
        if (size < expected.size) return false
        var difference = 0
        for (i in expected.indices) difference = difference or (this[i].toInt() xor expected[i].toInt())
        return difference == 0
    }

    companion object {
        private const val TAG = "BodyForgerBle"
        private const val RESPONSE_TIMEOUT_MS = 5_000L

        /** Canaux d'événement : la balance y pousse des états, sans que rien n'en dépende. */
        private val OPTIONAL_CHANNELS = listOf(
            HuaweiCharacteristic.STATUS_SENTINEL,
            HuaweiCharacteristic.CAPABILITIES_RESPONSE
        )

        /** Sans ces trois-là, aucune réponse d'authentification ne peut nous parvenir. */
        private val REQUIRED_CHANNELS = listOf(
            HuaweiCharacteristic.AUTH_REQUEST,
            HuaweiCharacteristic.AUTH_TOKENS,
            HuaweiCharacteristic.SESSION_KEY
        )

    }
}
