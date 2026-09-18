package cc.tumtum.capture

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.health.connect.client.PermissionController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * Trazer do meu relógio: read the night the watch already recorded.
 *
 * The screen is a corridor of gates — provider present, permission granted,
 * window chosen, readings found — and it shows what it found *before*
 * anything is uploaded: how many readings, at what cadence, and what that
 * cadence can support. Density decides the promise
 * (docs/health-connect-plan.md §2): dense enough and the night can have
 * moments; sparse and it is a curve, said plainly, with the one-sentence
 * fix — start a workout next time — offered right there.
 *
 * Every gate can also fail, and each failure gets its own sentence. "Your
 * watch wrote nothing here" is a fact about the watch; "I could not ask" is
 * a fact about us; the two never share a message.
 */
class WatchImportActivity : ComponentActivity() {

    private lateinit var api: TumtumApi
    private lateinit var stateView: TextView
    private lateinit var actionButton: Button
    private lateinit var windowTitle: TextView
    private lateinit var eventOption: RadioButton
    private lateinit var recentOption: RadioButton
    private lateinit var eventHint: TextView
    private lateinit var readButton: Button
    private lateinit var sourceView: TextView
    private lateinit var reportView: TextView
    private lateinit var verdictView: TextView
    private lateinit var sendButton: Button

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /** The chosen event, once loaded — null while loading or when none is chosen. */
    private var event: EventDetail? = null

    /**
     * Readings split by the app that wrote them, richest first — never a
     * single merged list. Health Connect is a shared store, and a cadence
     * computed across two bands describes a device nobody is wearing.
     */
    private var sources: List<Pair<String, List<HealthConnectReader.Reading>>> = emptyList()
    private var chosen: Int = 0

    /**
     * Must be registered before the activity starts, which is why it lives
     * here and not inside the click listener that launches it.
     */
    private val permissionRequest = registerForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        if (granted.containsAll(HealthConnectReader.PERMISSIONS)) {
            showWindowChoice()
        } else {
            // A system that remembers earlier refusals may not even open the
            // dialog, so the sentence covers the invisible case too.
            showState(getString(R.string.watch_permission_denied))
            actionButton.text = getString(R.string.watch_allow)
            actionButton.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watch_import)
        api = TumtumApi(applicationContext)

        stateView = findViewById(R.id.state)
        actionButton = findViewById(R.id.action)
        windowTitle = findViewById(R.id.windowTitle)
        eventOption = findViewById(R.id.optionEvent)
        recentOption = findViewById(R.id.optionRecent)
        eventHint = findViewById(R.id.eventHint)
        readButton = findViewById(R.id.read)
        sourceView = findViewById(R.id.source)
        sourceView.setOnClickListener { offerSources() }
        reportView = findViewById(R.id.report)
        verdictView = findViewById(R.id.verdict)
        sendButton = findViewById(R.id.send)

