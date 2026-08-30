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
    fun `chaque caracteristique du protocole a son UUID`() {
        for (characteristic in HuaweiCharacteristic.entries) {
            assertNotNull("$characteristic sans UUID", profile[characteristic])
        }
    }

    @Test
    fun `deux caracteristiques ne partagent jamais un UUID`() {
        val uuids = profile.characteristics.values
        assertEquals(uuids.size, uuids.toSet().size)
    }

    @Test
    fun `un UUID recu se retrouve, un inconnu reste inconnu`() {
        val uuid = profile[HuaweiCharacteristic.BIA_STREAM]!!
        assertEquals(HuaweiCharacteristic.BIA_STREAM, profile.characteristicOf(uuid))
        assertNull(profile.characteristicOf(UUID.fromString("00000000-0000-0000-0000-000000000000")))
    }

    @Test
    fun `seul le transport de la cle de session est protege par la cle racine`() {
        // La clé de session ne peut évidemment pas se protéger elle-même : c'est la clé
        // racine, dérivée du MAC, qui convoie son échange.
        val rootProtected = HuaweiCharacteristic.entries
            .filter { it.protection == HuaweiCharacteristic.Protection.ROOT_KEY }
        assertEquals(listOf(HuaweiCharacteristic.SESSION_KEY), rootProtected)
    }

    @Test
    fun `l'authentification se joue en clair, ce qui la suit ne l'est plus`() {
        // Rien n'est encore négocié pendant le handshake : il ne peut pas être chiffré.
        assertEquals(HuaweiCharacteristic.Protection.CLEAR, HuaweiCharacteristic.AUTH_REQUEST.protection)
        assertEquals(HuaweiCharacteristic.Protection.CLEAR, HuaweiCharacteristic.AUTH_TOKENS.protection)
        // Tout ce qui touche à l'athlète, en revanche, voyage chiffré.
        for (sensitive in listOf(
            HuaweiCharacteristic.HUID_REGISTRATION,
            HuaweiCharacteristic.USER_PROFILE,
            HuaweiCharacteristic.BIA_STREAM
        )) {
            assertEquals("$sensitive", HuaweiCharacteristic.Protection.SESSION_KEY, sensitive.protection)
        }
    }

    @Test
    fun `les caracteristiques standard du SIG se reconnaissent a leur forme`() {
        // Motif `0000XXXX-0000-1000-8000-00805f9b34fb` : allouées par le Bluetooth SIG, elles
        // ne dépendent d'aucun modèle. Les onze autres sont propriétaires.
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
    fun `tous les modeles partent du profil de la Pro, comme hypothese testable`() {
        // Relevé sur la Pro seule ; une caractéristique absente à la découverte des services
        // réfute l'hypothèse pour un autre modèle — et se diagnostique, elle.
        for (model in HuaweiScaleModel.entries) {
            assertEquals(HuaweiGattProfile.SCALE_3_PRO, model.gattProfile)
        }
    }

    @Test
    fun `le descripteur de configuration client est celui du standard`() {
        assertTrue(
            HuaweiGattProfile.CLIENT_CONFIG_DESCRIPTOR.toString()
                .startsWith("00002902-0000-1000-8000")
        )
    }
}
