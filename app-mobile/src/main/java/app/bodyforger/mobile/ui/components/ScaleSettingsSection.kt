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
            "Tapotez la balance du pied pour la réveiller, puis lancez la recherche. " +
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
                "Aucun appareil pour l'instant. Tapotez la balance du pied : elle ne s'annonce que quelques secondes.",
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        val (compatible, others) = discovered.partition { it.isCompatible }

        compatible.forEach { scale -> DeviceRow(scale, onClick = { onAssociate(scale) }) }

        if (others.isNotEmpty()) {
            Text(
                // Montrés pour que l'on voie ce qui s'annonce vraiment : une balance présente
                // sous un nom inattendu se repère ici, et nulle part ailleurs.
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
 * L'appairage en cours.
 *
 * ⚠️ La gravure du profil dans la mémoire de la balance a lieu **avant** que l'athlète ne
 * monte, et consomme un emplacement définitivement. L'écran le dit plutôt que de le taire.
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
            "La pesée de calibration fait partie de l'appairage : sans elle, l'association ne sera pas enregistrée.",
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

/**
 * Une ligne d'appareil.
 *
 * Un appareil non reconnu s'affiche sans être cliquable : le montrer aide au diagnostic,
 * mais tenter une gravure dessus reviendrait à écrire au hasard dans un matériel inconnu.
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
