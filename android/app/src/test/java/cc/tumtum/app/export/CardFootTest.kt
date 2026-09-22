package cc.tumtum.app.export

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The card's foot (version A2, 22/09): the event's name, and how much of it fits. */
class CardFootTest {

    // One unit per character: enough to test the rule, independent of fonts.
    private val perChar: (String) -> Float = { it.length.toFloat() }

    @Test
    fun `a short name shares the line with the time and the wordmark`() {
        assertFalse(CardFoot.ownRow("ENSAIO3"))
    }

    @Test
    fun `a real event's name takes its own row rather than being cut to four letters`() {
        assertTrue(CardFoot.ownRow("SÃO PAULO × VITÓRIA"))
    }

    @Test
    fun `a name that fits is left alone`() {
        assertEquals("ENSAIO3", CardFoot.fit("ENSAIO3", 20f, perChar))
    }

    @Test
    fun `a name that does not fit is cut with an ellipsis and never emptied`() {
        val cut = CardFoot.fit("TESTE - MADONNA - CONFESSIONS 2", 10f, perChar)

        assertTrue(cut.endsWith("…"))
        assertTrue(cut.length <= 10)
        assertTrue(cut.length > 1)
    }
}
