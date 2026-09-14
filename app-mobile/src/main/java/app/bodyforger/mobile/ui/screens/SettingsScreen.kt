package app.bodyforger.mobile.ui.screens

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
import app.bodyforger.mobile.R
import app.bodyforger.mobile.mcp.McpViewModel
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
import app.bodyforger.mobile.ui.components.HealthConnectSettingsSection
import app.bodyforger.mobile.ui.components.McpSettingsSection
import app.bodyforger.mobile.ui.components.OnboardingSettingsSection
import app.bodyforger.mobile.ui.components.ScaleSettingsSection
import app.bodyforger.mobile.ui.components.SectionStatus
import app.bodyforger.mobile.ui.components.SettingsHeader
import app.bodyforger.mobile.ui.components.SettingsSection
import app.bodyforger.mobile.ui.components.SettingsSectionType
import app.bodyforger.mobile.ui.components.engineLabelRes
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
    onOpenHealthConnect: () -> Unit = {},
    onOpenMcp: () -> Unit = {},
    scaleViewModel: ScaleViewModel = koinViewModel(),
    profileViewModel: AthleteProfileViewModel = koinViewModel(),
    appSettingsViewModel: AppSettingsViewModel = koinViewModel(),
    goalsViewModel: GoalsViewModel = koinViewModel(),
    onboardingViewModel: OnboardingViewModel = koinViewModel(),
    mcpViewModel: McpViewModel = koinViewModel()
) {
    val state by scaleViewModel.state.collectAsState()
    val profile by profileViewModel.profile.collectAsState()
    val measurementProfile = profile.biaProfileOn(LocalDate.now())
    val engineIds = appSettingsViewModel.engineIds
    val selectedEngine by appSettingsViewModel.selectedEngineId.collectAsState()
    val defaultUnit by appSettingsViewModel.defaultWeightUnit.collectAsState()
    val defaultHeightUnit by appSettingsViewModel.defaultHeightUnit.collectAsState()
    val goalStandings by goalsViewModel.standings.collectAsState()
    val mcpUiState by mcpViewModel.uiState.collectAsState()
    var addingGoal by remember { mutableStateOf(false) }

    var openSection by remember {
        mutableStateOf(if (expandScale) SettingsSectionType.SCALE else SettingsSectionType.ATHLETE)
    }
    var showingBiaInfo by remember { mutableStateOf(false) }

    val permissions = remember { app.bodyforger.mobile.ui.components.bluetoothPermissions() }
    val requestPermissions = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted -> if (granted.values.all { it }) scaleViewModel.startScan() }

    if (showingBiaInfo) {
        BiaProfileInfoDialog(onDismiss = { showingBiaInfo = false })
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Obsidian).statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp)
    ) {
        SettingsHeader(onBack, huid = state.huid)

        SettingsSection(
            title = stringResource(R.string.settings_athlete),
            status = SectionStatus.NEUTRAL,
            summary = profile.name ?: stringResource(R.string.settings_athlete_anonymous),
            isExpanded = openSection == SettingsSectionType.ATHLETE,
            onToggle = { openSection = openSection.toggled(SettingsSectionType.ATHLETE) }
        ) {
            AthleteIdentityForm(
                name = profile.name,
                onSave = { newName ->
                    profileViewModel.save(newName, profile.sex, profile.birthDateIso, profile.heightCm)
                    openSection = SettingsSectionType.NONE
                }
            )
        }

        SettingsSection(
            title = stringResource(R.string.settings_bia_profile),
            status = if (profile.isComplete) SectionStatus.DONE else SectionStatus.INCOMPLETE,
            summary = stringResource(
                if (profile.isComplete) R.string.settings_bia_configured else R.string.settings_bia_missing
            ),
            isExpanded = openSection == SettingsSectionType.BIA,
            onToggle = { openSection = openSection.toggled(SettingsSectionType.BIA) },
            onInfo = { showingBiaInfo = true }
        ) {
            AthleteProfileForm(
                profile = profile,
                heightUnit = defaultHeightUnit,
                onSave = { sex, birthDateIso, heightCm ->
                    profileViewModel.save(profile.name, sex, birthDateIso, heightCm)
                    openSection = SettingsSectionType.NONE
                }
            )
        }

        SettingsSection(
            title = stringResource(R.string.settings_scale),
            status = if (state.isAssociated) SectionStatus.DONE else SectionStatus.INCOMPLETE,
            summary = stringResource(
                if (state.isAssociated) R.string.settings_scale_paired else R.string.settings_scale_none
            ),
            isExpanded = openSection == SettingsSectionType.SCALE,
            onToggle = { openSection = openSection.toggled(SettingsSectionType.SCALE) }
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
            summary = app.bodyforger.mobile.ui.components.formatGoalsSummary(goalStandings, defaultUnit),
            isExpanded = openSection == SettingsSectionType.GOALS,
            onToggle = { openSection = openSection.toggled(SettingsSectionType.GOALS) }
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
            isExpanded = openSection == SettingsSectionType.UNIT,
            onToggle = { openSection = openSection.toggled(SettingsSectionType.UNIT) }
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
                isExpanded = openSection == SettingsSectionType.ENGINE,
                onToggle = { openSection = openSection.toggled(SettingsSectionType.ENGINE) }
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
            isExpanded = openSection == SettingsSectionType.ONBOARDING,
            onToggle = { openSection = openSection.toggled(SettingsSectionType.ONBOARDING) }
        ) {
            // Replaying leaves the screen: the tour starts on Home and drives its own way
            // through the tabs.
            OnboardingSettingsSection(
                onReplayTour = onboardingViewModel::replayTour,
                onReplaySetup = onboardingViewModel::replaySetup
            )
        }

        HealthConnectSettingsSection(
            uiState = mcpUiState,
            onClick = onOpenHealthConnect
        )

        McpSettingsSection(
            uiState = mcpUiState,
            onClick = onOpenMcp
        )
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
