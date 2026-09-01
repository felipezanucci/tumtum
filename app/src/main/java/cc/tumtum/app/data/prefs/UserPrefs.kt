package cc.tumtum.app.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
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
) {
    val watchConnected: Boolean get() = sourcePackage != null
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

    /** Apagar conta apaga tudo (§7). O Room é limpo pelo repositório. */
    suspend fun wipe() {
        context.dataStore.edit { it.clear() }
    }
}
