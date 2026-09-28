package cc.tumtum.app.data.repo

import cc.tumtum.app.AppContainer
import cc.tumtum.app.domain.ConsentText
import cc.tumtum.app.domain.StopReason
import cc.tumtum.app.service.CaptureBus
import cc.tumtum.app.service.CaptureService
import cc.tumtum.app.service.ForcedStop
import java.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * No capture without "Ler sua batida" (28/09, item 21 — critical).
 *
 * Until this day the app sent people to the consent screen only when no
 * sensor or watch was set up, so a phone with a Polar already paired started
 * recording on a brand-new account that had never granted `read_heart_rate`.
 * Reading a heartbeat is the processing the consent is *for*; recording one
 * without it is the one thing this app may not do.
 *
 * Whether it is granted: the phone's last answer for this account when that
 * answer is yes — a venue has no signal, and a yes heard before the show is
 * still a yes. Otherwise the server is asked now (a grant made on the web
 * since reaches the phone here), and without an answer the phone's own last
 * word stands. Null when neither can say: then nothing records either, and
 * the screen says why.
 */
suspend fun AppContainer.readingGranted(): Boolean? {
    val cached = prefs.state.first().granted(ConsentText.READ_HEART_RATE)
    if (cached == true) return true
    return runCatching { api.getConsents().granted(ConsentText.READ_HEART_RATE) }.getOrNull() ?: cached
}

private val stopping = Mutex()

/**
 * "Ler sua batida" went off while a night was recording (28/09): the capture
 * ends there. The strap stops; the night is saved on the phone with what was
 * written while the consent stood — the watch is not read again — and it
 * carries [StopReason.READING_REVOKED], so its screen says why it is short.
 * [CaptureBus.forcedStop] tells the screens that were showing the capture.
 *
 * Nothing happens without an open event. Safe to call twice: the second
 * finds no event.
 */
suspend fun AppContainer.stopCaptureForReadingRevoked(): Unit = stopping.withLock {
    val event = nights.activeEvent.first() ?: return@withLock
    CaptureService.stop(appContext)
    val endedAt = Instant.now()
    nights.closeEvent(event.id, endedAt)
    val closed = event.copy(endAt = endedAt)
    val measurement = nights.measureSources(closed, endedAt, readHealthConnect = false)
    val source = measurement.sources.firstOrNull { it.hasData }?.packageName
    val nightId = source?.let { saveEndedNight(closed, measurement, it, stopReason = StopReason.READING_REVOKED) }
    if (nightId == null) endNight.clear()
    CaptureBus.forcedStop.value = ForcedStop(StopReason.READING_REVOKED, nightId, System.currentTimeMillis())
}
