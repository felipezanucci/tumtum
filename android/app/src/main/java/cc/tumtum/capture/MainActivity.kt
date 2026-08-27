package cc.tumtum.capture

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.ParcelUuid
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Sign in, choose the night, capture, send.
 *
 * The capture itself is deliberately untouched: it is the one part of this
 * project proven at real scale — 26,999 readings in 27,000 seconds with the
 * screen off. Everything added here goes around it.
 *
 * Three failures on 2026-08-27 shaped this screen, all of them the same family
 * — the app stating something false about its own state:
 *
 *  - It hid the login row whenever a token was *stored*. An expired token is
 *    still stored, so the app looked signed in, captured for as long as you
 *    liked, and failed only at the upload, with no way back to a password
 *    field. Recovering meant wiping the app's data, which would have taken a
 *    whole night's readings with it.
 *  - After that failure it said "toque de novo" while the button underneath
 *    read "Conectar sensor" and did in fact start a *new* capture.
 *  - The only code path that opened anything beyond this screen was the
 *    success branch of the upload, so a failed send left the entire product
 *    unreachable.
 */
class MainActivity : Activity() {

    private var service: CaptureService? = null
    private var scanner: android.bluetooth.le.BluetoothLeScanner? = null
    private var scanning = false

    private lateinit var bpmView: TextView
    private lateinit var bpmLabel: TextView
    private lateinit var statusView: TextView
    private lateinit var sessionNotice: TextView
    private lateinit var actionButton: Button
    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var loginButton: Button
    private lateinit var eventLabel: TextView
    private lateinit var eventPicker: Spinner
    private lateinit var openNights: TextView

    private lateinit var api: TumtumApi

    /** Login, events and upload all block on the network; none may run on this thread. */
    private val io = Executors.newSingleThreadExecutor()
    private var sending = false

    private var events: List<EventBrief> = emptyList()
    private var eventsFailed = false
    private var eventsLoading = false

    /**
     * A result that must outlive the next render. "Enviado" was being written
     * to the status line and overwritten one frame later by render(), so a
     * successful upload and a dead button looked identical.
     */
    private var notice: String? = null

    /** Set when a scan is waiting for the service to come up. */
    private var pendingScan = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            service = (binder as CaptureService.LocalBinder).service.also {
                it.listener = { runOnUiThread(::render) }
            }
            if (pendingScan) {
                pendingScan = false
                startScan()
            }
            render()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bpmView = findViewById(R.id.bpm)
        bpmLabel = findViewById(R.id.bpmLabel)
        statusView = findViewById(R.id.status)
        sessionNotice = findViewById(R.id.sessionNotice)
        actionButton = findViewById(R.id.action)
        emailField = findViewById(R.id.email)
        passwordField = findViewById(R.id.password)
        loginButton = findViewById(R.id.login)
        eventLabel = findViewById(R.id.eventLabel)
        eventPicker = findViewById(R.id.eventPicker)
        openNights = findViewById(R.id.openNights)

        api = TumtumApi(applicationContext)
        showLastCrashIfAny()
        actionButton.setOnClickListener { onAction() }
        loginButton.setOnClickListener { onLogin() }
        openNights.setOnClickListener { startActivity(Intent(this, SessionsActivity::class.java)) }

        renderEvents()
        render()

