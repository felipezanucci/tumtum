package cc.tumtum.app.data.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * SharedPreferences encrypted with a key held by the Android Keystore
 * (LGPD remediation, 26/09). Used for the session tokens and for the key of
 * the encrypted database — the two secrets on the phone that open someone's
 * heart-rate data.
 */
object SecurePrefs {

    fun open(context: Context, name: String): SharedPreferences {
        val app = context.applicationContext
        return try {
            create(app, name)
        } catch (e: Exception) {
            // The Keystore key behind this file is gone or unreadable (a
            // restore onto another phone, a vendor Keystore bug). The file can
            // never be read again, so it is dropped and started over: the
            // person signs in again, which is the honest cost.
            app.deleteSharedPreferences(name)
            create(app, name)
        }
    }

    private fun create(context: Context, name: String): SharedPreferences {
        val master = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            name,
            master,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }
}

/** The server's two tokens: the one-hour access token and the refresh token that renews it. */
data class Tokens(val access: String, val refresh: String?)

/**
 * Where the session tokens live since 26/09: encrypted, not in the plain
 * DataStore file with the rest of the preferences. A 90-day refresh token is
 * the whole account to whoever copies it.
 *
 * [UserPrefs] combines [tokens] with its DataStore, so every screen still
 * sees one `UserState` and nothing outside the prefs layer changed.
 */
class SecureTokens(context: Context) {
    private val app = context.applicationContext
    private val prefs: SharedPreferences by lazy { SecurePrefs.open(app, FILE) }
    private val current: MutableStateFlow<Tokens?> by lazy { MutableStateFlow(read()) }

    /** The tokens as they stand, read off the main thread the first time. */
    val tokens: Flow<Tokens?> = flow { emitAll(current) }.flowOn(Dispatchers.IO)

    fun peek(): Tokens? = current.value

    fun set(tokens: Tokens?) {
        val editor = prefs.edit()
        if (tokens == null) {
            editor.remove(KEY_ACCESS)
            editor.remove(KEY_REFRESH)
        } else {
            editor.putString(KEY_ACCESS, tokens.access)
            if (tokens.refresh == null) editor.remove(KEY_REFRESH) else editor.putString(KEY_REFRESH, tokens.refresh)
        }
        editor.commit()
        current.value = tokens
    }

    /** The server refused the refresh token: it goes, the access token stays until it expires. */
    fun dropRefresh() {
        val now = current.value ?: return
        set(now.copy(refresh = null))
    }

    private fun read(): Tokens? {
        val access = prefs.getString(KEY_ACCESS, null) ?: return null
        return Tokens(access, prefs.getString(KEY_REFRESH, null))
    }

    private companion object {
        const val FILE = "tumtum_secure_session"
        const val KEY_ACCESS = "access_token"
        const val KEY_REFRESH = "refresh_token"
    }
}
