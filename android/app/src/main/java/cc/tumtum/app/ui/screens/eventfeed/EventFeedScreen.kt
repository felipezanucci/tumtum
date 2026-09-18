package cc.tumtum.app.ui.screens.eventfeed

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cc.tumtum.app.R
import cc.tumtum.app.ui.Fmt
import cc.tumtum.app.ui.components.Avatar
import cc.tumtum.app.ui.components.MomentCard
import cc.tumtum.app.ui.components.OutlineBadge
import cc.tumtum.app.ui.nav.Routes
import cc.tumtum.app.ui.nav.appContainer
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType

/**
 * b6 — Feed do evento: todo mundo que estava no mesmo evento.
 * "64% bateram o próprio pico" — histórias, nunca ranking de BPM.
 */
@Composable
fun EventFeedScreen(nav: NavHostController) {
    val container = appContainer()
    val feed by container.social.eventFeed.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .background(TT.Paper)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding(),
    ) {
        // Cabeçalho ácido do evento
        Column(Modifier.fillMaxWidth().background(TT.Acid).statusBarsPadding().padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 22.dp)) {
            Text(
                stringResource(R.string.event_feed_back),
                style = TTType.Meta.copy(letterSpacing = 0.04.em),
                color = TT.Ink.copy(alpha = 0.6f),
                modifier = Modifier.clickable { nav.popBackStack() },
            )
            Spacer(Modifier.height(14.dp))
            Text(
                feed.eventName,
                style = TTType.ShoutSmall.copy(fontSize = 26.sp, lineHeight = 26.5.sp),
                color = TT.Ink,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                feed.venueDate,
                style = TTType.BodySmall.copy(fontWeight = FontWeight.Medium),
                color = TT.Ink.copy(alpha = 0.65f),
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(R.string.event_feed_shared, Fmt.thousands(feed.sharedCount)),
                    style = TTType.MetaSmall.copy(fontWeight = FontWeight.Bold),
                    color = TT.Acid,
                    modifier = Modifier.background(TT.Ink).padding(horizontal = 9.dp, vertical = 5.dp),
                )
                OutlineBadge(
                    stringResource(R.string.event_feed_peak, feed.collectivePeakLabel),
                    borderColor = TT.Ink.copy(alpha = 0.35f),
                    contentColor = TT.Ink,
                    hPad = 9.dp,
                    vPad = 5.dp,
                )
            }
        }

        Text(
            stringResource(R.string.event_feed_headline),
            style = TTType.ItemSub.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
            color = TT.Ink,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 6.dp),
        )
        Text(
            stringResource(R.string.event_feed_body, Fmt.thousands(feed.sharedCount), feed.samePeakPct),
            style = TTType.Footnote,
            color = TT.Gray70,
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        Column(
            Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            feed.moments.forEach { moment ->
                MomentCard(
                    moment = moment,
                    onToggleSenti = { container.social.toggleSenti(moment.id) },
                    onOpenProfile = { nav.navigate(Routes.profile(moment.user.handle)) },
                )
            }
            // Linhas compactas — frase + número, sem plate.
            feed.compactMoments.forEach { moment ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, TT.Gray10, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 13.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Avatar(moment.user.initials, moment.user.avatarSkin, size = 32.dp)
                    Text(
                        "“${moment.quote}”",
                        style = TTType.BodySmall.copy(fontStyle = FontStyle.Italic),
                        color = TT.Gray70,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        "${moment.bpm}",
                        style = TTType.NumberRow.copy(fontSize = 18.sp),
                        color = TT.Ink,
                    )
                }
            }
        }

        if (feed.userWasThere) {
            // Você tava lá — postar é sempre ativo, nunca automático.
            Row(
                Modifier
                    .padding(start = 24.dp, end = 24.dp, bottom = 18.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TT.Rose)
                    .padding(horizontal = 16.dp, vertical = 15.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.event_feed_you_were_there),
                    style = TTType.ItemSub.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                    color = TT.Ink,
                )
                Text(
                    stringResource(R.string.event_feed_post),
                    style = TTType.MetaSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                    color = TT.Rose,
                    modifier = Modifier
                        .background(TT.Ink)
                        .clickable { nav.navigate(Routes.You) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
        }
    }
}
