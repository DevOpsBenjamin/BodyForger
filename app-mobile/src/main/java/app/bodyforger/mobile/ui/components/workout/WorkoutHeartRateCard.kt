package app.bodyforger.mobile.ui.components.workout

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.core.model.WorkoutHeartRateSample
import app.bodyforger.core.model.WorkoutSet
import app.bodyforger.mobile.R
import app.bodyforger.mobile.ui.theme.CrimsonRed
import app.bodyforger.mobile.ui.theme.SurfaceDark
import app.bodyforger.mobile.ui.theme.TextMuted
import app.bodyforger.mobile.ui.theme.TextPrimary
import app.bodyforger.mobile.ui.theme.TextSecondary

/**
 * Colours for the exercise bands, kept apart from the theme's accents so a band is never
 * mistaken for the curve itself. They repeat past the last one: a session with more exercises
 * than colours still reads, because the legend names every band.
 */
private val BandColours = listOf(
    Color(0xFF4C8DFF), Color(0xFF00C39A), Color(0xFFB57BFF), Color(0xFFFFA24C),
    Color(0xFF3FC1C9), Color(0xFFE86A92), Color(0xFF9BC53D), Color(0xFFC9A227)
)

private const val BAND_ALPHA = 0.22f

/** One exercise's stretch of the session, as the graph shades it. */
private data class ExerciseBand(
    val exerciseName: String,
    val startEpochMs: Long,
    val endEpochMs: Long,
    val colour: Color
)

/**
 * The heart rate curve of a session, banded by exercise.
 *
 * The bands are what make the curve readable: a climb means little on its own, and a lot once
 * it sits under the name of the press that caused it. Sets with no recorded timing produce no
 * band rather than a guessed one, so an imported session that never had timing shows a bare
 * curve instead of an invented structure.
 *
 * A session with no heart rate says so and invites pairing a watch: an empty chart would read
 * as a flat zero, which is a measurement rather than an absence.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkoutHeartRateCard(
    samples: List<WorkoutHeartRateSample>,
    sets: List<WorkoutSet> = emptyList(),
    modifier: Modifier = Modifier
) {
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

        val ordered = remember(samples) { samples.sortedBy { it.timestampEpochMs } }
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

        // A flat curve would divide by zero; one beat of range keeps it centred instead.
        val span = (highest - lowest).coerceAtLeast(1)
        val firstAt = ordered.first().timestampEpochMs
        val duration = (ordered.last().timestampEpochMs - firstAt).coerceAtLeast(1L)
        val bands = remember(sets) { bandsOf(sets) }

        Canvas(modifier = Modifier.fillMaxWidth().height(140.dp)) {
            bands.forEach { band ->
                val left = size.width * (band.startEpochMs - firstAt).toFloat() / duration
                val right = size.width * (band.endEpochMs - firstAt).toFloat() / duration
                val clampedLeft = left.coerceIn(0f, size.width)
                val clampedRight = right.coerceIn(0f, size.width)
                if (clampedRight > clampedLeft) {
                    drawRect(
                        color = band.colour.copy(alpha = BAND_ALPHA),
                        topLeft = Offset(clampedLeft, 0f),
                        size = Size(clampedRight - clampedLeft, size.height)
                    )
                }
            }

            val path = Path()
            ordered.forEachIndexed { index, sample ->
                val x = size.width * (sample.timestampEpochMs - firstAt).toFloat() / duration
                val y = size.height * (1f - (sample.bpm - lowest).toFloat() / span)
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, color = CrimsonRed, style = Stroke(width = 2.5f))
            ordered.lastOrNull()?.let {
                val y = size.height * (1f - (it.bpm - lowest).toFloat() / span)
                drawCircle(CrimsonRed, radius = 4f, center = Offset(size.width, y))
            }
        }

        if (bands.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 12.dp)
            ) {
                bands.distinctBy { it.exerciseName }.forEach { band ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(band.colour)
                        )
                        Text(
                            text = band.exerciseName,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 5.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Groups consecutive sets of the same exercise into one band.
 *
 * Consecutive rather than by name: an exercise the athlete came back to later in the session is
 * two stretches of time, and shading the gap between them would claim they were one.
 */
private fun bandsOf(sets: List<WorkoutSet>): List<ExerciseBand> {
    val timed = sets
        .filter { it.startedAtEpochMs != null && it.completedAtEpochMs != null }
        .sortedBy { it.startedAtEpochMs }
    if (timed.isEmpty()) return emptyList()

    val colours = mutableMapOf<String, Color>()
    val bands = mutableListOf<ExerciseBand>()
    var current = timed.first()
    var start = current.startedAtEpochMs!!
    var end = current.completedAtEpochMs!!

    timed.drop(1).forEach { set ->
        if (set.exerciseId == current.exerciseId) {
            end = set.completedAtEpochMs!!
        } else {
            bands += ExerciseBand(current.exerciseName, start, end, colourFor(current.exerciseId, current.exerciseName, colours))
            current = set
            start = set.startedAtEpochMs!!
            end = set.completedAtEpochMs!!
        }
    }
    bands += ExerciseBand(current.exerciseName, start, end, colourFor(current.exerciseId, current.exerciseName, colours))
    return bands
}

/** One colour per exercise, stable across its stretches within the session. */
private fun colourFor(exerciseId: String, name: String, taken: MutableMap<String, Color>): Color =
    taken.getOrPut(exerciseId.ifEmpty { name }) { BandColours[taken.size % BandColours.size] }