        // Bind without creating: if a capture is already running this attaches
        // to it, and if none is, nothing starts. Starting a connectedDevice
        // foreground service before the Bluetooth permission exists is refused
        // outright on Android 14 — which crashed the app on its first launch,
        // before it could ask for anything.
        bindService(Intent(this, CaptureService::class.java), connection, 0)
    }

    /**
     * A token can die while the app sits in the background, so what the screen
     * claims about the session is decided here rather than once at startup.
     */
    override fun onResume() {
        super.onResume()
        if (api.signedIn && events.isEmpty() && !eventsFailed && !eventsLoading) loadEvents()
        render()
    }

    override fun onPause() {
        super.onPause()
        saveSelectedEvent()
    }

    override fun onDestroy() {
        service?.listener = null
        // Unbinding does not stop the service: that is the point. The capture
        // outlives this screen, and outlives the screen being off.
        runCatching { unbindService(connection) }
        io.shutdown()
        super.onDestroy()
    }

    // --- The one button ---

    private fun onAction() {
        if (sending) return

        if (isCapturing() || scanning) {
            stopScan()
            service?.stopCapture()
            finishAndSend()
            return
        }

        // "Enviar de novo" now means exactly that. It used to fall through and
        // start a fresh scan, under a label promising a retry.
        if (hasUnsentCapture()) {
            finishAndSend()
            return
        }

        notice = null
        if (!ensurePermissions()) return
        beginCapture()
    }

    /**
     * CONNECTING counts as capturing. It did not, while the button already
     * read "Encerrar e enviar" during it — so pressing the button mid-connect
     * silently began a second capture instead of ending the first.
     */
    private fun isCapturing(): Boolean = when (service?.state) {
        HeartRateMonitor.State.CONNECTING,
        HeartRateMonitor.State.CONNECTED,
        HeartRateMonitor.State.RECONNECTING -> true
        else -> false
    }

    /** Readings that were captured and have not reached the server yet. */
    private fun hasUnsentCapture(): Boolean {
        val current = service ?: return false
        return current.firstReadingAt() != null && current.samples.size >= MIN_SAMPLES
    }

    private fun beginCapture() {
        val intent = Intent(this, CaptureService::class.java)
        try {
            startForegroundService(intent)
        } catch (e: Exception) {
            notice = getString(R.string.service_failed, e.message)
            render()
            return
        }
        if (service != null) {
            startScan()
        } else {
            pendingScan = true
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    /**
     * Ending a capture uploads it, then asks the backend to find the peaks.
     *
     * That second call is not optional and nothing else makes it: creating a
     * session does not run detection, and reading an experience only reads
     * peaks already stored. A capture that skipped it showed an empty list
     * under "Seus picos" — which the site renders as "sua batida seguiu no
     * mesmo ritmo". After six hours of a festival that sentence is a lie.
     *
     * On failure nothing is lost. The samples stay in the service, the button
     * says "Enviar de novo", and pressing it retries — festival cellular fails
     * often enough that retry has to be the design, not the exception.
     */
    private fun finishAndSend() {
        val current = service ?: return
        val startedAt = current.firstReadingAt()
        if (startedAt == null || current.samples.size < MIN_SAMPLES) {
            notice = getString(R.string.too_few)
            render()
            return
        }

        // Without a live token the 401 is certain. Say so before spending a
        // megabyte of somebody's data to be told the same thing.
        if (!api.signedIn) {
            notice = getString(R.string.session_expired_holding, current.samples.size)
            render()
            return
        }

        sending = true
        notice = null
        actionButton.isEnabled = false
        statusView.text = getString(R.string.sending, current.samples.size)

        val snapshot = current.samples.toList()
        val eventId = selectedEventId()
        val device = current.deviceName ?: "Sensor BLE"

        io.execute {
            val upload = runCatching { api.uploadSession(startedAt, snapshot, device, eventId) }
            // Detection is a separate call and must not be able to fail the
            // upload: a retry of the whole 1.3 MB would create a second copy
            // of the same night.
            val analysed = upload.getOrNull()?.let { id -> runCatching { api.analyze(id) }.isSuccess }

            runOnUiThread {
                sending = false
                actionButton.isEnabled = true
                upload
                    .onSuccess { sessionId ->
                        current.clearCapture()
                        notice = if (analysed == true) {
                            getString(R.string.sent)
                        } else {
                            getString(R.string.sent_not_analysed)
                        }
                        NightActivity.open(this, sessionId)
                    }
                    .onFailure { error ->
                        if (error is TumtumApi.ApiException && error.code == 401) {
                            // The token is dead. Forget it so the login row
                            // comes back — and keep every reading, because
                            // this is the only copy of them anywhere.
                            api.signOut()
                            notice = getString(R.string.session_expired_holding, snapshot.size)
                        } else {
                            notice = getString(R.string.send_failed, error.message) +
                                "\n" + getString(R.string.held_readings, snapshot.size)
                        }
                    }
                render()
            }
        }
    }

    // --- Sign in ---

    private fun onLogin() {
        val email = emailField.text.toString().trim()
        val password = passwordField.text.toString()
        if (email.isEmpty() || password.isEmpty()) return
        loginButton.isEnabled = false
        loginButton.text = getString(R.string.signing_in)
        io.execute {
            val result = runCatching { api.login(email, password) }
            runOnUiThread {
                loginButton.isEnabled = true
                loginButton.text = getString(R.string.sign_in)
                result
                    .onSuccess {
                        passwordField.setText("")
                        notice = null
                        loadEvents()
                    }
                    .onFailure { notice = it.message }
                render()
            }
        }
    }

    // --- Events ---

    private fun loadEvents() {
        if (eventsLoading) return
        eventsLoading = true
        io.execute {
            val result = runCatching { api.listEvents() }
            runOnUiThread {
                eventsLoading = false
                result
                    .onSuccess {
                        events = it
                        eventsFailed = false
                    }
                    .onFailure {
                        // An empty picker is a claim. "No events exist" and "I
                        // could not ask" have to look different, or somebody
                        // captures a night unlinked believing it was linked.
                        eventsFailed = true
                    }
                renderEvents()
                render()
            }
        }
    }

    private fun renderEvents() {
        val labels = mutableListOf(getString(R.string.event_none))
        labels += events.map { it.label }
        val adapter = ArrayAdapter(this, R.layout.spinner_item, labels)
        adapter.setDropDownViewResource(R.layout.spinner_item)
        eventPicker.adapter = adapter

        val saved = getSharedPreferences("tumtum", MODE_PRIVATE).getString(SELECTED_EVENT, null)
        val index = events.indexOfFirst { it.id == saved }
        if (index >= 0) eventPicker.setSelection(index + 1)

        eventLabel.text = if (eventsFailed) {
            getString(R.string.events_failed)
        } else {
            getString(R.string.event_label)
        }
    }

    private fun selectedEventId(): String? {
        val position = eventPicker.selectedItemPosition
        if (position <= 0) return null
        return events.getOrNull(position - 1)?.id
    }

    /**
     * Remembered across a restart of this screen. The service keeps capturing
     * while the Activity is destroyed and rebuilt, and a picker that quietly
     * reset itself to "Sem evento" would unlink a night nobody thought to
     * re-check.
     */
    private fun saveSelectedEvent() {
        if (events.isEmpty()) return
        getSharedPreferences("tumtum", MODE_PRIVATE).edit()
            .putString(SELECTED_EVENT, selectedEventId())
            .apply()
    }

    // --- What the screen says ---

    private fun render() {
        val current = service
        val capturing = isCapturing() || scanning
        val holding = !capturing && hasUnsentCapture()
        val authed = api.signedIn

        val loginVisibility = if (authed) View.GONE else View.VISIBLE
        emailField.visibility = loginVisibility
        passwordField.visibility = loginVisibility
        loginButton.visibility = loginVisibility

        // The picker only matters before a capture: once readings exist, the
        // night they belong to is already decided.
        val eventVisibility = if (authed && !capturing && !holding) View.VISIBLE else View.GONE
        eventLabel.visibility = eventVisibility
        eventPicker.visibility = eventVisibility

        // A capture in flight keeps its own display even with a dead token —
        // the strap does not care about authentication, and hiding a running
        // capture would be its own lie.
        val captureVisibility = if (authed || capturing || holding) View.VISIBLE else View.GONE
        bpmView.visibility = captureVisibility
        bpmLabel.visibility = captureVisibility
        statusView.visibility = captureVisibility

        actionButton.visibility = if (capturing || authed) View.VISIBLE else View.GONE
        openNights.visibility = if (authed) View.VISIBLE else View.GONE

        bpmView.text = current?.lastBpm?.toString() ?: "--"
        renderSessionNotice(holding)

        // While an upload runs, the status line belongs to it.
        if (sending) return

        actionButton.text = when {
            scanning -> getString(R.string.cancel)
            isCapturing() -> getString(R.string.finish_and_send)
            holding -> getString(R.string.send_again)
            else -> getString(R.string.connect)
        }

        val state = current?.state
        val readings = current?.samples?.size ?: 0
        statusView.text = notice ?: when {
            scanning -> getString(R.string.searching)
            state == HeartRateMonitor.State.CONNECTING -> getString(R.string.connecting)
            // Name the sensor: connection is silent and first-match by design,
            // so the screen has to say what it latched onto.
            state == HeartRateMonitor.State.CONNECTED -> listOfNotNull(
                current?.deviceName,
                getString(R.string.readings, readings),
            ).joinToString(" · ")
            state == HeartRateMonitor.State.RECONNECTING -> getString(R.string.reconnecting)
            state == HeartRateMonitor.State.ERROR ->
                current?.detail ?: getString(R.string.failed)
            holding -> getString(R.string.held_readings, readings)
            else -> getString(R.string.idle)
        }
    }

    /**
     * The line that would have saved the afternoon of 2026-08-27: what the app
     * knows about its own session, said before a strap goes on rather than
     * after six hours of capture.
     */
    private fun renderSessionNotice(holding: Boolean) {
        val text = when {
            !api.signedIn && holding ->
                getString(R.string.session_expired_holding, service?.samples?.size ?: 0)
            !api.signedIn && api.token != null -> getString(R.string.session_expired)
            else -> api.sessionRemainingMillis()
                ?.takeIf { it < EXPIRY_WARNING_MILLIS }
                ?.let { getString(R.string.session_expiring, humanDuration(it)) }
        }
        sessionNotice.text = text.orEmpty()
        sessionNotice.visibility = if (text == null) View.GONE else View.VISIBLE
    }

    private fun humanDuration(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        if (hours >= 1) return getString(R.string.hours_short, hours.toInt())
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        return getString(R.string.minutes_short, minutes.toInt())
    }

    /** Report a previous crash once, then forget it. */
    private fun showLastCrashIfAny() {
        val prefs = getSharedPreferences("tumtum", MODE_PRIVATE)
        val crash = prefs.getString("last_crash", null) ?: return
        prefs.edit().remove("last_crash").apply()
        notice = getString(R.string.last_crash, crash)
    }

    // --- Bluetooth ---

    /**
     * Android 12 split Bluetooth into scan and connect permissions, and both
     * have to be held before a scan will return anything — a scan without them
     * fails silently, which looks exactly like no sensor being present.
     */
    private fun ensurePermissions(): Boolean {
        val needed = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            needed += Manifest.permission.BLUETOOTH_SCAN
            needed += Manifest.permission.BLUETOOTH_CONNECT
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            needed += Manifest.permission.POST_NOTIFICATIONS
        }
        val missing = needed.filter {
            checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isEmpty()) return true
        requestPermissions(missing.toTypedArray(), PERMISSION_REQUEST)
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        if (requestCode == PERMISSION_REQUEST && grantResults.isNotEmpty() &&
            grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        ) {
            beginCapture()
        } else {
            notice = getString(R.string.needs_bluetooth_permission)
            render()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startScan() {
        val adapter = getSystemService(BluetoothManager::class.java)?.adapter
        if (adapter == null || !adapter.isEnabled) {
            notice = getString(R.string.turn_bluetooth_on)
            render()
            startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            return
        }

        scanner = adapter.bluetoothLeScanner
        scanning = true
        notice = null

        // Filter on the service itself: anything advertising 0x180D speaks the
        // standard, and anything that does not is no use to us regardless of
        // what its box says.
        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(HeartRateMonitor.HEART_RATE_SERVICE))
            .build()
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        runCatching { scanner?.startScan(listOf(filter), settings, scanCallback) }
        render()
    }

    @SuppressLint("MissingPermission")
    private fun stopScan() {
        if (!scanning) return
        scanning = false
        runCatching { scanner?.stopScan(scanCallback) }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            // The first strap that answers wins. At a pilot there is one sensor
            // per phone, and asking someone to pick from a list in the dark is
            // a worse failure than connecting to the wrong one.
            takeDevice(result.device)
        }

        override fun onScanFailed(errorCode: Int) {
            scanning = false
            runOnUiThread {
                notice = getString(R.string.scan_failed, errorCode)
                render()
            }
        }
    }

    private fun takeDevice(device: BluetoothDevice) {
        stopScan()
        runOnUiThread {
            service?.startCapture(device)
            render()
        }
    }

    companion object {
        private const val PERMISSION_REQUEST = 1
        private const val SELECTED_EVENT = "selected_event_id"

        /** The backend refuses to analyse fewer than this, so neither do we. */
        private const val MIN_SAMPLES = 10

        /** Half a day: enough warning to sign in again before a six-hour show. */
        private val EXPIRY_WARNING_MILLIS = TimeUnit.HOURS.toMillis(12)
    }
}
