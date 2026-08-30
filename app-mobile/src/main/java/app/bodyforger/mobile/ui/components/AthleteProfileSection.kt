package app.bodyforger.mobile.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.core.model.AthleteProfile
import app.bodyforger.core.model.BiologicalSex
import app.bodyforger.mobile.ui.theme.ElectricCyan
import app.bodyforger.mobile.ui.theme.SurfaceBorder
import app.bodyforger.mobile.ui.theme.TextMuted
import app.bodyforger.mobile.ui.theme.TextPrimary
import app.bodyforger.mobile.ui.theme.TextSecondary
import java.time.LocalDate

/**
 * The athlete's own measurements, which the scale needs before it can weigh anyone.
 *
 * Nothing here has a default: an unset field stays visibly empty rather than showing a
 * plausible number the athlete would never think to correct.
 */
@Composable
fun AthleteProfileSection(
    profile: AthleteProfile,
    onSexSelected: (BiologicalSex) -> Unit,
    onBirthDateChanged: (String?) -> Unit,
    onHeightChanged: (Double?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionTitle("PROFIL DE MESURE")

        Card {
            SexSelector(profile.sex, onSexSelected)
            Spacer(Modifier.height(14.dp))
            BirthDateField(profile, onBirthDateChanged)
            Spacer(Modifier.height(14.dp))
            HeightField(profile.heightCm, onHeightChanged)

            if (!profile.isComplete) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Complétez ces trois champs : la balance en a besoin pour calculer " +
                        "la masse grasse. Sans eux, aucune pesée n'est lancée.",
                    color = TextMuted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
private fun SexSelector(selected: BiologicalSex?, onSelected: (BiologicalSex) -> Unit) {
    FieldLabel("Sexe biologique")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        BiologicalSex.entries.forEach { sex ->
            val isSelected = sex == selected
            Button(
                onClick = { onSelected(sex) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) ElectricCyan else SurfaceBorder,
                    contentColor = if (isSelected) Color.Black else TextSecondary
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).height(40.dp)
            ) {
                Text(
                    text = if (sex == BiologicalSex.MALE) "Homme" else "Femme",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * The three parts of a date of birth, typed one at a time.
 *
 * The parts are held here rather than derived from the profile: until all three are entered
 * they form no date, so a field reading back from the profile would clear itself at every
 * keystroke and never fill.
 */
@Composable
private fun BirthDateField(profile: AthleteProfile, onChanged: (String?) -> Unit) {
    var day by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }

    // Ce que la base renvoie fait foi tant que l'athlète n'a rien tapé.
    LaunchedEffect(profile.birthDateIso) {
        profile.birthDate?.let {
            day = it.dayOfMonth.toString()
            month = it.monthValue.toString()
            year = it.year.toString()
        }
    }

    fun commit() = onChanged(dateFrom(day, month, year))

    val age = profile.ageYearsOn(LocalDate.now())
    FieldLabel(if (age != null) "Date de naissance — $age ans" else "Date de naissance")

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CompactNumberInput(
            value = day,
            onValueChange = { day = it; commit() },
            placeholder = "JJ",
            maxLength = DAY_DIGITS,
            modifier = Modifier.width(52.dp)
        )
        CompactNumberInput(
            value = month,
            onValueChange = { month = it; commit() },
            placeholder = "MM",
            maxLength = MONTH_DIGITS,
            modifier = Modifier.width(52.dp)
        )
        CompactNumberInput(
            value = year,
            onValueChange = { year = it; commit() },
            placeholder = "AAAA",
            maxLength = YEAR_DIGITS,
            modifier = Modifier.width(72.dp)
        )
    }
}

@Composable
private fun HeightField(heightCm: Double?, onChanged: (Double?) -> Unit) {
    FieldLabel("Taille (cm)")
    CompactNumberInput(
        value = heightCm?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }.orEmpty(),
        onValueChange = { onChanged(it.toDoubleOrNull()) },
        placeholder = "cm",
        isDecimal = true,
        modifier = Modifier.width(88.dp)
    )
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        color = TextPrimary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

/** Digits each part accepts, so a slip of the thumb cannot make a five-digit year. */
private const val DAY_DIGITS = 2
private const val MONTH_DIGITS = 2
private const val YEAR_DIGITS = 4

/**
 * The date the three fields spell out, or null while they spell nothing valid.
 *
 * A part still missing, a date that does not exist — 31 February — or one not yet reached
 * yields null, so the profile stays incomplete rather than accepting something it would later
 * turn into a wrong age.
 */
private fun dateFrom(day: String, month: String, year: String): String? {
    val d = day.toIntOrNull() ?: return null
    val m = month.toIntOrNull() ?: return null
    val y = year.toIntOrNull() ?: return null
    return runCatching { LocalDate.of(y, m, d) }
        .getOrNull()
        ?.takeIf { it.isBefore(LocalDate.now()) }
        ?.toString()
}
