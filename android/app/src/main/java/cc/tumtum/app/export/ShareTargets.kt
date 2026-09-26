package cc.tumtum.app.export

import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import org.json.JSONObject
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
 * - **Status do WhatsApp** — the same file, addressed to Status by an
 *   undocumented extra ([whatsappStatus], 24/09).
 * - **Copiar card / Salvar card** — the card alone, transparent, for any
 *   editor that takes a pasted or picked image. Strava's "copy to clipboard"
 *   is the precedent for pasting into a Story.
 * - **Mais apps** — Android's list, with the finished file: TikTok, X,
 *   Snapchat, Telegram, the gallery.
 *
 * - **Facebook Stories** — Meta's same channel, `com.facebook.stories.ADD_TO_STORY`,
 *   which refuses without the sharing app's Facebook App ID (registered 24/09).
 *
 * - **Snapchat** — Creative Kit Lite, Snap's code-only path since 10/2024 (no
 *   SDK): an intent to `snapchat://creativekit/preview` with the video or
 *   photo as the background and the card as a sticker, carrying the client ID
 *   from Snap's portal. Until Snap approves the app, only its Demo Users can
 *   share.
 *
 * - **TikTok** — Share Kit, by the intent its Android SDK sends, built here
 *   without the SDK: the SDK pulls in Google's advertising-ID library, which
 *   would add the AD_ID permission and change the app's Play data-safety
 *   answers for a share button. TikTok takes no sticker, so it receives the
 *   finished file (the card burned into the video, or the card as a picture).
 *   Until TikTok approves the app, only the sandbox's target users can share.
 */
object ShareTargets {

    const val INSTAGRAM = "com.instagram.android"
    const val FACEBOOK = "com.facebook.katana"
    const val SNAPCHAT = "com.snapchat.android"
    // TikTok ships under two package names, by region; either one takes it.
    private val TIKTOK = listOf("com.zhiliaoapp.musically", "com.ss.android.ugc.trill")
    private val WHATSAPP = listOf("com.whatsapp", "com.whatsapp.w4b")
    private const val STORY_ACTION = "com.instagram.share.ADD_TO_STORY"
    private const val FACEBOOK_STORY_ACTION = "com.facebook.stories.ADD_TO_STORY"

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

