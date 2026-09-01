package cc.tumtum.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cc.tumtum.app.TumTumApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Reiniciar o aparelho no meio da sessão → sessão retomada (§4.3). */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val app = context.applicationContext as? TumTumApp ?: return
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val state = app.container.prefs.state.first()
                val eventId = state.activeCaptureEventId
                val address = state.bleAddress
                if (eventId != null && address != null) {
                    val event = app.container.db.eventDao().byId(eventId)
                    if (event != null && event.endAt == null) {
                        CaptureService.start(context, eventId, address, restartReason = "BOOT_RESTART")
                    }
                }
            } finally {
                pending.finish()
            }
        }
    }
}
