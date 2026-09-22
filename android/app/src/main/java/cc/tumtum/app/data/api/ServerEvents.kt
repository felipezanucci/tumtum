package cc.tumtum.app.data.api

import org.json.JSONArray
import org.json.JSONObject
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.abs

/** An event as the server lists it — the thing a night attaches to, and the owner of the timeline that names moments. */
data class ServerEvent(
    val id: String,
    val name: String,
    val date: LocalDate?,
    val venue: String?,
    val city: String?,
    val eventType: String,
    /**
     * When it starts and ends on the phone's clock (21/09): the server's
     * `date` and the wall-clock digits of `start_time` / `end_time`, read in
     * the phone's zone. The offset the server sends carries nothing — see
     * `offset_aware` in the backend's event schema — and is never read. An
     * end before the start is the next day. Null when the server has no time
     * for the event; then there is nothing for a fan to activate.
     */
    val startAt: Instant? = null,
    val endAt: Instant? = null,
) {
    /** `10/10 · São Paulo × Vitória` — one line, readable in a dark room. */
    val label: String
        get() = (date?.let { "%02d/%02d · ".format(it.dayOfMonth, it.monthValue) } ?: "") + name

    /**
     * Everything about the event **except its name**: `10/10 · Morumbi`.
     *
     * For a line that sits under the name. The feed row used [label] there and
     * so printed the name twice, once in bold and once again beside the date
     * (#31, 22/09). Null when the server gave nothing but a name.
     */
    val details: String?
        get() = listOfNotNull(
            date?.let { "%02d/%02d".format(it.dayOfMonth, it.monthValue) },
            venue?.takeIf { it.isNotBlank() } ?: city?.takeIf { it.isNotBlank() },
        ).joinToString(" · ").ifBlank { null }

    /** When the night is over: the server's end, or [ServerEvents.NIGHT_LENGTH] after the start. */
    val endsAt: Instant?
        get() = endAt ?: startAt?.plus(ServerEvents.NIGHT_LENGTH)

    fun isUpcomingAt(now: Instant): Boolean = startAt?.isAfter(now) == true

    fun isLiveAt(now: Instant): Boolean {
        val start = startAt ?: return false
        val end = endsAt ?: return false
        return !now.isBefore(start) && now.isBefore(end)
    }

    fun isPastAt(now: Instant): Boolean {
        val end = endsAt ?: return false
        return !now.isBefore(end)
    }
}

/** Reads `GET /api/events` and picks what matters tonight. Pure, tested. */
object ServerEvents {
    /**
     * A night with no end on the server is taken to last this long. It is a
     * window to ask the watch over, and the badge for "still on" — never a
     * reading. An operator who registers from the phone always sends an end.
     */
    val NIGHT_LENGTH: Duration = Duration.ofHours(5)

    fun parse(json: String, zone: ZoneId = ZoneId.systemDefault()): List<ServerEvent> {
        val array = JSONArray(json)
        return (0 until array.length()).map { i -> from(array.getJSONObject(i), zone) }
    }

    fun from(item: JSONObject, zone: ZoneId = ZoneId.systemDefault()): ServerEvent {
        val date = runCatching { LocalDate.parse(item.getString("date")) }.getOrNull()
        val start = wallClock(item.text("start_time"))
        val end = wallClock(item.text("end_time"))
        val startAt = if (date != null && start != null) date.atTime(start).atZone(zone).toInstant() else null
        var endAt = if (date != null && end != null) date.atTime(end).atZone(zone).toInstant() else null
        if (startAt != null && endAt != null && !endAt.isAfter(startAt)) endAt = endAt.plus(Duration.ofDays(1))
        return ServerEvent(
            id = item.getString("id"),
            name = item.optString("name", "Evento"),
            date = date,
            venue = item.text("venue"),
            city = item.text("city"),
            eventType = item.optString("event_type", "concert"),
            startAt = startAt,
            endAt = if (startAt != null) endAt else null,
        )
    }

    /**
     * "19:30:00Z", "19:30:00-03:00", "19:30:00.000000+00:00", "19:30" — the
     * hour and minute, nothing else. The offset is the column's, not the
     * event's, and is discarded on purpose.
     */
    fun wallClock(text: String?): LocalTime? {
        val m = WALL_CLOCK.find(text?.trim() ?: return null) ?: return null
        val hour = m.groupValues[1].toInt()
        val minute = m.groupValues[2].toInt()
        if (hour !in 0..23 || minute !in 0..59) return null
        return LocalTime.of(hour, minute)
    }

    private val WALL_CLOCK = Regex("""^(\d{2}):(\d{2})""")

    /**
     * A string field that may be absent, JSON null or blank — all three mean "none".
     * Android's org.json turns a JSON null into the text "null" in optString (the
     * reference library, which the unit tests run on, returns the fallback), so
     * the venue "null" showed up on the phone and not in the tests: check isNull first.
     */
    private fun JSONObject.text(key: String): String? =
        if (!has(key) || isNull(key)) null else optString(key, "").ifBlank { null }

    /**
     * The events worth offering before a capture: nearest to today first,
     * ties broken by date, at most [limit]. The server orders by date
     * descending and caps at 50, which puts next year's festival above
     * tonight's match; this puts tonight first.
     */
    fun nearest(events: List<ServerEvent>, today: LocalDate, limit: Int = 8): List<ServerEvent> =
        events.sortedWith(
            compareBy<ServerEvent> { it.date?.let { d -> abs(d.toEpochDay() - today.toEpochDay()) } ?: Long.MAX_VALUE }
                .thenBy { it.date },
        ).take(limit)

    /**
     * What a fan sees on AO VIVO: the events TumTum registered that have not
     * ended — one going on right now first, then the soonest — each a single
     * tap away from being activated.
     *
     * An event with no time is an operator's leftover and is not offered:
     * there is nothing to count down to. An event already over simply leaves
     * the list; bringing a past night back was cut on 22/09.
     */
    fun forFan(events: List<ServerEvent>, now: Instant, limit: Int = 6): List<ServerEvent> =
        events.filter { it.startAt != null && !it.isPastAt(now) }
            .sortedBy { it.startAt }
            .take(limit)
}

/** The things a person can mark with one tap in the dark, and what the server calls each. */
object MarkKinds {
    const val GOAL = "goal"
    const val SONG = "song_start"
    const val MOMENT = "highlight"

    /**
     * The two anchors of a match (22/09): the instant each half really
     * started. API-Football gives every goal a match minute, and a minute is
     * only a wall-clock time from the whistle it counts from — without these
     * the server falls back to the scheduled kick-off and an assumed
     * interval, and a second-half goal can land fifteen minutes off. One tap
     * each, once per match, and every goal and card names itself for
     * everyone at the game.
     */
    const val KICKOFF = "kickoff"
    const val SECOND_HALF = "second_half"

    fun isAnchor(entryType: String): Boolean = entryType == KICKOFF || entryType == SECOND_HALF

    /** What the correlator will name the moment: the tap's own label, nothing typed. */
    fun label(entryType: String, tapLabel: String): String = tapLabel
}
