package app.bodyforger.mobile.ui.components.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.core.model.WorkoutSet
import app.bodyforger.mobile.R
import app.bodyforger.mobile.ui.theme.AmberGold
import app.bodyforger.mobile.ui.theme.ElectricCyan
import app.bodyforger.mobile.ui.theme.NeonLime
import app.bodyforger.mobile.ui.theme.SurfaceBorder
import app.bodyforger.mobile.ui.theme.SurfaceDark
import app.bodyforger.mobile.ui.theme.TextMuted
import app.bodyforger.mobile.ui.theme.TextPrimary
import app.bodyforger.mobile.ui.theme.TextSecondary
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val SECONDS_PER_DP = 2

/**
 * The session as it unfolded, read top to bottom.
 *
 * Effort and rest are drawn to the same scale, so the shape of the session is legible at a
 * glance: a heavy triple reads as a short block under a long gap, an accessory as the reverse.
 * A set that never recorded its timing keeps its row without a bar, rather than being dropped
 * from the chronology or given a plausible length.
 *
 * `actualRestSeconds` is the rest that *preceded* a set, so the gap drawn under one set reads
 * the value of the next: the rest after a heavy press belongs to the set that follows it.
 */
@Composable
fun WorkoutTimelineCard(sets: List<WorkoutSet>, modifier: Modifier = Modifier) {
    val ordered = sets.sortedBy { it.startedAtEpochMs ?: it.completedAtEpochMs ?: Long.MAX_VALUE }
    val zone = ZoneId.systemDefault()
    val clock = DateTimeFormatter.ofPattern("HH:mm:ss")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.workout_detail_timeline),
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        ordered.forEachIndexed { index, set ->
            val started = set.startedAtEpochMs
            val ended = set.completedAtEpochMs
            val effortSeconds = if (started != null && ended != null && ended > started) {
                ((ended - started) / 1000).toInt()
            } else {
                null
            }

            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = started?.let { Instant.ofEpochMilli(it).atZone(zone).format(clock) }
                        ?: stringResource(R.string.workout_detail_no_time),
                    color = if (started != null) TextMuted else SurfaceBorder,
                    fontSize = 11.sp,
                    modifier = Modifier.width(62.dp).padding(top = 2.dp)
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(18.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .height(barHeight(effortSeconds))
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (set.isCompleted) NeonLime else SurfaceBorder)
                    )
                    val rest = ordered.getOrNull(index + 1)?.actualRestSeconds
                    if (index < ordered.lastIndex && rest != null) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(barHeight(rest))
                                .background(AmberGold.copy(alpha = 0.45f))
                        )
                    } else if (index < ordered.lastIndex) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f).padding(bottom = 6.dp)) {
                    Text(
                        text = set.exerciseName,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(text = describeSet(set), color = TextSecondary, fontSize = 12.sp)
                        effortSeconds?.let {
                            Text(
                                text = stringResource(R.string.workout_detail_seconds, it),
                                color = ElectricCyan,
                                fontSize = 11.sp
                            )
                        }
                        set.rpe?.let {
                            Text(
                                text = stringResource(R.string.workout_detail_rpe, it),
                                color = AmberGold,
                                fontSize = 11.sp
                            )
                        }
                    }
                    ordered.getOrNull(index + 1)?.actualRestSeconds?.let {
                        Text(
                            text = stringResource(R.string.workout_detail_rest, it),
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

/** Seconds to height, floored so a very short set still leaves a mark. */
private fun barHeight(seconds: Int?) = ((seconds ?: 0) / SECONDS_PER_DP).coerceIn(6, 120).dp

private fun describeSet(set: WorkoutSet): String = when {
    set.reps > 0 && set.weightKg > 0 -> "${trim(set.weightKg)} kg × ${set.reps}"
    set.reps > 0 -> "× ${set.reps}"
    set.weightKg > 0 -> "${trim(set.weightKg)} kg"
    else -> "—"
}

private fun trim(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else String.format("%.1f", value)
