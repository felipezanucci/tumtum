package cc.tumtum.app.data.api

import cc.tumtum.app.domain.ConsentText
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The consent wire format (26/09). What these defend: a purpose the server
 * did not mention is not consent, a revoked one reads as off, and the body
 * the phone sends names only the purposes it means to change.
 */
class ConsentsTest {

    private val snapshot = """
        {"text_version":"2026-09-26","consents":[
          {"purpose":"terms","granted":true,"granted_at":"2026-09-26T12:00:00Z","revoked_at":null,"text_version":"2026-09-26"},
          {"purpose":"read_heart_rate","granted":true,"granted_at":"2026-09-26T12:00:01+00:00","revoked_at":null,"text_version":"2026-09-26"},
          {"purpose":"keep_night","granted":false,"granted_at":"2026-09-20T10:00:00Z","revoked_at":"2026-09-25T10:00:00Z","text_version":"2026-09-26"},
          {"purpose":"crowd_stats","granted":false,"granted_at":null,"revoked_at":null,"text_version":null}
        ]}
    """.trimIndent()

    @Test
    fun `a snapshot is read purpose by purpose`() {
        val s = ConsentSnapshot.parse(snapshot)
        assertEquals("2026-09-26", s.textVersion)
        assertTrue(s.granted(ConsentText.TERMS))
        assertTrue(s.granted(ConsentText.READ_HEART_RATE))
        assertNotNull(s.consents.first { it.purpose == ConsentText.TERMS }.grantedAt)
    }

    @Test
    fun `a revoked consent is off, with its dates kept`() {
        val s = ConsentSnapshot.parse(snapshot)
        assertFalse(s.granted(ConsentText.KEEP_NIGHT))
        assertNotNull(s.consents.first { it.purpose == ConsentText.KEEP_NIGHT }.revokedAt)
    }

    @Test
    fun `a purpose the server did not mention is not consent`() {
        val map = ConsentSnapshot.parse(snapshot).asMap()
        assertEquals(ConsentText.PURPOSES.toSet(), map.keys)
        assertFalse(map.getValue(ConsentText.MARKETING))
        assertFalse(map.getValue(ConsentText.ARTIST_COMPARE))
        assertFalse(map.getValue(ConsentText.IMPROVE_DETECTION))
    }

    @Test
    fun `an empty answer grants nothing`() {
        val s = ConsentSnapshot.parse("{}")
        assertEquals(ConsentText.VERSION, s.textVersion)
        assertTrue(s.asMap().values.none { it })
    }

    @Test
    fun `the PUT body carries the version, the means and only the purposes given`() {
        val body = JSONObject(
            ConsentSnapshot.putBody(mapOf(ConsentText.KEEP_NIGHT to true, "made_up" to true), ConsentText.MEANS_TAP),
        )
        assertEquals(ConsentText.VERSION, body.getString("text_version"))
        assertEquals("tap", body.getString("means"))
        val purposes = body.getJSONObject("purposes")
        assertTrue(purposes.getBoolean("keep_night"))
        assertFalse(purposes.has("made_up"))
        assertEquals(1, purposes.length())
    }

    @Test
    fun `a consent_required refusal names its purpose`() {
        val body = """{"detail":"Falta o consentimento.","code":"consent_required","purpose":"keep_night"}"""
        assertEquals("keep_night", ConsentSnapshot.requiredPurpose(body))
    }

    @Test
    fun `a consent_required refusal nested in detail is read the same`() {
        val body = """{"detail":{"detail":"Falta.","code":"consent_required","purpose":"crowd_stats"}}"""
        assertEquals("crowd_stats", ConsentSnapshot.requiredPurpose(body))
    }

    @Test
    fun `any other refusal names no purpose`() {
        assertNull(ConsentSnapshot.requiredPurpose("""{"detail":"Você não estava lá."}"""))
        assertNull(ConsentSnapshot.requiredPurpose("not json"))
    }
}
