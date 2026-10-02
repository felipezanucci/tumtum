package cc.tumtum.app.domain

/**
 * Whether a night can be shown in its event's feed, and when not, why
 * (28/09, item 31). The founder never found the feed button: it appeared only
 * on the screen after sharing somewhere else, and only when posting would
 * work — so a night that could not post showed nothing, and one that could
 * hid the way. Now every door to the feed is always there; when it cannot
 * open, it says which of these it is, under it.
 *
 * The server is still the judge (a 403 is said in its own words); this only
 * keeps the phone from offering what it already knows will be refused.
 */
object FeedGate {

    /** The server counts a night as attendance with this many readings inside the event (ATTENDANCE_MIN_READINGS). */
    const val MIN_EVENT_READINGS = 60

    sealed interface Closed {
        /** Recorded under another account (#58): only that account may post it. */
        data object OtherAccount : Closed

        /** Not on the server because "Guardar a noite" is off: the way is the key (02/10). */
        data object NotKept : Closed

        /** The key is on and the night has not reached the server yet: it is on its way, or a retry is. */
        data object NotUploaded : Closed

        /** On the server, but its event is not: there is no feed to post to. */
        data object NoEvent : Closed

        /** On the server with too few readings inside the event's window. */
        data class FewReadings(val readings: Int) : Closed
    }

    /**
     * Null when the night can be posted. [keepingNights] is the account's
     * "Guardar a noite" as last heard (null when unknown); [eventReadings]
     * is null when the server has not said, and then only the server decides.
     */
    fun closed(
        serverSessionId: String?,
        serverEventId: String?,
        eventReadings: Int?,
        ownerUserId: String?,
        viewerId: String?,
        keepingNights: Boolean?,
    ): Closed? = when {
        ownerUserId != null && viewerId != null && ownerUserId != viewerId -> Closed.OtherAccount
        keepingNights == false -> Closed.NotKept
        serverSessionId == null && keepingNights == true -> Closed.NotUploaded
        serverSessionId == null -> Closed.NotKept
        serverEventId == null -> Closed.NoEvent
        eventReadings != null && eventReadings < MIN_EVENT_READINGS -> Closed.FewReadings(eventReadings)
        else -> null
    }
}
