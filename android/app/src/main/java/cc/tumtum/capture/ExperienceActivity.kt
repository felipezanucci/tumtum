package cc.tumtum.capture

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

/**
 * The night, relived — inside the app.
 *
 * The curve, the peaks, the card and the sharing are the site's screens,
 * shown here rather than rebuilt here. They are the product: everything fixed
 * on them lands in this app on the next page load, with no second codebase to
 * fix twice. Native does what only native can — the capture — and this shows
 * what the web already does well.
 */
class ExperienceActivity : Activity() {

    private lateinit var web: WebView
    private var bootstrapped = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        web = WebView(this)
        setContentView(web)

        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        // The page prefers this over its fallback platform list; see ShareBridge.
        web.addJavascriptInterface(ShareBridge(this), "TumTumAndroid")

        val token = TumtumApi(applicationContext).token
        val target = intent.getStringExtra(EXTRA_URL) ?: WEB_URL

        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                // The site's "Ao vivo" is a dead end inside this frame — a
                // WebView has no Web Bluetooth, so that page can only say "your
                // browser can't do this" about the one thing the app around it
                // does natively. Found on the first night: tapping Ao vivo in
                // the nav stranded the person one screen away from the real
                // capture. It now closes the frame, landing exactly there.
                if (request.url.host == Uri.parse(WEB_URL).host && request.url.path == "/live") {
                    finish()
                    return true
                }
                return false
            }

            override fun onPageFinished(view: WebView, url: String) {
                // The site keeps its session in localStorage, which belongs to
                // the page, not to us. So: land on the origin once, hand over
                // the token the person already typed into the native screen,
                // then go where we were going. Without this they would be asked
                // to sign in a second time to see their own night.
                if (!bootstrapped) {
                    bootstrapped = true
                    if (token != null) {
                        view.evaluateJavascript(
                            "localStorage.setItem('access_token', '" + token.replace("'", "") + "');"
                        ) { view.loadUrl(target) }
                    } else {
                        view.loadUrl(target)
                    }
                }
            }
        }

        // The card's "Baixar" is a real download; hand it to the system so it
        // lands in the gallery, where sharing to any app is already solved.
        web.setDownloadListener { url, _, _, _, _ ->
            runCatching {
                val request = DownloadManager.Request(Uri.parse(url))
                    .setNotificationVisibility(
                        DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                    )
                    .setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS, "tumtum-card.png"
                    )
                (getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
            }
        }

        web.loadUrl(WEB_URL)
    }

    @Deprecated("Deprecated in Android; fine for a back-goes-back web screen.")
    override fun onBackPressed() {
        if (web.canGoBack()) web.goBack() else @Suppress("DEPRECATION") super.onBackPressed()
    }

    companion object {
        /** The product lives here; the app frames it. */
        const val WEB_URL = "https://tumtum.cc"
        private const val EXTRA_URL = "url"

        fun open(context: Context, path: String) {
            context.startActivity(
                Intent(context, ExperienceActivity::class.java)
                    .putExtra(EXTRA_URL, WEB_URL + path)
            )
        }
    }
}
