package cc.tumtum.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConsentTextTest {

    @Test
    fun `the version is the contract's literal`() {
        assertEquals("2026-09-26", ConsentText.VERSION)
    }

    @Test
    fun `seven purposes, two for the core loop, five optional`() {
        assertEquals(7, ConsentText.PURPOSES.size)
        assertEquals(setOf("terms", "read_heart_rate"), ConsentText.CORE)
        assertEquals(
            listOf("keep_night", "crowd_stats", "artist_compare", "improve_detection", "marketing"),
            ConsentText.PURPOSES.filter { ConsentText.isOptional(it) },
        )
        assertFalse(ConsentText.isOptional("unknown"))
    }

    @Test
    fun `switches start from what the server granted and off for everything else`() {
        val start = ConsentText.startingSwitches(mapOf("terms" to true, "marketing" to false))
        assertEquals(ConsentText.PURPOSES.toSet(), start.keys)
        assertTrue(start.getValue("terms"))
        // Never pre-ticked by the app: not even the core ones.
        assertFalse(start.getValue("read_heart_rate"))
        assertTrue(ConsentText.PURPOSES.filter { ConsentText.isOptional(it) }.none { start.getValue(it) })
    }
}
