package cc.tumtum.app.data.api

import cc.tumtum.app.data.prefs.Session
import cc.tumtum.app.data.prefs.UserPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Everything this app asks of the backend.
 *
 * HttpURLConnection and org.json, on purpose: both ship with Android, so the
 * wire carries no dependency to get wrong while this is built without a
 * device to compile against. Ported from the capture app on 2026-09-18 —
 * Etapa 1 of docs/one-app-plan.md, the first byte this app ever sent to a
 * server. Every call is a suspend function that runs on IO; the screens own
 * what to say, this owns the wire format.
 *
 * The token lives in [UserPrefs] with the rest of the account, so the
 * screens see "signed in" and "signed out" through the same state flow as
 * everything else.
 */
class TumtumApi(private val prefs: UserPrefs) {

    private val renewLock = Mutex()

    /** The server refused or could not do what was asked; [detail] is its own sentence. */
    class ApiException(val code: Int, val detail: String) : IOException(detail)

    /** Who the token says we are, as the server describes it. */
    data class Me(val id: String, val email: String, val name: String)

    // --- Auth ---

    /** Creates the account and signs in: the server answers with a token. */
    suspend fun register(email: String, name: String, password: String): Session {
        val body = JSONObject().put("email", email).put("name", name).put("password", password)
        val response = JSONObject(request("POST", "/api/auth/register", body.toString(), token = null))
        return storeSession(response.getString("access_token"), response.optRefresh())
    }

    suspend fun login(email: String, password: String): Session {
        val body = JSONObject().put("email", email).put("password", password)
        val response = JSONObject(request("POST", "/api/auth/login", body.toString(), token = null))
        return storeSession(response.getString("access_token"), response.optRefresh())
    }

    suspend fun me(): Me {
        val json = JSONObject(request("GET", "/api/auth/me", null, token = requireToken()))
        return Me(id = json.getString("id"), email = json.getString("email"), name = json.getString("name"))
    }

    /**
     * "Sair" means out (#34): the server revokes this phone's refresh chain,
     * then the phone forgets it. Offline, the phone still forgets — the chain
     * then simply dies unused after 90 days.
     */
    suspend fun signOut() {
        prefs.state.first().session?.refreshToken?.let { refresh ->
            runCatching {
                request("POST", "/api/auth/logout", JSONObject().put("refresh_token", refresh).toString(), token = null)
            }
        }
        prefs.clearSession()
    }

    /**
     * Deletes the account on the server — readings, moments, cards, all of
     * it — then forgets the token. Throws when the server did not do it, so
     * the caller never wipes the phone believing the server followed.
     */
    suspend fun deleteAccount() {
        request("DELETE", "/api/users/me", null, token = requireToken())
        prefs.clearSession()
    }

    // --- Nights (Etapa 2) ---

    // --- Events (Etapa 3) ---

    /** The events somebody could be standing in. Public on the server, so this works without a token. */
    suspend fun listEvents(): List<ServerEvent> =
        ServerEvents.parse(request("GET", "/api/events", null, token = null))

    /**
     * Creates the event on the server; answers with its id. Date and times are
     * the event's own wall clock (21/09): the server stores the digits and the
     * app reads the digits back — the offset on the wire is the column's, not
     * the event's, and neither side reads it.
     */
    suspend fun createEvent(
        name: String,
        venue: String?,
        date: java.time.LocalDate,
        eventType: String,
        startTime: java.time.LocalTime? = null,
        endTime: java.time.LocalTime? = null,
    ): String {
        val body = JSONObject()
            .put("name", name)
            .put("date", date.toString())
            .put("event_type", eventType)
        if (!venue.isNullOrBlank()) body.put("venue", venue)
        startTime?.let { body.put("start_time", TIME_FMT.format(it)) }
        endTime?.let { body.put("end_time", TIME_FMT.format(it)) }
        val response = JSONObject(request("POST", "/api/events", body.toString(), token = requireToken()))
        return response.getString("id")
    }

    /** One tap on the capture screen becomes one timeline entry — the thing that names a moment. */
    suspend fun addTimelineEntry(serverEventId: String, at: java.time.Instant, label: String, entryType: String) {
        val body = JSONObject()
            .put("timestamp", SessionPayload.iso(at))
            .put("label", label)
            .put("entry_type", entryType)
        request("POST", "/api/events/$serverEventId/timeline", body.toString(), token = requireToken())
    }

