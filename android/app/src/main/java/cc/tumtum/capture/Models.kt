package cc.tumtum.capture

import org.json.JSONArray
import org.json.JSONObject

/**
 * What the native screens draw, and how it is read off the wire.
 *
 * Parsed by hand with org.json for the same reason the HTTP client is
 * HttpURLConnection: both ship with Android, so the APK carries no runtime
 * dependencies and there is no version matrix to get wrong while this is built
 * without a phone to compile against.
 *
 * Every field the backend declares nullable is nullable here. A screen that
 * renders 0 where the server said "unknown" is the bug class this project
 * keeps finding — the app stating something it was never told.
 */

/** A `null` in JSON is not the same as a missing key, and neither is a value. */
internal fun JSONObject.stringOrNull(key: String): String? =
    if (isNull(key)) null else optString(key, "").takeIf { it.isNotEmpty() }

/** `isNull` is true for an absent key as well as an explicit null, which is what we want. */
internal fun JSONObject.intOrNull(key: String): Int? =
    if (isNull(key)) null else optInt(key)

data class EventBrief(
    val id: String,
    val name: String,
    /** Plain `YYYY-MM-DD`, no time and no zone — that is what the column holds. */
    val date: String,
    val venue: String?,
    val city: String?,
) {
    /** `29/08 · Realness Festival 2026` — one line, readable in a dark room. */
    val label: String
        get() {
            val parts = date.split("-")
            val day = if (parts.size == 3) "${parts[2]}/${parts[1]}" else date
            return "$day · $name"
        }

    companion object {
        fun from(json: JSONObject) = EventBrief(
            id = json.getString("id"),
            name = json.optString("name", "Evento"),
            date = json.optString("date", ""),
            venue = json.stringOrNull("venue"),
            city = json.stringOrNull("city"),
        )

        fun listFrom(array: JSONArray): List<EventBrief> =
            (0 until array.length()).map { from(array.getJSONObject(it)) }
    }
}

data class SessionSummary(
    val id: String,
    val eventId: String?,
    val startTimeMillis: Long?,
    val endTimeMillis: Long?,
    val avgBpm: Int?,
    val maxBpm: Int?,
    val minBpm: Int?,
    val qualityScore: Int?,
    val sourceDevice: String?,
) {
    /** Seconds captured, or null when either end of the session is unreadable. */
    val durationSeconds: Long?
        get() {
            val start = startTimeMillis ?: return null
            val end = endTimeMillis ?: return null
            return (end - start) / 1000
        }

    companion object {
        fun from(json: JSONObject) = SessionSummary(
            id = json.getString("id"),
            eventId = json.stringOrNull("event_id"),
            startTimeMillis = Iso8601.toMillis(json.stringOrNull("start_time")),
            endTimeMillis = Iso8601.toMillis(json.stringOrNull("end_time")),
            avgBpm = json.intOrNull("avg_bpm"),
            maxBpm = json.intOrNull("max_bpm"),
            minBpm = json.intOrNull("min_bpm"),
            qualityScore = json.intOrNull("data_quality_score"),
            sourceDevice = json.stringOrNull("source_device"),
        )

        fun listFrom(array: JSONArray): List<SessionSummary> =
            (0 until array.length()).map { from(array.getJSONObject(it)) }
    }
}

data class Peak(
    val id: String,
    val timestampMillis: Long?,
    val bpm: Int,
    val durationSeconds: Int,
    val rank: Int?,
    /** The moment of the night this peak landed on, when the event has a timeline. */
    val matchedLabel: String?,
) {
    companion object {
        fun from(json: JSONObject) = Peak(
            id = json.getString("id"),
            timestampMillis = Iso8601.toMillis(json.stringOrNull("timestamp")),
            bpm = json.optInt("bpm"),
            durationSeconds = json.optInt("duration_seconds"),
            rank = json.intOrNull("rank"),
            matchedLabel = json.stringOrNull("matched_label"),
        )

        fun listFrom(array: JSONArray): List<Peak> =
            (0 until array.length()).map { from(array.getJSONObject(it)) }
    }
}

data class HrPoint(val timeMillis: Long, val bpm: Int)

data class Experience(
    val session: SessionSummary,
    val peaks: List<Peak>,
    val hrData: List<HrPoint>,
) {
    companion object {
        fun from(json: JSONObject): Experience {
            val points = json.optJSONArray("hr_data") ?: JSONArray()
            val series = ArrayList<HrPoint>(points.length())
            for (i in 0 until points.length()) {
                val point = points.getJSONObject(i)
                // A point whose time cannot be read has no place on an axis;
                // dropping it is honest, plotting it at the epoch is not.
                val at = Iso8601.toMillis(point.stringOrNull("time")) ?: continue
                series.add(HrPoint(at, point.optInt("bpm")))
            }
            return Experience(
                session = SessionSummary.from(json.getJSONObject("session")),
                peaks = Peak.listFrom(json.optJSONArray("peaks") ?: JSONArray()),
                hrData = series,
            )
        }
    }
}
