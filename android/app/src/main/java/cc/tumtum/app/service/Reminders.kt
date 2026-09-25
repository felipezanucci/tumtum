package cc.tumtum.app.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import cc.tumtum.app.AppContainer
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.flow.first

/**
 * The two reminders the product allows itself (docs/app-psychology-principles.md
 * §5.4 and §5.7): "Sua noite abriu" when a locked night unlocks, and "Bota o
 * relógio" an hour before the event the person marked. Nothing else — no
 * streak, no guilt, no re-engagement.
 *
 * AlarmManager, inexact but allowed while idle: no new dependency, no exact-
 * alarm permission, delivered within minutes of the time. Alarms do not
 * survive a reboot or an app update, so [rescheduleAll] runs on both.
 */
object Reminders {
    const val EXTRA_KIND = "kind"
    const val EXTRA_NIGHT_ID = "nightId"
    const val EXTRA_TITLE = "title"
    const val EXTRA_TEXT = "text"
    const val KIND_REVEAL = "reveal"
    const val KIND_EVENT = "event"

    private const val REQUEST_EVENT = 7001
    private const val REQUEST_REVEAL_BASE = 8000

    /** One hour before the event: enough to put the watch on, not enough to forget. */
    fun eventReminderAt(startAt: Instant): Instant = startAt.minus(Duration.ofHours(1))

    fun scheduleReveal(context: Context, nightId: Long, revealAt: Instant, title: String, text: String) {
        schedule(context, REQUEST_REVEAL_BASE + (nightId % 1000).toInt(), revealAt, KIND_REVEAL, nightId, title, text)
    }

    fun scheduleEvent(context: Context, startAt: Instant, title: String, text: String) {
        schedule(context, REQUEST_EVENT, eventReminderAt(startAt), KIND_EVENT, null, title, text)
    }

    fun cancelEvent(context: Context) {
        alarmManager(context).cancel(pending(context, REQUEST_EVENT, Intent(context, ReminderReceiver::class.java)))
    }

    fun cancelReveal(context: Context, nightId: Long) {
        alarmManager(context).cancel(
            pending(context, REQUEST_REVEAL_BASE + (nightId % 1000).toInt(), Intent(context, ReminderReceiver::class.java)),
        )
    }

    /**
     * After a reboot, an update or an account change: every reminder still in
     * the future is set again from what the phone knows — **for the viewing
     * account only** (25/09). Four "Sua noite abriu." arrived at 10h01, two of
     * them for nights deleted an hour before; a sealed night of another
     * account is not this person's to be told about either.
     */
    suspend fun rescheduleAll(context: Context, container: AppContainer) {
        val now = Instant.now()
        val state = container.prefs.state.first()
        val up = state.upcoming
        if (up != null && eventReminderAt(up.startAt).isAfter(now)) {
            scheduleEvent(
                context, up.startAt,
                context.getString(cc.tumtum.app.R.string.remind_event_title, up.name),
                context.getString(cc.tumtum.app.R.string.remind_event_text),
            )
        } else {
            cancelEvent(context)
        }
        container.db.nightDao().lockedAfter(now.toEpochMilli()).forEach { night ->
            if (night.ownerUserId != null && night.ownerUserId != state.viewerId) {
                cancelReveal(context, night.id)
                return@forEach
            }
            scheduleReveal(
                context, night.id, Instant.ofEpochMilli(night.revealAt ?: return@forEach),
                context.getString(cc.tumtum.app.R.string.remind_reveal_title),
                context.getString(cc.tumtum.app.R.string.remind_reveal_text, night.eventName),
            )
        }
    }

    private fun schedule(
        context: Context,
        requestCode: Int,
        at: Instant,
        kind: String,
        nightId: Long?,
        title: String,
        text: String,
    ) {
        if (!at.isAfter(Instant.now())) return
        val intent = Intent(context, ReminderReceiver::class.java)
            .putExtra(EXTRA_KIND, kind)
            .putExtra(EXTRA_TITLE, title)
            .putExtra(EXTRA_TEXT, text)
        if (nightId != null) intent.putExtra(EXTRA_NIGHT_ID, nightId)
        alarmManager(context).setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            at.toEpochMilli(),
            pending(context, requestCode, intent),
        )
    }

    private fun pending(context: Context, requestCode: Int, intent: Intent): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun alarmManager(context: Context): AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
}
