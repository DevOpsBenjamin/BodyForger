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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.mobile.R
import app.bodyforger.mobile.library.LibraryViewModel
import app.bodyforger.mobile.profile.AthleteProfileViewModel
import app.bodyforger.mobile.profile.BiometricsViewModel
import app.bodyforger.mobile.profile.GoalsViewModel
import app.bodyforger.mobile.scale.ScaleViewModel
import app.bodyforger.mobile.ui.components.HomeActionCards
import app.bodyforger.mobile.ui.components.HomeVolumeProgressCard
import app.bodyforger.mobile.ui.components.HomeWeightGraphCard
import app.bodyforger.mobile.ui.components.ScalePickerDialog
import app.bodyforger.mobile.ui.components.WeighInFeedback
import app.bodyforger.mobile.ui.theme.NeonLime
import app.bodyforger.mobile.ui.theme.Obsidian
import app.bodyforger.mobile.ui.theme.SurfaceBorder
import app.bodyforger.mobile.ui.theme.SurfaceElevated
import app.bodyforger.mobile.ui.theme.TextPrimary
import app.bodyforger.mobile.ui.theme.TextSecondary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    onNavigateToWorkout: () -> Unit,
    onNavigateToBiometrics: () -> Unit,
    onConfigureScale: () -> Unit = {},
    scaleViewModel: ScaleViewModel = koinViewModel(),
    profileViewModel: AthleteProfileViewModel = koinViewModel(),
    biometricsViewModel: BiometricsViewModel = koinViewModel(),
    library: LibraryViewModel = koinViewModel(),
    goalsViewModel: GoalsViewModel = koinViewModel(),
    onOpenSettings: () -> Unit = {}
) {
    val scaleState by scaleViewModel.state.collectAsState()
    val weighIns by biometricsViewModel.history.collectAsState()
    val profile by profileViewModel.profile.collectAsState()
    val measurementProfile = profile.biaProfileOn(LocalDate.now())

    val today = LocalDate.now()
    val routines by library.routines.collectAsState()
    val activeGoal by goalsViewModel.active.collectAsState()
    // The planner assigns routines to weekdays; today's is whichever claims this one.
    val todaysRoutine = remember(routines, today) {
        routines.firstOrNull { today.dayOfWeek.value in it.assignedDays }
    }

    // A weigh-in is driven from here: this is the screen the athlete reaches standing on the scale.
    WeighInFeedback(state = scaleState, onDismiss = scaleViewModel::clearWeighInFeedback)

    var pickingScale by remember { mutableStateOf(false) }
    if (pickingScale) {
        ScalePickerDialog(
            scales = scaleState.associations,
            onPick = { chosen ->
                pickingScale = false
                measurementProfile?.let { scaleViewModel.weighIn(chosen.deviceAddress, it) }
            },
            onDismiss = { pickingScale = false }
        )
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        // --- 1. TOP BAR: app name + date + settings button ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "BODYFORGER",
                    color = NeonLime,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = today.format(HOME_DATE_FORMAT).replaceFirstChar { it.uppercase() },
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(SurfaceElevated)
                    .border(1.dp, SurfaceBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onOpenSettings) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.cd_settings),
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // --- 2. WEIGHT TREND GRAPH ---
        HomeWeightGraphCard(weighIns = weighIns, goal = activeGoal)

        Spacer(modifier = Modifier.height(16.dp))

        HomeActionCards(
            todaysRoutine = todaysRoutine,
            scaleName = scaleState.onlyAssociation?.advertisedName,
            onNavigateToWorkout = onNavigateToWorkout,
            onNavigateToBiometrics = onNavigateToBiometrics,
            isScaleReady = scaleState.isAssociated && measurementProfile != null,
            onWeighIn = {
                // A single paired scale is not chosen, it is stepped on.
                val only = scaleState.onlyAssociation
                if (only != null) measurementProfile?.let { scaleViewModel.weighIn(only.deviceAddress, it) }
                else pickingScale = true
            },
            onConfigureScale = onConfigureScale
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- 4. WEEKLY VOLUME SUMMARY ---
        HomeVolumeProgressCard()
    }
}

/** "Friday 28 August 2026" in English, "vendredi 28 août 2026" in French — the locale decides. */
private val HOME_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy")
