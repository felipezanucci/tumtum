package cc.tumtum.app.ui.screens.card

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cc.tumtum.app.R
import cc.tumtum.app.data.CardPhotoStore
import cc.tumtum.app.data.api.ServerSeries
import cc.tumtum.app.data.repo.PostResult
import cc.tumtum.app.domain.Night
import cc.tumtum.app.domain.Skin
import cc.tumtum.app.export.CardRenderer
import cc.tumtum.app.export.CardSticker
import cc.tumtum.app.export.ShareTargets
import cc.tumtum.app.export.VideoCard
import cc.tumtum.app.export.VideoFrame
import cc.tumtum.app.ui.Fmt
import cc.tumtum.app.ui.components.BackArrow
import cc.tumtum.app.ui.components.ShareCardView
import cc.tumtum.app.ui.components.TTButton
import cc.tumtum.app.ui.components.TTButtonStyle
import cc.tumtum.app.ui.nav.Routes
import cc.tumtum.app.ui.nav.appContainer
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * What the person put behind the black card: their own photo, or their own
 * video. A video's preview is its first frame — what the card will sit over.
 */
private sealed interface CardMedia {
    val preview: Bitmap

    data class Photo(override val preview: Bitmap) : CardMedia
    data class Video(
        val uri: Uri,
        override val preview: Bitmap,
        val durationMs: Long?,
    ) : CardMedia
}

/** Where a card goes (#60): one button per network, each by its own best road. */
private enum class ShareTo { Instagram, WhatsApp, Copy, Save, More }

/**
 * Seu card (UI kit do core loop). Compartilhar é sempre ativo: nada sai
 * daqui sem o toque em Compartilhar. "Postar no feed" saiu em 19/09 (item 37):
 * postava num repositório falso deste celular e confirmava que a galera
 * podia sentir — ninguém podia. Volta quando o feed for o do servidor.
 *
 * Compartilhar abre os destinos, um botão por rede (#60, 23/09 — o modelo do
 * Spotify e do Strava): Instagram recebe o vídeo de fundo e o card solto, que
 * se arrasta; WhatsApp e "Mais apps" recebem o arquivo pronto, com o card
 * gravado; "Copiar" e "Salvar" dão o card sozinho, transparente, para
 * qualquer editor. Ver [ShareTargets].
 */
