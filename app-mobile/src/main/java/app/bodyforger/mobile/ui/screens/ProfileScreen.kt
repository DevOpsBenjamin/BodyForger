package app.bodyforger.mobile.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.core.model.WorkoutSession
import app.bodyforger.mobile.R
import app.bodyforger.mobile.library.LibraryViewModel
import app.bodyforger.mobile.stats.TrainingStats
import app.bodyforger.mobile.ui.components.ActivityHeatmapCard
import app.bodyforger.mobile.ui.components.HistoryWorkoutCard
import app.bodyforger.mobile.ui.components.ProfileTotalStatCard
import app.bodyforger.mobile.ui.theme.AmberGold
import app.bodyforger.mobile.ui.theme.ElectricCyan
import app.bodyforger.mobile.ui.theme.NeonLime
import app.bodyforger.mobile.ui.theme.Obsidian
import app.bodyforger.mobile.ui.theme.SurfaceBorder
import app.bodyforger.mobile.ui.theme.SurfaceElevated
import app.bodyforger.mobile.ui.theme.TextMuted
import app.bodyforger.mobile.ui.theme.TextPrimary
import app.bodyforger.mobile.ui.theme.TextSecondary
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import org.koin.androidx.compose.koinViewModel

data class HistoryWorkoutItem(
    val id: String,
    val title: String,
    val dateDisplay: String,
    val durationDisplay: String,
    val avgBpm: Int,
    val totalTonnageKg: Double,
    val exerciseSummary: String,
    val personalRecordHighlight: String? = null
)

@Composable
fun ProfileScreen(
    onOpenSettings: () -> Unit = {},
    library: LibraryViewModel = koinViewModel()
) {
    val scrollState = rememberScrollState()
    val sessions by library.completedSessions.collectAsState()

    val workoutHistory = remember(sessions) { sessions.map { it.toHistoryItem() } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        // --- 1. EN-TÊTE PROFIL ATHLÈTE ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(SurfaceElevated)
                        .border(1.5.dp, NeonLime, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar",
                        tint = NeonLime,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "BENJAMIN D.",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = "🔥 14 semaines consécutives",
                            color = AmberGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(SurfaceElevated)
                    .border(1.dp, SurfaceBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Paramètres",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // --- 2. CHIFFRES CLÉS ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ProfileTotalStatCard(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.profile_stat_sessions),
                value = sessions.size.toString(),
                color = NeonLime
            )
            ProfileTotalStatCard(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.profile_stat_tonnage),
                value = stringResource(R.string.unit_tonnes, TrainingStats.totalTonnes(sessions)),
                color = ElectricCyan
            )
            ProfileTotalStatCard(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.profile_stat_hours),
                value = stringResource(R.string.unit_hours, TrainingStats.totalHours(sessions)),
                color = AmberGold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- 3. HEATMAP D'ACTIVITÉ ---
        ActivityHeatmapCard(sessions = sessions)

        Spacer(modifier = Modifier.height(24.dp))

        // --- 4. HISTORIQUE DES SÉANCES ---
        Text(
            text = "HISTORIQUE DES ACTIVITÉS",
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (workoutHistory.isEmpty()) {
            Text(
                text = stringResource(R.string.profile_history_empty),
                color = TextMuted,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        } else {
            workoutHistory.forEach { item -> HistoryWorkoutCard(item = item) }
        }
    }
}

/**
 * A completed session as the history card shows it.
 *
 * Nothing is filled in that the session does not carry: a workout with no heart rate shows
 * none, rather than a plausible number.
 */
private fun WorkoutSession.toHistoryItem(): HistoryWorkoutItem {
    val minutes = TrainingStats.durationMinutes(this)
    val startedAt = Instant.ofEpochMilli(startedAtEpochMs).atZone(ZoneId.systemDefault())
    return HistoryWorkoutItem(
        id = id,
        title = title,
        dateDisplay = startedAt.format(HISTORY_DATE_FORMAT),
        durationDisplay = minutes?.let { "$it min" }.orEmpty(),
        avgBpm = averageHeartRateBpm ?: 0,
        totalTonnageKg = TrainingStats.totalTonnageKg(listOf(this)),
        exerciseSummary = TrainingStats.exerciseNames(this).joinToString(", "),
        personalRecordHighlight = null
    )
}

private val HISTORY_DATE_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEEE d MMMM • HH:mm")
