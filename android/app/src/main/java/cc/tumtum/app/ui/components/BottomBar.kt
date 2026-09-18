package cc.tumtum.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import cc.tumtum.app.R
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType

enum class TTTab { Feed, Live, You }

/**
 * Navegação §5: três zonas, barra fixa, label sempre visível, sem ícone inventado.
 * AO VIVO mora no centro como pílula rosa. Nada de gaveta, nada de FAB.
 */
@Composable
fun TTBottomBar(current: TTTab, onSelect: (TTTab) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.background(TT.Paper)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(TT.Gray10))
        Row(
            Modifier
                .fillMaxWidth()
                .height(64.dp),
        ) {
            TabLabel(stringResource(R.string.nav_feed), current == TTTab.Feed, Modifier.weight(1f)) { onSelect(TTTab.Feed) }
            Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.nav_live),
                    style = TTType.MetaSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.08.em),
                    color = TT.Ink,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(TT.Rose)
                        .clickable { onSelect(TTTab.Live) }
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                )
            }
            TabLabel(stringResource(R.string.nav_you), current == TTTab.You, Modifier.weight(1f)) { onSelect(TTTab.You) }
        }
        Box(Modifier.fillMaxWidth().background(TT.Paper).navigationBarsPadding())
    }
}

@Composable
private fun TabLabel(text: String, active: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier
                .size(width = 20.dp, height = 3.dp)
                .background(if (active) TT.Ink else Color.Transparent),
        )
        Box(Modifier.height(5.dp))
        Text(
            text,
            style = TTType.MetaSmall.copy(
                letterSpacing = 0.1.em,
                fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold,
            ),
            color = if (active) TT.Ink else TT.Gray45,
        )
    }
}
