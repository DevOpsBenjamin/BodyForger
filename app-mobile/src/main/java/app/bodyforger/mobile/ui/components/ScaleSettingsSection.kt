package app.bodyforger.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import app.bodyforger.core.model.BiaProfile
import app.bodyforger.mobile.scale.ScaleUiState
import app.bodyforger.mobile.ui.theme.ElectricCyan
import app.bodyforger.mobile.ui.theme.SurfaceBorder
import app.bodyforger.mobile.ui.theme.SurfaceDark
import app.bodyforger.mobile.ui.theme.TextPrimary
import app.bodyforger.mobile.ui.theme.TextSecondary

/**
 * The Scale section of the settings: associate a device, then weigh in.
 *
 * Pairing happens once; as long as an association exists, weigh-ins use it directly.
 */
@Composable
fun ScaleSettingsSection(
    state: ScaleUiState,
    measurementProfile: BiaProfile?,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onAssociate: (DiscoveredScale) -> Unit,
    onForget: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {


        when {
            state.isPairing -> Pairing(state)
            state.association != null -> AssociatedScale(state, onForget)
            state.isScanning -> Scanning(state.discovered, onAssociate, onStopScan)
            else -> NotAssociated(onStartScan)
        }

        state.scanError?.let { message ->
            Card {
                Text("Recherche impossible", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(message, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            }
        }
        state.failure?.let { Failure(it) }
        state.weightAwaitingBodyFat?.let { massKg ->
            Card {
                Text("Pesée relevée, sans composition", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text("%.2f kg".format(massKg), color = ElectricCyan, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(
                    "La balance n'a pas relevé d'impédance — la poignée n'a pas été saisie. " +
                        "Le relevé attend une saisie du taux de masse grasse avant d'être conservé.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
        state.lastLog?.let { log ->
            Card {
                Text("Dernière pesée", color = TextSecondary, fontSize = 11.sp)
                Text(
                    "%.2f kg · %.1f %% de masse grasse".format(log.massKg, log.bodyFatPercentage),
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                val readings = log.rawImpedances.ohmsByReading.size
                Text(
                    if (readings > 0) "$readings résistances relevées et conservées"
                    else "Aucune impédance : masse seule",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

