package cc.tumtum.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import cc.tumtum.app.domain.FeedMoment
import cc.tumtum.app.domain.Skin
import cc.tumtum.app.ui.theme.InstrumentSans
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType

/**
 * Linha do feed (b5/b6): o card que a pessoa escolheu compartilhar + uma frase.
 * Reação única — SENTI TB, toggle, uma vez por momento (§ Social).
 */
@Composable
fun MomentCard(
    moment: FeedMoment,
    onToggleSenti: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenProfile: (() -> Unit)? = null,
) {
    val plateBg = skinColor(moment.skin)
    val plateFg = if (moment.skin == Skin.BLACK) TT.Paper else TT.Ink
    val numColor = if (moment.skin == Skin.BLACK) TT.Rose else TT.Ink

    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, TT.Gray10, RoundedCornerShape(12.dp)),
    ) {
        // Cabeçalho
        Row(
            Modifier
                .fillMaxWidth()
                .let { m -> onOpenProfile?.let { m.clickable(onClick = it) } ?: m }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar(moment.user.initials, moment.user.avatarSkin, size = 32.dp)
            Column {
                Text(
                    "@${moment.user.handle}",
                    style = TTType.ItemSub.copy(fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold),
                    color = TT.Ink,
                )
                Text(
                    "${moment.eventName} · ${moment.whenLabel}",
                    style = TTType.ItemSub.copy(fontSize = 11.5.sp),
                    color = TT.Gray45,
                )
            }
        }
        // Plate do momento
        Column(Modifier.fillMaxWidth().background(plateBg).padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 14.dp)) {
            Text(moment.title, style = TTType.CardShout, color = plateFg)
            Text(
                "${moment.bpm}",
                style = TextStyle(
                    fontFamily = InstrumentSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 64.sp,
                    lineHeight = 54.sp,
                    letterSpacing = (-0.05).em,
                ),
                color = numColor,
                modifier = Modifier.padding(top = 10.dp),
            )
            Row(
                Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    moment.metaLabel,
                    style = TTType.Meta.copy(letterSpacing = 0.em),
                    color = plateFg,
                )
                Wordmark(width = 56.dp, onDark = moment.skin == Skin.BLACK)
            }
        }
        // Frase + SENTI TB
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "“${moment.quote}”",
                style = TTType.BodySmall.copy(fontStyle = FontStyle.Italic),
                color = TT.Gray70,
                modifier = Modifier.weight(1f),
            )
            SentiTbBadge(count = moment.sentiCount, active = moment.sentiByMe, onToggle = onToggleSenti)
        }
    }
}

@Composable
fun SentiTbBadge(count: Int, active: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val label = if (count > 0) "SENTI TB · $count" else "SENTI TB"
    if (active) {
        Text(
            label,
            style = TTType.MetaSmall.copy(fontWeight = FontWeight.Bold),
            color = TT.Ink,
            modifier = modifier
                .background(TT.Acid)
                .clickable(onClick = onToggle)
                .padding(horizontal = 9.dp, vertical = 5.dp),
        )
    } else {
        Text(
            label,
            style = TTType.MetaSmall,
            color = TT.Gray70,
            modifier = modifier
                .border(1.dp, TT.Gray10)
                .clickable(onClick = onToggle)
                .padding(horizontal = 9.dp, vertical = 5.dp),
        )
    }
}
