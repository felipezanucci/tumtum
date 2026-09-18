package cc.tumtum.capture

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity

/**
 * The night, relived — inside the app.
 *
 * The curve, the peaks, the card and the sharing are the site's screens,
 * shown here rather than rebuilt here. They are the product: everything fixed
 * on them lands in this app on the next page load, with no second codebase to
 * fix twice. Native does what only native can — the capture — and this shows
 * what the web already does well.
 */
class ExperienceActivity : ComponentActivity() {

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
        val sessionId = intent.getStringExtra(EXTRA_SESSION)
        val target = if (sessionId != null) "$WEB_URL/experience?session=$sessionId" else "$WEB_URL/sessions"

        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                // The site's browser capture screen is a dead end inside this
                // frame — a WebView has no Web Bluetooth, so that page can only
                // say "your browser can't do this" about the one thing the app
                // around it does natively. Found on the first night: tapping
                // "Ao vivo" in the nav stranded the person one screen away from
                // the real capture. It now closes the frame, landing exactly
                // there.
                //
                // That nav item has since been removed entirely — capture is
                // this app's job, and the browser could never be a fallback for
                // it anyway (the H10's two BLE connections are already taken by
                // this app and by Polar's own during an event). This stays as
                // the second net: the route still exists, and anything that
                // reaches it from inside the frame lands on the native screen
                // rather than on an apology.
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

        web.loadUrl(BOOTSTRAP_URL)
    }

    @Deprecated("Deprecated in Android; fine for a back-goes-back web screen.")
    override fun onBackPressed() {
        if (web.canGoBack()) web.goBack() else @Suppress("DEPRECATION") super.onBackPressed()
    }

    companion object {
        /** The product lives here; the app frames it. */
        const val WEB_URL = "https://tumtum.cc"

        /**
         * Where the token handover happens.
         *
         * This used to be the site root, which since 2026-08-26 is the public
         * sales page — so every time somebody opened their own night inside
         * the app, the marketing landing flashed up first. The bootstrap only
         * needs *a* page on the same origin to reach localStorage, and the
         * sign-in screen is both cheap and the one page whose appearance
         * during a sign-in handover is not a lie.
         */
        private const val BOOTSTRAP_URL = "$WEB_URL/login"

        private const val EXTRA_SESSION = "session_id"

        fun open(context: Context, sessionId: String) {
            context.startActivity(
                Intent(context, ExperienceActivity::class.java)
                    .putExtra(EXTRA_SESSION, sessionId)
            )
        }
    }
}
