package cc.tumtum.app.data.api

import cc.tumtum.app.domain.HrSample
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.format.DateTimeFormatter

/**
 * The body of `POST /api/health/sessions`, built from a night's samples.
 *
 * Pure, so it can be tested without a phone. Two things happen here on
 * purpose: readings outside the server's accepted range (30–250 bpm) are
 * dropped rather than sent, because one rejected value would fail the whole
 * upload with a validation error; and readings sharing an instant keep only
 * the first, because `hr_data` is keyed by (time, session) — the server does
 * the same, this just keeps the bytes off the wire.
 */
object SessionPayload {
    const val MIN_BPM = 30
    const val MAX_BPM = 250

    fun build(
        startAt: Instant,
        endAt: Instant,
        sourceDevice: String,
        samples: List<HrSample>,
        serverEventId: String? = null,
    ): JSONObject {
        val points = JSONArray()
        var lastMillis: Long? = null
        for (sample in samples.sortedBy { it.time }) {
            if (sample.bpm !in MIN_BPM..MAX_BPM) continue
            val millis = sample.time.toEpochMilli()
            if (millis == lastMillis) continue
            lastMillis = millis
            points.put(JSONObject().put("time", iso(sample.time)).put("bpm", sample.bpm))
        }
        val body = JSONObject()
            .put("start_time", iso(startAt))
            .put("end_time", iso(endAt))
            .put("source_device", sourceDevice)
            .put("data_points", points)
        // The event is what makes the night mean something: without it no
        // moment can be named, because the server has no timeline to match.
        if (serverEventId != null) body.put("event_id", serverEventId)
        return body
    }

    fun iso(instant: Instant): String = DateTimeFormatter.ISO_INSTANT.format(instant)
}
