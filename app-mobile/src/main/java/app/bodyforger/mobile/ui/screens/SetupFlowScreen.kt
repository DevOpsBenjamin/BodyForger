package app.bodyforger.mobile.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.core.model.BiaProfile
import app.bodyforger.core.model.WeightUnit
import app.bodyforger.mobile.R
import app.bodyforger.mobile.onboarding.SetupStep
import app.bodyforger.mobile.profile.AppSettingsViewModel
import app.bodyforger.mobile.profile.AthleteProfileViewModel
import app.bodyforger.mobile.profile.GoalsViewModel
import app.bodyforger.mobile.scale.ScaleViewModel
import app.bodyforger.mobile.ui.components.AddGoalDialog
import app.bodyforger.mobile.ui.components.AthleteIdentityForm
import app.bodyforger.mobile.ui.components.AthleteProfileForm
import app.bodyforger.mobile.ui.components.DefaultUnitsSection
import app.bodyforger.mobile.ui.components.GoalsSection
import app.bodyforger.mobile.ui.components.ScaleSettingsSection
import app.bodyforger.mobile.ui.theme.NeonLime
import app.bodyforger.mobile.ui.theme.Obsidian
import app.bodyforger.mobile.ui.theme.SurfaceBorder
import app.bodyforger.mobile.ui.theme.TextMuted
import app.bodyforger.mobile.ui.theme.TextPrimary
import app.bodyforger.mobile.ui.theme.TextTertiary
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate

/**
 * What a fresh install asks before it can do anything useful, one question per screen.
 *
 * It is the Settings sections, in the order that makes them a chain rather than a list, with
 * the same forms doing the same writes — a second set of fields would be a second chance to
 * disagree with the first. What changes is the framing: each step says why it is being asked,
 * and every one of them can be left.
 *
 * A step that has been answered turns its button into Continue; one that has not offers to
 * skip it, in its own words — a scale is not skipped for the same reason a name is. And the
 * whole flow can be dropped from the first screen, which marks it done for good: replay is
 * asked for from Settings, never imposed.
 */
@Composable
fun SetupFlowScreen(
    onFinish: () -> Unit,
    scaleViewModel: ScaleViewModel = koinViewModel(),
    profileViewModel: AthleteProfileViewModel = koinViewModel(),
    appSettingsViewModel: AppSettingsViewModel = koinViewModel(),
    goalsViewModel: GoalsViewModel = koinViewModel()
) {
    var step by remember { mutableStateOf(SetupStep.entries.first()) }
    var addingGoal by remember { mutableStateOf(false) }

    val scaleState by scaleViewModel.state.collectAsState()
    val profile by profileViewModel.profile.collectAsState()
    val defaultUnit by appSettingsViewModel.defaultWeightUnit.collectAsState()
    val goalStandings by goalsViewModel.standings.collectAsState()

    fun back() {
        step.previous()?.let { step = it }
    }

    // Back walks the flow rather than leaving it: dropping the athlete on a home screen the
    // flow has not finished setting up would give them no way of asking for the rest. Leaving
    // is done by the skip button, which records it.
    BackHandler { back() }

    val isAnswered = when (step) {
        // Kilograms and centimetres are already an answer; this step only offers to change it.
        SetupStep.UNITS -> true
        SetupStep.NAME -> !profile.name.isNullOrBlank()
        SetupStep.PROFILE -> profile.isComplete
        SetupStep.SCALE -> scaleState.isAssociated
        SetupStep.GOALS -> goalStandings.isNotEmpty()
    }

    fun advance() {
        val next = step.next()
        if (next == null) onFinish() else step = next
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = stringResource(step.titleRes),
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(top = 28.dp)
        )
        Text(
            text = stringResource(step.whyRes),
            color = TextMuted,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            StepContent(
                step = step,
                scaleViewModel = scaleViewModel,
                profileViewModel = profileViewModel,
                appSettingsViewModel = appSettingsViewModel,
                goalsViewModel = goalsViewModel,
                onAddGoal = { addingGoal = true }
            )
        }

        // Back, where the athlete stands, forward: one row, at the thumb, saying at a glance
        // how much of this is left.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = ::back, enabled = !step.isFirst) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.setup_previous),
                    tint = if (step.isFirst) SurfaceBorder else TextMuted
                )
            }

            StepIndicator(step)

            Button(
                onClick = ::advance,
                colors = ButtonDefaults.buttonColors(containerColor = NeonLime, contentColor = Color.Black),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 18.dp),
                modifier = Modifier.height(46.dp)
            ) {
                Text(
                    text = stringResource(
                        when {
                            step.isLast && isAnswered -> R.string.setup_finish
                            isAnswered -> R.string.setup_continue
                            else -> R.string.setup_skip_step
                        }
                    ),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .size(16.dp)
                )
            }
        }

        // Present from the first screen, as asked: someone who wants none of this should not
        // have to walk through five screens to say so.
        TextButton(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Text(text = stringResource(R.string.setup_skip_all), color = TextTertiary, fontSize = 13.sp)
        }
    }

    if (addingGoal) {
        AddGoalDialog(
            unit = defaultUnit,
            onDismiss = { addingGoal = false },
            onConfirm = { massKg, bodyFat, horizon ->
                goalsViewModel.add(massKg, bodyFat, horizon)
                addingGoal = false
            }
        )
    }
}