    /** Uploads a night's readings; the server answers with the session id it gave them. */
    suspend fun createSession(
        startAt: java.time.Instant,
        endAt: java.time.Instant,
        sourceDevice: String,
        samples: List<cc.tumtum.app.domain.HrSample>,
        serverEventId: String? = null,
    ): String {
        val body = SessionPayload.build(startAt, endAt, sourceDevice, samples, serverEventId)
        val response = JSONObject(request("POST", "/api/health/sessions", body.toString(), token = requireToken()))
        return response.getString("id")
    }

    // --- The event's feed (22/09) ---
    //
    // Per event, and only for the people who were at it. The server proves
    // that with an hr_sessions row and answers 403 otherwise; the repository
    // turns that one code into its own state, because "you were not there" and
    // "it did not load" are different things and the screen must not blur them.

    suspend fun eventFeed(serverEventId: String): ServerFeed =
        ServerFeed.parse(request("GET", "/api/events/$serverEventId/feed", null, token = requireToken()))

    suspend fun crowd(serverEventId: String): ServerCrowd =
        ServerCrowd.parse(request("GET", "/api/events/$serverEventId/crowd", null, token = requireToken()))

    /** Publishes one moment. Never called except from an explicit tap — it is health data. */
    suspend fun postMoment(
        serverEventId: String,
        serverSessionId: String,
        bpm: Int,
        at: java.time.Instant,
        label: String?,
        quote: String?,
        skin: String,
        toSeries: Boolean = false,
    ) {
        val body = JSONObject()
            .put("session_id", serverSessionId)
            .put("to_series", toSeries)
            .put("bpm", bpm)
            .put("moment_at", SessionPayload.iso(at))
            .put("skin", skin)
        if (!label.isNullOrBlank()) body.put("label", label)
        if (!quote.isNullOrBlank()) body.put("quote", quote)
        request("POST", "/api/events/$serverEventId/feed", body.toString(), token = requireToken())
    }

    /** Takes it down. The consent to publish is only real while this works. */
    suspend fun deletePost(serverEventId: String, postId: String) {
        request("DELETE", "/api/events/$serverEventId/feed/$postId", null, token = requireToken())
    }

    /** SENTI TB, toggled. The server answers with the post as it now stands — count and all. */
    suspend fun toggleSenti(base: String, postId: String): ServerPost =
        ServerPost.parse(request("POST", "$base/feed/$postId/senti", "", token = requireToken()))

    /** The tour's feed (#33): every post shown to it, from every date. */
    suspend fun seriesFeed(seriesId: String): ServerSeriesFeed =
        ServerSeriesFeed.parse(request("GET", "/api/series/$seriesId/feed", null, token = requireToken()))

    /** Which tour, club or championship an event belongs to. Public, like the event. */
    suspend fun eventSeries(serverEventId: String): ServerSeries? =
        ServerSeries.parseOrNull(request("GET", "/api/events/$serverEventId/series", null, token = null))

    // --- Report and block (#36, 22/09) ---
    //
    // Both are made from a post, because the feed names nobody any other way.

    /** abuse · fake · other. */
    suspend fun reportPost(base: String, postId: String, reason: String) {
        request(
            "POST",
            "$base/feed/$postId/report",
            JSONObject().put("reason", reason).toString(),
            token = requireToken(),
        )
    }

    /** Stop seeing the person who posted this, and stop being seen by them. */
    suspend fun blockAuthor(base: String, postId: String) {
        request("POST", "$base/feed/$postId/block", "", token = requireToken())
    }

    data class BlockedPerson(val id: String, val name: String, val initials: String)

    suspend fun myBlocks(): List<BlockedPerson> {
        val array = org.json.JSONArray(request("GET", "/api/users/me/blocks", null, token = requireToken()))
        return (0 until array.length()).map { i ->
            val o = array.getJSONObject(i)
            BlockedPerson(id = o.getString("id"), name = o.optString("name", "Alguém"), initials = o.optString("initials", "TT"))
        }
    }

    suspend fun unblock(blockId: String) {
        request("DELETE", "/api/users/me/blocks/$blockId", null, token = requireToken())
    }

    /** Runs the detector on an uploaded night and returns its moments, named where the event has a timeline. */
    suspend fun analyze(serverSessionId: String): List<ServerMoment> =
        ServerMoments.parse(request("POST", "/api/experience/$serverSessionId/analyze", "", token = requireToken()))

