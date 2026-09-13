package app.bodyforger.core.ble.huawei

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 *
 */
class HuaweiCryptoTest {

    private val keys = HuaweiKeyMaterial.SCALE_3_PRO

    private val fictitiousMac = "AA:BB:CC:DD:EE:FF"
    private val otherMac = "11:22:33:44:55:66"

    private val scaleNonce = ByteArray(16) { it.toByte() }
    private val clientNonce = ByteArray(16) { (it + 16).toByte() }

    @Test
    fun `the root key matches the reference`() {
        assertEquals(
            "d0d01fae597b65147ee89b03e26dd8ec",
            HuaweiCrypto.deriveRootKey(keys, fictitiousMac).toHex()
        )
        assertEquals(
            "41c30ee71f3e88defdaf4bf5a216e4dc",
            HuaweiCrypto.deriveRootKey(keys, otherMac).toHex()
        )
    }

    @Test
    fun `two scales never share the same root key`() {
        assertNotEquals(
            HuaweiCrypto.deriveRootKey(keys, fictitiousMac).toHex(),
            HuaweiCrypto.deriveRootKey(keys, otherMac).toHex()
        )
    }

    @Test
    fun `derivation ignores case and separators`() {
        val reference = HuaweiCrypto.deriveRootKey(keys, fictitiousMac).toHex()
        assertEquals(reference, HuaweiCrypto.deriveRootKey(keys, "aa:bb:cc:dd:ee:ff").toHex())
        assertEquals(reference, HuaweiCrypto.deriveRootKey(keys, "AABBCCDDEEFF").toHex())
        assertEquals(reference, HuaweiCrypto.deriveRootKey(keys, "AA-BB-CC-DD-EE-FF").toHex())
    }

    @Test
    fun `the same scale always yields the same key`() {
        assertArrayEquals(
            HuaweiCrypto.deriveRootKey(keys, fictitiousMac),
            HuaweiCrypto.deriveRootKey(keys, fictitiousMac)
        )
    }

    // --- Authentification mutuelle ---

    @Test
    fun `both tokens match the reference`() {
        assertEquals(
            "e2c2c283c9545d4d67bce6e302ae0c0f274571455d92148746d037fe100def58",
            HuaweiCrypto.clientToken(keys, scaleNonce, clientNonce).toHex()
        )
        assertEquals(
            "7d1c75ca91ef42b547ea1738b98b2ec2c853d53873fe617666e9a06839b7d052",
            HuaweiCrypto.expectedScaleToken(keys, scaleNonce, clientNonce).toHex()
        )
    }

    @Test
    fun `the scale's token differs from the client's`() {
        assertNotEquals(
            HuaweiCrypto.clientToken(keys, scaleNonce, clientNonce).toHex(),
            HuaweiCrypto.expectedScaleToken(keys, scaleNonce, clientNonce).toHex()
        )
    }

    @Test
    fun `flipping a single bit of a nonce changes the whole token`() {
        val altered = clientNonce.copyOf().also { it[0] = (it[0].toInt() xor 1).toByte() }
        assertNotEquals(
            HuaweiCrypto.clientToken(keys, scaleNonce, clientNonce).toHex(),
            HuaweiCrypto.clientToken(keys, scaleNonce, altered).toHex()
        )
    }

    @Test
    fun `the order of the nonces is not interchangeable`() {
        assertNotEquals(
            HuaweiCrypto.clientToken(keys, scaleNonce, clientNonce).toHex(),
            HuaweiCrypto.clientToken(keys, clientNonce, scaleNonce).toHex()
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `a nonce of the wrong size is refused`() {
        HuaweiCrypto.clientToken(keys, ByteArray(8), clientNonce)
    }

    // --- Chiffrement de session ---

    @Test
    fun `what is encrypted decrypts`() {
        val key = ByteArray(16) { (it * 7).toByte() }
        val iv = ByteArray(16) { (it * 3).toByte() }
        val clear = "profil utilisateur".toByteArray()

        val sealed = HuaweiCrypto.encrypt(key, iv, clear)
        assertArrayEquals(clear, HuaweiCrypto.decrypt(key, sealed))
    }

    @Test
    fun `the IV travels in the clear ahead of the encrypted body`() {
        val key = ByteArray(16)
        val iv = ByteArray(16) { (it + 1).toByte() }
        val clear = ByteArray(30) { 0x41 }

        val sealed = HuaweiCrypto.encrypt(key, iv, clear)
        assertEquals(HuaweiCrypto.IV_BYTES + clear.size, sealed.size)
        assertArrayEquals(iv, sealed.copyOfRange(0, HuaweiCrypto.IV_BYTES))
        // The body itself must show nothing of the plaintext.
        assertFalse(sealed.copyOfRange(HuaweiCrypto.IV_BYTES, sealed.size).contentEquals(clear))
    }

    @Test
    fun `two different IVs produce two different ciphertexts`() {
        val key = ByteArray(16) { 9 }
        val clear = ByteArray(24) { 0x2A }
        val first = HuaweiCrypto.encrypt(key, ByteArray(16) { 1 }, clear)
        val second = HuaweiCrypto.encrypt(key, ByteArray(16) { 2 }, clear)
        assertFalse(first.contentEquals(second))
    }

    @Test
    fun `a payload too short to carry its IV is refused`() {
        val key = ByteArray(16)
        assertNull(HuaweiCrypto.decrypt(key, ByteArray(HuaweiCrypto.IV_BYTES)))
        assertNull(HuaweiCrypto.decrypt(key, ByteArray(4)))
        assertNull(HuaweiCrypto.decrypt(key, ByteArray(0)))
    }

    @Test
    fun `counter mode adds no padding`() {
        // CTR is a stream cipher: a payload of any size keeps its size.
        val key = ByteArray(16)
        val iv = ByteArray(16)
        for (size in listOf(1, 15, 16, 17, 69)) {
            val sealed = HuaweiCrypto.encrypt(key, iv, ByteArray(size))
            assertEquals(HuaweiCrypto.IV_BYTES + size, sealed.size)
        }
    }

    @Test
    fun `a wrong key never returns the plaintext`() {
        val iv = ByteArray(16) { 5 }
        val clear = "tare de calibration".toByteArray()
        val sealed = HuaweiCrypto.encrypt(ByteArray(16) { 1 }, iv, clear)
        assertFalse(HuaweiCrypto.decrypt(ByteArray(16) { 2 }, sealed).contentEquals(clear))
    }

    @Test
    fun `the root key is indeed sixteen bytes`() {
        assertEquals(HuaweiCrypto.KEY_BYTES, HuaweiCrypto.deriveRootKey(keys, fictitiousMac).size)
        assertTrue(HuaweiCrypto.deriveRootKey(keys, fictitiousMac).any { it != 0.toByte() })
    }

    @Test
    fun `the key material belongs to the model, not to the engine`() {
        for (model in HuaweiScaleModel.entries) {
            assertEquals(HuaweiKeyMaterial.SCALE_3_PRO, model.keyMaterial)
        }
    }

    @Test
    fun `different key material produces a different root key`() {
        val other = HuaweiKeyMaterial(
            authenticationSecret = ByteArray(16) { 1 },
            whiteboxFirst = ByteArray(16) { 2 },
            whiteboxSecond = ByteArray(16) { 3 }
        )
        assertNotEquals(
            HuaweiCrypto.deriveRootKey(keys, fictitiousMac).toHex(),
            HuaweiCrypto.deriveRootKey(other, fictitiousMac).toHex()
        )
    }

    private fun ByteArray.toHex() = joinToString("") { "%02x".format(it) }
}
