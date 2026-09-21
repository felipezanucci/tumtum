package cc.tumtum.app.domain

import java.time.Instant

/**
 * What the operator's sheet hands back (21/09): an event to register — typed,
 * or picked from the server's list. A fan never produces one of these; a fan
 * activates a [cc.tumtum.app.data.api.ServerEvent] the server already lists.
 */
data class NewEvent(
    val name: String,
    val venue: String,
    val eventType: String = "concert",
    val serverEventId: String? = null,
    /** Set by the Upcoming and Past modes; null means "now". */
    val startAt: Instant? = null,
    /** Set by the Past mode only. */
    val endAt: Instant? = null,
)
