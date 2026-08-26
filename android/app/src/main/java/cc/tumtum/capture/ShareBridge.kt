package cc.tumtum.capture

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.webkit.JavascriptInterface
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

/**
 * The page's way out of the frame.
 *
 * A WebView has no navigator.share, so the site inside it could only offer
 * its fallback list of platform links — a dead end in the one place that can
 * open the real Android share sheet. The page detects this bridge and hands
 * the share over: the app downloads the card and opens the system sheet with
 * the picture attached, which is what sharing a card means on a phone.
 */
class ShareBridge(private val activity: Activity) {

    private val io = Executors.newSingleThreadExecutor()

    @JavascriptInterface
    fun shareImage(imageUrl: String, text: String) {
        io.execute {
            val uri = runCatching { download(imageUrl) }.getOrNull()
            activity.runOnUiThread { openSheet(uri, text) }
        }
    }

    private fun download(imageUrl: String): Uri? {
        if (imageUrl.isEmpty()) return null
        val connection = URL(imageUrl).openConnection() as HttpURLConnection
        return try {
            connection.connectTimeout = 15_000
            connection.readTimeout = 60_000
            if (connection.responseCode !in 200..299) return null
            val dir = File(activity.cacheDir, "cards").apply { mkdirs() }
            val file = File(dir, "tumtum-card.png")
            connection.inputStream.use { input ->
                file.outputStream().use { input.copyTo(it) }
            }
            Uri.parse("content://" + CardProvider.AUTHORITY + "/" + file.name)
        } finally {
            connection.disconnect()
        }
    }

    /** With the image when the download worked; the text still carries the link either way. */
    private fun openSheet(image: Uri?, text: String) {
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
