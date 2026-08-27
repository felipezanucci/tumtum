package cc.tumtum.capture

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Everything this app asks of the backend.
 *
 * HttpURLConnection and org.json, on purpose. Both ship with Android, so the
 * APK still carries no runtime dependencies — no version matrix to get wrong
 * while this is built without a device to compile against.
 *
 * Everything here is synchronous and must be called off the main thread. The
 * screens own their threads; this owns the wire format.
 */
class TumtumApi(context: Context) {

    private val prefs = context.getSharedPreferences("tumtum", Context.MODE_PRIVATE)

    var token: String?
        get() = prefs.getString("access_token", null)
        private set(value) {
            prefs.edit().putString("access_token", value).apply()
        }

    /**
     * Signed in means a token that is still alive — not merely one that exists.
     *
     * The distinction cost an afternoon on 2026-08-27. The app hid its login
     * row whenever a token was *stored*, and an expired token is still stored,
     * so it presented itself as signed in, captured happily, and only failed
     * at the upload, with no route back to a password field.
     */
    val signedIn: Boolean
        get() = token?.let { !AccessToken.isExpired(it, System.currentTimeMillis()) } ?: false

    /** Millis before the session dies, so a screen can warn before a show, not after. */
    fun sessionRemainingMillis(): Long? =
        token?.let { AccessToken.remainingMillis(it, System.currentTimeMillis()) }

    class ApiException(val code: Int, message: String) : IOException(message)

    /** Forget the token. Called when the server refuses it, and on sign-out. */
    fun signOut() {
        prefs.edit().remove("access_token").apply()
    }

    // --- Auth ---

    fun login(email: String, password: String) {
        val body = JSONObject().put("email", email).put("password", password)
        val response = JSONObject(request("POST", "/api/auth/login", body.toString(), auth = false))
        token = response.getString("access_token")
    }

    // --- Events ---

    /**
     * The events somebody could be standing in. Public on the backend, so this
     * works before a token exists and keeps working after one dies.
     */
    fun listEvents(): List<EventBrief> =
        EventBrief.listFrom(JSONArray(request("GET", "/api/events", null, auth = false)))

    fun getEvent(eventId: String): EventDetail =
        EventDetail.from(JSONObject(request("GET", "/api/events/$eventId", null, auth = false)))

    /**
     * Correct an event. Absent fields stay untouched — the backend's PATCH
     * semantics — and the call itself goes out as POST because
     * HttpURLConnection refuses the PATCH verb outright; the backend carries
     * a POST twin of the route for exactly this client.
     */
    fun updateEvent(eventId: String, changes: JSONObject) {
        request("POST", "/api/events/$eventId", changes.toString(), auth = true)
    }

    // --- Capture upload ---

    /**
     * Upload one capture as a session, and return its id.
     *
     * `eventId` is what makes the night mean something: without it the card
     * reads "Evento" and no peak can be matched to a moment, because the
     * backend has no timeline to match against.
     */
    fun uploadSession(
        firstReadingAtMillis: Long,
        samples: List<CaptureService.Sample>,
        deviceName: String?,
        eventId: String?,
    ): String {
        require(samples.isNotEmpty()) { "Nada capturado" }

        val points = JSONArray()
        val seen = HashSet<Long>()
        for (sample in samples) {
            val at = firstReadingAtMillis + sample.elapsedMs
            // hr_data is keyed by (time, session_id): a duplicate timestamp
            // aborts the whole insert server-side, and losing a night to one
            // repeated instant is never the right trade.
            if (!seen.add(at / 1000)) continue
            points.put(
                JSONObject()
                    .put("time", isoUtc(at))
                    .put("bpm", sample.bpm)
                    .put("source", "android_ble")
            )
        }

        val body = JSONObject()
            .put("start_time", isoUtc(firstReadingAtMillis))
            .put("end_time", isoUtc(firstReadingAtMillis + samples.last().elapsedMs))
            .put("source_device", deviceName ?: "Sensor BLE")
            .put("data_points", points)
        if (eventId != null) body.put("event_id", eventId)

        return JSONObject(request("POST", "/api/health/sessions", body.toString(), auth = true))
            .getString("id")
    }

