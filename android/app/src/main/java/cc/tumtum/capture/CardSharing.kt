package cc.tumtum.capture

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Getting a card out of the app and into somewhere people will see it.
 *
 * Shared by the native card screen and by the WebView bridge, so the two ways
 * of reaching a card share one implementation of the part that is easy to get
 * subtly wrong: another app cannot read our cache directly, and file:// URIs
 * have been banned from intents since Android 7, so a shared image has to
 * arrive through a content provider.
 */
object CardSharing {

    /** Where a downloaded card lands. CardProvider serves exactly this directory. */
    private fun cardsDir(context: Context) = File(context.cacheDir, "cards").apply { mkdirs() }

    /**
     * Download a card image and return the file it landed in, or null.
     *
     * Blocks. Must not be called on the main thread.
     */
    fun download(context: Context, imageUrl: String, name: String = "tumtum-card.png"): File? {
        if (imageUrl.isEmpty()) return null
        val connection = URL(imageUrl).openConnection() as HttpURLConnection
        return try {
            connection.connectTimeout = 15_000
            connection.readTimeout = 60_000
            if (connection.responseCode !in 200..299) return null
            val file = File(cardsDir(context), name)
            connection.inputStream.use { input ->
                file.outputStream().use { input.copyTo(it) }
            }
            file
        } catch (e: Exception) {
            null
        } finally {
            connection.disconnect()
        }
    }

    fun contentUri(file: File): Uri =
        Uri.parse("content://" + CardProvider.AUTHORITY + "/" + file.name)

    /**
     * Open the system share sheet with the picture attached.
     *
     * With the image when the download worked; the text still carries the
     * link either way, so a failed download degrades to a share rather than
     * to nothing.
     */
    fun openSheet(activity: Activity, image: Uri?, text: String) {
        val send = Intent(Intent.ACTION_SEND).apply {
            if (image != null) {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, image)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else {
                type = "text/plain"
            }
            if (text.isNotEmpty()) putExtra(Intent.EXTRA_TEXT, text)
        }
        runCatching { activity.startActivity(Intent.createChooser(send, null)) }
    }
}
