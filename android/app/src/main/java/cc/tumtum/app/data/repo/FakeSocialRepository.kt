package cc.tumtum.app.data.repo

import cc.tumtum.app.data.prefs.Account
import cc.tumtum.app.domain.CrowdPeak
import cc.tumtum.app.domain.CrowdStats
import cc.tumtum.app.domain.EventFeed
import cc.tumtum.app.domain.FeedMoment
import cc.tumtum.app.domain.GalleryNight
import cc.tumtum.app.domain.Night
import cc.tumtum.app.domain.PublicProfile
import cc.tumtum.app.domain.Skin
import cc.tumtum.app.domain.SocialUser
import cc.tumtum.app.domain.UpcomingEvent
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * O social ainda não tem backend (§2): repositório trocável com dados fake,
 * atrás de interface. Os dados são os mesmos dos mockups (b5/b6/b8/a4).
 */
interface SocialRepository {
    val upcoming: StateFlow<UpcomingEvent?>
    val friendsFeed: StateFlow<List<FeedMoment>>
    val eventFeed: StateFlow<EventFeed>
    fun crowdStats(eventName: String): CrowdStats
    fun profile(handle: String): PublicProfile?
    fun toggleSenti(momentId: Long)
    fun toggleFollow(handle: String)
    fun postOwnMoment(night: Night, skin: Skin, account: Account, quote: String? = null)
}

class FakeSocialRepository : SocialRepository {

    private val mariana = SocialUser("mariana", "Mariana Alves", "MA", Skin.PINK)
    private val rodcosta = SocialUser("rodcosta", "Rodrigo Costa", "RC", Skin.YELLOW)
    private val jureis = SocialUser("jureis", "Juliana Reis", "JU", Skin.BLACK, city = "na pista premium")
    private val pbarros = SocialUser("pbarros", "Paula Barros", "PB", Skin.YELLOW)
    private val ltoledo = SocialUser("ltoledo", "Lucas Toledo", "LT", Skin.PINK)

    private val ids = AtomicLong(100)

    override val upcoming = MutableStateFlow<UpcomingEvent?>(
        UpcomingEvent(eventName = "Taylor Swift", friendsConfirmed = 3),
    )

    override val friendsFeed = MutableStateFlow(
        listOf(
            FeedMoment(
                id = 1, user = mariana,
                eventName = "Lollapalooza — Dia 2", whenLabel = "ontem",
                title = "EU TAVA TRANQUILO.\nAÍ VEIO ISSO.",
                bpm = 194, metaLabel = "bpm às 23h47",
                quote = "aqui acabou meu psicológico",
                skin = Skin.PINK, sentiCount = 12, sentiByMe = true,
            ),
            FeedMoment(
                id = 2, user = rodcosta,
                eventName = "Palmeiras x Corinthians", whenLabel = "sáb",
                title = "FOI AQUI QUE EU PERDI\nA COMPOSTURA.",
                bpm = 176, metaLabel = "bpm aos 89 do segundo tempo",
                quote = "não respondo pelo que aconteceu depois",
                skin = Skin.BLACK, sentiCount = 47, sentiByMe = false,
            ),
        ),
    )

    override val eventFeed = MutableStateFlow(
        EventFeed(
            eventName = "Taylor Swift\n— São Paulo, N2",
            venueDate = "Estádio do Morumbi · 14.11.26",
            sharedCount = 8734,
            collectivePeakLabel = "22H41",
            samePeakPct = 64,
            moments = listOf(
                FeedMoment(
                    id = 3, user = jureis,
                    eventName = "Taylor Swift — N2", whenLabel = "na pista premium",
                    title = "ATÉ AQUI EU TAVA\nME COMPORTANDO.",
                    bpm = 201, metaLabel = "bpm às 22h41",
                    quote = "meu corpo entregou tudo",
                    skin = Skin.PINK, sentiCount = 312, sentiByMe = true,
                ),
            ),
            compactMoments = listOf(
                FeedMoment(
                    id = 4, user = pbarros,
                    eventName = "Taylor Swift — N2", whenLabel = "",
                    title = "", bpm = 188, metaLabel = "",
                    quote = "provas de que eu senti demais",
                    skin = Skin.YELLOW, sentiCount = 0, sentiByMe = false,
                ),
                FeedMoment(
                    id = 5, user = ltoledo,
                    eventName = "Taylor Swift — N2", whenLabel = "",
                    title = "", bpm = 179, metaLabel = "",
                    quote = "esse foi o exato momento do surto",
                    skin = Skin.PINK, sentiCount = 0, sentiByMe = false,
                ),
            ),
            userWasThere = true,
        ),
    )

    private val followed = MutableStateFlow<Set<String>>(emptySet())

    /** a4 — agregado, sem ranking de BPM entre corpos. */
    override fun crowdStats(eventName: String): CrowdStats = CrowdStats(
        eventName = eventName,
        sharedCount = 3412,
        cohortPct = 71,
        windowStartLabel = "23h45",
        windowEndLabel = "23h49",
        peaks = listOf(
            CrowdPeak("23h47", "O pico coletivo da noite", 2423, highlight = true),
            CrowdPeak("22h58", "Segundo momento mais coletivo", 1877, highlight = false),
            CrowdPeak("21h33", "Abertura do set", 1204, highlight = false),
        ),
    )

    override fun profile(handle: String): PublicProfile? {
        val user = listOf(mariana, rodcosta, jureis, pbarros, ltoledo).find { it.handle == handle } ?: return null
        return PublicProfile(
            user = user.copy(city = "São Paulo", tribes = listOf("FESTIVALEIRO", "PALMEIRAS")),
            nightCount = 14,
            friendCount = 89,
            recordBpm = 194,
            publicNights = listOf(
                GalleryNight(-1, "LOLLA D2", "22.03.26", 194, Skin.PINK),
                GalleryNight(-2, "PALMEIRAS", "08.03.26", 176, Skin.BLACK),
                GalleryNight(-3, "COLDPLAY", "14.02.26", 142, Skin.YELLOW),
            ),
            followedByMe = handle in followed.value,
        )
    }

    override fun toggleSenti(momentId: Long) {
        fun FeedMoment.toggled() =
            if (id == momentId) {
                copy(sentiByMe = !sentiByMe, sentiCount = if (sentiByMe) sentiCount - 1 else sentiCount + 1)
            } else this
        friendsFeed.update { list -> list.map { it.toggled() } }
        eventFeed.update { feed ->
            feed.copy(
                moments = feed.moments.map { it.toggled() },
                compactMoments = feed.compactMoments.map { it.toggled() },
            )
        }
    }

    override fun toggleFollow(handle: String) {
        followed.update { if (handle in it) it - handle else it + handle }
    }

    /** Compartilhar é sempre ativo: só entra no feed o que o usuário mandou. */
    override fun postOwnMoment(night: Night, skin: Skin, account: Account, quote: String?) {
        val peakLabel = DateTimeFormatter.ofPattern("HH'h'mm")
            .format(night.peakAt.atZone(ZoneId.systemDefault()))
        val moment = FeedMoment(
            id = ids.incrementAndGet(),
            user = SocialUser(account.username, account.name, account.initials, Skin.BLACK),
            eventName = night.eventName,
            whenLabel = "agora",
            title = "EU TAVA TRANQUILO.\nAÍ VEIO ISSO.",
            bpm = night.peakBpm,
            metaLabel = "bpm às $peakLabel",
            quote = quote ?: "essa noite ficou registrada",
            skin = skin,
            sentiCount = 0,
            sentiByMe = false,
        )
        friendsFeed.update { listOf(moment) + it }
    }
}
