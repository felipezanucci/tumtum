package cc.tumtum.app.ui.screens.eventfeed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cc.tumtum.app.R
import cc.tumtum.app.data.api.ServerSeries
import cc.tumtum.app.data.repo.PostResult
import cc.tumtum.app.domain.ConsentText
import cc.tumtum.app.domain.FeedGate
import cc.tumtum.app.domain.Night
import cc.tumtum.app.domain.Skin
import cc.tumtum.app.ui.Fmt
import cc.tumtum.app.ui.components.BackArrow
import cc.tumtum.app.ui.components.TTButton
import cc.tumtum.app.ui.components.TTButtonStyle
import cc.tumtum.app.ui.components.feedClosedText
import cc.tumtum.app.ui.nav.Routes
import cc.tumtum.app.ui.nav.appContainer
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType
import kotlinx.coroutines.launch

/**
 * "Mostrar pra galera" as its own step (28/09, item 31) — reached from the
 * empty feed's "Mostrar a minha", from "Feed do evento" on the share screen
 * and from the card's done screen. Until this day it lived only on that
 * last one, after sharing to another network and coming back, and the feed's
 * own button sent the person to the skin chooser instead: the founder never
 * found how to post.
 *
 * Posting is not sharing: it puts a heart rate at a named minute in front of
 * strangers who were at the same event. So the screen is the quiet kind —
 * what goes, who sees it, that it can be taken down — and one Pink act. Once
 * it is posted the person lands on the feed, with the post in it.
 */
@Composable
fun ShowToFeedScreen(nav: NavHostController, nightId: Long, skin: Skin?) {
    val container = appContainer()
    val night by container.nights.night(nightId).collectAsStateWithLifecycle(initialValue = null)
    var eventId by remember(nightId) { mutableStateOf<String?>(null) }
    var series by remember(nightId) { mutableStateOf<ServerSeries?>(null) }
    // Until the event is looked up, no reason is given: "no feed" for a
    // question not yet asked would be a claim the phone cannot make.
    var looked by remember(nightId) { mutableStateOf(false) }
    LaunchedEffect(nightId) {
        eventId = container.nights.serverEventIdFor(nightId)
        eventId?.let { series = container.social.seriesOf(it) }
        looked = true
    }
    val n = night ?: return

    Column(
        Modifier
            .fillMaxSize()
            .background(TT.Night)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 28.dp, end = 28.dp, top = 22.dp, bottom = 26.dp),
    ) {
        BackArrow(onClick = { nav.popBackStack() }, onDark = true)
        Spacer(Modifier.height(22.dp))
        Text(stringResource(R.string.show_feed_label), style = TTType.MetaWide, color = TT.Acid)
        Spacer(Modifier.height(10.dp))
        Text(n.eventName, style = TTType.TitleSmall, color = TT.Paper)
        Spacer(Modifier.height(18.dp))
        // What goes: the number and its minute — the same the post will carry.
        Row(verticalAlignment = Alignment.Bottom) {
            Text("${n.peakBpm}", style = TTType.Hero.copy(fontSize = 72.sp, lineHeight = 66.sp), color = TT.Rose)
            Spacer(Modifier.width(10.dp))
            Text(
                stringResource(R.string.reveal_bpm) + " " + stringResource(R.string.reveal_at, Fmt.hour(n.peakAt)),
                style = TTType.ItemSub.copy(fontSize = 14.sp),
                color = TT.Gray45,
                modifier = Modifier.padding(bottom = 10.dp),
            )
        }
        n.moments.firstOrNull { it.isPeak }?.label?.let {
            Spacer(Modifier.height(4.dp))
            Text(it, style = TTType.BodySmall, color = TT.Paper)
        }
        Spacer(Modifier.height(22.dp))
        if (!looked) return@Column
        FeedPostConfirm(
            night = n,
            serverEventId = eventId,
            series = series,
            skin = skin ?: n.skin ?: Skin.PINK,
            onPosted = { target ->
                // Back to the feed, where the post now is: the one behind this
                // screen reloads as it comes back; from anywhere else, it opens.
                if (nav.previousBackStackEntry?.destination?.route == Routes.EventFeed) {
                    nav.popBackStack()
                } else {
                    nav.navigate(Routes.eventFeed(target, n.eventName)) {
                        popUpTo(Routes.ShowToFeed) { inclusive = true }
                    }
                }
            },
            onCancel = { nav.popBackStack() },
        )
    }
}

/**
 * The confirmation itself (extracted from the card's done screen, 28/09):
 * who will see it, "Pode mostrar", "Agora não", and the server's own words
 * when it says no (#58). When the night cannot be posted the button stays,
 * and the reason is under it ([FeedGate]).
 */
@Composable
fun FeedPostConfirm(
    night: Night,
    serverEventId: String?,
    series: ServerSeries?,
    skin: Skin,
    onPosted: (serverEventId: String) -> Unit,
    onCancel: () -> Unit,
) {
    val container = appContainer()
    val scope = rememberCoroutineScope()
    val user by container.prefs.state.collectAsStateWithLifecycle(initialValue = null)
    var posting by remember { mutableStateOf(false) }
    var failure by remember { mutableStateOf<String?>(null) }
    val postFailedText = stringResource(R.string.feed_post_failed)
    val postOfflineText = stringResource(R.string.feed_post_offline)
    val postSignedOutText = stringResource(R.string.feed_post_signed_out)

    val closed = FeedGate.closed(
        serverSessionId = night.serverSessionId,
        serverEventId = serverEventId,
        eventReadings = night.eventReadings,
        ownerUserId = night.ownerUserId,
        viewerId = user?.session?.userId,
        keepingNights = user?.granted(ConsentText.KEEP_NIGHT),
    )
    val toTour = series != null

    Text(
        stringResource(if (toTour) R.string.feed_post_consent_tour else R.string.feed_post_consent),
        style = TTType.Body,
        color = TT.Gray45,
    )
    Spacer(Modifier.height(18.dp))
    TTButton(
        stringResource(if (posting) R.string.feed_post_running else R.string.feed_post_confirm),
        TTButtonStyle.Rose,
        enabled = closed == null && !posting,
        onDeclined = if (closed != null) ({ failure = null }) else null,
        onClick = {
            val target = serverEventId ?: return@TTButton
            val sessionId = night.serverSessionId ?: return@TTButton
            posting = true
            failure = null
            scope.launch {
                val result = container.social.post(
                    serverEventId = target,
                    serverSessionId = sessionId,
                    bpm = night.peakBpm,
                    at = night.peakAt,
                    label = night.moments.firstOrNull { it.isPeak }?.label,
                    quote = null,
                    skin = skin.name,
                    toSeries = toTour,
                )
                posting = false
                when (result) {
                    PostResult.Posted -> onPosted(target)
                    is PostResult.Refused -> failure = result.detail
                    PostResult.SignedOut -> failure = postSignedOutText
                    is PostResult.Failed -> failure = if (result.offline) postOfflineText else postFailedText
                }
            }
        },
    )
    // The reason sits under the button it shuts, where the eye already is.
    val why = closed?.let { feedClosedText(it) } ?: failure
    why?.let {
        Spacer(Modifier.height(8.dp))
        Text(it, style = TTType.BodySmall, color = TT.Rose)
    }
    Spacer(Modifier.height(10.dp))
    TTButton(
        stringResource(R.string.feed_post_cancel),
        TTButtonStyle.OutlineOnDark,
        enabled = !posting,
        onClick = onCancel,
    )
}
