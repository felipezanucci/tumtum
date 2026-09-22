package cc.tumtum.app.export

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri

/**
 * The first frame of the person's video, for the preview on the card screen —
 * what the card will sit over.
 *
 * All that is left of `InstagramStory` (#40, 22/09). Felipe: *"a gente pode
 * tirar esse botão de postar no Instagram Stories."* The card is burned into
 * the video and leaves through the system share sheet, which reaches
 * Instagram, TikTok, X, WhatsApp and the gallery alike; a button naming one
 * network put that one above the others, on a screen with too much on it
 * already. The `<queries>` entry that let the app ask whether Instagram was
 * installed went with it — a question the app no longer needs to ask about
 * anybody's phone.
 */
object VideoFrame {
    fun first(context: Context, source: Uri): Bitmap? = runCatching {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, source)
            retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        } finally {
            retriever.release()
        }
    }.getOrNull()
}
