package app.bodyforger.wear.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.bodyforger.wear.presentation.screens.WearHomeScreen
import app.bodyforger.wear.presentation.screens.WearLiveWorkoutScreen
import app.bodyforger.wear.presentation.screens.WearWeighInScreen
import app.bodyforger.wear.presentation.theme.BodyForgerWearTheme
import app.bodyforger.wear.tile.BodyForgerTileService

enum class WearScreenState {
    HOME,
    LIVE_WORKOUT,
    WEIGH_IN
}

class WearMainActivity : ComponentActivity() {

    private var currentScreen by mutableStateOf(WearScreenState.HOME)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)

        setContent {
            BodyForgerWearTheme {
                WearApp(
                    screenState = currentScreen,
                    onNavigate = { currentScreen = it }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val target = intent?.getStringExtra(BodyForgerTileService.EXTRA_NAV_TARGET)
        currentScreen = when (target) {
            BodyForgerTileService.TARGET_WORKOUT -> WearScreenState.LIVE_WORKOUT
            BodyForgerTileService.TARGET_WEIGH_IN -> WearScreenState.WEIGH_IN
            else -> WearScreenState.HOME
        }
    }
}

@Composable
fun WearApp(
    screenState: WearScreenState,
    onNavigate: (WearScreenState) -> Unit
) {
    when (screenState) {
        WearScreenState.HOME -> {
            WearHomeScreen(
                onStartWorkout = { onNavigate(WearScreenState.LIVE_WORKOUT) },
                onStartWeighIn = { onNavigate(WearScreenState.WEIGH_IN) }
            )
        }
        WearScreenState.LIVE_WORKOUT -> {
            WearLiveWorkoutScreen(
                onFinishWorkout = { onNavigate(WearScreenState.HOME) }
            )
        }
        WearScreenState.WEIGH_IN -> {
            WearWeighInScreen(
                onDismiss = { onNavigate(WearScreenState.HOME) }
            )
        }
    }
}
