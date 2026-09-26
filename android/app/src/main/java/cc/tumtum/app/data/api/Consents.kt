package cc.tumtum.app.data.api

import cc.tumtum.app.domain.ConsentText
import java.time.Instant
import org.json.JSONArray
import org.json.JSONObject

/**
 * The person's consents as the server records them (`GET/PUT /api/consents`,
 * LGPD remediation, 26/09). One entry per purpose, always all seven on the
 * server's side; a purpose missing from the answer reads as **not granted** —
 * absence is never consent.
 */
data class ConsentSnapshot(
    val textVersion: String,
    val consents: List<Entry>,
) {
    data class Entry(
        val purpose: String,
        val granted: Boolean,
        val grantedAt: Instant?,
        val revokedAt: Instant?,
        val textVersion: String?,
    )

    fun granted(purpose: String): Boolean = consents.any { it.purpose == purpose && it.granted }

    /** Every known purpose with its state; unknown or missing ones are false. */
    fun asMap(): Map<String, Boolean> = ConsentText.PURPOSES.associateWith { granted(it) }

    companion object {
        /** Reads the body of GET or PUT. Pure, tested. */
        fun parse(json: String): ConsentSnapshot {
            val o = JSONObject(json)
            val array = o.optJSONArray("consents") ?: JSONArray()
            return ConsentSnapshot(
                textVersion = Json.text(o, "text_version") ?: ConsentText.VERSION,
                consents = (0 until array.length()).mapNotNull { i ->
                    val c = array.optJSONObject(i) ?: return@mapNotNull null
                    val purpose = Json.text(c, "purpose") ?: return@mapNotNull null
                    Entry(
                        purpose = purpose,
                        granted = c.optBoolean("granted", false),
                        grantedAt = instantOrNull(c, "granted_at"),
                        revokedAt = instantOrNull(c, "revoked_at"),
                        textVersion = Json.text(c, "text_version"),
                    )
                },
            )
        }

        /**
         * The PUT body: only the purposes given change on the server, so a
         * screen sends exactly the switches it showed. Unknown keys are
         * dropped here rather than sent for the server to refuse.
         */
        fun putBody(purposes: Map<String, Boolean>, means: String, textVersion: String = ConsentText.VERSION): String {
            val map = JSONObject()
            purposes.filterKeys { it in ConsentText.PURPOSES }.forEach { (k, v) -> map.put(k, v) }
            return JSONObject()
                .put("text_version", textVersion)
                .put("means", means)
                .put("purposes", map)
                .toString()
        }

        /**
         * A 403 body that names a missing consent — `{"code": "consent_required",
         * "purpose": "keep_night"}` — or null for any other refusal.
         */
        fun requiredPurpose(errorBody: String): String? = runCatching {
            val top = JSONObject(errorBody)
            // The contract puts code and purpose beside `detail`; a server that
            // raises them inside `detail` is read the same way.
            val o = if (Json.text(top, "code") != null) top else top.optJSONObject("detail") ?: top
            if (Json.text(o, "code") == "consent_required") Json.text(o, "purpose") else null
        }.getOrNull()

        private fun instantOrNull(o: JSONObject, key: String): Instant? =
            Json.text(o, key)?.let { runCatching { Json.instant(it) }.getOrNull() }
    }
}
