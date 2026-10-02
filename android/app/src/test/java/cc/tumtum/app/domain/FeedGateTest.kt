package cc.tumtum.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** When the feed's doors open, and what they say when they do not (28/09). */
class FeedGateTest {

    private fun closed(
        session: String? = "s1",
        event: String? = "e1",
        readings: Int? = 120,
        owner: String? = "u1",
        viewer: String? = "u1",
        keeping: Boolean? = true,
    ) = FeedGate.closed(session, event, readings, owner, viewer, keeping)

    @Test
    fun `a kept night with enough beats at its event opens`() {
        assertNull(closed())
        // The server has not said how many: it decides, the phone does not guess.
        assertNull(closed(readings = null))
        assertNull(closed(readings = 60))
    }

    @Test
    fun `another account's night is said first`() {
        assertEquals(FeedGate.Closed.OtherAccount, closed(owner = "u2", session = null))
    }

    @Test
    fun `a night not on the server, or with keeping off, needs keeping`() {
        assertEquals(FeedGate.Closed.NotKept, closed(keeping = false))
        // Keeping unknown and nothing on the server: the key is still the way.
        assertEquals(FeedGate.Closed.NotKept, closed(session = null, keeping = null))
    }

    @Test
    fun `with keeping on, a night not yet on the server is on its way, not unkept`() {
        // 02/10: the key sends every night by itself, so the door says "ainda
        // não subiu", and never offers to turn on a key that is already on.
        assertEquals(FeedGate.Closed.NotUploaded, closed(session = null, keeping = true))
    }

    @Test
    fun `a night whose event is not on the server has no feed`() {
        assertEquals(FeedGate.Closed.NoEvent, closed(event = null))
    }

    @Test
    fun `under sixty beats inside the event the feed stays shut`() {
        assertEquals(FeedGate.Closed.FewReadings(17), closed(readings = 17))
    }
}
