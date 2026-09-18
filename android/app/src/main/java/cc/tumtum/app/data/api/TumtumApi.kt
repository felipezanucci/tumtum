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
    }
}
