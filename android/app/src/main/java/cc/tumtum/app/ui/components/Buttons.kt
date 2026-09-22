package cc.tumtum.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType

/** Botão §3: raio 12dp, altura 56dp, label 16/600. */
enum class TTButtonStyle { Ink, Rose, Acid, Outline, OutlineOnDark, OutlineAcid }

@Composable
fun TTButton(
    text: String,
    style: TTButtonStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val bg = when (style) {
        TTButtonStyle.Ink -> TT.Ink
        TTButtonStyle.Rose -> TT.Rose
        TTButtonStyle.Acid -> TT.Acid
        TTButtonStyle.Outline, TTButtonStyle.OutlineOnDark, TTButtonStyle.OutlineAcid -> Color.Transparent
    }
    // Nunca texto branco sobre rosa/amarelo (manual): label preto nos dois.
    val fg = when (style) {
        TTButtonStyle.Ink -> TT.Paper
        TTButtonStyle.Rose, TTButtonStyle.Acid -> TT.Ink
        TTButtonStyle.Outline -> TT.Ink
        TTButtonStyle.OutlineOnDark -> TT.Paper
        // Toxic Yellow on black is 18.97:1 — the loudest thing the palette has
        // on a dark surface, which is what a secondary action needs when the
        // first build made it a line of small caps nobody saw (22/09).
        TTButtonStyle.OutlineAcid -> TT.Acid
    }
    val borderColor = when (style) {
        TTButtonStyle.Outline -> TT.Gray10
        TTButtonStyle.OutlineOnDark -> TT.Ink600
        TTButtonStyle.OutlineAcid -> TT.Acid
        else -> null
    }
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(shape)
            .background(bg)
            .let { m -> borderColor?.let { m.border(1.dp, it, shape) } ?: m }
            .clickable(enabled = enabled, onClick = onClick)
            .alpha(if (enabled) 1f else 0.4f),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = TTType.Button, color = fg)
    }
}
