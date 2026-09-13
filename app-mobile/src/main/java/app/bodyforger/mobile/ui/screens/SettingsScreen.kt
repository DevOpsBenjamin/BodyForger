package app.bodyforger.mobile.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.core.bia.ModelSelector
import app.bodyforger.mobile.R
import app.bodyforger.mobile.profile.AppSettingsViewModel
import app.bodyforger.mobile.profile.AthleteProfileViewModel
import app.bodyforger.mobile.onboarding.OnboardingViewModel
import app.bodyforger.mobile.profile.GoalsViewModel
import app.bodyforger.mobile.scale.ScaleViewModel
import app.bodyforger.mobile.ui.components.AddGoalDialog
import app.bodyforger.mobile.ui.components.AthleteIdentityForm
import app.bodyforger.mobile.ui.components.AthleteProfileForm
import app.bodyforger.mobile.ui.components.BiaEngineSection
import app.bodyforger.mobile.ui.components.BiaProfileInfoDialog
import app.bodyforger.mobile.ui.components.DefaultUnitsSection
import app.bodyforger.mobile.ui.components.GoalsSection
import app.bodyforger.mobile.ui.components.OnboardingSettingsSection
import app.bodyforger.mobile.ui.components.ScaleSettingsSection
import app.bodyforger.mobile.ui.components.SectionStatus
import app.bodyforger.mobile.ui.components.SettingsSection
import app.bodyforger.mobile.ui.text.label
import app.bodyforger.mobile.ui.theme.Obsidian
import app.bodyforger.mobile.ui.theme.TextPrimary
import app.bodyforger.mobile.ui.theme.TextSecondary
import java.time.LocalDate
import org.koin.androidx.compose.koinViewModel

