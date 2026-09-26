package cc.tumtum.app.domain

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * The card's title, as the backend's `moment_copy()` builds it
 * (backend/tests/test_card_curve.py): a line only when the night's own
 * numbers prove it.
 */
class CardCopyTest {

    @Test
    fun `a rise above the night's average names both`() {
        assertEquals(CardCopy.Title.AboveAverage(78, "01H24"), CardCopy.title(116, 78, "01h24"))
    }

    @Test
    fun `no average, then only the moment`() {
        assertEquals(CardCopy.Title.HeartAt("01H24"), CardCopy.title(116, null, "01h24"))
    }

    @Test
    fun `a peak under the average promises no rise`() {
        assertEquals(CardCopy.Title.HeartAt("01H24"), CardCopy.title(70, 78, "01h24"))
    }

    @Test
    fun `a peak equal to the average promises no rise either`() {
        assertEquals(CardCopy.Title.HeartAt("01H24"), CardCopy.title(78, 78, "01h24"))
    }

    @Test
    fun `a hidden hour leaves no time in the title`() {
        val title = CardCopy.title(116, 78, null)
        assertEquals(CardCopy.Title.AboveAverage(78, null), title)
        assertEquals(CardCopy.Title.HeartAt(null), CardCopy.title(116, null, "  "))
    }

    @Test
    fun `a night with no moment says so, and nothing about a mood`() {
        assertEquals(CardCopy.Title.NoMoments, CardCopy.title(90, 80, "22h00", hasMoments = false))
    }

    @Test
    fun `the average is the rounded mean of the samples`() {
        val t = Instant.parse("2026-09-26T22:00:00Z")
        val samples = listOf(70, 71, 72, 80).mapIndexed { i, bpm -> HrSample(t.plusSeconds(i.toLong()), bpm) }
        assertEquals(73, CardCopy.averageBpm(samples)) // 73.25
        assertNull(CardCopy.averageBpm(emptyList()))
    }

    @Test
    fun `bpm exato off goes to the ten below, with a plus`() {
        assertEquals("110+", CardCopy.bpmLabel(116, exact = false))
        assertEquals("110+", CardCopy.bpmLabel(110, exact = false))
        assertEquals("180+", CardCopy.bpmLabel(187, exact = false))
        assertEquals("116", CardCopy.bpmLabel(116, exact = true))
    }
}
