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
internal fun NotAssociated(onStartScan: () -> Unit) {
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
internal fun Scanning(
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
internal fun DeviceRow(scale: DiscoveredScale, onClick: (() -> Unit)?) {
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
