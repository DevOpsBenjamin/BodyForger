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
 * The handshake against a scale that is only a contract, never an Android class: the
 * negotiation is pure logic, so it is testable off-device.
 */
class HuaweiHandshakeTest {

    private val mac = "AA:BB:CC:DD:EE:FF"
    private val model = HuaweiScaleModel.HUAWEI_SCALE_3_PRO
    private val keys = model.keyMaterial

    /** Predictable nonces: the test must be able to recompute what the scale should answer. */
    private val predictable = object : SecureRandom() {
        private var seed = 0
        override fun nextBytes(bytes: ByteArray) {
            for (i in bytes.indices) bytes[i] = (seed + i).toByte()
            seed++
        }
    }

    @Test
    fun `a compliant scale yields a session key`() = runTest {
        val transport = FakeScale()
        val sessionKey = HuaweiHandshake(transport, model, predictable).negotiate(mac)

        assertNotNull("the negotiation should succeed", sessionKey)
        assertEquals(HuaweiCrypto.KEY_BYTES, sessionKey!!.size)
    }

    @Test
    fun `subscription precedes every write`() = runTest {
        // Subscribing after the first write means the answer is already gone: the driver
        // would be talking into the void.
        val transport = FakeScale()
        HuaweiHandshake(transport, model, predictable).negotiate(mac)

        val firstWrite = transport.journal.indexOfFirst { it.startsWith("write") }
        val lastSubscribe = transport.journal.indexOfLast { it.startsWith("subscribe") }
        assertTrue("subscriptions after the first write", lastSubscribe < firstWrite)
    }

    @Test
    fun `a wrong scale token aborts the negotiation`() = runTest {
        val transport = FakeScale(scaleTokenValid = false)
        assertNull(HuaweiHandshake(transport, model, predictable).negotiate(mac))
    }

    @Test
    fun `a session key is never sent after a wrong token`() = runTest {
        val transport = FakeScale(scaleTokenValid = false)
        HuaweiHandshake(transport, model, predictable).negotiate(mac)
        assertFalse(
            "the session key must not be sent to an unauthenticated scale",
            transport.journal.any { it == "write:SESSION_KEY" }
        )
    }

    @Test
    fun `a silent scale fails the negotiation`() = runTest {
        assertNull(HuaweiHandshake(FakeScale(answers = false), model, predictable).negotiate(mac))
    }

    @Test
    fun `a nonce that is too short is refused`() = runTest {
        // rien ne le signale.
        val transport = FakeScale(scaleNonceBytes = 8)
        assertNull(HuaweiHandshake(transport, model, predictable).negotiate(mac))
    }

    @Test
    fun `a refused subscription stops everything`() = runTest {
        assertNull(HuaweiHandshake(FakeScale(canSubscribe = false), model, predictable).negotiate(mac))
    }

    /** A scale that answers correctly, except where the test makes it deviate. */
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

        override suspend fun writeRaw(
            characteristic: HuaweiCharacteristic,
            frame: ByteArray,
            withResponse: Boolean
        ): Boolean = write(characteristic, frame, withResponse)

        override fun close() = Unit

        private fun emit(characteristic: HuaweiCharacteristic, payload: ByteArray) {
            flow.tryEmit(ScaleNotification(characteristic, payload, encrypted = false))
        }
    }
}
