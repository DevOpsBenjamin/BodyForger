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
    val samples: List<HealthHeartRateSample>
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

