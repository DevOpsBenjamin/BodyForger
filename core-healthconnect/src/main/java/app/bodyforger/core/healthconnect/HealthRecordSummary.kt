package app.bodyforger.core.healthconnect

data class HealthSegmentDetail(
    val startTimeEpochMs: Long,
    val endTimeEpochMs: Long,
    val startTimeIso: String,
    val endTimeIso: String,
    val segmentType: Int,
    val segmentTypeName: String,
    val repetitions: Int
)

data class HealthSessionDetail(
    val id: String,
    val title: String?,
    val notes: String?,
    val startTimeEpochMs: Long,
    val endTimeEpochMs: Long,
    val startTimeIso: String,
    val endTimeIso: String,
    val durationMinutes: Long,
    val exerciseType: Int,
    val exerciseTypeName: String,
    val sourcePackage: String,
    val segments: List<HealthSegmentDetail>
)

data class HealthMetricDetail(
    val timeEpochMs: Long,
    val timeIso: String,
    val type: String,
    val value: Double,
    val unit: String,
    val sourcePackage: String
)

data class HealthHeartRateSample(
    val timeEpochMs: Long,
    val timeIso: String,
    val bpm: Long
)

data class HealthHeartRateSeries(
    val startTimeEpochMs: Long,
    val endTimeEpochMs: Long,
    val startTimeIso: String,
    val endTimeIso: String,
    val sourcePackage: String,
    val sampleCount: Int,
    val minBpm: Long,
    val maxBpm: Long,
    val avgBpm: Double,
    val deviceManufacturer: String? = null,
    val deviceModel: String? = null,
    val samples: List<HealthHeartRateSample> = emptyList()
)

data class HealthInspectionOverview(
    val isAvailable: Boolean,
    val monthsScanned: Int,
    val grantedPermissions: List<String>,
    val missingPermissions: List<String>,
    val hasHistoryPermission: Boolean,
    val totalSessions: Int,
    val totalHeartRateRecords: Int,
    val totalWeightRecords: Int,
    val totalBodyFatRecords: Int,
    val totalStepsRecords: Int,
    val oldestRecordIso: String?,
    val newestRecordIso: String?,
    val sourcePackages: List<String>
)

fun resolveExerciseSegmentName(segmentType: Int): String {
    return when (segmentType) {
        androidx.health.connect.client.records.ExerciseSegment.EXERCISE_SEGMENT_TYPE_BENCH_PRESS -> "BENCH_PRESS"
        androidx.health.connect.client.records.ExerciseSegment.EXERCISE_SEGMENT_TYPE_SQUAT -> "SQUAT"
        androidx.health.connect.client.records.ExerciseSegment.EXERCISE_SEGMENT_TYPE_DEADLIFT -> "DEADLIFT"
        androidx.health.connect.client.records.ExerciseSegment.EXERCISE_SEGMENT_TYPE_BARBELL_SHOULDER_PRESS -> "BARBELL_SHOULDER_PRESS"
        androidx.health.connect.client.records.ExerciseSegment.EXERCISE_SEGMENT_TYPE_DUMBBELL_ROW -> "DUMBBELL_ROW"
        androidx.health.connect.client.records.ExerciseSegment.EXERCISE_SEGMENT_TYPE_PULL_UP -> "PULL_UP"
        androidx.health.connect.client.records.ExerciseSegment.EXERCISE_SEGMENT_TYPE_REST -> "REST"
        androidx.health.connect.client.records.ExerciseSegment.EXERCISE_SEGMENT_TYPE_PAUSE -> "PAUSE"
        else -> "SEGMENT_$segmentType"
    }
}

fun resolveExerciseSessionTypeName(exerciseType: Int): String = when (exerciseType) {
    androidx.health.connect.client.records.ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING -> "STRENGTH_TRAINING"
    androidx.health.connect.client.records.ExerciseSessionRecord.EXERCISE_TYPE_WEIGHTLIFTING -> "WEIGHTLIFTING"
    androidx.health.connect.client.records.ExerciseSessionRecord.EXERCISE_TYPE_HIGH_INTENSITY_INTERVAL_TRAINING -> "HIIT"
    androidx.health.connect.client.records.ExerciseSessionRecord.EXERCISE_TYPE_CALISTHENICS -> "CALISTHENICS"
    androidx.health.connect.client.records.ExerciseSessionRecord.EXERCISE_TYPE_RUNNING -> "RUNNING"
    androidx.health.connect.client.records.ExerciseSessionRecord.EXERCISE_TYPE_WALKING -> "WALKING"
    androidx.health.connect.client.records.ExerciseSessionRecord.EXERCISE_TYPE_BIKING -> "BIKING"
    androidx.health.connect.client.records.ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_POOL,
    androidx.health.connect.client.records.ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_OPEN_WATER -> "SWIMMING"
    androidx.health.connect.client.records.ExerciseSessionRecord.EXERCISE_TYPE_YOGA -> "YOGA"
    androidx.health.connect.client.records.ExerciseSessionRecord.EXERCISE_TYPE_OTHER_WORKOUT -> "OTHER_WORKOUT"
    else -> "TYPE_$exerciseType"
}

fun androidx.health.connect.client.records.ExerciseSessionRecord.toSessionDetail(): HealthSessionDetail {
    val durationMin = java.time.Duration.between(startTime, endTime).toMinutes()
    val typeName = resolveExerciseSessionTypeName(exerciseType)

    val segmentDetails = segments.map { seg ->
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

    return HealthSessionDetail(
        id = metadata.id,
        title = title,
        notes = notes,
        startTimeEpochMs = startTime.toEpochMilli(),
        endTimeEpochMs = endTime.toEpochMilli(),
        startTimeIso = startTime.toString(),
        endTimeIso = endTime.toString(),
        durationMinutes = durationMin,
        exerciseType = exerciseType,
        exerciseTypeName = typeName,
        sourcePackage = metadata.dataOrigin.packageName,
        segments = segmentDetails
    )
}

fun androidx.health.connect.client.records.HeartRateRecord.toSeries(includeSamples: Boolean): HealthHeartRateSeries {
    val count = samples.size
    var minBpm = Long.MAX_VALUE
    var maxBpm = 0L
    var sumBpm = 0L
    samples.forEach { s ->
        val bpm = s.beatsPerMinute
        if (bpm < minBpm) minBpm = bpm
        if (bpm > maxBpm) maxBpm = bpm
        sumBpm += bpm
    }
    val rawSamples = if (includeSamples) {
        samples.map { HealthHeartRateSample(it.time.toEpochMilli(), it.time.toString(), it.beatsPerMinute) }
    } else emptyList()

    return HealthHeartRateSeries(
        startTimeEpochMs = startTime.toEpochMilli(),
        endTimeEpochMs = endTime.toEpochMilli(),
        startTimeIso = startTime.toString(),
        endTimeIso = endTime.toString(),
        sourcePackage = metadata.dataOrigin.packageName,
        sampleCount = count,
        minBpm = if (count > 0) minBpm else 0L,
        maxBpm = maxBpm,
        avgBpm = if (count > 0) sumBpm.toDouble() / count else 0.0,
        deviceManufacturer = metadata.device?.manufacturer,
        deviceModel = metadata.device?.model,
        samples = rawSamples
    )
}


