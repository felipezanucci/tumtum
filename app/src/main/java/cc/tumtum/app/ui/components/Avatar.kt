package cc.tumtum.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.tumtum.app.domain.Skin
import cc.tumtum.app.ui.theme.InstrumentSans
import cc.tumtum.app.ui.theme.TT

/** Avatar de iniciais. Fundo nas quatro peles; texto sempre com contraste medido. */
@Composable
fun Avatar(initials: String, skin: Skin, size: Dp = 36.dp, modifier: Modifier = Modifier) {
    val bg = when (skin) {
        Skin.PINK -> TT.Rose
        Skin.BLACK -> TT.Ink
        Skin.YELLOW -> TT.Acid
        Skin.WHITE -> TT.Paper
    }
    val fg = if (skin == Skin.BLACK) TT.Paper else TT.Ink
    Box(
        modifier = modifier.size(size).clip(CircleShape).background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            initials,
            style = TextStyle(
                fontFamily = InstrumentSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = (size.value * 0.36f).sp,
                color = fg,
            ),
        )
    }
}

fun skinColor(skin: Skin): Color = when (skin) {
    Skin.PINK -> TT.Rose
    Skin.BLACK -> TT.Ink
    Skin.YELLOW -> TT.Acid
    Skin.WHITE -> TT.Paper
}
