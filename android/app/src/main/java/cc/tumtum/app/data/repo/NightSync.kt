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

            var serverId = night.serverSessionId
            if (serverId == null) {
                val samples = db.nightDao().samplesOf(nightId)
                    .map { HrSample(Instant.ofEpochMilli(it.time), it.bpm) }
                serverId = api.createSession(
                    startAt = Instant.ofEpochMilli(night.startAt),
                    endAt = Instant.ofEpochMilli(night.endAt),
                    sourceDevice = night.sourceLabel,
                    samples = samples,
                )
                db.nightDao().setServerSessionId(nightId, serverId)
                db.nightDao().setUploadState(nightId, "SENT", null)
            }

            val moments = api.analyze(serverId)
            val top = moments.maxOfOrNull { it.bpm }
            db.nightDao().replaceMoments(
                nightId,
                moments.map {
                    MomentEntity(
                        nightId = nightId,
                        bpm = it.bpm,
                        at = it.at.toEpochMilli(),
                        durationSec = it.durationSec,
                        isPeak = it.bpm == top,
                        label = it.label,
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
        }
    }

    companion object {
        const val ERR_NO_SESSION = "no_session"
        const val ERR_EXPIRED = "expired"
        const val ERR_OFFLINE = "offline"
    }
}
