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
}
