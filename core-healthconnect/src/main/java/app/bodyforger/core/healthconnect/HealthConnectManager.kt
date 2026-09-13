package app.bodyforger.core.healthconnect

import android.content.Context
import androidx.health.connect.client.HealthConnectClient

/**
 * Whether a Health Connect provider exists on the device this runs on.
 *
 * Phones and watches do not answer the same way, and the difference decides who exports:
 * a watch with no provider has to hand its sessions to the phone.
 */
enum class HealthConnectAvailability {
    AVAILABLE,
    UNAVAILABLE,
    UPDATE_REQUIRED,
    UNKNOWN
}

class HealthConnectManager(private val context: Context) {

    fun availability(): HealthConnectAvailability =
        when (HealthConnectClient.getSdkStatus(context)) {
            HealthConnectClient.SDK_AVAILABLE -> HealthConnectAvailability.AVAILABLE
            HealthConnectClient.SDK_UNAVAILABLE -> HealthConnectAvailability.UNAVAILABLE
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED ->
                HealthConnectAvailability.UPDATE_REQUIRED
            else -> HealthConnectAvailability.UNKNOWN
        }

    fun isAvailable(): Boolean = availability() == HealthConnectAvailability.AVAILABLE
}
