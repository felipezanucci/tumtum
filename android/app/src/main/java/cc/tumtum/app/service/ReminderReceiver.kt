package cc.tumtum.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import cc.tumtum.app.MainActivity
import cc.tumtum.app.R
import cc.tumtum.app.TumTumApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Posts the one notification a reminder is; tapping it opens the night, or the AO VIVO tab. */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val kind = intent.getStringExtra(Reminders.EXTRA_KIND) ?: return
        val title = intent.getStringExtra(Reminders.EXTRA_TITLE) ?: return
        val text = intent.getStringExtra(Reminders.EXTRA_TEXT) ?: ""
        val nightId = if (intent.hasExtra(Reminders.EXTRA_NIGHT_ID)) intent.getLongExtra(Reminders.EXTRA_NIGHT_ID, -1) else null

        // Without the permission the post is silently dropped by Android; the
        // screens that schedule a reminder say so beforehand.
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return

        val app = context.applicationContext as? TumTumApp
        if (nightId == null || app == null) {
            post(context, kind, title, text, nightId)
            return
        }
        // "Sua noite abriu." only for a night that is still here and is the
        // viewing account's (25/09): four arrived at 10h01, two for nights
        // deleted an hour earlier, and tapping one opened a blank screen.
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val night = app.container.db.nightDao().nightRow(nightId)
                val viewer = app.container.prefs.state.first().viewerId
                if (night != null && (night.ownerUserId == null || night.ownerUserId == viewer)) {
                    post(context, kind, title, text, nightId)
                }
            } finally {
                pending.finish()
            }
        }
    }

    private fun post(context: Context, kind: String, title: String, text: String, nightId: Long?) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, context.getString(R.string.remind_channel), NotificationManager.IMPORTANCE_DEFAULT),
        )
        val open = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        if (nightId != null && nightId >= 0) open.putExtra(MainActivity.EXTRA_OPEN_NIGHT_ID, nightId)
        if (kind == Reminders.KIND_EVENT) open.putExtra(MainActivity.EXTRA_OPEN_LIVE, true)
        val contentIntent = PendingIntent.getActivity(
            context,
            (nightId ?: 0L).toInt(),
            open,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(contentIntent)
            .build()
        runCatching { manager.notify(NOTIFICATION_BASE + (nightId ?: 0L).toInt(), notification) }
    }

    companion object {
        private const val CHANNEL_ID = "lembretes"
        private const val NOTIFICATION_BASE = 2000
    }
}
