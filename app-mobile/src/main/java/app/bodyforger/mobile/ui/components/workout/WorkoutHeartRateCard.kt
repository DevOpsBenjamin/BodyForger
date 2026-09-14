package app.bodyforger.mobile.ui.components.workout

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.core.model.WorkoutHeartRateSample
import app.bodyforger.mobile.R
import app.bodyforger.mobile.ui.theme.CrimsonRed
import app.bodyforger.mobile.ui.theme.SurfaceDark
import app.bodyforger.mobile.ui.theme.TextMuted
import app.bodyforger.mobile.ui.theme.TextPrimary
import app.bodyforger.mobile.ui.theme.TextSecondary

/**
 * The heart rate curve of a session.
 *
 * A session with no heart rate says so and invites the athlete to connect a watch: an empty
 * chart would read as a flat zero, which is a measurement rather than an absence.
 */
@Composable
fun WorkoutHeartRateCard(samples: List<WorkoutHeartRateSample>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.workout_detail_heart_rate),
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        if (samples.isEmpty()) {
            Text(
                text = stringResource(R.string.workout_detail_heart_rate_empty),
                color = TextMuted,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            return@Column
        }

        val ordered = samples.sortedBy { it.timestampEpochMs }
        val lowest = ordered.minOf { it.bpm }
        val highest = ordered.maxOf { it.bpm }
        val average = ordered.sumOf { it.bpm } / ordered.size

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
        ) {
            listOf(
                R.string.workout_detail_bpm_min to lowest,
                R.string.workout_detail_bpm_avg to average,
                R.string.workout_detail_bpm_max to highest
            ).forEach { (label, value) ->
                Column {
                    Text(text = stringResource(label), color = TextMuted, fontSize = 10.sp)
                    Text(
                        text = stringResource(R.string.workout_detail_bpm, value),
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // A flat curve would divide by zero; one bar of range keeps it centred instead.
        val span = (highest - lowest).coerceAtLeast(1)
        val firstAt = ordered.first().timestampEpochMs
        val duration = (ordered.last().timestampEpochMs - firstAt).coerceAtLeast(1L)

        Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
            val path = Path()
            ordered.forEachIndexed { index, sample ->
                val x = size.width * (sample.timestampEpochMs - firstAt) / duration
                val y = size.height * (1f - (sample.bpm - lowest).toFloat() / span)
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, color = CrimsonRed, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f))
            ordered.lastOrNull()?.let {
                val y = size.height * (1f - (it.bpm - lowest).toFloat() / span)
                drawCircle(CrimsonRed, radius = 4f, center = Offset(size.width, y))
            }
        }
    }
}
