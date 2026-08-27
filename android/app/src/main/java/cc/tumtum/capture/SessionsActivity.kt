package cc.tumtum.capture

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

/**
 * Every capture, listed — and the app's way in that does not depend on an
 * upload having just worked.
 *
 * Until this existed the only code path that opened anything beyond the
 * capture screen was the success branch of the upload. So on 2026-08-27, when
 * a token quietly expired and the send failed, the whole product became
 * unreachable from inside its own app: no sessions, no cards, no night.
 *
 * The four states below are the point of the screen. An empty list is a claim
 * about the world, and "nada aqui" must never stand in for "não consegui
 * perguntar" — the rule this project earned twelve times over.
 */
class SessionsActivity : Activity() {

    private lateinit var api: TumtumApi
    private lateinit var stateView: TextView
    private lateinit var retryButton: Button
    private lateinit var list: LinearLayout

    private val io = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sessions)
        api = TumtumApi(applicationContext)
        stateView = findViewById(R.id.state)
        retryButton = findViewById(R.id.retry)
        list = findViewById(R.id.list)
        retryButton.setOnClickListener { load() }
    }

    /** Reloaded on every return, so a night sent a minute ago is already here. */
    override fun onResume() {
        super.onResume()
        load()
    }

    override fun onDestroy() {
        io.shutdown()
        super.onDestroy()
    }

    private fun load() {
        if (!api.signedIn) {
            showState(getString(R.string.nights_signed_out), retry = false)
            list.removeAllViews()
            return
        }

        showState(getString(R.string.loading), retry = false)
        io.execute {
            val result = runCatching { api.listSessions() }
            runOnUiThread {
                result
                    .onSuccess { render(it) }
                    .onFailure { error ->
                        if (error is TumtumApi.ApiException && error.code == 401) {
                            // Forget a token the server has already refused,
                            // so the capture screen offers a password field
                            // again instead of pretending to be signed in.
                            api.signOut()
                            showState(getString(R.string.nights_signed_out), retry = false)
                        } else {
                            showState(
                                getString(R.string.nights_failed, error.message),
                                retry = true,
                            )
                        }
                        list.removeAllViews()
                    }
            }
        }
    }

    private fun render(sessions: List<SessionSummary>) {
        list.removeAllViews()

        if (sessions.isEmpty()) {
            showState(getString(R.string.nights_empty), retry = false)
            return
        }
        stateView.visibility = View.GONE
        retryButton.visibility = View.GONE

        // Newest first: "did my capture arrive?" is always about the last one.
        val ordered = sessions.sortedByDescending { it.startTimeMillis ?: 0L }
        val inflater = LayoutInflater.from(this)

        for (session in ordered) {
            val row = inflater.inflate(R.layout.session_row, list, false)
            row.findViewById<TextView>(R.id.whenLabel).text = whenLabel(session)
            row.findViewById<TextView>(R.id.detail).text = detailLabel(session)
            row.findViewById<TextView>(R.id.max).text =
                session.maxBpm?.let { getString(R.string.max_bpm, it) }.orEmpty()
            row.findViewById<TextView>(R.id.avg).text =
                session.avgBpm?.let { getString(R.string.avg_bpm, it) }.orEmpty()
            row.setOnClickListener { ExperienceActivity.open(this, session.id) }
            list.addView(row)
        }
    }

    /** "29 de ago · 22:14", in the phone's own timezone. */
    private fun whenLabel(session: SessionSummary): String {
        val start = session.startTimeMillis ?: return "—"
        return DAY_AND_TIME.format(Date(start))
    }

    private fun detailLabel(session: SessionSummary): String {
        val parts = mutableListOf<String>()
        session.durationSeconds?.let { parts += duration(it) }
        session.sourceDevice?.let { parts += it }
        return parts.joinToString(" · ")
    }

    private fun duration(seconds: Long): String {
        val minutes = seconds / 60
        return if (minutes >= 60) {
            getString(R.string.duration_hm, (minutes / 60).toInt(), (minutes % 60).toInt())
        } else {
            getString(R.string.duration_m, minutes.toInt())
        }
    }

    private fun showState(text: String, retry: Boolean) {
        stateView.text = text
        stateView.visibility = View.VISIBLE
        retryButton.visibility = if (retry) View.VISIBLE else View.GONE
    }

    companion object {
        private val DAY_AND_TIME = SimpleDateFormat("dd 'de' MMM · HH:mm", Locale("pt", "BR"))

        fun open(activity: Activity) {
            activity.startActivity(Intent(activity, SessionsActivity::class.java))
        }
    }
}
