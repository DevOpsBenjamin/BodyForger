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
import app.bodyforger.mobile.R
import androidx.compose.ui.res.stringResource
import app.bodyforger.mobile.stats.TrainingStats
import app.bodyforger.core.model.WorkoutSession
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                Text(
                    text = "RÉGULARITÉ & ENTRAÎNEMENT",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = stringResource(
                        R.string.profile_sessions_this_week,
                        TrainingStats.sessionsThisWeek(sessions, System.currentTimeMillis())
                    ),
                    color = NeonLime,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
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
                Text(text = "Moins", color = TextMuted, fontSize = 9.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(SurfaceElevated))
                Spacer(modifier = Modifier.width(3.dp))
                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(NeonLime.copy(alpha = 0.4f)))
                Spacer(modifier = Modifier.width(3.dp))
                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(NeonLime))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Plus", color = TextMuted, fontSize = 9.sp)
            }
        }
    }
}

@Composable
fun ActivityHeatmapGrid(sessions: List<WorkoutSession>) {
    val activeDays = remember(sessions) {
        TrainingStats.activeDayOffsets(sessions, System.currentTimeMillis(), HEATMAP_DAYS)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        for (col in 0 until HEATMAP_COLUMNS) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (row in 0 until HEATMAP_ROWS) {
                    val dayIndex = col * HEATMAP_ROWS + row
                    val boxColor = if (activeDays.contains(dayIndex)) NeonLime else SurfaceElevated

                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(boxColor)
                    )
                }
            }
        }
    }
}

/** The grid covers the last ten weeks, read column by column. */
private const val HEATMAP_COLUMNS = 14
private const val HEATMAP_ROWS = 5
private const val HEATMAP_DAYS = HEATMAP_COLUMNS * HEATMAP_ROWS
