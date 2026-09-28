package cc.tumtum.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConsentTextTest {

    @Test
    fun `the version is the contract's literal`() {
        assertEquals("2026-09-26.1", ConsentText.VERSION)
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

    @Test
    fun `what is missing frames every core key that is off, not only the one asked for`() {
        // 27/09: a night waiting for keep_night, on an account with read_heart_rate off.
        val granted = mapOf("terms" to true, "read_heart_rate" to false, "keep_night" to false)
        assertEquals(listOf("keep_night", "read_heart_rate"), ConsentText.missingFor("keep_night", granted))
        assertEquals(listOf("keep_night"), ConsentText.missingFor("keep_night", granted + ("read_heart_rate" to true)))
    }

    @Test
    fun `a night waiting to go up needs both keys the server takes it with`() {
        val granted = mapOf("terms" to true, "read_heart_rate" to true, "keep_night" to false)
        assertEquals(listOf("read_heart_rate", "keep_night"), ConsentText.missingFor("read_heart_rate", granted, keepingNight = true))
        assertEquals(listOf("read_heart_rate"), ConsentText.missingFor("read_heart_rate", granted))
    }
}
