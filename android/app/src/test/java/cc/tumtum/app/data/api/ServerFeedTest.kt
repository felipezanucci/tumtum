package cc.tumtum.app.data.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The feed's wire format.
 *
 * What these defend is the difference between kinds of nothing. A crowd the
 * server would not publish, a feed nobody posted to, and a field that came
 * back null are three different things, and every one of them used to be
 * rendered by invented people (`FakeSocialRepository`, deleted 22/09).
 */
class ServerFeedTest {

    @Test
    fun `a feed with posts is read whole`() {
        val json = """
            {"event_id":"e1","event_name":"São Paulo × Vitória","venue":"Morumbi",
             "date":"2026-10-10",
             "posts":[
               {"id":"p1","author":{"name":"Felipe Zanucci","initials":"FZ"},
                "bpm":187,"moment_at":"2026-10-10T22:41:00Z","label":"⚽ Gol",
                "quote":"não respondo pelo que veio depois","skin":"PINK",
                "created_at":"2026-10-10T23:00:00Z","reactions":3,
                "reacted_by_me":true,"mine":false}
             ]}
        """.trimIndent()

        val feed = ServerFeed.parse(json)

        assertEquals("São Paulo × Vitória", feed.eventName)
        assertEquals("Morumbi", feed.venue)
        val post = feed.posts.single()
        assertEquals("Felipe Zanucci", post.authorName)
        assertEquals("FZ", post.authorInitials)
        assertEquals(187, post.bpm)
        assertEquals("⚽ Gol", post.label)
        assertEquals(3, post.reactions)
        assertTrue(post.reactedByMe)
        assertFalse(post.mine)
    }

    @Test
    fun `a feed nobody posted to is empty, not broken`() {
        val json = """{"event_id":"e1","event_name":"Rolê","venue":null,
            "date":"2026-10-10","posts":[]}"""
        val feed = ServerFeed.parse(json)

        assertEquals(0, feed.posts.size)
        assertNull(feed.venue)
    }

    @Test
    fun `a post with no name and no words keeps its number`() {
        // A moment nothing named carries no title, never an invented one.
        val json = """
            {"event_id":"e1","event_name":"Rolê","venue":null,"date":"2026-10-10",
             "posts":[{"id":"p1","author":{"name":"Alguém","initials":"AL"},
              "bpm":142,"moment_at":"2026-10-10T22:41:00Z","label":null,
              "quote":null,"skin":"BLACK","created_at":"2026-10-10T23:00:00Z",
              "reactions":0,"reacted_by_me":false,"mine":true}]}
        """.trimIndent()

        val post = ServerFeed.parse(json).posts.single()

        assertNull(post.label)
        assertNull(post.quote)
        assertEquals(142, post.bpm)
        assertTrue(post.mine)
    }

    @Test
    fun `a blank string is read as absent, like a null`() {
        val json = """
            {"event_id":"e1","event_name":"Rolê","venue":"","date":"2026-10-10",
             "posts":[{"id":"p1","author":{"name":"Alguém","initials":"AL"},
              "bpm":142,"moment_at":"2026-10-10T22:41:00Z","label":"",
              "quote":"","skin":"BLACK","created_at":"2026-10-10T23:00:00Z",
              "reactions":0,"reacted_by_me":false,"mine":false}]}
        """.trimIndent()

        val feed = ServerFeed.parse(json)

        assertNull(feed.venue)
        assertNull(feed.posts.single().label)
    }

    @Test
    fun `a crowd the server refuses to publish is not an empty crowd`() {
        // enough=false is the privacy floor, not "nobody felt anything". The
        // count comes back so the screen can say how few we still are.
        val json = """{"measured_nights":2,"enough":false,"shared_count":0,
            "moments":[],"top":null}"""

        val crowd = ServerCrowd.parse(json)

        assertFalse(crowd.enough)
        assertEquals(2, crowd.measuredNights)
        assertNull(crowd.top)
    }

    @Test
    fun `a crowd above the floor carries its moments and its top`() {
        val json = """
            {"measured_nights":9,"enough":true,"shared_count":4,
             "moments":[{"at":"2026-10-10T22:41:00Z","people":6,"label":"⚽ Gol"},
                        {"at":"2026-10-10T23:12:00Z","people":4,"label":null}],
             "top":{"at":"2026-10-10T22:41:00Z","people":6,"label":"⚽ Gol"}}
        """.trimIndent()

        val crowd = ServerCrowd.parse(json)

        assertTrue(crowd.enough)
        assertEquals(9, crowd.measuredNights)
        assertEquals(4, crowd.sharedCount)
        assertEquals(2, crowd.moments.size)
        assertNull(crowd.moments[1].label)
        assertEquals(6, crowd.top?.people)
    }

