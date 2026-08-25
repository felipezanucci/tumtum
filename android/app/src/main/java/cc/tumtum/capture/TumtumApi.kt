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
 * The two calls this app makes: sign in, and hand a night's readings to the
 * backend that already knows what to do with them.
 *
 * HttpURLConnection and org.json, on purpose. Both ship with Android, so the
 * APK still carries no dependencies — no version matrix to get wrong while
 * this is built without a device to compile against. Two endpoints do not
 * justify an HTTP client library.
 *
 * Everything here is synchronous and must be called off the main thread; the
 * service owns the thread, this owns the wire format.
 */
class TumtumApi(context: Context) {

    private val prefs = context.getSharedPreferences("tumtum", Context.MODE_PRIVATE)

    var token: String?
        get() = prefs.getString("access_token", null)
        private set(value) {
            prefs.edit().putString("access_token", value).apply()
        }

    val signedIn: Boolean get() = token != null

    class ApiException(val code: Int, message: String) : IOException(message)

    /** Sign in and keep the token for every later call. */
    fun login(email: String, password: String) {
        val body = JSONObject()
            .put("email", email)
            .put("password", password)
        val response = post("/api/auth/login", body.toString(), auth = false)
        token = response.getString("access_token")
    }

    fun logout() {
        token = null
    }

    /**
     * Upload one capture as a session. Returns the session id, which is all
     * the web app needs to show the night: the curve, the peaks and the card
     * live there, not here.
     */
    fun uploadSession(
        firstReadingAtMillis: Long,
        samples: List<CaptureService.Sample>,
        deviceName: String?,
    ): String {
        require(samples.isNotEmpty()) { "Nada capturado" }

        val points = JSONArray()
        val seen = HashSet<Long>()
        for (sample in samples) {
            val at = firstReadingAtMillis + sample.elapsedMs
            // hr_data is keyed by (time, session_id): a duplicate timestamp
            // aborts the whole insert server-side, and losing a night to one
            // repeated instant is never the right trade. Same dedup the web
            // client had to learn.
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

        return post("/api/health/sessions", body.toString(), auth = true).getString("id")
    }

    private fun post(path: String, body: String, auth: Boolean): JSONObject {
        val connection = URL(BASE_URL + path).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            if (auth) {
                connection.setRequestProperty("Authorization", "Bearer $token")
            }
            // A festival's cellular is slow, not absent. The upload is over a
            // megabyte after six hours; give it room before declaring failure.
            connection.connectTimeout = 15_000
            connection.readTimeout = 120_000

            connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

            val code = connection.responseCode
            val text = (if (code in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()?.readText().orEmpty()

            if (code !in 200..299) {
                val detail = runCatching { JSONObject(text).getString("detail") }
                    .getOrDefault("Erro $code")
                throw ApiException(code, detail)
            }
            return JSONObject(text)
        } finally {
            connection.disconnect()
        }
    }

    private fun isoUtc(millis: Long): String =
        FORMAT.get()!!.format(Date(millis))

    companion object {
        const val BASE_URL = "https://tumtum-production.up.railway.app"

        /** SimpleDateFormat is not thread-safe; one per thread is. */
        private val FORMAT = object : ThreadLocal<SimpleDateFormat>() {
            override fun initialValue() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                .apply { timeZone = TimeZone.getTimeZone("UTC") }
        }
    }
}
