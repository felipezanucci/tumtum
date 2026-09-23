package cc.tumtum.app.data.api

import java.time.Instant
import java.time.OffsetDateTime
import org.json.JSONArray
import org.json.JSONObject

/** One published moment, as the server hands it over. */
data class ServerPost(
    val id: String,
    val authorName: String,
    val authorInitials: String,
    val bpm: Int,
    val at: Instant,
    val label: String?,
    val quote: String?,
    val skin: String,
    val reactions: Int,
    val reactedByMe: Boolean,
    /** Whether the viewer may take this one down — the undo is shown only where it exists. */
    val mine: Boolean,
    /** The night it belongs to — the "Só a minha noite" filter reads it, and so does the undo. */
    val eventId: String? = null,
    /** Which night it is from: in a tour's feed several dates sit together (#65). */
    val eventName: String? = null,
    val eventDate: java.time.LocalDate? = null,
    val eventCity: String? = null,
) {
    companion object {
        /**
         * One post, whether it arrived inside the feed or alone as the answer
         * to a SENTI TB. Until 22/09 the second never got parsed at all: the
         * reaction's response was dropped on the wire and the screen had to
         * refetch the whole feed to find out what its own tap had done.
         */
        fun parse(p: JSONObject): ServerPost {
            val author = p.optJSONObject("author") ?: JSONObject()
            return ServerPost(
                id = p.optString("id", ""),
                authorName = author.optString("name", "Alguém"),
                authorInitials = author.optString("initials", "TT"),
                bpm = p.optInt("bpm", 0),
                at = Json.instant(p.getString("moment_at")),
                label = Json.text(p, "label"),
                quote = Json.text(p, "quote"),
                skin = p.optString("skin", "BLACK"),
                reactions = p.optInt("reactions", 0),
                reactedByMe = p.optBoolean("reacted_by_me", false),
                mine = p.optBoolean("mine", false),
                eventId = Json.text(p, "event_id"),
                eventName = Json.text(p, "event_name"),
                eventDate = Json.text(p, "event_date")?.let { java.time.LocalDate.parse(it) },
                eventCity = Json.text(p, "event_city"),
            )
        }

        fun parse(json: String): ServerPost = parse(JSONObject(json))
    }
}

/** The tour an event belongs to (#33); its dates share one feed (#65). */
data class ServerSeries(val id: String, val name: String, val kind: String, val dates: Int) {
    companion object {
        fun parse(o: JSONObject): ServerSeries = ServerSeries(
            id = o.getString("id"),
            name = o.optString("name", ""),
            kind = o.optString("kind", "tour"),
            dates = o.optInt("dates", 0),
        )

        /** `GET /api/events/{id}/series` answers the series or JSON null. */
        fun parseOrNull(json: String): ServerSeries? =
            json.trim().takeIf { it.startsWith("{") }?.let { parse(JSONObject(it)) }
    }
}

/** One date of the feed — a tour has several, a show on its own has one (#65). */
data class ServerFeedDate(
    val id: String,
    val name: String,
    val date: java.time.LocalDate?,
    val city: String?,
) {
    companion object {
        fun parse(o: JSONObject): ServerFeedDate = ServerFeedDate(
            id = o.optString("id", ""),
            name = o.optString("name", ""),
            date = Json.text(o, "date")?.let { java.time.LocalDate.parse(it) },
            city = Json.text(o, "city"),
        )
    }
}

/**
 * The one feed an event opens onto (#65, 23/09).
 *
 * When the event is part of a tour, [series] names it and [posts] come from
 * every date, each carrying its own — "Só a minha noite" is a filter the
 * screen applies, never a second feed. [dates] is one entry for a show on its
 * own. [hiddenByBlock] is how many posts a block kept out, so an empty feed
 * can say why it is empty (#63).
 */
data class ServerFeed(
    val eventId: String,
    val eventName: String,
    val venue: String?,
    val posts: List<ServerPost>,
    val series: ServerSeries? = null,
    val dates: List<ServerFeedDate> = emptyList(),
    val hiddenByBlock: Int = 0,
) {
    /** Reads `GET /api/events/{id}/feed`. Pure, tested. */
    companion object {
        fun parse(json: String): ServerFeed {
            val o = JSONObject(json)
            val array = o.optJSONArray("posts") ?: JSONArray()
            return ServerFeed(
                eventId = o.optString("event_id", ""),
                eventName = o.optString("event_name", ""),
                venue = Json.text(o, "venue"),
                posts = (0 until array.length()).map { i -> ServerPost.parse(array.getJSONObject(i)) },
                series = o.optJSONObject("series")?.let { ServerSeries.parse(it) },
                dates = (o.optJSONArray("events") ?: JSONArray()).let { d ->
                    (0 until d.length()).map { ServerFeedDate.parse(d.getJSONObject(it)) }
                },
                hiddenByBlock = o.optInt("hidden_by_block", 0),
            )
        }
    }
}

/**
 * Card 04 — the crowd, **or an honest account of why there is no crowd yet**.
 *
 * [enough] false is neither an error nor emptiness: it is the server refusing
 * to publish a figure computed over too few people, because below its floor a
 * collective number is a fact about each person in it. [measuredNights] comes
 * back anyway so the screen can say *"ainda somos poucos aqui"* instead of
 * drawing a zero — a zero is a claim about the world.
 */
data class ServerCrowd(
    val measuredNights: Int,
    val enough: Boolean,
    val sharedCount: Int,
    val moments: List<CrowdMoment>,
    val top: CrowdMoment?,
) {
    data class CrowdMoment(val at: Instant, val people: Int, val label: String?)

    /** Reads `GET /api/events/{id}/crowd`. Pure, tested. */
    companion object {
        fun parse(json: String): ServerCrowd {
            val o = JSONObject(json)
            val array = o.optJSONArray("moments") ?: JSONArray()
            return ServerCrowd(
                measuredNights = o.optInt("measured_nights", 0),
                enough = o.optBoolean("enough", false),
                sharedCount = o.optInt("shared_count", 0),
                moments = (0 until array.length()).map { i ->
                    moment(array.getJSONObject(i))
                },
                top = o.optJSONObject("top")?.let { moment(it) },
            )
        }

        private fun moment(o: JSONObject) = CrowdMoment(
            at = Json.instant(o.getString("at")),
            people = o.optInt("people", 0),
            label = Json.text(o, "label"),
        )
    }
}

/** The two things every payload here needs and org.json does badly. */
internal object Json {
    /** The server writes `...Z` or `...+00:00`; both are one instant. */
    fun instant(text: String): Instant = OffsetDateTime.parse(text).toInstant()

    /** A string that may be JSON null, missing, or blank — all of them null. */
    fun text(o: JSONObject, key: String): String? =
        if (o.isNull(key)) null else o.optString(key, "").ifBlank { null }
}
