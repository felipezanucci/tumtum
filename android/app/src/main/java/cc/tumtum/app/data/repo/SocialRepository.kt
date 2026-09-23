package cc.tumtum.app.data.repo

import cc.tumtum.app.data.api.ServerCrowd
import cc.tumtum.app.data.api.ServerFeedDate
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
        /** The tour this event belongs to, whose feed this is (#65); null for a show on its own. */
        val series: ServerSeries? = null,
        /** The dates this feed covers, oldest first — more than one only for a tour. */
        val dates: List<ServerFeedDate> = emptyList(),
        /** Posts a block kept out — the empty feed says so instead of "nobody posted" (#63). */
        val hiddenByBlock: Int = 0,
    ) : FeedState {
        val isEmpty: Boolean get() = posts.isEmpty()

        /** Several dates in one feed: each post says which, and the night filter appears. */
        val spansDates: Boolean get() = dates.size > 1
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
 * Where a feed's taps go. Since #65 there is one feed per event — the tour's
 * when there is one — so this is always the event the fan came in through;
 * the server finds the post in the tour from there.
 */
data class FeedTarget(val id: String) {
    val path: String get() = "/api/events/$id"
}

/**
 * What posting a moment did (#58, 23/09).
 *
 * `post()` used to reduce every answer to a Boolean, so the server's own
 * sentence — "Essa noite não é sua ou não é deste evento." — became "Não deu
 * pra mostrar agora", and nobody could tell a refusal from a network blip.
 * Felipe hit exactly that: signed in with one account, posting a night the
 * other account had uploaded. The server's words are kept and said.
 */
sealed interface PostResult {
    data object Posted : PostResult

    /** The server said no, in its own words. */
    data class Refused(val detail: String) : PostResult

    data object SignedOut : PostResult

    data class Failed(val offline: Boolean) : PostResult
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
        FeedState.Ready(
            feed.eventName, feed.venue, feed.posts, feed.series, feed.dates, feed.hiddenByBlock,
        )
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
    ): PostResult = try {
        api.postMoment(serverEventId, serverSessionId, bpm, at, label, quote, skin, toSeries)
        PostResult.Posted
    } catch (e: CancellationException) {
        throw e
    } catch (e: TumtumApi.ApiException) {
        when {
            e.code == 401 -> PostResult.SignedOut
            e.code in 400..499 && e.detail.isNotBlank() -> PostResult.Refused(e.detail)
            else -> PostResult.Failed(offline = false)
        }
    } catch (e: IOException) {
        PostResult.Failed(offline = true)
    } catch (e: Exception) {
        PostResult.Failed(offline = false)
    }

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
