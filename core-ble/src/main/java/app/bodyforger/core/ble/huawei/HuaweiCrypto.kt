package app.bodyforger.core.ble.huawei

import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * La couche cryptographique de la famille Haige.
 *
 * Trois mécanismes distincts, qu'il vaut mieux ne pas confondre :
 *
 * * La **clé racine** se dérive de l'adresse MAC physique de la balance. Elle ne sert qu'à
 *   convoyer la clé de session, et se recalcule à chaque connexion sans rien mémoriser.
 * * L'**authentification mutuelle** échange deux aléas et deux jetons HMAC : la balance
 *   prouve qu'elle connaît le secret constructeur, et l'application aussi.
 * * La **clé de session**, tirée au hasard à chaque connexion, chiffre ensuite les charges
 *   utiles en AES-128-CTR.
 *
 * ⚠️ Le matériel de clés n'appartient pas à cette classe mais au **modèle** : celles connues
 * ont été relevées sur la Scale 3 Pro, et rien ne dit qu'un autre modèle les partage. Voir
 * [HuaweiKeyMaterial].
 *
 * ⚠️ La clé racine dépend du **MAC physique**, pas d'un identifiant de plateforme. Sur
 * Android et Wear OS le scan natif le fournit directement ; c'est ce qui rend l'appairage
 * réalisable depuis la montre (cf. #7, #19). Une plateforme qui masque le MAC derrière un
 * UUID aléatoire — macOS le fait — ne peut pas dériver cette clé.
 */
object HuaweiCrypto {

    /** Suffixes distinguant le jeton de l'application de celui de la balance. */
    private val CLIENT_SALT = "1123".toByteArray(Charsets.UTF_8)
    private val SCALE_SALT = "9856".toByteArray(Charsets.UTF_8)

    const val KEY_BYTES = 16
    const val IV_BYTES = 16
    const val NONCE_BYTES = 16

    /**
     * Dérive la clé racine de l'adresse MAC physique.
     *
     * Deux passes : les tables de la boîte blanche sont d'abord mêlées et condensées, puis
     * ce condensat est recombiné avec le MAC. Le résultat est déterministe — une même
     * balance donne toujours la même clé — et ne quitte jamais l'appareil.
     *
     * @param macAddress au format `AA:BB:CC:DD:EE:FF`, séparateurs optionnels.
     */
    fun deriveRootKey(keys: HuaweiKeyMaterial, macAddress: String): ByteArray {
        val normalised = (macAddress.replace(":", "").replace("-", "").uppercase() + "0000")
            .toByteArray(Charsets.UTF_8)
        require(normalised.size >= KEY_BYTES) { "Adresse MAC trop courte : $macAddress" }

        val whitebox = ByteArray(KEY_BYTES) {
            ((keys.whiteboxFirst[it].toInt() shl 4) xor keys.whiteboxSecond[it].toInt()).toByte()
        }
        val condensed = sha256(whitebox).copyOf(KEY_BYTES)
        val mixed = ByteArray(KEY_BYTES) {
            ((condensed[it].toInt() and 0xFF shr 6) xor (normalised[it].toInt() and 0xFF)).toByte()
        }
        return sha256(mixed).copyOf(KEY_BYTES)
    }

    /**
     * Le jeton que l'application présente à la balance pour prouver qu'elle connaît le
     * secret constructeur. Double HMAC : le premier dérive une clé de l'aléa, le second
     * signe ce même aléa avec elle.
     */
    fun clientToken(keys: HuaweiKeyMaterial, scaleNonce: ByteArray, clientNonce: ByteArray): ByteArray =
        token(keys, scaleNonce, clientNonce, CLIENT_SALT)

    /**
     * Le jeton que la balance doit renvoyer. Le comparer à ce qu'elle envoie **authentifie
     * le matériel** : sans cette vérification, n'importe quel appareil se faisant passer
     * pour la balance serait accepté.
     */
    fun expectedScaleToken(keys: HuaweiKeyMaterial, scaleNonce: ByteArray, clientNonce: ByteArray): ByteArray =
        token(keys, scaleNonce, clientNonce, SCALE_SALT)

    private fun token(
        keys: HuaweiKeyMaterial,
        scaleNonce: ByteArray,
        clientNonce: ByteArray,
        salt: ByteArray
    ): ByteArray {
        require(scaleNonce.size == NONCE_BYTES) { "Aléa balance de ${scaleNonce.size} octets" }
        require(clientNonce.size == NONCE_BYTES) { "Aléa client de ${clientNonce.size} octets" }
        val combined = scaleNonce + clientNonce
        return hmacSha256(hmacSha256(keys.authenticationSecret + salt, combined), combined)
    }

    /**
     * Chiffre une charge utile : `[ IV de 16 octets ] + [ corps chiffré ]`.
     *
     * L'IV voyage en clair devant le corps, comme le protocole l'impose. Il doit être
     * imprévisible à chaque envoi : réutiliser un IV en mode CTR avec la même clé révèle le
     * ou-exclusif des deux clairs.
     */
    fun encrypt(sessionKey: ByteArray, iv: ByteArray, plaintext: ByteArray): ByteArray =
        iv + counterMode(Cipher.ENCRYPT_MODE, sessionKey, iv, plaintext)

    /**
     * Déchiffre une charge utile reçue, ou rend `null` si elle est trop courte pour porter
     * son propre IV — une trame tronquée n'est pas une trame vide.
     */
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
        require(key.size == KEY_BYTES) { "Clé de ${key.size} octets, 16 attendus" }
        require(iv.size == IV_BYTES) { "IV de ${iv.size} octets, 16 attendus" }
        return Cipher.getInstance("AES/CTR/NoPadding").apply {
            init(mode, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
        }.doFinal(data)
    }

    private fun sha256(data: ByteArray): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(data)

    private fun hmacSha256(key: ByteArray, data: ByteArray): ByteArray =
        Mac.getInstance("HmacSHA256").apply { init(SecretKeySpec(key, "HmacSHA256")) }.doFinal(data)

}
