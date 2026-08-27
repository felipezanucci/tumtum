package cc.tumtum.capture

import android.app.Activity
import android.content.Context
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
 * A night, natively: the curve, the moments, and the card that comes out of it.
 *
 * This replaces the WebView as the place a capture is relived. Felipe's call,
 * 2026-08-27: the whole experience happens inside the app, so the "abrir no
 * site" net that briefly lived at the bottom of this screen was removed the
 * same day it appeared. ExperienceActivity stays in the codebase, doorless,
 * because deleting a working screen two days before the only six-hour test
 * buys nothing.
 *
 * The empty state is the whole design problem here. An empty list of moments
 * can mean three different things, and the site's version of this screen said
 * "sua batida seguiu no mesmo ritmo" for all of them — including the case
 * where nothing had looked yet. After six hours of a festival that sentence is
 * a lie, and it is the thirteenth time this project has found the app claiming
 * something it never checked.
 */
class NightActivity : Activity() {

    private lateinit var api: TumtumApi
    private lateinit var whenView: TextView
    private lateinit var statsView: TextView
    private lateinit var stateView: TextView
    private lateinit var retryButton: Button
    private lateinit var curve: HRCurveView
    private lateinit var curveCaption: TextView
    private lateinit var momentsTitle: TextView
    private lateinit var momentsEmpty: TextView
    private lateinit var findMoments: Button
    private lateinit var moments: LinearLayout
    private lateinit var makeCard: Button

    private val io = Executors.newSingleThreadExecutor()

    private lateinit var sessionId: String
    private var experience: Experience? = null

