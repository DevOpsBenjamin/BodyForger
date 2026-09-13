package app.bodyforger.mobile.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.mobile.R
import app.bodyforger.mobile.ui.theme.ElectricCyan
import app.bodyforger.mobile.ui.theme.NeonLime
import app.bodyforger.mobile.ui.theme.SurfaceBorder
import app.bodyforger.mobile.ui.theme.TextMuted
import app.bodyforger.mobile.ui.theme.TextPrimary

/**
 * What the athlete calls themselves.
 *
 * Optional: it only names the data on screen. Left empty, everything reads as anonymous.
 */
@Composable
fun AthleteIdentityForm(
    name: String?,
    onSave: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var typed by remember(name) { mutableStateOf(name.orEmpty()) }

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = typed,
            onValueChange = { typed = it },
            singleLine = true,
            label = { Text(stringResource(R.string.settings_athlete_name), fontSize = 12.sp) },
            placeholder = { Text(stringResource(R.string.settings_athlete_anonymous), color = TextMuted) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = ElectricCyan,
                unfocusedBorderColor = SurfaceBorder,
                focusedLabelColor = ElectricCyan,
                unfocusedLabelColor = TextMuted,
                cursorColor = NeonLime
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.settings_athlete_hint),
            color = TextMuted,
            fontSize = 11.sp,
            lineHeight = 15.sp
        )

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { onSave(typed.trim().takeIf { it.isNotEmpty() }) },
            colors = ButtonDefaults.buttonColors(containerColor = NeonLime, contentColor = Color.Black),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(46.dp)
        ) {
            Text(stringResource(R.string.profile_save), fontSize = 13.sp, fontWeight = FontWeight.Black)
        }
    }
}
