package cc.tumtum.capture

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.SystemClock

/**
 * Holds a capture alive for a whole event.
 *
 * This is the reason the app exists. A browser cannot keep a Bluetooth
 * connection with the screen off — Android freezes the page and the connection
 * goes with it — which is why the web version has to burn the screen for six
 * hours and ask whoever is wearing the strap to nurse a phone through a show.
 * A foreground service has no such limit: the phone goes in a pocket, dark and
 * asleep, and the readings keep arriving.
 *
 * Samples are held here rather than in the Activity, because the Activity is
 * destroyed and recreated for something as ordinary as a rotation, and a night
 * cannot depend on a screen staying alive.
 */
class CaptureService : Service() {

    data class Sample(val elapsedMs: Long, val bpm: Int)

    private val binder = LocalBinder()
    private var monitor: HeartRateMonitor? = null

    /** Wall clock at the first reading; offsets are measured from there. */
    private var startedAtMillis: Long = 0
    private var startedAtElapsed: Long = 0

    val samples = mutableListOf<Sample>()
    var lastBpm: Int? = null
        private set
    var state: HeartRateMonitor.State = HeartRateMonitor.State.IDLE
        private set
    var detail: String? = null
        private set

    /** Set by whoever is showing the capture, so the screen can follow it. */
    var listener: (() -> Unit)? = null

    inner class LocalBinder : Binder() {
        val service: CaptureService get() = this@CaptureService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification(null))
        // If Android kills us for memory, come back. A capture that quietly
        // stopped halfway through a night is the failure this app exists to
        // avoid, and the whole point of holding the samples in a service.
        return START_STICKY
    }

    fun startCapture(device: BluetoothDevice) {
        if (monitor != null) return
        monitor = HeartRateMonitor(
            context = applicationContext,
            onReading = { reading ->
                if (startedAtMillis == 0L) {
                    startedAtMillis = System.currentTimeMillis()
                    startedAtElapsed = SystemClock.elapsedRealtime()
                }
                // Elapsed real time, not wall clock: it does not jump when the
                // network corrects the phone's clock mid-capture, which would
                // otherwise fold two readings onto one timestamp.
                samples.add(Sample(SystemClock.elapsedRealtime() - startedAtElapsed, reading.bpm))
                lastBpm = reading.bpm
                notifyWatcher()
            },
            onState = { next, why ->
                state = next
                detail = why
                notifyWatcher()
            },
        ).also { it.connect(device) }
    }

    fun stopCapture() {
        monitor?.stop()
        monitor = null
    }

    /** Wall-clock instant of the first reading, or null before one arrives. */
    fun firstReadingAt(): Long? = startedAtMillis.takeIf { it != 0L }

    private fun notifyWatcher() {
        listener?.invoke()
        getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, buildNotification(lastBpm))
    }

    private fun buildNotification(bpm: Int?): Notification {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                getString(R.string.capture_channel_name),
                // Low: this is a status light, not an interruption. It will sit
                // in the shade for six hours.
                NotificationManager.IMPORTANCE_LOW,
            )
        )

        val open = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )

        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.capture_notification_title))
            .setContentText(
                if (bpm != null) "$bpm bpm · ${samples.size} leituras"
                else getString(R.string.capture_notification_waiting)
            )
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentIntent(open)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        stopCapture()
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "captura"
        private const val NOTIFICATION_ID = 1
    }
}
