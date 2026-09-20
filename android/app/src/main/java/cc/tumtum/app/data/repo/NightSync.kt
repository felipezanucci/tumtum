package cc.tumtum.app.data.repo

import cc.tumtum.app.data.api.TumtumApi
import cc.tumtum.app.data.db.MomentEntity
import cc.tumtum.app.data.db.TumTumDatabase
import cc.tumtum.app.data.prefs.UserPrefs
import cc.tumtum.app.domain.HrSample
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import java.time.Instant
import java.time.ZoneId

/**
 * The night goes up, and the server's moments come back — Etapa 2 of
 * docs/one-app-plan.md, 2026-09-18.
 *
 * The phone stays the source of truth for the readings: a night is saved in
 * Room first, and only then offered to the server. Upload failure costs
 * nothing but a retry — on the next app start, on opening the night, or by
 * the button on the reveal. Success replaces the phone's top-N moments with
 * the detector's, and the night records which it holds, so the reveal never
 * shows one while claiming the other.
 *
 * States, as the night carries them: PENDING (never sent, or no account yet),
 * SENT (readings on the server, analysis not yet back), ANALYSED (done),
 * FAILED (the last attempt did not go through, and `uploadError` says why in
 * a key the screen translates).
 */
/** The two real steps of a sync, for a screen to show the work as it happens (§5.2). */
enum class SyncPhase { SENDING, ANALYSING }

class NightSync(
    private val db: TumTumDatabase,
    private val api: TumtumApi,
    private val prefs: UserPrefs,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val gate = Mutex()
    private val inFlight = MutableStateFlow<Set<Long>>(emptySet())

    /** Nights being uploaded right now, for a screen to say "enviando…" truthfully. */
    val uploading: StateFlow<Set<Long>> = inFlight
    private val phases = MutableStateFlow<Map<Long, SyncPhase>>(emptyMap())
    /** Which step each in-flight night is on — never a step that did not run. */
    val phase: StateFlow<Map<Long, SyncPhase>> = phases

    fun uploadLater(nightId: Long) {
        scope.launch { upload(nightId) }
    }

    fun retryPendingLater() {
        scope.launch { retryPending() }
    }

    suspend fun retryPending() {
        db.nightDao().pendingUpload().forEach { upload(it.id) }
    }

    suspend fun upload(nightId: Long) {
        val claimed = gate.withLock {
            if (nightId in inFlight.value) false else { inFlight.update { it + nightId }; true }
        }
        if (!claimed) return
        try {
            val session = prefs.state.first().session
            if (session == null) {
                db.nightDao().setUploadState(nightId, "PENDING", ERR_NO_SESSION)
                return
            }
            if (!session.isLive(System.currentTimeMillis())) {
                db.nightDao().setUploadState(nightId, "FAILED", ERR_EXPIRED)
                return
            }
            val night = db.nightDao().nightRow(nightId) ?: return

            // Etapa 3: the event exists on the server before the night does,
            // and every mark tapped during the capture becomes a timeline
            // entry there — the correlator names moments from it. Marks are
            // pushed on every attempt, so a late one still lands before the
            // next analysis.
            val serverEventId = ensureServerEvent(night.eventId)
            if (serverEventId != null) pushMarks(night.eventId, serverEventId)

            var serverId = night.serverSessionId
            if (serverId == null) {
                phases.update { it + (nightId to SyncPhase.SENDING) }
                val samples = db.nightDao().samplesOf(nightId)
                    .map { HrSample(Instant.ofEpochMilli(it.time), it.bpm) }
                serverId = api.createSession(
                    startAt = Instant.ofEpochMilli(night.startAt),
                    endAt = Instant.ofEpochMilli(night.endAt),
                    sourceDevice = night.sourceLabel,
                    samples = samples,
                    serverEventId = serverEventId,
                )
                db.nightDao().setServerSessionId(nightId, serverId)
                db.nightDao().setUploadState(nightId, "SENT", null)
            }

            phases.update { it + (nightId to SyncPhase.ANALYSING) }
            val moments = api.analyze(serverId)
            val top = moments.maxOfOrNull { it.bpm }
            // A label the person typed (§5.6) outlives the server's answer when the
            // server has none for the same moment (within a minute).
            val previous = db.nightDao().momentsOf(nightId).filter { it.label != null }
            db.nightDao().replaceMoments(
                nightId,
                moments.map {
                    MomentEntity(
                        nightId = nightId,
                        bpm = it.bpm,
                        at = it.at.toEpochMilli(),
                        durationSec = it.durationSec,
                        isPeak = it.bpm == top,
                        label = it.label ?: previous.firstOrNull { p -> kotlin.math.abs(p.at - it.at.toEpochMilli()) <= 60_000 }?.label,
                    )
                },
            )
            db.nightDao().setUploadState(nightId, "ANALYSED", null)
        } catch (e: TumtumApi.ApiException) {
            val reason = if (e.code == 401) ERR_EXPIRED else "server:${e.code} ${e.detail}"
            db.nightDao().setUploadState(nightId, "FAILED", reason)
        } catch (e: IOException) {
            db.nightDao().setUploadState(nightId, "FAILED", ERR_OFFLINE)
        } catch (e: Exception) {
            db.nightDao().setUploadState(nightId, "FAILED", "error:${e.javaClass.simpleName}")
        } finally {
            inFlight.update { it - nightId }
            phases.update { it - nightId }
        }
    }

    /** The local event's server twin: the one it was picked from, or one created now from its name, venue, date and kind. */
    private suspend fun ensureServerEvent(localEventId: Long): String? {
        val event = db.eventDao().byId(localEventId) ?: return null
        event.serverEventId?.let { return it }
        val date = Instant.ofEpochMilli(event.startAt).atZone(ZoneId.systemDefault()).toLocalDate()
        val id = api.createEvent(event.name, event.venue.ifBlank { null }, date, event.eventType)
        db.eventDao().setServerEventId(localEventId, id)
        return id
    }

    private suspend fun pushMarks(localEventId: Long, serverEventId: String) {
        for (mark in db.markDao().unsyncedFor(localEventId)) {
            api.addTimelineEntry(serverEventId, Instant.ofEpochMilli(mark.at), mark.label, mark.entryType)
            db.markDao().markSynced(mark.id)
        }
    }

    companion object {
        const val ERR_NO_SESSION = "no_session"
        const val ERR_EXPIRED = "expired"
        const val ERR_OFFLINE = "offline"
    }
}