    /**
     * Whether detection has been run during this visit.
     *
     * Without it the screen cannot tell "nobody has looked yet" from "we
     * looked and found nothing", and those two deserve different sentences.
     */
    private var lookedForMoments = false
    private var busy = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_night)
        api = TumtumApi(applicationContext)

        whenView = findViewById(R.id.nightWhen)
        statsView = findViewById(R.id.stats)
        stateView = findViewById(R.id.state)
        retryButton = findViewById(R.id.retry)
        curve = findViewById(R.id.curve)
        curveCaption = findViewById(R.id.curveCaption)
        momentsTitle = findViewById(R.id.momentsTitle)
        momentsEmpty = findViewById(R.id.momentsEmpty)
        findMoments = findViewById(R.id.findMoments)
        moments = findViewById(R.id.moments)
        makeCard = findViewById(R.id.makeCard)

        sessionId = intent.getStringExtra(EXTRA_SESSION).orEmpty()

        retryButton.setOnClickListener { load() }
        findMoments.setOnClickListener { analyse() }
        makeCard.setOnClickListener { onMakeCard() }

        load()
    }

    override fun onDestroy() {
        io.shutdown()
        super.onDestroy()
    }

    private fun load() {
        if (sessionId.isEmpty()) {
            showState(getString(R.string.night_failed, "sem sessão"), retry = false)
            return
        }
        if (!api.signedIn) {
            showState(getString(R.string.nights_signed_out), retry = false)
            return
        }
        showState(getString(R.string.loading), retry = false)
        io.execute {
            val result = runCatching { api.getExperience(sessionId) }
            runOnUiThread {
                result
                    .onSuccess {
                        experience = it
                        render()
                    }
                    .onFailure { error ->
                        if (error is TumtumApi.ApiException && error.code == 401) {
                            api.signOut()
                            showState(getString(R.string.nights_signed_out), retry = false)
                        } else {
                            showState(
                                getString(R.string.night_failed, error.message),
                                retry = true,
                            )
                        }
                    }
            }
        }
    }

    /** Ask the backend to look for moments, then show what it found. */
    private fun analyse() {
        if (busy) return
        busy = true
        findMoments.isEnabled = false
        findMoments.text = getString(R.string.finding_moments)
        io.execute {
            val analysed = runCatching { api.analyze(sessionId) }
            val refreshed = if (analysed.isSuccess) {
                runCatching { api.getExperience(sessionId) }.getOrNull()
            } else {
                null
            }
            runOnUiThread {
                busy = false
                findMoments.isEnabled = true
                findMoments.text = getString(R.string.find_moments)
                if (refreshed != null) {
                    lookedForMoments = true
                    experience = refreshed
                    render()
                } else {
                    val error = analysed.exceptionOrNull()
                    momentsEmpty.text = getString(R.string.night_failed, error?.message)
                    momentsEmpty.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun render() {
        val data = experience ?: return
        stateView.visibility = View.GONE
        retryButton.visibility = View.GONE

        val session = data.session
        whenView.text = session.startTimeMillis?.let { DAY_AND_TIME.format(Date(it)) }.orEmpty()

        statsView.text = listOfNotNull(
            session.avgBpm?.let { getString(R.string.stat_avg, it) },
            session.maxBpm?.let { getString(R.string.stat_max, it) },
            session.minBpm?.let { getString(R.string.stat_min, it) },
            session.qualityScore?.let { getString(R.string.stat_quality, it) },
        ).joinToString("   ")

        if (data.hrData.size >= 2) {
            curve.show(data.hrData, data.peaks)
            curve.visibility = View.VISIBLE
            curveCaption.visibility = View.VISIBLE
        } else {
            curve.visibility = View.GONE
            curveCaption.visibility = View.GONE
        }

        momentsTitle.visibility = View.VISIBLE
        renderMoments(data)

        // A card can be made from any night: card 01 needs only a peak or the
        // session's own maximum, which every capture has.
        makeCard.visibility = View.VISIBLE
    }

    private fun renderMoments(data: Experience) {
        moments.removeAllViews()

        if (data.peaks.isNotEmpty()) {
            momentsEmpty.visibility = View.GONE
            findMoments.visibility = View.GONE
            val inflater = LayoutInflater.from(this)
            for (peak in data.peaks) {
                val row = inflater.inflate(R.layout.moment_row, moments, false)
                row.findViewById<TextView>(R.id.rank).text =
                    peak.rank?.let { getString(R.string.moment_rank, it) }.orEmpty()
                row.findViewById<TextView>(R.id.momentBpm).text =
                    getString(R.string.moment_bpm, peak.bpm)
                // The matched moment when the event carries a timeline; the
                // clock when it does not. Never a blank line.
                row.findViewById<TextView>(R.id.momentLabel).text = peak.matchedLabel
                    ?: peak.timestampMillis?.let { CLOCK.format(Date(it)) }.orEmpty()
                row.findViewById<TextView>(R.id.momentDuration).text =
                    getString(R.string.moment_duration, peak.durationSeconds)
                moments.addView(row)
            }
            return
        }

        // Nothing to show. Which of the three silences is it?
        val seconds = data.session.durationSeconds
        momentsEmpty.visibility = View.VISIBLE
        when {
            seconds != null && seconds < BASELINE_WINDOW_SECONDS -> {
                // Correct behaviour, not a failure: a rise needs the five
                // minutes around it to stand out against.
                momentsEmpty.text = getString(
                    R.string.moments_too_short,
                    maxOf(1L, Math.round(seconds / 60.0)).toInt(),
                )
                findMoments.visibility = View.GONE
            }
            lookedForMoments -> {
                momentsEmpty.text = getString(R.string.moments_none)
                findMoments.visibility = View.GONE
            }
            else -> {
                momentsEmpty.text = getString(R.string.moments_not_looked)
                findMoments.visibility = View.VISIBLE
            }
        }
    }

    private fun onMakeCard() {
        if (busy) return
        busy = true
        makeCard.isEnabled = false
        makeCard.text = getString(R.string.making_card)
        val topPeak = experience?.peaks?.firstOrNull()?.id
        io.execute {
            val result = runCatching { api.createCard(sessionId, topPeak) }
            runOnUiThread {
                busy = false
                makeCard.isEnabled = true
                makeCard.text = getString(R.string.make_card)
                result
                    .onSuccess { CardActivity.open(this, it) }
                    .onFailure {
                        stateView.text = getString(R.string.card_failed, it.message)
                        stateView.visibility = View.VISIBLE
                    }
            }
        }
    }

    private fun showState(text: String, retry: Boolean) {
        stateView.text = text
        stateView.visibility = View.VISIBLE
        retryButton.visibility = if (retry) View.VISIBLE else View.GONE
        curve.visibility = View.GONE
        curveCaption.visibility = View.GONE
        momentsTitle.visibility = View.GONE
        momentsEmpty.visibility = View.GONE
        findMoments.visibility = View.GONE
        makeCard.visibility = View.GONE
        moments.removeAllViews()
    }

    companion object {
        private const val EXTRA_SESSION = "session_id"

        /** The detector's baseline window, from CLAUDE.md and peak_detection.py. */
        private const val BASELINE_WINDOW_SECONDS = 300L

        private val DAY_AND_TIME = SimpleDateFormat("dd 'de' MMMM · HH:mm", Locale("pt", "BR"))
        private val CLOCK = SimpleDateFormat("HH:mm", Locale("pt", "BR"))

        fun open(context: Context, sessionId: String) {
            context.startActivity(
                Intent(context, NightActivity::class.java)
                    .putExtra(EXTRA_SESSION, sessionId)
            )
        }
    }
}
