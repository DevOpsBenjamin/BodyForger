package app.bodyforger.core.ble.huawei

import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Root key derivation, mutual authentication and session encryption.
 *
 * Mechanisms and their rationale: `docs/BLE_PROTOCOL.md` §2. Key material belongs to the
 * model — see [HuaweiKeyMaterial].
 */
object HuaweiCrypto {

    /** Salts telling the client token apart from the scale's. */
    private val CLIENT_SALT = "1123".toByteArray(Charsets.UTF_8)
    private val SCALE_SALT = "9856".toByteArray(Charsets.UTF_8)

    const val KEY_BYTES = 16
    const val IV_BYTES = 16
    const val NONCE_BYTES = 16

    /** Derives the root key from the physical MAC address, separators optional. */
    fun deriveRootKey(keys: HuaweiKeyMaterial, macAddress: String): ByteArray {
        val normalised = (macAddress.replace(":", "").replace("-", "").uppercase() + "0000")
            .toByteArray(Charsets.UTF_8)
        require(normalised.size >= KEY_BYTES) { "MAC address too short: $macAddress" }

        val whitebox = ByteArray(KEY_BYTES) {
            ((keys.whiteboxFirst[it].toInt() shl 4) xor keys.whiteboxSecond[it].toInt()).toByte()
        }
        val condensed = sha256(whitebox).copyOf(KEY_BYTES)
        val mixed = ByteArray(KEY_BYTES) {
            ((condensed[it].toInt() and 0xFF shr 6) xor (normalised[it].toInt() and 0xFF)).toByte()
        }
        return sha256(mixed).copyOf(KEY_BYTES)
    }

    /** The token proving to the scale that we hold the manufacturer secret. */
    fun clientToken(keys: HuaweiKeyMaterial, scaleNonce: ByteArray, clientNonce: ByteArray): ByteArray =
        token(keys, scaleNonce, clientNonce, CLIENT_SALT)

    /** The token the scale must return; comparing it authenticates the hardware. */
    fun expectedScaleToken(keys: HuaweiKeyMaterial, scaleNonce: ByteArray, clientNonce: ByteArray): ByteArray =
        token(keys, scaleNonce, clientNonce, SCALE_SALT)

    private fun token(
        keys: HuaweiKeyMaterial,
        scaleNonce: ByteArray,
        clientNonce: ByteArray,
        salt: ByteArray
    ): ByteArray {
        require(scaleNonce.size == NONCE_BYTES) { "Scale nonce of ${scaleNonce.size} bytes" }
        require(clientNonce.size == NONCE_BYTES) { "Client nonce of ${clientNonce.size} bytes" }
        val combined = scaleNonce + clientNonce
        return hmacSha256(hmacSha256(keys.authenticationSecret + salt, combined), combined)
    }

    /** Seals a payload as `[ 16-byte IV ] + [ ciphertext ]`. The IV must never repeat. */
    fun encrypt(sessionKey: ByteArray, iv: ByteArray, plaintext: ByteArray): ByteArray =
        iv + counterMode(Cipher.ENCRYPT_MODE, sessionKey, iv, plaintext)

    /** Opens a received payload, or `null` when it is too short to carry its own IV. */
    fun decrypt(sessionKey: ByteArray, payload: ByteArray): ByteArray? {
        if (payload.size <= IV_BYTES) return null
        return counterMode(
            Cipher.DECRYPT_MODE,
            sessionKey,
            payload.copyOfRange(0, IV_BYTES),
            payload.copyOfRange(IV_BYTES, payload.size)
        )
    }

    private fun counterMode(mode: Int, key: ByteArray, iv: ByteArray, data: ByteArray): ByteArray {
        require(key.size == KEY_BYTES) { "Key of ${key.size} bytes, $KEY_BYTES expected" }
        require(iv.size == IV_BYTES) { "IV of ${iv.size} bytes, $IV_BYTES expected" }
        return Cipher.getInstance("AES/CTR/NoPadding").apply {
            init(mode, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
        }.doFinal(data)
    }

    private fun sha256(data: ByteArray): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(data)

    private fun hmacSha256(key: ByteArray, data: ByteArray): ByteArray =
        Mac.getInstance("HmacSHA256").apply { init(SecretKeySpec(key, "HmacSHA256")) }.doFinal(data)

}
