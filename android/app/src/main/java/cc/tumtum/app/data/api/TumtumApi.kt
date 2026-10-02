package cc.tumtum.app.data.api

import cc.tumtum.app.BuildConfig
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
import java.time.LocalDate

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
    open class ApiException(val code: Int, val detail: String) : IOException(detail)

    /**
     * The server refused because a consent is missing (403, `code:
     * "consent_required"`, 26/09). Typed, so a screen opens the consent screen
     * on [purpose] — the contract says never to retry this silently.
     */
    class ConsentRequired(val purpose: String, detail: String) : ApiException(403, detail)

    /**
     * Who the token says we are, as the server describes it — including
     * whether it operates the platform, and the birth date (null until given:
     * accounts from before 26/09 are asked for it once, at the gate).
     */
    data class Me(
        val id: String,
        val email: String,
        val name: String,
        val isAdmin: Boolean = false,
        val birthDate: LocalDate? = null,
        /** The @ the server holds (28/09); null for an account made before it that has not chosen one. */
        val username: String? = null,
    )

    // --- Auth ---

    /**
     * Step one of an account (#64, 24/09): the server mails a 6-digit code to
     * [email] and creates nothing. The account exists only after
     * [signupConfirm], so an address nobody reads never becomes one.
     */
    suspend fun signupStart(
        email: String,
        name: String,
        password: String,
        birthDate: LocalDate,
        termsAccepted: Boolean = true,
        readHeartRate: Boolean = false,
        username: String,
    ): SignupStarted {
        // Since 26/09 the account carries its birth date and the person's own
        // tick on the Terms and the Privacy Policy, with the text version they
        // read. The server refuses under 18 and without the tick, in its own words.
        val body = JSONObject()
            .put("email", email)
            .put("name", name)
            .put("password", password)
            .put("birth_date", birthDate.toString())
            .put("terms_accepted", termsAccepted)
            .put("consent_text_version", cc.tumtum.app.domain.ConsentText.VERSION)
            .put("read_heart_rate", readHeartRate)
            // The @ (28/09): checked again by the server before any mail
            // leaves, and held for this address while the code is open.
            .put("username", username)
        val response = JSONObject(request("POST", "/api/auth/register/start", body.toString(), token = null))
        return SignupStarted.from(response, asked = email)
    }

    /** Step two: the code came back, the account is created, and the server answers with a token. */
    suspend fun signupConfirm(email: String, code: String): Session {
        val body = JSONObject().put("email", email).put("code", code)
        val response = JSONObject(request("POST", "/api/auth/register/confirm", body.toString(), token = null))
        return freshSignIn(response)
    }

    suspend fun login(email: String, password: String): Session {
        val body = JSONObject().put("email", email).put("password", password)
        val response = JSONObject(request("POST", "/api/auth/login", body.toString(), token = null))
        return freshSignIn(response)
    }

    /**
     * "Esqueci a senha" (02/10): the server mails a 6-digit code (and the
     * site's link) to [email] if it has an account, and says the same either
     * way — the answer tells nobody whether the address has one.
     */
    suspend fun forgotPassword(email: String) {
        val body = JSONObject().put("email", email)
        request("POST", "/api/auth/forgot-password", body.toString(), token = null)
    }

    /** The code from that mail and a new password: the password changes, every other device is signed out, and this one is signed in. */
    suspend fun resetPasswordWithCode(email: String, code: String, password: String): Session {
        val body = JSONObject().put("email", email).put("code", code).put("password", password)
        val response = JSONObject(request("POST", "/api/auth/reset-password/code", body.toString(), token = null))
        return freshSignIn(response)
    }

    /**
     * A sign-in, as opposed to a renewal (28/09): whatever the phone had heard
     * about consents is forgotten — it may be another account's, or older than
     * a change made on the web — and asked again at once. Offline, the next
     * screen that needs it asks.
     */
    private suspend fun freshSignIn(response: JSONObject): Session {
        prefs.clearConsents()
        val session = storeSession(response.getString("access_token"), response.optRefresh())
        runCatching { getConsents() }
        return session
    }

    /**
     * Who the token says we are. Also records the server's word on the
     * operator role (item 52, 25/09): the operator tools follow `is_admin`,
     * never a switch on the phone alone.
     */
    suspend fun me(): Me {
        val json = JSONObject(request("GET", "/api/auth/me", null, token = requireToken()))
        val me = Me(
            id = json.getString("id"),
            email = json.getString("email"),
            name = json.getString("name"),
            isAdmin = json.optBoolean("is_admin", false),
            birthDate = Json.text(json, "birth_date")?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
            username = Json.text(json, "username"),
        )
        prefs.setOperatorUserId(if (me.isAdmin) me.id else null)
        // The @ is the server's (28/09): what it says replaces what the phone had.
        prefs.setServerUsername(me.email, me.username)
        if (me.username == null) {
            claimPendingUsername(me.email)?.let { return me.copy(username = it) }
        }
        return me
    }

    // --- The @ (28/09) ---

    /**
     * Whether [name] can be an @, asked while the person types. No token: an
     * @ is public by nature. Throws when there is no answer — the screen then
     * says it could not check, never that the name is free.
     */
    suspend fun checkUsername(name: String): UsernameAnswer {
        val segment = java.net.URLEncoder.encode(name, "UTF-8").replace("+", "%20")
        return UsernameAnswer.parse(request("GET", "/api/auth/username/$segment", null, token = null), asked = name)
    }

    /**
     * Chooses the @ of an account that has none (`PATCH /api/users/me`), once:
     * the server takes it only while its own is null, and answers 409 when it
     * is taken or the account already has another, 422 when it is not an @.
     * On success the server's answer is kept as this account's @.
     */
    suspend fun setUsername(name: String): String {
        val body = JSONObject().put("username", name)
        val response = JSONObject(request("PATCH", "/api/users/me", body.toString(), token = requireToken()))
        val held = Json.text(response, "username") ?: name
        val email = Json.text(response, "email") ?: prefs.state.first().account?.email.orEmpty()
        prefs.setServerUsername(email, held)
        return held
    }

    /**
     * An account made before 28/09 has no @ on the server, and its phone may
     * have one the person chose back then. That one is claimed here, once:
     * accepted, it becomes the account's; refused (taken by now, reserved, not
     * a valid @), it is marked so and Configurações asks for another. No
     * answer (offline) leaves it pending, to be tried on the next `/me`.
     * Answers with the @ now held, or null.
     */
    private suspend fun claimPendingUsername(email: String): String? {
        val account = prefs.state.first().account ?: return null
        if (!account.belongsTo(email) || account.username != null || account.pendingRefused) return null
        val pending = account.pendingUsername ?: return null
        return try {
            setUsername(pending)
        } catch (e: ApiException) {
            if (e.code == 409 || e.code == 422) prefs.markUsernameRefused(email)
            null
        } catch (e: IOException) {
            null
        }
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
     *
     * Since 26/09 it asks the password again (`POST /api/users/me/delete`):
     * a phone left unlocked on a table must not be enough to erase someone.
     * A wrong password is a 401 the caller says as such.
     */
    suspend fun deleteAccount(password: String) {
        val body = JSONObject().put("password", password)
        request("POST", "/api/users/me/delete", body.toString(), token = requireToken())
        prefs.clearSession()
    }

    /**
     * Once, for an account that has none (`PATCH /api/users/me`, 26/09): the
     * server takes a birth date only while it is null, and refuses under 18
     * with its own sentence.
     */
    /**
     * The name, on the account (28/09): Configurações said "Salvo." over a
     * name kept only on the phone, while the feed showed the server's. The
     * server's answer — one space between words — is what the phone keeps.
     */
    suspend fun setName(name: String): String {
        val body = JSONObject().put("name", name)
        val response = JSONObject(request("PATCH", "/api/users/me", body.toString(), token = requireToken()))
        return Json.text(response, "name") ?: name
    }

    suspend fun patchBirthDate(date: LocalDate) {
        val body = JSONObject().put("birth_date", date.toString())
        request("PATCH", "/api/users/me", body.toString(), token = requireToken())
    }

    // --- Consent (26/09) ---

    /**
     * What this account agreed to, purpose by purpose, as the server recorded
     * it. Every answer is also kept on the phone for this account (28/09), so
     * the capture can check `read_heart_rate` where there is no signal.
     */
    suspend fun getConsents(): ConsentSnapshot =
        keepConsents(request("GET", "/api/consents", null, token = requireToken()))

    /**
     * Grants or revokes the purposes given, and only those. The server keeps
     * the history append-only and answers with the state as it now stands —
     * which the phone keeps, like [getConsents].
     */
    suspend fun putConsents(purposes: Map<String, Boolean>, means: String = cc.tumtum.app.domain.ConsentText.MEANS_TAP): ConsentSnapshot =
        keepConsents(request("PUT", "/api/consents", ConsentSnapshot.putBody(purposes, means), token = requireToken()))

    private suspend fun keepConsents(body: String): ConsentSnapshot {
        val snapshot = ConsentSnapshot.parse(body)
        prefs.setConsents(prefs.state.first().session?.userId, body)
        return snapshot
    }

    /**
     * "Apagar esta noite" (26/09): the server deletes the session with its
     * readings, moments, cards and feed posts. A 404 means it is already gone.
     */
    suspend fun deleteNight(serverSessionId: String) {
        request("DELETE", "/api/health/sessions/$serverSessionId", null, token = requireToken())
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

    /**
     * A night as the server took it: its id, and how many of its readings fell
     * inside the event's window (`event_readings`, 28/09) — the number the feed
     * counts attendance by. Null when the server did not say (no event, or an
     * older server).
     */
    data class CreatedSession(val id: String, val eventReadings: Int?)

    /**
     * Uploads a night's readings; the server answers with the session it made.
     * Since 28/09 it takes one only with both `read_heart_rate` and
     * `keep_night` granted, and says which is missing (a [ConsentRequired]).
     */
    suspend fun createSession(
        startAt: java.time.Instant,
        endAt: java.time.Instant,
        sourceDevice: String,
        samples: List<cc.tumtum.app.domain.HrSample>,
        serverEventId: String? = null,
    ): CreatedSession {
        val body = SessionPayload.build(startAt, endAt, sourceDevice, samples, serverEventId)
        val response = JSONObject(request("POST", "/api/health/sessions", body.toString(), token = requireToken()))
        return CreatedSession(
            id = response.getString("id"),
            eventReadings = if (response.has("event_readings") && !response.isNull("event_readings")) {
                response.optInt("event_readings")
            } else {
                null
            },
        )
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
            if (e.code == 401) prefs.markRenewalRefused()
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
                // Which client made the request (26/09): recorded with each
                // consent, so the history says where a choice was made.
                connection.setRequestProperty("X-Tumtum-Client", CLIENT)
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
                    val detail = runCatching {
                        val o = JSONObject(text)
                        // A structured refusal may nest its sentence one level down.
                        o.optJSONObject("detail")?.let { Json.text(it, "detail") } ?: o.getString("detail")
                    }.getOrDefault("Erro $code")
                    // A missing consent is its own thing (26/09): the screen
                    // opens the consent on that purpose instead of retrying.
                    if (code == 403) {
                        ConsentSnapshot.requiredPurpose(text)?.let { throw ConsentRequired(it, detail) }
                    }
                    throw ApiException(code, detail)
                }
                text
            } finally {
                connection.disconnect()
            }
        }

    companion object {
        const val BASE_URL = "https://tumtum-production.up.railway.app"

        /** `X-Tumtum-Client`, as the shared contract spells it. */
        private val CLIENT = "android/${BuildConfig.VERSION_CODE}"

        /** Renew a minute early, so a request never leaves with a token that dies in flight. */
        private const val RENEW_MARGIN_MS = 60_000L
        private val TIME_FMT: java.time.format.DateTimeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")
    }
}