@Composable
fun CardScreen(nav: NavHostController, nightId: Long, skin: Skin) {
    val container = appContainer()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val night by container.nights.night(nightId).collectAsStateWithLifecycle(initialValue = null)
    var sharing by remember { mutableStateOf(false) }
    // The encoder's own figure while a video is being burned, or null. Never a
    // made-up percentage: a bar that moves on a timer is a lie about work.
    var burning by remember { mutableStateOf<Int?>(null) }
    // The share sheet came back. That is all it means: whether the card was
    // sent, nobody here knows, and the screen says only what is true.
    var cameBack by remember { mutableStateOf(false) }
    var failure by remember { mutableStateOf<Int?>(null) }
    // What a copy or a save did — said where the eye is, since neither leaves
    // the screen (a step the app takes is said, 21/09).
    var notice by remember { mutableStateOf<Int?>(null) }
    // The destinations are open (#60).
    var choosing by remember { mutableStateOf(false) }
    val hasInstagram = remember { ShareTargets.installed(context, ShareTargets.INSTAGRAM) }
    val whatsapp = remember { ShareTargets.whatsapp(context) }
    val shareLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { cameBack = true }
    val nights by container.nights.nights().collectAsStateWithLifecycle(initialValue = emptyList())
    var media by remember { mutableStateOf<CardMedia?>(null) }
    var loadingMedia by remember { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        loadingMedia = true
        failure = null
        scope.launch {
            media = withContext(Dispatchers.IO) {
                val type = context.contentResolver.getType(uri).orEmpty()
                if (type.startsWith("video/")) {
                    VideoFrame.first(context, uri)?.let {
                        CardMedia.Video(uri, it, VideoCard.durationMs(context, uri))
                    }
                } else {
                    CardRenderer.loadPhoto(context, uri)?.let { CardMedia.Photo(it) }
                }
            }
            if (media == null) failure = R.string.card_media_unreadable
            loadingMedia = false
        }
    }
    val n = night ?: return
    // The photo behind the last shared black card comes back with the night
    // (21/09): "Compartilhar de novo" and the gallery show the card that went out.
    var photoRestored by remember { mutableStateOf(false) }
    LaunchedEffect(n.photoPath) {
        if (!photoRestored && skin == Skin.BLACK && n.photoPath != null) {
            media = withContext(Dispatchers.IO) { CardPhotoStore.load(n.photoPath) }?.let { CardMedia.Photo(it) }
        }
        photoRestored = true
    }

    val cardTitle = stringResource(R.string.reveal_default_title)
    val cardMeta = stringResource(R.string.reveal_bpm) + " " + stringResource(R.string.reveal_at, Fmt.hour(n.peakAt))
    // The event only — the hour is already in "bpm às 22h12" beside it (A2).
    val cardChip = n.eventName.uppercase()
    val video = media as? CardMedia.Video
    val busy = sharing || loadingMedia

    /** The night keeps what its last shared card looked like — a video leaves its first frame. */
    suspend fun publish(chosen: CardMedia?) {
        val photoPath = if (skin == Skin.BLACK && chosen != null) {
            CardPhotoStore.save(context, n.id, chosen.preview, n.photoPath)
        } else {
            CardPhotoStore.delete(n.photoPath)
            null
        }
        container.nights.publish(n.id, skin, photoPath)
    }

    fun render(photo: Bitmap? = null, sticker: Boolean = false): Bitmap =
        CardRenderer.render(context, n, skin, cardTitle, cardMeta, cardChip, photo = photo, sticker = sticker)

    /** The card alone: on black, a sticker cut to its own block; on the other skins, the whole card. */
    fun cardAlone(): Bitmap = if (skin == Skin.BLACK) CardSticker.crop(render(sticker = true)) else render()

    /** The finished file — the card burned into the video, or the card as a picture. */
    suspend fun finished(chosen: CardMedia?): Pair<java.io.File, String>? =
        if (chosen is CardMedia.Video) {
            burning = 0
            val sticker = withContext(Dispatchers.IO) { render(sticker = true) }
            val file = VideoCard.burn(context, chosen.uri, sticker, n.id) { burning = it }
            burning = null
            file?.let { it to "video/mp4" }
        } else {
            withContext(Dispatchers.IO) {
                runCatching {
                    val png = "tumtum-${n.id}-${skin.name.lowercase()}.png"
                    CardRenderer.writePng(context, render(photo = chosen?.preview), png) to "image/png"
                }.getOrNull()
            }
        }

    /** Instagram's Story editor: the person's video or photo behind, the card on top and movable. */
    suspend fun instagram(chosen: CardMedia?): android.content.Intent? = withContext(Dispatchers.IO) {
        runCatching {
            if (skin == Skin.BLACK && chosen != null) {
                val sticker = CardRenderer.writePng(context, cardAlone(), "tumtum-${n.id}-sticker.png")
                val (background, mime) = when (chosen) {
                    is CardMedia.Video -> ShareTargets.copyVideo(context, chosen.uri, n.id)
                        ?: return@runCatching null
                    is CardMedia.Photo ->
                        ShareTargets.writeJpeg(context, chosen.preview, "story-${n.id}.jpg") to "image/jpeg"
                }
                ShareTargets.instagramStory(context, background, mime, sticker)
            } else {
                val card = CardRenderer.writePng(context, render(), "tumtum-${n.id}-${skin.name.lowercase()}.png")
                ShareTargets.instagramStory(context, card, "image/png", null)
            }
        }.getOrNull()
    }

    fun go(target: ShareTo) {
        sharing = true
        failure = null
        notice = null
        val chosen = media
        scope.launch {
            publish(chosen)
            when (target) {
                ShareTo.Instagram -> {
                    val intent = instagram(chosen)
                    if (intent == null) {
                        failure = R.string.card_share_instagram_failed
                    } else {
                        runCatching { shareLauncher.launch(intent) }
                            .onFailure { failure = R.string.card_share_instagram_failed }
                    }
                }
                ShareTo.WhatsApp, ShareTo.More -> {
                    val file = finished(chosen)
                    if (file == null) {
                        failure = if (chosen is CardMedia.Video) {
                            R.string.card_share_video_failed
                        } else {
                            R.string.card_share_failed
                        }
                    } else {
                        val (f, mime) = file
                        val app = whatsapp?.takeIf { target == ShareTo.WhatsApp }
                        val intent = app?.let { ShareTargets.toApp(context, it, f, mime) }
                            ?: CardRenderer.shareFileIntent(context, f, mime)
                        runCatching { shareLauncher.launch(intent) }
                            .onFailure { failure = R.string.card_share_failed }
                    }
                }
                ShareTo.Copy -> {
                    val ok = withContext(Dispatchers.IO) {
                        runCatching {
                            ShareTargets.copy(context, CardRenderer.writePng(context, cardAlone(), "tumtum-${n.id}-card.png"))
                        }.getOrDefault(false)
                    }
                    if (ok) notice = R.string.card_copied else failure = R.string.card_copy_failed
                }
                ShareTo.Save -> {
                    val ok = withContext(Dispatchers.IO) {
                        runCatching {
                            ShareTargets.saveToGallery(context, cardAlone(), "tumtum-${n.id}-${System.currentTimeMillis()}.png")
                        }.getOrDefault(false)
                    }
                    if (ok) notice = R.string.card_saved else failure = R.string.card_save_failed
                }
            }
            sharing = false
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(TT.Night)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 26.dp, end = 26.dp, top = 22.dp, bottom = 24.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            BackArrow(onClick = { nav.popBackStack() }, onDark = true)
            Spacer(Modifier.weight(1f))
            Text(stringResource(R.string.card_label), style = TTType.MetaSmall, color = TT.Acid)
        }
        // The preview takes the room that is left and no more (#41, 22/09).
        // It was a fixed 214 dp wide — 380 dp tall — inside a weighted box, so
        // when the done state stacked its title, stats and buttons below, the
        // box shrank under the card and the card drew straight over "8 noites
        // em 2026 · 10 momentos". Its width now follows the height it gets.
        BoxWithConstraints(
            Modifier.weight(1f).fillMaxWidth().padding(vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            ShareCardView(
                skin = skin,
                title = cardTitle,
                bpm = n.peakBpm,
                meta = cardMeta,
                chip = cardChip,
                width = minOf(214.dp, maxHeight * (9f / 16f), maxWidth),
                curveSamples = if (skin == Skin.BLACK) n.samples else null,
                curveWindow = if (skin == Skin.BLACK) n.startAt to n.endAt else null,
                photo = media?.preview?.asImageBitmap(),
            )
        }
        // While the destinations are open the photo/video choice is made, and
        // its buttons give the room to the destinations.
        if (skin == Skin.BLACK && !cameBack && !choosing) {
            // A button in Toxic Yellow, not a line of small caps (b145). With
            // something already behind the card it splits in two, because going
            // from photo to video used to mean tirar-and-then-colocar — two
            // steps where one would do.
            if (media == null) {
                TTButton(
                    stringResource(if (loadingMedia) R.string.card_media_loading else R.string.card_media_add),
                    TTButtonStyle.OutlineAcid,
                    enabled = !busy,
                    onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)) },
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TTButton(
                        stringResource(if (loadingMedia) R.string.card_media_loading else R.string.card_media_swap),
                        TTButtonStyle.OutlineAcid,
                        enabled = !busy,
                        onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)) },
                        modifier = Modifier.weight(1f),
                    )
                    TTButton(
                        stringResource(R.string.card_media_remove),
                        TTButtonStyle.OutlineOnDark,
                        enabled = !busy,
                        onClick = { media = null },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            val trimmed = video?.durationMs?.let { it > VideoCard.MAX_MS } == true
            Text(
                when {
                    trimmed -> stringResource(R.string.card_video_trimmed, (VideoCard.MAX_MS / 1000).toInt())
                    video != null -> stringResource(R.string.card_video_hint)
                    else -> stringResource(R.string.card_media_hint)
                },
                style = TTType.BodySmall,
                color = if (trimmed) TT.Acid else TT.Gray45,
                maxLines = 2,
            )
            Spacer(Modifier.height(10.dp))
        }
        failure?.let {
            Text(stringResource(it), style = TTType.BodySmall, color = TT.Rose, maxLines = 3)
            Spacer(Modifier.height(8.dp))
        }
        notice?.let {
            Text(stringResource(it), style = TTType.BodySmall, color = TT.Acid, maxLines = 3)
            Spacer(Modifier.height(8.dp))
        }
        if (cameBack) {
            // The loop ends on a TumTum screen, not on Android's share sheet
            // (§5.3 of the 19/09 research — peak–end). The card exists and the
            // night carries its skin: that is the claim, and it is true.
            Text(
                stringResource(R.string.card_done_title),
                style = TTType.ShoutSmall.copy(fontSize = 23.sp, lineHeight = 24.5.sp),
                color = TT.Paper,
            )
            Spacer(Modifier.height(4.dp))
            val year = java.time.Year.now().value
            val nightsThisYear = nights.count { it.date.atZone(java.time.ZoneId.systemDefault()).year == year }
            Text(
                stringResource(R.string.card_done_stats, nightsThisYear, year, nights.sumOf { it.momentCount }),
                style = TTType.BodySmall,
                color = TT.Gray45,
            )
            Spacer(Modifier.height(14.dp))
            DoneActions(
                nav = nav,
                nightId = n.id,
                night = n,
                skin = skin,
                onShareAgain = {
                    cameBack = false
                    choosing = true
                },
            )
        } else if (choosing) {
            ShareChoices(
                hasInstagram = hasInstagram,
                hasWhatsapp = whatsapp != null,
                canSave = ShareTargets.canSaveToGallery,
                busy = busy,
                status = when {
                    burning != null -> stringResource(R.string.card_share_burning, burning ?: 0)
                    sharing -> stringResource(R.string.card_share_running)
                    else -> null
                },
                onPick = { go(it) },
                onBack = {
                    choosing = false
                    notice = null
                },
            )
        } else {
            // Compartilhar é sempre ativo (§1): abre os destinos (#60).
            TTButton(
                stringResource(R.string.card_share),
                TTButtonStyle.Rose,
                enabled = !busy,
                onClick = {
                    failure = null
                    choosing = true
                },
            )
        }
        Spacer(Modifier.height(2.dp))
    }
}

