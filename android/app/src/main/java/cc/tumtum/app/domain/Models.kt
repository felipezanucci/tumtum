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
    /** What caused it: the event's timeline, or the person themselves (§5.6). */
    val label: String? = null,
    /** The row on the phone, so the person can name it. 0 for a moment not yet stored. */
    val id: Long = 0,
)

/** Where a night stands with the server (Etapa 2). */
enum class UploadState { PENDING, SENT, ANALYSED, FAILED }

/** Who found the moments on screen: the phone's top-N, or the server's detector. */
enum class MomentsSource { LOCAL, SERVER }

/** O evento marcado pelo usuário — define a janela de leitura (§7). */
data class EventSession(
    val id: Long,
    val name: String,
    val venue: String,
    val startAt: Instant,
    val endAt: Instant?,   // null = ainda ao vivo
    /** The server's id, when the event came from its list or was created there (Etapa 3). */
    val serverEventId: String? = null,
    val eventType: String = "concert",
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
    /** Trava da revela (protocolo): antes deste instante a curva não aparece. */
    val revealAt: Instant? = null,
    val samples: List<HrSample> = emptyList(),
    val gaps: List<Gap> = emptyList(),
    val moments: List<Moment> = emptyList(),
    val serverSessionId: String? = null,
    val uploadState: UploadState = UploadState.PENDING,
    val uploadError: String? = null,
    val momentsSource: MomentsSource = MomentsSource.LOCAL,
    /** The photo behind the last shared black card, when there was one. */
    val photoPath: String? = null,
    /** The account that uploaded it; null when unknown (before 23/09). */
    val ownerUserId: String? = null,
    /** The person asked to keep it on the server (26/09). Without it, nothing uploads. */
    val sendRequested: Boolean = false,
    /** When its readings reached the server; null while they have not. */
    val sentAt: Instant? = null,
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

// ---- Social: the event's feed, over the real server (22/09) ----

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
    /** The server's id for the post, for a reaction or a take-down. */
    val postId: String = "",
    /** Whether the viewer may take it down — shown only where it is true. */
    val mine: Boolean = false,
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
    /** False until a card was chosen: the night exists, its skin does not yet. */
    val published: Boolean = true,
    /** The photo behind the card, drawn on the tile when the skin is black. */
    val photoPath: String? = null,
)
