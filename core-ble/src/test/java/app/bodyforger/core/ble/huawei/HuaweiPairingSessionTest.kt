package app.bodyforger.core.ble.huawei

import app.bodyforger.core.ble.AthleteInstruction
import app.bodyforger.core.ble.PairingState
import app.bodyforger.core.ble.ScaleNotification
import app.bodyforger.core.ble.ScaleTransport
import app.bodyforger.core.ble.SessionFailure
import app.bodyforger.core.model.BiaProfile
import app.bodyforger.core.model.BiologicalSex
import app.bodyforger.core.model.ScaleUserProfile
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.SecureRandom
import java.time.LocalDateTime

class HuaweiPairingSessionTest {

    private val model = HuaweiScaleModel.HUAWEI_SCALE_3_PRO
    private val address = "AA:BB:CC:DD:EE:FF"
    private val huid = "30033000012345678"
    private val profile = ScaleUserProfile(BiaProfile(BiologicalSex.MALE, 30, 180.0))

    private val predictable = object : SecureRandom() {
        private var seed = 0
        override fun nextBytes(bytes: ByteArray) {
            for (i in bytes.indices) bytes[i] = (seed + i).toByte()
            seed++
        }
    }

    @Test
    fun `un appairage complet produit une Association portant la tare relevee`() = runTest {
        val states = session(FakeScale()).run(address, "HUAWEI Scale 3 Pro-467", huid, profile).toList()

        val completed = states.last() as PairingState.Completed
        val association = completed.association
        assertEquals(address, association.deviceAddress)
        assertEquals(huid, association.huid)
        assertEquals(81.25, association.tareKg, 1e-9)
        // L'athlete etant deja monte, la pesee de validation est conservee plutot que perdue.
        assertEquals(80.00, completed.validation!!.massKg, 1e-9)
    }

    @Test
    fun `l'athlete n'est invite a monter qu'une seule fois`() = runTest {
        // La tare puis la trame BIA arrivent pendant la meme montee : deux invitations le
        // feraient descendre entre les deux et perdraient la mesure.
        val states = session(FakeScale()).run(address, "Pro", huid, profile).toList()
        val invitations = states.filterIsInstance<PairingState.Progress>()
            .count { it.instructions.contains(AthleteInstruction.STEP_ON_BAREFOOT) }
        assertEquals(1, invitations)
    }

    @Test
    fun `la gravure precede l'invitation a monter`() = runTest {
        // Quand l'athlete monte, l'emplacement memoire est deja consomme. C'est ce qui rend
        // un abandon benin, et ce que l'ordre doit refleter.
        val transport = FakeScale()
        val states = session(transport).run(address, "Pro", huid, profile).toList()

        val stepOnIndex = states.indexOfFirst {
            it is PairingState.Progress && it.instructions.contains(AthleteInstruction.STEP_ON_BAREFOOT)
        }
        val engravingIndex = states.indexOfFirst {
            it is PairingState.Progress && it.detail?.contains("Gravure") == true
        }
        assertTrue("la gravure doit etre ecrite", transport.journal.contains("write:HUID_REGISTRATION"))
        assertTrue("gravure avant la montee", engravingIndex < stepOnIndex)
    }

    @Test
    fun `une balance qui ne livre pas de trame de validation s'appaire quand meme`() = runTest {
        // Rien n'etablit que tout materiel en produise une pendant l'appairage : la tare
        // suffit a fonder l'Association.
        val states = session(FakeScale(sendsValidation = false)).run(address, "Pro", huid, profile).toList()
        val completed = states.last() as PairingState.Completed
        assertEquals(81.25, completed.association.tareKg, 1e-9)
        assertTrue(completed.validation == null)
    }

    @Test
    fun `sans tare, aucune Association n'est produite`() = runTest {
        val states = session(FakeScale(sendsTare = false), tareTimeoutMs = 50)
            .run(address, "Pro", huid, profile).toList()

        assertEquals(SessionFailure.TIMED_OUT, (states.last() as PairingState.Failed).reason)
        assertFalse(states.any { it is PairingState.Completed })
    }

    @Test
    fun `une tare nulle est une absence de pesee, pas une tare`() = runTest {
        val states = session(FakeScale(tareHundredths = 0), tareTimeoutMs = 50)
            .run(address, "Pro", huid, profile).toList()
        assertEquals(SessionFailure.TIMED_OUT, (states.last() as PairingState.Failed).reason)
    }

    @Test
    fun `le mode association est desarme meme quand la tare manque`() = runTest {
        val transport = FakeScale(sendsTare = false)
        session(transport, tareTimeoutMs = 50).run(address, "Pro", huid, profile).toList()

        assertEquals(2, transport.journal.count { it == "write:BINDING_CONTROL" })
    }

