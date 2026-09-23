package cc.tumtum.app.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** O evento marcado — define a janela de leitura (§7). endAt null = ao vivo. */
@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val venue: String,
    val startAt: Long,
    val endAt: Long? = null,
    /** Offset wallClock − elapsedRealtime no início e fim da sessão (§8). */
    val clockOffsetStartMs: Long? = null,
    val clockOffsetEndMs: Long? = null,
    /** The server's id for this event (Etapa 3): chosen from its list, or created at upload. Null = not there yet. */
    val serverEventId: String? = null,
    /** concert · sports · festival — what the server's schema accepts. */
    val eventType: String = "concert",
)

/**
 * A mark: one tap during the capture — the goal, the song, the moment —
 * with the wall clock of the tap. It becomes a timeline entry on the server
 * the next time the night syncs, and until then it lives here.
 */
@Entity(
    tableName = "marks",
    indices = [Index("eventId")],
)
data class MarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventId: Long,
    val at: Long,
    val label: String,
    val entryType: String,
    val synced: Boolean = false,
)

@Entity(tableName = "nights")
data class NightEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventId: Long,
    val eventName: String,
    val venue: String,
    val startAt: Long,
    val endAt: Long,
    val peakBpm: Int,
    val peakAt: Long,
    val coveragePct: Int,
    val momentCount: Int,
    val sourcePackage: String,
    val sourceLabel: String,
    /** Pele escolhida no card (PINK/BLACK/YELLOW/WHITE); null = não publicada. */
    val skin: String? = null,
    val published: Boolean = false,
    /** Offset wallClock − elapsedRealtime no início e fim da sessão (§8). */
    val clockOffsetStartMs: Long? = null,
    val clockOffsetEndMs: Long? = null,
    /** Trava da revela (protocolo do teste): a noite só abre neste instante. Null = sem trava. */
    val revealAt: Long? = null,
    /** The server's id for this night once uploaded (Etapa 2, 2026-09-18). */
    val serverSessionId: String? = null,
    /** PENDING · SENT · ANALYSED · FAILED — where the night stands with the server. */
    val uploadState: String = "PENDING",
    /** Why the last attempt failed, as a key the screen translates: offline · expired · no_session · server:<code> <detail>. */
    val uploadError: String? = null,
    /** LOCAL (the phone's top-N) or SERVER (the detector). The screen says which. */
    val momentsSource: String = "LOCAL",
    /** The photo behind the last shared black card (21/09), a file in filesDir. Null = none. */
    val photoPath: String? = null,
    /**
     * The account that uploaded this night (#58, 23/09) — the server's user
     * id. A night belongs to the account that sent it, and only that account
     * may post from it; null for nights uploaded before this was recorded.
     */
    val ownerUserId: String? = null,
)

/** Amostras cruas da noite — guardadas como lidas. Buraco é ausência de linha. */
@Entity(
    tableName = "samples",
    indices = [Index("nightId")],
)
data class SampleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nightId: Long,
    val time: Long,
    val bpm: Int,
)

@Entity(
    tableName = "moments",
    indices = [Index("nightId")],
)
data class MomentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nightId: Long,
    val bpm: Int,
    val at: Long,
    val durationSec: Int,
    val isPeak: Boolean,
    /** What caused it — the song, the goal — when the event has a timeline. Server moments only. */
    val label: String? = null,
    /**
     * Vestigial. Held the server's guesses for a moment it could not name,
     * which were offered to the person as chips; that went on 22/09 — the
     * app either knows or says nothing, and never asks the fan to remember
     * which song a heartbeat belonged to. Nothing writes or reads it.
     *
     * The column stays because dropping one costs a Room migration that
     * recreates the table on every phone that already has b152, and a
     * botched migration crashes the app on open. An always-null TEXT costs
     * nothing; that trade is not worth taking.
     */
    val candidates: String? = null,
)
