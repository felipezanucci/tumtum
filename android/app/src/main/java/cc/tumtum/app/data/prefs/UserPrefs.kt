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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tumtum_prefs")

data class Account(
    val name: String,
    val username: String,
    val email: String,
    val tribes: Set<String>,
) {
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
}

/**
 * The server's side of the account: a JWT and the user id it names.
 * Live means the token's own expiry has not passed — the server is still
 * the authority, this only decides what a screen may promise.
 */
data class Session(val token: String, val userId: String?) {
    fun isLive(nowMillis: Long): Boolean =
        !cc.tumtum.app.data.api.AccessToken.isExpired(token, nowMillis)
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
) {
    val watchConnected: Boolean get() = sourcePackage != null
    val sensorPaired: Boolean get() = bleAddress != null
}

class UserPrefs(private val context: Context) {

    private object Keys {
        val onboarded = booleanPreferencesKey("onboarded")
        val name = stringPreferencesKey("name")
        val username = stringPreferencesKey("username")
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
        val accessToken = stringPreferencesKey("access_token")
        val userId = stringPreferencesKey("user_id")
    }

    val state: Flow<UserState> = context.dataStore.data.map { p ->
        val username = p[Keys.username]
        UserState(
            onboarded = p[Keys.onboarded] ?: false,
            account = username?.let {
                Account(
                    name = p[Keys.name] ?: "",
                    username = it,
                    email = p[Keys.email] ?: "",
                    tribes = p[Keys.tribes] ?: emptySet(),
                )
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
            session = p[Keys.accessToken]?.let { Session(token = it, userId = p[Keys.userId]) },
        )
    }

    suspend fun setSession(session: Session) {
        context.dataStore.edit { p ->
            p[Keys.accessToken] = session.token
            session.userId?.let { p[Keys.userId] = it } ?: p.remove(Keys.userId)
        }
    }

    /** Sign-out: the token goes, the local profile and the nights stay. */
    suspend fun clearSession() {
        context.dataStore.edit { p ->
            p.remove(Keys.accessToken)
            p.remove(Keys.userId)
        }
    }

    suspend fun createAccount(account: Account) {
        context.dataStore.edit { p ->
            p[Keys.name] = account.name
            p[Keys.username] = account.username
            p[Keys.email] = account.email
            p[Keys.tribes] = account.tribes
        }
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

    /** Nome e @ editáveis — o @ é sempre o que a pessoa escolheu. */
    suspend fun setProfile(name: String, username: String) {
        context.dataStore.edit { p ->
            if (name.isNotBlank()) p[Keys.name] = name.trim()
            if (username.isNotBlank()) p[Keys.username] = username
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
        context.dataStore.edit { it.clear() }
    }
}
