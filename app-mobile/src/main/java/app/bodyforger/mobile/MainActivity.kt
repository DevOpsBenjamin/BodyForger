package app.bodyforger.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.health.connect.client.PermissionController
import app.bodyforger.core.healthconnect.HealthConnectManager
import app.bodyforger.core.healthconnect.HealthConnectPermissions
import app.bodyforger.mobile.navigation.BodyForgerNavHost
import app.bodyforger.mobile.navigation.Destination
import app.bodyforger.mobile.navigation.Tab
import app.bodyforger.mobile.navigation.currentTab
import app.bodyforger.mobile.navigation.leaveTour
import app.bodyforger.mobile.navigation.showForTour
import app.bodyforger.mobile.navigation.switchTab
import app.bodyforger.mobile.onboarding.OnboardingViewModel
import app.bodyforger.mobile.onboarding.ScreenTourOverlay
import app.bodyforger.mobile.onboarding.TourStop
import app.bodyforger.mobile.profile.AppSettingsViewModel
import app.bodyforger.mobile.ui.components.ActiveWorkoutMiniBar
import app.bodyforger.mobile.ui.components.BodyForgerBottomNav
import app.bodyforger.mobile.ui.components.HealthConnectRationaleDialog
import app.bodyforger.mobile.ui.components.ResumeWorkoutDialog
import app.bodyforger.mobile.ui.theme.BodyForgerTheme
import app.bodyforger.mobile.ui.theme.Obsidian
import app.bodyforger.mobile.workout.LiveWorkoutViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    private val healthConnectManager: HealthConnectManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BodyForgerTheme {
                BodyForgerApp(healthConnectManager = healthConnectManager)
            }
        }
    }
}

/**
 * The frame around the graph: the bottom bar, the mini bar of a running workout, and the
 * offer to pick up an interrupted session.
 *
 * Everything else is a destination — see `navigation/BodyForgerNavHost`.
 */
@Composable
fun BodyForgerApp(
    workout: LiveWorkoutViewModel = koinViewModel(),
    onboarding: OnboardingViewModel = koinViewModel(),
    appSettings: AppSettingsViewModel = koinViewModel(),
    healthConnectManager: HealthConnectManager
) {
    val navController = rememberNavController()
    val currentDestination by navController.currentBackStackEntryAsState()
    val currentTab = currentDestination?.destination.currentTab()
    val onSetupScreen = currentDestination?.destination?.hasRoute<Destination.Setup>() == true

    val context = androidx.compose.ui.platform.LocalContext.current
    val intent = (context as? android.app.Activity)?.intent
    LaunchedEffect(intent?.data) {
        val uri = intent?.data
        if (uri?.scheme == "bodyforger" && (uri.host == "mcp" || uri.host == "healthconnect")) {
            navController.navigate(Destination.HealthConnectMcp) { launchSingleTop = true }
        }
    }

    val tourDue by onboarding.tourDue.collectAsState()
    val setupDue by onboarding.setupDue.collectAsState()
    var tourStop by remember { mutableStateOf(TourStop.entries.first()) }

    // A replay asked for from Settings arrives as the tour falling due again; it has to start
    // over rather than resume at the stop the last run ended on.
    LaunchedEffect(tourDue) {
        if (tourDue == true) tourStop = TourStop.entries.first()
    }

    // The tour drives the navigation rather than the athlete: each stop moves the app to the
    // screen it is about, and the card is drawn over whatever arrives.
    LaunchedEffect(tourDue, tourStop) {
        if (tourDue == true) navController.showForTour(tourStop.destination)
    }

    // The tour comes first, then the questions: being shown the screens is what makes the
    // questions about them mean something. Both are waited for rather than assumed — a null
    // is the settings row not read yet, and neither should flash by while it loads.
    //
    // Going in and coming back out both follow the stored answer, never the tap that caused
    // it. Leaving the screen on the tap and recording it in the background raced: the flow
    // was still owed for the instant the write took, and the athlete was put straight back
    // on the step they had just left.
    LaunchedEffect(tourDue, setupDue, onSetupScreen) {
        when {
            tourDue == false && setupDue == true && !onSetupScreen ->
                navController.navigate(Destination.Setup) { launchSingleTop = true }

            setupDue == false && onSetupScreen -> navController.switchTab(Tab.HOME)
        }
    }

    val interruptedSession by workout.resumable.collectAsState()
    val liveWorkout by workout.active.collectAsState()
    val restTimer by workout.restTimer.collectAsState()

    if (tourDue == true) {
        ScreenTourOverlay(
            stop = tourStop,
            onNext = {
                val next = tourStop.next()
                if (next == null) {
                    // The last stop is Settings; the tour hands the app back on Home, where it
                    // would have opened had there been no tour.
                    onboarding.markTourSeen()
                    navController.leaveTour()
                } else {
                    tourStop = next
                }
            },
            onPrevious = { tourStop.previous()?.let { tourStop = it } },
            onSkip = {
                onboarding.markTourSeen()
                navController.leaveTour()
            }
        )
    }

    val healthConnectDismissed by appSettings.healthConnectPromptDismissed.collectAsState()
    val isHealthConnectAvailable = remember { healthConnectManager.isAvailable() }

    val healthConnectPermissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) {
        appSettings.setHealthConnectPromptDismissed(true)
    }

    LaunchedEffect(healthConnectDismissed, isHealthConnectAvailable) {
        if (isHealthConnectAvailable && healthConnectDismissed == false) {
            val client = healthConnectManager.getClientOrNull()
            if (client != null) {
                val granted = try {
                    client.permissionController.getGrantedPermissions()
                } catch (_: Exception) {
                    emptySet()
                }
                if (HealthConnectPermissions.CORE_READ_PERMISSIONS.all { it in granted }) {
                    appSettings.setHealthConnectPromptDismissed(true)
                }
            }
        }
    }

    // A session left open is settled before anything else: the athlete must not discover it
    // in the middle of the next one.
    interruptedSession?.let { session ->
        ResumeWorkoutDialog(
            session = session,
            onResume = {
                workout.resume(session)
                navController.navigate(Destination.LiveWorkout)
            },
            onFinishAsIs = { workout.finishInterrupted(session) },
            onDelete = { workout.deleteInterrupted(session) }
        )
    }

    val showHealthConnectRationale = isHealthConnectAvailable &&
        tourDue == false &&
        healthConnectDismissed == false &&
        interruptedSession == null

    if (showHealthConnectRationale) {
        HealthConnectRationaleDialog(
            onAccept = {
                appSettings.setHealthConnectPromptDismissed(true)
                healthConnectPermissionLauncher.launch(HealthConnectPermissions.CORE_READ_PERMISSIONS)
            },
            onDecline = {
                appSettings.setHealthConnectPromptDismissed(true)
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Obsidian,
        bottomBar = {
            // The bar belongs to the tabs alone: a full screen must stay full.
            if (currentTab != null) {
                Column {
                    ActiveWorkoutMiniBar(
                        isVisible = liveWorkout != null,
                        workoutTitle = liveWorkout?.session?.title.orEmpty(),
                        restSecondsRemaining = restTimer?.secondsRemaining,
                        onClick = { navController.navigate(Destination.LiveWorkout) }
                    )
                    BodyForgerBottomNav(
                        currentTab = currentTab,
                        onTabSelected = navController::switchTab
                    )
                }
            }
        }
    ) { innerPadding ->
        BodyForgerNavHost(
            navController = navController,
            onSetupFinished = onboarding::markSetupDone,
            workout = workout,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
