package cc.tumtum.capture

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import java.io.File
import java.util.concurrent.Executors

/**
 * The card, and the one thing anybody wants to do with it.
 *
 * The image itself is generated on the server — card 01 is drawn to the brand
 * manual there, at Story size and inside the safe areas — so this screen
 * fetches a PNG rather than redrawing one. Native or not, that call is the
 * same; the independence worth having here is not having to leave the app.
 *
 * The file is downloaded once and used twice: decoded for the preview, and
 * handed to the share sheet through CardProvider. Sharing an image somebody
 * has not seen would be its own small dishonesty.
 */
class CardActivity : Activity() {

    private lateinit var api: TumtumApi
    private lateinit var stateView: TextView
    private lateinit var retryButton: Button
    private lateinit var cardView: ImageView
    private lateinit var shareButton: Button

    private val io = Executors.newSingleThreadExecutor()

    private lateinit var cardId: String
    private var file: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card)
        api = TumtumApi(applicationContext)

        stateView = findViewById(R.id.state)
        retryButton = findViewById(R.id.retry)
        cardView = findViewById(R.id.card)
        shareButton = findViewById(R.id.share)

        cardId = intent.getStringExtra(EXTRA_CARD).orEmpty()
        retryButton.setOnClickListener { load() }
        shareButton.setOnClickListener { share() }
        Chrome.wire(this, api)

        load()
    }

    override fun onDestroy() {
        io.shutdown()
        super.onDestroy()
    }

    private fun load() {
        if (cardId.isEmpty()) {
            showState(getString(R.string.card_failed, "sem card"), retry = false)
            return
        }
        showState(getString(R.string.loading), retry = false)
        cardView.visibility = View.GONE
        shareButton.visibility = View.GONE

        io.execute {
            // The image endpoint is public so a shared link can render a
            // preview; no token is needed to fetch our own.
            val downloaded = CardSharing.download(this, api.cardImageUrl(cardId), "$cardId.png")
            val bitmap = downloaded?.let {
                runCatching { BitmapFactory.decodeFile(it.absolutePath) }.getOrNull()
            }
            runOnUiThread {
                if (bitmap == null) {
                    showState(
                        getString(R.string.card_image_failed, "não consegui baixar"),
                        retry = true,
                    )
                    return@runOnUiThread
                }
                file = downloaded
                cardView.setImageBitmap(bitmap)
                cardView.visibility = View.VISIBLE
                shareButton.visibility = View.VISIBLE
                stateView.visibility = View.GONE
                retryButton.visibility = View.GONE
            }
        }
    }

    private fun share() {
        val image = file ?: return
        CardSharing.openSheet(this, CardSharing.contentUri(image), getString(R.string.share_text))
        // Recorded, never guessed: the sheet does not tell us which app was
        // picked, so the platform is logged as "native" and nothing more.
        io.execute { api.recordShare(cardId) }
    }

    private fun showState(text: String, retry: Boolean) {
        stateView.text = text
        stateView.visibility = View.VISIBLE
        retryButton.visibility = if (retry) View.VISIBLE else View.GONE
    }

    companion object {
        private const val EXTRA_CARD = "card_id"

        fun open(context: Context, cardId: String) {
            context.startActivity(
                Intent(context, CardActivity::class.java).putExtra(EXTRA_CARD, cardId)
            )
        }
    }
}
