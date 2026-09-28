package cc.tumtum.app.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tumtum_prefs")

data class Account(
    val name: String,
    /**
     * The @ **the server holds** for this account (28/09), or null while it
     * holds none. Until 28/09 this was whatever the phone was told at sign-up,
     * checked against six hard-coded names, and two accounts held @fezanu.
     * Since then only the server's answer is kept here — from the sign-up it
     * confirmed, from `/me`, from a `PATCH` it accepted — so anything that
     * shows it shows a name that really is this account's.
     */
    val username: String?,
    val email: String,
    val tribes: Set<String>,
    /**
     * An @ chosen on this phone before the server kept one (accounts made
     * before 28/09), waiting to be claimed. Never shown as the account's: it
     * may be someone else's by now. The claim is tried once the server can be
     * asked ([cc.tumtum.app.data.api.TumtumApi.me]).
     */
    val pendingUsername: String? = null,
    /**
     * The server refused [pendingUsername] — taken, reserved or not a valid
     * @. It is not tried again; Configurações asks the person to choose, with
     * the refused name in the field so the line under it says why.
     */
    val pendingRefused: Boolean = false,
) {
    /** What the profile route is keyed by: the @, or [ME] while there is none (never a real @ — too short). */
    val profileKey: String get() = username ?: ME

    companion object {
        const val ME = "me"
    }

    val initials: String
        get() = name.split(" ")
            .filter { it.isNotBlank() }
            .let { parts ->
                when {
                    parts.isEmpty() -> "TT"
                    parts.size == 1 -> parts[0].take(2).uppercase()
                    else -> "${parts.first().first()}${parts.last().first()}".uppercase()
                }
            }

    /**
     * Whether this profile on the phone is the one of the account signing in
     * (24/09). The name, the @, the tribes and the photo live only on the
     * phone, so they belong to whoever made them: a different account
     * arriving starts from its own, never from the previous person's.
     */
    fun belongsTo(email: String): Boolean =
        this.email.isNotBlank() && this.email.trim().equals(email.trim(), ignoreCase = true)
}

/**
 * The server's side of the account: a JWT, the user id it names, and the
 * refresh token that renews it (#34, 22/09).
 *
 * Live means the session can still be used without a password: the access
 * token has not expired, **or** there is a refresh token to renew it. The
 * access token lasts an hour now; judging liveness by it alone would have
 * every screen announce an expired session sixty minutes after sign-in. The
 * server is still the authority — when it refuses the refresh token, the app
 * drops it and the session reads as expired, which is then true.
 */
data class Session(val token: String, val userId: String?, val refreshToken: String? = null) {
    fun isLive(nowMillis: Long): Boolean =
        refreshToken != null || !cc.tumtum.app.data.api.AccessToken.isExpired(token, nowMillis)
}

/** The next event the person marked (§5.7): the calendar is the trigger, not a push. */
data class UpcomingEvent(
    val name: String,
    val venue: String,
    val eventType: String,
    val startAt: java.time.Instant,
    val serverEventId: String? = null,
)

