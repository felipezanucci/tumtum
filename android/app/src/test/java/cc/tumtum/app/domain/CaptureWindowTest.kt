package cc.tumtum.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class CaptureWindowTest {
    private val start = Instant.parse("2026-10-27T01:00:00Z") // 26/10, 22h00 in São Paulo

    @Test
    fun `the window opens thirty minutes before the start, not a day early`() {
        assertEquals(Instant.parse("2026-10-27T00:30:00Z"), CaptureWindow.opensAt(start))
        assertFalse(CaptureWindow.isOpen(Instant.parse("2026-10-02T17:27:00Z"), start))
        assertFalse(CaptureWindow.isOpen(Instant.parse("2026-10-27T00:29:59Z"), start))
        assertTrue(CaptureWindow.isOpen(Instant.parse("2026-10-27T00:30:00Z"), start))
        assertTrue(CaptureWindow.isOpen(Instant.parse("2026-10-27T02:10:00Z"), start))
    }
}
