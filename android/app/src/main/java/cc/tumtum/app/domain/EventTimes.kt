package cc.tumtum.app.domain

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * The date and times the operator picks for an event that is not "now": the
 * next one (§5.7) or a night that already happened (§5.1). Pure, so the rules
 * are testable without a screen. Since 21/09 nothing here is typed — a time
 * is never typed — so there is no "could not read that"; what is left is
 * meaning: a next event in the past, a past night that has not ended, an end
 * before the start that crossed midnight, and a night longer than
 * [MAX_NIGHT], which is a slip of the wheel, not a night.
 */
object EventTimes {
    sealed class Result {
        data class Ok(val startAt: Instant, val endAt: Instant?) : Result()
        data class Error(val reason: Reason) : Result()
    }

    enum class Reason { IN_PAST, NOT_PAST, TOO_LONG }

    val MAX_NIGHT: Duration = Duration.ofHours(16)

    private val dateFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val timeFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("HH'h'mm")

    /** "21/09/2026" — how a picked date reads in its field. */
    fun formatDate(day: LocalDate): String = dateFmt.format(day)

    /** "21h30" — how a picked time reads in its field, the house's hour. */
    fun formatTime(time: LocalTime): String = timeFmt.format(time)

    fun today(now: Instant, zone: ZoneId = ZoneId.systemDefault()): LocalDate = now.atZone(zone).toLocalDate()

    fun yesterday(now: Instant, zone: ZoneId = ZoneId.systemDefault()): LocalDate = today(now, zone).minusDays(1)

    /** The next event: one date, one time, in the future. */
    fun upcoming(day: LocalDate, time: LocalTime, now: Instant, zone: ZoneId = ZoneId.systemDefault()): Result {
        val startAt = day.atTime(time).atZone(zone).toInstant()
        if (!startAt.isAfter(now)) return Result.Error(Reason.IN_PAST)
        return Result.Ok(startAt, null)
    }

    /** A night that already happened: one date, a start and an end; an end before the start is the next day. */
    fun past(day: LocalDate, start: LocalTime, end: LocalTime, now: Instant, zone: ZoneId = ZoneId.systemDefault()): Result {
        val startAt = day.atTime(start).atZone(zone).toInstant()
        var endAt = day.atTime(end).atZone(zone).toInstant()
        if (!endAt.isAfter(startAt)) endAt = endAt.plus(Duration.ofDays(1))
        if (endAt.isAfter(now)) return Result.Error(Reason.NOT_PAST)
        if (Duration.between(startAt, endAt) > MAX_NIGHT) return Result.Error(Reason.TOO_LONG)
        return Result.Ok(startAt, endAt)
    }
}
