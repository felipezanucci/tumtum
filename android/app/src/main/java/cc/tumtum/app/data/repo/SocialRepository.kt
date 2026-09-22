package cc.tumtum.app.data.repo

import cc.tumtum.app.data.api.ServerCrowd
import cc.tumtum.app.data.api.ServerPost
import cc.tumtum.app.data.api.TumtumApi
import java.io.IOException
import java.time.Instant

/**
 * The event's feed, over the real server.
 *
 * **This replaces `FakeSocialRepository`, deleted 2026-09-22.** That one fed
 * the app's *first screen* with invented people carrying invented heart rates
 * — Mariana Alves at 194 bpm, 8,734 people who shared at a Taylor Swift show
 * — and it had been on the Play internal testing track since 18/09. It is the
 * worst instance of the defect this project keeps counting: the app stating
 * something false. Everything before it was a stale control or a message
 * describing the wrong condition; that was fabricated people and fabricated
 * measurements, in a product whose whole promise is that the number is really
 * yours. Nothing here invents anything, ever.
 *
 * The shape of every result is the point. A feed can fail four ways and they
 * are **not** the same thing:
 *
 *  - it loaded and is empty — nobody has posted yet
 *  - you were not there — the server refuses, and says so in its own words
 *  - you are signed out
 *  - it did not load
 *
 * An empty list would collapse all four into "nothing here", which is a claim
 * about the world the app cannot support. So [FeedState] keeps them apart and
 * every screen must say which one it got.
 */
sealed interface FeedState {
    data object Loading : FeedState

    data class Ready(
        val eventName: String,
        val venue: String?,
        val posts: List<ServerPost>,
    ) : FeedState {
        val isEmpty: Boolean get() = posts.isEmpty()
    }

    /** The server refused: this account has no measured night at this event. */
    data object NotThere : FeedState

    data object SignedOut : FeedState

    /** It did not load. [offline] separates "no network" from "the server said no". */
    data class Failed(val offline: Boolean) : FeedState
}

sealed interface CrowdState {
    data object Loading : CrowdState

    /**
     * The crowd, or the honest refusal. [crowd].enough false means the server
     * would not publish a figure over so few people — the screen says how few,
     * never a zero.
     */
    data class Ready(val crowd: ServerCrowd) : CrowdState

    data object NotThere : CrowdState

    data class Failed(val offline: Boolean) : CrowdState
}

class SocialRepository(private val api: TumtumApi) {

    suspend fun feed(serverEventId: String): FeedState = guard(
        onRefused = FeedState.NotThere,
        onSignedOut = FeedState.SignedOut,
        onFailed = { FeedState.Failed(it) },
    ) {
        val feed = api.eventFeed(serverEventId)
        FeedState.Ready(feed.eventName, feed.venue, feed.posts)
    }

    suspend fun crowd(serverEventId: String): CrowdState = guard(
        onRefused = CrowdState.NotThere,
        onSignedOut = CrowdState.Failed(offline = false),
        onFailed = { CrowdState.Failed(it) },
    ) {
        CrowdState.Ready(api.crowd(serverEventId))
    }

    /**
     * Publishes one moment — **only ever from an explicit tap.**
     *
     * A post carries the person's heart rate at a named minute, in front of
     * strangers who happened to be at the same event. Nothing here is derived
     * from a night that merely exists, and [takeDown] is the undo the consent
     * depends on.
     */
    suspend fun post(
        serverEventId: String,
        serverSessionId: String,
        bpm: Int,
        at: Instant,
        label: String?,
        quote: String?,
        skin: String,
    ): Boolean = runCatching {
        api.postMoment(serverEventId, serverSessionId, bpm, at, label, quote, skin)
    }.isSuccess

    suspend fun takeDown(serverEventId: String, postId: String): Boolean =
        runCatching { api.deletePost(serverEventId, postId) }.isSuccess

    suspend fun toggleSenti(serverEventId: String, postId: String): Boolean =
        runCatching { api.toggleSenti(serverEventId, postId) }.isSuccess

    /**
     * Runs [block], turning the two codes that mean something specific into
     * their own states and everything else into an honest failure.
     *
     * 403 is the server saying this account was not at the event, which is a
     * fact about the person and not a fault; 401 is a token that expired. Both
     * deserve their own sentence on screen, and neither is "could not load".
     */
    private suspend fun <T> guard(
        onRefused: T,
        onSignedOut: T,
        onFailed: (offline: Boolean) -> T,
        block: suspend () -> T,
    ): T = try {
        block()
    } catch (e: TumtumApi.ApiException) {
        when (e.code) {
            403 -> onRefused
            401 -> onSignedOut
            else -> onFailed(false)
        }
    } catch (e: IOException) {
        onFailed(true)
    } catch (e: Exception) {
        onFailed(false)
    }
}
