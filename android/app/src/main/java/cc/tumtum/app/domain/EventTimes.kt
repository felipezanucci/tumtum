package cc.tumtum.app.domain

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * The date and time a person types for an event that is not "now": the next
 * one they marked (§5.7) or a night that already happened (§5.1). Pure, so the
 * rules are testable without a screen: typed as dd/MM/yyyy and HH:mm, a past
 * night whose end is before its start crossed midnight, and a night longer
 * than [MAX_NIGHT] is a typo, not a night.
 */
object EventTimes {
    sealed class Result {
        data class Ok(val startAt: Instant, val endAt: Instant?) : Result()
        data class Error(val reason: Reason) : Result()
    }

    enum class Reason { UNPARSEABLE, IN_PAST, NOT_PAST, TOO_LONG }

    val MAX_NIGHT: Duration = Duration.ofHours(16)

    private val dateFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val timeFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun formatDate(day: LocalDate): String = dateFmt.format(day)

    fun today(now: Instant, zone: ZoneId = ZoneId.systemDefault()): String = formatDate(now.atZone(zone).toLocalDate())

    fun yesterday(now: Instant, zone: ZoneId = ZoneId.systemDefault()): String =
        formatDate(now.atZone(zone).toLocalDate().minusDays(1))

    /** The next event: one date, one time, in the future. */
    fun upcoming(dateText: String, timeText: String, now: Instant, zone: ZoneId = ZoneId.systemDefault()): Result {
        val day = parseDate(dateText) ?: return Result.Error(Reason.UNPARSEABLE)
        val time = parseTime(timeText) ?: return Result.Error(Reason.UNPARSEABLE)
        val startAt = day.atTime(time).atZone(zone).toInstant()
        if (!startAt.isAfter(now)) return Result.Error(Reason.IN_PAST)
        return Result.Ok(startAt, null)
    }

    /** A night that already happened: one date, a start and an end; an end before the start is the next day. */
    fun past(
        dateText: String,
        startText: String,
        endText: String,
        now: Instant,
        zone: ZoneId = ZoneId.systemDefault(),
    ): Result {
        val day = parseDate(dateText) ?: return Result.Error(Reason.UNPARSEABLE)
        val start = parseTime(startText) ?: return Result.Error(Reason.UNPARSEABLE)
        val end = parseTime(endText) ?: return Result.Error(Reason.UNPARSEABLE)
        val startAt = day.atTime(start).atZone(zone).toInstant()
        var endAt = day.atTime(end).atZone(zone).toInstant()
        if (!endAt.isAfter(startAt)) endAt = endAt.plus(Duration.ofDays(1))
        if (endAt.isAfter(now)) return Result.Error(Reason.NOT_PAST)
        if (Duration.between(startAt, endAt) > MAX_NIGHT) return Result.Error(Reason.TOO_LONG)
        return Result.Ok(startAt, endAt)
    }

    private fun parseDate(text: String): LocalDate? =
        try { LocalDate.parse(text.trim(), dateFmt) } catch (e: DateTimeParseException) { null }

    private fun parseTime(text: String): LocalTime? =
        try { LocalTime.parse(text.trim().replace('h', ':'), timeFmt) } catch (e: DateTimeParseException) { null }
}