data class UserState(
    val onboarded: Boolean,
    val account: Account?,
    /** Null until the account has signed in to the server (Etapa 1, 2026-09-18). */
    val session: Session? = null,
    val sourcePackage: String?,
    val sourceLabel: String?,
    /** Sensor BLE pareado (Polar H10/Verity Sense…), lembrado entre sessões (§10). */
    val bleAddress: String? = null,
    val bleName: String? = null,
    /** Identificador do participante do experimento (P01…P18, §9). */
    val participantId: String? = null,
    /** Foto de perfil (arquivo local em filesDir); null = avatar de iniciais. */
    val avatarPath: String? = null,
    /** Sessão de captura ativa — sobrevive à morte do processo (§4.3). */
    val activeCaptureEventId: Long? = null,
    /** Trava da revela (protocolo): noites novas só abrem às 10h da manhã seguinte. */
    val revealLockEnabled: Boolean = false,
    /**
     * Operator mode (Etapa 3, 2026-09-18): the GOL · MÚSICA · MOMENTO taps show
     * on the capture only on the phone of whoever runs the test. Their marks
     * go to the event's shared timeline and name everyone's moments; a fan
     * is never asked to do this.
     */
    val operatorMarks: Boolean = false,
    /**
     * Operator registration (21/09): the three shortcuts that create an event
     * from the phone — now, the next one, a past night — show on AO VIVO only
     * with this on. Events are TumTum's; a fan only ever picks one from the
     * list, and never sees a name, a venue or a time to type.
     */
    val operatorEvents: Boolean = false,
    /** The next marked event, if any — one at a time, by design. */
    val upcoming: UpcomingEvent? = null,
    /**
     * The account whose nights this phone shows: **the signed-in one, and
     * nobody when signed out** (25/09, b196 round). b196 kept the last
     * account after Sair "so the person who signed out still finds their
     * nights", and Felipe, signed out, saw his FZ, his name and his nights
     * everywhere: *"tudo dá a entender de que eu ainda estou na minha conta"*.
     * Signed out, the phone is nobody's. A different account signing in sees
     * only its own; the others' nights are hidden, never deleted.
     */
    val viewerId: String? = null,
    /**
     * The last account signed in here, kept after Sair — only to own a night
     * whose session ended in the middle of the capture. Never shown.
     */
    val lastUserId: String? = null,
    /**
     * The account the server confirmed as an operator (item 52, 25/09), from
     * `is_admin` on /api/auth/me. The operator tools show only while it is the
     * signed-in account: a local switch alone let a fan's phone "register"
     * events the server then refused.
     */
    val operatorUserId: String? = null,
    /** How the last session ended, and when — so the next "why am I out?" answers itself (25/09). */
    val sessionEnded: SessionEnd? = null,
    /**
     * The signed-in account's consents as the server last answered them
     * (28/09), or null when the phone has no answer for **this** account. A
     * capture starts only with `read_heart_rate` granted here, and a venue has
     * no signal: the check has to work from the last answer. Another
     * account's answer never counts — it is read only when its owner is the
     * signed-in one.
     */
    val consents: cc.tumtum.app.data.api.ConsentSnapshot? = null,
) {
    val watchConnected: Boolean get() = sourcePackage != null
    val sensorPaired: Boolean get() = bleAddress != null

    /** An account is on this phone — live or expired, but not signed out. */
    val signedIn: Boolean get() = session != null

    /** True only while the signed-in account is the one the server called an operator. */
    val isOperator: Boolean
        get() = operatorUserId != null && operatorUserId == session?.userId

    /** Whether the signed-in account granted [purpose], as last heard; null when the phone never heard. */
    fun granted(purpose: String): Boolean? = consents?.granted(purpose)

    /** The GOL · MÚSICA · MOMENTO taps: switched on here, and granted by the server. */
    val marksOn: Boolean get() = operatorMarks && isOperator

    /** The event-registration shortcuts on AO VIVO: switched on here, and granted by the server. */
    val eventsOn: Boolean get() = operatorEvents && isOperator

    /**
     * The raw-session export (§9, the protocol's manual extraction): on the
     * operator's account only. A fan never sees it (25/09: "não tem que
     * aparecer essa opção de exportar"). The participant code that also
     * opened it went away at Felipe's word the same day.
     */
    val showsExport: Boolean get() = isOperator
}

/**
 * Why the phone is out of the account (25/09). Felipe picked the phone up one
 * morning signed out and nobody could say whether he had tapped Sair or the
 * server had refused to renew: the line that would have told was overwritten
 * the moment he signed in again. The reason is kept until the next sign-in.
 */
data class SessionEnd(val reason: String, val at: java.time.Instant) {
    companion object {
        /** The person tapped Sair. */
        const val SIGNED_OUT = "signed_out"
        /** The server refused to renew the session: expired, revoked, or reused. */
        const val REFUSED = "refused"
    }
}

class UserPrefs(private val context: Context) {

