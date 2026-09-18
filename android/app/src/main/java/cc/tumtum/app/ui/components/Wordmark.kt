package cc.tumtum.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import cc.tumtum.app.R

/**
 * O wordmark é sempre o asset (EULA da Chosmos proíbe imitação tipográfica).
 * Proporção 636:96.
 */
@Composable
fun Wordmark(width: Dp, onDark: Boolean = false, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(if (onDark) R.drawable.wordmark_white else R.drawable.wordmark_black),
        contentDescription = stringResource(R.string.wordmark_cd),
        modifier = modifier
            .width(width)
            .aspectRatio(636f / 96f),
    )
}
