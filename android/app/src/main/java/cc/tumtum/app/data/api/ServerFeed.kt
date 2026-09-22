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
    /** The night it belongs to — so it can be taken down from the series feed too (#33). */
    val eventId: String? = null,
    /** Which night it is from, only in a series feed, where several dates sit together. */
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

/** A tour, club or championship — the level above one night (#33). */
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

/** The series feed: every post shown to the tour, from every date. */
data class ServerSeriesFeed(
    val series: ServerSeries,
    val posts: List<ServerPost>,
) {
    companion object {
        fun parse(json: String): ServerSeriesFeed {
            val o = JSONObject(json)
            val events = o.optJSONArray("events") ?: JSONArray()
            val posts = o.optJSONArray("posts") ?: JSONArray()
            return ServerSeriesFeed(
                series = ServerSeries(
                    id = o.optString("series_id", ""),
                    name = o.optString("name", ""),
                    kind = o.optString("kind", "tour"),
                    dates = events.length(),
                ),
                posts = (0 until posts.length()).map { ServerPost.parse(posts.getJSONObject(it)) },
            )
        }
    }
}

/** The event's feed: the people who were there, and what they chose to show. */
data class ServerFeed(
    val eventId: String,
    val eventName: String,
    val venue: String?,
    val posts: List<ServerPost>,
    /** The level above this night, when there is one — the door to it. */
    val series: ServerSeries? = null,
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
