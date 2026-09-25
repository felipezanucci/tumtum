package cc.tumtum.app.data.prefs

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The operator's tools follow the server's word (item 52, 25/09). A phone
 * whose switches were on under a fan's account "registered" an event the
 * server then refused; the switches alone decide nothing now.
 */
class UserStateTest {

    private fun state(
        sessionUser: String? = "ana",
        operatorUserId: String? = null,
        marks: Boolean = false,
        events: Boolean = false,
        participant: String? = null,
    ) = UserState(
        onboarded = true,
        account = null,
        session = sessionUser?.let { Session(token = "t", userId = it, refreshToken = "r") },
        sourcePackage = null,
        sourceLabel = null,
        participantId = participant,
        operatorMarks = marks,
        operatorEvents = events,
        operatorUserId = operatorUserId,
    )

    @Test
    fun `the account the server called an operator is one`() {
        assertTrue(state(operatorUserId = "ana").isOperator)
    }

    @Test
    fun `another account on the same phone is not, whatever was recorded before`() {
        assertFalse(state(sessionUser = "bia", operatorUserId = "ana").isOperator)
    }

    @Test
    fun `signed out, nobody is an operator`() {
        assertFalse(state(sessionUser = null, operatorUserId = "ana").isOperator)
    }

    @Test
    fun `a switch left on does nothing for a fan`() {
        val fan = state(marks = true, events = true)
        assertFalse(fan.marksOn)
        assertFalse(fan.eventsOn)
    }

    @Test
    fun `the operator's switches work when they are on`() {
        val op = state(operatorUserId = "ana", marks = true, events = true)
        assertTrue(op.marksOn)
        assertTrue(op.eventsOn)
        assertFalse(state(operatorUserId = "ana").marksOn)
    }

    @Test
    fun `the export shows to the operator and on a protocol phone, never to a fan`() {
        assertTrue(state(operatorUserId = "ana").showsExport)
        assertTrue(state(participant = "P01").showsExport)
        assertFalse(state().showsExport)
        assertFalse(state(participant = "  ").showsExport)
    }
}