/**
 * "Mostrar pra galera" — the one place a moment becomes public.
 *
 * Posting is **not** sharing. The share sheet sends a picture the person
 * controls to people they chose; this puts their heart rate, at a named
 * minute, in front of strangers who happen to have been at the same event.
 * That is health data, so:
 *
 *  - it is asked at the moment of posting, never agreed once in a setting;
 *  - the sentence says what will be visible and to whom, in plain words;
 *  - it is undoable, and the feed shows *tirar do feed* on your own posts.
 *
 * **One question** (#65, 23/09). A show in a tour posts to the tour — the
 * people at every date — and the sentence says exactly that; a show on its
 * own posts to the people who were there. The two-way choice ("rolê e
 * turnê" / "só pro rolê") is gone with the second feed it chose between.
 *
 * It appears only when there is a feed to post to: the night must have
 * reached the server and belong to an event that exists there. Otherwise
 * there is nothing honest to offer, so nothing is offered.
 */
@Composable
private fun DoneActions(
    nav: NavHostController,
    nightId: Long,
    night: Night,
    skin: Skin,
    onShareAgain: () -> Unit,
) {
    // **One Pink button at a time** (#41, 22/09). The done screen stacked five
    // blocks with two Pink buttons in them — "Ver a galeria" and "Pode
    // mostrar" — so the eye had no first place to land. Now the screen has
    // one primary act, chosen by where the person is:
    //
    //  - the night can go to its feed → "Mostrar pra galera…";
    //  - they are being asked       → "Pode mostrar", and nothing else;
    //  - it is posted, or cannot be → "Ver a galeria".
    //
    // Everything else is quiet: outlined, or a line of text.
    val container = appContainer()
    val scope = rememberCoroutineScope()
    var eventId by remember(nightId) { mutableStateOf<String?>(null) }
    var asking by remember { mutableStateOf(false) }
    var posting by remember { mutableStateOf(false) }
    var posted by remember(nightId) { mutableStateOf(false) }
    // What the last attempt got back, said in words (#58): the server's own
    // sentence when it refused, never a generic "não deu".
    var failure by remember { mutableStateOf<String?>(null) }
    val user by container.prefs.state.collectAsStateWithLifecycle(initialValue = null)

    // The tour above this night, if any (#33): it changes what "posting" can
    // mean, so the consent has to name it.
    var series by remember(nightId) { mutableStateOf<ServerSeries?>(null) }

    LaunchedEffect(nightId) {
        eventId = container.nights.serverEventIdFor(nightId)
        eventId?.let { series = container.social.seriesOf(it) }
    }

    val target = eventId
    val sessionId = night.serverSessionId
    // A night belongs to the account that uploaded it (#58). Signed in as
    // another, the server would refuse — so the app says so instead of
    // offering an act that cannot succeed.
    val signedInAs = user?.session?.userId
    val otherAccount = night.ownerUserId != null && signedInAs != null && night.ownerUserId != signedInAs
    val canPost = target != null && sessionId != null && !otherAccount
    val postFailedText = stringResource(R.string.feed_post_failed)
    val postOfflineText = stringResource(R.string.feed_post_offline)
    val postSignedOutText = stringResource(R.string.feed_post_signed_out)

    val openGallery = {
        nav.navigate(Routes.Gallery) {
            popUpTo(Routes.Feed) { saveState = true }
            launchSingleTop = true
        }
    }

    fun send(toSeries: Boolean) {
        posting = true
        scope.launch {
            val result = container.social.post(
                serverEventId = target!!,
                serverSessionId = sessionId!!,
                bpm = night.peakBpm,
                at = night.peakAt,
                label = night.moments.firstOrNull { it.isPeak }?.label,
                quote = null,
                skin = skin.name,
                toSeries = toSeries,
            )
            posted = result is PostResult.Posted
            failure = when (result) {
                PostResult.Posted -> null
                is PostResult.Refused -> result.detail
                PostResult.SignedOut -> postSignedOutText
                is PostResult.Failed -> if (result.offline) postOfflineText else postFailedText
            }
            asking = false
            posting = false
        }
    }

    val toTour = series != null
    if (asking && canPost) {
        Text(
            stringResource(if (toTour) R.string.feed_post_consent_tour else R.string.feed_post_consent),
            style = TTType.BodySmall,
            color = TT.Gray45,
        )
        Spacer(Modifier.height(12.dp))
        TTButton(
            stringResource(if (posting) R.string.feed_post_running else R.string.feed_post_confirm),
            TTButtonStyle.Rose,
            enabled = !posting,
            onClick = { send(toSeries = toTour) },
        )
        Spacer(Modifier.height(8.dp))
        TTButton(
            stringResource(R.string.feed_post_cancel),
            TTButtonStyle.OutlineOnDark,
            enabled = !posting,
            onClick = { asking = false },
        )
        return
    }

    if (canPost && !posted) {
        TTButton(
            stringResource(if (toTour) R.string.feed_post_cta_tour else R.string.feed_post_cta),
            TTButtonStyle.Rose,
            onClick = { asking = true; failure = null },
        )
        failure?.let {
            Spacer(Modifier.height(6.dp))
            Text(it, style = TTType.BodySmall, color = TT.Rose)
        }
        Spacer(Modifier.height(10.dp))
        TTButton(
            stringResource(R.string.card_done_gallery),
            TTButtonStyle.OutlineOnDark,
            onClick = openGallery,
        )
    } else {
        if (posted) {
            Text(stringResource(R.string.feed_post_done), style = TTType.BodySmall, color = TT.Acid)
            Spacer(Modifier.height(12.dp))
        } else if (otherAccount) {
            Text(stringResource(R.string.feed_post_other_account), style = TTType.BodySmall, color = TT.Gray45)
            Spacer(Modifier.height(12.dp))
        }
        TTButton(
            stringResource(R.string.card_done_gallery),
            TTButtonStyle.Rose,
            onClick = openGallery,
        )
    }
    Spacer(Modifier.height(4.dp))
    Text(
        stringResource(R.string.card_share_again),
        style = TTType.Button.copy(fontSize = 14.sp),
        color = TT.Paper,
        modifier = Modifier.clickable(onClick = onShareAgain).padding(vertical = 10.dp),
    )
}

