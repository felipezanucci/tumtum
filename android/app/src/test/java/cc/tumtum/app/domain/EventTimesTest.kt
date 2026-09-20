package cc.tumtum.app.domain

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class EventTimesTest {

    private val sp: ZoneId = ZoneId.of("America/Sao_Paulo")

    private fun at(y: Int, m: Int, d: Int, h: Int, min: Int = 0): Instant =
        ZonedDateTime.of(y, m, d, h, min, 0, 0, sp).toInstant()

    private val now = at(2026, 9, 20, 12)

    @Test
    fun `o proximo evento e lido de dia e hora`() {
        val r = EventTimes.upcoming("10/10/2026", "16:00", now, sp)
        assertEquals(EventTimes.Result.Ok(at(2026, 10, 10, 16), null), r)
    }

    @Test
    fun `hora com h tambem vale`() {
        val r = EventTimes.upcoming("10/10/2026", "16h00", now, sp)
        assertEquals(EventTimes.Result.Ok(at(2026, 10, 10, 16), null), r)
    }

    @Test
    fun `um proximo evento no passado e recusado`() {
        assertEquals(EventTimes.Result.Error(EventTimes.Reason.IN_PAST), EventTimes.upcoming("19/09/2026", "21:00", now, sp))
    }

    @Test
    fun `data ilegivel e recusada`() {
        assertEquals(EventTimes.Result.Error(EventTimes.Reason.UNPARSEABLE), EventTimes.upcoming("sábado", "21:00", now, sp))
        assertEquals(EventTimes.Result.Error(EventTimes.Reason.UNPARSEABLE), EventTimes.past("19/09/2026", "21", "23:00", now, sp))
    }

    @Test
    fun `uma noite que vara a madrugada termina no dia seguinte`() {
        val r = EventTimes.past("19/09/2026", "21:00", "02:30", now, sp)
        assertEquals(EventTimes.Result.Ok(at(2026, 9, 19, 21), at(2026, 9, 20, 2, 30)), r)
    }

    @Test
    fun `uma noite que ainda nao terminou nao e passado`() {
        assertEquals(EventTimes.Result.Error(EventTimes.Reason.NOT_PAST), EventTimes.past("20/09/2026", "11:00", "14:00", now, sp))
    }

    @Test
    fun `mais de 16 horas e um erro de digitacao`() {
        assertEquals(EventTimes.Result.Error(EventTimes.Reason.TOO_LONG), EventTimes.past("18/09/2026", "10:00", "09:00", now, sp))
    }

    @Test
    fun `ontem e formatado no padrao do campo`() {
        assertEquals("19/09/2026", EventTimes.yesterday(now, sp))
        assertEquals("20/09/2026", EventTimes.today(now, sp))
    }
}
