package cc.tumtum.app.data.repo

import cc.tumtum.app.data.api.TumtumApi
import cc.tumtum.app.data.db.MomentEntity
import cc.tumtum.app.data.db.TumTumDatabase
import cc.tumtum.app.data.prefs.UserPrefs
import cc.tumtum.app.domain.ConsentText
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
 * Room first, and **goes to the server only when its owner asks** (26/09):
 * the reveal's "Guardar minha noite na TumTum", with the `keep_night`
 * consent on ([requestSend]). A night nobody asked to send is never touched
 * here. Once asked, a failure costs nothing but a retry — on the next app
 * start, or by the button on the reveal. Success replaces the phone's top-N moments with
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

/** What asking to keep a night came to, before anything was sent (26/09). */
sealed interface SendRequest {
    /** Asked and started: the night is flagged and the upload is under way. */
    data object Started : SendRequest

    /** The consent to keep nights is off: the screen opens it, focused on [purpose]. */
    data class NeedsConsent(val purpose: String) : SendRequest

    data object SignedOut : SendRequest

    data object Offline : SendRequest

    /** The server answered something else; [detail] is its own sentence. */
    data class Failed(val detail: String) : SendRequest
}

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

    /**
     * The tap on "Guardar minha noite na TumTum" (26/09). The consent is asked
     * of the server first — never assumed from an old answer — and only with
     * `keep_night` granted is the night flagged and sent. Without it nothing
     * is flagged, so no retry will ever send a night whose owner did not agree.
     *
     * With [start] false the caller runs [upload] itself (a screen that waits
     * for the result); otherwise it runs here, in the background.
     */
    suspend fun requestSend(nightId: Long, start: Boolean = true): SendRequest {
        val session = prefs.state.first().session
        if (session == null || !session.isLive(System.currentTimeMillis())) return SendRequest.SignedOut
        val consents = try {
            api.getConsents()
        } catch (e: TumtumApi.ApiException) {
            return if (e.code == 401) SendRequest.SignedOut else SendRequest.Failed(e.detail)
        } catch (e: IOException) {
            return SendRequest.Offline
        }
        if (!consents.granted(ConsentText.KEEP_NIGHT)) return SendRequest.NeedsConsent(ConsentText.KEEP_NIGHT)
        db.nightDao().setSendRequested(nightId, true)
        if (start) uploadLater(nightId)
        return SendRequest.Started
    }

    /** The consent screen just recorded `keep_night` for this night's sake: flag it and send it. */
    fun sendAfterConsent(nightId: Long) {
        scope.launch {
            db.nightDao().setSendRequested(nightId, true)
            upload(nightId)
        }
    }

    /**
     * A mark goes up on its own, the moment it is tapped (22/09).
     *
     * Until then `pushMarks` ran only inside [upload], as a side effect of a
     * night reaching the server — and on 22/09 that cost a whole match: the
     * capture had no readings, so no night was ever created, so the operator's
     * APITO, 2º TEMPO and two GOL stayed on the phone. **The anchors are the
     * event's truth, not one person's capture.** If the operator's strap drops
     * or their battery dies, every fan at that match loses the two taps that
     * turn API-Football's minutes into real times — which is exactly the
     * situation the taps exist for.
     *
     * Failure costs nothing: the mark stays unsynced and [retryPending] finds
     * it again through `eventsWithUnsynced`, with no night involved.
     */
    fun pushMarksLater(localEventId: Long) {
        scope.launch { runCatching { pushMarksFor(localEventId) } }
    }

    private suspend fun pushMarksFor(localEventId: Long) {
        if (db.markDao().unsyncedFor(localEventId).isEmpty()) return
        val session = prefs.state.first().session ?: return
        if (!session.isLive(System.currentTimeMillis())) return
        val serverEventId = ensureServerEvent(localEventId) ?: return
        pushMarks(localEventId, serverEventId)
    }

    fun retryPendingLater() {
        scope.launch { retryPending() }
    }

    /**
     * The capture screen's "Tentar de novo" for an event the server did not
     * take (25/09): registers it now and answers why not when it cannot —
     * null on success, or [ERR_NO_SESSION], [ERR_NOT_OPERATOR], [ERR_OFFLINE]
     * or the server's own sentence.
     */
    suspend fun registerEventNow(localEventId: Long): String? {
        val session = prefs.state.first().session
        if (session == null || !session.isLive(System.currentTimeMillis())) return ERR_NO_SESSION
        return try {
            ensureServerEvent(localEventId)
            pushMarks(localEventId, db.eventDao().byId(localEventId)?.serverEventId ?: return null)
            null
        } catch (e: TumtumApi.ApiException) {
            when (e.code) {
                401 -> ERR_NO_SESSION
                403 -> ERR_NOT_OPERATOR
                else -> e.detail
            }
        } catch (e: IOException) {
            ERR_OFFLINE
        }
    }

    suspend fun retryPending() {
        db.nightDao().pendingUpload().forEach { upload(it.id) }
        // Marks no night will ever carry — see pushMarksLater.
        db.markDao().eventsWithUnsynced().forEach { runCatching { pushMarksFor(it) } }
    }

    /**
     * Runs [block], answering null when the server **refused** it — and
     * rethrowing anything else, so a failure that a retry would fix stays a
     * failure. See the call sites in [upload]: 403 is a statement about who
     * this account is, which no retry changes.
     */
    private suspend fun <T> withoutRefusal(block: suspend () -> T): T? =
        try {
            block()
        } catch (e: TumtumApi.ApiException) {
            if (e.code == 403) null else throw e
        }

    suspend fun upload(nightId: Long) {
        val claimed = gate.withLock {
            if (nightId in inFlight.value) false else { inFlight.update { it + nightId }; true }
        }
        if (!claimed) return
        // The first step is lit from the first byte: the event, the marks and
        // the readings are all "sending", and a screen watching this must never
        // see a night in flight with no step on.
        phases.update { it + (nightId to SyncPhase.SENDING) }
        try {
            val session = prefs.state.first().session
            val night = db.nightDao().nightRow(nightId) ?: return
            // Only a night its owner asked to keep ever leaves the phone (26/09).
            if (!night.sendRequested) return
            // Another account's night waits for that account (25/09): sent
            // under this one, it would become this person's on the server.
            if (night.ownerUserId != null && session?.userId != null && night.ownerUserId != session.userId) return
            if (session == null) {
                db.nightDao().setUploadState(nightId, "PENDING", ERR_NO_SESSION)
                return
            }
            if (!session.isLive(System.currentTimeMillis())) {
                db.nightDao().setUploadState(nightId, "FAILED", ERR_EXPIRED)
                return
            }

            // Etapa 3: the event exists on the server before the night does,
            // and every mark tapped during the capture becomes a timeline
            // entry there — the correlator names moments from it. Marks are
            // pushed on every attempt, so a late one still lands before the
            // next analysis.
            //
            // A **refusal** here is survivable, and only a refusal (item 47,
            // 22/09). The event and its timeline are TumTum's side of the
            // night; the readings are the person's. Both are operator-only on
            // the server, so an account that does not operate the platform
            // gets a 403 — and that must never cost somebody their own night.
            //
            // Nothing else is swallowed, and the difference matters: a night
            // that uploads while the event could not be created is linked to
            // no event **for good**, because `serverSessionId` is stored and
            // never recreated. Letting a network blip through that door would
            // cost the night its names permanently, where failing the upload
            // costs only a retry.
            val serverEventId = withoutRefusal { ensureServerEvent(night.eventId) }
            if (serverEventId != null) withoutRefusal { pushMarks(night.eventId, serverEventId) }

            var serverId = night.serverSessionId
            if (serverId == null) {
                val samples = db.nightDao().samplesOf(nightId)
                    .map { HrSample(Instant.ofEpochMilli(it.time), it.bpm) }
                serverId = api.createSession(
                    startAt = Instant.ofEpochMilli(night.startAt),
                    endAt = Instant.ofEpochMilli(night.endAt),
                    sourceDevice = night.sourceLabel,
                    samples = samples,
                    serverEventId = serverEventId,
                )
                db.nightDao().setServerSessionId(nightId, serverId, session.userId, System.currentTimeMillis())
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
        } catch (e: TumtumApi.ConsentRequired) {
            // The consent was revoked between the tap and the upload (26/09).
            // The ask is withdrawn so no retry sends it silently; the reveal
            // says what is missing and opens the consent screen on it.
            db.nightDao().setSendRequested(nightId, false)
            db.nightDao().setUploadState(nightId, "FAILED", ERR_CONSENT_PREFIX + e.purpose)
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

    /**
     * The local event's server twin: the one it was picked from, or one created
     * now from its name, venue, date, kind — and, since 21/09, its start and
     * end, so the fans' list can count down to it and ask a watch over it.
     */
    private suspend fun ensureServerEvent(localEventId: Long): String? {
        val event = db.eventDao().byId(localEventId) ?: return null
        event.serverEventId?.let { return it }
        val zone = ZoneId.systemDefault()
        val start = Instant.ofEpochMilli(event.startAt).atZone(zone)
        val end = event.endAt?.let { Instant.ofEpochMilli(it).atZone(zone) }
        val id = api.createEvent(
            event.name, event.venue.ifBlank { null }, start.toLocalDate(), event.eventType,
            startTime = start.toLocalTime(), endTime = end?.toLocalTime(),
        )
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
        const val ERR_NOT_OPERATOR = "not_operator"

        /** `consent:<purpose>` — the server refused for a missing consent. */
        const val ERR_CONSENT_PREFIX = "consent:"

        /** The purpose a failed upload is waiting on, or null. */
        fun consentMissing(uploadError: String?): String? =
            uploadError?.takeIf { it.startsWith(ERR_CONSENT_PREFIX) }?.removePrefix(ERR_CONSENT_PREFIX)?.ifBlank { null }
    }
}
