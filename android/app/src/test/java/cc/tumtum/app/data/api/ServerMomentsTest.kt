package cc.tumtum.app.data.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

/** The detector's answer, as the server writes it, read back without losing the name of the moment. */
class ServerMomentsTest {

    @Test
    fun `reads a named moment and an unnamed one`() {
        val json = """[
            {"id":"a","session_id":"s","timestamp":"2026-10-10T22:07:12+00:00","bpm":134,"duration_seconds":166,
             "magnitude":498.0,"timeline_entry_id":"t","rank":1,"matched_label":"⚽ Gol! Calleri"},
            {"id":"b","session_id":"s","timestamp":"2026-10-10T22:40:00Z","bpm":118,"duration_seconds":31,
             "magnitude":87.0,"timeline_entry_id":null,"rank":2,"matched_label":null}
        ]"""
        val moments = ServerMoments.parse(json)

        assertEquals(2, moments.size)
        assertEquals("⚽ Gol! Calleri", moments[0].label)
        assertEquals(Instant.parse("2026-10-10T22:07:12Z"), moments[0].at)
        assertEquals(166, moments[0].durationSec)
        assertEquals(1, moments[0].rank)
        assertNull(moments[1].label)
        assertEquals(Instant.parse("2026-10-10T22:40:00Z"), moments[1].at)
    }

    @Test
    fun `an empty answer is an empty list, not an error`() {
        assertEquals(0, ServerMoments.parse("[]").size)
    }

    @Test
    fun `an unnamed moment carries the server's guesses, a named one carries none`() {
        val json = """[
            {"id":"a","session_id":"s","timestamp":"2026-10-10T22:07:12Z","bpm":134,"duration_seconds":166,
             "magnitude":498.0,"timeline_entry_id":null,"rank":1,"matched_label":null,
             "candidate_labels":["Música 9","Música 8","Música 7"]},
            {"id":"b","session_id":"s","timestamp":"2026-10-10T22:40:00Z","bpm":118,"duration_seconds":31,
             "magnitude":87.0,"timeline_entry_id":"t","rank":2,"matched_label":"GOL","candidate_labels":[]}
        ]"""
        val moments = ServerMoments.parse(json)

        assertEquals(listOf("Música 9", "Música 8", "Música 7"), moments[0].candidates)
        assertNull(moments[0].label)
        assertEquals(emptyList<String>(), moments[1].candidates)
    }

    @Test
    fun `a server that predates the guess list is read as before`() {
        val json = """[{"id":"a","session_id":"s","timestamp":"2026-10-10T22:07:12Z","bpm":134,
            "duration_seconds":166,"magnitude":498.0,"timeline_entry_id":null,"rank":1,"matched_label":null}]"""
        assertEquals(emptyList<String>(), ServerMoments.parse(json)[0].candidates)
    }
}
