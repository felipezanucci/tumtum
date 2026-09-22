package cc.tumtum.app.export

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.BitmapOverlay
import androidx.media3.effect.OverlayEffect
import androidx.media3.effect.Presentation
import androidx.media3.effect.TextureOverlay
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import com.google.common.collect.ImmutableList
import java.io.File
import kotlin.coroutines.resume
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

/**
 * The card burned into the person's own video, so it can be posted anywhere
 * (decision log, item 43, 22/09).
 *
 * Felipe: *"pros usuários poderem subir video em qualquer rede social. seja
 * instagram, x, snap, tiktok, etc."* The insight that makes this small is that
 * it is **not N integrations** — Instagram, X, TikTok, Snapchat, WhatsApp,
 * Telegram and "save to the gallery" all take the same thing: one MP4. Burn
 * the card into the file and Android's own share sheet covers every one of
 * them, with no social-network SDK at all.
 *
 * The estimate this replaces was 400–600 lines of MediaExtractor, MediaCodec
 * and EGL by hand. `androidx.media3` Transformer does that pipeline for us:
 * decode, draw a bitmap over each frame, encode, mux, with the audio passed
 * through. It needs API 23 and this app is minSdk 28.
 *
 * Three choices taken here, each reversible:
 *
 * - **9:16, scaled to fill and centre-cropped.** The card is a Story. A video
 *   shot 16:9 loses its sides rather than gaining black bars, which is what
 *   every Story tool does and what someone who filmed vertically expects.
 * - **[MAX_MS] from the start.** Encoding costs real seconds and they scale
 *   with length. The screen says the video was trimmed rather than quietly
 *   shortening it.
 * - **The overlay is the same sticker PNG** the Instagram route uses — no
 *   background of its own, a gradient under the block of type — so both paths
 *   show the person the same card.
 *
 * **None of this is verified.** There is no Android SDK and no device in the
 * environment it was written in, CI compiles it but never runs it on hardware,
 * and encoder behaviour is the classic thing that differs per chipset. It is
 * built to fail loudly: [burn] returns null and the caller keeps the photo and
 * the Instagram routes standing.
 */
object VideoCard {

    /** How much of the video is kept, from its start. */
    const val MAX_MS = 30_000L

    private const val W = 1080
    private const val H = 1920

    /** How long the chosen video runs, so the screen can say it will be trimmed. */
    fun durationMs(context: Context, source: Uri): Long? = runCatching {
        val retriever = android.media.MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, source)
            retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
        } finally {
            retriever.release()
        }
    }.getOrNull()

    /**
     * Writes [source] with [overlay] burned into it and returns the file, or
     * null if the export failed. [onProgress] is fed the encoder's own figure,
     * 0–100, never a made-up one — while it has none, it is not called.
     */
    @OptIn(UnstableApi::class)
    suspend fun burn(
        context: Context,
        source: Uri,
        overlay: Bitmap,
        nightId: Long,
        onProgress: (Int) -> Unit = {},
    ): File? = withContext(Dispatchers.Main) {
        val dir = File(context.cacheDir, "cards").apply { mkdirs() }
        val out = File(dir, "tumtum-$nightId.mp4")
        out.delete()

        val item = MediaItem.Builder()
            .setUri(source)
            .setClippingConfiguration(
                MediaItem.ClippingConfiguration.Builder().setEndPositionMs(MAX_MS).build(),
            )
            .build()

        val overlays: ImmutableList<TextureOverlay> =
            ImmutableList.of(BitmapOverlay.createStaticBitmapOverlay(overlay))

        val edited = EditedMediaItem.Builder(item)
            .setEffects(
                Effects(
                    /* audioProcessors = */ emptyList(),
                    // Presentation first so the overlay lands on the finished
                    // 9:16 frame rather than on the source's own shape. Whether
                    // the pipeline honours that order, and whether a full-frame
                    // static overlay lands edge to edge, are the two things only
                    // a real device can answer.
                    listOf(
                        Presentation.createForWidthAndHeight(
                            W, H, Presentation.LAYOUT_SCALE_TO_FIT_WITH_CROP,
                        ),
                        OverlayEffect(overlays),
                    ),
                ),
            )
            .build()

        suspendCancellableCoroutine { cont ->
            val transformer = Transformer.Builder(context)
                .addListener(
                    object : Transformer.Listener {
                        override fun onCompleted(composition: Composition, result: ExportResult) {
                            if (cont.isActive) cont.resume(out.takeIf { it.length() > 0 })
                        }

                        override fun onError(
                            composition: Composition,
                            result: ExportResult,
                            exception: ExportException,
                        ) {
                            out.delete()
                            if (cont.isActive) cont.resume(null)
                        }
                    },
                )
                .build()

            // The encoder's own progress, polled. Media3 answers "not started"
            // and "unavailable" as well as a figure, and those are not zero —
            // a bar that sits at 0% because nobody asked is the same lie as a
            // spinner that means nothing.
            val poller = CoroutineScope(Dispatchers.Main).launch {
                val holder = ProgressHolder()
                while (cont.isActive) {
                    if (transformer.getProgress(holder) == Transformer.PROGRESS_STATE_AVAILABLE) {
                        onProgress(holder.progress)
                    }
                    delay(250)
                }
            }
            cont.invokeOnCancellation {
                poller.cancel()
                runCatching { transformer.cancel() }
                out.delete()
            }

            runCatching { transformer.start(edited, out.absolutePath) }
                .onFailure {
                    poller.cancel()
                    if (cont.isActive) cont.resume(null)
                }
        }
    }
}
