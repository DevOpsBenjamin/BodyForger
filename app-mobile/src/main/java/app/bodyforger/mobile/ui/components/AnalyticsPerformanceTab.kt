package app.bodyforger.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import org.koin.androidx.compose.koinViewModel
import app.bodyforger.mobile.stats.TrainingStats
import app.bodyforger.mobile.library.LibraryViewModel
import app.bodyforger.mobile.R
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.mobile.ui.theme.AmberGold
import app.bodyforger.mobile.ui.theme.ElectricCyan
import app.bodyforger.mobile.ui.theme.NeonLime
import app.bodyforger.mobile.ui.theme.SurfaceBorder
import app.bodyforger.mobile.ui.theme.SurfaceDark
import app.bodyforger.mobile.ui.theme.SurfaceElevated
import app.bodyforger.mobile.ui.theme.TextMuted
import app.bodyforger.mobile.ui.theme.TextPrimary
import app.bodyforger.mobile.ui.theme.TextSecondary

@Composable
fun AnalyticsPerformanceTab(
    modifier: Modifier = Modifier,
    library: LibraryViewModel = koinViewModel()
) {
    val sessions by library.completedSessions.collectAsState()
    val weeklyTonnage = remember(sessions) {
        val now = System.currentTimeMillis()
        TrainingStats.tonnageBetween(sessions, now - ONE_WEEK_MS, now) / KILOGRAMS_PER_TONNE
    }
    val records = remember(sessions) { TrainingStats.personalRecords(sessions).take(RECORDS_SHOWN) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, NeonLime.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = null, tint = NeonLime, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = stringResource(R.string.stats_weekly_tonnage), color = TextMuted, fontSize = 11.sp)
                    Text(
                        text = stringResource(R.string.unit_tonnes, weeklyTonnage),
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

        }

        Spacer(modifier = Modifier.height(18.dp))

        // 2. Heatmap & Volume par Muscle
        Text(
            text = "VOLUME PAR GROUPE MUSCULAIRE (SÉRIES)",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceBorder, RoundedCornerShape(18.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                MuscleVolumeRow(muscle = "Pectoraux", completed = 14, target = 16, color = NeonLime)
                MuscleVolumeRow(muscle = "Grand Dorsal / Dos", completed = 16, target = 16, color = NeonLime)
                MuscleVolumeRow(muscle = "Épaules (Deltoïdes)", completed = 10, target = 12, color = AmberGold)
                MuscleVolumeRow(muscle = "Quadriceps & Ischios", completed = 12, target = 16, color = AmberGold)
                MuscleVolumeRow(muscle = "Bras (Biceps / Triceps)", completed = 12, target = 12, color = NeonLime, isLast = true)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 3. Records Personnels & 1RM
        Text(
            text = "ESTIMATION DES 1RM & RECORDS (PRS)",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceBorder, RoundedCornerShape(18.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (records.isEmpty()) {
                    Text(
                        text = stringResource(R.string.stats_records_empty),
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
                records.forEachIndexed { index, record ->
                    PRRow(
                        exercise = record.exerciseName,
                        oneRM = stringResource(R.string.unit_kilograms, record.estimatedOneRepMaxKg),
                        repRecord = stringResource(
                            R.string.stats_record_best_set,
                            record.bestWeightKg,
                            record.bestReps
                        ),
                        isLast = index == records.lastIndex
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun MuscleVolumeRow(muscle: String, completed: Int, target: Int, color: Color, isLast: Boolean = false) {
    val ratio = (completed.toFloat() / target).coerceIn(0f, 1f)
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = muscle, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(text = "$completed / $target séries", color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { ratio },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(2.5.dp)),
            color = color,
            trackColor = SurfaceElevated
        )
    }
    if (!isLast) {
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun PRRow(exercise: String, oneRM: String, repRecord: String, isLast: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = exercise, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(
                text = stringResource(R.string.stats_record_prefix, repRecord),
                color = TextMuted,
                fontSize = 11.sp
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = stringResource(R.string.stats_one_rep_max, oneRM),
                color = NeonLime,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
    if (!isLast) {
        HorizontalDivider(color = SurfaceElevated)
    }
}

private const val ONE_WEEK_MS = 7L * 24 * 60 * 60 * 1000
private const val KILOGRAMS_PER_TONNE = 1_000.0

/** How many records the card shows before it stops being a summary. */
private const val RECORDS_SHOWN = 3
