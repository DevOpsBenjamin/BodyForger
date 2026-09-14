package app.bodyforger.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.core.model.WeightUnit
import app.bodyforger.core.model.WorkoutSession
import app.bodyforger.mobile.R
import app.bodyforger.mobile.ui.components.workout.WorkoutHeartRateCard
import app.bodyforger.mobile.ui.components.workout.WorkoutTimelineCard
import app.bodyforger.mobile.ui.theme.NeonLime
import app.bodyforger.mobile.ui.theme.Obsidian
import app.bodyforger.mobile.ui.theme.SurfaceDark
import app.bodyforger.mobile.ui.theme.TextMuted
import app.bodyforger.mobile.ui.theme.TextPrimary
import app.bodyforger.mobile.ui.theme.TextSecondary
import app.bodyforger.mobile.workout.WorkoutDetailViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import org.koin.androidx.compose.koinViewModel

/**
 * One past session, read back: what was lifted, in what order, and how the effort ran.
 *
 * Deliberately plain for now — the history had nowhere to lead, and a session's sets, notes
 * and heart rate had no screen at all.
 */
@Composable
fun WorkoutDetailScreen(
    sessionId: String,
    onBack: () -> Unit,
    unit: WeightUnit = WeightUnit.KG,
    viewModel: WorkoutDetailViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(sessionId) { viewModel.load(sessionId) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, bottom = 32.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    tint = TextPrimary
                )
            }
        }

        val session = state.session
        when {
            state.isLoading -> Text(
                text = stringResource(R.string.workout_detail_loading),
                color = TextMuted,
                fontSize = 13.sp
            )

            session == null -> Text(
                text = stringResource(R.string.workout_detail_missing),
                color = TextMuted,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )

            else -> {
                SessionHeader(session, unit)
                Spacer(modifier = Modifier.height(16.dp))
                WorkoutHeartRateCard(samples = state.heartRates)
                Spacer(modifier = Modifier.height(16.dp))
                WorkoutTimelineCard(sets = session.sets)

                if (session.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceDark)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.workout_detail_notes),
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = session.notes,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionHeader(session: WorkoutSession, unit: WeightUnit) {
    val zone = ZoneId.systemDefault()
    val dayFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)
    val started = Instant.ofEpochMilli(session.startedAtEpochMs).atZone(zone)
    val minutes = session.endedAtEpochMs
        ?.let { ((it - session.startedAtEpochMs) / 60_000L).toInt() }

    Text(text = session.title, color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    Text(
        text = "${started.format(dayFormat)} · ${started.format(DateTimeFormatter.ofPattern("HH:mm"))}",
        color = TextMuted,
        fontSize = 12.sp,
        modifier = Modifier.padding(top = 4.dp)
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.padding(top = 16.dp)
    ) {
        Stat(stringResource(R.string.workout_detail_duration), minutes?.let { "$it min" })
        Stat(stringResource(R.string.workout_detail_sets), session.sets.size.toString())
        Stat(
            stringResource(R.string.workout_detail_volume),
            unit.formatWithSymbol(session.totalVolumeKg)
        )
    }
}

@Composable
private fun Stat(label: String, value: String?) {
    Column {
        Text(text = label, color = TextMuted, fontSize = 10.sp, letterSpacing = 0.5.sp)
        Text(
            text = value ?: stringResource(R.string.workout_detail_no_value),
            color = if (value != null) NeonLime else TextMuted,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
