package app.bodyforger.mobile.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import java.time.LocalDate
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import app.bodyforger.mobile.ui.components.AthleteProfileSection
import app.bodyforger.mobile.profile.AthleteProfileViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import app.bodyforger.core.model.BiaProfile
import app.bodyforger.mobile.scale.ScaleViewModel
import app.bodyforger.mobile.ui.components.ScaleSettingsSection
import app.bodyforger.mobile.ui.theme.Obsidian
import app.bodyforger.mobile.ui.theme.TextPrimary
import app.bodyforger.mobile.ui.theme.TextSecondary

/**
 * Application settings.
 *
 * First section: the connected scale. Naming a profile and exporting data will follow — #34.
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    scaleViewModel: ScaleViewModel = koinViewModel(),
    profileViewModel: AthleteProfileViewModel = koinViewModel()
) {
    val state by scaleViewModel.state.collectAsState()
    val profile by profileViewModel.profile.collectAsState()
    // Sans profil complet, la balance calculerait une masse grasse a partir de rien.
    val measurementProfile = profile.biaProfileOn(LocalDate.now())

    // Depuis Android 12, le scan et la connexion ont leurs propres permissions ; avant, le
    // scan passait par la localisation faute de mieux.
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
    } else {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }
    val requestPermissions = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        // Sans permission, inutile de lancer un scan qui ne verrait rien.
        if (granted.values.all { it }) scaleViewModel.startScan()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
            // sans cette marge.
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = TextPrimary)
            }
            Text(
                text = "PARAMÈTRES",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Text(
            text = "Identifiant de mesure : ${state.huid ?: "…"}",
            color = TextSecondary,
            fontSize = 11.sp,
            modifier = Modifier.padding(start = 12.dp, bottom = 20.dp)
        )

        AthleteProfileSection(
            profile = profile,
            onSexSelected = { profileViewModel.save(it, profile.birthDateIso, profile.heightCm) },
            onBirthDateChanged = { profileViewModel.save(profile.sex, it, profile.heightCm) },
            onHeightChanged = { profileViewModel.save(profile.sex, profile.birthDateIso, it) }
        )

        Spacer(Modifier.height(20.dp))

        ScaleSettingsSection(
            state = state,
            measurementProfile = measurementProfile,
            onStartScan = { requestPermissions.launch(permissions) },
            onStopScan = scaleViewModel::stopScan,
            onAssociate = { scale -> measurementProfile?.let { scaleViewModel.associate(scale, it) } },
            onForget = scaleViewModel::forgetScale,
            onWeighIn = { measurementProfile?.let { scaleViewModel.weighIn(it) } }
        )
    }
}
