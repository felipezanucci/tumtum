package cc.tumtum.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The @ on the phone (28/09). b220 said "disponível" to every name but six
 * written into the app, and two accounts held @fezanu. What these defend is
 * the line under the field: it is true of the name on screen, and it never
 * says "disponível" without the server's yes for that very name.
 */
class UsernameTest {

    @Test
    fun `the field keeps lower-case letters, digits and underscore`() {
        assertEquals(Username.Edit("fe_zanu2", dropped = false), Username.edit("Fe_Zanu2"))
    }

    @Test
    fun `accents come off their letters instead of taking the letter away`() {
        assertEquals(Username.Edit("joao", dropped = false), Username.edit("João"))
        assertEquals(Username.Edit("conceicao", dropped = false), Username.edit("Conceição"))
    }

    @Test
    fun `a leading at sign goes without a word`() {
        assertEquals(Username.Edit("fezanu", dropped = false), Username.edit("@fezanu"))
    }

    @Test
    fun `anything else dropped is reported, so the screen can say it`() {
        assertEquals(Username.Edit("fezanu", dropped = true), Username.edit("fe zanu"))
        assertEquals(Username.Edit("fezanu", dropped = true), Username.edit("fe.zanu!"))
    }

    @Test
    fun `lengths the phone can judge alone`() {
        assertEquals(Username.Problem.TOO_SHORT, Username.problem("fe"))
        assertEquals(Username.Problem.TOO_LONG, Username.problem("a".repeat(21)))
        assertEquals(Username.Problem.BAD_CHARS, Username.problem("joão"))
        assertNull(Username.problem("fez"))
        assertNull(Username.problem("a".repeat(20)))
    }

    @Test
    fun `an empty field claims nothing`() {
        assertEquals(Username.Status.Empty, Username.status("", null))
    }

    @Test
    fun `a local rule is said before anything is asked`() {
        assertEquals(Username.Status.Local(Username.Problem.TOO_SHORT), Username.status("fe", null))
        // Even with an old yes for a longer name on record.
        assertEquals(
            Username.Status.Local(Username.Problem.TOO_SHORT),
            Username.status("fe", Username.Check.Available("fezanu")),
        )
    }

    @Test
    fun `no answer yet reads as checking, never as available`() {
        assertEquals(Username.Status.Checking, Username.status("fezanu", null))
        assertFalse(Username.confirmed("fezanu", null))
    }

    @Test
    fun `an answer about another name is no answer`() {
        // "fezan" was free; the person typed one more letter.
        val stale = Username.Check.Available("fezan")
        assertEquals(Username.Status.Checking, Username.status("fezanu", stale))
        assertFalse(Username.confirmed("fezanu", stale))
    }

    @Test
    fun `only the server's yes for this name is available`() {
        val yes = Username.Check.Available("fezanu")
        assertEquals(Username.Status.Available, Username.status("fezanu", yes))
        assertTrue(Username.confirmed("fezanu", yes))
    }

    @Test
    fun `the server's no is said in its own words`() {
        val no = Username.Check.Unavailable("fezanu", "Esse @ já tem dono. Tenta outro.")
        assertEquals(Username.Status.Unavailable("Esse @ já tem dono. Tenta outro."), Username.status("fezanu", no))
        assertFalse(Username.confirmed("fezanu", no))
    }

    @Test
    fun `a failed request is its own state, not a yes`() {
        val failed = Username.Check.Failed("fezanu")
        assertEquals(Username.Status.Failed, Username.status("fezanu", failed))
        assertFalse(Username.confirmed("fezanu", failed))
    }

    @Test
    fun `the server's refusals about the at are told from the rest`() {
        assertTrue(Username.isAboutUsername("Esse @ já tem dono. Tenta outro."))
        assertTrue(Username.isAboutUsername("O @ precisa de pelo menos 3 letras ou números."))
        assertTrue(Username.isAboutUsername("Esse @ é da TumTum."))
        assertTrue(Username.isAboutUsername("O @ é fixo: escolhido uma vez, não muda."))
        assertFalse(Username.isAboutUsername("Email já cadastrado"))
        assertFalse(Username.isAboutUsername("Você precisa ter 18 anos ou mais para usar a TumTum."))
        assertFalse(Username.isAboutUsername(null))
    }
}
