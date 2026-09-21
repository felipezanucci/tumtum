package cc.tumtum.app.domain

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
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
        val r = EventTimes.upcoming(LocalDate.of(2026, 10, 10), LocalTime.of(16, 0), now, sp)
        assertEquals(EventTimes.Result.Ok(at(2026, 10, 10, 16), null), r)
    }

    @Test
    fun `um proximo evento no passado e recusado`() {
        val r = EventTimes.upcoming(LocalDate.of(2026, 9, 19), LocalTime.of(21, 0), now, sp)
        assertEquals(EventTimes.Result.Error(EventTimes.Reason.IN_PAST), r)
    }

    @Test
    fun `uma noite que vara a madrugada termina no dia seguinte`() {
        val r = EventTimes.past(LocalDate.of(2026, 9, 19), LocalTime.of(21, 0), LocalTime.of(2, 30), now, sp)
        assertEquals(EventTimes.Result.Ok(at(2026, 9, 19, 21), at(2026, 9, 20, 2, 30)), r)
    }

    @Test
    fun `uma noite que ainda nao terminou nao e passado`() {
        val r = EventTimes.past(LocalDate.of(2026, 9, 20), LocalTime.of(11, 0), LocalTime.of(14, 0), now, sp)
        assertEquals(EventTimes.Result.Error(EventTimes.Reason.NOT_PAST), r)
    }

    @Test
    fun `mais de 16 horas e um deslize da roda`() {
        val r = EventTimes.past(LocalDate.of(2026, 9, 18), LocalTime.of(10, 0), LocalTime.of(9, 0), now, sp)
        assertEquals(EventTimes.Result.Error(EventTimes.Reason.TOO_LONG), r)
    }

    @Test
    fun `hoje e ontem saem do relogio do aparelho`() {
        assertEquals(LocalDate.of(2026, 9, 20), EventTimes.today(now, sp))
        assertEquals(LocalDate.of(2026, 9, 19), EventTimes.yesterday(now, sp))
    }

    @Test
    fun `os campos mostram a data e a hora no formato da casa`() {
        assertEquals("21/09/2026", EventTimes.formatDate(LocalDate.of(2026, 9, 21)))
        assertEquals("19h30", EventTimes.formatTime(LocalTime.of(19, 30)))
    }
}
