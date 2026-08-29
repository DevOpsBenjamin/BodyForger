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
 * The negotiation every Haige session opens with, pairing or weigh-in.
 *
 * Mechanisms: `docs/BLE_PROTOCOL.md` §2.
 */
class HuaweiHandshake(
    private val transport: ScaleTransport,
    private val model: HuaweiScaleModel,
    private val random: SecureRandom = SecureRandom()
) {

    /** Negotiates and returns the session key, or `null` on failure. */
    suspend fun negotiate(macAddress: String): ByteArray? {
        val keys = model.keyMaterial
        val rootKey = HuaweiCrypto.deriveRootKey(keys, macAddress)

        // Event channels are useful but not required; the three below are.
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

        val clientNonce = ByteArray(HuaweiCrypto.NONCE_BYTES).also(random::nextBytes)
        val clientToken = HuaweiCrypto.clientToken(keys, scaleNonce, clientNonce)
        val scaleToken = exchange(HuaweiCharacteristic.AUTH_TOKENS) {
            transport.write(HuaweiCharacteristic.AUTH_TOKENS, clientNonce + clientToken)
        }
        if (scaleToken == null) {
            Log.w(TAG, "la balance n'a pas répondu son jeton")
            return null
        }
        val expected = HuaweiCrypto.expectedScaleToken(keys, scaleNonce, clientNonce)
        if (!scaleToken.startsWithBytes(expected)) {
            Log.w(TAG, "jeton de la balance invalide (${scaleToken.size} o)")
            return null
        }

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

        transport.writeRaw(
            HuaweiCharacteristic.CAPABILITIES_REQUEST,
            HuaweiCommands.HOST_CAPABILITIES,
            withResponse = false
        )

        return sessionKey
    }

    /**
     * Writes, then awaits the answer — starting to listen **first**.
     *
     * The notification flow has no buffer — `docs/BLE_PROTOCOL.md` §7.
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

    /** Constant-time prefix comparison. */
    private fun ByteArray.startsWithBytes(expected: ByteArray): Boolean {
        if (size < expected.size) return false
        var difference = 0
        for (i in expected.indices) difference = difference or (this[i].toInt() xor expected[i].toInt())
        return difference == 0
    }

    companion object {
        private const val TAG = "BodyForgerBle"
        private const val RESPONSE_TIMEOUT_MS = 5_000L

        /** Event channels: the scale pushes states there, nothing depends on them. */
        private val OPTIONAL_CHANNELS = listOf(
            HuaweiCharacteristic.STATUS_SENTINEL,
            HuaweiCharacteristic.CAPABILITIES_RESPONSE
        )

        /** Without these three, no authentication answer can reach us. */
        private val REQUIRED_CHANNELS = listOf(
            HuaweiCharacteristic.AUTH_REQUEST,
            HuaweiCharacteristic.AUTH_TOKENS,
            HuaweiCharacteristic.SESSION_KEY
        )

    }
}
