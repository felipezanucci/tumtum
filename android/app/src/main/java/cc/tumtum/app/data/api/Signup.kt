package cc.tumtum.app.data.api

import org.json.JSONObject

/**
 * The answer to the first step of an account (#64, 24/09): where the code
 * went, how long it lives, and how long before another may be asked for.
 * The server's own numbers, so the screen never promises a window the
 * server does not keep.
 */
data class SignupStarted(
    val email: String,
    val expiresInSeconds: Int,
    val resendAfterSeconds: Int,
) {
    val expiresInMinutes: Int get() = (expiresInSeconds + 59) / 60

    companion object {
        fun from(json: JSONObject, asked: String) = SignupStarted(
            email = json.optString("email", "").ifBlank { asked },
            expiresInSeconds = json.optInt("expires_in_seconds", 15 * 60),
            resendAfterSeconds = json.optInt("resend_after_seconds", 60),
        )
    }
}

/** The six digits a person types back from the e-mail. Pure, so it is tested without a device. */
object SignupCode {
    const val LENGTH = 6

    /**
     * What the field keeps of what was typed or pasted: ASCII digits, at most
     * six. A paste of the whole subject line ("390875 é seu código…") keeps
     * the code; a space or a dash from a mail client is dropped.
     */
    fun digits(input: String): String = input.filter { it in '0'..'9' }.take(LENGTH)

    fun isComplete(code: String): Boolean = code.length == LENGTH && code.all { it in '0'..'9' }

    /** Whole seconds before another code may be asked for, rounded up; 0 means now. */
    fun secondsUntil(resendAtMs: Long, nowMs: Long): Int =
        if (nowMs >= resendAtMs) 0 else ((resendAtMs - nowMs + 999) / 1000).toInt()
}
