package cc.tumtum.app.ui.screens.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cc.tumtum.app.R
import cc.tumtum.app.data.api.ServerEvent
import cc.tumtum.app.data.api.ServerEvents
import cc.tumtum.app.data.repo.NightRepository
import cc.tumtum.app.domain.Skin
import cc.tumtum.app.ui.components.AccountCorner
import cc.tumtum.app.ui.components.Wordmark
import cc.tumtum.app.ui.nav.appContainer
import cc.tumtum.app.ui.screens.eventfeed.EventFeedBody
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType
import java.time.Instant

/**
 * b5 — a primeira tela: o feed do evento de agora, e os outros numa faixa.
 *
 * **Rewritten 2026-09-22.** Until that day this was the app's start
 * destination showing `FakeSocialRepository`: a banner announcing "Hoje:
 * Taylor Swift · 3 amigos confirmados" and moments by Mariana Alves and
 * Rodrigo Costa, people who do not exist, carrying heart rates nobody
 * measured. It had been on the Play internal testing track since 18/09.
 *
 * There is no friends feed, by decision: the feed is **per event**, so that
 * only the people who were somewhere together can talk about it.
 *
 * **Opens on the posts, 28/09.** From 22/09 this screen was the list of
 * events — "ONDE A TUMTUM TÁ ROLANDO." — and each one a door to its feed.
 * With b222 in his hand Felipe chose for the tab to open straight on a feed:
 * the one rolling now, else the last one this account has a night at, else
 * the latest. The other events became a strip of chips above it, and the
 * strip is the navigation. **Never an event still to come** (02/10): with
 * b227 the tab opened on a test event dated a month ahead, first in the
 * strip because it was the newest date, and its feed said "Sua noite não
 * chegou aqui" about a night that could not have happened yet. The strip now
 * puts what is on, then what has begun, then what is coming, at the end. The feed under it is [EventFeedBody], the same
 * body the event's own screen draws — one feed, not two copies of one.
 */
@Composable
fun FeedScreen(nav: NavHostController) {
    val container = appContainer()
    val user by container.prefs.state.collectAsStateWithLifecycle(initialValue = null)
    var events by remember { mutableStateOf<List<ServerEvent>?>(null) }
    var failed by remember { mutableStateOf(false) }
    // The chip on, kept across a trip to another tab. [picked]: the person
    // chose it, so a later look at their nights never moves them off it.
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var picked by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // An empty list and a list that did not load are different claims,
        // and this screen has to be able to tell them apart.
        runCatching { container.api.listEvents() }
            .onSuccess { list -> events = ServerEvents.forFeedStrip(list, Instant.now()); failed = false }
            .onFailure { events = null; failed = true }
    }
    val viewerId = user?.session?.userId
    LaunchedEffect(events, viewerId) {
        val list = events ?: return@LaunchedEffect
        val kept = selectedId?.takeIf { id -> list.any { it.id == id } }
        if (picked && kept != null) return@LaunchedEffect
        selectedId = currentEvent(list, Instant.now(), container.nights, viewerId)?.id
        picked = false
    }

    // #30, 22/09 — Felipe: "tá muito cinza, preta e branca… mais colorida,
    // mais viva." The manual's digital default is a black canvas with Pink as
    // the main emphasis and Toxic Yellow as the second explosion: the chip of
    // the feed on screen is Pink, and a night that is on now wears a yellow
    // ROLANDO wherever it sits in the strip.
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

        val list = events
        when {
            failed -> Status(stringResource(R.string.feed_failed))
            list == null -> Status(stringResource(R.string.feed_loading))
            list.isEmpty() -> Status(stringResource(R.string.feed_empty))
            else -> {
                val now = Instant.now()
                val strip = rememberLazyListState()
                // The chip the tab chose is brought into view: past three
                // upcoming dates, the night that is on now would otherwise be
                // Pink off the edge of the screen. A chip the person tapped is
                // already where their finger was.
                LaunchedEffect(selectedId, list) {
                    val at = list.indexOfFirst { it.id == selectedId }
                    if (at >= 0 && !picked) strip.animateScrollToItem(at)
                }
                LazyRow(
                    Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 14.dp),
                    state = strip,
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(list, key = { it.id }) { event ->
                        EventChip(event, selected = event.id == selectedId, live = event.isLiveAt(now)) {
                            selectedId = event.id
                            picked = true
                        }
                    }
                }
                val shown = list.firstOrNull { it.id == selectedId }
                if (shown == null) {
                    // The events are here and which one is this account's is
                    // still being asked of the phone — a moment, and said. Not
                    // "Carregando os eventos…": those are on screen already.
                    Status(stringResource(R.string.event_feed_loading))
                } else {
                    // A fresh body per event: its state, its scroll, its
                    // sentences all belong to the feed they were about.
                    key(shown.id) {
                        EventFeedBody(
                            nav,
                            eventId = shown.id,
                            eventName = shown.name,
                            embedded = true,
                            upcoming = shown.takeIf { ServerEvents.notYet(it, now) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

/**
 * The feed the tab opens on ([ServerEvents.feedDefault]), with the phone
 * asked which of the events that have begun this account has a night at.
 * [list] is the strip, in [ServerEvents.forFeedStrip]'s order.
 */
private suspend fun currentEvent(
    list: List<ServerEvent>,
    now: Instant,
    nights: NightRepository,
    viewerId: String?,
): ServerEvent? {
    val withNight = list.filterNot { ServerEvents.notYet(it, now) }
        .filter { event ->
            nights.uploadedNightAt(event.id, viewerId) != null || nights.unsentNightAt(event.id) != null
        }
        .map { it.id }
        .toSet()
    return ServerEvents.feedDefault(list, now, hasNight = { it.id in withNight })
}

/** What the tab has instead of a strip: loading, failed, or truly none. */
@Composable
private fun Status(text: String) {
    Text(
        text,
        style = TTType.BodySmall,
        color = TT.Gray45,
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 22.dp),
    )
}

/**
 * An event in the strip. The one on screen is Pink with black type on it
 * (7.93:1; never white on Pink); the rest are outlined on the black. A night
 * that is on now says ROLANDO in Toxic Yellow — on the Pink chip as a black
 * pill with yellow type, on an outlined one as a yellow pill with black type.
 */
@Composable
private fun EventChip(event: ServerEvent, selected: Boolean, live: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(999.dp)
    Row(
        Modifier
            .clip(shape)
            .background(if (selected) TT.Rose else TT.Ink)
            .border(1.dp, if (selected) TT.Rose else TT.Ink600, shape)
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = if (live) 6.dp else 16.dp)
            .height(40.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            event.name,
            style = TTType.ItemSub.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
            color = if (selected) TT.Ink else TT.Paper,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 220.dp),
        )
        if (live) {
            Text(
                stringResource(R.string.feed_row_live),
                style = TTType.MetaSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                color = if (selected) TT.Acid else TT.Ink,
                modifier = Modifier
                    .clip(shape)
                    .background(if (selected) TT.Ink else TT.Acid)
                    .padding(horizontal = 9.dp, vertical = 5.dp),
            )
        }
    }
}
