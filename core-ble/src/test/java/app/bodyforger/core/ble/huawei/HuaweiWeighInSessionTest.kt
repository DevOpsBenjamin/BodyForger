package app.bodyforger.core.ble.huawei

import app.bodyforger.core.ble.ScaleNotification
import app.bodyforger.core.ble.ScaleTransport
import app.bodyforger.core.ble.SessionFailure
import app.bodyforger.core.ble.SessionPhase
import app.bodyforger.core.ble.WeighInState
import app.bodyforger.core.model.BiaProfile
import app.bodyforger.core.model.BiologicalSex
import app.bodyforger.core.model.ScaleAssociation
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

/**
 * La pesée complète, jouée contre un faux transport. C'est l'orchestration qui se trompe,
 * pas le Bluetooth — d'où l'intérêt de pouvoir la dérouler sans matériel.
 */
class HuaweiWeighInSessionTest {

    private val model = HuaweiScaleModel.HUAWEI_SCALE_3_PRO
    private val association = ScaleAssociation(
        deviceAddress = "AA:BB:CC:DD:EE:FF",
        huid = "30033000012345678",
        tareKg = 80.0,
        advertisedName = "HUAWEI Scale 3 Pro-467"
    )
    private val profile = ScaleUserProfile(
        physiology = BiaProfile(BiologicalSex.MALE, ageYears = 30, heightCm = 180.0),
        lastWeightKg = 80.0
    )

    private val predictable = object : SecureRandom() {
        private var seed = 0
        override fun nextBytes(bytes: ByteArray) {
            for (i in bytes.indices) bytes[i] = (seed + i).toByte()
            seed++
        }
    }

    /** Trame synthétique : 80,00 kg, 18,5 % de gras, 72 bpm, douze résistances. */
    private val telemetryFrame = hex(
        "401fb900e9070309071e000736101a1890154a150e15c8144800740e861542130613ca128e12"
    )

    @Test
    fun `une pesee nominale aboutit sur la telemetrie`() = runTest {
        val states = session(FakeScale()).run(association, association.huid, profile).toList()

        val completed = states.last() as WeighInState.Completed
        assertEquals(80.00, completed.telemetry.massKg, 1e-9)
        assertEquals(12, completed.telemetry.rawImpedances.ohmsByReading.size)
    }

    @Test
    fun `la progression couvre les phases dans l'ordre`() = runTest {
        val states = session(FakeScale()).run(association, association.huid, profile).toList()
        val phases = states.filterIsInstance<WeighInState.Progress>().map { it.phase }

        assertEquals(SessionPhase.DISCOVERING, phases.first())
        assertEquals(SessionPhase.MEASURING, phases.last())
        // Une phase ne revient jamais en arrière.
        assertEquals(phases.sortedBy { it.ordinal }, phases)
    }

    @Test
    fun `l'athlete est invite a monter avant la mesure, pas avant`() = runTest {
        val states = session(FakeScale()).run(association, association.huid, profile).toList()
        val stepOn = states.filterIsInstance<WeighInState.Progress>()
            .first { it.phase == SessionPhase.AWAITING_ATHLETE }

        // Monter et saisir la poignée sont simultanés : les séparer ferait relâcher la
        // poignée avant le relevé.
        assertEquals(2, stepOn.instructions.size)
    }

    @Test
    fun `une balance qui ne repond pas au handshake ne recoit jamais le profil`() = runTest {
        val transport = FakeScale(authenticates = false)
        val states = session(transport).run(association, association.huid, profile).toList()

        assertEquals(SessionFailure.REJECTED_BY_DEVICE, (states.last() as WeighInState.Failed).reason)
        assertFalse(
            "le profil de l'athlète ne doit pas partir vers un appareil non authentifié",
            transport.journal.contains("write:USER_PROFILE")
        )
    }

    @Test
    fun `une connexion impossible echoue sans rien tenter`() = runTest {
        val transport = FakeScale(connects = false)
        val states = session(transport).run(association, association.huid, profile).toList()

        assertEquals(SessionFailure.CONNECTION_LOST, (states.last() as WeighInState.Failed).reason)
        assertTrue(transport.journal.none { it.startsWith("write") })
    }