    /**
     * Facebook's Story editor, the same shape as Instagram's: [background]
     * behind, [sticker] on top and movable. The App ID is required here, so
     * with none there is no intent and no button.
     */
    fun facebookStory(context: Context, background: File, backgroundMime: String, sticker: File?): Intent? {
        val appId = context.getString(R.string.facebook_app_id)
        if (appId.isBlank()) return null
        val backgroundUri = uriFor(context, background)
        val stickerUri = sticker?.let { uriFor(context, it) }
        val intent = Intent(FACEBOOK_STORY_ACTION).apply {
            setDataAndType(backgroundUri, backgroundMime)
            stickerUri?.let { putExtra("interactive_asset_uri", it) }
            putExtra("com.facebook.platform.extra.APPLICATION_ID", appId)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.grantUriPermission(FACEBOOK, backgroundUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        stickerUri?.let { context.grantUriPermission(FACEBOOK, it, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
        val resolved = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return if (resolved != null) intent else null
    }

    /**
     * Snapchat's editor with [background] full screen and [sticker] on top,
     * movable — Creative Kit Lite's "share to preview". [stickerAspect] is the
     * sticker's height over its width, so it is placed at its own shape.
     *
     * Sized the way the card sits everywhere else (24/09, third try). b180
     * asked for 300 dp wide and ran off the bottom; b182–b186 fitted it in
     * the 300 dp box Snap's SDK documents and it came out narrow, "fora de
     * proporção" next to the same card on TikTok. Creative Kit Lite drew
     * b180's sticker taller than 300 dp, so the box is not enforced here:
     * the card now takes the screen's width, margins included, as it does
     * in the video burned for TikTok, with its foot above Snapchat's row of
     * friends.
     */
    fun snapchatPreview(
        context: Context,
        background: File,
        backgroundMime: String,
        sticker: File?,
        stickerAspect: Float,
    ): Intent? {
        val clientId = context.getString(R.string.snap_client_id)
        if (clientId.isBlank()) return null
        val backgroundUri = uriFor(context, background)
        val intent = Intent(Intent.ACTION_SEND).apply {
            setPackage(SNAPCHAT)
            putExtra("CLIENT_ID", clientId)
            setDataAndType(
                Uri.parse("snapchat://creativekit/preview"),
                if (backgroundMime.startsWith("video")) "video/*" else "image/*",
            )
            putExtra(Intent.EXTRA_STREAM, backgroundUri)
            // Snap's sample passes an empty PendingIntent for Snapchat to
            // answer to; the key and the request code are theirs.
            putExtra(
                "RESULT_INTENT",
                PendingIntent.getActivity(context, 9834, Intent(), PendingIntent.FLAG_IMMUTABLE),
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.grantUriPermission(SNAPCHAT, backgroundUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        sticker?.let {
            val stickerUri = uriFor(context, it)
            context.grantUriPermission(SNAPCHAT, stickerUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val screen = context.resources.configuration
            val place = snapStickerPlacement(stickerAspect, screen.screenWidthDp, screen.screenHeightDp)
            val json = JSONObject()
                .put("uri", stickerUri.toString())
                .put("posX", 0.5)
                .put("posY", place.posY)
                .put("rotation", 0)
                .put("widthDp", place.widthDp)
                .put("heightDp", place.heightDp)
            intent.putExtra("sticker", json.toString())
        }
        val resolved = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return if (resolved != null) intent else null
    }

    /** Where Snapchat's sticker goes: its size in dp and its centre's height. */
    data class SnapPlacement(val widthDp: Int, val heightDp: Int, val posY: Double)

    // Fractions of the screen's height, measured on Felipe's phone (24/09):
    // Snapchat's row of friends begins about 77% down, and its top bar ends
    // about 12% down. The card lives between the two.
    private const val SNAP_BOTTOM = 0.76
    private const val SNAP_TOP = 0.12

    /**
     * The card at the screen's full width and its own shape, its foot at
     * [SNAP_BOTTOM]; narrowed only if it would otherwise climb above
     * [SNAP_TOP]. Pure, so it is tested without a device.
     */
    fun snapStickerPlacement(aspect: Float, screenWidthDp: Int, screenHeightDp: Int): SnapPlacement {
        val a = if (aspect.isFinite() && aspect > 0f) aspect else 1f
        val screenH = screenHeightDp.coerceAtLeast(1)
        var width = screenWidthDp.coerceAtLeast(1)
        var height = (width * a).toInt()
        val room = (screenH * (SNAP_BOTTOM - SNAP_TOP)).toInt()
        if (height > room) {
            height = room
            width = (room / a).toInt()
        }
        val posY = SNAP_BOTTOM - height.toDouble() / screenH / 2
        return SnapPlacement(width, height, posY)
    }

    /**
     * [photo] cover-scaled and centre-cropped to 1080 × 1920. Snap asks for
     * a 9:16 background in its preview, and warns that any other shape
     * breaks its editable canvas.
     */
    fun storyFrame(photo: Bitmap): Bitmap {
        val w = 1080
        val h = 1920
        val scale = maxOf(w.toFloat() / photo.width, h.toFloat() / photo.height)
        val sw = (w / scale).toInt().coerceIn(1, photo.width)
        val sh = (h / scale).toInt().coerceIn(1, photo.height)
        val cropped = Bitmap.createBitmap(photo, (photo.width - sw) / 2, (photo.height - sh) / 2, sw, sh)
        return Bitmap.createScaledBitmap(cropped, w, h, true)
    }

    /**
     * A sticker Snapchat accepts: PNG, at most 1 MB (Snap's limit). The card
     * block is scaled down until it fits; at Story size on a phone nobody
     * sees the difference between 1080 and 600 pixels wide.
     */
    fun snapSticker(context: Context, sticker: Bitmap, name: String): File {
        var bitmap = if (sticker.width > 720) {
            Bitmap.createScaledBitmap(sticker, 720, sticker.height * 720 / sticker.width, true)
        } else {
            sticker
        }
        val dir = File(context.cacheDir, "cards").apply { mkdirs() }
        val file = File(dir, name)
        while (true) {
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            if (file.length() <= 1_000_000L || bitmap.width <= 240) return file
            bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width * 3 / 4, bitmap.height * 3 / 4, true)
        }
    }

    /** The TikTok on this phone, or null. */
    fun tiktok(context: Context): String? = TIKTOK.firstOrNull { installed(context, it) }

    /**
     * TikTok's editor with [file] — the same intent TikTok's Share Kit SDK
     * (2.3.1) sends, key for key: its share activity by name, the request as
     * extras, the media as a list of URI strings. Because the URI travels in
     * an extra and not as the intent's data, TikTok is granted read on it
     * explicitly.
     */
    fun tiktokShare(context: Context, pkg: String, file: File, mime: String): Intent? {
        val clientKey = context.getString(R.string.tiktok_client_key)
        if (clientKey.isBlank()) return null
        val uri = uriFor(context, file)
        context.grantUriPermission(pkg, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val video = mime.startsWith("video")
        return Intent(Intent.ACTION_SEND).apply {
            component = android.content.ComponentName(pkg, "com.ss.android.ugc.aweme.share.SystemShareActivity")
            type = if (video) "video/*" else "image/*"
            putExtra("_bytedance_params_type", 3) // a share request
            putExtra("_aweme_params_caller_open_sdk_name", "TikTok-Open-Android-SDK-Share")
            putExtra("_aweme_params_caller_open_sdk_version", "2.3.1")
            putExtra("_aweme_open_sdk_params_client_key", clientKey)
            putStringArrayListExtra(
                if (video) "AWEME_EXTRA_VIDEO_MESSAGE_PATH" else "AWEME_EXTRA_IMAGE_MESSAGE_PATH",
                arrayListOf(uri.toString()),
            )
            putExtra("_aweme_open_sdk_params_share_format", 0) // the ordinary editor, not green screen
            putExtra("_aweme_open_sdk_params_caller_package", context.packageName)
            putExtra(
                "_aweme_open_sdk_params_caller_local_entry",
                "${context.packageName}.export.TikTokShareResultActivity",
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    /** [file] handed straight to one app's own share screen. */
    fun toApp(context: Context, pkg: String, file: File, mime: String): Intent =
        Intent(Intent.ACTION_SEND)
            .setType(mime)
            .setPackage(pkg)
            .putExtra(Intent.EXTRA_STREAM, uriFor(context, file))
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

    /**
     * [file] addressed to WhatsApp's **Status** rather than a chat (#63, 24/09).
     *
     * WhatsApp publishes no way for another app to post to Status. The road
     * known to work is an undocumented extra: the chat id `status@broadcast`,
     * which is the id WhatsApp itself gives Status. It works on many versions
     * and can stop working without notice, and when WhatsApp ignores it the
     * person lands in the ordinary chat picker instead — where *Meu status*
     * is the first row. Nothing comes back to say which happened, so the
     * screen says both roads before the tap, never that it reached Status.
     */
    fun whatsappStatus(context: Context, pkg: String, file: File, mime: String): Intent =
        toApp(context, pkg, file, mime).putExtra("jid", WHATSAPP_STATUS_JID)

    const val WHATSAPP_STATUS_JID = "status@broadcast"

    /** The card on the clipboard, as an image a Story editor can paste. */
    fun copy(context: Context, card: File): Boolean = runCatching {
        val clipboard = context.getSystemService(ClipboardManager::class.java) ?: return false
        clipboard.setPrimaryClip(ClipData.newUri(context.contentResolver, "TumTum", uriFor(context, card)))
        true
    }.getOrDefault(false)

    /**
     * The card in the phone's **camera roll**, beside the photos the person
     * took. Android 10+ only.
     *
     * b189 saved it to Pictures/TumTum, and on Felipe's phone it could be
     * found in the Files app but not among his photos (24/09). The card is
     * saved to be picked later, from the photo picker of a Story, and that
     * picker opens on the camera roll. So it goes where the camera writes
     * (DCIM/Camera), the one folder every gallery shows first, Google Photos
     * included, which shows other folders only under Library. Two details
     * make it appear at once and in the right place:
     *
     *  - **IS_PENDING** while the bytes are written, cleared afterwards, so the
     *    gallery indexes a finished file, with its size and dimensions, and
     *    never an empty one;
     *  - **DATE_TAKEN** set to now, so it sorts as the newest photo and not
     *    at 1970, where a gallery puts an image with no date.
     *
     * A write that fails removes its half-made entry, so no blank thumbnail
     * is left in the roll.
     */
    fun saveToGallery(context: Context, card: Bitmap, name: String): Boolean {
        if (!canSaveToGallery) return false
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, CAMERA_ROLL)
            put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
            put(MediaStore.Images.Media.WIDTH, card.width)
            put(MediaStore.Images.Media.HEIGHT, card.height)
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = runCatching { resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) }
            .getOrNull() ?: return false
        return runCatching {
            val written = resolver.openOutputStream(uri)?.use { card.compress(Bitmap.CompressFormat.PNG, 100, it) } == true
            check(written) { "the card was not written" }
            resolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
            true
        }.getOrElse {
            runCatching { resolver.delete(uri, null, null) }
            false
        }
    }

    /** Where the camera writes its photos: the camera roll every gallery opens on. */
    const val CAMERA_ROLL = "DCIM/Camera"

    /**
     * The person's video, into our cache so a FileProvider URI of ours can be
     * handed on: a picker's `content://` grant is ours, not transferable, and
     * Instagram opening it would fail with no explanation.
     *
     * Since 26/09 it is **remuxed, not copied** ([StripMetadata]): a byte copy
     * carried the camera's GPS position and date to the Story. The result is
     * always an MP4; when the remux fails, nothing is handed over — never the
     * original in its place.
     */
    fun copyVideo(context: Context, source: Uri, nightId: Long): Pair<File, String>? = runCatching {
        val dir = File(context.cacheDir, "cards").apply { mkdirs() }
        val file = File(dir, "story-$nightId.mp4")
        if (!StripMetadata.remux(context, source, file)) {
            file.delete()
            return null
        }
        file to "video/mp4"
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

    /**
     * Only the rows: the card's own block at the full width of its sheet,
     * margins kept, so it sits as it does on the burned video (Snapchat,
     * 24/09).
     */
    fun cropRows(bitmap: Bitmap): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
        val b = opaqueBounds(pixels, w, h) ?: return bitmap
        return Bitmap.createBitmap(bitmap, 0, b[1], w, b[3] - b[1])
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
