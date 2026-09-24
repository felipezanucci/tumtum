package cc.tumtum.app.export

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The share menu's networks (#61, 24/09): two to a row, and never a hole
 * beside a lone button.
 */
class ShareGridTest {

    private fun halves(n: Int) = List(n) { true }

    @Test
    fun `an even count fills every row in pairs`() {
        assertEquals(listOf(listOf(0, 1), listOf(2, 3), listOf(4, 5)), ShareGrid.rows(halves(6)))
    }

    @Test
    fun `the last of an odd count takes the whole row`() {
        // b187 on Felipe's phone: Instagram, Facebook, Snapchat, TikTok, WhatsApp.
        assertEquals(listOf(listOf(0, 1), listOf(2, 3), listOf(4)), ShareGrid.rows(halves(5)))
    }

    @Test
    fun `a label too long for half a row takes the whole row`() {
        // Five that fit, then "Status do WhatsApp" on a narrow phone.
        val rows = ShareGrid.rows(listOf(true, true, true, true, true, false))
        assertEquals(listOf(listOf(0, 1), listOf(2, 3), listOf(4), listOf(5)), rows)
    }

    @Test
    fun `a button left before a long label is not left beside a hole`() {
        assertEquals(listOf(listOf(0), listOf(1), listOf(2)), ShareGrid.rows(listOf(true, false, true)))
        assertEquals(listOf(listOf(0), listOf(1, 2)), ShareGrid.rows(listOf(false, true, true)))
    }

    @Test
    fun `one button, or none`() {
        assertEquals(listOf(listOf(0)), ShareGrid.rows(halves(1)))
        assertEquals(emptyList<List<Int>>(), ShareGrid.rows(emptyList()))
    }

    @Test
    fun `the order is never changed and no row holds more than two`() {
        val patterns = (0 until 64).map { bits -> List(6) { i -> (bits shr i) and 1 == 1 } }
        for (fits in patterns) {
            val rows = ShareGrid.rows(fits)
            assertEquals(fits.indices.toList(), rows.flatten())
            assertTrue(rows.all { it.size in 1..2 })
            // A pair only ever holds two labels that fit in half.
            assertTrue(rows.filter { it.size == 2 }.all { (a, b) -> fits[a] && fits[b] })
        }
    }
}
