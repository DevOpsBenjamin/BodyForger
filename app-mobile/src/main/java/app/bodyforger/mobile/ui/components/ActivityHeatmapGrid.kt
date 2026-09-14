package app.bodyforger.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(SurfaceElevated))
                Spacer(modifier = Modifier.width(3.dp))
                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(NeonLime.copy(alpha = 0.4f)))
                Spacer(modifier = Modifier.width(3.dp))
                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(NeonLime))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = stringResource(R.string.heatmap_more), color = TextMuted, fontSize = 9.sp)
            }
        }
    }
}

@Composable
fun ActivityHeatmapGrid(sessions: List<WorkoutSession>) {
    val weeks = remember(sessions) {
        TrainingStats.activityWeeks(sessions, System.currentTimeMillis(), HEATMAP_WEEKS)
    }
    val initials = stringResource(R.string.heatmap_weekday_initials).split(",")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // The weekday initials are what turn the grid into a calendar: without them a gap is
        // just a gap, and with them it is a weekend.
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            initials.take(TrainingStats.DAYS_IN_A_WEEK).forEach { initial ->
                Box(modifier = Modifier.size(width = 10.dp, height = 14.dp)) {
                    Text(
                        text = initial,
                        color = TextMuted,
                        fontSize = 8.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        weeks.forEach { week ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                week.forEach { trained ->
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (trained) NeonLime else SurfaceElevated)
                    )
                }
            }
        }
    }
}

/** Fourteen weeks, one column each, Monday at the top — the shape GitHub made legible. */
private const val HEATMAP_WEEKS = 14