    /**
     * The session tokens, encrypted (26/09). They lived in this DataStore
     * until then, in plain text; [migrateLegacyTokens] moves an old pair
     * across once, and until it has run the old pair is still read, so an
     * update never signs anybody out.
     */
    private val secure = SecureTokens(context)

    private object Keys {
        val onboarded = booleanPreferencesKey("onboarded")
        val name = stringPreferencesKey("name")
        val username = stringPreferencesKey("username")
        /**
         * Whether [username] is the server's (28/09). Absent on every install
         * from before it: those @s were the phone's alone, and read as pending.
         */
        val usernameOnServer = booleanPreferencesKey("username_on_server")
        /** The server refused the pending @; it is not claimed again. */
        val usernameRefused = booleanPreferencesKey("username_refused")
        val email = stringPreferencesKey("email")
        val tribes = stringSetPreferencesKey("tribes")
        val sourcePackage = stringPreferencesKey("source_package")
        val sourceLabel = stringPreferencesKey("source_label")
        val bleAddress = stringPreferencesKey("ble_address")
        val bleName = stringPreferencesKey("ble_name")
        val participantId = stringPreferencesKey("participant_id")
        val avatarPath = stringPreferencesKey("avatar_path")
        val activeCaptureEventId = longPreferencesKey("active_capture_event_id")
        val revealLockEnabled = booleanPreferencesKey("reveal_lock_enabled")
        val operatorMarks = booleanPreferencesKey("operator_marks")
        val operatorEvents = booleanPreferencesKey("operator_events")
        val upcomingName = stringPreferencesKey("upcoming_name")
        val upcomingVenue = stringPreferencesKey("upcoming_venue")
        val upcomingType = stringPreferencesKey("upcoming_type")
        val upcomingStartAt = longPreferencesKey("upcoming_start_at")
        val upcomingServerEventId = stringPreferencesKey("upcoming_server_event_id")
        /** Legacy (before 26/09): read only to migrate. The tokens live in [SecureTokens]. */
        val accessToken = stringPreferencesKey("access_token")
        val userId = stringPreferencesKey("user_id")
        /** Legacy, like [accessToken]. */
        val refreshToken = stringPreferencesKey("refresh_token")
        val viewerId = stringPreferencesKey("viewer_user_id")
        val operatorUserId = stringPreferencesKey("operator_user_id")
        val sessionEndedReason = stringPreferencesKey("session_ended_reason")
        val sessionEndedAt = longPreferencesKey("session_ended_at")
        /** The last GET/PUT /api/consents body, and the account it was about (28/09). */
        val consentsJson = stringPreferencesKey("consents_json")
        val consentsUserId = stringPreferencesKey("consents_user_id")
    }

