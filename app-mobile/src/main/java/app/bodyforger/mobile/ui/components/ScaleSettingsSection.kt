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
import app.bodyforger.core.ble.AthleteInstruction
import app.bodyforger.core.ble.DiscoveredScale
import app.bodyforger.core.ble.SessionFailure
import app.bodyforger.mobile.scale.ScaleUiState
import app.bodyforger.mobile.ui.theme.ElectricCyan
import app.bodyforger.mobile.ui.theme.SurfaceBorder
import app.bodyforger.mobile.ui.theme.SurfaceDark
import app.bodyforger.mobile.ui.theme.TextPrimary
import app.bodyforger.mobile.ui.theme.TextSecondary

/**
 * La section Balance des paramètres : associer un matériel, puis peser.
 *
 * L'appairage se fait **une seule fois**. Tant qu'une Association existe, la pesée s'y
 * rattache directement — c'est le Mode 2 acté en #7, et l'appairage ne se rejoue jamais.
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
            state.association != null -> AssociatedScale(state, onForget, onWeighIn)
            state.isScanning -> Scanning(state.discovered, onAssociate, onStopScan)
            else -> NotAssociated(onStartScan)
        }

        state.failure?.let { Failure(it) }
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
            "Tapotez la balance du pied pour la réveiller, puis lancez la recherche.",
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
                "Aucune balance pour l'instant. Tapotez-la du pied : elle ne s'annonce que quelques secondes.",
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        discovered.forEach { scale ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .clickable { onAssociate(scale) },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(scale.recognised.displayName, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(scale.advertisedName, color = TextSecondary, fontSize = 11.sp)
                }
                Text("${scale.signalStrengthDbm} dBm", color = ElectricCyan, fontSize = 12.sp)
            }
        }
        OutlinedButton(onClick = onStopScan, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
            Text("Arrêter", color = TextSecondary)
        }
    }
}

@Composable
private fun AssociatedScale(state: ScaleUiState, onForget: () -> Unit, onWeighIn: () -> Unit) {
    Card {
        Text(state.association!!.advertisedName, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        Text("Associée · ${state.association.deviceAddress}", color = TextSecondary, fontSize = 11.sp)

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

/** Les consignes viennent du vocabulaire commun : l'interface se contente de les rendre. */
private fun instructionLabel(instruction: AthleteInstruction): String = when (instruction) {
    AthleteInstruction.TAP_SCALE_TO_WAKE -> "Tapotez la balance du pied"
    AthleteInstruction.STAY_OFF_PLATFORM -> "Restez hors du plateau"
    AthleteInstruction.STEP_ON_BAREFOOT -> "Montez pieds nus sur la balance"
    AthleteInstruction.GRIP_HANDLE -> "Saisissez la poignée des deux mains"
    AthleteInstruction.STEP_OFF -> "Descendez du plateau"
}

private fun failureLabel(failure: SessionFailure): String = when (failure) {
    SessionFailure.DEVICE_NOT_FOUND -> "Balance introuvable. Tapotez-la pour la réveiller."
    SessionFailure.CONNECTION_LOST -> "Liaison perdue en cours de séquence."
    SessionFailure.REJECTED_BY_DEVICE -> "La balance a refusé la connexion."
    SessionFailure.TIMED_OUT -> "Aucune mesure : la pesée n'a pas abouti à temps."
    SessionFailure.NOT_ASSOCIATED -> "Aucune balance associée."
    SessionFailure.DEVICE_ERROR -> "La balance a signalé une erreur."
}
