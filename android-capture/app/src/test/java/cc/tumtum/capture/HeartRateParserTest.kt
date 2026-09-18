package cc.tumtum.capture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * These run on a laptop, with no phone and no sensor. The parser is the one
 * piece of this app that can be proven correct without either, so it is.
 */
class HeartRateParserTest {

    private fun bytes(vararg values: Int) = ByteArray(values.size) { values[it].toByte() }

    @Test
    fun `reads the common case, one byte of rate and nothing else`() {
        // flags 0x00: 8-bit rate, no contact reporting, no energy, no intervals.
        val m = HeartRateParser.parse(bytes(0x00, 72))!!
        assertEquals(72, m.bpm)
        assertTrue(m.rrIntervalsMs.isEmpty())
        assertNull(m.contact)
    }

    @Test
    fun `reads a rate above 255, which needs two bytes`() {
        // flags 0x01: 16-bit rate. 0x012C little-endian is 300 — out of range —
        // so use 0x00B4 = 180, a rate a person actually reaches at a show.
        val m = HeartRateParser.parse(bytes(0x01, 0xB4, 0x00))!!
        assertEquals(180, m.bpm)
    }

    @Test
    fun `reports contact only when the sensor says it supports it`() {
        // 0x04 supported, 0x02 detected.
        assertEquals(true, HeartRateParser.parse(bytes(0x06, 70))!!.contact)
        assertEquals(false, HeartRateParser.parse(bytes(0x04, 70))!!.contact)
        // Bit 0x02 alone means nothing without 0x04, and must not be read as false.
        assertNull(HeartRateParser.parse(bytes(0x02, 70))!!.contact)
    }

    @Test
    fun `converts intervals from 1024ths of a second to milliseconds`() {
        // flags 0x10: intervals present. 1024 sixteenths is exactly one second.
        val m = HeartRateParser.parse(bytes(0x10, 60, 0x00, 0x04))!!
        assertEquals(listOf(1000), m.rrIntervalsMs)
    }

    @Test
    fun `steps over energy expended to reach the intervals behind it`() {
        // flags 0x18: energy present AND intervals present. Reading the energy
        // bytes as an interval is the classic way to get this wrong.
        val m = HeartRateParser.parse(bytes(0x18, 60, 0xFF, 0xFF, 0x00, 0x04))!!
        assertEquals(listOf(1000), m.rrIntervalsMs)
    }

    @Test
    fun `reads every interval in a notification, not just the first`() {
        val m = HeartRateParser.parse(bytes(0x10, 60, 0x00, 0x04, 0x00, 0x03))!!
        assertEquals(2, m.rrIntervalsMs.size)
        assertEquals(1000, m.rrIntervalsMs[0])
        assertEquals(750, m.rrIntervalsMs[1])
    }

    @Test
    fun `drops intervals outside what the backend will store`() {
        // 0x0010 = 16/1024 s = 16ms, far below a real beat.
        val m = HeartRateParser.parse(bytes(0x10, 60, 0x10, 0x00, 0x00, 0x04))!!
        assertEquals(listOf(1000), m.rrIntervalsMs)
    }

    @Test
    fun `refuses a rate outside what a person has`() {
        assertNull(HeartRateParser.parse(bytes(0x00, 20)))
        assertNull(HeartRateParser.parse(bytes(0x00, 0xFF)))
    }

    @Test
    fun `refuses a notification too short to hold a reading`() {
        assertNull(HeartRateParser.parse(bytes(0x00)))
        assertNull(HeartRateParser.parse(ByteArray(0)))
        // Claims a 16-bit rate but carries only one byte of it.
        assertNull(HeartRateParser.parse(bytes(0x01, 0xB4)))
    }

    @Test
    fun `survives a truncated interval at the end`() {
        // One stray byte where an interval should be: taken, it would be read
        // as a nonsense value rather than ignored.
        val m = HeartRateParser.parse(bytes(0x10, 60, 0x00, 0x04, 0x33))!!
        assertEquals(listOf(1000), m.rrIntervalsMs)
    }
}
