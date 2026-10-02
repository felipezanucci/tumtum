package cc.tumtum.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class GapLabelTest {
    private val t0 = Instant.parse("2026-10-02T17:05:00Z")
    private fun gap(fromSec: Long, toSec: Long) = Gap(t0.plusSeconds(fromSec), t0.plusSeconds(toSec))

    @Test
    fun `the whole time is said, minutes and seconds`() {
        // Night 8, 02/10: a 93-second dropout read "1 MIN SEM DADO".
        assertEquals(GapLabel.Words.MinutesSeconds(1, 33), GapLabel.words(93))
        assertEquals(GapLabel.Words.Seconds(45), GapLabel.words(45))
        assertEquals(GapLabel.Words.Minutes(12), GapLabel.words(720))
    }

    @Test
    fun `several gaps add up, and the label sits under the biggest`() {
        val gaps = listOf(gap(60, 90), gap(500, 593))
        assertEquals(123L, GapLabel.totalSeconds(gaps))
        // 660 s of night; the biggest gap's middle is at 546.5 s.
        assertEquals(546.5f / 660f, GapLabel.anchor(gaps, t0, t0.plusSeconds(660))!!, 0.001f)
    }

    @Test
    fun `no gap, no label`() {
        assertNull(GapLabel.anchor(emptyList(), t0, t0.plusSeconds(600)))
    }
}
