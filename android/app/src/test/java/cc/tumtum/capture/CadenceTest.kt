package cc.tumtum.capture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The cadence measurement is what keeps the watch-import screen honest: it
 * decides whether the screen says "dá para procurar seus momentos" or the
 * quieter truth. A measurement that flatters sparse data would be the app
 * promising a result it cannot deliver — the exact bug class this project
 * keeps finding, moved one layer down.
 */
class CadenceTest {

    private fun seconds(vararg s: Long): List<Long> = s.map { it * 1000 }

    @Test
    fun `nothing to measure is null, not a zero cadence`() {
        assertNull(Cadence.of(emptyList()))
    }

    @Test
    fun `a single reading has no span and never counts as dense`() {
        val cadence = Cadence.of(seconds(100))!!
        assertEquals(1, cadence.count)
        assertEquals(0L, cadence.spanSeconds)
        assertFalse(cadence.denseEnough)
    }

    @Test
    fun `one reading per second is dense`() {
        val cadence = Cadence.of((0L until 600L).map { it * 1000 })!!
        assertEquals(600, cadence.count)
        assertEquals(1_000L, cadence.medianGapMillis)
        assertTrue(cadence.coverage > 0.99)
        assertTrue(cadence.denseEnough)
    }

    @Test
    fun `one reading every ten minutes is not dense`() {
        // Six hours of Samsung's every-10-minutes setting: 37 points.
        val cadence = Cadence.of((0L..36L).map { it * 600_000 })!!
        assertEquals(37, cadence.count)
        assertEquals(600_000L, cadence.medianGapMillis)
        assertFalse(cadence.denseEnough)
    }

    @Test
    fun `dense clusters around dead half-hours have a good median and are still not dense`() {
        // Ten minutes at 1 Hz, thirty dead minutes, ten more at 1 Hz: the
        // median gap is a lovely second, and the middle of the night is gone.
        val times = (0L until 600L).map { it * 1000 } +
            (2400L until 3000L).map { it * 1000 }
        val cadence = Cadence.of(times)!!
        assertEquals(1_000L, cadence.medianGapMillis)
        assertTrue(cadence.coverage < 0.8)
        assertFalse(cadence.denseEnough)
    }

    @Test
    fun `a metronome slower than the detector's window is not dense either`() {
        // Every 9 seconds forever: high coverage of the night in wall-clock
        // terms, but the 5-second smoothing window mostly sees one point.
        val cadence = Cadence.of((0L..400L).map { it * 9_000 })!!
        assertEquals(9_000L, cadence.medianGapMillis)
        assertFalse(cadence.denseEnough)
    }

    @Test
    fun `unsorted input measures the same night`() {
        val cadence = Cadence.of(seconds(30, 10, 20, 0, 40))!!
        assertEquals(5, cadence.count)
        assertEquals(40L, cadence.spanSeconds)
        assertEquals(10_000L, cadence.medianGapMillis)
    }

    @Test
    fun `span reads as hours and minutes`() {
        assertEquals("4h30", Cadence(2, 16_200, 1000, 1.0).spanLabel)
        assertEquals("25 min", Cadence(2, 1_500, 1000, 1.0).spanLabel)
    }
}