    val state: Flow<UserState> = combine(context.dataStore.data, secure.tokens) { p, secureTokens ->
        val tokens = secureTokens ?: p[Keys.accessToken]?.let { Tokens(it, p[Keys.refreshToken]) }
        val username = p[Keys.username]
        val onServer = p[Keys.usernameOnServer] == true
        UserState(
            onboarded = p[Keys.onboarded] ?: false,
            // A profile exists when the phone has one — an @ or an address.
            // Since 28/09 an account may be here with no @ at all (made before
            // the server kept one, and not chosen yet).
            account = if (username != null || p[Keys.email] != null) {
                Account(
                    name = p[Keys.name] ?: "",
                    username = username?.takeIf { onServer },
                    email = p[Keys.email] ?: "",
                    tribes = p[Keys.tribes] ?: emptySet(),
                    pendingUsername = username?.takeIf { !onServer },
                    pendingRefused = !onServer && p[Keys.usernameRefused] == true,
                )
            } else {
                null
            },
            sourcePackage = p[Keys.sourcePackage],
            sourceLabel = p[Keys.sourceLabel],
            bleAddress = p[Keys.bleAddress],
            bleName = p[Keys.bleName],
            participantId = p[Keys.participantId],
            avatarPath = p[Keys.avatarPath],
            activeCaptureEventId = p[Keys.activeCaptureEventId],
            revealLockEnabled = p[Keys.revealLockEnabled] ?: false,
            operatorMarks = p[Keys.operatorMarks] ?: false,
            operatorEvents = p[Keys.operatorEvents] ?: false,
            upcoming = p[Keys.upcomingName]?.let { n ->
                p[Keys.upcomingStartAt]?.let { at ->
                    UpcomingEvent(
                        name = n,
                        venue = p[Keys.upcomingVenue] ?: "",
                        eventType = p[Keys.upcomingType] ?: "concert",
                        startAt = java.time.Instant.ofEpochMilli(at),
                        serverEventId = p[Keys.upcomingServerEventId],
                    )
                }
            },
            session = tokens?.let {
                Session(token = it.access, userId = p[Keys.userId], refreshToken = it.refresh)
            },
            viewerId = p[Keys.userId],
            lastUserId = p[Keys.viewerId] ?: p[Keys.userId],
            operatorUserId = p[Keys.operatorUserId],
            sessionEnded = p[Keys.sessionEndedReason]?.let { reason ->
                p[Keys.sessionEndedAt]?.let { SessionEnd(reason, java.time.Instant.ofEpochMilli(it)) }
            },
            consents = p[Keys.consentsJson]
                ?.takeIf { p[Keys.userId] != null && p[Keys.consentsUserId] == p[Keys.userId] }
                ?.let { runCatching { cc.tumtum.app.data.api.ConsentSnapshot.parse(it) }.getOrNull() },
        )
    }

    /** What the server just said about [userId]'s consents — the body as it came (28/09). */
    suspend fun setConsents(userId: String?, json: String) {
        context.dataStore.edit { p ->
            if (userId == null) {
                p.remove(Keys.consentsJson)
                p.remove(Keys.consentsUserId)
            } else {
                p[Keys.consentsJson] = json
                p[Keys.consentsUserId] = userId
            }
        }
    }

    /** A fresh sign-in or a Sair: the phone forgets what it heard, and asks again. */
    suspend fun clearConsents() {
        context.dataStore.edit { p ->
            p.remove(Keys.consentsJson)
            p.remove(Keys.consentsUserId)
        }
    }

    suspend fun setSession(session: Session) {
        withContext(Dispatchers.IO) { secure.set(Tokens(session.token, session.refreshToken)) }
        context.dataStore.edit { p ->
            p.remove(Keys.accessToken)
            p.remove(Keys.refreshToken)
            session.userId?.let { p[Keys.userId] = it } ?: p.remove(Keys.userId)
            // Remembered past Sair only to own a night cut by it (lastUserId).
            session.userId?.let { p[Keys.viewerId] = it }
            p.remove(Keys.sessionEndedReason)
            p.remove(Keys.sessionEndedAt)
        }
    }

    /**
     * Sign-out: the token goes, the local profile and the nights stay — and
     * the phone remembers that this was a Sair, and when.
     */
    suspend fun clearSession(reason: String = SessionEnd.SIGNED_OUT) {
        withContext(Dispatchers.IO) { secure.set(null) }
        context.dataStore.edit { p ->
            p.remove(Keys.accessToken)
            p.remove(Keys.userId)
            p.remove(Keys.refreshToken)
            // Signed out, the phone holds nobody's consents (28/09).
            p.remove(Keys.consentsJson)
            p.remove(Keys.consentsUserId)
            p[Keys.sessionEndedReason] = reason
            p[Keys.sessionEndedAt] = System.currentTimeMillis()
        }
    }

    /**
     * The server refused to renew (#34): the refresh token goes, so every
     * screen now reads the session as expired, and the phone keeps why.
     */
    suspend fun markRenewalRefused() {
        withContext(Dispatchers.IO) { secure.dropRefresh() }
        context.dataStore.edit { p ->
            p.remove(Keys.refreshToken)
            p[Keys.sessionEndedReason] = SessionEnd.REFUSED
            p[Keys.sessionEndedAt] = System.currentTimeMillis()
        }
    }

