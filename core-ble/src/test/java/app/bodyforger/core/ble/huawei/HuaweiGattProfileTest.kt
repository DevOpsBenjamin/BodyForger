package app.bodyforger.core.ble.huawei

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class HuaweiGattProfileTest {

    private val profile = HuaweiGattProfile.SCALE_3_PRO

    @Test
    fun `every characteristic of the protocol has its UUID`() {
        for (characteristic in HuaweiCharacteristic.entries) {
            assertNotNull("$characteristic sans UUID", profile[characteristic])
        }
    }

    @Test
    fun `two characteristics never share a UUID`() {
        val uuids = profile.characteristics.values
        assertEquals(uuids.size, uuids.toSet().size)
    }

    @Test
    fun `a received UUID is found again, an unknown one stays unknown`() {
        val uuid = profile[HuaweiCharacteristic.BIA_STREAM]!!
        assertEquals(HuaweiCharacteristic.BIA_STREAM, profile.characteristicOf(uuid))
        assertNull(profile.characteristicOf(UUID.fromString("00000000-0000-0000-0000-000000000000")))
    }

    @Test
    fun `only the session key's transport is protected by the root key`() {
        val rootProtected = HuaweiCharacteristic.entries
            .filter { it.protection == HuaweiCharacteristic.Protection.ROOT_KEY }
        assertEquals(listOf(HuaweiCharacteristic.SESSION_KEY), rootProtected)
    }

    @Test
    fun `authentication plays out in the clear, what follows it does not`() {
        assertEquals(HuaweiCharacteristic.Protection.CLEAR, HuaweiCharacteristic.AUTH_REQUEST.protection)
        assertEquals(HuaweiCharacteristic.Protection.CLEAR, HuaweiCharacteristic.AUTH_TOKENS.protection)
        for (sensitive in listOf(
            HuaweiCharacteristic.HUID_REGISTRATION,
            HuaweiCharacteristic.USER_PROFILE,
            HuaweiCharacteristic.BIA_STREAM
        )) {
            assertEquals("$sensitive", HuaweiCharacteristic.Protection.SESSION_KEY, sensitive.protection)
        }
    }

    @Test
    fun `the SIG standard characteristics are recognised by their shape`() {
        val sigSuffix = "-0000-1000-8000-00805f9b34fb"
        val standard = profile.characteristics.filterValues { it.toString().endsWith(sigSuffix) }
        assertEquals(
            setOf(
                HuaweiCharacteristic.TIME_SYNC,
                HuaweiCharacteristic.CAPABILITIES_REQUEST,
                HuaweiCharacteristic.CAPABILITIES_RESPONSE
            ),
            standard.keys
        )
        assertEquals(12, profile.characteristics.size - standard.size)
    }

    @Test
    fun `every model starts from the Pro's profile, as a testable hypothesis`() {
        for (model in HuaweiScaleModel.entries) {
            assertEquals(HuaweiGattProfile.SCALE_3_PRO, model.gattProfile)
        }
    }

    @Test
    fun `the client configuration descriptor is the standard one`() {
        assertTrue(
            HuaweiGattProfile.CLIENT_CONFIG_DESCRIPTOR.toString()
                .startsWith("00002902-0000-1000-8000")
        )
    }
}
