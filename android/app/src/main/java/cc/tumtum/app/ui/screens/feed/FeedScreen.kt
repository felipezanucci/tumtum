package cc.tumtum.app.ui.screens.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cc.tumtum.app.R
import cc.tumtum.app.data.api.ServerEvent
import cc.tumtum.app.domain.Skin
import cc.tumtum.app.ui.components.AccountCorner
import cc.tumtum.app.ui.components.Wordmark
import cc.tumtum.app.ui.nav.Routes
import cc.tumtum.app.ui.nav.appContainer
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType
import java.time.Instant

/**
 * b5 — a primeira tela: os rolês, e o de cada um leva ao seu feed.
 *
 * **Rewritten 2026-09-22.** Until that day this was the app's start
 * destination showing `FakeSocialRepository`: a banner announcing "Hoje:
 * Taylor Swift · 3 amigos confirmados" and moments by Mariana Alves and
 * Rodrigo Costa, people who do not exist, carrying heart rates nobody
 * measured. It had been on the Play internal testing track since 18/09.
 *
 * There is no friends feed, by decision: the feed is **per event**, so that
 * only the people who were somewhere together can talk about it. This screen
 * is therefore the list of events, and each one is a door.
 */
@Composable
fun FeedScreen(nav: NavHostController) {
    val container = appContainer()
    val user by container.prefs.state.collectAsStateWithLifecycle(initialValue = null)
    var events by remember { mutableStateOf<List<ServerEvent>?>(null) }
    var failed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // An empty list and a list that did not load are different claims,
        // and this screen has to be able to tell them apart.
        runCatching { container.api.listEvents() }
            .onSuccess { events = it; failed = false }
            .onFailure { events = null; failed = true }
    }

    // #30, 22/09 — Felipe: "tá muito cinza, preta e branca… mais colorida,
    // mais viva." The manual's digital default is a black canvas with Pink as
    // the main emphasis and Toxic Yellow as the second explosion, and this
    // screen had neither: white page, grey rows, one yellow row when
    // something happened to be live. Pink on black is half as loud as the old
    // lime, so the emphasis comes from surface and scale — a full Pink block
    // for the night that is on now, yellow doors on the rest.
    Column(Modifier.fillMaxSize().background(TT.Ink).statusBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Wordmark(width = 92.dp, onDark = true)
            // A black avatar on the black canvas would vanish.
            AccountCorner(user, nav, Skin.PINK)
        }

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 22.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "headline") {
                Text(
                    buildAnnotatedString {
                        append(stringResource(R.string.feed_headline_a))
                        append("\n")
                        withStyle(SpanStyle(color = TT.Rose)) {
                            append(stringResource(R.string.feed_headline_b))
                        }
                    },
                    style = TTType.ShoutSmall.copy(fontSize = 34.sp, lineHeight = 34.sp),
                    color = TT.Paper,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.feed_sub),
                    style = TTType.BodySmall,
                    color = TT.Gray45,
                )
                Spacer(Modifier.height(14.dp))
            }

            val list = events
            when {
                failed -> item(key = "failed") {
                    Text(
                        stringResource(R.string.feed_failed),
                        style = TTType.BodySmall,
                        color = TT.Gray45,
                    )
                }

                list == null -> item(key = "loading") {
                    Text(
                        stringResource(R.string.feed_loading),
                        style = TTType.BodySmall,
                        color = TT.Gray45,
                    )
                }

                list.isEmpty() -> item(key = "empty") {
                    Text(
                        stringResource(R.string.feed_empty),
                        style = TTType.BodySmall,
                        color = TT.Gray45,
                    )
                }

                else -> eventRows(list, nav)
            }
        }
    }
}

private fun LazyListScope.eventRows(list: List<ServerEvent>, nav: NavHostController) {
    val now = Instant.now()
    list.sortedByDescending { it.startAt ?: Instant.EPOCH }.forEach { event ->
        item(key = event.id) { EventRow(event, now, nav) }
    }
}

@Composable
private fun EventRow(event: ServerEvent, now: Instant, nav: NavHostController) {
    val live = event.isLiveAt(now)
    // Live: the whole row is Pink, black type on it (7.93:1; never white on
    // Pink). The rest: dark cards whose door is Toxic Yellow.
    val surface = if (live) TT.Rose else TT.Ink800
    val title = if (live) TT.Ink else TT.Paper
    val meta = if (live) TT.Ink.copy(alpha = 0.7f) else TT.Gray45
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(surface)
            .clickable { nav.navigate(Routes.eventFeed(event.id, event.name)) }
            .padding(horizontal = 18.dp, vertical = if (live) 22.dp else 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                event.name,
                style = TTType.ItemSub.copy(
                    fontSize = if (live) 20.sp else 16.sp,
                    lineHeight = if (live) 22.sp else 19.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = title,
            )
            event.details?.let {
                Spacer(Modifier.height(3.dp))
                Text(it, style = TTType.ItemSub.copy(fontSize = 12.sp), color = meta)
            }
        }
        Text(
            stringResource(if (live) R.string.feed_row_live else R.string.feed_row_open),
            style = TTType.MetaSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
            color = if (live) TT.Acid else TT.Ink,
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(if (live) TT.Ink else TT.Acid)
                .padding(horizontal = 12.dp, vertical = 7.dp),
        )
    }
}
