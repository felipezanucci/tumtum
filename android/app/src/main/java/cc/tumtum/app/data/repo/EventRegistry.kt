package cc.tumtum.app.data.repo

import cc.tumtum.app.AppContainer
import cc.tumtum.app.R
import cc.tumtum.app.data.api.TumtumApi
import cc.tumtum.app.domain.NewEvent
import java.io.IOException
import java.time.Instant
import java.time.ZoneId

/** What registering an event on the server came to: its id, or why it stayed on this phone. */
data class Registration(val serverEventId: String?, val error: String?)

/**
 * The operator registers an event (21/09): it goes to the server first, so
 * every fan's AO VIVO lists it, and only then to this phone. One picked from
 * the server's list is already there. A failure is said, never hidden — the
 * event still works on this phone, and the night creates its twin at upload
 * — but no fan sees it until the server has it.
 */
suspend fun AppContainer.registerEvent(spec: NewEvent): Registration {
    spec.serverEventId?.let { return Registration(it, null) }
    val zone = ZoneId.systemDefault()
    val start = (spec.startAt ?: Instant.now()).atZone(zone)
    val end = spec.endAt?.atZone(zone)
    return try {
        val id = api.createEvent(
            name = spec.name.trim(),
            venue = spec.venue.trim().ifBlank { null },
            date = start.toLocalDate(),
            eventType = spec.eventType,
            startTime = start.toLocalTime(),
            endTime = end?.toLocalTime(),
        )
        Registration(id, null)
    } catch (e: TumtumApi.ApiException) {
        Registration(null, e.detail)
    } catch (e: IOException) {
        Registration(null, appContext.getString(R.string.event_server_offline))
    }
}
