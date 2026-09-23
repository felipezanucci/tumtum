package cc.tumtum.app.export

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import cc.tumtum.app.R
import java.io.File

/**
 * One button per network, each by the best road that network offers (#60,
 * Felipe 23/09).
 *
 * The question was how to keep the card movable over the person's own video
 * without giving one network special treatment. The precedent he asked for
 * settled it: Spotify and Strava both open a row of destinations, and each
 * one takes the richest thing its app accepts — a sticker for Instagram
 * Stories, a finished file for WhatsApp, the system list for the rest, and a
 * card to copy or download for anywhere else. Treating every network by its
 * own best road is the neutral rule; "one file for all" was only neutral by
 * giving every network the poorest road.
 *
 * - **Instagram Stories** — `com.instagram.share.ADD_TO_STORY`: the video (or
 *   photo) as the background, the card as a sticker the person can move and
 *   resize. Measured 22/09: it works without a Facebook App ID, and Instagram
 *   draws the sticker smaller than the screen — which is why the sticker is
 *   cropped to the card's own block ([CardSticker]) rather than sent as a
 *   1080×1920 sheet that is mostly transparent.
 * - **WhatsApp** — the finished file (card burned into the video), straight
 *   into WhatsApp's own picker.
 * - **Copiar card / Salvar card** — the card alone, transparent, for any
 *   editor that takes a pasted or picked image. Strava's "copy to clipboard"
 *   is the precedent for pasting into a Story.
 * - **Mais apps** — Android's list, with the finished file: TikTok, X,
 *   Snapchat, Telegram, the gallery.
 *
 * Facebook Stories uses Meta's same channel but refuses without a registered
 * Facebook App ID; TikTok and Snapchat each need their own SDK. None of the
 * three is offered until it can work.
 */
object ShareTargets {

    const val INSTAGRAM = "com.instagram.android"
    private val WHATSAPP = listOf("com.whatsapp", "com.whatsapp.w4b")
    private const val STORY_ACTION = "com.instagram.share.ADD_TO_STORY"

    /** Needs the `<queries>` entries in the manifest on Android 11+. */
    fun installed(context: Context, pkg: String): Boolean = runCatching {
        context.packageManager.getPackageInfo(pkg, 0)
        true
    }.getOrDefault(false)

    /** The WhatsApp on this phone — the ordinary one first — or null. */
    fun whatsapp(context: Context): String? = WHATSAPP.firstOrNull { installed(context, it) }

    /** Saving to the gallery without a permission prompt needs Android 10. */
    val canSaveToGallery: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    private fun uriFor(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    /**
     * Instagram's Story editor with [background] behind and [sticker] on top.
     * Null when Instagram cannot take it, so the screen says so rather than
     * launching into nothing.
     */
    fun instagramStory(context: Context, background: File, backgroundMime: String, sticker: File?): Intent? {
        val backgroundUri = uriFor(context, background)
        val stickerUri = sticker?.let { uriFor(context, it) }
        val intent = Intent(STORY_ACTION).apply {
            setDataAndType(backgroundUri, backgroundMime)
            stickerUri?.let { putExtra("interactive_asset_uri", it) }
            // Meta asks for the sharing app's Facebook App ID. Measured 22/09:
            // Instagram accepts the intent without one. It is a build resource,
            // empty until registered, and sent only when present.
            val appId = context.getString(R.string.facebook_app_id)
            if (appId.isNotBlank()) putExtra("source_application", appId)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.grantUriPermission(INSTAGRAM, backgroundUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        stickerUri?.let { context.grantUriPermission(INSTAGRAM, it, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
        val resolved = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return if (resolved != null) intent else null
    }

    /** [file] handed straight to one app's own share screen. */
    fun toApp(context: Context, pkg: String, file: File, mime: String): Intent =
        Intent(Intent.ACTION_SEND)
            .setType(mime)
            .setPackage(pkg)
            .putExtra(Intent.EXTRA_STREAM, uriFor(context, file))
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

    /** The card on the clipboard, as an image a Story editor can paste. */
    fun copy(context: Context, card: File): Boolean = runCatching {
        val clipboard = context.getSystemService(ClipboardManager::class.java) ?: return false
        clipboard.setPrimaryClip(ClipData.newUri(context.contentResolver, "TumTum", uriFor(context, card)))
        true
    }.getOrDefault(false)

    /** The card in the phone's gallery, under Pictures/TumTum. Android 10+ only. */
    fun saveToGallery(context: Context, card: Bitmap, name: String): Boolean {
        if (!canSaveToGallery) return false
        return runCatching {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, name)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/TumTum")
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false
            resolver.openOutputStream(uri)?.use { card.compress(Bitmap.CompressFormat.PNG, 100, it) } ?: return false
            true
        }.getOrDefault(false)
    }

    /**
     * The person's video, copied into our cache so a FileProvider URI of ours
     * can be handed on: a picker's `content://` grant is ours, not
     * transferable, and Instagram opening it would fail with no explanation.
     */
    fun copyVideo(context: Context, source: Uri, nightId: Long): Pair<File, String>? = runCatching {
        val dir = File(context.cacheDir, "cards").apply { mkdirs() }
        val mov = context.contentResolver.getType(source) == "video/quicktime"
        val file = File(dir, "story-$nightId.${if (mov) "mov" else "mp4"}")
        context.contentResolver.openInputStream(source)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        } ?: return null
        file to if (mov) "video/quicktime" else "video/mp4"
    }.getOrNull()

    /** A still (the person's photo) written where our FileProvider can serve it. */
    fun writeJpeg(context: Context, bitmap: Bitmap, name: String): File {
        val dir = File(context.cacheDir, "cards").apply { mkdirs() }
        val file = File(dir, name)
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 92, it) }
        return file
    }
}

/**
 * The card as a sticker: only the block that carries it, not the 1080×1920
 * sheet it is drawn on. Instagram draws a sticker smaller than the screen
 * (measured 22/09), so a full sheet arrived as a small card lost in a large
 * transparent box that was awkward to grab. Cropped to what is not fully
 * transparent, the sticker *is* the card.
 */
object CardSticker {
    /**
     * The rows and columns of [pixels] (ARGB, [width] wide) holding anything
     * not fully transparent, as `[left, top, right, bottom)` — or null when
     * everything is. Pure, so it is tested without a device.
     */
    fun opaqueBounds(pixels: IntArray, width: Int, height: Int): IntArray? {
        var left = width
        var top = height
        var right = -1
        var bottom = -1
        for (y in 0 until height) {
            val row = y * width
            for (x in 0 until width) {
                if ((pixels[row + x] ushr 24) != 0) {
                    if (x < left) left = x
                    if (x > right) right = x
                    if (y < top) top = y
                    if (y > bottom) bottom = y
                }
            }
        }
        return if (right < 0) null else intArrayOf(left, top, right + 1, bottom + 1)
    }

    fun crop(bitmap: Bitmap): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
        val b = opaqueBounds(pixels, w, h) ?: return bitmap
        return Bitmap.createBitmap(bitmap, b[0], b[1], b[2] - b[0], b[3] - b[1])
    }
}
