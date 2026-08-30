package app.bodyforger.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.mobile.ui.theme.NeonLime
import app.bodyforger.mobile.ui.theme.SurfaceBorder
import app.bodyforger.mobile.ui.theme.SurfaceElevated
import app.bodyforger.mobile.ui.theme.TextMuted
import app.bodyforger.mobile.ui.theme.TextPrimary

/**
 * A small numeric field that behaves the way a training log should.
 *
 * Focusing it selects what is there, so typing replaces the value instead of appending to it:
 * going from 90 to 100 is three keystrokes, not two deletions and three keystrokes.
 *
 * [maxLength] caps what can be typed, so a slip of the thumb cannot make a five-digit year.
 *
 * The text being typed is held here, not by the caller. A caller that only accepts parseable
 * values would rewrite the field on every keystroke, making it impossible to clear — the digits
 * would grow back as fast as they were deleted. An emptied field simply reports nothing until
 * it reads as a number again.
 */
@Composable
fun CompactNumberInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "-",
    isDecimal: Boolean = false,
    maxLength: Int? = null,
    modifier: Modifier = Modifier,
    height: Dp = 38.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    var typed by remember { mutableStateOf(TextFieldValue(value)) }
    var lastSeenValue by remember { mutableStateOf(value) }

    // On n'adopte la valeur du modèle que lorsqu'elle bouge d'elle-même — un report de la
    // dernière séance. Un appelant qui refuse une saisie illisible laisse sa valeur inchangée,
    // et le champ garde alors ce qui est tapé, y compris vide.
    if (value != lastSeenValue) {
        lastSeenValue = value
        if (value != typed.text) typed = TextFieldValue(value, TextRange(value.length))
    }

    // Le tap qui donne le focus place aussi le curseur, et cette pose arrive après la
    // sélection. Sans en neutraliser exactement une, un tap sur deux ne sélectionnait rien.
    var awaitingEntryTap by remember { mutableStateOf(false) }

    LaunchedEffect(isFocused) {
        typed = if (isFocused) {
            awaitingEntryTap = true
            typed.copy(selection = TextRange(0, typed.text.length))
        } else {
            // Le surlignage se dessine sans le focus : sans ce repli, tous les champs déjà
            // visités resteraient surlignés en même temps.
            typed.copy(selection = TextRange(typed.text.length))
        }
    }

    BasicTextField(
        value = typed,
        onValueChange = { edited ->
            val isEntryTap = awaitingEntryTap && edited.text == typed.text
            awaitingEntryTap = false
            // Le repositionnement du tap d'entrée est ignoré ; tout tap ultérieur passe, pour
            // que l'athlète puisse quand même poser son curseur où il veut.
            if (isEntryTap) return@BasicTextField

            // La limite est tenue ici, sinon le texte affiché dépasserait la valeur retenue.
            if (maxLength != null && edited.text.length > maxLength) return@BasicTextField
            typed = edited
            if (edited.text != value) onValueChange(edited.text)
        },
        interactionSource = interactionSource,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isDecimal) KeyboardType.Decimal else KeyboardType.Number
        ),
        textStyle = TextStyle(
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        ),
        cursorBrush = SolidColor(NeonLime),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceElevated)
                    .border(
                        1.dp,
                        if (isFocused) NeonLime else SurfaceBorder,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                if (typed.text.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = TextMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
                innerTextField()
            }
        },
        modifier = modifier
    )
}
