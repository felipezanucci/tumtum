package cc.tumtum.app.export

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri

/**
 * The first frame of the person's video, for the preview on the card screen —
 * what the card will sit over.
 *
 * What was left of `InstagramStory` after #40 (22/09) removed the one
 * button that named Instagram. #60 (23/09) brought Instagram back as one
 * destination among several, each by its own best road — see [ShareTargets].
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
