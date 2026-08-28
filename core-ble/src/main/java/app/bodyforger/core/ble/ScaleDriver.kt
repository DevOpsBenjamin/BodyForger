package app.bodyforger.core.ble

import app.bodyforger.core.model.BodyLog

interface ScaleDriver {
    val name: String
    suspend fun connectAndReadTelemetry(deviceAddress: String): Result<BodyLog>
}
