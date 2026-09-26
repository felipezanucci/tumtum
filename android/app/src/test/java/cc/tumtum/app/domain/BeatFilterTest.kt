package cc.tumtum.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.roundToInt

/**
 * The shape of night 18 (25/09, b198): a Polar H10 worn for about a minute,
 * then taken off. The strap flagged "contact not supported" in every packet;
 * what changed when it came off was the R-R.
 *
 * The fixture **mirrors that export's shape without carrying it** (LGPD
 * remediation, 26/09): until then it held the 67 real readings of the
 * founder's night, in a public repository. What the rule needs is only the
 * shape — a run on the skin where every reading carries an R-R, then the last
 * value repeated nine times without one, then zeros until the strap gave up —
 * so the values below are generated: a plausible resting heart drifting
 * between the low 60s and the low 80s, the same length as the original.
 */
class BeatFilterTest {

    /** 67 readings worn, every one with an R-R — synthetic, a slow rise and fall around 72 bpm. */
    private val worn: List<Int> = (0 until 67).map { i ->
        (72 + 9 * kotlin.math.sin(i / 10.0) + ((i * 7) % 3 - 1)).roundToInt()
    }

    private fun night18(): List<RawReading> {
        val readings = mutableListOf<RawReading>()
        worn.forEach { readings += RawReading(readings.size * 1_000L, it, 0, hasRr = true) }
        // Off the skin: the last value, nine times, with no R-R…
        repeat(9) { readings += RawReading(readings.size * 1_000L, worn.last(), 0, hasRr = false) }
        // …then 0, eighteen times, until it disconnected on its own.
        repeat(18) { readings += RawReading(readings.size * 1_000L, 0, 0, hasRr = false) }
        return readings
    }

    @Test
    fun `the synthetic run stays in a resting heart's range`() {
        assertEquals(67, worn.size)
        assertTrue(worn.all { it in 55..95 })
    }

    @Test
    fun `night 18 keeps the minute worn and nothing after`() {
        val beats = BeatFilter.beats(night18())
        assertEquals(worn, beats.map { it.bpm })
        assertEquals(66_000L, beats.last().timeMs)
    }

    @Test
    fun `live, the number goes after two readings and the zeros never show`() {
        val tracker = OnSkinTracker()
        val accepted = night18().map { tracker.accept(it.bpm, it.contactStatus, it.hasRr) }
        assertTrue(accepted.take(worn.size).all { it })
        // The first two of the run cannot be taken back while they arrive.
        assertEquals(listOf(true, true), accepted.subList(worn.size, worn.size + 2))
        assertTrue(accepted.drop(worn.size + 2).none { it })
        assertTrue(tracker.proven)
    }

    @Test
    fun `a slow heart skips an R-R now and then, and stays`() {
        // At 40 bpm a beat lands every 1.5 s, so one packet in three can carry none.
        val readings = (0 until 30).map { i -> RawReading(i * 1_000L, 40, 0, hasRr = i % 3 != 1) }
        assertEquals(30, BeatFilter.beats(readings).size)
        val tracker = OnSkinTracker()
        assertTrue(readings.all { tracker.accept(it.bpm, it.contactStatus, it.hasRr) })
    }

    @Test
    fun `two readings without R-R stay, three go`() {
        val pattern = listOf(true, false, false, true, false, false, false, true)
        val readings = pattern.mapIndexed { i, rr -> RawReading(i * 1_000L, 90, 0, rr) }
        assertEquals(listOf(0L, 1_000L, 2_000L, 3_000L, 7_000L), BeatFilter.beats(readings).map { it.timeMs })
    }

    @Test
    fun `a sensor that never sends R-R is believed, within a heart's range`() {
        val readings = listOf(0, 72, 74, 300, 75).mapIndexed { i, bpm -> RawReading(i * 1_000L, bpm, 0, hasRr = false) }
        assertEquals(listOf(72, 74, 75), BeatFilter.beats(readings).map { it.bpm })
    }

    @Test
    fun `without R-R, the setup waits twenty readings before it believes`() {
        val tracker = OnSkinTracker()
        repeat(BeatFilter.NO_RR_PROOF - 1) { assertTrue(tracker.accept(80, 0, hasRr = false)) }
        assertFalse(tracker.proven)
        tracker.accept(80, 0, hasRr = false)
        assertTrue(tracker.proven)
    }

    @Test
    fun `zeros do not prove anything`() {
        val tracker = OnSkinTracker()
        repeat(40) { assertFalse(tracker.accept(0, 0, hasRr = false)) }
        assertFalse(tracker.proven)
    }

    @Test
    fun `a sensor that says no contact is believed, R-R or not`() {
        val readings = listOf(RawReading(0, 80, 3, true), RawReading(1_000, 80, 2, true))
        assertEquals(listOf(0L), BeatFilter.beats(readings).map { it.timeMs })
        assertFalse(OnSkinTracker().accept(80, 2, hasRr = true))
    }
}
