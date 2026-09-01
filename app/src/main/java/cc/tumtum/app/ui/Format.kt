package cc.tumtum.app.ui

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Formatação da casa: número é número; hora é "23h47"; data é "22.03.26". */
object Fmt {
    private val zone: ZoneId get() = ZoneId.systemDefault()
    private val hour = DateTimeFormatter.ofPattern("HH'h'mm")
    private val date = DateTimeFormatter.ofPattern("dd.MM.yy")

    fun hour(t: Instant): String = hour.format(t.atZone(zone))
    fun date(t: Instant): String = date.format(t.atZone(zone))

    /** "6H38" — duração da noite. */
    fun durationChip(d: Duration): String = "%dH%02d".format(d.toHours(), d.toMinutesPart())

    /** "06:27:44" — cronômetro do ao vivo. */
    fun stopwatch(d: Duration): String =
        "%02d:%02d:%02d".format(d.toHours(), d.toMinutesPart(), d.toSecondsPart())

    /** "8.734" — milhar com ponto, como no manual. */
    fun thousands(n: Int): String = "%,d".format(n).replace(',', '.')

    /** Mês por extenso curto para "desde fevereiro". */
    fun monthName(t: Instant): String {
        val months = listOf(
            "janeiro", "fevereiro", "março", "abril", "maio", "junho",
            "julho", "agosto", "setembro", "outubro", "novembro", "dezembro",
        )
        return months[t.atZone(zone).monthValue - 1]
    }
}
