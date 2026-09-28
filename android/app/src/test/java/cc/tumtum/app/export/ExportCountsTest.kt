package cc.tumtum.app.export

import org.junit.Assert.assertEquals
import org.junit.Test

/** What an export says about itself is counted from what it carries (28/09). */
class ExportCountsTest {

    @Test
    fun `coverage is seconds with a beat over seconds elapsed`() {
        // The export of 27/09: about 60 s of beats, a two-minute hole, 60 s more, in 250 s.
        val start = 1_790_627_293_061L
        val beats = (0 until 61).map { start + 1_400 + it * 1_000L } + (0 until 61).map { start + 185_000 + it * 1_000L }
        assertEquals(48, ExportCounts.coveragePct(beats, start, start + 251_981))
    }

    @Test
    fun `two beats in one second count that second once, and nothing outside counts`() {
        assertEquals(10, ExportCounts.coveragePct(listOf(0L, 500L, 999L, 20_000L), 0L, 10_000L))
        assertEquals(0, ExportCounts.coveragePct(emptyList(), 0L, 10_000L))
        assertEquals(0, ExportCounts.coveragePct(listOf(5L), 10L, 10L))
    }

    @Test
    fun `a connection line loses the strap's serial`() {
        assertEquals("status=0 device=Polar H10", ExportCounts.withoutSerial("status=0 device=Polar H10 19B38E3F"))
        assertEquals("attempt=2 autoConnect=true", ExportCounts.withoutSerial("attempt=2 autoConnect=true"))
    }
}
