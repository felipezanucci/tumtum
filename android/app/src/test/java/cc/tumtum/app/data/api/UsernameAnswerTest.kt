package cc.tumtum.app.data.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** `GET /api/auth/username/{name}` (28/09): "disponível" only on an explicit yes. */
class UsernameAnswerTest {

    @Test
    fun `a free name`() {
        val a = UsernameAnswer.parse("""{"username":"fezanu","available":true,"reason":null}""", asked = "fezanu")
        assertEquals(UsernameAnswer("fezanu", available = true, reason = null), a)
    }

    @Test
    fun `a taken name carries the server's sentence`() {
        val a = UsernameAnswer.parse(
            """{"username":"fezanu","available":false,"reason":"Esse @ já tem dono. Tenta outro."}""",
            asked = "fezanu",
        )
        assertFalse(a.available)
        assertEquals("Esse @ já tem dono. Tenta outro.", a.reason)
    }

    @Test
    fun `an answer that does not say yes is a no`() {
        val a = UsernameAnswer.parse("""{"username":"fezanu"}""", asked = "fezanu")
        assertFalse(a.available)
        assertNull(a.reason)
    }

    @Test
    fun `the name asked stands in when the server does not echo it`() {
        val a = UsernameAnswer.parse("""{"available":true}""", asked = "fezanu")
        assertEquals("fezanu", a.username)
        assertTrue(a.available)
    }
}
