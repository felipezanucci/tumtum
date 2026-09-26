package cc.tumtum.app.ui.screens.account

import android.content.Context
import cc.tumtum.app.R
import cc.tumtum.app.data.api.TumtumApi
import java.io.IOException

/**
 * What an account screen says when the server says no.
 *
 * Each branch names the real condition. "Something went wrong" would be the
 * bug class this project keeps finding — the app stating something false
 * about its own state — with a friendlier face.
 */
object AuthErrors {
    fun messageFor(error: Throwable, context: Context): String = when (error) {
        is TumtumApi.ApiException -> when (error.code) {
            401 -> context.getString(R.string.auth_error_credentials)
            409 -> context.getString(R.string.auth_error_taken)
            // The sign-up code's answers (#64) are sentences written for the
            // person — a wrong code with the tries left, a code gone stale,
            // a wait before another, a mail that could not leave. Shown as
            // the server wrote them.
            400, 410, 429, 503 -> error.detail
            // Pydantic's refusal carries a list, not a sentence; a refusal
            // the server raises on purpose (under 18, the Terms not ticked,
            // 26/09) is a sentence, shown as the server wrote it.
            422 -> if (isSentence(error.detail)) error.detail else context.getString(R.string.auth_error_invalid)
            else -> context.getString(R.string.auth_error_server, "${error.code} · ${error.detail}")
        }
        is IOException -> context.getString(R.string.auth_error_offline)
        else -> context.getString(R.string.auth_error_server, error.message ?: error.javaClass.simpleName)
    }

    /** A `detail` written for a person, not a serialised list or object of field errors. */
    fun isSentence(detail: String): Boolean {
        val t = detail.trimStart()
        return t.isNotEmpty() && !t.startsWith("[") && !t.startsWith("{") && !t.startsWith("Erro ")
    }

    /** The local @ for an account that arrives from the server without one. */
    fun handleFrom(email: String): String =
        email.substringBefore('@').lowercase().filter { it.isLetterOrDigit() || it == '_' }.ifBlank { "tumtum" }
}
