package cc.tumtum.app.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The photo behind a night's black card (§5.11), kept with the night once the
 * card goes out. Until 21/09 it lived only in the card screen's memory — "for
 * this card, this share" — so the gallery showed the skin and not the photo,
 * and "Compartilhar de novo" after leaving the screen had lost it. The night
 * keeps what its last shared card looked like; that is the gallery's claim.
 *
 * Stored at card size (the long side capped at 1920 px), as JPEG, in filesDir.
 * A new file per save, named by night and time, so Compose sees a new path
 * when the photo changes and never a stale cache.
 */
object CardPhotoStore {

    private const val MAX_SIDE = 1920

    suspend fun save(context: Context, nightId: Long, photo: Bitmap, previousPath: String?): String? =
        withContext(Dispatchers.IO) {
            runCatching {
                val dir = File(context.filesDir, "card_photos").apply { mkdirs() }
                val file = File(dir, "night_${nightId}_${System.currentTimeMillis()}.jpg")
                val longest = maxOf(photo.width, photo.height)
                val scaled = if (longest > MAX_SIDE) {
                    val k = MAX_SIDE.toFloat() / longest
                    Bitmap.createScaledBitmap(photo, (photo.width * k).toInt().coerceAtLeast(1), (photo.height * k).toInt().coerceAtLeast(1), true)
                } else {
                    photo
                }
                file.outputStream().use { out -> scaled.compress(Bitmap.CompressFormat.JPEG, 88, out) }
                if (scaled !== photo) scaled.recycle()
                delete(previousPath)
                file.absolutePath
            }.getOrNull()
        }

    fun delete(path: String?) {
        path?.let {
            runCatching { File(it).delete() }
            runCatching { File(it + VIDEO_SUFFIX).delete() }
        }
    }

    /*
     * A video's address, kept beside its first frame (24/09). Until then a
     * card made over a video came back, on the next visit, as a card over a
     * photo of its first frame: it looked the same, and TikTok and Snapchat
     * received a still — "o vídeo não toca", found by Felipe on the phone.
     * Kept as a small file next to the frame so it lives and dies with it,
     * with no new column on the night.
     */
    private const val VIDEO_SUFFIX = ".video"

    fun saveVideo(photoPath: String, video: Uri) {
        runCatching { File(photoPath + VIDEO_SUFFIX).writeText(video.toString()) }
    }

    /** The video the frame at [photoPath] came from, if it was a video. */
    fun videoOf(photoPath: String?): Uri? = photoPath?.let { path ->
        runCatching {
            File(path + VIDEO_SUFFIX).takeIf { it.exists() }?.readText()?.trim()?.takeIf { it.isNotEmpty() }?.let(Uri::parse)
        }.getOrNull()
    }

    /** Apagar conta apaga tudo (§7): the photos go with the nights. */
    fun deleteAll(context: Context) {
        runCatching { File(context.filesDir, "card_photos").deleteRecursively() }
    }

    /** The photo at card size, for the card screen. Null when the file is gone. */
    fun load(path: String?): Bitmap? = path?.let { runCatching { BitmapFactory.decodeFile(it) }.getOrNull() }

    /** A small copy for a gallery tile: decoded no larger than [maxSide] on its long side. */
    fun loadThumb(path: String?, maxSide: Int = 480): Bitmap? = path?.let {
        runCatching {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(it, bounds)
            if (bounds.outWidth <= 0) return@runCatching null
            var sample = 1
            while (bounds.outWidth / (sample * 2) >= maxSide && bounds.outHeight / (sample * 2) >= maxSide) sample *= 2
            BitmapFactory.decodeFile(it, BitmapFactory.Options().apply { inSampleSize = sample })
        }.getOrNull()
    }
}