    @Test
    fun `une balance non authentifiee ne recoit jamais de gravure`() = runTest {
        val transport = FakeScale(authenticates = false)
        val states = session(transport).run(address, "Pro", huid, profile).toList()

        assertEquals(SessionFailure.REJECTED_BY_DEVICE, (states.last() as PairingState.Failed).reason)
        assertFalse(transport.journal.contains("write:HUID_REGISTRATION"))
    }

    @Test
    fun `le HUID grave est celui fourni, jamais un nouveau`() = runTest {
        val transport = FakeScale()
        session(transport).run(address, "Pro", huid, profile).toList()
        assertTrue(transport.engravedHuid?.startsWith(huid) == true)
    }

    private fun session(transport: ScaleTransport, tareTimeoutMs: Long = 5_000) =
        HuaweiPairingSession(
            transport = transport,
            model = model,
            clock = { LocalDateTime.of(2025, 3, 9, 7, 30, 0) },
            random = predictable,
            athleteTimeoutMs = tareTimeoutMs
        )

    private fun hex(value: String) = ByteArray(value.length / 2) {
        value.substring(it * 2, it * 2 + 2).toInt(16).toByte()
    }

    private inner class FakeScale(
        private val authenticates: Boolean = true,
        private val sendsTare: Boolean = true,
        private val sendsValidation: Boolean = true,
        private val tareHundredths: Int = 8125
    ) : ScaleTransport {

        val journal = mutableListOf<String>()
        var engravedHuid: String? = null
        private val scaleNonce = ByteArray(HuaweiCrypto.NONCE_BYTES) { (0xA0 + it).toByte() }
        private var sessionKey: ByteArray? = null
        private val rootKey = HuaweiCrypto.deriveRootKey(model.keyMaterial, address)

        private val validationFrame = ByteArray(76 / 2) { 0 }.also {
            hex("401fb900e9070309071e000736101a1890154a150e15c8144800740e861542130613ca128e12").copyInto(it)
        }

        private val flow = MutableSharedFlow<ScaleNotification>(replay = 16, onBufferOverflow = BufferOverflow.DROP_OLDEST)
        override val incoming: Flow<ScaleNotification> = flow

        override suspend fun connect() = true
        override suspend fun subscribe(characteristic: HuaweiCharacteristic) = true
        override suspend fun unsubscribe(characteristic: HuaweiCharacteristic) = true
        override suspend fun writeRaw(
            characteristic: HuaweiCharacteristic,
            frame: ByteArray,
            withResponse: Boolean
        ): Boolean = write(characteristic, frame, withResponse)

        override fun close() = Unit

        override suspend fun write(
            characteristic: HuaweiCharacteristic,
            payload: ByteArray,
            withResponse: Boolean
        ): Boolean {
            journal += "write:$characteristic"
            when (characteristic) {
                HuaweiCharacteristic.AUTH_REQUEST -> if (authenticates) emit(characteristic, scaleNonce)
                HuaweiCharacteristic.AUTH_TOKENS -> if (authenticates) {
                    val clientNonce = payload.copyOf(HuaweiCrypto.NONCE_BYTES)
                    emit(characteristic, HuaweiCrypto.expectedScaleToken(model.keyMaterial, scaleNonce, clientNonce))
                }
                HuaweiCharacteristic.SESSION_KEY -> {
                    sessionKey = HuaweiCrypto.decrypt(rootKey, payload)
                    emit(characteristic, byteArrayOf(0))
                }
                HuaweiCharacteristic.HUID_REGISTRATION -> {
                    val key = sessionKey ?: return true
                    engravedHuid = HuaweiCrypto.decrypt(key, payload)?.toString(Charsets.US_ASCII)
                    if (sendsTare) {
                        val tare = byteArrayOf((tareHundredths and 0xFF).toByte(), (tareHundredths shr 8).toByte())
                        val iv = ByteArray(HuaweiCrypto.IV_BYTES) { 0x22 }
                        emit(characteristic, HuaweiCrypto.encrypt(key, iv, tare))
                        // La trame de validation suit, pendant la meme montee.
                        if (sendsValidation) emit(
                            HuaweiCharacteristic.BIA_STREAM,
                            HuaweiCrypto.encrypt(key, ByteArray(HuaweiCrypto.IV_BYTES) { 0x33 }, validationFrame)
                        )
                    }
                }
                else -> Unit
            }
            return true
        }

        private fun emit(characteristic: HuaweiCharacteristic, payload: ByteArray) {
            flow.tryEmit(ScaleNotification(characteristic, payload, encrypted = false))
        }
    }
}
