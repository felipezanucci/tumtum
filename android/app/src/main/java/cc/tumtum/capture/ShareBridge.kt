package cc.tumtum.capture

import android.app.Activity
import android.webkit.JavascriptInterface
import java.util.concurrent.Executors

/**
 * The page's way out of the frame.
 *
 * A WebView has no navigator.share, so the site inside it could only offer its
 * fallback list of platform links — a dead end in the one place that can open
 * the real Android share sheet. The page detects this bridge and hands the
 * share over.
 *
 * The download and the sheet now live in CardSharing, shared with the native
 * card screen: two ways of reaching a card, one implementation of the part
 * that is easy to get subtly wrong.
 */
class ShareBridge(private val activity: Activity) {

    private val io = Executors.newSingleThreadExecutor()

    @JavascriptInterface
    fun shareImage(imageUrl: String, text: String) {
        io.execute {
            val file = CardSharing.download(activity, imageUrl)
            val uri = file?.let { CardSharing.contentUri(it) }
            activity.runOnUiThread { CardSharing.openSheet(activity, uri, text) }
        }
    }
}
