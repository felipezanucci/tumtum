package cc.tumtum.app.ui.screens.card

import android.content.ActivityNotFoundException
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import cc.tumtum.app.domain.Skin
import cc.tumtum.app.export.CardRenderer
import cc.tumtum.app.export.InstagramStory
import cc.tumtum.app.export.VideoCard
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

/**
 * Seu card (UI kit do core loop). Compartilhar é sempre ativo: nada sai
 * daqui sem o toque em Compartilhar. "Postar no feed" saiu em 19/09 (item 37):
 * postava num repositório falso deste celular e confirmava que a galera
 * podia sentir — ninguém podia. Volta quando o feed for o do servidor.
 *
 * Com um vídeo atrás (22/09), o card é **gravado dentro do arquivo** e sai
 * pelo share sheet do sistema — Instagram, X, TikTok, Snap, WhatsApp, galeria,
 * todos aceitam um MP4. O `ADD_TO_STORY` do Instagram fica como atalho, para
 * quem quer o card móvel dentro do editor de Story.
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
    val shareLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { cameBack = true }
    val nights by container.nights.nights().collectAsStateWithLifecycle(initialValue = emptyList())
    var media by remember { mutableStateOf<CardMedia?>(null) }
    var loadingMedia by remember { mutableStateOf(false) }
    val instagram = remember { InstagramStory.isInstalled(context) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        loadingMedia = true
        failure = null
        scope.launch {
            media = withContext(Dispatchers.IO) {
                val type = context.contentResolver.getType(uri).orEmpty()
                if (type.startsWith("video/")) {
                    InstagramStory.firstFrame(context, uri)?.let {
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
    val cardChip = "${n.eventName.uppercase()} · ${Fmt.hour(n.peakAt).uppercase()}"
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
        Box(Modifier.weight(1f).fillMaxWidth().padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
            ShareCardView(
                skin = skin,
                title = cardTitle,
                bpm = n.peakBpm,
                meta = cardMeta,
                chip = cardChip,
                width = 214.dp,
                curveSamples = if (skin == Skin.BLACK) n.samples else null,
                curveWindow = if (skin == Skin.BLACK) n.startAt to n.endAt else null,
                photo = media?.preview?.asImageBitmap(),
            )
        }
        if (skin == Skin.BLACK && !cameBack) {
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
        if (cameBack) {
            // The loop ends on a TumTum screen, not on Android's share sheet
            // (§5.3 of the 19/09 research — peak–end). The card exists and the
            // night carries its skin: that is the claim, and it is true.
            Text(
                stringResource(R.string.card_done_title),
                style = TTType.ShoutSmall.copy(fontSize = 23.sp, lineHeight = 24.5.sp),
                color = TT.Paper,
            )
            Spacer(Modifier.height(6.dp))
            val year = java.time.Year.now().value
            val nightsThisYear = nights.count { it.date.atZone(java.time.ZoneId.systemDefault()).year == year }
            Text(
                stringResource(R.string.card_done_stats, nightsThisYear, year, nights.sumOf { it.momentCount }),
                style = TTType.BodySmall,
                color = TT.Gray45,
            )
            Spacer(Modifier.height(14.dp))
            TTButton(
                stringResource(R.string.card_done_gallery),
                TTButtonStyle.Rose,
                onClick = {
                    nav.navigate(Routes.Gallery) {
                        popUpTo(Routes.Feed) { saveState = true }
                        launchSingleTop = true
                    }
                },
            )
            Spacer(Modifier.height(10.dp))
            TTButton(
                stringResource(R.string.card_share_again),
                TTButtonStyle.OutlineOnDark,
                onClick = { cameBack = false },
            )
        } else {
            // Compartilhar é sempre ativo (§1). Sem vídeo, o card vira PNG
            // 1080×1920 e sai pelo share sheet. Com vídeo, ele é gravado dentro
            // do MP4 e sai pelo mesmo share sheet — que é o que faz ele chegar
            // em qualquer rede sem uma integração por rede.
            TTButton(
                when {
                    burning != null -> stringResource(R.string.card_share_burning, burning ?: 0)
                    sharing -> stringResource(R.string.card_share_running)
                    else -> stringResource(R.string.card_share)
                },
                TTButtonStyle.Rose,
                enabled = !busy,
                onClick = {
                    sharing = true
                    failure = null
                    val chosen = media
                    scope.launch {
                        publish(chosen)
                        val intent = if (chosen is CardMedia.Video) {
                            burning = 0
                            val sticker = withContext(Dispatchers.IO) {
                                CardRenderer.render(context, n, skin, cardTitle, cardMeta, cardChip, sticker = true)
                            }
                            val file = VideoCard.burn(context, chosen.uri, sticker, n.id) { burning = it }
                            burning = null
                            file?.let { CardRenderer.shareFileIntent(context, it, "video/mp4") }
                        } else {
                            withContext(Dispatchers.IO) {
                                runCatching {
                                    val bitmap = CardRenderer.render(context, n, skin, cardTitle, cardMeta, cardChip, photo = chosen?.preview)
                                    CardRenderer.shareIntent(context, bitmap, "tumtum-${n.id}-${skin.name.lowercase()}.png")
                                }.getOrNull()
                            }
                        }
                        if (intent == null) {
                            failure = if (chosen is CardMedia.Video) {
                                R.string.card_share_video_failed
                            } else {
                                R.string.card_share_failed
                            }
                        } else {
                            runCatching { shareLauncher.launch(intent) }
                                .onFailure { failure = R.string.card_share_failed }
                        }
                        sharing = false
                    }
                },
            )
            // The shortcut, for the one destination that can do better than a
            // finished file: inside Instagram's Story editor the card stays a
            // sticker the person can move, and the video stays live.
            if (video != null && instagram) {
                Spacer(Modifier.height(10.dp))
                TTButton(
                    stringResource(R.string.card_story_shortcut),
                    TTButtonStyle.OutlineOnDark,
                    enabled = !busy,
                    onClick = {
                        sharing = true
                        failure = null
                        scope.launch {
                            publish(video)
                            val intent = withContext(Dispatchers.IO) {
                                runCatching {
                                    val sticker = CardRenderer.render(context, n, skin, cardTitle, cardMeta, cardChip, sticker = true)
                                    val stickerFile = CardRenderer.writePng(context, sticker, "tumtum-${n.id}-sticker.png")
                                    InstagramStory.copyVideo(context, video.uri, n.id)
                                        ?.let { InstagramStory.intent(context, it, stickerFile) }
                                }.getOrNull()
                            }
                            if (intent == null) {
                                failure = R.string.card_instagram_refused
                            } else {
                                try {
                                    shareLauncher.launch(intent)
                                } catch (e: ActivityNotFoundException) {
                                    failure = R.string.card_instagram_refused
                                }
                            }
                            sharing = false
                        }
                    },
                )
            }
        }
        Spacer(Modifier.height(2.dp))
    }
}
