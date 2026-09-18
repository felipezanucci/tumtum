package cc.tumtum.app.data.api

import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import kotlin.math.abs

/** An event as the server lists it — the thing a night attaches to, and the owner of the timeline that names moments. */
data class ServerEvent(
    val id: String,
    val name: String,
    val date: LocalDate?,
    val venue: String?,
    val city: String?,
    val eventType: String,
) {
    /** `10/10 · São Paulo × Vitória` — one line, readable in a dark room. */
    val label: String
        get() = (date?.let { "%02d/%02d · ".format(it.dayOfMonth, it.monthValue) } ?: "") + name
}

/** Reads `GET /api/events` and picks what matters tonight. Pure, tested. */
object ServerEvents {
    fun parse(json: String): List<ServerEvent> {
        val array = JSONArray(json)
        return (0 until array.length()).map { i -> from(array.getJSONObject(i)) }
    }

    fun from(item: JSONObject): ServerEvent = ServerEvent(
        id = item.getString("id"),
        name = item.optString("name", "Evento"),
        date = runCatching { LocalDate.parse(item.getString("date")) }.getOrNull(),
        venue = item.optString("venue", "").ifBlank { null },
        city = item.optString("city", "").ifBlank { null },
        eventType = item.optString("event_type", "concert"),
    )

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
}

/** The three things a person can mark with one tap in the dark, and what the server calls each. */
object MarkKinds {
    const val GOAL = "goal"
    const val SONG = "song_start"
    const val MOMENT = "highlight"

    /** What the correlator will name the moment: the tap's own label, nothing typed. */
    fun label(entryType: String, tapLabel: String): String = tapLabel
}
