package app.bodyforger.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.core.model.WorkoutSession
import app.bodyforger.mobile.R
import app.bodyforger.mobile.stats.TrainingStats
import app.bodyforger.mobile.ui.theme.NeonLime
import app.bodyforger.mobile.ui.theme.SurfaceBorder
import app.bodyforger.mobile.ui.theme.SurfaceDark
import app.bodyforger.mobile.ui.theme.SurfaceElevated
import app.bodyforger.mobile.ui.theme.TextMuted

@Composable
fun ActivityHeatmapCard(sessions: List<WorkoutSession>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, SurfaceBorder, RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // The title yields its width first: the count on the right is the shorter of
                // the two and the one nobody wants wrapped across the heading.
                Text(
                    text = stringResource(R.string.heatmap_title),
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    lineHeight = 13.sp,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(
                        R.string.profile_sessions_this_week,
                        TrainingStats.sessionsThisWeek(sessions, System.currentTimeMillis())
                    ),
                    color = NeonLime,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 13.sp,
                    textAlign = TextAlign.End
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            ActivityHeatmapGrid(sessions)

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(R.string.heatmap_less), color = TextMuted, fontSize = 9.sp)
                Spacer(modifier = Modifier.width(4.dp))
                // The legend shows every shade the grid can draw: three swatches for five steps
                // promised a gradation the cells did not have.
                (0..4).forEach { sessionCount ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(shadeFor(sessionCount))
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                }
                Spacer(modifier = Modifier.width(1.dp))
                Text(text = stringResource(R.string.heatmap_more), color = TextMuted, fontSize = 9.sp)
            }
        }
    }
}

@Composable
fun ActivityHeatmapGrid(sessions: List<WorkoutSession>) {
    val counts = remember(sessions) {
        TrainingStats.weeklySessionCounts(sessions, System.currentTimeMillis(), HEATMAP_WEEKS)
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        counts.chunked(WEEKS_PER_ROW).forEach { quarter ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                quarter.forEach { sessionCount ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(3.dp))
                            .background(shadeFor(sessionCount))
                    )
                }
                // A year is not a whole number of quarters; the last row keeps its cells the
                // size of every other row rather than stretching to fill the gap.
                repeat(WEEKS_PER_ROW - quarter.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Sessions in a week, as a shade.
 *
 * Four steps, because a training week tops out: nobody reads the difference between six sessions
 * and seven, and everybody reads the difference between one and three.
 */
private fun shadeFor(sessions: Int): Color = when (sessions) {
    0 -> SurfaceElevated
    1 -> NeonLime.copy(alpha = 0.3f)
    2 -> NeonLime.copy(alpha = 0.55f)
    3 -> NeonLime.copy(alpha = 0.78f)
    else -> NeonLime
}

/** A year, laid out as four rows of thirteen weeks. */
private const val HEATMAP_WEEKS = 52
private const val WEEKS_PER_ROW = 13
