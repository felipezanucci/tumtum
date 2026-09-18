package cc.tumtum.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.tumtum.app.ui.theme.InstrumentSans

/**
 * Plate opaco (manual): texto sobre cor mora em retângulo sólido,
 * nunca solto sobre imagem, nunca com sombra.
 */
@Composable
fun PlateText(
    text: String,
    background: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle(
        fontFamily = InstrumentSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
    ),
) {
    Text(
        text,
        style = style,
        color = contentColor,
        modifier = modifier
            .background(background)
            .padding(horizontal = 12.dp, vertical = 7.dp),
    )
}
