package cc.tumtum.app.ui.screens.card

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cc.tumtum.app.R
import cc.tumtum.app.data.CardPhotoStore
import cc.tumtum.app.data.LocalFiles
import cc.tumtum.app.domain.CardCopy
import cc.tumtum.app.domain.ConsentText
import cc.tumtum.app.domain.FeedGate
import cc.tumtum.app.ui.components.cardTitleText
import cc.tumtum.app.ui.components.SystemBars
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import cc.tumtum.app.data.api.ServerSeries
import cc.tumtum.app.domain.Skin
import cc.tumtum.app.export.CardRenderer
import cc.tumtum.app.export.CardSticker
import cc.tumtum.app.export.ShareTargets
import cc.tumtum.app.export.VideoCard
import cc.tumtum.app.export.VideoFrame
import cc.tumtum.app.ui.Fmt
import cc.tumtum.app.ui.components.BackArrow
import cc.tumtum.app.ui.components.feedClosedText
import cc.tumtum.app.ui.components.skinColor
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
private enum class ShareTo { Instagram, Facebook, Snapchat, TikTok, WhatsApp, WhatsAppStatus, Copy, Save, More }

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
    // What that figure is the progress of: the card burned into the video, or
    // the video only re-encoded for Snapchat, which has no card in it.
    var burnLabel by remember { mutableStateOf(R.string.card_share_burning) }
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
    val hasFacebook = remember { ShareTargets.installed(context, ShareTargets.FACEBOOK) }
    val hasSnapchat = remember { ShareTargets.installed(context, ShareTargets.SNAPCHAT) }
    val tiktok = remember { ShareTargets.tiktok(context) }
    val whatsapp = remember { ShareTargets.whatsapp(context) }
    val shareLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        cameBack = true
        // The receiving app has had its read (26/09): cards, stickers and
        // videos older than an hour leave the share cache.
        scope.launch(Dispatchers.IO) { LocalFiles.pruneShareCache(context) }
    }
    // What the card shows (26/09): the peak, its hour, the event — each can
    // be taken off before sharing, and the number can go to the ten below.
    var showHour by remember { mutableStateOf(true) }
    var showEvent by remember { mutableStateOf(true) }
    var exactBpm by remember { mutableStateOf(true) }
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
                    // Kept past this visit, so the card can reopen the video
                    // itself and not only its first frame (24/09).
                    runCatching {
                        context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
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
    // A video comes back as the video (24/09), not as its first frame posing
    // as one; and when the video is gone from the phone, the screen says the
    // card will go out over a still, instead of letting it look the same.
    LaunchedEffect(n.photoPath) {
        if (!photoRestored && skin == Skin.BLACK && n.photoPath != null) {
            val (restored, videoGone) = withContext<Pair<CardMedia?, Boolean>>(Dispatchers.IO) {
                val still = CardPhotoStore.load(n.photoPath) ?: return@withContext null to false
                val uri = CardPhotoStore.videoOf(n.photoPath) ?: return@withContext CardMedia.Photo(still) to false
                val readable = runCatching {
                    context.contentResolver.openFileDescriptor(uri, "r")?.use { true } ?: false
                }.getOrDefault(false)
                if (readable) {
                    CardMedia.Video(uri, still, VideoCard.durationMs(context, uri)) to false
                } else {
                    CardMedia.Photo(still) to true
                }
            }
            media = restored
            if (videoGone) notice = R.string.card_video_gone
        }
        photoRestored = true
    }

    // The title the night's numbers prove (26/09) — with no hour in it when
    // the hour is hidden, or the switch would be a lie.
    val cardTitle = cardTitleText(
        CardCopy.title(
            n.peakBpm,
            CardCopy.averageBpm(n.samples),
            if (showHour) Fmt.hour(n.peakAt) else null,
            hasMoments = n.moments.isNotEmpty(),
        ),
    )
    val cardMeta = if (showHour) {
        stringResource(R.string.reveal_bpm) + " " + stringResource(R.string.reveal_at, Fmt.hour(n.peakAt))
    } else {
        stringResource(R.string.reveal_bpm)
    }
    // The event only — the hour is already in "bpm às 22h12" beside it (A2).
    val cardChip = if (showEvent) n.eventName.uppercase() else null
    val cardBpm = CardCopy.bpmLabel(n.peakBpm, exactBpm)
    val video = media as? CardMedia.Video
    val busy = sharing || loadingMedia

    // The event's feed, for "Feed do evento" and the done screen (28/09, item
    // 31): looked up once, and no reason is given before it has been.
    val user by container.prefs.state.collectAsStateWithLifecycle(initialValue = null)
    var feedEventId by remember(nightId) { mutableStateOf<String?>(null) }
    var feedSeries by remember(nightId) { mutableStateOf<ServerSeries?>(null) }
    var feedLooked by remember(nightId) { mutableStateOf(false) }
    LaunchedEffect(nightId) {
        feedEventId = container.nights.serverEventIdFor(nightId)
        feedEventId?.let { feedSeries = container.social.seriesOf(it) }
        feedLooked = true
    }
    val feedClosed = FeedGate.closed(
        serverSessionId = n.serverSessionId,
        serverEventId = feedEventId,
        eventReadings = n.eventReadings,
        ownerUserId = n.ownerUserId,
        viewerId = user?.session?.userId,
        keepingNights = user?.granted(ConsentText.KEEP_NIGHT),
    )
    // What goes to the feed is decided on its own screen — the quiet kind (28/09).
    val openFeedPost = { nav.navigate(Routes.showToFeed(n.id, skin)) }

    /** The night keeps what its last shared card looked like: a photo, or a video's first frame and its address. */
    suspend fun publish(chosen: CardMedia?) {
        val photoPath = if (skin == Skin.BLACK && chosen != null) {
            CardPhotoStore.save(context, n.id, chosen.preview, n.photoPath)?.also { path ->
                if (chosen is CardMedia.Video) CardPhotoStore.saveVideo(path, chosen.uri)
            }
        } else {
            CardPhotoStore.delete(n.photoPath)
            null
        }
        container.nights.publish(n.id, skin, photoPath)
    }

    fun render(photo: Bitmap? = null, sticker: Boolean = false, bare: Boolean = false): Bitmap =
        CardRenderer.render(
            context, n, skin, cardTitle, cardMeta, cardChip,
            photo = photo, sticker = sticker, bare = bare, bpmLabel = cardBpm,
        )

    // The preview is the card that goes out (28/09, items 27 and 29): the
    // same renderer, the same title, hour, event and number, drawn once per
    // change and shown scaled. The Compose copy of the layout had its own
    // minimum type sizes, so at preview scale the event box was squeezed to
    // "T…" over a card that printed "TESTE". Over a video it is the first
    // frame under the card — what the sticker is burned onto.
    var preview by remember { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(skin, cardTitle, cardMeta, cardChip, cardBpm, media, n.id, n.samples.size, n.peakBpm) {
        val chosen = media
        preview = withContext(Dispatchers.Default) { runCatching { render(photo = chosen?.preview) }.getOrNull() }
    }

    /**
     * The card alone: on black, a sticker cut to its own block with no wash
     * behind it, since the person moves it where it reads; on the other
     * skins, the whole card.
     */
    fun cardAlone(): Bitmap =
        if (skin == Skin.BLACK) CardSticker.crop(render(sticker = true, bare = true)) else render()

    /** The finished file — the card burned into the video, or the card as a picture. */
    suspend fun finished(chosen: CardMedia?): Pair<java.io.File, String>? =
        if (chosen is CardMedia.Video) {
            burnLabel = R.string.card_share_burning
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

    /**
     * A story editor — Instagram's, Facebook's or Snapchat's, one shape: the
     * person's video or photo behind, the card on top and movable.
     */
    suspend fun story(chosen: CardMedia?, target: ShareTo): android.content.Intent? = withContext(Dispatchers.IO) {
        runCatching {
            fun editorFor(background: java.io.File, mime: String, sticker: java.io.File?, aspect: Float) = when (target) {
                ShareTo.Facebook -> ShareTargets.facebookStory(context, background, mime, sticker)
                ShareTo.Snapchat -> ShareTargets.snapchatPreview(context, background, mime, sticker, aspect)
                else -> ShareTargets.instagramStory(context, background, mime, sticker)
            }
            if (skin == Skin.BLACK && chosen != null) {
                // Snapchat takes the card at the screen's width, margins and
                // all; the other editors take it cut to its block.
                val alone = if (target == ShareTo.Snapchat) {
                    CardSticker.cropRows(render(sticker = true, bare = true))
                } else {
                    cardAlone()
                }
                val aspect = alone.height.toFloat() / alone.width
                val sticker = if (target == ShareTo.Snapchat) {
                    ShareTargets.snapSticker(context, alone, "tumtum-${n.id}-snap-sticker.png")
                } else {
                    CardRenderer.writePng(context, alone, "tumtum-${n.id}-sticker.png")
                }
                val (background, mime) = when {
                    // Snapchat's preview wants a 9:16 background (Snap's own
                    // note: any other shape breaks its canvas), and a phone's
                    // recording may be another shape or a codec its preview
                    // leaves frozen on the first frame — tested 24/09. So it
                    // gets the video re-encoded the way the card's own video
                    // is, only with nothing drawn on it.
                    target == ShareTo.Snapchat && chosen is CardMedia.Video -> {
                        withContext(Dispatchers.Main) {
                            burnLabel = R.string.card_share_preparing_snapchat
                            burning = 0
                        }
                        val file = VideoCard.burn(context, chosen.uri, null, n.id, "snap-${n.id}.mp4") {
                            burning = it
                        }
                        withContext(Dispatchers.Main) {
                            burning = null
                            // Said as what it is, not as Snapchat refusing.
                            if (file == null) failure = R.string.card_share_snapchat_video_failed
                        }
                        (file ?: return@runCatching null) to "video/mp4"
                    }
                    target == ShareTo.Snapchat && chosen is CardMedia.Photo ->
                        ShareTargets.writeJpeg(context, ShareTargets.storyFrame(chosen.preview), "snap-${n.id}.jpg") to "image/jpeg"
                    chosen is CardMedia.Video -> ShareTargets.copyVideo(context, chosen.uri, n.id)
                        ?: return@runCatching null
                    chosen is CardMedia.Photo ->
                        ShareTargets.writeJpeg(context, chosen.preview, "story-${n.id}.jpg") to "image/jpeg"
                    else -> return@runCatching null
                }
                editorFor(background, mime, sticker, aspect)
            } else {
                val card = CardRenderer.writePng(context, render(), "tumtum-${n.id}-${skin.name.lowercase()}.png")
                editorFor(card, "image/png", null, 1f)
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
                ShareTo.Instagram, ShareTo.Facebook, ShareTo.Snapchat -> {
                    val refused = when (target) {
                        ShareTo.Facebook -> R.string.card_share_facebook_failed
                        ShareTo.Snapchat -> R.string.card_share_snapchat_failed
                        else -> R.string.card_share_instagram_failed
                    }
                    val intent = story(chosen, target)
                    if (intent == null) {
                        if (failure == null) failure = refused
                    } else if (target == ShareTo.Snapchat) {
                        // Launched the way Snap's own Creative Kit Lite sample
                        // does: a new task, not for a result. On b180 and b182
                        // Snapchat showed the video's first frame and never
                        // played it; this is the one difference left between
                        // our intent and Snap's, so it is the next thing tried,
                        // not a known cause (24/09). The screen still says only
                        // that the person came back.
                        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        runCatching { context.startActivity(intent) }
                            .onSuccess {
                                cameBack = true
                                scope.launch(Dispatchers.IO) { LocalFiles.pruneShareCache(context) }
                            }
                            .onFailure { failure = refused }
                    } else {
                        runCatching { shareLauncher.launch(intent) }
                            .onFailure { failure = refused }
                    }
                }
                ShareTo.WhatsApp, ShareTo.WhatsAppStatus, ShareTo.More, ShareTo.TikTok -> {
                    val file = finished(chosen)
                    if (file == null) {
                        failure = if (chosen is CardMedia.Video) {
                            R.string.card_share_video_failed
                        } else {
                            R.string.card_share_failed
                        }
                    } else {
                        val (f, mime) = file
                        val intent = when (target) {
                            // TikTok takes no sticker: the finished file goes
                            // to its editor through Share Kit.
                            ShareTo.TikTok -> tiktok?.let { ShareTargets.tiktokShare(context, it, f, mime) }
                            ShareTo.WhatsApp -> whatsapp?.let { ShareTargets.toApp(context, it, f, mime) }
                            ShareTo.WhatsAppStatus -> whatsapp?.let { ShareTargets.whatsappStatus(context, it, f, mime) }
                            else -> null
                        } ?: CardRenderer.shareFileIntent(context, f, mime)
                        val refused = if (target == ShareTo.TikTok) R.string.card_share_tiktok_failed else R.string.card_share_failed
                        runCatching { shareLauncher.launch(intent) }
                            .onFailure {
                                // Status refused outright (#63): the chat picker,
                                // where Meu status is the first row — and the
                                // screen says the road changed.
                                val chats = if (target == ShareTo.WhatsAppStatus) {
                                    whatsapp?.let { ShareTargets.toApp(context, it, f, mime) }
                                } else {
                                    null
                                }
                                if (chats != null && runCatching { shareLauncher.launch(chats) }.isSuccess) {
                                    notice = R.string.card_share_whatsapp_status_fallback
                                } else {
                                    failure = refused
                                }
                            }
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

    SystemBars(lightIcons = true)
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
            val previewWidth = minOf(214.dp, maxHeight * (9f / 16f), maxWidth)
            val shown = preview
            if (shown != null) {
                Image(
                    bitmap = shown.asImageBitmap(),
                    contentDescription = cardTitle,
                    modifier = Modifier.width(previewWidth).aspectRatio(9f / 16f),
                )
            } else {
                // A skin-coloured card while the first drawing is made, never a blank.
                Box(Modifier.width(previewWidth).aspectRatio(9f / 16f).background(skinColor(skin)))
            }
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
            val momentsTotal = nights.sumOf { it.momentCount }
            // Plurals (28/09, item 33): "1 momentos" was on the first done screen.
            Text(
                stringResource(
                    R.string.card_done_stats,
                    pluralStringResource(R.plurals.card_done_nights, nightsThisYear, nightsThisYear, year),
                    pluralStringResource(R.plurals.card_done_moments, momentsTotal, momentsTotal),
                ),
                style = TTType.BodySmall,
                color = TT.Gray45,
            )
            Spacer(Modifier.height(14.dp))
            DoneActions(
                nav = nav,
                closed = feedClosed.takeIf { feedLooked },
                looked = feedLooked,
                toTour = feedSeries != null,
                onFeed = openFeedPost,
                onKeep = { nav.navigate(Routes.consent(ConsentText.KEEP_NIGHT, n.id)) },
                onShareAgain = {
                    cameBack = false
                    choosing = true
                },
            )
        } else if (choosing) {
            ShareChoices(
                feedClosed = feedClosed,
                feedLooked = feedLooked,
                onFeed = openFeedPost,
                onKeep = { nav.navigate(Routes.consent(ConsentText.KEEP_NIGHT, n.id)) },
                hasInstagram = hasInstagram,
                hasFacebook = hasFacebook,
                hasSnapchat = hasSnapchat,
                hasTiktok = tiktok != null,
                hasWhatsapp = whatsapp != null,
                canSave = ShareTargets.canSaveToGallery,
                busy = busy,
                status = when {
                    burning != null -> stringResource(burnLabel, burning ?: 0)
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
            // What goes out, said before it goes (26/09): one line, and a
            // switch for each thing the card shows about the night.
            CardShows(
                showHour = showHour,
                showEvent = showEvent,
                exactBpm = exactBpm,
                onHour = { showHour = it },
                onEvent = { showEvent = it },
                onExact = { exactBpm = it },
            )
            Spacer(Modifier.height(10.dp))
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
 * "Esse card mostra seu pico, a hora e o evento." — and a switch for each
 * (26/09). The person sees what a card says about them before it leaves,
 * and can take the hour or the event off, or show the number only to the ten.
 */
@Composable
private fun CardShows(
    showHour: Boolean,
    showEvent: Boolean,
    exactBpm: Boolean,
    onHour: (Boolean) -> Unit,
    onEvent: (Boolean) -> Unit,
    onExact: (Boolean) -> Unit,
) {
    Text(stringResource(R.string.card_shows), style = TTType.BodySmall, color = TT.Gray45)
    Spacer(Modifier.height(4.dp))
    CardShowRow(stringResource(R.string.card_show_hour), showHour, onHour)
    CardShowRow(stringResource(R.string.card_show_event), showEvent, onEvent)
    CardShowRow(stringResource(R.string.card_show_exact), exactBpm, onExact)
}

@Composable
private fun CardShowRow(label: String, on: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(40.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = TTType.BodySmall, color = TT.Paper, modifier = Modifier.weight(1f))
        Switch(
            checked = on,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TT.Ink,
                checkedTrackColor = TT.Rose,
                checkedBorderColor = TT.Rose,
                uncheckedThumbColor = TT.Gray45,
                uncheckedTrackColor = TT.Ink700,
                uncheckedBorderColor = TT.Ink600,
            ),
        )
    }
}

/**
 * The done screen's acts: the feed, the gallery, "Compartilhar de novo".
 *
 * "Mostrar pra galera" — the one place a moment becomes public — **is always
 * here** (28/09, item 31). It used to appear only when posting would work, so
 * a night that could not post showed nothing and nobody learned there was a
 * feed; now, when it cannot open, the reason is under it ([FeedGate]). The
 * confirmation itself moved to its own screen ([Routes.ShowToFeed]), the same
 * one the feed and the share screen open, and the post lands in the feed.
 *
 * **One Pink button at a time** (#41, 22/09): the feed when it can open, the
 * gallery when it cannot. Everything else is quiet: outlined, or a line of text.
 */
@Composable
private fun DoneActions(
    nav: NavHostController,
    closed: FeedGate.Closed?,
    looked: Boolean,
    toTour: Boolean,
    onFeed: () -> Unit,
    onKeep: () -> Unit,
    onShareAgain: () -> Unit,
) {
    val openGallery = {
        nav.navigate(Routes.Gallery) {
            popUpTo(Routes.Feed) { saveState = true }
            launchSingleTop = true
        }
    }
    val feedLabel = stringResource(if (toTour) R.string.feed_post_cta_tour else R.string.feed_post_cta)
    if (looked && closed == null) {
        TTButton(feedLabel, TTButtonStyle.Rose, onClick = onFeed)
        Spacer(Modifier.height(10.dp))
        TTButton(stringResource(R.string.card_done_gallery), TTButtonStyle.OutlineOnDark, onClick = openGallery)
    } else {
        // Shut, and saying why under it — the tap is swallowed, never ignored silently.
        TTButton(feedLabel, TTButtonStyle.OutlineOnDark, enabled = false, onClick = onFeed)
        if (closed != null) {
            Spacer(Modifier.height(6.dp))
            Text(feedClosedText(closed), style = TTType.BodySmall, color = TT.Gray45)
            if (closed == FeedGate.Closed.NotKept) {
                Text(
                    stringResource(R.string.feed_post_keep_link),
                    style = TTType.Button.copy(fontSize = 14.sp),
                    color = TT.Acid,
                    modifier = Modifier.clickable(onClick = onKeep).padding(vertical = 8.dp),
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        TTButton(stringResource(R.string.card_done_gallery), TTButtonStyle.Rose, onClick = openGallery)
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
 * The destinations (#60): the event's feed, the networks on this phone, each
 * by its own best road, then the card alone to copy or save, then every other
 * app.
 *
 * **Where does this go, first** (#62, Felipe 24/09). Until 28/09 every network
 * was a TumTum Pink button, two to a row (ShareGrid, #61): seven Pink
 * buttons on one screen, which Felipe called *"muito grosseiro"* with b222 in
 * his hand — and which broke the one-Pink-button rule (#41, 22/09) on the
 * screen that most needs a landing point for the eye. Now the feed is the
 * one Pink button, the only destination that is TumTum's own, and the
 * networks are a row of their own marks — white circles, the mark in black,
 * the name under it — the way Spotify's and Strava's share rows read.
 * Equal among themselves, in the order they always had. Copiar, Salvar and
 * Mais apps sit beneath as small outlined chips: the way out for everything
 * else, not the answer.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShareChoices(
    feedClosed: FeedGate.Closed?,
    feedLooked: Boolean,
    onFeed: () -> Unit,
    /** "Ligar 'Guardar a noite'": the consent screen on that key, this night waiting to go up. */
    onKeep: () -> Unit,
    hasInstagram: Boolean,
    hasFacebook: Boolean,
    hasSnapchat: Boolean,
    hasTiktok: Boolean,
    hasWhatsapp: Boolean,
    canSave: Boolean,
    busy: Boolean,
    status: String?,
    onPick: (ShareTo) -> Unit,
    onBack: () -> Unit,
) {
    Text(stringResource(R.string.card_share_to), style = TTType.MetaSmall, color = TT.Acid)
    Spacer(Modifier.height(8.dp))
    // The event's feed first (28/09, item 31): the one place here that is
    // TumTum's own, and the one the founder could not find. Always shown; when
    // this night cannot go there, the button stays and says why under it.
    TTButton(
        stringResource(R.string.card_share_feed),
        TTButtonStyle.Rose,
        enabled = !busy && feedLooked && feedClosed == null,
        onClick = onFeed,
    )
    if (feedLooked && feedClosed != null) {
        Spacer(Modifier.height(6.dp))
        Text(feedClosedText(feedClosed), style = TTType.BodySmall, color = TT.Gray45)
        // The sentence was only a reason (02/10, Felipe on b227): with the
        // key off, the way to turn it on is here, and the night goes up with it.
        if (feedClosed == FeedGate.Closed.NotKept) {
            Text(
                stringResource(R.string.feed_post_keep_link),
                style = TTType.Button.copy(fontSize = 14.sp),
                color = TT.Acid,
                modifier = Modifier.clickable(enabled = !busy, onClick = onKeep).padding(vertical = 8.dp),
            )
        }
    }
    Spacer(Modifier.height(14.dp))
    // The networks on this phone. Status comes right after WhatsApp (#63) and
    // wears the same mark, so its name under it is what tells the two apart.
    val networks = buildList {
        if (hasInstagram) {
            add(ShareNetwork(ShareTo.Instagram, R.drawable.ic_brand_instagram, stringResource(R.string.card_share_instagram)))
        }
        if (hasFacebook) {
            add(ShareNetwork(ShareTo.Facebook, R.drawable.ic_brand_facebook, stringResource(R.string.card_share_facebook)))
        }
        if (hasSnapchat) {
            add(ShareNetwork(ShareTo.Snapchat, R.drawable.ic_brand_snapchat, stringResource(R.string.card_share_snapchat)))
        }
        if (hasTiktok) {
            add(ShareNetwork(ShareTo.TikTok, R.drawable.ic_brand_tiktok, stringResource(R.string.card_share_tiktok)))
        }
        if (hasWhatsapp) {
            add(ShareNetwork(ShareTo.WhatsApp, R.drawable.ic_brand_whatsapp, stringResource(R.string.card_share_whatsapp)))
            add(
                ShareNetwork(
                    ShareTo.WhatsAppStatus,
                    R.drawable.ic_brand_whatsapp,
                    stringResource(R.string.card_share_status_short),
                    spoken = stringResource(R.string.card_share_whatsapp_status),
                ),
            )
        }
    }
    if (networks.isNotEmpty()) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            // One row when they fit, otherwise rows as even as the count allows
            // (six on a narrow phone are three and three, never five and one).
            // A cell is as wide as its widest name, measured, not guessed: a
            // name that wrapped would sit on two lines under its mark.
            val measurer = rememberTextMeasurer()
            val density = LocalDensity.current
            val widest = networks.maxOf { measurer.measure(it.label, TTType.MetaSmall).size.width }
            val cell = maxOf(ShareIconSize, with(density) { widest.toDp() })
            val gap = 6.dp
            val fit = ((maxWidth + gap) / (cell + gap)).toInt().coerceIn(1, networks.size)
            val rows = (networks.size + fit - 1) / fit
            val perRow = (networks.size + rows - 1) / rows
            // Aligned to "Feed do evento" above (02/10, Felipe): the first
            // column's circles start where the Pink button starts, the last
            // column's end where it ends, the rest evenly between. A name is
            // centred under its circle and may reach past the edge by a few
            // dp — into the page margin, never into the next name.
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                networks.chunked(perRow).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        row.forEach { network ->
                            ShareIcon(network, enabled = !busy) { onPick(network.target) }
                        }
                        // The last row keeps the columns of the one above it.
                        repeat(perRow - row.size) { Spacer(Modifier.width(ShareIconSize)) }
                    }
                }
            }
        }
        Spacer(Modifier.height(14.dp))
    }
    // The card alone, and every other app: quieter, and wrapping onto a second
    // line on a narrow phone rather than squeezing a label onto two. Spread to
    // the Pink button's edges like the circles above (02/10).
    FlowRow(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        QuietChoice(stringResource(R.string.card_share_copy), enabled = !busy) { onPick(ShareTo.Copy) }
        if (canSave) {
            QuietChoice(stringResource(R.string.card_share_save), enabled = !busy) { onPick(ShareTo.Save) }
        }
        QuietChoice(stringResource(R.string.card_share_more), enabled = !busy) { onPick(ShareTo.More) }
    }
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
    if ((hasInstagram || hasFacebook || hasSnapchat) && status == null) {
        Text(stringResource(R.string.card_share_instagram_hint), style = TTType.Footnote, color = TT.Gray45)
    }
    // Nothing comes back to say whether WhatsApp opened Status or the chat
    // list (#63), so both roads are said before the tap, never after it.
    if (hasWhatsapp && status == null) {
        Spacer(Modifier.height(4.dp))
        Text(stringResource(R.string.card_share_whatsapp_status_hint), style = TTType.Footnote, color = TT.Gray45)
    }
}

/**
 * A secondary destination (#62): outlined, 44 dp, sized to its own label.
 * Like [TTButton] it never fades; while a share is being prepared a tap is
 * swallowed, and the status line under the choices already says why.
 */
@Composable
private fun QuietChoice(text: String, enabled: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        Modifier
            .height(44.dp)
            .clip(shape)
            .border(1.dp, TT.Ink600, shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = TTType.Button.copy(fontSize = 14.sp), color = TT.Paper, maxLines = 1)
    }
}

/** A network on the share screen: where it goes, its mark, its name, and the name said aloud. */
private data class ShareNetwork(
    val target: ShareTo,
    val icon: Int,
    val label: String,
    val spoken: String? = null,
)

private val ShareIconSize = 52.dp

/**
 * A network as its own mark (28/09): a circle on the black canvas, the mark
 * at its own 1:1, the name under it. Like [TTButton] it never fades; while a
 * share is being prepared a tap is swallowed, and the status line under the
 * choices already says why.
 *
 * **Quieter, 02/10.** The first version was six solid white discs with the
 * marks in black, and in Felipe's hand on b227 it still read as *"um pouco
 * grosseiro"* — six white spots louder than the one Pink button above them.
 * The circles are now the same outlined dark as "Copiar card" below, with
 * the mark in white (each brand allows white as well as black), so the Pink
 * "Feed do evento" is the only loud thing on the screen.
 */
@Composable
private fun ShareIcon(network: ShareNetwork, enabled: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    // As wide as the circle, so the grid can put circles on the button's
    // edges; not clipped, so a name wider than its circle still shows whole.
    Column(
        modifier
            .width(ShareIconSize)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(ShareIconSize)
                .clip(CircleShape)
                .background(TT.Ink800)
                .border(1.dp, TT.Ink600, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(network.icon),
                // "Status" alone would not say whose status; the WhatsApp mark
                // on it is not read aloud.
                contentDescription = network.spoken,
                tint = TT.Paper,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            network.label,
            style = TTType.MetaSmall,
            color = TT.Gray45,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.wrapContentWidth(unbounded = true),
        )
    }
}
