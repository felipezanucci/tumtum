package cc.tumtum.capture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Base64

/**
 * The bug these cover cost a whole afternoon two days before the first real
 * capture: the app hid its login row whenever a token was *stored*, and an
 * expired token is still a stored token. So it presented itself as signed in,
 * captured happily, and only failed at the upload — with no way back.
 */
class AccessTokenTest {

    /** A token shaped like a real one. The signature is never inspected here. */
    private fun jwt(payload: String): String {
        val encoder = Base64.getUrlEncoder().withoutPadding()
        val header = encoder.encodeToString("""{"alg":"HS256","typ":"JWT"}""".toByteArray())
        val body = encoder.encodeToString(payload.toByteArray())
        return "$header.$body.not-a-real-signature"
    }

    @Test
    fun `reads the expiry the token carries`() {
        val token = jwt("""{"sub":"3f2b","exp":1788042600}""")
        assertEquals(1788042600_000L, AccessToken.expiresAtMillis(token))
    }

    @Test
    fun `a token past its expiry is expired`() {
        val token = jwt("""{"sub":"3f2b","exp":1788042600}""")
        assertTrue(AccessToken.isExpired(token, 1788042601_000L))
        assertFalse(AccessToken.isExpired(token, 1788042599_000L))
    }

    @Test
    fun `the exact instant of expiry counts as expired`() {
        val token = jwt("""{"exp":1788042600}""")
        assertTrue(AccessToken.isExpired(token, 1788042600_000L))
    }

    @Test
    fun `reports how long is left, which is what warns somebody before a show`() {
        val token = jwt("""{"exp":1788042600}""")
        assertEquals(60_000L, AccessToken.remainingMillis(token, 1788042540_000L))
    }

    @Test
    fun `never reports negative time remaining`() {
        val token = jwt("""{"exp":1788042600}""")
        assertEquals(0L, AccessToken.remainingMillis(token, 1788142600_000L))
    }

    @Test
    fun `a token with no exp claim has no readable expiry`() {
        val token = jwt("""{"sub":"3f2b"}""")
        assertNull(AccessToken.expiresAtMillis(token))
    }

    /**
     * The server is the authority on what a token is worth. Refusing one we
     * merely failed to read would lock somebody out of a working account,
     * which is worse than letting the upload discover it.
     */
    @Test
    fun `an unreadable token is treated as live rather than dead`() {
        assertFalse(AccessToken.isExpired("not a jwt at all", 1788042600_000L))
        assertFalse(AccessToken.isExpired("", 1788042600_000L))
        assertFalse(AccessToken.isExpired("only.two", 1788042600_000L))
        assertNull(AccessToken.expiresAtMillis("%%%.%%%.%%%"))
    }
}
