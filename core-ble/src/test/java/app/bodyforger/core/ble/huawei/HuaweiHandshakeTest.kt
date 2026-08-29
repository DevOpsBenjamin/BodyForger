package app.bodyforger.core.ble.huawei

import app.bodyforger.core.ble.ScaleNotification
import app.bodyforger.core.ble.ScaleTransport
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.SecureRandom

/**
 * Le handshake se vérifie sans matériel : c'est précisément pour cela que le transport est
 * un contrat et non une classe Android.
 *
 * Le faux transport joue le rôle d'une balance qui répond correctement, et chaque test le
 * fait dévier sur un point pour observer si la négociation s'en aperçoit.
 */
class HuaweiHandshakeTest {

    private val mac = "AA:BB:CC:DD:EE:FF"
    private val model = HuaweiScaleModel.HUAWEI_SCALE_3_PRO
    private val keys = model.keyMaterial

    /** Aléas prévisibles : le test doit pouvoir recalculer ce que la balance devrait répondre. */
    private val predictable = object : SecureRandom() {
        private var seed = 0
        override fun nextBytes(bytes: ByteArray) {
            for (i in bytes.indices) bytes[i] = (seed + i).toByte()
            seed++
        }
    }

    @Test
    fun `une balance conforme livre une cle de session`() = runTest {
        val transport = FakeScale()
        val sessionKey = HuaweiHandshake(transport, model, predictable).negotiate(mac)

        assertNotNull("la négociation devrait aboutir", sessionKey)
        assertEquals(HuaweiCrypto.KEY_BYTES, sessionKey!!.size)
    }

    @Test
    fun `l'abonnement precede toute ecriture`() = runTest {
        // Les réponses arrivent par notification : écrire avant de s'être abonné revient à
        // parler dans le vide.
        val transport = FakeScale()
        HuaweiHandshake(transport, model, predictable).negotiate(mac)

        val firstWrite = transport.journal.indexOfFirst { it.startsWith("write") }
        val lastSubscribe = transport.journal.indexOfLast { it.startsWith("subscribe") }
        assertTrue("abonnements après la première écriture", lastSubscribe < firstWrite)
    }

    @Test
    fun `un jeton de balance faux interrompt la negociation`() = runTest {
        // La référence calculait ce jeton sans jamais le comparer : n'importe quel appareil
        // annonçant le bon nom aurait été accepté, puis aurait reçu le profil de l'athlète.
        val transport = FakeScale(scaleTokenValid = false)
        assertNull(HuaweiHandshake(transport, model, predictable).negotiate(mac))
    }

    @Test
    fun `une cle de session n'est jamais transmise apres un jeton faux`() = runTest {
        val transport = FakeScale(scaleTokenValid = false)
        HuaweiHandshake(transport, model, predictable).negotiate(mac)
        assertFalse(
            "la clé de session ne doit pas partir vers un appareil non authentifié",
            transport.journal.any { it == "write:SESSION_KEY" }
        )
    }

    @Test
    fun `une balance muette fait echouer la negociation`() = runTest {
        assertNull(HuaweiHandshake(FakeScale(answers = false), model, predictable).negotiate(mac))
    }

    @Test
    fun `un alea trop court est refuse`() = runTest {
        // Un aléa tronqué complété par des zéros affaiblirait l'authentification sans que
        // rien ne le signale.
        val transport = FakeScale(scaleNonceBytes = 8)
        assertNull(HuaweiHandshake(transport, model, predictable).negotiate(mac))
    }

    @Test
    fun `un abonnement refuse arrete tout`() = runTest {
        assertNull(HuaweiHandshake(FakeScale(canSubscribe = false), model, predictable).negotiate(mac))
    }

    /** Une balance qui répond correctement, sauf là où le test la fait dévier. */
    private inner class FakeScale(
        private val answers: Boolean = true,
        private val scaleTokenValid: Boolean = true,
        private val canSubscribe: Boolean = true,
        private val scaleNonceBytes: Int = HuaweiCrypto.NONCE_BYTES
    ) : ScaleTransport {

        val journal = mutableListOf<String>()
        private val scaleNonce = ByteArray(scaleNonceBytes) { (0xA0 + it).toByte() }
        private var clientNonce: ByteArray? = null

        private val flow = MutableSharedFlow<ScaleNotification>(
            replay = 8,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        override val incoming: Flow<ScaleNotification> = flow

        override suspend fun connect() = true

        override suspend fun subscribe(characteristic: HuaweiCharacteristic): Boolean {
            journal += "subscribe:$characteristic"
            return canSubscribe
        }

        override suspend fun unsubscribe(characteristic: HuaweiCharacteristic) = true

        override suspend fun write(
            characteristic: HuaweiCharacteristic,
            payload: ByteArray,
            withResponse: Boolean
        ): Boolean {
            journal += "write:$characteristic"
            if (!answers) return true
            when (characteristic) {
                HuaweiCharacteristic.AUTH_REQUEST -> emit(characteristic, scaleNonce)
                HuaweiCharacteristic.AUTH_TOKENS -> {
                    clientNonce = payload.copyOf(HuaweiCrypto.NONCE_BYTES)
                    val token = if (scaleTokenValid) {
                        HuaweiCrypto.expectedScaleToken(keys, scaleNonce, clientNonce!!)
                    } else {
                        ByteArray(32) { 0x7F }
                    }
                    emit(characteristic, token)
                }
                HuaweiCharacteristic.SESSION_KEY -> emit(characteristic, byteArrayOf(0))
                else -> Unit
            }
            return true
        }

        override fun close() = Unit

        private fun emit(characteristic: HuaweiCharacteristic, payload: ByteArray) {
            flow.tryEmit(ScaleNotification(characteristic, payload, encrypted = false))
        }
    }
}
