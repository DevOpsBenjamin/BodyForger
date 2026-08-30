package app.bodyforger.mobile.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.core.ble.DiscoveredScale
import app.bodyforger.core.ble.SessionFailure
import app.bodyforger.mobile.scale.ScaleUiState
import app.bodyforger.mobile.ui.theme.ElectricCyan
import app.bodyforger.mobile.ui.theme.TextPrimary
import app.bodyforger.mobile.ui.theme.TextSecondary

@Composable
internal fun Pairing(state: ScaleUiState) {
    Card {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(color = ElectricCyan, strokeWidth = 2.dp, modifier = Modifier.padding(end = 12.dp))
            Text("Appairage en cours", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
        state.pairingStep?.let { (index, total) ->
            Text(
                "Étape ${index + 1} sur $total",
                color = ElectricCyan,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
        state.pairingInstructions.forEach { instruction ->
            Text(instructionLabel(instruction), color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
        Text(
            "Une simple pesée de calibration termine l'appairage : sans elle, l'association ne sera pas enregistrée.",
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

/**
 * One device row.
 *
 * An unrecognised device shows but is not clickable: without a driver, engraving on it would
 * mean writing at random into unknown hardware.
 */

@Composable
internal fun AssociatedScale(state: ScaleUiState, onForget: () -> Unit) {
    Card {
        Text(state.association!!.advertisedName, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        Text("Associée · ${state.association.deviceAddress}", color = TextSecondary, fontSize = 11.sp)
        Text(
            "Tare de calibration : %.2f kg".format(state.association.tareKg),
            color = TextSecondary,
            fontSize = 11.sp
        )

        state.progress?.let { progress ->
            Text(
                "Étape ${progress.index + 1} sur ${progress.totalSteps}",
                color = ElectricCyan,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 12.dp)
            )
            progress.instructions.forEach { instruction ->
                Text(instructionLabel(instruction), color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }

        OutlinedButton(onClick = onForget, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text("Oublier cette balance", color = TextSecondary)
        }
    }
}

@Composable
internal fun Failure(failure: SessionFailure) {
    Card {
        Text("Pesée interrompue", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Text(failureLabel(failure), color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
    }
}
