package cc.tumtum.app.export

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.core.content.FileProvider
import cc.tumtum.app.R
import java.io.File

/**
 * The card over the person's own video, in the place this product actually
 * gets posted (item 43, 22/09).
 *
 * Compositing the card onto a video ourselves means MediaExtractor →
 * MediaCodec → an OpenGL surface → MediaCodec → MediaMuxer, hundreds of
 * lines that behave differently per chipset and cannot be verified from an
 * environment with no SDK. Instagram's own Stories intent does the same job
 * for the one destination that matters: it takes a **background** (the
 * video, playing) and a **sticker** (a PNG with transparency — our card
 * minus its black, scrim kept, so the type stays legible over anything) and
 * composes them in the Story editor, where the person can still move,
 * resize or drop the sticker before posting. Nothing is rendered here.
 *
 * What this cannot promise, and the first build in a hand has to answer:
 * how large Instagram draws a 1080×1920 sticker (edge to edge, or inset as a
 * movable sticker — both are usable, only one is the mock-up), and how long
 * a video it accepts before trimming. Neither number is asserted anywhere in
 * the app; the intent is offered and Instagram decides.
 *
 * The intent is the one Meta documents for Android: action
 * `com.instagram.share.ADD_TO_STORY`, the background as the data URI, the
 * sticker as `interactive_asset_uri`, the Facebook App ID as
 * `source_application`. Both files are served by our own FileProvider, with
 * an explicit grant to Instagram's package for each.
 */
object InstagramStory {

    const val PACKAGE = "com.instagram.android"
    private const val ACTION = "com.instagram.share.ADD_TO_STORY"

    /** Whether Instagram is on this phone. Needs the `<queries>` entry in the manifest on Android 11+. */
    fun isInstalled(context: Context): Boolean = runCatching {
        context.packageManager.getPackageInfo(PACKAGE, 0)
        true
    }.getOrDefault(false)

    /**
     * The person's video, copied into our cache so a FileProvider URI of ours
     * can be handed to Instagram: a picker's `content://` grant is ours, not
     * transferable, and Instagram opening it would fail with no explanation.
     * The copy carries the source's extension so Instagram sees the container
     * it is.
     */
    fun copyVideo(context: Context, source: Uri, nightId: Long): File? = runCatching {
        val dir = File(context.cacheDir, "cards").apply { mkdirs() }
        val ext = when (context.contentResolver.getType(source)) {
            "video/quicktime" -> "mov"
            else -> "mp4"
        }
        val file = File(dir, "story-$nightId.$ext")
        context.contentResolver.openInputStream(source)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        } ?: return null
        file
    }.getOrNull()

    /** The first frame, for the preview on the card screen — what the card will sit over. */
    fun firstFrame(context: Context, source: Uri): Bitmap? = runCatching {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, source)
            retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        } finally {
            retriever.release()
        }
    }.getOrNull()

    /**
     * The intent that opens Instagram's Story editor with [video] behind and
     * [sticker] on top. Returns null when Instagram cannot take it, so the
     * screen says so rather than launching into nothing.
     */
    fun intent(context: Context, video: File, sticker: File): Intent? {
        val authority = "${context.packageName}.fileprovider"
        val videoUri = FileProvider.getUriForFile(context, authority, video)
        val stickerUri = FileProvider.getUriForFile(context, authority, sticker)
        val intent = Intent(ACTION).apply {
            setDataAndType(videoUri, if (video.extension == "mov") "video/quicktime" else "video/mp4")
            putExtra("interactive_asset_uri", stickerUri)
            // Meta's docs ask for the Facebook App ID of the sharing app. It is
            // a build resource so it never sits in code; empty until Felipe
            // registers one, and the intent is still sent — whether Instagram
            // accepts it without one is the phone's to answer.
            val appId = context.getString(R.string.facebook_app_id)
            if (appId.isNotBlank()) putExtra("source_application", appId)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.grantUriPermission(PACKAGE, videoUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.grantUriPermission(PACKAGE, stickerUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val resolved = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return if (resolved != null) intent else null
    }
}
