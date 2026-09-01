package cc.tumtum.app.domain

import java.time.Instant

/** Pele do card — a forma fica, a pele muda. */
enum class Skin { PINK, BLACK, YELLOW, WHITE }

/** Uma amostra de FC lida do Health Connect. Nunca inventada. */
data class HrSample(val time: Instant, val bpm: Int)

/** Buraco de captura (> 60s sem amostra). Aparece como buraco, visível (§7). */
data class Gap(val start: Instant, val end: Instant)

/** Um momento: trecho em que o coração disparou. */
data class Moment(
    val bpm: Int,
    val at: Instant,
    val durationSec: Int,
    val isPeak: Boolean = false,
)

/** O evento marcado pelo usuário — define a janela de leitura (§7). */
data class EventSession(
    val id: Long,
    val name: String,
    val venue: String,
    val startAt: Instant,
    val endAt: Instant?,   // null = ainda ao vivo
)

/** Uma noite capturada e analisada. */
data class Night(
    val id: Long,
    val eventName: String,
    val venue: String,
    val date: Instant,
    val startAt: Instant,
    val endAt: Instant,
    val peakBpm: Int,
    val peakAt: Instant,
    val coveragePct: Int,
    val momentCount: Int,
    val sourcePackage: String,
    val sourceLabel: String,
    val skin: Skin?,        // null = ainda não publicada
    val published: Boolean,
    val samples: List<HrSample> = emptyList(),
    val gaps: List<Gap> = emptyList(),
    val moments: List<Moment> = emptyList(),
)

/** Fonte disponível no Health Connect, com densidade real medida na janela (b4). */
data class WatchSource(
    val packageName: String,
    val label: String,
    val coveragePct: Int,
    val medianIntervalSec: Int,
    val hasData: Boolean,
    val isBest: Boolean,
)

// ---- Social (repositório fake até o backend existir, §2) ----

data class SocialUser(
    val handle: String,
    val displayName: String,
    val initials: String,
    val avatarSkin: Skin,
    val city: String = "",
    val tribes: List<String> = emptyList(),
)

/** A unidade social é o card compartilhado + uma frase. Reação única: SENTI TB. */
data class FeedMoment(
    val id: Long,
    val user: SocialUser,
    val eventName: String,
    val whenLabel: String,
    val title: String,
    val bpm: Int,
    val metaLabel: String,
    val quote: String,
    val skin: Skin,
    val sentiCount: Int,
    val sentiByMe: Boolean,
    val showCurve: Boolean = false,
)

data class UpcomingEvent(
    val eventName: String,
    val friendsConfirmed: Int,
)

data class EventFeed(
    val eventName: String,
    val venueDate: String,
    val sharedCount: Int,
    val collectivePeakLabel: String,
    val samePeakPct: Int,
    val moments: List<FeedMoment>,
    val compactMoments: List<FeedMoment>,
    val userWasThere: Boolean,
)

data class CrowdStats(
    val eventName: String,
    val sharedCount: Int,
    val cohortPct: Int,
    val windowStartLabel: String,
    val windowEndLabel: String,
    val peaks: List<CrowdPeak>,
)

data class CrowdPeak(val timeLabel: String, val label: String, val people: Int, val highlight: Boolean)

data class PublicProfile(
    val user: SocialUser,
    val nightCount: Int,
    val friendCount: Int,
    val recordBpm: Int,
    val publicNights: List<GalleryNight>,
    val followedByMe: Boolean,
)

/** Capa de noite na galeria/perfil: número grande + pele. */
data class GalleryNight(
    val nightId: Long,
    val label: String,
    val dateLabel: String,
    val peakBpm: Int,
    val skin: Skin,
)
