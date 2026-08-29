package app.bodyforger.core.ble.huawei

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Vecteurs calculés indépendamment depuis la spécification (`../BLE/TECH.md` §2), sur des
 * **adresses MAC fictives** : la dérivation étant déterministe, un MAC réel figerait le
 * secret d'une balance existante dans un dépôt public.
 *
 * Ces valeurs valident la transcription Kotlin contre l'implémentation Python de référence.
 */
class HuaweiCryptoTest {

    private val fictitiousMac = "AA:BB:CC:DD:EE:FF"
    private val otherMac = "11:22:33:44:55:66"

    private val scaleNonce = ByteArray(16) { it.toByte() }
    private val clientNonce = ByteArray(16) { (it + 16).toByte() }

    // --- Dérivation de la clé racine ---

    @Test
    fun `la cle racine correspond a la reference`() {
        assertEquals(
            "d0d01fae597b65147ee89b03e26dd8ec",
            HuaweiCrypto.deriveRootKey(fictitiousMac).toHex()
        )
        assertEquals(
            "41c30ee71f3e88defdaf4bf5a216e4dc",
            HuaweiCrypto.deriveRootKey(otherMac).toHex()
        )
    }

    @Test
    fun `deux balances ne partagent jamais la meme cle racine`() {
        assertNotEquals(
            HuaweiCrypto.deriveRootKey(fictitiousMac).toHex(),
            HuaweiCrypto.deriveRootKey(otherMac).toHex()
        )
    }

    @Test
    fun `la derivation ignore la casse et les separateurs`() {
        val reference = HuaweiCrypto.deriveRootKey(fictitiousMac).toHex()
        assertEquals(reference, HuaweiCrypto.deriveRootKey("aa:bb:cc:dd:ee:ff").toHex())
        assertEquals(reference, HuaweiCrypto.deriveRootKey("AABBCCDDEEFF").toHex())
        assertEquals(reference, HuaweiCrypto.deriveRootKey("AA-BB-CC-DD-EE-FF").toHex())
    }

    @Test
    fun `la meme balance donne toujours la meme cle`() {
        // Rien n'est mémorisé entre deux connexions : la clé se recalcule.
        assertArrayEquals(
            HuaweiCrypto.deriveRootKey(fictitiousMac),
            HuaweiCrypto.deriveRootKey(fictitiousMac)
        )
    }

    // --- Authentification mutuelle ---

    @Test
    fun `les deux jetons correspondent a la reference`() {
        assertEquals(
            "e2c2c283c9545d4d67bce6e302ae0c0f274571455d92148746d037fe100def58",
            HuaweiCrypto.clientToken(scaleNonce, clientNonce).toHex()
        )
        assertEquals(
            "7d1c75ca91ef42b547ea1738b98b2ec2c853d53873fe617666e9a06839b7d052",
            HuaweiCrypto.expectedScaleToken(scaleNonce, clientNonce).toHex()
        )
    }

    @Test
    fun `le jeton de la balance differe de celui du client`() {
        // Deux sels distincts : sans cela, rejouer le jeton reçu suffirait à s'authentifier.
        assertNotEquals(
            HuaweiCrypto.clientToken(scaleNonce, clientNonce).toHex(),
            HuaweiCrypto.expectedScaleToken(scaleNonce, clientNonce).toHex()
        )
    }

    @Test
    fun `changer un seul bit d'un alea change tout le jeton`() {
        val altered = clientNonce.copyOf().also { it[0] = (it[0].toInt() xor 1).toByte() }
        assertNotEquals(
            HuaweiCrypto.clientToken(scaleNonce, clientNonce).toHex(),
            HuaweiCrypto.clientToken(scaleNonce, altered).toHex()
        )
    }

    @Test
    fun `l'ordre des aleas n'est pas interchangeable`() {
        assertNotEquals(
            HuaweiCrypto.clientToken(scaleNonce, clientNonce).toHex(),
            HuaweiCrypto.clientToken(clientNonce, scaleNonce).toHex()
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `un alea de mauvaise taille est refuse`() {
        HuaweiCrypto.clientToken(ByteArray(8), clientNonce)
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
        // En mode CTR, réutiliser un IV avec la même clé révèle le ou-exclusif des clairs.
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
        assertEquals(HuaweiCrypto.KEY_BYTES, HuaweiCrypto.deriveRootKey(fictitiousMac).size)
        assertTrue(HuaweiCrypto.deriveRootKey(fictitiousMac).any { it != 0.toByte() })
    }

    private fun ByteArray.toHex() = joinToString("") { "%02x".format(it) }
}
