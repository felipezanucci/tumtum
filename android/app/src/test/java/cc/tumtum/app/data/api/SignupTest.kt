package cc.tumtum.app.data.api

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The sign-up code (#64, 24/09): what the field keeps, and what the server said. */
class SignupTest {

    @Test
    fun `the field keeps six digits and nothing else`() {
        assertEquals("123456", SignupCode.digits("123 456"))
        assertEquals("123456", SignupCode.digits("123-456"))
        assertEquals("123456", SignupCode.digits("1234567"))
        assertEquals("012345", SignupCode.digits("012345"))
        assertEquals("", SignupCode.digits("abc"))
    }

    @Test
    fun `a pasted subject line keeps the code`() {
        assertEquals("390875", SignupCode.digits("390875 é seu código da TumTum"))
    }

    @Test
    fun `only six ASCII digits are a code`() {
        assertTrue(SignupCode.isComplete("012345"))
        assertFalse(SignupCode.isComplete("12345"))
        assertFalse(SignupCode.isComplete("１２３４５６"))
    }

    @Test
    fun `the wait to ask again rounds up and never goes negative`() {
        assertEquals(60, SignupCode.secondsUntil(resendAtMs = 60_000, nowMs = 0))
        assertEquals(1, SignupCode.secondsUntil(resendAtMs = 60_000, nowMs = 59_500))
        assertEquals(0, SignupCode.secondsUntil(resendAtMs = 60_000, nowMs = 60_000))
        assertEquals(0, SignupCode.secondsUntil(resendAtMs = 60_000, nowMs = 90_000))
    }

    @Test
    fun `the server's numbers are read, and missing ones fall back`() {
        val said = SignupStarted.from(
            JSONObject("""{"email":"Ana@x.cc","expires_in_seconds":900,"resend_after_seconds":60}"""),
            asked = "ana@X.cc",
        )
        assertEquals(SignupStarted("Ana@x.cc", 900, 60), said)
        assertEquals(15, said.expiresInMinutes)

        val bare = SignupStarted.from(JSONObject("{}"), asked = "ana@x.cc")
        assertEquals("ana@x.cc", bare.email)
        assertEquals(60, bare.resendAfterSeconds)
    }
}
