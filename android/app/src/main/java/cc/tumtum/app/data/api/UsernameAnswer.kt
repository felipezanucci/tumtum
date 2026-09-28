package cc.tumtum.app.data.api

import org.json.JSONObject

/**
 * The server's word on one @ (`GET /api/auth/username/{name}`, 28/09): the
 * name as it would be stored, whether it can be had, and — when it cannot —
 * the sentence to show under the field. The reasons are the server's own
 * ("Esse @ já tem dono. Tenta outro.", "Esse @ é da TumTum."), so the phone
 * never keeps its own list of taken or reserved names again.
 */
data class UsernameAnswer(val username: String, val available: Boolean, val reason: String?) {
    companion object {
        fun parse(json: String, asked: String): UsernameAnswer {
            val o = JSONObject(json)
            return UsernameAnswer(
                username = Json.text(o, "username") ?: asked,
                // Missing means no: "disponível" is said only on an explicit yes.
                available = o.optBoolean("available", false),
                reason = Json.text(o, "reason"),
            )
        }
    }
}
