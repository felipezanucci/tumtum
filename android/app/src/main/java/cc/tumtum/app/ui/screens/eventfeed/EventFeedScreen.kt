package cc.tumtum.app.ui.screens.eventfeed

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import cc.tumtum.app.R
import cc.tumtum.app.data.api.ServerCrowd
import cc.tumtum.app.data.api.ServerPost
import cc.tumtum.app.data.repo.CrowdState
import cc.tumtum.app.data.repo.FeedState
import cc.tumtum.app.domain.FeedMoment
import cc.tumtum.app.domain.Skin
import cc.tumtum.app.domain.SocialUser
import cc.tumtum.app.ui.Fmt
import cc.tumtum.app.ui.components.MomentCard
import cc.tumtum.app.ui.nav.appContainer
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType
import kotlinx.coroutines.launch

/**
 * b6 — o feed do evento: quem estava lá, e só.
 *
 * Rewritten 2026-09-22. Until that day this screen was fed by
 * `FakeSocialRepository` and showed invented people with invented heart
 * rates — 8,734 who shared, 64% who beat their own peak, at a Taylor Swift
 * show nobody went to. It now shows what the server has, and when it has
 * nothing it **says which kind of nothing**: empty, refused, or unreachable.
 * Those are three different sentences and collapsing them into one blank
 * list is the defect this project keeps counting.
 */
@Composable
fun EventFeedScreen(nav: NavHostController, eventId: String) {
    val container = appContainer()
    val scope = rememberCoroutineScope()
    var state by remember(eventId) { mutableStateOf<FeedState>(FeedState.Loading) }
    var crowd by remember(eventId) { mutableStateOf<CrowdState>(CrowdState.Loading) }
    var tick by remember { mutableStateOf(0) }

    LaunchedEffect(eventId, tick) {
        state = container.social.feed(eventId)
        crowd = container.social.crowd(eventId)
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(TT.Paper)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding(),
    ) {
        val ready = state as? FeedState.Ready
        Column(
            Modifier
                .fillMaxWidth()
                .background(TT.Acid)
                .statusBarsPadding()
                .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 22.dp),
        ) {
            Text(
                stringResource(R.string.event_feed_back),
                style = TTType.Meta.copy(letterSpacing = 0.04.em),
                color = TT.Ink.copy(alpha = 0.6f),
                modifier = Modifier.clickable { nav.popBackStack() },
            )
            Spacer(Modifier.height(14.dp))
            Text(
                ready?.eventName ?: stringResource(R.string.event_feed_title_fallback),
                style = TTType.ShoutSmall.copy(fontSize = 26.sp, lineHeight = 26.5.sp),
                color = TT.Ink,
            )
            ready?.venue?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(6.dp))
                Text(
                    it,
                    style = TTType.BodySmall.copy(fontWeight = FontWeight.Medium),
                    color = TT.Ink.copy(alpha = 0.65f),
                )
            }
            Spacer(Modifier.height(16.dp))
            CrowdLine(crowd)
        }

        Column(
            Modifier.padding(start = 24.dp, end = 24.dp, top = 18.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            when (val s = state) {
                is FeedState.Loading -> Note(stringResource(R.string.event_feed_loading))

                // Not an empty list: the server said this account has no
                // measured night here, and that is its own sentence.
                is FeedState.NotThere -> Note(stringResource(R.string.event_feed_not_there))

                is FeedState.SignedOut -> Note(stringResource(R.string.event_feed_signed_out))

                is FeedState.Failed -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Note(
                        stringResource(
                            if (s.offline) R.string.event_feed_offline else R.string.event_feed_failed,
                        ),
                    )
                    Text(
                        stringResource(R.string.event_feed_retry),
                        style = TTType.MetaSmall.copy(fontWeight = FontWeight.Bold),
                        color = TT.Rose,
                        modifier = Modifier.clickable { tick++ },
                    )
                }

                is FeedState.Ready ->
                    if (s.isEmpty) {
                        Note(stringResource(R.string.event_feed_empty))
                    } else {
                        s.posts.forEach { post ->
                            MomentCard(
                                moment = post.asMoment(s.eventName),
                                onToggleSenti = {
                                    scope.launch {
                                        container.social.toggleSenti(eventId, post.id)
                                        tick++
                                    }
                                },
                            )
                            if (post.mine) {
                                Text(
                                    stringResource(R.string.event_feed_take_down),
                                    style = TTType.MetaSmall,
                                    color = TT.Gray45,
                                    modifier = Modifier.clickable {
                                        scope.launch {
                                            container.social.takeDown(eventId, post.id)
                                            tick++
                                        }
                                    },
                                )
                            }
                        }
                    }
            }
        }
    }
}

/** Card 04, as one line — or an honest account of why there is not one yet. */
@Composable
private fun CrowdLine(state: CrowdState) {
    val text = when (state) {
        is CrowdState.Loading -> return
        is CrowdState.NotThere -> return
        is CrowdState.Failed -> return
        is CrowdState.Ready -> crowdText(state.crowd)
    } ?: return
    Text(
        text,
        style = TTType.MetaSmall.copy(fontWeight = FontWeight.Bold),
        color = TT.Ink,
        modifier = Modifier.background(TT.Ink.copy(alpha = 0.08f)).padding(horizontal = 9.dp, vertical = 5.dp),
    )
}

@Composable
private fun crowdText(crowd: ServerCrowd): String? {
    // Below the server's floor no collective figure exists, because over a
    // small crowd it is a fact about each person in it. Say how few, never a
    // zero — an empty state is a claim.
    if (!crowd.enough) {
        return stringResource(R.string.crowd_too_few, crowd.measuredNights)
    }
    val top = crowd.top ?: return stringResource(R.string.crowd_nights, crowd.measuredNights)
    return stringResource(R.string.crowd_top, top.people, Fmt.hour(top.at))
}

@Composable
private fun Note(text: String) {
    Text(text, style = TTType.BodySmall, color = TT.Gray70)
}

private fun ServerPost.asMoment(eventName: String): FeedMoment {
    val skinValue = runCatching { Skin.valueOf(skin) }.getOrDefault(Skin.BLACK)
    return FeedMoment(
        id = id.hashCode().toLong(),
        postId = id,
        mine = mine,
        user = SocialUser(handle = "", displayName = authorName, initials = authorInitials, avatarSkin = skinValue),
        eventName = eventName,
        whenLabel = Fmt.hour(at),
        // The moment's own name when the timeline gave it one, and nothing
        // invented when it did not.
        title = label?.uppercase().orEmpty(),
        bpm = bpm,
        metaLabel = "bpm",
        quote = quote.orEmpty(),
        skin = skinValue,
        sentiCount = reactions,
        sentiByMe = reactedByMe,
    )
}
