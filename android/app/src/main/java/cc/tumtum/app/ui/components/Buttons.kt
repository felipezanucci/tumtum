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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType

/** Botão §3: raio 12dp, altura 56dp, label 16/600. */
enum class TTButtonStyle { Ink, Rose, Acid, Outline, OutlineOnDark, OutlineAcid }

/**
 * The product's button. **It never fades.**
 *
 * Until 2026-09-22 a disabled button was drawn at 40% alpha. TumTum Pink at
 * 40% over white is roughly #FFD4DE — a wash that, on a phone in real light,
 * cannot be told from the page. Felipe raised it more than once ("esse botão
 * fica rosa só quando a gente clica, mas ele tem que ficar rosa o tempo
 * inteiro"): the front door's main action looked absent, or already used, on
 * a form that was ready to send. The app stating something false about its
 * own state, drawn in colour.
 *
 * So the fill is always the fill. "Not yet" stops being a colour and becomes
 * a sentence: when [enabled] is false a tap goes to [onDeclined], and the
 * screen says what is missing next to where it is missing (21/09: *a tap the
 * app declines is said, where the eye already is*). Without [onDeclined] the
 * tap is simply swallowed — right for an action already in flight, whose
 * label already says so ("Entrando…"), and nothing else.
 */
@Composable
fun TTButton(
    text: String,
    style: TTButtonStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onDeclined: (() -> Unit)? = null,
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
            .clickable(enabled = enabled || onDeclined != null) {
                if (enabled) onClick() else onDeclined?.invoke()
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = TTType.Button, color = fg)
    }
}
