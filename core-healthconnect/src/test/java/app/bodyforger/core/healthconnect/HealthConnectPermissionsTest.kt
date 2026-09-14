package app.bodyforger.core.healthconnect

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HealthConnectPermissionsTest {

    @Test
    fun corePermissionsContainHistoryPermission() {
        assertTrue(
            HealthConnectPermissions.CORE_READ_PERMISSIONS.contains(
                HealthConnectPermissions.READ_HEALTH_DATA_HISTORY
            )
        )
    }

    @Test
    fun hasHistoryPermissionReturnsTrueWhenPresent() {
        val granted = setOf(HealthConnectPermissions.READ_HEALTH_DATA_HISTORY)
        assertTrue(HealthConnectPermissions.hasHistoryPermission(granted))
    }

    @Test
    fun hasHistoryPermissionReturnsFalseWhenMissing() {
        val granted = setOf("android.permission.health.READ_EXERCISE")
        assertFalse(HealthConnectPermissions.hasHistoryPermission(granted))
    }

    @Test
    fun corePermissionsContainNineExpectedTypes() {
        assertEquals(9, HealthConnectPermissions.CORE_READ_PERMISSIONS.size)
    }
}