    /** What the server said about this account's operator role: [userId] when it is one, null when not. */
    suspend fun setOperatorUserId(userId: String?) {
        context.dataStore.edit { p ->
            if (userId == null) p.remove(Keys.operatorUserId) else p[Keys.operatorUserId] = userId
        }
    }

    suspend fun createAccount(account: Account) {
        context.dataStore.edit { p ->
            p[Keys.name] = account.name
            writeUsername(p, account)
            p[Keys.email] = account.email
            p[Keys.tribes] = account.tribes
        }
    }

    /** The @ as [account] has it: the server's, or a pending one from before 28/09, or none. */
    private fun writeUsername(p: androidx.datastore.preferences.core.MutablePreferences, account: Account) {
        val name = account.username ?: account.pendingUsername
        if (name == null) p.remove(Keys.username) else p[Keys.username] = name
        p[Keys.usernameOnServer] = account.username != null
        if (account.username == null && account.pendingRefused) {
            p[Keys.usernameRefused] = true
        } else {
            p.remove(Keys.usernameRefused)
        }
    }

    /**
     * What the server says the @ of the account at [email] is (28/09) — on
     * every `/me` and after a `PATCH` it accepted. The server is the source
     * of truth: its @ replaces whatever the phone had. Null from the server
     * leaves a pending @ pending, and turns one the phone believed was held
     * back into pending — it is then claimed again, never shown as held.
     * Another account's profile on the phone is never touched.
     */
    suspend fun setServerUsername(email: String, username: String?) {
        context.dataStore.edit { p ->
            val mine = p[Keys.email]?.let { it.isNotBlank() && it.trim().equals(email.trim(), ignoreCase = true) } == true
            if (!mine) return@edit
            if (username != null) {
                p[Keys.username] = username
                p[Keys.usernameOnServer] = true
                p.remove(Keys.usernameRefused)
            } else if (p[Keys.usernameOnServer] == true) {
                p[Keys.usernameOnServer] = false
            }
        }
    }

    /** The server refused the pending @ of the account at [email]: kept only to show why, never claimed again. */
    suspend fun markUsernameRefused(email: String) {
        context.dataStore.edit { p ->
            val mine = p[Keys.email]?.let { it.isNotBlank() && it.trim().equals(email.trim(), ignoreCase = true) } == true
            if (!mine || p[Keys.usernameOnServer] == true) return@edit
            p[Keys.usernameRefused] = true
        }
    }

    /**
     * Another person's account arrives on this phone (24/09): the profile is
     * theirs from the first screen, and **the previous person's photo goes**.
     * Test 7 showed Felipe's photo over the name *teste1* — the photo is
     * kept only here, and nothing ever cleared it, so the app put one
     * person's face on another person's account.
     *
     * Since 25/09 the previous person's **sensor, watch, participant id and
     * marked event** go too. A new account on the phone opened setup with
     * Felipe's Polar H10 already "paired", and no way to search for one's
     * own: the same leak as the photo, one screen later.
     */
    suspend fun replaceAccount(account: Account) {
        val previousPhoto = state.first().avatarPath
        context.dataStore.edit { p ->
            p[Keys.name] = account.name
            writeUsername(p, account)
            p[Keys.email] = account.email
            p[Keys.tribes] = account.tribes
            p.remove(Keys.avatarPath)
            p.remove(Keys.bleAddress)
            p.remove(Keys.bleName)
            p.remove(Keys.sourcePackage)
            p.remove(Keys.sourceLabel)
            p.remove(Keys.participantId)
            p.remove(Keys.upcomingName); p.remove(Keys.upcomingVenue); p.remove(Keys.upcomingType)
            p.remove(Keys.upcomingStartAt); p.remove(Keys.upcomingServerEventId)
        }
        previousPhoto?.let { runCatching { java.io.File(it).delete() } }
    }

    suspend fun setOnboarded() {
        context.dataStore.edit { it[Keys.onboarded] = true }
    }

    suspend fun setSource(packageName: String, label: String) {
        context.dataStore.edit { p ->
            p[Keys.sourcePackage] = packageName
            p[Keys.sourceLabel] = label
        }
    }

