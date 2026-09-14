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

        return records.map { record ->
            val durationMin = Duration.between(record.startTime, record.endTime).toMinutes()
            val typeName = ExerciseSessionRecord.EXERCISE_TYPE_INT_TO_STRING_MAP[record.exerciseType]
                ?: "TYPE_${record.exerciseType}"

            val segmentDetails = record.segments.map { seg ->
                HealthSegmentDetail(
                    startTimeEpochMs = seg.startTime.toEpochMilli(),
                    endTimeEpochMs = seg.endTime.toEpochMilli(),
                    startTimeIso = seg.startTime.toString(),
                    endTimeIso = seg.endTime.toString(),
                    segmentType = seg.segmentType,
                    segmentTypeName = resolveExerciseSegmentName(seg.segmentType),
                    repetitions = seg.repetitions
                )
            }

            HealthSessionDetail(
                id = record.metadata.id,
                title = record.title,
                notes = record.notes,
                startTimeEpochMs = record.startTime.toEpochMilli(),
                endTimeEpochMs = record.endTime.toEpochMilli(),
                startTimeIso = record.startTime.toString(),
                endTimeIso = record.endTime.toString(),
                durationMinutes = durationMin,
                exerciseType = record.exerciseType,
                exerciseTypeName = typeName,
                sourcePackage = record.metadata.dataOrigin.packageName,
                segments = segmentDetails
            )
        }
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
        endTime: Instant
    ): List<HealthHeartRateSeries> {
        val records = readAllPages(
            recordType = HeartRateRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        return records.map { hr ->
            HealthHeartRateSeries(
                startTimeEpochMs = hr.startTime.toEpochMilli(),
                endTimeEpochMs = hr.endTime.toEpochMilli(),
                startTimeIso = hr.startTime.toString(),
                endTimeIso = hr.endTime.toString(),
                sourcePackage = hr.metadata.dataOrigin.packageName,
                samples = hr.samples.map { sample ->
                    HealthHeartRateSample(
                        timeEpochMs = sample.time.toEpochMilli(),
                        timeIso = sample.time.toString(),
                        bpm = sample.beatsPerMinute
                    )
                }
            )
        }
    }

    private suspend fun <T : androidx.health.connect.client.records.Record> readAllPages(
        recordType: kotlin.reflect.KClass<T>,
        timeRangeFilter: TimeRangeFilter
    ): List<T> {
        val allRecords = mutableListOf<T>()
        var nextToken: String? = null

        do {
            val request = ReadRecordsRequest(
                recordType = recordType,
                timeRangeFilter = timeRangeFilter,
                pageSize = PAGE_SIZE,
                pageToken = nextToken
            )
            val response = client.readRecords(request)
            allRecords.addAll(response.records)
            nextToken = response.pageToken
        } while (nextToken != null)

        return allRecords
    }

    companion object {
        const val DEFAULT_MONTHS_BACK = 12
        const val DAYS_PER_MONTH = 30L
        const val PAGE_SIZE = 1000
    }
}
