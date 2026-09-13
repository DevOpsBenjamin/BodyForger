package app.bodyforger.core.healthconnect

import android.content.Context
import androidx.health.connect.client.HealthConnectClient

class HealthConnectManager(private val context: Context) {
    fun isAvailable(): Boolean {
        return HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
    }
}