/**
 * Settings, as a list of sections that fold away once dealt with.
 *
 * Only one is open at a time: they are steps, and the athlete is looking for the one still
 * left to do. [expandScale] opens the scale section straight away, for callers arriving from
 * a screen that needed a scale and did not find one.
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    expandScale: Boolean = false,
    scaleViewModel: ScaleViewModel = koinViewModel(),
    profileViewModel: AthleteProfileViewModel = koinViewModel(),
    appSettingsViewModel: AppSettingsViewModel = koinViewModel(),
    goalsViewModel: GoalsViewModel = koinViewModel(),
    onboardingViewModel: OnboardingViewModel = koinViewModel()
) {
    val state by scaleViewModel.state.collectAsState()
    val profile by profileViewModel.profile.collectAsState()
    val measurementProfile = profile.biaProfileOn(LocalDate.now())
    val engineIds = appSettingsViewModel.engineIds
    val selectedEngine by appSettingsViewModel.selectedEngineId.collectAsState()
    val defaultUnit by appSettingsViewModel.defaultWeightUnit.collectAsState()
    val defaultHeightUnit by appSettingsViewModel.defaultHeightUnit.collectAsState()
    val goalStandings by goalsViewModel.standings.collectAsState()
    var addingGoal by remember { mutableStateOf(false) }

    var openSection by remember {
        mutableStateOf(if (expandScale) Section.SCALE else Section.entries.first { it == Section.ATHLETE })
    }
    var showingBiaInfo by remember { mutableStateOf(false) }

    // Since Android 12, scanning and connecting have their own permissions; before that, a
    // scan went through location for want of anything better.
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

    if (showingBiaInfo) {
        BiaProfileInfoDialog(onDismiss = { showingBiaInfo = false })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp)
    ) {
        Header(onBack, huid = state.huid)

        SettingsSection(
            title = stringResource(R.string.settings_athlete),
            status = SectionStatus.NEUTRAL,
            summary = profile.name ?: stringResource(R.string.settings_athlete_anonymous),
            isExpanded = openSection == Section.ATHLETE,
            onToggle = { openSection = openSection.toggled(Section.ATHLETE) }
        ) {
            AthleteIdentityForm(
                name = profile.name,
                onSave = { newName ->
                    profileViewModel.save(newName, profile.sex, profile.birthDateIso, profile.heightCm)
                    openSection = Section.NONE
                }
            )
        }

        SettingsSection(
            title = stringResource(R.string.settings_bia_profile),
            status = if (profile.isComplete) SectionStatus.DONE else SectionStatus.INCOMPLETE,
            summary = stringResource(
                if (profile.isComplete) R.string.settings_bia_configured else R.string.settings_bia_missing
            ),
            isExpanded = openSection == Section.BIA,
            onToggle = { openSection = openSection.toggled(Section.BIA) },
            onInfo = { showingBiaInfo = true }
        ) {
            AthleteProfileForm(
                profile = profile,
                heightUnit = defaultHeightUnit,
                onSave = { sex, birthDateIso, heightCm ->
                    profileViewModel.save(profile.name, sex, birthDateIso, heightCm)
                    openSection = Section.NONE
                }
            )
        }

        SettingsSection(
            title = stringResource(R.string.settings_scale),
            status = if (state.isAssociated) SectionStatus.DONE else SectionStatus.INCOMPLETE,
            summary = stringResource(
                if (state.isAssociated) R.string.settings_scale_paired else R.string.settings_scale_none
            ),
            isExpanded = openSection == Section.SCALE,
            onToggle = { openSection = openSection.toggled(Section.SCALE) }
        ) {
            ScaleSettingsSection(
                unit = defaultUnit,
                state = state,
                measurementProfile = measurementProfile,
                onStartScan = { requestPermissions.launch(permissions) },
                onStopScan = scaleViewModel::stopScan,
                onAssociate = { scale -> measurementProfile?.let { scaleViewModel.associate(scale, it) } },
                onForget = scaleViewModel::forgetScale
            )
        }

        SettingsSection(
            title = stringResource(R.string.settings_goals),
            status = if (goalStandings.any { !it.goal.isValidated }) SectionStatus.DONE else SectionStatus.INCOMPLETE,
            summary = goalStandings.firstOrNull { !it.goal.isValidated }?.let { standing ->
                standing.goal.targetBodyFatPercentage
                    ?.let { fat ->
                        stringResource(
                            R.string.goals_target_mass_and_fat,
                            defaultUnit.formatWithSymbol(standing.goal.targetMassKg),
                            fat
                        )
                    }
                    ?: stringResource(
                        R.string.goals_target_mass,
                        defaultUnit.formatWithSymbol(standing.goal.targetMassKg)
                    )
            } ?: stringResource(R.string.settings_goals_none),
            isExpanded = openSection == Section.GOALS,
            onToggle = { openSection = openSection.toggled(Section.GOALS) }
        ) {
            GoalsSection(
                standings = goalStandings,
                unit = defaultUnit,
                onAdd = { addingGoal = true },
                onRemove = goalsViewModel::remove,
                onToggleValidated = goalsViewModel::setValidated
            )
        }

        SettingsSection(
            title = stringResource(R.string.settings_default_units),
            status = SectionStatus.NEUTRAL,
            summary = stringResource(R.string.settings_units_summary, defaultUnit.label(), defaultHeightUnit.label()),
            isExpanded = openSection == Section.UNIT,
            onToggle = { openSection = openSection.toggled(Section.UNIT) }
        ) {
            DefaultUnitsSection(
                weightUnit = defaultUnit,
                heightUnit = defaultHeightUnit,
                onSelectWeight = appSettingsViewModel::selectDefaultWeightUnit,
                onSelectHeight = appSettingsViewModel::selectDefaultHeightUnit
            )
        }

        // Only worth a choice when more than one engine is compiled into the build.
        if (engineIds.size > 1) {
            SettingsSection(
                title = stringResource(R.string.settings_engine),
                status = SectionStatus.NEUTRAL,
                summary = stringResource(engineLabelRes(selectedEngine)),
                isExpanded = openSection == Section.ENGINE,
                onToggle = { openSection = openSection.toggled(Section.ENGINE) }
            ) {
                BiaEngineSection(
                    engineIds = engineIds,
                    selectedId = selectedEngine,
                    onSelect = appSettingsViewModel::selectEngine
                )
            }
        }

        SettingsSection(
            title = stringResource(R.string.settings_onboarding),
            status = SectionStatus.NEUTRAL,
            summary = stringResource(R.string.settings_onboarding_summary),
            isExpanded = openSection == Section.ONBOARDING,
            onToggle = { openSection = openSection.toggled(Section.ONBOARDING) }
        ) {
            // Replaying leaves the screen: the tour starts on Home and drives its own way
            // through the tabs.
            OnboardingSettingsSection(
                onReplayTour = onboardingViewModel::replayTour,
                onReplaySetup = onboardingViewModel::replaySetup
            )
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

private fun engineLabelRes(id: String): Int = when (id) {
    ModelSelector.FORGEFIT_PRIVATE -> R.string.settings_engine_forgefit_private
    else -> R.string.settings_engine_forgefit_mit
}

@Composable
private fun Header(onBack: () -> Unit, huid: String?) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = TextPrimary)
        }
        Text(
            text = stringResource(R.string.nav_settings),
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 4.dp)
        )
    }

    Text(
        text = stringResource(R.string.settings_measurement_id, huid ?: "…"),
        color = TextSecondary,
        fontSize = 11.sp,
        modifier = Modifier.padding(start = 12.dp, bottom = 20.dp)
    )
}

/** Which section is unfolded. Only one at a time: they are steps, not a list to browse. */
private enum class Section {
    NONE,
    ATHLETE,
    BIA,
    SCALE,
    GOALS,
    UNIT,
    ENGINE,
    ONBOARDING;

    fun toggled(tapped: Section): Section = if (this == tapped) NONE else tapped
}
