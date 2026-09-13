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
    fun `a complete pairing produces an Association carrying the tare that was read`() = runTest {
        val states = session(FakeScale()).run(address, "HUAWEI Scale 3 Pro-467", huid, profile).toList()

        val completed = states.last() as PairingState.Completed
        val association = completed.association
        assertEquals(address, association.deviceAddress)
        assertEquals(huid, association.huid)
        assertEquals(81.25, association.tareKg, 1e-9)
        // The athlete is already standing on it, so the confirmation reading is kept rather
        // than thrown away.
        assertEquals(80.00, completed.validation!!.massKg, 1e-9)
    }

    @Test
    fun `the tare is a mass-only weigh-in`() = runTest {
        // Neither the handle nor bare feet: with no impedance to read, skin contact with the
        // electrodes does not matter, and demanding it would be a gratuitous constraint.
        val states = session(FakeScale()).run(address, "Pro", huid, profile).toList()
        val stepOn = states.filterIsInstance<PairingState.Progress>()
            .first { it.instructions.contains(AthleteInstruction.STEP_ON) }
        assertEquals(listOf(AthleteInstruction.STEP_ON), stepOn.instructions)
    }

    @Test
    fun `the athlete is invited to step on exactly once`() = runTest {
        // The tare and then the BIA frame arrive during the same step-on: two invitations
        // would make the athlete step off in between, losing the measurement.
        val states = session(FakeScale()).run(address, "Pro", huid, profile).toList()
        val invitations = states.filterIsInstance<PairingState.Progress>()
            .count { it.instructions.contains(AthleteInstruction.STEP_ON) }
        assertEquals(1, invitations)
    }

    @Test
    fun `engraving precedes the invitation to step on`() = runTest {
        // By the time the athlete steps on, the memory slot is already consumed. That is what
        // makes abandoning the pairing harmless, and what the ordering has to reflect.
        val transport = FakeScale()
        val states = session(transport).run(address, "Pro", huid, profile).toList()

        val stepOnIndex = states.indexOfFirst {
            it is PairingState.Progress && it.instructions.contains(AthleteInstruction.STEP_ON)
        }
        val engravingIndex = states.indexOfFirst {
            it is PairingState.Progress && it.detail?.contains("Engraving") == true
        }
        assertTrue("la gravure doit etre ecrite", transport.journal.contains("write:HUID_REGISTRATION"))
        assertTrue("gravure avant la montee", engravingIndex < stepOnIndex)
    }

    @Test
    fun `a scale that delivers no confirmation frame pairs anyway`() = runTest {
        // Nothing establishes that every piece of hardware emits one during pairing: the tare
        // alone is enough to found the Association.
        val states = session(FakeScale(sendsValidation = false)).run(address, "Pro", huid, profile).toList()
        val completed = states.last() as PairingState.Completed
        assertEquals(81.25, completed.association.tareKg, 1e-9)
        assertTrue(completed.validation == null)
    }

    @Test
    fun `without a tare, no Association is produced`() = runTest {
        val states = session(FakeScale(sendsTare = false), tareTimeoutMs = 50)
            .run(address, "Pro", huid, profile).toList()

        assertEquals(SessionFailure.TIMED_OUT, (states.last() as PairingState.Failed).reason)
        assertFalse(states.any { it is PairingState.Completed })
    }

    @Test
    fun `a refusal status is never read as a tare`() = runTest {
        // Without this check, the status byte blended into the low half of the weight and
        // produced an absurd tare that nothing flagged.
        val states = session(FakeScale(statusByte = 1), tareTimeoutMs = 50)
            .run(address, "Pro", huid, profile).toList()
        assertEquals(SessionFailure.TIMED_OUT, (states.last() as PairingState.Failed).reason)
    }

    @Test
    fun `a zero tare is an absent weigh-in, not a tare`() = runTest {
        val states = session(FakeScale(tareHundredths = 0), tareTimeoutMs = 50)
            .run(address, "Pro", huid, profile).toList()
        assertEquals(SessionFailure.TIMED_OUT, (states.last() as PairingState.Failed).reason)
    }

    @Test
    fun `association mode is disarmed even when the tare is missing`() = runTest {
        val transport = FakeScale(sendsTare = false)
        session(transport, tareTimeoutMs = 50).run(address, "Pro", huid, profile).toList()

        assertEquals(2, transport.journal.count { it == "write:BINDING_CONTROL" })
    }

    @Test
    fun `an unauthenticated scale never receives an engraving`() = runTest {
        val transport = FakeScale(authenticates = false)
        val states = session(transport).run(address, "Pro", huid, profile).toList()

        assertEquals(SessionFailure.REJECTED_BY_DEVICE, (states.last() as PairingState.Failed).reason)
        assertFalse(transport.journal.contains("write:HUID_REGISTRATION"))
    }

    @Test
    fun `the HUID engraved is the one supplied, never a fresh one`() = runTest {
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
        private val tareHundredths: Int = 8125,
        private val statusByte: Int = 0
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
                        // Octet de statut, puis le poids en centiemes : la structure reelle.
                        val tare = byteArrayOf(
                            statusByte.toByte(),
                            (tareHundredths and 0xFF).toByte(),
                            (tareHundredths shr 8).toByte()
                        )
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