    /** "Trocar relógio" on setup (25/09): the chosen watch is forgotten, nothing else. */
    suspend fun clearSource() {
        context.dataStore.edit { p ->
            p.remove(Keys.sourcePackage)
            p.remove(Keys.sourceLabel)
        }
    }

    suspend fun setSensor(address: String, name: String) {
        context.dataStore.edit { p ->
            p[Keys.bleAddress] = address
            p[Keys.bleName] = name
        }
    }

    suspend fun clearSensor() {
        context.dataStore.edit { p ->
            p.remove(Keys.bleAddress)
            p.remove(Keys.bleName)
        }
    }

    suspend fun setName(name: String) {
        context.dataStore.edit { p -> if (name.isNotBlank()) p[Keys.name] = name.trim() }
    }

    /**
     * The one-time repair of a pre-b9 profile (an e-mail stored as the @).
     * It touches only a pending @ — one the server holds is the server's.
     */
    suspend fun setProfile(name: String, username: String) {
        context.dataStore.edit { p ->
            if (name.isNotBlank()) p[Keys.name] = name.trim()
            if (username.isNotBlank() && p[Keys.usernameOnServer] != true) p[Keys.username] = username
        }
    }

    suspend fun setAvatarPath(path: String?) {
        context.dataStore.edit { p ->
            if (path == null) p.remove(Keys.avatarPath) else p[Keys.avatarPath] = path
        }
    }

    suspend fun setParticipantId(id: String) {
        context.dataStore.edit { p ->
            if (id.isBlank()) p.remove(Keys.participantId) else p[Keys.participantId] = id.trim()
        }
    }

    suspend fun setUpcoming(event: UpcomingEvent) {
        context.dataStore.edit { p ->
            p[Keys.upcomingName] = event.name
            p[Keys.upcomingVenue] = event.venue
            p[Keys.upcomingType] = event.eventType
            p[Keys.upcomingStartAt] = event.startAt.toEpochMilli()
            event.serverEventId?.let { p[Keys.upcomingServerEventId] = it } ?: p.remove(Keys.upcomingServerEventId)
        }
    }

    suspend fun clearUpcoming() {
        context.dataStore.edit { p ->
            p.remove(Keys.upcomingName); p.remove(Keys.upcomingVenue); p.remove(Keys.upcomingType)
            p.remove(Keys.upcomingStartAt); p.remove(Keys.upcomingServerEventId)
        }
    }

    suspend fun setRevealLock(enabled: Boolean) {
        context.dataStore.edit { it[Keys.revealLockEnabled] = enabled }
    }

    suspend fun setOperatorMarks(enabled: Boolean) {
        context.dataStore.edit { it[Keys.operatorMarks] = enabled }
    }

    suspend fun setOperatorEvents(enabled: Boolean) {
        context.dataStore.edit { it[Keys.operatorEvents] = enabled }
    }

    suspend fun setActiveCapture(eventId: Long) {
        context.dataStore.edit { it[Keys.activeCaptureEventId] = eventId }
    }

    suspend fun clearActiveCapture() {
        context.dataStore.edit { it.remove(Keys.activeCaptureEventId) }
    }

    /** Apagar conta apaga tudo (§7). O Room é limpo pelo repositório. */
    suspend fun wipe() {
        withContext(Dispatchers.IO) { secure.set(null) }
        context.dataStore.edit { it.clear() }
    }

    /**
     * Once per install, at start (26/09): a session kept in plain DataStore
     * by an older build moves into [SecureTokens] and leaves this file.
     */
    suspend fun migrateLegacyTokens() {
        val p = context.dataStore.data.first()
        val access = p[Keys.accessToken] ?: return
        withContext(Dispatchers.IO) {
            if (secure.peek() == null) secure.set(Tokens(access, p[Keys.refreshToken]))
        }
        context.dataStore.edit { e ->
            e.remove(Keys.accessToken)
            e.remove(Keys.refreshToken)
        }
    }
}
