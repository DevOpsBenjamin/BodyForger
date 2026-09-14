package app.bodyforger.core.healthconnect

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.LeanBodyMassRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord

object HealthConnectPermissions {

    const val READ_HEALTH_DATA_HISTORY = "android.permission.health.READ_HEALTH_DATA_HISTORY"
    const val READ_HEALTH_DATA_IN_BACKGROUND = "android.permission.health.READ_HEALTH_DATA_IN_BACKGROUND"

    val CORE_READ_PERMISSIONS: Set<String> = setOf(
        READ_HEALTH_DATA_HISTORY,
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(WeightRecord::class),
        HealthPermission.getReadPermission(BodyFatRecord::class),
        HealthPermission.getReadPermission(LeanBodyMassRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class)
    )

    suspend fun getGrantedPermissions(client: HealthConnectClient): Set<String> {
        return client.permissionController.getGrantedPermissions()
    }

    fun hasHistoryPermission(granted: Set<String>): Boolean =
        granted.contains(READ_HEALTH_DATA_HISTORY)

    fun hasExercisePermission(granted: Set<String>): Boolean =
        granted.contains(HealthPermission.getReadPermission(ExerciseSessionRecord::class))

    fun hasWeightPermission(granted: Set<String>): Boolean =
        granted.contains(HealthPermission.getReadPermission(WeightRecord::class))

    fun hasBodyFatPermission(granted: Set<String>): Boolean =
        granted.contains(HealthPermission.getReadPermission(BodyFatRecord::class))

    fun hasHeartRatePermission(granted: Set<String>): Boolean =
        granted.contains(HealthPermission.getReadPermission(HeartRateRecord::class))
}