    private fun JSONObject.optRefresh(): String? =
        if (isNull("refresh_token")) null else optString("refresh_token", "").ifBlank { null }

    private suspend fun storeSession(token: String, refreshToken: String?): Session {
        // The user id is in the token's `sub`; reading it here spares a round
        // trip and keeps the session self-describing when the network is gone.
        val userId = runCatching {
            val payload = String(
                java.util.Base64.getUrlDecoder().decode(token.split('.')[1]),
                Charsets.UTF_8,
            )
            JSONObject(payload).getString("sub")
        }.getOrNull()
        val session = Session(token = token, userId = userId, refreshToken = refreshToken)
        prefs.setSession(session)
        return session
    }

    /**
     * A token the server will accept, renewing it first when it is about to
     * expire (#34). Until 22/09 this threw "Sessão expirada" the moment the
     * 24-hour token ran out, and nothing could renew it.
     */
    private suspend fun requireToken(): String {
        val session = prefs.state.first().session ?: throw ApiException(401, "Sem sessão")
        if (!AccessToken.isExpired(session.token, System.currentTimeMillis() + RENEW_MARGIN_MS)) {
            return session.token
        }
        return renew() ?: throw ApiException(401, "Sessão expirada")
    }

    /**
     * Trades the refresh token for a new pair, **one caller at a time**.
     *
     * The server rotates on every use and treats a spent token presented again
     * as a stolen copy — revoking the whole chain. Two requests renewing at
     * once would do exactly that to ourselves, so renewals are serialised, and
     * the second caller finds the first one's fresh token instead of spending
     * the old one again.
     */
    private suspend fun renew(): String? = renewLock.withLock {
        val session = prefs.state.first().session ?: return@withLock null
        if (!AccessToken.isExpired(session.token, System.currentTimeMillis() + RENEW_MARGIN_MS)) {
            return@withLock session.token
        }
        val refresh = session.refreshToken ?: return@withLock null
        val response = try {
            JSONObject(
                request(
                    "POST",
                    "/api/auth/refresh",
                    JSONObject().put("refresh_token", refresh).toString(),
                    token = null,
                ),
            )
        } catch (e: ApiException) {
            // The server refused the chain: expired, revoked, or reused. Drop
            // it so every screen now says "expired", which is finally true.
            // Anything else (offline) propagates and the token is kept.
            if (e.code == 401) prefs.setSession(session.copy(refreshToken = null))
            return@withLock null
        }
        storeSession(response.getString("access_token"), response.optRefresh() ?: refresh).token
    }

    // --- Wire ---

    private suspend fun request(method: String, path: String, body: String?, token: String?): String =
        withContext(Dispatchers.IO) {
            val connection = URL(BASE_URL + path).openConnection() as HttpURLConnection
            try {
                connection.requestMethod = method
                connection.setRequestProperty("Accept", "application/json")
                if (token != null) connection.setRequestProperty("Authorization", "Bearer $token")
                if (body != null) {
                    connection.doOutput = true
                    connection.setRequestProperty("Content-Type", "application/json")
                }
                // A venue's cellular is slow, not absent. Give a request room
                // before declaring failure; the night upload (Etapa 2) will
                // need even more.
                connection.connectTimeout = 15_000
                connection.readTimeout = 120_000

                if (body != null) {
                    connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
                }

                val code = connection.responseCode
                val text = (if (code in 200..299) connection.inputStream else connection.errorStream)
                    ?.bufferedReader()?.readText().orEmpty()

                if (code !in 200..299) {
                    // FastAPI puts a sentence in `detail` for the errors it
                    // raises on purpose, and a list of field problems for the
                    // ones Pydantic raises. Only the first is worth showing.
                    val detail = runCatching { JSONObject(text).getString("detail") }
                        .getOrDefault("Erro $code")
                    throw ApiException(code, detail)
                }
                text
            } finally {
                connection.disconnect()
            }
        }

    companion object {
        const val BASE_URL = "https://tumtum-production.up.railway.app"

        /** Renew a minute early, so a request never leaves with a token that dies in flight. */
        private const val RENEW_MARGIN_MS = 60_000L
        private val TIME_FMT: java.time.format.DateTimeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")
    }
}