/**
 * The destinations (#60): the networks on this phone, each by its own best
 * road, then the card alone to copy or save, then every other app. All
 * outlined and equal — Spotify's row does not rank the networks either.
 */
@Composable
private fun ShareChoices(
    hasInstagram: Boolean,
    hasWhatsapp: Boolean,
    canSave: Boolean,
    busy: Boolean,
    status: String?,
    onPick: (ShareTo) -> Unit,
    onBack: () -> Unit,
) {
    Text(stringResource(R.string.card_share_to), style = TTType.MetaSmall, color = TT.Acid)
    Spacer(Modifier.height(8.dp))
    if (hasInstagram || hasWhatsapp) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (hasInstagram) {
                TTButton(
                    stringResource(R.string.card_share_instagram),
                    TTButtonStyle.OutlineOnDark,
                    enabled = !busy,
                    onClick = { onPick(ShareTo.Instagram) },
                    modifier = Modifier.weight(1f),
                )
            }
            if (hasWhatsapp) {
                TTButton(
                    stringResource(R.string.card_share_whatsapp),
                    TTButtonStyle.OutlineOnDark,
                    enabled = !busy,
                    onClick = { onPick(ShareTo.WhatsApp) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TTButton(
            stringResource(R.string.card_share_copy),
            TTButtonStyle.OutlineOnDark,
            enabled = !busy,
            onClick = { onPick(ShareTo.Copy) },
            modifier = Modifier.weight(1f),
        )
        if (canSave) {
            TTButton(
                stringResource(R.string.card_share_save),
                TTButtonStyle.OutlineOnDark,
                enabled = !busy,
                onClick = { onPick(ShareTo.Save) },
                modifier = Modifier.weight(1f),
            )
        }
    }
    Spacer(Modifier.height(8.dp))
    TTButton(
        stringResource(R.string.card_share_more),
        TTButtonStyle.OutlineOnDark,
        enabled = !busy,
        onClick = { onPick(ShareTo.More) },
    )
    Spacer(Modifier.height(6.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            status ?: stringResource(R.string.card_share_back),
            style = TTType.Button.copy(fontSize = 14.sp),
            color = if (status != null) TT.Acid else TT.Paper,
            modifier = Modifier
                .clickable(enabled = !busy, onClick = onBack)
                .padding(vertical = 10.dp),
        )
    }
    if (hasInstagram && status == null) {
        Text(stringResource(R.string.card_share_instagram_hint), style = TTType.Footnote, color = TT.Gray45)
    }
}
