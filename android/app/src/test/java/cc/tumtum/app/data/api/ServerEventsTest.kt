package cc.tumtum.app.data.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

/** The server's event list, read and ordered for tonight. */
class ServerEventsTest {

    private val json = """[
        {"id":"far","name":"Primavera Sound","date":"2026-12-05","venue":"Interlagos","city":"São Paulo","event_type":"festival",
         "subtitle":null,"country":"BR","start_time":null,"end_time":null,"external_id":null,"cover_image_url":null,"created_at":"2026-09-01T00:00:00Z"},
        {"id":"tonight","name":"São Paulo × Vitória","date":"2026-10-10","venue":"MorumBIS","city":"São Paulo","event_type":"sports",
         "subtitle":null,"country":"BR","start_time":null,"end_time":null,"external_id":null,"cover_image_url":null,"created_at":"2026-09-01T00:00:00Z"},
        {"id":"past","name":"Realness Festival","date":"2026-08-29","venue":null,"city":null,"event_type":"festival",
         "subtitle":null,"country":null,"start_time":null,"end_time":null,"external_id":null,"cover_image_url":null,"created_at":"2026-08-01T00:00:00Z"}
    ]"""

    @Test
    fun `reads the fields the sheet shows`() {
        val events = ServerEvents.parse(json)
        assertEquals(3, events.size)
        assertEquals("10/10 · São Paulo × Vitória", events[1].label)
        assertEquals("sports", events[1].eventType)
        assertNull(events[2].venue)
    }

    @Test
    fun `tonight comes first, whatever the server's order`() {
        val nearest = ServerEvents.nearest(ServerEvents.parse(json), LocalDate.of(2026, 10, 10))
        assertEquals(listOf("tonight", "past", "far"), nearest.map { it.id })
    }

    @Test
    fun `the list is capped`() {
        val many = (1..20).map { ServerEvent("e$it", "E$it", LocalDate.of(2026, 10, it), null, null, "concert") }
        assertEquals(8, ServerEvents.nearest(many, LocalDate.of(2026, 10, 10)).size)
        assertEquals("e10", ServerEvents.nearest(many, LocalDate.of(2026, 10, 10)).first().id)
    }
}
