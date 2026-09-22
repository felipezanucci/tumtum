package cc.tumtum.app.data.repo

import cc.tumtum.app.data.api.ServerCrowd
import cc.tumtum.app.data.api.ServerPost
import cc.tumtum.app.data.api.ServerSeries
import cc.tumtum.app.data.api.TumtumApi
import java.io.IOException
import java.time.Instant
import kotlin.coroutines.cancellation.CancellationException

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
        /** On an event's feed: the tour above it, if any. On a series feed: itself. */
        val series: ServerSeries? = null,
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

/**
 * Which feed a tap was made in: one night's rolê, or the tour above it (#33).
 * The actions are the same on both; only the address and the gate differ.
 */
sealed interface FeedTarget {
    val id: String
    val path: String

    data class Event(override val id: String) : FeedTarget {
        override val path: String get() = "/api/events/$id"
    }

    data class Series(override val id: String) : FeedTarget {
        override val path: String get() = "/api/series/$id"
    }
}

/**
 * What a tap on the feed actually did.
 *
 * Until 22/09 every action here was `runCatching { … }.isSuccess`, and the
 * screen ignored even that Boolean. A reaction the server refused, one that
 * never left the phone and one that went through were the same nothing on
 * screen — Felipe tapped SENTI TB, the counter did not move, and nobody could
 * say why, because the answer had been thrown away twice. Same shape as the
 * football search that same evening (#43).
 */
sealed interface Outcome<out T> {
    data class Done<T>(val value: T) : Outcome<T>

    data object NotThere : Outcome<Nothing>

    data object SignedOut : Outcome<Nothing>

    data class Failed(val offline: Boolean) : Outcome<Nothing>
}

class SocialRepository(private val api: TumtumApi) {

    suspend fun feed(serverEventId: String): FeedState = guard(
        onRefused = FeedState.NotThere,
        onSignedOut = FeedState.SignedOut,
        onFailed = { FeedState.Failed(it) },
    ) {
        val feed = api.eventFeed(serverEventId)
        FeedState.Ready(feed.eventName, feed.venue, feed.posts, feed.series)
    }

    suspend fun seriesFeed(seriesId: String): FeedState = guard(
        onRefused = FeedState.NotThere,
        onSignedOut = FeedState.SignedOut,
        onFailed = { FeedState.Failed(it) },
    ) {
        val feed = api.seriesFeed(seriesId)
        FeedState.Ready(feed.series.name, null, feed.posts, feed.series)
    }

    /** The series an event belongs to; null when it has none *or* the question failed. */
    suspend fun seriesOf(serverEventId: String): ServerSeries? =
        (outcome { api.eventSeries(serverEventId) } as? Outcome.Done)?.value

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
        toSeries: Boolean = false,
    ): Boolean = outcome {
        api.postMoment(serverEventId, serverSessionId, bpm, at, label, quote, skin, toSeries)
    } is Outcome.Done

    suspend fun takeDown(serverEventId: String, postId: String): Outcome<Unit> =
        outcome { api.deletePost(serverEventId, postId) }

    /** The post as the server now has it — the count comes from here, not from a refetch. */
    suspend fun toggleSenti(target: FeedTarget, postId: String): Outcome<ServerPost> =
        outcome { api.toggleSenti(target.path, postId) }

    suspend fun report(target: FeedTarget, postId: String, reason: String): Outcome<Unit> =
        outcome { api.reportPost(target.path, postId, reason) }

    suspend fun block(target: FeedTarget, postId: String): Outcome<Unit> =
        outcome { api.blockAuthor(target.path, postId) }

    suspend fun blocked(): Outcome<List<TumtumApi.BlockedPerson>> = outcome { api.myBlocks() }

    suspend fun unblock(blockId: String): Outcome<Unit> = outcome { api.unblock(blockId) }

    private suspend fun <T> outcome(block: suspend () -> T): Outcome<T> = guard(
        onRefused = Outcome.NotThere,
        onSignedOut = Outcome.SignedOut,
        onFailed = { Outcome.Failed(it) },
    ) { Outcome.Done(block()) }

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
    } catch (e: CancellationException) {
        // **Never a failure.** A cancelled request is one somebody stopped
        // waiting for — here, the screen starting a newer load. Until 22/09 the
        // generic catch below took it (CancellationException *is* an
        // Exception), returned Failed, and the screen painted "Não deu pra
        // carregar o rolê" for a request that was never in trouble: the error
        // Felipe saw for a second after taking his post down.
        throw e
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
