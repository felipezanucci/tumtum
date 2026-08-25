package cc.tumtum.capture

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.ParcelUuid
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import java.util.concurrent.Executors

/**
 * One screen: connect, watch it capture, stop.
 *
 * Deliberately thin. The curve, the peaks, the card and the sharing all live on
 * the web app, where they already work — duplicating them natively would throw
 * away everything that has been fixed there. This is a recorder.
 */
class MainActivity : Activity() {

    private var service: CaptureService? = null
    private var scanner: android.bluetooth.le.BluetoothLeScanner? = null
    private var scanning = false

    private lateinit var bpmView: TextView
    private lateinit var statusView: TextView
    private lateinit var actionButton: Button
    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var loginButton: Button

    private lateinit var api: TumtumApi

    /** Login and upload block on the network; neither may run on this thread. */
    private val io = Executors.newSingleThreadExecutor()
    private var sending = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            service = (binder as CaptureService.LocalBinder).service.also {
                it.listener = { runOnUiThread(::render) }
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
        statusView = findViewById(R.id.status)
        actionButton = findViewById(R.id.action)
        emailField = findViewById(R.id.email)
        passwordField = findViewById(R.id.password)
        loginButton = findViewById(R.id.login)

        api = TumtumApi(applicationContext)
        actionButton.setOnClickListener { onAction() }
        loginButton.setOnClickListener { onLogin() }
        renderAuth()

        val intent = Intent(this, CaptureService::class.java)
        startForegroundService(intent)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        service?.listener = null
        // Unbinding does not stop the service: that is the point. The capture
        // outlives this screen, and outlives the screen being off.
        runCatching { unbindService(connection) }
        io.shutdown()
        super.onDestroy()
    }

    private fun onAction() {
        if (sending) return
        val running = service?.state == HeartRateMonitor.State.CONNECTED ||
            service?.state == HeartRateMonitor.State.RECONNECTING
        if (running || scanning) {
            stopScan()
            service?.stopCapture()
            finishAndSend()
            return
        }
        if (!ensurePermissions()) return
        startScan()
    }

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
                result.onFailure { statusView.text = it.message }
                renderAuth()
            }
        }
    }

    /** The login row exists only until there is a token; then it gets out of the way. */
    private fun renderAuth() {
        val gone = if (api.signedIn) android.view.View.GONE else android.view.View.VISIBLE
        emailField.visibility = gone
        passwordField.visibility = gone
        loginButton.visibility = gone
        actionButton.visibility = if (api.signedIn) android.view.View.VISIBLE else android.view.View.GONE
    }

    /**
     * Ending a capture uploads it. On failure nothing is lost: the samples
     * stay in the service, the button stays on screen, and pressing it again
     * retries — festival cellular fails often enough that retry has to be the
     * design, not the exception.
     */
    private fun finishAndSend() {
        val current = service ?: return
        val startedAt = current.firstReadingAt()
        if (startedAt == null || current.samples.size < 10) {
            statusView.text = getString(R.string.too_few)
            render()
            return
        }
        sending = true
        statusView.text = getString(R.string.sending, current.samples.size)
        val snapshot = current.samples.toList()
        val name = "Sensor BLE"
        io.execute {
            val result = runCatching { api.uploadSession(startedAt, snapshot, name) }
            runOnUiThread {
                sending = false
                result
                    .onSuccess {
                        current.samples.clear()
                        statusView.text = getString(R.string.sent)
                    }
                    .onFailure {
                        statusView.text = getString(R.string.send_failed, it.message)
                    }
                render()
            }
        }
    }

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
        if (requestCode == PERMISSION_REQUEST && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            startScan()
        } else {
            statusView.text = getString(R.string.needs_bluetooth_permission)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startScan() {
        val adapter = getSystemService(BluetoothManager::class.java)?.adapter
        if (adapter == null || !adapter.isEnabled) {
            statusView.text = getString(R.string.turn_bluetooth_on)
            startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            return
        }

        scanner = adapter.bluetoothLeScanner
        scanning = true
        statusView.text = getString(R.string.searching)
        actionButton.text = getString(R.string.cancel)

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
                statusView.text = getString(R.string.scan_failed, errorCode)
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

    private fun render() {
        val current = service ?: return
        bpmView.text = current.lastBpm?.toString() ?: "--"
        if (sending) return
        actionButton.text = when (current.state) {
            HeartRateMonitor.State.CONNECTED,
            HeartRateMonitor.State.RECONNECTING,
            HeartRateMonitor.State.CONNECTING -> getString(R.string.finish_and_send)
            else -> getString(R.string.connect)
        }
        statusView.text = when (current.state) {
            HeartRateMonitor.State.CONNECTING -> getString(R.string.connecting)
            HeartRateMonitor.State.CONNECTED ->
                getString(R.string.readings, current.samples.size)
            HeartRateMonitor.State.RECONNECTING -> getString(R.string.reconnecting)
            HeartRateMonitor.State.ERROR -> current.detail ?: getString(R.string.failed)
            else -> getString(R.string.idle)
        }
    }

    companion object {
        private const val PERMISSION_REQUEST = 1
    }
}
