package cc.tumtum.app.domain

import java.time.Instant
import java.time.ZoneId

/**
 * A trava da revela — o lacre do protocolo do teste: a memória da pessoa é
 * colhida no cartão cego antes de qualquer dado; a curva só abre às 10h da
 * manhã seguinte, sóbria e com atenção. Uma vez vista, a memória não é mais dela.
 */
object RevealLock {

    const val REVEAL_HOUR = 10

    /**
     * Show que termina antes das 10h (ex.: 2h da manhã) abre às 10h do MESMO dia;
     * fim depois das 10h abre às 10h do dia seguinte.
     */
    fun revealAt(windowEnd: Instant, zone: ZoneId = ZoneId.systemDefault()): Instant {
        val local = windowEnd.atZone(zone)
        val tenSameDay = local.toLocalDate().atStartOfDay(zone).plusHours(REVEAL_HOUR.toLong())
        return if (local.isBefore(tenSameDay)) tenSameDay.toInstant() else tenSameDay.plusDays(1).toInstant()
    }

    fun isLocked(revealAt: Instant?, now: Instant = Instant.now()): Boolean =
        revealAt != null && now.isBefore(revealAt)
}
