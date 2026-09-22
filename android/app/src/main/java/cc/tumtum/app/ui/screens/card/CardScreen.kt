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
 * video (item 43, 22/09). A video's preview is its first frame — what the
 * card will sit over — and it leaves through Instagram's Story editor, the
 * one place that can play it under the card, instead of the system sheet.
 */
private sealed interface CardMedia {
    val preview: Bitmap

    data class Photo(override val preview: Bitmap) : CardMedia
    data class Video(val uri: Uri, override val preview: Bitmap) : CardMedia
}

/**
 * Seu card (UI kit do core loop). Compartilhar é sempre ativo: nada sai
 * daqui sem o toque em Compartilhar. "Postar no feed" saiu em 19/09 (item 37):
 * postava num repositório falso deste celular e confirmava que a galera
 * podia sentir — ninguém podia. Volta quando o feed for o do servidor.
 */
@Composable
fun CardScreen(nav: NavHostController, nightId: Long, skin: Skin) {
    val container = appContainer()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val night by container.nights.night(nightId).collectAsStateWithLifecycle(initialValue = null)
    var sharing by remember { mutableStateOf(false) }
    // The share sheet came back. That is all it means: whether the card was
    // sent, nobody here knows, and the screen says only what is true.
    var cameBack by remember { mutableStateOf(false) }
    var failure by remember { mutableStateOf<Int?>(null) }
    val shareLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { cameBack = true }
    val nights by container.nights.nights().collectAsStateWithLifecycle(initialValue = emptyList())
    // A fan's own photo or video behind the black skin (§5.11, item 43): for this card, this share.
    var media by remember { mutableStateOf<CardMedia?>(null) }
    var loadingMedia by remember { mutableStateOf(false) }
    val instagram = remember { InstagramStory.isInstalled(context) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        loadingMedia = true
        scope.launch {
            media = withContext(Dispatchers.IO) {
                val type = context.contentResolver.getType(uri).orEmpty()
                if (type.startsWith("video/")) {
                    InstagramStory.firstFrame(context, uri)?.let { CardMedia.Video(uri, it) }
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
            // A button in Toxic Yellow, not a line of small caps. Felipe missed
            // the option entirely on b145 (22/09): white meta type above a pink
            // CTA reads as a caption for the card, not as a thing to press.
            TTButton(
                stringResource(
                    when {
                        loadingMedia -> R.string.card_media_loading
                        video != null -> R.string.card_video_remove
                        media != null -> R.string.card_photo_remove
                        instagram -> R.string.card_media_add
                        else -> R.string.card_photo_add
                    },
                ),
                TTButtonStyle.OutlineAcid,
                enabled = !loadingMedia && !sharing,
                onClick = {
                    if (media == null) {
                        // Video only where it can go out: without Instagram the
                        // picker offers photos, and the line below says why.
                        picker.launch(
                            PickVisualMediaRequest(
                                if (instagram) {
                                    ActivityResultContracts.PickVisualMedia.ImageAndVideo
                                } else {
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                },
                            ),
                        )
                    } else {
                        media = null
                    }
                },
            )
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(
                    when {
                        video != null -> R.string.card_video_hint
                        !instagram -> R.string.card_video_needs_instagram
                        else -> R.string.card_media_hint
                    },
                ),
                style = TTType.BodySmall,
                color = TT.Gray45,
                maxLines = 2,
            )
            Spacer(Modifier.height(10.dp))
        }
        failure?.let {
            Text(stringResource(it), style = TTType.BodySmall, color = TT.Rose, maxLines = 2)
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
            // Compartilhar é sempre ativo (§1): o card vira PNG 1080×1920 e sai
            // pelo share sheet do sistema, com a imagem anexa — ou, com um
            // vídeo atrás, vira a camada de cima no editor de Story do
            // Instagram, com o vídeo tocando embaixo. Nada sai sem este toque.
            TTButton(
                stringResource(
                    when {
                        sharing && video != null -> R.string.card_share_instagram_running
                        sharing -> R.string.card_share_running
                        video != null -> R.string.card_share_instagram
                        else -> R.string.card_share
                    },
                ),
                TTButtonStyle.Rose,
                enabled = !sharing && !loadingMedia,
                onClick = {
                    sharing = true
                    failure = null
                    val chosen = media
                    scope.launch {
                        // A pele escolhida fica com a noite (capa da galeria) no
                        // momento em que o card sai — antes era o "Postar" que gravava.
                        // Com a pele preta, a foto vai junto (de um vídeo, o
                        // primeiro quadro); com outra, ou sem foto, a noite deixa de ter uma.
                        val photoPath = if (skin == Skin.BLACK && chosen != null) {
                            CardPhotoStore.save(context, n.id, chosen.preview, n.photoPath)
                        } else {
                            CardPhotoStore.delete(n.photoPath)
                            null
                        }
                        container.nights.publish(n.id, skin, photoPath)
                        val intent = withContext(Dispatchers.IO) {
                            runCatching {
                                if (chosen is CardMedia.Video) {
                                    val sticker = CardRenderer.render(context, n, skin, cardTitle, cardMeta, cardChip, sticker = true)
                                    val stickerFile = CardRenderer.writePng(context, sticker, "tumtum-${n.id}-sticker.png")
                                    val videoFile = InstagramStory.copyVideo(context, chosen.uri, n.id)
                                    if (videoFile == null) null else InstagramStory.intent(context, videoFile, stickerFile)
                                } else {
                                    val bitmap = CardRenderer.render(context, n, skin, cardTitle, cardMeta, cardChip, photo = chosen?.preview)
                                    CardRenderer.shareIntent(context, bitmap, "tumtum-${n.id}-${skin.name.lowercase()}.png")
                                }
                            }.getOrNull()
                        }
                        if (intent == null) {
                            // Instagram would not take it, or the video could not
                            // be read: said here, not swallowed behind a button
                            // that went back to normal.
                            failure = if (chosen is CardMedia.Video) R.string.card_instagram_refused else R.string.card_share_failed
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
        Spacer(Modifier.height(2.dp))
    }
}
