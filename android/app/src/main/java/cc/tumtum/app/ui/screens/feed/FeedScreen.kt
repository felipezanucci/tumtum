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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cc.tumtum.app.R
import cc.tumtum.app.data.api.ServerEvent
import cc.tumtum.app.domain.Skin
import cc.tumtum.app.ui.components.UserAvatar
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

    Column(Modifier.fillMaxSize().background(TT.Paper).statusBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Wordmark(width = 92.dp)
            UserAvatar(
                user?.account?.initials ?: "TT",
                Skin.BLACK,
                photoPath = user?.avatarPath,
                modifier = Modifier.clickable {
                    user?.account?.let { nav.navigate(Routes.profile(it.username)) }
                },
            )
        }

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 22.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "headline") {
                Text(
                    stringResource(R.string.feed_headline),
                    style = TTType.ShoutSmall.copy(fontSize = 24.sp, lineHeight = 25.sp),
                    color = TT.Ink,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.feed_sub),
                    style = TTType.BodySmall,
                    color = TT.Gray70,
                )
                Spacer(Modifier.height(10.dp))
            }

            val list = events
            when {
                failed -> item(key = "failed") {
                    Text(
                        stringResource(R.string.feed_failed),
                        style = TTType.BodySmall,
                        color = TT.Gray70,
                    )
                }

                list == null -> item(key = "loading") {
                    Text(
                        stringResource(R.string.feed_loading),
                        style = TTType.BodySmall,
                        color = TT.Gray70,
                    )
                }

                list.isEmpty() -> item(key = "empty") {
                    Text(
                        stringResource(R.string.feed_empty),
                        style = TTType.BodySmall,
                        color = TT.Gray70,
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
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (live) TT.Acid else TT.Gray10)
            .clickable { nav.navigate(Routes.eventFeed(event.id)) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                event.name,
                style = TTType.ItemSub.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                color = TT.Ink,
            )
            Text(
                event.label,
                style = TTType.ItemSub.copy(fontSize = 12.sp),
                color = TT.Ink.copy(alpha = 0.6f),
            )
        }
        Text(
            stringResource(if (live) R.string.feed_row_live else R.string.feed_row_open),
            style = TTType.MetaSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
            color = if (live) TT.Acid else TT.Paper,
            modifier = Modifier.background(TT.Ink).padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}
