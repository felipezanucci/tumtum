package cc.tumtum.app.export

import cc.tumtum.app.domain.HrSample
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The card's line (#61): smoother, but never across a gap and never below the peak. */
class CurvePathTest {

    private val start = Instant.parse("2026-10-26T22:00:00Z")

    private fun night(seconds: IntRange, bpm: (Int) -> Int) =
        seconds.map { HrSample(start.plusSeconds(it.toLong()), bpm(it)) }

    @Test
    fun `two hours of 1 Hz become about a hundred and forty points`() {
        val samples = night(0 until 7200) { 100 + (it % 7) }
        val points = CurvePath.segments(samples, start, start.plusSeconds(7200)).flatten()

        assertTrue(points.size in 120..160)
    }

    @Test
    fun `a gap stays a gap`() {
        val before = night(0 until 600) { 110 }
        val after = night(1200 until 1800) { 120 }
        val segments = CurvePath.segments(before + after, start, start.plusSeconds(1800))

        assertEquals(2, segments.size)
        assertTrue(segments[0].last().time.isBefore(start.plusSeconds(600)))
        assertTrue(!segments[1].first().time.isBefore(start.plusSeconds(1200)))
    }

    @Test
    fun `a strap taken off for fifty seconds is a gap on the card too`() {
        val before = night(0 until 40) { 75 }
        val after = night(90 until 130) { 70 }
        val segments = CurvePath.segments(before + after, start, start.plusSeconds(130))

        assertEquals(2, segments.size)
    }

    @Test
    fun `the peak survives the averaging, at its own time`() {
        val samples = night(0 until 3600) { if (it == 1800) 187 else 120 }
        val points = CurvePath.segments(samples, start, start.plusSeconds(3600)).flatten()

        val top = points.maxBy { it.bpm }
        assertEquals(187f, top.bpm)
        assertEquals(start.plusSeconds(1800), top.time)
    }

    @Test
    fun `the noise is averaged out`() {
        val samples = night(0 until 3600) { if (it % 2 == 0) 110 else 130 }
        val points = CurvePath.segments(samples, start, start.plusSeconds(3600)).flatten()

        // The first point is the peak's bucket (130); the rest sit near 120.
        assertTrue(points.drop(1).all { it.bpm in 118f..122f })
    }

    @Test
    fun `nothing measured draws nothing`() {
        assertEquals(emptyList<List<CurvePath.Point>>(), CurvePath.segments(emptyList(), start, start.plusSeconds(60)))
    }
}
