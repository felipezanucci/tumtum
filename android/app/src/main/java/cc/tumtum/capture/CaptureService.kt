package cc.tumtum.capture

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Holds a capture alive for a whole event.
 *
 * This is the reason the app exists. A browser cannot keep a Bluetooth
 * connection with the screen off — Android freezes the page and the connection
 * goes with it — which is why the web version has to burn the screen for six
 * hours. A foreground service has no such limit: the phone goes in a pocket,
 * dark and asleep, and the readings keep arriving.
 *
 * Nothing is captured yet. This is the shell, proven to start and stay up,
 * before any Bluetooth is put inside it.
 */
class CaptureService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())
        // If Android kills us for memory, come back — a capture that quietly
        // stopped halfway through a night is the failure we are here to avoid.
        return START_STICKY
    }

    private fun buildNotification(): Notification {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                getString(R.string.capture_channel_name),
                // Low: this notification is a status light, not an interruption.
                NotificationManager.IMPORTANCE_LOW,
            )
        )

        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.capture_notification_title))
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "captura"
        private const val NOTIFICATION_ID = 1
    }
}
