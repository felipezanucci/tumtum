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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cc.tumtum.app.R
import cc.tumtum.app.data.api.ServerCrowd
import cc.tumtum.app.data.api.ServerPost
import cc.tumtum.app.data.db.NightEntity
import cc.tumtum.app.data.repo.CrowdState
import cc.tumtum.app.data.repo.FeedState
import cc.tumtum.app.data.repo.Outcome
import cc.tumtum.app.domain.FeedMoment
import cc.tumtum.app.domain.Skin
import cc.tumtum.app.domain.SocialUser
import cc.tumtum.app.ui.Fmt
import cc.tumtum.app.ui.components.MomentCard
import cc.tumtum.app.ui.components.TTButton
import cc.tumtum.app.ui.components.TTButtonStyle
import cc.tumtum.app.ui.nav.Routes
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
fun EventFeedScreen(nav: NavHostController, eventId: String, eventName: String? = null) {
    val container = appContainer()
    val scope = rememberCoroutineScope()
    var state by remember(eventId) { mutableStateOf<FeedState>(FeedState.Loading) }
    var crowd by remember(eventId) { mutableStateOf<CrowdState>(CrowdState.Loading) }
    var tick by remember { mutableStateOf(0) }
    val user by container.prefs.state.collectAsStateWithLifecycle(initialValue = null)

    // The name the previous screen already had (#32, 22/09). The header used to
    // fall back to "Rolê" whenever the feed was not Ready — a name the app had
    // one tap earlier and dropped. It is kept, and refreshed by the server's.
    var knownName by remember(eventId) { mutableStateOf(eventName?.takeIf { it.isNotBlank() }) }

    // Posts with a tap still in flight, and a sentence about the last tap that
    // did not go through — said under the card it was made on (#47).
    var inFlight by remember(eventId) { mutableStateOf(setOf<String>()) }
    var notice by remember(eventId) { mutableStateOf<Pair<String, Int>?>(null) }

    // This phone's night at the event, if it reached the server: the empty
    // feed offers it instead of inviting an act it gives no way to do (#48).
    var myNight by remember(eventId) { mutableStateOf<NightEntity?>(null) }

    LaunchedEffect(eventId, tick) {
        // A reload that gets cancelled by a newer one now simply stops: the
        // repository no longer turns cancellation into Failed (#46), so the
        // screen keeps what it had instead of flashing an error.
        state = container.social.feed(eventId)
        (state as? FeedState.Ready)?.eventName?.takeIf { it.isNotBlank() }?.let { knownName = it }
        crowd = container.social.crowd(eventId)
    }
    LaunchedEffect(eventId) {
        myNight = container.nights.uploadedNightAt(eventId)
    }

    fun replacePost(updated: ServerPost) {
        val ready = state as? FeedState.Ready ?: return
        state = ready.copy(posts = ready.posts.map { if (it.id == updated.id) updated else it })
    }

    fun dropPost(postId: String) {
        val ready = state as? FeedState.Ready ?: return
        state = ready.copy(posts = ready.posts.filterNot { it.id == postId })
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
                ready?.eventName?.takeIf { it.isNotBlank() } ?: knownName
                    ?: stringResource(R.string.event_feed_title_fallback),
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

                // #35, 22/09. Felipe read "Entra na sua conta" while the app still
                // showed his name and avatar — two claims that contradicted each
                // other. The truth was a token that had expired, so that is what
                // is said, with the way back one tap away instead of in Configurações.
                is FeedState.SignedOut -> Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    val expired = user?.session != null
                    Note(
                        stringResource(
                            if (expired) R.string.event_feed_session_expired else R.string.event_feed_signed_out,
                        ),
                    )
                    TTButton(
                        stringResource(if (expired) R.string.event_feed_sign_in_again else R.string.event_feed_sign_in),
                        TTButtonStyle.Rose,
                        onClick = { nav.navigate(Routes.Login) },
                    )
                }

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
                        EmptyFeed(myNight, nav)
                    } else {
                        s.posts.forEach { post ->
                            val busy = post.id in inFlight
                            MomentCard(
                                moment = post.asMoment(),
                                onToggleSenti = {
                                    if (!busy) {
                                        inFlight = inFlight + post.id
                                        notice = null
                                        scope.launch {
                                            // The server's answer *is* the new count.
                                            // No refetch: nothing to race, nothing
                                            // to cancel, nothing to guess (#47).
                                            when (val r = container.social.toggleSenti(eventId, post.id)) {
                                                is Outcome.Done -> replacePost(r.value)
                                                Outcome.NotThere ->
                                                    notice = post.id to R.string.event_feed_senti_refused
                                                Outcome.SignedOut -> state = FeedState.SignedOut
                                                is Outcome.Failed -> notice = post.id to
                                                    if (r.offline) {
                                                        R.string.event_feed_senti_offline
                                                    } else {
                                                        R.string.event_feed_senti_failed
                                                    }
                                            }
                                            inFlight = inFlight - post.id
                                        }
                                    }
                                },
                            )
                            if (post.mine) {
                                Text(
                                    stringResource(
                                        if (busy) R.string.event_feed_taking_down else R.string.event_feed_take_down,
                                    ),
                                    style = TTType.MetaSmall,
                                    color = TT.Gray45,
                                    modifier = Modifier.clickable(enabled = !busy) {
                                        inFlight = inFlight + post.id
                                        notice = null
                                        scope.launch {
                                            when (val r = container.social.takeDown(eventId, post.id)) {
                                                is Outcome.Done -> dropPost(post.id)
                                                Outcome.SignedOut -> state = FeedState.SignedOut
                                                is Outcome.Failed -> notice = post.id to
                                                    if (r.offline) {
                                                        R.string.event_feed_takedown_offline
                                                    } else {
                                                        R.string.event_feed_takedown_failed
                                                    }
                                                // Taking your own post down is not gated on
                                                // attendance, so a 403 here is a failure.
                                                Outcome.NotThere -> notice = post.id to R.string.event_feed_takedown_failed
                                            }
                                            inFlight = inFlight - post.id
                                        }
                                    },
                                )
                            }
                            notice?.takeIf { it.first == post.id }?.let { (_, res) ->
                                Text(stringResource(res), style = TTType.BodySmall, color = TT.Rose)
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

/**
 * The empty feed, **with a door** (#48, 22/09).
 *
 * It used to say "Pode ser você" and offer nothing to tap: to accept, a fan had
 * to back out, find their night and start again by another road. Now the
 * sentence and the button are born together, and when this phone cannot offer
 * the act, the sentence stops inviting it — "pode ser você" with no way to be
 * is one more thing the app would be saying that is not true.
 */
@Composable
private fun EmptyFeed(night: NightEntity?, nav: NavHostController) {
    val now = System.currentTimeMillis()
    when {
        night != null && (night.revealAt == null || night.revealAt <= now) ->
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Note(stringResource(R.string.event_feed_empty))
                TTButton(
                    stringResource(R.string.event_feed_show_mine),
                    TTButtonStyle.Rose,
                    onClick = { nav.navigate(Routes.choose(night.id)) },
                )
            }

        // Sealed by the reveal lock: the night exists, and says when it opens.
        night != null -> Note(
            stringResource(
                R.string.event_feed_empty_locked,
                Fmt.hour(java.time.Instant.ofEpochMilli(night.revealAt ?: now)),
            ),
        )

        // The server says this account was there, but the night is not on this
        // phone — another phone, or a reinstall. Nothing to offer, so no invite.
        else -> Note(stringResource(R.string.event_feed_empty_elsewhere))
    }
}

/**
 * A post as the card draws it. **No event name** (#31): every post on this
 * screen is from the same event, whose name is the header right above, so the
 * line under the author said it again on every card. It says when instead.
 */
private fun ServerPost.asMoment(): FeedMoment {
    val skinValue = runCatching { Skin.valueOf(skin) }.getOrDefault(Skin.BLACK)
    return FeedMoment(
        id = id.hashCode().toLong(),
        postId = id,
        mine = mine,
        user = SocialUser(handle = "", displayName = authorName, initials = authorInitials, avatarSkin = skinValue),
        eventName = "",
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
