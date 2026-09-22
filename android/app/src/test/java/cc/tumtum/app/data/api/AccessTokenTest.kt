package cc.tumtum.app.data.api

import cc.tumtum.app.data.prefs.Session
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Base64

/**
 * The app must know whether its own token is alive before it relies on it —
 * a dead token discovered at the end of a night is the failure this exists
 * to prevent.
 */
class AccessTokenTest {

    private fun jwt(payload: String): String {
        val enc = Base64.getUrlEncoder().withoutPadding()
        val header = enc.encodeToString("""{"alg":"HS256","typ":"JWT"}""".toByteArray())
        val body = enc.encodeToString(payload.toByteArray())
        return "$header.$body.signature"
    }

    @Test
    fun `reads the expiry in millis`() {
        assertEquals(1_800_000_000_000L, AccessToken.expiresAtMillis(jwt("""{"sub":"u1","exp":1800000000}""")))
    }

    @Test
    fun `expired when now is at or past exp`() {
        val token = jwt("""{"exp":1800000000}""")
        assertTrue(AccessToken.isExpired(token, 1_800_000_000_000L))
        assertTrue(AccessToken.isExpired(token, 1_800_000_001_000L))
        assertFalse(AccessToken.isExpired(token, 1_799_999_999_000L))
    }

    @Test
    fun `a token without a readable expiry is treated as live`() {
        assertNull(AccessToken.expiresAtMillis("not-a-jwt"))
        assertFalse(AccessToken.isExpired("not-a-jwt", Long.MAX_VALUE))
        assertNull(AccessToken.expiresAtMillis(jwt("""{"sub":"u1"}""")))
    }

    @Test
    fun `remaining time never goes negative`() {
        val token = jwt("""{"exp":1800000000}""")
        assertEquals(0L, AccessToken.remainingMillis(token, 1_900_000_000_000L))
        assertEquals(1_000L, AccessToken.remainingMillis(token, 1_799_999_999_000L))
    }

    @Test
    fun `without a refresh token a session is live exactly while its token is`() {
        val session = Session(token = jwt("""{"exp":1800000000}"""), userId = "u1")
        assertTrue(session.isLive(1_700_000_000_000L))
        assertFalse(session.isLive(1_800_000_000_000L))
    }

    @Test
    fun `with a refresh token an expired hour is not an expired session`() {
        // #34, 22/09: the access token lasts an hour now. Judged by it alone,
        // every screen would announce "sessão expirada" sixty minutes after
        // sign-in while the app could renew it without a word.
        val session = Session(token = jwt("""{"exp":1800000000}"""), userId = "u1", refreshToken = "r".repeat(43))
        assertTrue(session.isLive(1_900_000_000_000L))
    }
}
