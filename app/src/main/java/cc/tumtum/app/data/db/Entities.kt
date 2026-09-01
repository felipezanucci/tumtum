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
)
