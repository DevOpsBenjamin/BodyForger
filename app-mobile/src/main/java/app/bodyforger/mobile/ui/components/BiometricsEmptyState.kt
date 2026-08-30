package app.bodyforger.mobile.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.mobile.ui.theme.ElectricCyan
import app.bodyforger.mobile.ui.theme.TextMuted
import app.bodyforger.mobile.ui.theme.TextPrimary

/**
 * What the biometrics screens show before a first real weigh-in.
 *
 * The alternative would be a screen of plausible figures, which the athlete would read as
 * their own body composition.
 */
@Composable
fun BiometricsEmptyState(
    isProfileComplete: Boolean,
    onOpenScale: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "AUCUNE MESURE",
            color = ElectricCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = if (isProfileComplete) {
                "Montez sur la balance pour votre première pesée : la composition corporelle " +
                    "se lit sur vos propres mesures, pas sur un exemple."
            } else {
                "Renseignez d'abord votre profil de mesure, puis pesez-vous."
            },
            color = TextMuted,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(22.dp))
        Button(
            onClick = onOpenScale,
            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = Color.Black),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(
                text = if (isProfileComplete) "PESER" else "COMPLÉTER MON PROFIL",
                color = Color.Black,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(text = "", color = TextPrimary)
    }
}
