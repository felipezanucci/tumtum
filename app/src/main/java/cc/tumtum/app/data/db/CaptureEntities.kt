package cc.tumtum.app.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Amostra BLE crua, persistida no instante em que chega (§4.2) — nada de buffer.
 * Chaveada pelo evento (a sessão); vira SampleEntity da noite quando a fonte
 * BLE é escolhida ao encerrar. Carimbo duplo de tempo (§8).
 */
@Entity(tableName = "ble_samples", indices = [Index("eventId")])
data class BleSampleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventId: Long,
    val wallClockMs: Long,
    val elapsedRealtimeMs: Long,
    val bpm: Int,
    /** bits 1–2 do 0x2A37: 0 = sem suporte, 2 = sem contato, 3 = contato ok. */
    val contactStatus: Int,
)

/** Intervalo RR (HRV) — só existe se capturado ao vivo (§3.1). Nunca descartado. */
@Entity(tableName = "rr_intervals", indices = [Index("eventId")])
data class RrIntervalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventId: Long,
    val wallClockMs: Long,
    val elapsedRealtimeMs: Long,
    val rrMs: Double,
)

/** Acelerômetro agregado a 1 Hz (§7): magnitude − gravidade, média e desvio da janela. */
@Entity(tableName = "motion", indices = [Index("eventId")])
data class MotionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventId: Long,
    val wallClockMs: Long,
    val elapsedRealtimeMs: Long,
    val magMean: Double,
    val magStd: Double,
)

/**
 * Transições de conexão (§3.3): com isto, cada buraco na curva tem explicação —
 * sensor caiu, Android matou o processo, ou o rádio sumiu na multidão.
 */
@Entity(tableName = "connection_events", indices = [Index("eventId")])
data class ConnectionEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventId: Long,
    val wallClockMs: Long,
    val elapsedRealtimeMs: Long,
    /** SESSION_START, CONNECTING, CONNECTED, SERVICES_READY, DISCONNECTED, RECONNECT_ATTEMPT, BATTERY, RSSI, SESSION_STOP, PROCESS_RESTART, BOOT_RESTART, WATCHDOG. */
    val type: String,
    val detail: String,
    val rssi: Int? = null,
)
