package app.bodyforger.mobile.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.bodyforger.mobile.R
import app.bodyforger.mobile.ui.theme.NeonLime
import app.bodyforger.mobile.ui.theme.SurfaceBorder
import app.bodyforger.mobile.ui.theme.SurfaceDark
import app.bodyforger.mobile.ui.theme.TextMuted
import app.bodyforger.mobile.ui.theme.TextPrimary
import app.bodyforger.mobile.ui.theme.TextTertiary

/**
 * The screen tour, drawn over whatever screen it is talking about.
 *
 * A dialog rather than a box inside the layout: it has to cover the bottom bar as well, and
 * it has to be modal — a tap beside the card must do nothing, or the athlete ends up two
 * screens away from the one being explained.
 *
 * Back does not dismiss it. Leaving is done by Skip, which is always on screen, so there is
 * exactly one way out and it is one the app can record.
 *
 * The scrim is light on purpose. It was opaque enough to hide the screen underneath, which
 * left the card talking about something nobody could see — the screen has to stay readable
 * behind it, since the screen is the subject. The card carries its own solid background, so
 * its own text loses no contrast.
 */
@Composable
fun ScreenTourOverlay(
    stop: TourStop,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .padding(horizontal = 28.dp)
                    .fillMaxWidth()
                    .border(1.dp, NeonLime.copy(alpha = 0.35f), RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = stringResource(stop.titleRes),
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = stringResource(stop.bodyRes),
                        color = TextMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 8.dp, bottom = 18.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StopIndicator(stop)
                        Button(
                            onClick = onNext,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonLime,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = stringResource(
                                    if (stop.isLast) R.string.tour_finish else R.string.tour_next
                                ),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Always reachable, on every stop, including the first: a tour nobody can leave
            // is a wall.
            TextButton(
                onClick = onSkip,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = stringResource(R.string.tour_skip),
                    color = TextTertiary,
                    fontSize = 13.sp
                )
            }
        }
    }
}

/** Where the athlete is in the tour: one dot per stop, the current one lit. */
@Composable
private fun StopIndicator(stop: TourStop) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        TourStop.entries.forEach { entry ->
            Box(
                modifier = Modifier
                    .size(if (entry == stop) 8.dp else 6.dp)
                    .clip(CircleShape)
                    .background(if (entry == stop) NeonLime else SurfaceBorder)
            )
        }
    }
}
