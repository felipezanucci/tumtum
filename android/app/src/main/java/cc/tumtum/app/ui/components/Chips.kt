package cc.tumtum.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType

/** Chip/badge/meta-label: raio 0 — retângulo seco (§3). */
@Composable
fun Badge(
    text: String,
    modifier: Modifier = Modifier,
    background: Color = TT.Acid,
    contentColor: Color = TT.Ink,
    hPad: Dp = 8.dp,
    vPad: Dp = 4.dp,
) {
    Text(
        text,
        style = TTType.MetaSmall,
        color = contentColor,
        modifier = modifier
            .background(background)
            .padding(horizontal = hPad, vertical = vPad),
    )
}

@Composable
fun OutlineBadge(
    text: String,
    modifier: Modifier = Modifier,
    borderColor: Color = TT.Gray10,
    contentColor: Color = TT.Gray55,
    hPad: Dp = 8.dp,
    vPad: Dp = 4.dp,
) {
    Text(
        text,
        style = TTType.MetaSmall,
        color = contentColor,
        modifier = modifier
            .border(1.dp, borderColor)
            .padding(horizontal = hPad, vertical = vPad),
    )
}

/** Tribo (b2): retângulo seco, ácido quando selecionado. */
@Composable
fun TribeChip(text: String, selected: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    if (selected) {
        Text(
            text,
            style = TTType.Meta,
            color = TT.Ink,
            modifier = modifier
                .background(TT.Acid)
                .clickable(onClick = onToggle)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        )
    } else {
        Text(
            text,
            style = TTType.Meta,
            color = TT.Gray70,
            modifier = modifier
                .border(1.dp, TT.Gray25)
                .clickable(onClick = onToggle)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        )
    }
}
