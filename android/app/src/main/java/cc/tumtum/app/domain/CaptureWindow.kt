package cc.tumtum.app.domain

import java.time.Duration
import java.time.Instant

/**
 * When a night may start recording (02/10). The consent text promises
 * "A gente só lê sua batida na janela do evento: de 30 minutos antes do
 * começo até 30 minutos depois do fim" — and until 02/10 the marked event's
 * "Começar agora" recorded at any hour, 24 days early included (night 8,
 * stuck to "Rihanna teste"). The button now opens with the window; before
 * that it says when. The operator keeps a way around it, for tests, said as
 * such.
 */
object CaptureWindow {
    val OPENS_BEFORE: Duration = Duration.ofMinutes(30)

    fun opensAt(startAt: Instant): Instant = startAt.minus(OPENS_BEFORE)

    fun isOpen(now: Instant, startAt: Instant): Boolean = !now.isBefore(opensAt(startAt))
}
