package app.bodyforger.core.ble.huawei

import app.bodyforger.core.model.BiaProfile
import app.bodyforger.core.model.BiologicalSex
import app.bodyforger.core.model.ScaleUserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class HuaweiPayloadsTest {

    private val athlete = ScaleUserProfile(
        physiology = BiaProfile(BiologicalSex.MALE, ageYears = 30, heightCm = 180.0),
        lastWeightKg = 80.0
    )
    private val huid = "30033000012345678"

    @Test
    fun `le profil fait exactement soixante-neuf octets`() {
        assertEquals(HuaweiPayloads.PROFILE_BYTES, HuaweiPayloads.userProfile(huid, athlete).size)
    }

    @Test
    fun `chaque champ se lit a sa place`() {
        val payload = HuaweiPayloads.userProfile(huid, athlete)
        assertEquals(huid, String(payload.copyOf(huid.length), Charsets.US_ASCII))
        assertTrue("le HUID doit être complété de zéros", payload.copyOfRange(huid.length, 30).all { it == 0.toByte() })
        assertTrue("l'UID secondaire reste nul", payload.copyOfRange(30, 62).all { it == 0.toByte() })
        assertEquals(1, payload[62].toInt())
        assertEquals(30, payload[63].toInt())
        assertEquals(180, payload.uint16(64))
        assertEquals(8000, payload.uint16(66))
        assertEquals(0, payload[68].toInt())
    }

    @Test
    fun `une femme se distingue par un seul octet`() {
        val woman = athlete.copy(physiology = athlete.physiology.copy(sex = BiologicalSex.FEMALE))
        assertEquals(0, HuaweiPayloads.userProfile(huid, woman)[62].toInt())
    }

    @Test
    fun `l'acquittement de mesure porte un autre type`() {
        val commit = HuaweiPayloads.userProfile(huid, athlete, HuaweiPayloads.ProfileKind.MEASUREMENT_COMMIT)
        assertEquals(2, commit[68].toInt())
    }

    @Test
    fun `un poids inconnu part a zero plutot qu'invente`() {
        val unknown = athlete.copy(lastWeightKg = null)
        assertEquals(0, HuaweiPayloads.userProfile(huid, unknown).uint16(66))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `un HUID trop long est refuse plutot que tronque`() {
        // Tronquer produirait un identifiant valide mais different : un second emplacement
        HuaweiPayloads.userProfile("3".repeat(31), athlete)
    }

    @Test
    fun `l'heure suit le format standard du SIG`() {
        val payload = HuaweiPayloads.currentTime(LocalDateTime.of(2025, 3, 9, 7, 30, 15))
        assertEquals(10, payload.size)
        assertEquals(2025, payload.uint16(0))
        assertEquals(3, payload[2].toInt())
        assertEquals(9, payload[3].toInt())
        assertEquals(7, payload[4].toInt())
        assertEquals(30, payload[5].toInt())
        assertEquals(15, payload[6].toInt())
        // 2025-03-09 est un dimanche : 7 en convention ISO.
        assertEquals(7, payload[7].toInt())
    }

    @Test
    fun `l'armement du mode association tient en un octet`() {
        assertEquals(1, HuaweiPayloads.bindingControl(armed = true)[0].toInt())
        assertEquals(0, HuaweiPayloads.bindingControl(armed = false)[0].toInt())
    }

    private fun ByteArray.uint16(offset: Int) =
        (this[offset].toInt() and 0xFF) or ((this[offset + 1].toInt() and 0xFF) shl 8)
}