    /**
     * Upload a night read from Health Connect, and return its id.
     *
     * Same wire shape as a strap capture, different provenance: the readings
     * carry absolute timestamps (the watch's own clock, via Health Connect)
     * instead of elapsed offsets. Bounds and the per-second dedupe mirror
     * uploadSession — the backend rejects the whole batch over one bad row,
     * and losing a night to one glitched sample is never the right trade.
     */
    fun uploadWatchReadings(
        readings: List<HealthConnectReader.Reading>,
        eventId: String?,
    ): String {
        require(readings.isNotEmpty()) { "Nada lido" }

        val points = JSONArray()
        val seen = HashSet<Long>()
        for (reading in readings) {
            if (reading.bpm < 30 || reading.bpm > 250) continue
            if (!seen.add(reading.timeMillis / 1000)) continue
            points.put(
                JSONObject()
                    .put("time", isoUtc(reading.timeMillis))
                    .put("bpm", reading.bpm)
                    .put("source", "health_connect")
            )
        }
        require(points.length() > 0) { "Nenhuma leitura válida" }

        val body = JSONObject()
            .put("start_time", isoUtc(readings.first().timeMillis))
            .put("end_time", isoUtc(readings.last().timeMillis))
            .put("source_device", "Health Connect")
            .put("data_points", points)
        if (eventId != null) body.put("event_id", eventId)

        return JSONObject(request("POST", "/api/health/sessions", body.toString(), auth = true))
            .getString("id")
    }

    // --- The night, once it is up ---

    fun listSessions(): List<SessionSummary> =
        SessionSummary.listFrom(JSONArray(request("GET", "/api/health/sessions", null, auth = true)))

    /**
     * Run peak detection over a session and store the result.
     *
     * Nothing else triggers this. Creating a session does not, and reading an
     * experience only reads peaks already in the table — so a capture that is
     * never analysed shows an empty list under "Seus picos", which the screen
     * would otherwise report as "your heart kept the same rhythm". After six
     * hours of a festival that is a lie, and it is the reason this call exists
     * at the end of every upload.
     */
    fun analyze(sessionId: String) {
        request("POST", "/api/experience/$sessionId/analyze", "", auth = true)
    }

    fun getExperience(sessionId: String): Experience =
        Experience.from(JSONObject(request("GET", "/api/experience/$sessionId", null, auth = true)))

    // --- Cards ---

    /** Generate a share card and return its id. */
    fun createCard(sessionId: String, peakId: String?): String {
        val body = JSONObject()
            .put("session_id", sessionId)
            .put("card_type", "solo")
            .put("format", "story")
        if (peakId != null) body.put("peak_id", peakId)
        return JSONObject(request("POST", "/api/cards", body.toString(), auth = true))
            .getString("id")
    }

    /** The image is served publicly so a shared link can render it. */
    fun cardImageUrl(cardId: String): String = "$BASE_URL/api/cards/$cardId/image"

    /** Record that a card was handed to the system share sheet. */
    fun recordShare(cardId: String) {
        val body = JSONObject().put("platform", "native")
        runCatching { request("POST", "/api/cards/$cardId/share", body.toString(), auth = true) }
    }

    // --- Wire ---

    private fun request(method: String, path: String, body: String?, auth: Boolean): String {
        val connection = URL(BASE_URL + path).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = method
            connection.setRequestProperty("Accept", "application/json")
            if (auth) {
                connection.setRequestProperty("Authorization", "Bearer $token")
            }
            if (body != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
            }
            // A festival's cellular is slow, not absent. The upload is over a
            // megabyte after six hours; give it room before declaring failure.
            connection.connectTimeout = 15_000
            connection.readTimeout = 120_000

            if (body != null) {
                connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            }

            val code = connection.responseCode
            val text = (if (code in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()?.readText().orEmpty()

            if (code !in 200..299) {
                // FastAPI puts a sentence in `detail` for the errors we raise,
                // and a list of field problems for the ones Pydantic raises.
                // Only the first is worth showing somebody in a dark room.
                val detail = runCatching { JSONObject(text).getString("detail") }
                    .getOrDefault("Erro $code")
                throw ApiException(code, detail)
            }
            return text
        } finally {
            connection.disconnect()
        }
    }

    private fun isoUtc(millis: Long): String = FORMAT.get()!!.format(Date(millis))

    companion object {
        const val BASE_URL = "https://tumtum-production.up.railway.app"

        /** SimpleDateFormat is not thread-safe; one per thread is. */
        private val FORMAT = object : ThreadLocal<SimpleDateFormat>() {
            override fun initialValue() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                .apply { timeZone = TimeZone.getTimeZone("UTC") }
        }
    }
}
