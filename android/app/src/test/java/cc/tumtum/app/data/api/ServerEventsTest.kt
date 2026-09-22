package cc.tumtum.app.data.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.json.JSONObject
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/** The server's event list, read and ordered for tonight. */
class ServerEventsTest {

    private val sp: ZoneId = ZoneId.of("America/Sao_Paulo")

    private fun at(y: Int, m: Int, d: Int, h: Int, min: Int = 0) = ZonedDateTime.of(y, m, d, h, min, 0, 0, sp).toInstant()

    private val json = """[
        {"id":"far","name":"Primavera Sound","date":"2026-12-05","venue":"Interlagos","city":"São Paulo","event_type":"festival",
         "subtitle":null,"country":"BR","start_time":null,"end_time":null,"external_id":null,"cover_image_url":null,"created_at":"2026-09-01T00:00:00Z"},
        {"id":"tonight","name":"São Paulo × Vitória","date":"2026-10-10","venue":"MorumBIS","city":"São Paulo","event_type":"sports",
         "subtitle":null,"country":"BR","start_time":"16:00:00Z","end_time":"18:15:00Z","external_id":null,"cover_image_url":null,"created_at":"2026-09-01T00:00:00Z"},
        {"id":"past","name":"Realness Festival","date":"2026-08-29","venue":null,"city":null,"event_type":"festival",
         "subtitle":null,"country":null,"start_time":"21:00:00-03:00","end_time":"03:30:00-03:00","external_id":null,"cover_image_url":null,"created_at":"2026-08-01T00:00:00Z"}
    ]"""

    @Test
    fun `reads the fields the sheet shows`() {
        val events = ServerEvents.parse(json, sp)
        assertEquals(3, events.size)
        assertEquals("10/10 · São Paulo × Vitória", events[1].label)
        assertEquals("sports", events[1].eventType)
        assertNull(events[2].venue)
        assertNull(events[2].city)
    }

    @Test
    fun `blank and missing venues are none, never the word null`() {
        val blank = ServerEvents.from(JSONObject("""{"id":"a","name":"A","date":"2026-10-10","venue":"  ","event_type":"concert"}"""))
        assertNull(blank.venue)
        assertNull(blank.city)
        val explicitNull = ServerEvents.from(JSONObject("""{"id":"b","name":"B","date":"2026-10-10","venue":null,"city":null}"""))
        assertNull(explicitNull.venue)
        assertEquals("concert", explicitNull.eventType)
    }

    @Test
    fun `tonight comes first, whatever the server's order`() {
        val nearest = ServerEvents.nearest(ServerEvents.parse(json, sp), LocalDate.of(2026, 10, 10))
        assertEquals(listOf("tonight", "past", "far"), nearest.map { it.id })
    }

    @Test
    fun `the list is capped`() {
        val many = (1..20).map { ServerEvent("e$it", "E$it", LocalDate.of(2026, 10, it), null, null, "concert") }
        assertEquals(8, ServerEvents.nearest(many, LocalDate.of(2026, 10, 10)).size)
        assertEquals("e10", ServerEvents.nearest(many, LocalDate.of(2026, 10, 10)).first().id)
    }

    // --- 21/09: the times the server always sent and the app threw away ---

    @Test
    fun `start and end are the server's digits on the phone's clock, whatever the offset`() {
        val events = ServerEvents.parse(json, sp).associateBy { it.id }
        // "16:00:00Z" is 16h00 in São Paulo: the Z is the column's, not the match's.
        assertEquals(at(2026, 10, 10, 16), events.getValue("tonight").startAt)
        assertEquals(at(2026, 10, 10, 18, 15), events.getValue("tonight").endAt)
        // An end before the start crossed midnight.
        assertEquals(at(2026, 8, 29, 21), events.getValue("past").startAt)
        assertEquals(at(2026, 8, 30, 3, 30), events.getValue("past").endAt)
        assertNull(events.getValue("far").startAt)
        assertNull(events.getValue("far").endAt)
    }

    @Test
    fun `wall clock reads only the hour and minute`() {
        assertEquals(LocalTime.of(19, 30), ServerEvents.wallClock("19:30:00Z"))
        assertEquals(LocalTime.of(19, 30), ServerEvents.wallClock("19:30:00.000000+00:00"))
        assertEquals(LocalTime.of(19, 30), ServerEvents.wallClock("19:30"))
        assertNull(ServerEvents.wallClock(null))
        assertNull(ServerEvents.wallClock("19h30"))
        assertNull(ServerEvents.wallClock("25:00:00Z"))
    }

    @Test
    fun `an event with no time stands nowhere against now`() {
        val far = ServerEvents.parse(json, sp).first { it.id == "far" }
        val now = at(2026, 12, 5, 20)
        assertFalse(far.isUpcomingAt(now))
        assertFalse(far.isLiveAt(now))
        assertFalse(far.isPastAt(now))
    }

    @Test
    fun `live is between start and end, and an event with no end lasts a night`() {
        val match = ServerEvents.parse(json, sp).first { it.id == "tonight" }
        assertTrue(match.isUpcomingAt(at(2026, 10, 10, 15, 59)))
        assertTrue(match.isLiveAt(at(2026, 10, 10, 16)))
        assertTrue(match.isLiveAt(at(2026, 10, 10, 18, 14)))
        assertTrue(match.isPastAt(at(2026, 10, 10, 18, 15)))
        val openEnded = ServerEvents.from(JSONObject("""{"id":"x","name":"X","date":"2026-10-10","start_time":"21:00:00Z"}"""), sp)
        assertTrue(openEnded.isLiveAt(at(2026, 10, 11, 1, 59)))
        assertTrue(openEnded.isPastAt(at(2026, 10, 11, 2)))
    }

    @Test
    fun `a fan sees what is going on and what is coming, soonest first, and never an event without a time`() {
        val events = ServerEvents.parse(json, sp) + listOf(
            ServerEvents.from(JSONObject("""{"id":"soon","name":"Soon","date":"2026-09-22","start_time":"20:00:00Z"}"""), sp),
            ServerEvents.from(JSONObject("""{"id":"live","name":"Live","date":"2026-09-21","start_time":"18:00:00Z","end_time":"23:00:00Z"}"""), sp),
        )
        // "far" has no time and "past" is over; neither is offered.
        assertEquals(listOf("live", "soon", "tonight"), ServerEvents.forFan(events, now = at(2026, 9, 21, 19)).map { it.id })
    }

    @Test
    fun `an event that ended simply leaves the list`() {
        val match = ServerEvents.parse(json, sp).first { it.id == "tonight" }
        assertEquals(listOf("tonight"), ServerEvents.forFan(listOf(match), now = at(2026, 10, 10, 18, 14)).map { it.id })
        assertEquals(emptyList<String>(), ServerEvents.forFan(listOf(match), now = at(2026, 10, 10, 18, 15)).map { it.id })
    }

    @Test
    fun `the fan's list is capped`() {
        val many = (1..10).map {
            ServerEvents.from(JSONObject("""{"id":"e$it","name":"E$it","date":"2026-10-${"%02d".format(it)}","start_time":"20:00:00Z"}"""), sp)
        }
        assertEquals(listOf("e5", "e6", "e7"), ServerEvents.forFan(many, now = at(2026, 10, 5, 12), limit = 3).map { it.id })
    }
}
