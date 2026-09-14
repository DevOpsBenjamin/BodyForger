package app.bodyforger.core.healthconnect

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.ExerciseSegment
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

class HealthConnectReader(private val client: HealthConnectClient) {

    suspend fun inspectOverview(monthsBack: Int = DEFAULT_MONTHS_BACK): HealthInspectionOverview {
        val granted = HealthConnectPermissions.getGrantedPermissions(client)
        val missing = HealthConnectPermissions.CORE_READ_PERMISSIONS.filterNot { granted.contains(it) }
        val hasHistory = HealthConnectPermissions.hasHistoryPermission(granted)

        val endTime = Instant.now()
        val startTime = endTime.minus(monthsBack.toLong() * DAYS_PER_MONTH, ChronoUnit.DAYS)
        val timeFilter = TimeRangeFilter.between(startTime, endTime)

        var totalSessions = 0
        var totalHeartRates = 0
        var totalWeights = 0
        var totalBodyFats = 0
        var totalSteps = 0

        val sourcePackages = mutableSetOf<String>()
        var oldestInstant: Instant? = null
        var newestInstant: Instant? = null

        fun trackBounds(instant: Instant) {
            if (oldestInstant == null || instant.isBefore(oldestInstant)) oldestInstant = instant
            if (newestInstant == null || instant.isAfter(newestInstant)) newestInstant = instant
        }

        if (HealthConnectPermissions.hasExercisePermission(granted)) {
            val sessions = readAllPages(ExerciseSessionRecord::class, timeFilter)
            totalSessions = sessions.size
            sessions.forEach {
                trackBounds(it.startTime)
                sourcePackages.add(it.metadata.dataOrigin.packageName)
            }
        }

        if (HealthConnectPermissions.hasHeartRatePermission(granted)) {
            val hrs = readAllPages(HeartRateRecord::class, timeFilter)
            totalHeartRates = hrs.size
            hrs.forEach {
                trackBounds(it.startTime)
                sourcePackages.add(it.metadata.dataOrigin.packageName)
            }
        }

        if (HealthConnectPermissions.hasWeightPermission(granted)) {
            val weights = readAllPages(WeightRecord::class, timeFilter)
            totalWeights = weights.size
            weights.forEach {
                trackBounds(it.time)
                sourcePackages.add(it.metadata.dataOrigin.packageName)
            }
        }

        if (HealthConnectPermissions.hasBodyFatPermission(granted)) {
            val fats = readAllPages(BodyFatRecord::class, timeFilter)
            totalBodyFats = fats.size
            fats.forEach {
                trackBounds(it.time)
                sourcePackages.add(it.metadata.dataOrigin.packageName)
            }
        }

        if (granted.contains("android.permission.health.READ_STEPS")) {
            val steps = readAllPages(StepsRecord::class, timeFilter)
            totalSteps = steps.size
            steps.forEach {
                trackBounds(it.startTime)
                sourcePackages.add(it.metadata.dataOrigin.packageName)
            }
        }

        return HealthInspectionOverview(
            isAvailable = true,
            monthsScanned = monthsBack,
            grantedPermissions = granted.toList(),
            missingPermissions = missing,
            hasHistoryPermission = hasHistory,
            totalSessions = totalSessions,
            totalHeartRateRecords = totalHeartRates,
            totalWeightRecords = totalWeights,
            totalBodyFatRecords = totalBodyFats,
            totalStepsRecords = totalSteps,
            oldestRecordIso = oldestInstant?.toString(),
            newestRecordIso = newestInstant?.toString(),
            sourcePackages = sourcePackages.toList().sorted()
        )
    }

    suspend fun readExerciseSessions(
        startTime: Instant,
        endTime: Instant
    ): List<HealthSessionDetail> {
        val records = readAllPages(
            recordType = ExerciseSessionRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        return records.map { it.toSessionDetail() }
    }

    suspend fun readWeights(
        startTime: Instant,
        endTime: Instant
    ): List<HealthMetricDetail> {
        val records = readAllPages(
            recordType = WeightRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        return records.map {
            HealthMetricDetail(
                timeEpochMs = it.time.toEpochMilli(),
                timeIso = it.time.toString(),
                type = "weight",
                value = it.weight.inKilograms,
                unit = "kg",
                sourcePackage = it.metadata.dataOrigin.packageName
            )
        }
    }

    suspend fun readBodyFats(
        startTime: Instant,
        endTime: Instant
    ): List<HealthMetricDetail> {
        val records = readAllPages(
            recordType = BodyFatRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        return records.map {
            HealthMetricDetail(
                timeEpochMs = it.time.toEpochMilli(),
                timeIso = it.time.toString(),
                type = "body_fat",
                value = it.percentage.value,
                unit = "%",
                sourcePackage = it.metadata.dataOrigin.packageName
            )
        }
    }

    suspend fun readHeartRates(
        startTime: Instant,
        endTime: Instant,
        limit: Int = DEFAULT_HEART_RATE_LIMIT,
        includeSamples: Boolean = false
    ): List<HealthHeartRateSeries> {
        val records = readAllPages(
            recordType = HeartRateRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime),
            maxRecords = limit
        )
        return records.map { it.toSeries(includeSamples) }
    }

    private suspend fun <T : androidx.health.connect.client.records.Record> readAllPages(
        recordType: kotlin.reflect.KClass<T>,
        timeRangeFilter: TimeRangeFilter,
        maxRecords: Int = MAX_FETCH_LIMIT
    ): List<T> {
        val allRecords = mutableListOf<T>()
        var nextToken: String? = null

        do {
            val remaining = maxRecords - allRecords.size
            if (remaining <= 0) break
            val fetchSize = minOf(PAGE_SIZE, remaining)
            val request = ReadRecordsRequest(
                recordType = recordType,
                timeRangeFilter = timeRangeFilter,
                pageSize = fetchSize,
                pageToken = nextToken
            )
            val response = client.readRecords(request)
            allRecords.addAll(response.records)
            nextToken = response.pageToken
        } while (nextToken != null && allRecords.size < maxRecords)

        return allRecords
    }

    companion object {
        const val DEFAULT_MONTHS_BACK = 12
        const val DAYS_PER_MONTH = 30L
        const val PAGE_SIZE = 1000
        const val MAX_FETCH_LIMIT = 5000
        const val DEFAULT_HEART_RATE_LIMIT = 100
    }
}