        readButton.setOnClickListener { read() }
        sendButton.setOnClickListener { send() }
        Chrome.wire(this, api)
    }

    override fun onResume() {
        super.onResume()
        begin()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    /**
     * Re-run from the top on every resume: availability and permission can
     * both change while the app is backgrounded — the person installs the
     * provider, or revokes us in its settings — and a screen remembering a
     * yes that is no longer true is a stale claim.
     */
    private fun begin() {
        hideEverything()
        if (!api.signedIn) {
            showState(getString(R.string.nights_signed_out))
            return
        }
        when (HealthConnectReader.status(this)) {
            HealthConnectReader.Status.NOT_SUPPORTED -> {
                showState(getString(R.string.watch_not_supported))
                offerPlayStore()
            }
            HealthConnectReader.Status.NEEDS_UPDATE -> {
                showState(getString(R.string.watch_needs_update))
                offerPlayStore()
            }
            HealthConnectReader.Status.READY -> {
                showState(getString(R.string.watch_checking))
                scope.launch {
                    val granted = runCatching {
                        withContext(Dispatchers.IO) { HealthConnectReader.hasPermission(this@WatchImportActivity) }
                    }.getOrDefault(false)
                    if (granted) showWindowChoice() else askPermission()
                }
            }
        }
    }

    private fun askPermission() {
        showState(getString(R.string.watch_permission_intro))
        actionButton.text = getString(R.string.watch_allow)
        actionButton.visibility = View.VISIBLE
        actionButton.setOnClickListener {
            permissionRequest.launch(HealthConnectReader.PERMISSIONS)
        }
    }

    private fun offerPlayStore() {
        actionButton.text = getString(R.string.watch_get_it)
        actionButton.visibility = View.VISIBLE
        actionButton.setOnClickListener {
            val market = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=com.google.android.apps.healthdata"),
            )
            try {
                startActivity(market)
            } catch (_: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata"),
                    )
                )
            }
        }
    }

    // --- The window ---

    private fun showWindowChoice() {
        stateView.visibility = View.GONE
        actionButton.visibility = View.GONE
        windowTitle.visibility = View.VISIBLE
        recentOption.visibility = View.VISIBLE
        readButton.visibility = View.VISIBLE
        recentOption.isChecked = true

        val chosenId = getSharedPreferences("tumtum", MODE_PRIVATE)
            .getString(Chrome.SELECTED_EVENT, null)
        if (chosenId == null) {
            eventHint.text = getString(R.string.watch_no_event)
            eventHint.visibility = View.VISIBLE
            return
        }
        scope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { api.getEvent(chosenId) }
            }
            result
                .onSuccess { detail ->
                    event = detail
                    if (eventWindow(detail) != null) {
                        eventOption.text = detail.name
                        eventOption.visibility = View.VISIBLE
                        eventOption.isChecked = true
                        recentOption.isChecked = false
                    } else {
                        eventHint.text = getString(R.string.watch_event_no_times, detail.name)
                        eventHint.visibility = View.VISIBLE
                    }
                }
                .onFailure {
                    // The recent window still works; say only what is missing.
                    eventHint.text = getString(R.string.watch_event_failed)
                    eventHint.visibility = View.VISIBLE
                }
        }
    }

    /**
     * The event's night as two instants, in the phone's zone — the phone was
     * at the show, so its zone is the night's zone. The API's time strings
     * are wall clock (`"22:00:00…"`, any suffix ignored, same as the edit
     * screen reads them); an end at or before the start means the small
     * hours of the next day, the convention the edit screen already names.
     */
    private fun eventWindow(detail: EventDetail): Pair<Long, Long>? {
        val date = runCatching { LocalDate.parse(detail.date) }.getOrNull() ?: return null
        val start = wallClock(detail.startTime) ?: return null
        val end = wallClock(detail.endTime) ?: return null
        val zone = ZoneId.systemDefault()
        val startAt = LocalDateTime.of(date, start)
        var endAt = LocalDateTime.of(date, end)
        if (!endAt.isAfter(startAt)) endAt = endAt.plusDays(1)
        return startAt.atZone(zone).toInstant().toEpochMilli() to
            endAt.atZone(zone).toInstant().toEpochMilli()
    }

    private fun wallClock(value: String?): LocalTime? {
        val parts = value?.split(":") ?: return null
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: return null
        val minute = parts.getOrNull(1)?.take(2)?.toIntOrNull() ?: return null
        return runCatching { LocalTime.of(hour, minute) }.getOrNull()
    }

    // --- Reading ---

    private fun read() {
        val window = if (eventOption.isChecked) {
            event?.let { eventWindow(it) } ?: return
        } else {
            val now = System.currentTimeMillis()
            (now - 6 * 3600_000L) to now
        }

        readButton.isEnabled = false
        readButton.text = getString(R.string.watch_reading)
        sourceView.visibility = View.GONE
        reportView.visibility = View.GONE
        verdictView.visibility = View.GONE
        sendButton.visibility = View.GONE

        scope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    HealthConnectReader.readHeartRate(
                        this@WatchImportActivity, window.first, window.second,
                    )
                }
            }
            readButton.isEnabled = true
            readButton.text = getString(R.string.watch_read)
            result
                .onSuccess { found ->
                    sources = HealthConnectReader.bySource(found)
                    chosen = 0
                    report()
                }
                .onFailure { error ->
                    reportView.text = getString(R.string.watch_read_failed, error.message)
                    reportView.visibility = View.VISIBLE
                }
        }
    }

    /**
     * What was found, before anything is uploaded, **for one source at a
     * time**. Naming the source is not decoration: two bands on one phone
     * both write here, and a blended cadence would describe a device nobody
     * is wearing — while reading as a perfectly confident number.
     */
    private fun report() {
        if (sources.isEmpty()) {
            reportView.text = getString(R.string.watch_none_found)
            reportView.visibility = View.VISIBLE
            return
        }
        val (origin, readings) = sources[chosen]
        val cadence = Cadence.of(readings.map { it.timeMillis })
        if (cadence == null) {
            reportView.text = getString(R.string.watch_none_found)
            reportView.visibility = View.VISIBLE
            return
        }

        sourceView.text = if (sources.size > 1) {
            getString(R.string.watch_source_pick, HealthConnectReader.label(origin), sources.size)
        } else {
            getString(R.string.watch_source_one, HealthConnectReader.label(origin))
        }
        sourceView.visibility = View.VISIBLE

        reportView.text = getString(
            R.string.watch_report,
            cadence.count,
            cadence.spanLabel,
            gapLabel(cadence.medianGapMillis),
            (cadence.coverage * 100).toInt(),
        )
        reportView.visibility = View.VISIBLE
        verdictView.text = getString(
            if (cadence.denseEnough) R.string.watch_verdict_dense
            else R.string.watch_verdict_sparse
        )
        verdictView.visibility = View.VISIBLE
        sendButton.visibility = View.VISIBLE
    }

    /** Switch which app's readings the report and the upload describe. */
    private fun offerSources() {
        if (sources.size < 2) return
        val popup = android.widget.PopupMenu(this, sourceView)
        sources.forEachIndexed { index, (origin, readings) ->
            popup.menu.add(
                0, index, index,
                getString(
                    R.string.watch_source_row,
                    HealthConnectReader.label(origin),
                    readings.size,
                ),
            )
        }
        popup.setOnMenuItemClickListener { item ->
            chosen = item.itemId
            report()
            true
        }
        popup.show()
    }

    private fun gapLabel(millis: Long): String {
        val seconds = (millis + 500) / 1000
        return if (seconds < 60) getString(R.string.watch_gap_seconds, seconds)
        else getString(R.string.watch_gap_minutes, seconds / 60)
    }

    // --- Upload ---

    private fun send() {
        sendButton.isEnabled = false
        sendButton.text = getString(R.string.watch_sending)
        val eventId = if (eventOption.isChecked) event?.id else null
        val (origin, readings) = sources[chosen]
        scope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    val sessionId = api.uploadWatchReadings(
                        readings, eventId, HealthConnectReader.label(origin),
                    )
                    // Detection is part of delivery, same as the capture path:
                    // a night uploaded but never analysed shows no moments and
                    // calls it calm. If only the analysis fails, the night
                    // screen owns the retry.
                    runCatching { api.analyze(sessionId) }
                    sessionId
                }
            }
            sendButton.isEnabled = true
            sendButton.text = getString(R.string.watch_send)
            result
                .onSuccess { sessionId ->
                    NightActivity.open(this@WatchImportActivity, sessionId)
                    finish()
                }
                .onFailure { error ->
                    if (api.signedIn) {
                        verdictView.text = getString(R.string.send_failed, error.message)
                        verdictView.visibility = View.VISIBLE
                    } else {
                        // A 401 signed us out; the readings live in Health
                        // Connect, not here, so nothing is lost by leaving.
                        begin()
                    }
                }
        }
    }

    // --- Plumbing ---

    private fun showState(text: String) {
        stateView.text = text
        stateView.visibility = View.VISIBLE
    }

    private fun hideEverything() {
        actionButton.visibility = View.GONE
        windowTitle.visibility = View.GONE
        eventOption.visibility = View.GONE
        recentOption.visibility = View.GONE
        eventHint.visibility = View.GONE
        readButton.visibility = View.GONE
        sourceView.visibility = View.GONE
        reportView.visibility = View.GONE
        verdictView.visibility = View.GONE
        sendButton.visibility = View.GONE
    }
}
