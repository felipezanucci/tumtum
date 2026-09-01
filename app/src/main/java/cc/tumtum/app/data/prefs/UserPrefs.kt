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

data class UserState(
    val onboarded: Boolean,
    val account: Account?,
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
        )
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
