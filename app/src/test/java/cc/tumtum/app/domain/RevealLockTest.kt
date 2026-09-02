package cc.tumtum.app.domain

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RevealLockTest {

    private val sp: ZoneId = ZoneId.of("America/Sao_Paulo")

    private fun at(y: Int, m: Int, d: Int, h: Int, min: Int = 0): Instant =
        ZonedDateTime.of(y, m, d, h, min, 0, 0, sp).toInstant()

    @Test
    fun `show que vara a madrugada abre as 10h do mesmo dia`() {
        // Fim às 2h17 de 26/09 → abre 26/09 10h
        assertEquals(at(2026, 9, 26, 10), RevealLock.revealAt(at(2026, 9, 26, 2, 17), sp))
    }

    @Test
    fun `show que termina antes da meia-noite abre as 10h do dia seguinte`() {
        // Fim às 23h40 de 25/09 → abre 26/09 10h
        assertEquals(at(2026, 9, 26, 10), RevealLock.revealAt(at(2026, 9, 25, 23, 40), sp))
    }

    @Test
    fun `fim exatamente as 10h vai para o dia seguinte`() {
        assertEquals(at(2026, 9, 27, 10), RevealLock.revealAt(at(2026, 9, 26, 10), sp))
    }

    @Test
    fun `isLocked respeita o relogio e o null`() {
        val reveal = at(2026, 9, 26, 10)
        assertTrue(RevealLock.isLocked(reveal, now = at(2026, 9, 26, 3)))
        assertFalse(RevealLock.isLocked(reveal, now = at(2026, 9, 26, 10)))
        assertFalse(RevealLock.isLocked(null, now = at(2026, 9, 26, 3)))
    }
}
