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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import cc.tumtum.app.data.repo.FeedTarget
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
/**
 * **One feed per event** (#65, Felipe 23/09). Until that day a show in a
 * tour had two — its own "rolê" and the tour's above it, behind a black door
 * — and in his hands it read as confusing: two words, two screens, and a
 * two-way consent for a choice nobody knew existed. Now an event in a tour
 * opens onto the tour's feed, starting on every date, each post saying which
 * night it is from, and "Só a minha noite" narrows it to the night this
 * person was at. A show on its own is simply its own feed.
 */
@Composable
fun EventFeedScreen(nav: NavHostController, eventId: String, eventName: String? = null) {
    val title = eventName
    val feed = FeedTarget(eventId)
    val container = appContainer()
    val scope = rememberCoroutineScope()
    var state by remember(eventId) { mutableStateOf<FeedState>(FeedState.Loading) }
    var crowd by remember(eventId) { mutableStateOf<CrowdState>(CrowdState.Loading) }
    var tick by remember { mutableStateOf(0) }
    val user by container.prefs.state.collectAsStateWithLifecycle(initialValue = null)

    // The name the previous screen already had (#32, 22/09). The header used to
    // fall back to "Rolê" whenever the feed was not Ready — a name the app had
    // one tap earlier and dropped. It is kept, and refreshed by the server's.
    var knownName by remember(eventId) { mutableStateOf(title?.takeIf { it.isNotBlank() }) }

    // Posts with a tap still in flight, and a sentence about the last tap that
    // did not go through — said under the card it was made on (#47).
    var inFlight by remember(eventId) { mutableStateOf(setOf<String>()) }
    var notice by remember(eventId) { mutableStateOf<Pair<String, Int>?>(null) }

    // This phone's night at the event, if it reached the server: the empty
    // feed offers it instead of inviting an act it gives no way to do (#48).
    var myNight by remember(eventId) { mutableStateOf<NightEntity?>(null) }

    // Report and block (#36): which post's menu is open, and the sentence a
    // block leaves at the top of the feed once its author's posts are gone.
    var moderating by remember(eventId) { mutableStateOf<ServerPost?>(null) }
    var banner by remember(eventId) { mutableStateOf<String?>(null) }

    // "Todas as datas" or "Só a minha noite" (#65). Opens on every date —
    // the fuller feed — and remembers the choice for as long as the screen.
    var onlyMine by remember(eventId) { mutableStateOf(false) }

    LaunchedEffect(eventId, tick) {
        // A reload that gets cancelled by a newer one now simply stops: the
        // repository no longer turns cancellation into Failed (#46), so the
        // screen keeps what it had instead of flashing an error.
        state = container.social.feed(eventId)
        (state as? FeedState.Ready)?.eventName?.takeIf { it.isNotBlank() }?.let { knownName = it }
        // Card 04 is a crowd at one night — this one, the night the person
        // was at — whatever dates the feed below spans.
        crowd = container.social.crowd(eventId)
    }
    LaunchedEffect(eventId, user?.session?.userId) {
        // Only the signed-in account's night is offered (#58).
        myNight = container.nights.uploadedNightAt(eventId, user?.session?.userId)
    }

    fun replacePost(updated: ServerPost) {
        val ready = state as? FeedState.Ready ?: return
        state = ready.copy(posts = ready.posts.map { if (it.id == updated.id) updated else it })
    }

    fun dropPost(postId: String) {
        val ready = state as? FeedState.Ready ?: return
        state = ready.copy(posts = ready.posts.filterNot { it.id == postId })
    }

    val context = LocalContext.current
    moderating?.let { chosen ->
        ModerationDialog(
            authorName = chosen.authorName,
            onDismiss = { moderating = null },
            onReport = { reason ->
                moderating = null
                inFlight = inFlight + chosen.id
                notice = null
                scope.launch {
                    notice = chosen.id to when (val r = container.social.report(feed, chosen.id, reason)) {
                        is Outcome.Done -> R.string.mod_reported
                        is Outcome.Failed -> if (r.offline) R.string.mod_offline else R.string.mod_failed
                        Outcome.NotThere, Outcome.SignedOut -> R.string.mod_failed
                    }
                    inFlight = inFlight - chosen.id
                }
            },
            onBlock = {
                moderating = null
                inFlight = inFlight + chosen.id
                notice = null
                scope.launch {
                    when (val r = container.social.block(feed, chosen.id)) {
                        is Outcome.Done -> {
                            // The server now hides every post by this person;
                            // a reload is the honest way to show exactly that.
                            banner = context.getString(R.string.mod_blocked, chosen.authorName)
                            tick++
                        }
                        is Outcome.Failed -> notice = chosen.id to
                            if (r.offline) R.string.mod_offline else R.string.mod_failed
                        Outcome.NotThere, Outcome.SignedOut -> notice = chosen.id to R.string.mod_failed
                    }
                    inFlight = inFlight - chosen.id
                }
            },
        )
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
            // The tour whose feed this is, only when it spans several dates —
            // a tour of one date is just this show, and "1 datas" was a
            // sentence the app should never have said (#66).
            val tour = ready?.takeIf { it.spansDates }
            val tourName = tour?.series?.name
            if (tour != null && tourName != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(R.string.feed_tour_line, tourName, tour.dates.size),
                    style = TTType.BodySmall.copy(fontWeight = FontWeight.Medium),
                    color = TT.Ink.copy(alpha = 0.7f),
                )
            }
            Spacer(Modifier.height(16.dp))
            CrowdLine(crowd)
            if (ready?.spansDates == true) {
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NightChip(stringResource(R.string.feed_filter_all), selected = !onlyMine) { onlyMine = false }
                    NightChip(stringResource(R.string.feed_filter_mine), selected = onlyMine) { onlyMine = true }
                }
            }
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

                is FeedState.Ready -> {
                    banner?.let { Note(it) }
                    val shown = if (onlyMine && s.spansDates) s.posts.filter { it.eventId == eventId } else s.posts
                    if (shown.isEmpty()) {
                        when {
                            // The filter emptied it, not the world: say so, and
                            // the way back is the chip right above.
                            s.posts.isNotEmpty() -> Note(stringResource(R.string.feed_filter_mine_empty))
                            // A block emptied it (#63): somebody did post.
                            s.hiddenByBlock > 0 -> Note(stringResource(R.string.event_feed_empty_blocked))
                            else -> EmptyFeed(myNight, nav)
                        }
                    } else {
                        shown.forEach { post ->
                            val busy = post.id in inFlight
                            MomentCard(
                                moment = post.asMoment(showNight = s.spansDates),
                                onToggleSenti = {
                                    if (!busy) {
                                        inFlight = inFlight + post.id
                                        notice = null
                                        scope.launch {
                                            // The server's answer *is* the new count.
                                            // No refetch: nothing to race, nothing
                                            // to cancel, nothing to guess (#47).
                                            when (val r = container.social.toggleSenti(feed, post.id)) {
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
                                            when (val r = container.social.takeDown(post.eventId ?: eventId, post.id)) {
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
                            if (!post.mine) {
                                Text(
                                    stringResource(R.string.mod_open),
                                    style = TTType.MetaSmall,
                                    color = TT.Gray45,
                                    modifier = Modifier.clickable(enabled = !busy) { moderating = post },
                                )
                            }
                            notice?.takeIf { it.first == post.id }?.let { (_, res) ->
                                Text(
                                    stringResource(res),
                                    style = TTType.BodySmall,
                                    color = if (res == R.string.mod_reported) TT.Ink else TT.Rose,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** One of the two night filters (#65): black when chosen, outlined when not. */
@Composable
private fun NightChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        label,
        style = TTType.MetaSmall.copy(fontWeight = FontWeight.Bold),
        color = if (selected) TT.Acid else TT.Ink,
        modifier = Modifier
            .background(if (selected) TT.Ink else TT.Ink.copy(alpha = 0.08f))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp),
    )
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
 * A post as the card draws it. **No event name** (#31): on a show's own feed
 * every post is from the event named right above, so the line under the
 * author says when instead. In a tour's feed, where several dates sit
 * together, it says which night: "São Paulo 24/10".
 */
private fun ServerPost.asMoment(showNight: Boolean = false): FeedMoment {
    val skinValue = runCatching { Skin.valueOf(skin) }.getOrDefault(Skin.BLACK)
    return FeedMoment(
        id = id.hashCode().toLong(),
        postId = id,
        mine = mine,
        user = SocialUser(handle = "", displayName = authorName, initials = authorInitials, avatarSkin = skinValue),
        eventName = if (showNight) {
            listOfNotNull(
                eventCity ?: eventName,
                eventDate?.let { "%02d/%02d".format(it.dayOfMonth, it.monthValue) },
            ).joinToString(" ")
        } else {
            ""
        },
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

/**
 * Report a post, or block the person who made it (#36, 22/09).
 *
 * A safety screen, so the brand goes quiet: plain words, no jokes, and each
 * choice says what it will do before it does it. Three reasons and no free
 * text — a report box is the one place strangers could otherwise write to
 * each other.
 */
@Composable
private fun ModerationDialog(
    authorName: String,
    onDismiss: () -> Unit,
    onReport: (String) -> Unit,
    onBlock: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TT.Paper,
        title = { Text(stringResource(R.string.mod_title), style = TTType.TitleSmall, color = TT.Ink) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(
                    "abuse" to R.string.mod_report_abuse,
                    "fake" to R.string.mod_report_fake,
                    "other" to R.string.mod_report_other,
                ).forEach { (reason, label) ->
                    Text(
                        stringResource(label),
                        style = TTType.Body,
                        color = TT.Ink,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onReport(reason) }
                            .padding(vertical = 10.dp),
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(R.string.mod_block, authorName),
                    style = TTType.Body.copy(fontWeight = FontWeight.SemiBold),
                    color = TT.Ink,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onBlock)
                        .padding(top = 10.dp),
                )
                Text(stringResource(R.string.mod_block_hint), style = TTType.Footnote, color = TT.Gray45)
            }
        },
        confirmButton = {},
        dismissButton = {
            Text(
                stringResource(R.string.mod_cancel),
                style = TTType.Button.copy(fontSize = 14.sp),
                color = TT.Gray70,
                modifier = Modifier.clickable(onClick = onDismiss).padding(12.dp),
            )
        },
    )
}