    @Test
    fun `un athlete qui ne monte jamais fait expirer la pesee`() = runTest {
        val transport = FakeScale(sendsTelemetry = false)
        val states = session(transport, athleteTimeoutMs = 50).run(association, association.huid, profile).toList()

        assertEquals(SessionFailure.TIMED_OUT, (states.last() as WeighInState.Failed).reason)
    }

    @Test
    fun `une trame illisible n'est jamais rendue comme une mesure`() = runTest {
        val transport = FakeScale(corruptTelemetry = true)
        val states = session(transport).run(association, association.huid, profile).toList()

        assertEquals(SessionFailure.DEVICE_ERROR, (states.last() as WeighInState.Failed).reason)
    }

    @Test
    fun `la balance recoit son acquittement pour clore la session`() = runTest {
        // Sans lui, elle reste armée et la pesée suivante devra attendre son extinction.
        val transport = FakeScale()
        session(transport).run(association, association.huid, profile).toList()

        assertEquals(2, transport.journal.count { it == "write:USER_PROFILE" })
    }

    @Test
    fun `rien n'est emis apres un echec`() = runTest {
        val states = session(FakeScale(connects = false)).run(association, association.huid, profile).toList()
        assertTrue(states.last() is WeighInState.Failed)
        assertEquals(1, states.count { it is WeighInState.Failed || it is WeighInState.Completed })
    }

    private fun session(
        transport: ScaleTransport,
        athleteTimeoutMs: Long = 5_000
    ) = HuaweiWeighInSession(
        transport = transport,
        model = model,
        clock = { LocalDateTime.of(2025, 3, 9, 7, 30, 0) },
        random = predictable,
        athleteTimeoutMs = athleteTimeoutMs
    )

    private inner class FakeScale(
        private val connects: Boolean = true,
        private val authenticates: Boolean = true,
        private val sendsTelemetry: Boolean = true,
        private val corruptTelemetry: Boolean = false
    ) : ScaleTransport {

        val journal = mutableListOf<String>()
        private val scaleNonce = ByteArray(HuaweiCrypto.NONCE_BYTES) { (0xA0 + it).toByte() }
        private var sessionKey: ByteArray? = null
        private var rootKey = HuaweiCrypto.deriveRootKey(model.keyMaterial, association.deviceAddress)

        private val flow = MutableSharedFlow<ScaleNotification>(
            replay = 16,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        override val incoming: Flow<ScaleNotification> = flow

        override suspend fun connect() = connects
        override suspend fun subscribe(characteristic: HuaweiCharacteristic) = true
        override suspend fun unsubscribe(characteristic: HuaweiCharacteristic) = true
        override fun close() = Unit

        override suspend fun write(
            characteristic: HuaweiCharacteristic,
            payload: ByteArray,
            withResponse: Boolean
        ): Boolean {
            journal += "write:$characteristic"
            when (characteristic) {
                HuaweiCharacteristic.AUTH_REQUEST ->
                    if (authenticates) emit(characteristic, scaleNonce)
                HuaweiCharacteristic.AUTH_TOKENS -> if (authenticates) {
                    val clientNonce = payload.copyOf(HuaweiCrypto.NONCE_BYTES)
                    emit(characteristic, HuaweiCrypto.expectedScaleToken(model.keyMaterial, scaleNonce, clientNonce))
                }
                HuaweiCharacteristic.SESSION_KEY -> {
                    sessionKey = HuaweiCrypto.decrypt(rootKey, payload)
                    emit(characteristic, byteArrayOf(0))
                }
                HuaweiCharacteristic.BIA_STREAM -> Unit
                HuaweiCharacteristic.USER_PROFILE -> if (sendsTelemetry && journal.count { it == "write:USER_PROFILE" } == 1) {
                    // La balance ne livre sa trame qu'une fois configurée.
                    val key = sessionKey ?: return true
                    val body = if (corruptTelemetry) ByteArray(4) else telemetryFrame
                    val iv = ByteArray(HuaweiCrypto.IV_BYTES) { 0x11 }
                    emit(HuaweiCharacteristic.BIA_STREAM, HuaweiCrypto.encrypt(key, iv, body))
                }
                else -> Unit
            }
            return true
        }

        private fun emit(characteristic: HuaweiCharacteristic, payload: ByteArray) {
            flow.tryEmit(ScaleNotification(characteristic, payload, encrypted = false))
        }
    }

    private fun hex(value: String) = ByteArray(value.length / 2) {
        value.substring(it * 2, it * 2 + 2).toInt(16).toByte()
    }
}
