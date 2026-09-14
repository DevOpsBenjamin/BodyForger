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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.core.ble.DiscoveredScale
import app.bodyforger.core.ble.SessionFailure
import app.bodyforger.core.model.BiaProfile
import app.bodyforger.core.model.WeightUnit
import app.bodyforger.mobile.R
import app.bodyforger.mobile.scale.ScaleUiState
import app.bodyforger.mobile.ui.theme.ElectricCyan
import app.bodyforger.mobile.ui.theme.SurfaceBorder
import app.bodyforger.mobile.ui.theme.SurfaceDark
import android.Manifest
import android.os.Build
import app.bodyforger.mobile.ui.theme.TextPrimary
import app.bodyforger.mobile.ui.theme.TextSecondary

fun bluetoothPermissions(): Array<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
    } else {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }

/**
 * The Scale section of the settings: associate a device, then weigh in.
 *
 * Pairing happens once; as long as an association exists, weigh-ins use it directly.
 */
@Composable
fun ScaleSettingsSection(
    /** What the athlete reads a body mass in; the reading itself is kilograms. */
    unit: WeightUnit,
    state: ScaleUiState,
    measurementProfile: BiaProfile?,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onAssociate: (DiscoveredScale) -> Unit,
    onForget: (deviceAddress: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {


        when {
            state.isPairing -> Pairing(state)
            // Every paired scale gets its own card: pairing a second one used to hide the
            // first, which could then not even be forgotten.
            state.isAssociated -> state.associations.forEach { paired ->
                AssociatedScale(paired, state) { onForget(paired.deviceAddress) }
            }
            state.isScanning -> Scanning(state.discovered, onAssociate, onStopScan)
            else -> NotAssociated(onStartScan)
        }

        state.scanError?.let { message ->
            Card {
                Text(stringResource(R.string.scale_search_failed), color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(message, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            }
        }
        state.failure?.let { Failure(it) }
        state.massOnlyReadingKg?.let { massKg ->
            Card {
                Text(stringResource(R.string.scale_mass_only_title), color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(unit.formatWithSymbol(massKg), color = ElectricCyan, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(
                    stringResource(R.string.scale_mass_only_body),
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
        state.lastLog?.let { log ->
            Card {
                Text(stringResource(R.string.scale_last_weigh_in), color = TextSecondary, fontSize = 11.sp)
                Text(
                    log.bodyFatPercentage
                        ?.let { stringResource(R.string.scale_mass_and_fat, unit.formatWithSymbol(log.massKg), it) }
                        ?: stringResource(R.string.scale_mass_only_line, unit.formatWithSymbol(log.massKg)),
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                val readings = log.rawImpedances.ohmsByReading.size
                Text(
                    if (readings > 0) stringResource(R.string.scale_resistances_kept, readings)
                    else stringResource(R.string.scale_no_impedance),
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

