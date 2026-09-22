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
    fun `a payload that still carries candidate_labels is read without them`() {
        // The field went on 22/09 and a deployed server may still send it for
        // a while. Reading past it must not cost the moment its own numbers.
        val json = """[
            {"id":"a","session_id":"s","timestamp":"2026-10-10T22:07:12Z","bpm":134,"duration_seconds":166,
             "magnitude":498.0,"timeline_entry_id":null,"rank":1,"matched_label":null,
             "candidate_labels":["Música 9","Música 8"]}
        ]"""
        val moment = ServerMoments.parse(json).single()

        assertEquals(134, moment.bpm)
        assertEquals(166, moment.durationSec)
        assertNull(moment.label)
    }
}
