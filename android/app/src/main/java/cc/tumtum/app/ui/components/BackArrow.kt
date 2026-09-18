package cc.tumtum.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType

/**
 * The one way back, drawn the same everywhere: a bold arrow in the text
 * colour of the surface, inside a 44 dp touch target. Until 18/09 each
 * screen drew its own — 14 sp, grey, no padding — and Felipe could not
 * find it. On dark surfaces pass [onDark].
 */
@Composable
fun BackArrow(onClick: () -> Unit, onDark: Boolean = false, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "←",
            style = TTType.Body.copy(fontSize = 24.sp, fontWeight = FontWeight.SemiBold, lineHeight = 24.sp),
            color = if (onDark) TT.Paper else TT.Ink,
        )
    }
}
