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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.core.model.AthleteProfile
import app.bodyforger.core.model.BiologicalSex
import app.bodyforger.mobile.R
import app.bodyforger.mobile.ui.theme.ElectricCyan
import app.bodyforger.mobile.ui.theme.NeonLime
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
/**
 * The athlete's own measurements, which the scale needs before it can weigh anyone.
 *
 * The form is held here and written on [onSave] rather than at every keystroke: a half-typed
 * height is not a height, and the athlete gets a moment where the app says it took the answer.
 */
@Composable
fun AthleteProfileForm(
    profile: AthleteProfile,
    onSave: (sex: BiologicalSex?, birthDateIso: String?, heightCm: Double?) -> Unit,
    modifier: Modifier = Modifier
) {
    var sex by remember(profile.sex) { mutableStateOf(profile.sex) }
    var birthDateIso by remember(profile.birthDateIso) { mutableStateOf(profile.birthDateIso) }
    var heightText by remember(profile.heightCm) {
        mutableStateOf(profile.heightCm?.let { formatHeight(it) }.orEmpty())
    }

    val height = heightText.toDoubleOrNull()
    val typed = profile.copy(sex = sex, birthDateIso = birthDateIso, heightCm = height)

    Column(modifier = modifier.fillMaxWidth()) {
        SexSelector(sex) { sex = it }
        Spacer(Modifier.height(14.dp))
        BirthDateField(typed) { birthDateIso = it }
        Spacer(Modifier.height(14.dp))
        HeightField(heightText) { heightText = it }

        Spacer(Modifier.height(18.dp))
        Button(
            onClick = { onSave(sex, birthDateIso, height) },
            enabled = typed.isComplete,
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonLime,
                contentColor = Color.Black,
                disabledContainerColor = SurfaceBorder,
                disabledContentColor = TextMuted
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(46.dp)
        ) {
            Text(
                text = stringResource(
                    if (typed.isComplete) R.string.profile_save else R.string.profile_incomplete
                ),
                fontSize = 13.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun SexSelector(selected: BiologicalSex?, onSelected: (BiologicalSex) -> Unit) {
    FieldLabel(stringResource(R.string.profile_sex))
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
                    text = stringResource(
                        if (sex == BiologicalSex.MALE) R.string.profile_sex_male else R.string.profile_sex_female
                    ),
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
    FieldLabel(
        if (age != null) stringResource(R.string.profile_birth_date_with_age, age)
        else stringResource(R.string.profile_birth_date)
    )

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CompactNumberInput(
            value = day,
            onValueChange = { day = it; commit() },
            placeholder = stringResource(R.string.profile_day),
            maxLength = DAY_DIGITS,
            modifier = Modifier.width(52.dp)
        )
        CompactNumberInput(
            value = month,
            onValueChange = { month = it; commit() },
            placeholder = stringResource(R.string.profile_month),
            maxLength = MONTH_DIGITS,
            modifier = Modifier.width(52.dp)
        )
        CompactNumberInput(
            value = year,
            onValueChange = { year = it; commit() },
            placeholder = stringResource(R.string.profile_year),
            maxLength = YEAR_DIGITS,
            modifier = Modifier.width(72.dp)
        )
    }
}

@Composable
private fun HeightField(value: String, onChanged: (String) -> Unit) {
    FieldLabel(stringResource(R.string.profile_height))
    CompactNumberInput(
        value = value,
        onValueChange = onChanged,
        placeholder = "cm",
        isDecimal = true,
        modifier = Modifier.width(88.dp)
    )
}

private fun formatHeight(heightCm: Double): String =
    if (heightCm % 1.0 == 0.0) heightCm.toInt().toString() else heightCm.toString()

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
