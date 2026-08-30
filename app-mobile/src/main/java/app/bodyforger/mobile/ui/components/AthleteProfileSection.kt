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

@Composable
private fun BirthDateField(profile: AthleteProfile, onChanged: (String?) -> Unit) {
    val birthDate = profile.birthDate
    val age = profile.ageYearsOn(LocalDate.now())

    FieldLabel(if (age != null) "Date de naissance — $age ans" else "Date de naissance")
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        DatePart(
            value = birthDate?.dayOfMonth,
            placeholder = "JJ",
            width = 52.dp,
            onChange = { onChanged(recomposeDate(birthDate, day = it)) }
        )
        DatePart(
            value = birthDate?.monthValue,
            placeholder = "MM",
            width = 52.dp,
            onChange = { onChanged(recomposeDate(birthDate, month = it)) }
        )
        DatePart(
            value = birthDate?.year,
            placeholder = "AAAA",
            width = 72.dp,
            onChange = { onChanged(recomposeDate(birthDate, year = it)) }
        )
    }
}

@Composable
private fun DatePart(value: Int?, placeholder: String, width: androidx.compose.ui.unit.Dp, onChange: (Int?) -> Unit) {
    CompactNumberInput(
        value = value?.toString().orEmpty(),
        onValueChange = { onChange(it.toIntOrNull()) },
        placeholder = placeholder,
        modifier = Modifier.width(width)
    )
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

/**
 * Rebuilds a birth date one field at a time, as the athlete types.
 *
 * A part still missing, or a date that does not exist — 31 February, a year not yet reached —
 * yields null, so the profile stays incomplete rather than accepting a date it will later
 * turn into a wrong age.
 */
private fun recomposeDate(
    current: LocalDate?,
    day: Int? = current?.dayOfMonth,
    month: Int? = current?.monthValue,
    year: Int? = current?.year
): String? {
    if (day == null || month == null || year == null) return null
    return runCatching { LocalDate.of(year, month, day) }
        .getOrNull()
        ?.takeIf { it.isBefore(LocalDate.now()) }
        ?.toString()
}
