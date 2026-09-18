package cc.tumtum.app.data.api

import cc.tumtum.app.domain.HrSample
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

/** The body of a night upload: what goes on the wire, and what is kept off it on purpose. */
class SessionPayloadTest {

    private val t0: Instant = Instant.parse("2026-10-10T21:30:00Z")

    @Test
    fun `readings outside the server's range are dropped, not sent`() {
        val body = SessionPayload.build(
            t0, t0.plusSeconds(3), "Polar H10",
            listOf(HrSample(t0, 0), HrSample(t0.plusSeconds(1), 88), HrSample(t0.plusSeconds(2), 251)),
        )
        val points = body.getJSONArray("data_points")
        assertEquals(1, points.length())
        assertEquals(88, points.getJSONObject(0).getInt("bpm"))
    }

    @Test
    fun `two readings on the same instant keep the first`() {
        val body = SessionPayload.build(
            t0, t0.plusSeconds(2), "Polar H10",
            listOf(HrSample(t0.plusSeconds(1), 90), HrSample(t0.plusSeconds(1), 91), HrSample(t0, 80)),
        )
        val points = body.getJSONArray("data_points")
        assertEquals(2, points.length())
        assertEquals(80, points.getJSONObject(0).getInt("bpm"))
        assertEquals(90, points.getJSONObject(1).getInt("bpm"))
    }

    @Test
    fun `times are ISO instants in UTC`() {
        val body = SessionPayload.build(t0, t0.plusSeconds(1), "Galaxy Fit3", listOf(HrSample(t0, 70)))
        assertEquals("2026-10-10T21:30:00Z", body.getString("start_time"))
        assertEquals("2026-10-10T21:30:00Z", body.getJSONArray("data_points").getJSONObject(0).getString("time"))
        assertEquals("Galaxy Fit3", body.getString("source_device"))
    }
}
