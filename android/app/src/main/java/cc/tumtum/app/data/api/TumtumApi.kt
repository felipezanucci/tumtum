package cc.tumtum.app.data.api

import cc.tumtum.app.data.prefs.Session
import cc.tumtum.app.data.prefs.UserPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
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

    /** The server refused or could not do what was asked; [detail] is its own sentence. */
    class ApiException(val code: Int, val detail: String) : IOException(detail)

    /** Who the token says we are, as the server describes it. */
    data class Me(val id: String, val email: String, val name: String)

    // --- Auth ---

    /** Creates the account and signs in: the server answers with a token. */
    suspend fun register(email: String, name: String, password: String): Session {
        val body = JSONObject().put("email", email).put("name", name).put("password", password)
        val response = JSONObject(request("POST", "/api/auth/register", body.toString(), token = null))
        return storeSession(response.getString("access_token"))
    }

    suspend fun login(email: String, password: String): Session {
        val body = JSONObject().put("email", email).put("password", password)
        val response = JSONObject(request("POST", "/api/auth/login", body.toString(), token = null))
        return storeSession(response.getString("access_token"))
    }

    suspend fun me(): Me {
        val json = JSONObject(request("GET", "/api/auth/me", null, token = requireToken()))
        return Me(id = json.getString("id"), email = json.getString("email"), name = json.getString("name"))
    }

    /** Forget the token. On sign-out, and when the server refuses it. */
    suspend fun signOut() = prefs.clearSession()

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

    /** Runs the detector on an uploaded night and returns its moments, named where the event has a timeline. */
    suspend fun analyze(serverSessionId: String): List<ServerMoment> =
        ServerMoments.parse(request("POST", "/api/experience/$serverSessionId/analyze", "", token = requireToken()))

    private suspend fun storeSession(token: String): Session {
        // The user id is in the token's `sub`; reading it here spares a round
        // trip and keeps the session self-describing when the network is gone.
        val userId = runCatching {
            val payload = String(
                java.util.Base64.getUrlDecoder().decode(token.split('.')[1]),
                Charsets.UTF_8,
            )
            JSONObject(payload).getString("sub")
        }.getOrNull()
        val session = Session(token = token, userId = userId)
        prefs.setSession(session)
        return session
    }

    private suspend fun requireToken(): String {
        val session = prefs.state.first().session ?: throw ApiException(401, "Sem sessão")
        if (!session.isLive(System.currentTimeMillis())) throw ApiException(401, "Sessão expirada")
        return session.token
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
        private val TIME_FMT: java.time.format.DateTimeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")
    }
}
