package app.bodyforger.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.bodyforger.mobile.navigation.BodyForgerNavHost
import app.bodyforger.mobile.navigation.Destination
import app.bodyforger.mobile.navigation.currentTab
import app.bodyforger.mobile.navigation.switchTab
import app.bodyforger.mobile.ui.components.ActiveWorkoutMiniBar
import app.bodyforger.mobile.ui.components.BodyForgerBottomNav
import app.bodyforger.mobile.ui.components.ResumeWorkoutDialog
import app.bodyforger.mobile.ui.theme.BodyForgerTheme
import app.bodyforger.mobile.ui.theme.Obsidian
import app.bodyforger.mobile.workout.LiveWorkoutViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BodyForgerTheme {
                BodyForgerApp()
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
fun BodyForgerApp(workout: LiveWorkoutViewModel = koinViewModel()) {
    val navController = rememberNavController()
    val currentDestination by navController.currentBackStackEntryAsState()
    val currentTab = currentDestination?.destination.currentTab()

    val interruptedSession by workout.resumable.collectAsState()
    val liveWorkout by workout.active.collectAsState()

    // Une séance laissée ouverte se traite avant tout le reste : l'athlète ne doit pas la
    // découvrir au milieu de la suivante.
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Obsidian,
        bottomBar = {
            // La barre n'appartient qu'aux onglets : un écran plein doit rester plein.
            if (currentTab != null) {
                Column {
                    ActiveWorkoutMiniBar(
                        isVisible = liveWorkout != null,
                        workoutTitle = liveWorkout?.session?.title.orEmpty(),
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
            workout = workout,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
