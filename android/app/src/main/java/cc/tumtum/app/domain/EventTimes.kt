package cc.tumtum.app.domain

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * The date and times the operator rolls for an event TumTum will cover
 * (§5.7 — the calendar is the trigger). Pure, so the rules are testable
 * without a screen.
 *
 * Nothing here is typed — a time is never typed (21/09) — so there is no
 * "could not read that". What is left is meaning: an event whose start has
 * already passed, and a night longer than [MAX_NIGHT], which is a slip of
 * the wheel rather than a night. An end at or before the start crossed
 * midnight and belongs to the next day, which is how a show that runs 22h→02h
 * is written.
 *
 * `past()` lived here until 22/09 and went with the "Trazer uma noite que já
 * passou" flow Felipe cut (entry of that day).
 */
object EventTimes {
    sealed class Result {
        data class Ok(val startAt: Instant, val endAt: Instant) : Result()
        data class Error(val reason: Reason) : Result()
    }

    enum class Reason { IN_PAST, TOO_LONG }

    val MAX_NIGHT: Duration = Duration.ofHours(16)

    private val dateFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val timeFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("HH'h'mm")

    /** "21/09/2026" — how a rolled date reads in its field. */
    fun formatDate(day: LocalDate): String = dateFmt.format(day)

    /** "21h30" — how a rolled time reads in its field, the house's hour. */
    fun formatTime(time: LocalTime): String = timeFmt.format(time)

    fun today(now: Instant, zone: ZoneId = ZoneId.systemDefault()): LocalDate = now.atZone(zone).toLocalDate()

    /** One date, a start in the future, and an end; an end at or before the start is the next day. */
    fun upcoming(
        day: LocalDate,
        start: LocalTime,
        end: LocalTime,
        now: Instant,
        zone: ZoneId = ZoneId.systemDefault(),
    ): Result {
        val startAt = day.atTime(start).atZone(zone).toInstant()
        if (!startAt.isAfter(now)) return Result.Error(Reason.IN_PAST)
        var endAt = day.atTime(end).atZone(zone).toInstant()
        if (!endAt.isAfter(startAt)) endAt = endAt.plus(Duration.ofDays(1))
        if (Duration.between(startAt, endAt) > MAX_NIGHT) return Result.Error(Reason.TOO_LONG)
        return Result.Ok(startAt, endAt)
    }
}