/**
 * The body of one step: the very form the Settings screen shows, so that a field answered
 * here and a field answered there write the same thing.
 */
@Composable
private fun StepContent(
    step: SetupStep,
    scaleViewModel: ScaleViewModel,
    profileViewModel: AthleteProfileViewModel,
    appSettingsViewModel: AppSettingsViewModel,
    goalsViewModel: GoalsViewModel,
    onAddGoal: () -> Unit
) {
    val scaleState by scaleViewModel.state.collectAsState()
    val profile by profileViewModel.profile.collectAsState()
    val defaultUnit by appSettingsViewModel.defaultWeightUnit.collectAsState()
    val defaultHeightUnit by appSettingsViewModel.defaultHeightUnit.collectAsState()
    val goalStandings by goalsViewModel.standings.collectAsState()

    when (step) {
        SetupStep.UNITS -> DefaultUnitsSection(
            weightUnit = defaultUnit,
            heightUnit = defaultHeightUnit,
            onSelectWeight = appSettingsViewModel::selectDefaultWeightUnit,
            onSelectHeight = appSettingsViewModel::selectDefaultHeightUnit
        )

        SetupStep.NAME -> AthleteIdentityForm(
            name = profile.name,
            onSave = { newName ->
                profileViewModel.save(newName, profile.sex, profile.birthDateIso, profile.heightCm)
            }
        )

        SetupStep.PROFILE -> AthleteProfileForm(
            profile = profile,
            heightUnit = defaultHeightUnit,
            onSave = { sex, birthDateIso, heightCm ->
                profileViewModel.save(profile.name, sex, birthDateIso, heightCm)
            }
        )

        SetupStep.SCALE -> ScalePairingStep(
            scaleViewModel = scaleViewModel,
            unit = defaultUnit,
            measurementProfile = profile.biaProfileOn(LocalDate.now())
        )

        SetupStep.GOALS -> GoalsSection(
            standings = goalStandings,
            unit = defaultUnit,
            onAdd = onAddGoal,
            onRemove = goalsViewModel::remove,
            onToggleValidated = goalsViewModel::setValidated
        )
    }
}

/**
 * Pairing, with the permission dance the Settings screen also does.
 *
 * Since Android 12 scanning and connecting have their own permissions; before that a scan
 * went through location for want of anything better.
 */
@Composable
private fun ScalePairingStep(
    scaleViewModel: ScaleViewModel,
    unit: WeightUnit,
    measurementProfile: BiaProfile?
) {
    val state by scaleViewModel.state.collectAsState()
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
    } else {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }
    val requestPermissions = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        // Without the permission, starting a scan that would see nothing is pointless.
        if (granted.values.all { it }) scaleViewModel.startScan()
    }

    ScaleSettingsSection(
        unit = unit,
        state = state,
        measurementProfile = measurementProfile,
        onStartScan = { requestPermissions.launch(permissions) },
        onStopScan = scaleViewModel::stopScan,
        onAssociate = { scale -> measurementProfile?.let { scaleViewModel.associate(scale, it) } },
        onForget = scaleViewModel::forgetScale
    )
}

/** Where the athlete is in the flow: one dot per step, the ones behind them lit. */
@Composable
private fun StepIndicator(step: SetupStep) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SetupStep.entries.forEach { entry ->
            Box(
                modifier = Modifier
                    .size(if (entry == step) 9.dp else 7.dp)
                    .clip(CircleShape)
                    .background(if (entry.ordinal <= step.ordinal) NeonLime else SurfaceBorder)
            )
        }
    }
}
