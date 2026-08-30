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
    fun `la cle racine correspond a la reference`() {
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
    fun `deux balances ne partagent jamais la meme cle racine`() {
        assertNotEquals(
            HuaweiCrypto.deriveRootKey(keys, fictitiousMac).toHex(),
            HuaweiCrypto.deriveRootKey(keys, otherMac).toHex()
        )
    }

    @Test
    fun `la derivation ignore la casse et les separateurs`() {
        val reference = HuaweiCrypto.deriveRootKey(keys, fictitiousMac).toHex()
        assertEquals(reference, HuaweiCrypto.deriveRootKey(keys, "aa:bb:cc:dd:ee:ff").toHex())
        assertEquals(reference, HuaweiCrypto.deriveRootKey(keys, "AABBCCDDEEFF").toHex())
        assertEquals(reference, HuaweiCrypto.deriveRootKey(keys, "AA-BB-CC-DD-EE-FF").toHex())
    }

    @Test
    fun `la meme balance donne toujours la meme cle`() {
        assertArrayEquals(
            HuaweiCrypto.deriveRootKey(keys, fictitiousMac),
            HuaweiCrypto.deriveRootKey(keys, fictitiousMac)
        )
    }

    // --- Authentification mutuelle ---

    @Test
    fun `les deux jetons correspondent a la reference`() {
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
    fun `le jeton de la balance differe de celui du client`() {
        assertNotEquals(
            HuaweiCrypto.clientToken(keys, scaleNonce, clientNonce).toHex(),
            HuaweiCrypto.expectedScaleToken(keys, scaleNonce, clientNonce).toHex()
        )
    }

    @Test
    fun `changer un seul bit d'un alea change tout le jeton`() {
        val altered = clientNonce.copyOf().also { it[0] = (it[0].toInt() xor 1).toByte() }
        assertNotEquals(
            HuaweiCrypto.clientToken(keys, scaleNonce, clientNonce).toHex(),
            HuaweiCrypto.clientToken(keys, scaleNonce, altered).toHex()
        )
    }

    @Test
    fun `l'ordre des aleas n'est pas interchangeable`() {
        assertNotEquals(
            HuaweiCrypto.clientToken(keys, scaleNonce, clientNonce).toHex(),
            HuaweiCrypto.clientToken(keys, clientNonce, scaleNonce).toHex()
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `un alea de mauvaise taille est refuse`() {
        HuaweiCrypto.clientToken(keys, ByteArray(8), clientNonce)
    }

    // --- Chiffrement de session ---

    @Test
    fun `ce qui est chiffre se dechiffre`() {
        val key = ByteArray(16) { (it * 7).toByte() }
        val iv = ByteArray(16) { (it * 3).toByte() }
        val clear = "profil utilisateur".toByteArray()

        val sealed = HuaweiCrypto.encrypt(key, iv, clear)
        assertArrayEquals(clear, HuaweiCrypto.decrypt(key, sealed))
    }

    @Test
    fun `l'IV voyage en clair devant le corps chiffre`() {
        val key = ByteArray(16)
        val iv = ByteArray(16) { (it + 1).toByte() }
        val clear = ByteArray(30) { 0x41 }

        val sealed = HuaweiCrypto.encrypt(key, iv, clear)
        assertEquals(HuaweiCrypto.IV_BYTES + clear.size, sealed.size)
        assertArrayEquals(iv, sealed.copyOfRange(0, HuaweiCrypto.IV_BYTES))
        // Le corps, lui, ne doit rien laisser voir du clair.
        assertFalse(sealed.copyOfRange(HuaweiCrypto.IV_BYTES, sealed.size).contentEquals(clear))
    }

    @Test
    fun `deux IV differents produisent deux chiffres differents`() {
        val key = ByteArray(16) { 9 }
        val clear = ByteArray(24) { 0x2A }
        val first = HuaweiCrypto.encrypt(key, ByteArray(16) { 1 }, clear)
        val second = HuaweiCrypto.encrypt(key, ByteArray(16) { 2 }, clear)
        assertFalse(first.contentEquals(second))
    }

    @Test
    fun `une charge trop courte pour porter son IV est refusee`() {
        val key = ByteArray(16)
        assertNull(HuaweiCrypto.decrypt(key, ByteArray(HuaweiCrypto.IV_BYTES)))
        assertNull(HuaweiCrypto.decrypt(key, ByteArray(4)))
        assertNull(HuaweiCrypto.decrypt(key, ByteArray(0)))
    }

    @Test
    fun `le mode compteur n'ajoute aucun remplissage`() {
        // CTR chiffre flot : une charge de taille quelconque garde sa taille.
        val key = ByteArray(16)
        val iv = ByteArray(16)
        for (size in listOf(1, 15, 16, 17, 69)) {
            val sealed = HuaweiCrypto.encrypt(key, iv, ByteArray(size))
            assertEquals(HuaweiCrypto.IV_BYTES + size, sealed.size)
        }
    }

    @Test
    fun `une mauvaise cle ne rend jamais le clair`() {
        val iv = ByteArray(16) { 5 }
        val clear = "tare de calibration".toByteArray()
        val sealed = HuaweiCrypto.encrypt(ByteArray(16) { 1 }, iv, clear)
        assertFalse(HuaweiCrypto.decrypt(ByteArray(16) { 2 }, sealed).contentEquals(clear))
    }

    @Test
    fun `la cle racine fait bien seize octets`() {
        assertEquals(HuaweiCrypto.KEY_BYTES, HuaweiCrypto.deriveRootKey(keys, fictitiousMac).size)
        assertTrue(HuaweiCrypto.deriveRootKey(keys, fictitiousMac).any { it != 0.toByte() })
    }

    @Test
    fun `le materiel de cles appartient au modele, pas au moteur`() {
        for (model in HuaweiScaleModel.entries) {
            assertEquals(HuaweiKeyMaterial.SCALE_3_PRO, model.keyMaterial)
        }
    }

    @Test
    fun `un materiel different produit une cle racine differente`() {
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
