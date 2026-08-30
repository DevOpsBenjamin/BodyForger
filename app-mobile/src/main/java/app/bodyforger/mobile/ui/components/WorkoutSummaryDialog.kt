package app.bodyforger.mobile.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.core.model.WorkoutSession
import app.bodyforger.mobile.R
import app.bodyforger.core.model.WorkoutActivityCategory
import app.bodyforger.mobile.ui.text.label
import app.bodyforger.mobile.ui.theme.AmberGold
import app.bodyforger.mobile.ui.theme.ElectricCyan
import app.bodyforger.mobile.ui.theme.NeonLime
import app.bodyforger.mobile.ui.theme.SurfaceBorder
import app.bodyforger.mobile.ui.theme.SurfaceDark
import app.bodyforger.mobile.ui.theme.SurfaceElevated
import app.bodyforger.mobile.ui.theme.TextMuted
import app.bodyforger.mobile.ui.theme.TextPrimary
import app.bodyforger.mobile.ui.theme.TextSecondary

data class ActivitySegmentSummary(
    val category: WorkoutActivityCategory,
    val setCount: Int
)

@Composable
fun WorkoutSummaryDialog(
    session: WorkoutSession,
    onConfirmSave: () -> Unit,
    onCancel: () -> Unit
) {
    val scrollState = rememberScrollState()

    val durationMs = if (session.endedAtEpochMs != null) {
        session.endedAtEpochMs!! - session.startedAtEpochMs
    } else {
        System.currentTimeMillis() - session.startedAtEpochMs
    }

    val totalSeconds = (durationMs / 1000).toInt().coerceAtLeast(0)
    val mins = totalSeconds / 60
    val secs = totalSeconds % 60
    val durationText = if (mins >= 60) {
        val hrs = mins / 60
        val remMins = mins % 60
        "${hrs}h ${remMins}m"
    } else {
        "${mins}m ${secs}s"
    }

    val completedSets = session.sets.filter { it.isCompleted }
    val totalVolumeKg = completedSets.sumOf { it.weightKg * it.reps }

    val muscleVolumeMap = completedSets
        .groupBy { it.primaryMuscle }
        .mapValues { it.value.size }
        .toList()
        .sortedByDescending { it.second }

    // Auto-Segmentation Google Health Connect
    val segments = remember(session.sets) {
        val list = mutableListOf<ActivitySegmentSummary>()
        var currentCat = session.sets.firstOrNull()?.activityCategory
        var count = 0
        session.sets.forEach { set ->
            if (set.activityCategory == currentCat) {
                count++
            } else {
                if (currentCat != null) {
                    list.add(ActivitySegmentSummary(currentCat, count))
                }
                currentCat = set.activityCategory
                count = 1
            }
        }
        if (currentCat != null) {
            list.add(ActivitySegmentSummary(currentCat, count))
        }
        list
    }

    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = SurfaceDark,
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = NeonLime,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.workout_summary_title),
                        color = NeonLime,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Text(
                    text = session.title,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f).border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = stringResource(R.string.workout_summary_duration), color = TextMuted, fontSize = 9.sp)
                            Text(text = durationText, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Tonnage Total
                    Card(
                        modifier = Modifier.weight(1f).border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = stringResource(R.string.workout_summary_volume), color = TextMuted, fontSize = 9.sp)
                            val tonnageDisplay = if (totalVolumeKg >= 1000) {
                                "${(totalVolumeKg / 100.0).toInt() / 10.0} T"
                            } else {
                                "${totalVolumeKg.toInt()} kg"
                            }
                            Text(text = tonnageDisplay, color = ElectricCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f).border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = NeonLime, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = stringResource(R.string.workout_summary_sets_completed), color = TextMuted, fontSize = 9.sp)
                            Text(text = "${completedSets.size}/${session.sets.size}", color = NeonLime, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Volume Musculaire
                Text(
                    text = stringResource(R.string.workout_summary_muscle_volume),
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        muscleVolumeMap.forEach { (muscle, setCount) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = muscle.label(), color = TextPrimary, fontSize = 12.sp)
                                Text(text = "$setCount séries", color = NeonLime, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Auto-Segmentation Health Connect
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Sync, contentDescription = null, tint = AmberGold, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.workout_summary_segments_title),
                        color = AmberGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        segments.forEachIndexed { idx, segmentItem ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Segment ${idx + 1} : ${segmentItem.category.label()}", color = TextPrimary, fontSize = 12.sp)
                                Text(text = "${segmentItem.setCount} séries", color = AmberGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(
                    text = stringResource(R.string.action_cancel),
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmSave,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonLime,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.workout_summary_save_btn),
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
            }
        }
    )
}
