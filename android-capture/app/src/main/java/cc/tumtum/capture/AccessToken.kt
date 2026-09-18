package cc.tumtum.capture

import org.json.JSONObject
import java.util.Base64

/**
 * What the app can know about its own token without asking anybody.
 *
 * A JWT carries its own expiry, so the app can tell a live session from a dead
 * one *before* someone straps a sensor on — rather than discovering it at the
 * end of a six-hour capture, with no way back to a login screen. That is not a
 * hypothetical: it happened on 2026-08-27, one day after signing in, and left
 * the app holding readings it could never upload.
 *
 * Nothing here is a security check. The server decides what a token is worth
 * and will refuse an expired one regardless. This only decides what the screen
 * should say and when it should ask for a password again.
 */
object AccessToken {

    /** Expiry instant in millis, or null when the token carries no readable one. */
    fun expiresAtMillis(jwt: String): Long? {
        val parts = jwt.split('.')
        if (parts.size < 2) return null

        val payload = try {
            String(Base64.getUrlDecoder().decode(parts[1]), Charsets.UTF_8)
        } catch (e: Exception) {
            return null
        }

        val seconds = try {
            JSONObject(payload).optLong("exp", 0L)
        } catch (e: Exception) {
            return null
        }

        // `exp` is seconds since the epoch, per RFC 7519.
        return if (seconds > 0L) seconds * 1000L else null
    }

    /**
     * A token whose expiry cannot be read is treated as live.
     *
     * The server is the authority. Refusing to use a token we merely failed to
     * parse would lock somebody out of an account that works perfectly well,
     * which is a worse failure than letting the upload find out for itself.
     */
    fun isExpired(jwt: String, nowMillis: Long): Boolean {
        val expiresAt = expiresAtMillis(jwt) ?: return false
        return nowMillis >= expiresAt
    }

    /**
     * Millis left before the token dies, or null when there is no readable
     * expiry. Used to warn somebody whose session will not outlive the show
     * they are about to record.
     */
    fun remainingMillis(jwt: String, nowMillis: Long): Long? =
        expiresAtMillis(jwt)?.let { (it - nowMillis).coerceAtLeast(0L) }
}
