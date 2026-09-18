package cc.tumtum.capture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Every timestamp the native screens draw passes through here: the curve's x
 * axis, the moment a peak happened, the start of a session. A parser that is
 * wrong by a factor of a thousand does not crash — it draws a plausible
 * picture of the wrong night, which is the failure this project keeps finding.
 */
class Iso8601Test {

    /** 2026-08-29T22:30:00Z. The festival, in round numbers. */
    private val festival = 1788042600_000L

    @Test
    fun `reads the Z form`() {
        assertEquals(festival, Iso8601.toMillis("2026-08-29T22:30:00Z"))
    }

    @Test
    fun `reads an explicit offset`() {
        assertEquals(festival, Iso8601.toMillis("2026-08-29T22:30:00+00:00"))
        // São Paulo, where the show actually is.
        assertEquals(festival, Iso8601.toMillis("2026-08-29T19:30:00-03:00"))
    }

    /**
     * Postgres returns microseconds and this app writes milliseconds. A
     * fixed-width pattern reads one of them correctly and the other by a
     * factor of a thousand.
     */
    @Test
    fun `reads any width of fractional second`() {
        assertEquals(festival + 123L, Iso8601.toMillis("2026-08-29T22:30:00.123Z"))
        assertEquals(festival + 123L, Iso8601.toMillis("2026-08-29T22:30:00.123456Z"))
        assertEquals(festival + 100L, Iso8601.toMillis("2026-08-29T22:30:00.1Z"))
        assertEquals(festival, Iso8601.toMillis("2026-08-29T22:30:00.000000Z"))
    }

    /** A timestamp that lost its offset means UTC everywhere in this project. */
    @Test
    fun `reads a naive timestamp as UTC rather than dropping it`() {
        assertEquals(festival, Iso8601.toMillis("2026-08-29T22:30:00"))
        assertEquals(festival + 456L, Iso8601.toMillis("2026-08-29T22:30:00.456"))
    }

    @Test
    fun `surrounding whitespace does not defeat it`() {
        assertEquals(festival, Iso8601.toMillis("  2026-08-29T22:30:00Z  "))
    }

    @Test
    fun `anything that is not a timestamp reads as absent, not as the epoch`() {
        assertNull(Iso8601.toMillis(null))
        assertNull(Iso8601.toMillis(""))
        assertNull(Iso8601.toMillis("   "))
        assertNull(Iso8601.toMillis("ontem à noite"))
        assertNull(Iso8601.toMillis("2026-08-29"))
    }
}
