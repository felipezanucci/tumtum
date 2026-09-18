package cc.tumtum.app.data.repo

import cc.tumtum.app.AppContainer
import cc.tumtum.app.domain.EventSession
import cc.tumtum.app.domain.RevealLock
import kotlinx.coroutines.flow.first

/**
 * The end of a night, once a source is chosen: save on the phone, offer to the
 * server, forget the transient state. One place for it, because two screens
 * finish a night — the capture itself when only one source has data (the
 * strap, in the pilot), and "Trazer do meu relógio" when there is a choice.
 * Returns the night id, or null when the source had no sample at all.
 */
suspend fun AppContainer.saveEndedNight(event: EventSession, measurement: SourceMeasurement, sourcePackage: String): Long? {
    // Trava da revela (protocolo): com o modo ligado, a noite só abre às 10h
    // da manhã seguinte — o cartão cego vem antes.
    val revealAt = if (prefs.state.first().revealLockEnabled) RevealLock.revealAt(measurement.windowEnd) else null
    val nightId = nights.saveNight(event, measurement, sourcePackage, revealAt) ?: return null
    // Etapa 2: saved on the phone first, then offered to the server. A failure costs a retry, never the night.
    sync.uploadLater(nightId)
    endNight.clear()
    return nightId
}
