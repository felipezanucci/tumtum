package cc.tumtum.app.ui.screens.you

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cc.tumtum.app.R
import cc.tumtum.app.domain.Night
import cc.tumtum.app.domain.Skin
import cc.tumtum.app.ui.Fmt
import cc.tumtum.app.ui.components.Avatar
import cc.tumtum.app.ui.components.Badge
import cc.tumtum.app.ui.components.BpmCurve
import cc.tumtum.app.ui.components.OutlineBadge
import cc.tumtum.app.ui.components.Wordmark
import cc.tumtum.app.ui.nav.Routes
import cc.tumtum.app.ui.nav.appContainer
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType
import java.time.Duration

/**
 * VOCÊ — a1 Suas noites, com as entradas de galeria, perfil e configurações
 * (§5: suas noites, a galeria de sentimentos, o perfil público, configurações).
 */
@Composable
fun YouScreen(nav: NavHostController) {
    val container = appContainer()
    val nights by container.nights.nights().collectAsStateWithLifecycle(initialValue = emptyList())
    val user by container.prefs.state.collectAsStateWithLifecycle(initialValue = null)
    val account = user?.account

    Column(Modifier.fillMaxSize().background(TT.Paper).statusBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Wordmark(width = 92.dp)
            cc.tumtum.app.ui.components.UserAvatar(
                account?.initials ?: "TT",
                Skin.BLACK,
                photoPath = user?.avatarPath,
                modifier = Modifier.clickable {
                    account?.let { nav.navigate(Routes.profile(it.username)) }
                },
            )
        }
        Text(
            stringResource(R.string.nights_title),
            style = TTType.Title,
            color = TT.Ink,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 26.dp, bottom = 4.dp),
        )
        Row(
            Modifier.padding(horizontal = 24.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlineBadge(
                stringResource(R.string.you_gallery).uppercase(),
                contentColor = TT.Gray70,
                modifier = Modifier.clickable { nav.navigate(Routes.Gallery) },
            )
            OutlineBadge(
                stringResource(R.string.you_settings).uppercase(),
                contentColor = TT.Gray70,
                modifier = Modifier.clickable { nav.navigate(Routes.Settings) },
            )
        }

        if (nights.isEmpty()) {
            Column(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 32.dp), verticalArrangement = Arrangement.Center) {
                Text(
                    stringResource(R.string.empty_title),
                    style = TTType.Shout.copy(fontSize = 34.sp, lineHeight = 35.sp),
                    color = TT.Ink,
                )
                Spacer(Modifier.height(10.dp))
                Text(stringResource(R.string.empty_subtitle), style = TTType.Body.copy(fontSize = 19.sp), color = TT.Gray45)
            }
        } else {
            LazyColumn(
                Modifier.weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 24.dp, end = 24.dp, top = 6.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(nights, key = { it.id }) { night ->
                    NightCard(night) { nav.navigate(Routes.reveal(night.id)) }
                }
            }
        }
    }
}

@Composable
private fun NightCard(night: Night, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    // Noite lacrada (trava da revela): a lista também não vaza número nem curva.
    val locked = cc.tumtum.app.domain.RevealLock.isLocked(night.revealAt)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, TT.Gray10, shape)
            .clickable(onClick = onClick)
            .padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    Fmt.date(night.date),
                    style = TTType.MetaSmall.copy(letterSpacing = 0.14.em),
                    color = TT.Gray55,
                )
                Text(night.eventName, style = TTType.ItemTitle, color = TT.Ink)
            }
            if (locked) {
                Badge(stringResource(R.string.locked_badge))
            } else {
            Row(verticalAlignment = Alignment.Bottom) {
                Text("${night.peakBpm}", style = TTType.NumberLarge, color = TT.Ink)
                Spacer(Modifier.width(5.dp))
                Text(
                    "bpm",
                    style = TTType.ItemSub.copy(fontSize = 12.sp),
                    color = TT.Gray55,
                    modifier = Modifier.padding(bottom = 5.dp),
                )
            }
            }
        }
        if (locked) {
            Text(
                stringResource(R.string.locked_row_hint),
                style = TTType.Footnote,
                color = TT.Gray45,
            )
        } else {
        BpmCurve(
            samples = night.samples,
            windowStart = night.startAt,
            windowEnd = night.endAt,
            lineColor = TT.DataLineOnLight,
            markerColor = TT.DataMarkerOnLight,
            gapColor = TT.Gray25,
            strokeWidth = 1.5.dp,
            markerRadius = 3.5.dp,
            modifier = Modifier.fillMaxWidth().height(44.dp),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Badge(stringResource(R.string.nights_moments, night.momentCount))
            OutlineBadge(Fmt.durationChip(Duration.between(night.startAt, night.endAt)))
            if (night.coveragePct < 90) {
                OutlineBadge(stringResource(R.string.nights_captured_pct, night.coveragePct))
            }
        }
        }
    }
}