    @Test
    fun `both offset spellings are one instant`() {
        val z = """{"event_id":"e","event_name":"R","venue":null,"date":"2026-10-10",
            "posts":[{"id":"p","author":{"name":"A","initials":"A"},"bpm":100,
            "moment_at":"2026-10-10T22:41:00Z","label":null,"quote":null,
            "skin":"BLACK","created_at":"2026-10-10T23:00:00Z","reactions":0,
            "reacted_by_me":false,"mine":false}]}"""
        val offset = z.replace("22:41:00Z", "19:41:00-03:00")

        assertEquals(
            ServerFeed.parse(z).posts.single().at,
            ServerFeed.parse(offset).posts.single().at,
        )
    }

    @Test
    fun `the answer to SENTI TB is read on its own, count and all`() {
        // #47, 22/09: this response used to be dropped on the wire, so the
        // screen could not show what its own tap had done.
        val json = """
            {"id":"p1","author":{"name":"Felipe Zanucci","initials":"FZ"},
             "bpm":109,"moment_at":"2026-09-22T21:10:00Z","label":null,"quote":null,
             "skin":"BLACK","created_at":"2026-09-22T21:20:00Z","reactions":1,
             "reacted_by_me":true,"mine":true}
        """.trimIndent()

        val post = ServerPost.parse(json)

        assertEquals("p1", post.id)
        assertEquals(1, post.reactions)
        assertTrue(post.reactedByMe)
    }

    @Test
    fun `an event in a tour carries the door to it`() {
        val json = """{"event_id":"e1","event_name":"Eras SP N1","venue":"Allianz","date":"2026-11-14",
            "posts":[],"series":{"id":"s1","name":"The Eras Tour — Brasil","kind":"tour","dates":5}}"""

        val series = ServerFeed.parse(json).series

        assertEquals("The Eras Tour — Brasil", series?.name)
        assertEquals(5, series?.dates)
    }

    @Test
    fun `an event in no tour carries no door, and JSON null is read as none`() {
        val json = """{"event_id":"e1","event_name":"Rolê","venue":null,"date":"2026-10-10","posts":[]}"""

        assertNull(ServerFeed.parse(json).series)
        assertNull(ServerSeries.parseOrNull("null"))
    }

    @Test
    fun `a tour's feed carries its dates, each post its night, and what a block hid`() {
        // #65: one feed per event — the tour's — with the night as a filter;
        // #63: an empty feed must know when a block emptied it.
        val json = """
            {"event_id":"rio","event_name":"Rihanna Rio","venue":null,"date":"2026-10-26",
             "series":{"id":"t1","name":"Rihanna no Brasil","kind":"tour","dates":2},
             "events":[{"id":"sp","name":"Rihanna SP","date":"2026-10-24","city":"São Paulo"},
                       {"id":"rio","name":"Rihanna Rio","date":"2026-10-26","city":"Rio de Janeiro"}],
             "hidden_by_block":1,
             "posts":[{"id":"p1","author":{"name":"Ana","initials":"A"},"bpm":150,
               "moment_at":"2026-10-24T22:41:00Z","label":null,"quote":null,"skin":"BLACK",
               "created_at":"2026-10-24T23:00:00Z","reactions":0,"reacted_by_me":false,
               "mine":false,"event_id":"sp","event_name":"Rihanna SP",
               "event_date":"2026-10-24","event_city":"São Paulo"}]}
        """.trimIndent()

        val feed = ServerFeed.parse(json)

        assertEquals("Rihanna no Brasil", feed.series?.name)
        assertEquals(listOf("sp", "rio"), feed.dates.map { it.id })
        assertEquals(1, feed.hiddenByBlock)
        assertEquals("sp", feed.posts.single().eventId)
        assertEquals("São Paulo", feed.posts.single().eventCity)
    }

    @Test
    fun `a feed from an older server has one date and nothing hidden`() {
        val json = """{"event_id":"e1","event_name":"Show","venue":null,
            "date":"2026-10-10","posts":[]}"""
        val feed = ServerFeed.parse(json)

        assertEquals(0, feed.dates.size)
        assertEquals(0, feed.hiddenByBlock)
    }
}
