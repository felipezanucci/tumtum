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

    @Test
    fun `a long name takes two lines before it loses a letter`() {
        val lines = CardFoot.wrap("TESTE - MADONNA - CONFESSIONS", 18f, measure = perChar)

        assertEquals(listOf("TESTE - MADONNA -", "CONFESSIONS"), lines)
    }

    @Test
    fun `a name too long for two lines is cut on the second`() {
        val lines = CardFoot.wrap("UM NOME MUITO MUITO MUITO LONGO DEMAIS PRA CABER", 12f, measure = perChar)

        assertEquals(2, lines.size)
        assertTrue(lines.last().endsWith("…"))
        assertTrue(lines.all { it.length <= 12 })
    }

    @Test
    fun `a short name stays on one line`() {
        assertEquals(listOf("SÃO PAULO × VITÓRIA"), CardFoot.wrap("SÃO PAULO × VITÓRIA", 40f, measure = perChar))
    }

    @Test
    fun `one word longer than the line is cut, never dropped`() {
        val lines = CardFoot.wrap("SUPERCALIFRAGILISTICO", 8f, measure = perChar)

        assertEquals(1, lines.size)
        assertTrue(lines.single().endsWith("…"))
    }
}
