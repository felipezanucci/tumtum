package cc.tumtum.app.data.api

import org.json.JSONArray
import java.time.Instant
import java.time.OffsetDateTime

/** A moment as the server found it: the detector's region, named by its cause when there is one. */
data class ServerMoment(
    val bpm: Int,
    val at: Instant,
    val durationSec: Int,
    val label: String?,
    val rank: Int?,
)

/** Reads the list `POST /api/experience/{id}/analyze` answers with. Pure, tested. */
object ServerMoments {
    fun parse(json: String): List<ServerMoment> {
        val array = JSONArray(json)
        return (0 until array.length()).map { i ->
            val item = array.getJSONObject(i)
            ServerMoment(
                bpm = item.getInt("bpm"),
                at = instant(item.getString("timestamp")),
                durationSec = item.optInt("duration_seconds", 0),
                label = item.optString("matched_label", "").ifBlank { null }
                    .takeUnless { item.isNull("matched_label") },
                rank = if (item.isNull("rank")) null else item.optInt("rank"),
            )
        }
    }

    /** The server writes `...Z` or `...+00:00`; both are one instant. */
    fun instant(text: String): Instant = OffsetDateTime.parse(text).toInstant()
}
