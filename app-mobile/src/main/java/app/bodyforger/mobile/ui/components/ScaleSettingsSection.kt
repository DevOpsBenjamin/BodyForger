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
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onAssociate: (DiscoveredScale) -> Unit,
    onForget: () -> Unit,
    onWeighIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionTitle("BALANCE CONNECTÉE")

        when {
            state.isPairing -> Pairing(state)
            state.association != null -> AssociatedScale(state, onForget, onWeighIn)
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

@Composable
private fun NotAssociated(onStartScan: () -> Unit) {
    Card {
        Text("Aucune balance associée", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        Text(
            // Sans ce geste, la balance ne s'annonce pas et reste invisible au scan.
            "Lancez la recherche, puis choisissez votre balance dans la liste. " +
                "L'appairage grave votre profil dans sa mémoire et demande une pesée de calibration.",
            color = TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )
        Button(
            onClick = onStartScan,
            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = Color.Black),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Rechercher une balance", fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun Scanning(
    discovered: List<DiscoveredScale>,
    onAssociate: (DiscoveredScale) -> Unit,
    onStopScan: () -> Unit
) {
    Card {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(color = ElectricCyan, strokeWidth = 2.dp, modifier = Modifier.padding(end = 12.dp))
            Text("Recherche en cours…", color = TextPrimary, fontSize = 14.sp)
        }
        if (discovered.isEmpty()) {
            Text(
                "Aucun appareil pour l'instant. Assurez-vous que la balance est à portée.",
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        val (compatible, others) = discovered.partition { it.isCompatible }

        compatible.forEach { scale -> DeviceRow(scale, onClick = { onAssociate(scale) }) }

        if (others.isNotEmpty()) {
            Text(
                "Autres appareils détectés",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 16.dp, bottom = 2.dp)
            )
            others.forEach { scale -> DeviceRow(scale, onClick = null) }
        }

        OutlinedButton(onClick = onStopScan, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
            Text("Arrêter", color = TextSecondary)
        }
    }
}

/**
 * Pairing in progress.
 *
 * ⚠️ Engraving happens **before** the athlete steps on, and consumes a memory slot for good.
 * The screen says so rather than hiding it.
 */
@Composable
private fun Pairing(state: ScaleUiState) {
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
private fun DeviceRow(scale: DiscoveredScale, onClick: (() -> Unit)?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = scale.recognised?.displayName ?: scale.advertisedName,
                color = if (onClick != null) TextPrimary else TextSecondary,
                fontSize = 14.sp,
                fontWeight = if (onClick != null) FontWeight.SemiBold else FontWeight.Normal
            )
            Text(
                text = if (scale.recognised != null) scale.advertisedName else scale.deviceAddress,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
        Text(
            text = "${scale.signalStrengthDbm} dBm",
            color = if (onClick != null) ElectricCyan else TextSecondary,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun AssociatedScale(state: ScaleUiState, onForget: () -> Unit, onWeighIn: () -> Unit) {
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

        Button(
            onClick = onWeighIn,
            enabled = !state.isWeighing,
            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = Color.Black),
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        ) { Text(if (state.isWeighing) "Pesée en cours…" else "Lancer une pesée", fontWeight = FontWeight.Bold) }

        OutlinedButton(onClick = onForget, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text("Oublier cette balance", color = TextSecondary)
        }
    }
}

@Composable
private fun Failure(failure: SessionFailure) {
    Card {
        Text("Pesée interrompue", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Text(failureLabel(failure), color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        color = ElectricCyan,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun Card(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .background(SurfaceDark, RoundedCornerShape(14.dp))
            .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
            .padding(16.dp),
        content = content
    )
}
