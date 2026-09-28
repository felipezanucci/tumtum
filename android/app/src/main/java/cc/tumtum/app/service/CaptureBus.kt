package cc.tumtum.app.service

import cc.tumtum.app.data.ble.BleConnectionState
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * O serviço é a fonte de verdade da sessão; a UI observa, não controla (§4.2).
 * Este é o espelho observável do que o CaptureService está fazendo.
 */
data class CaptureStatus(
    val active: Boolean = false,
    val eventId: Long? = null,
    val connection: BleConnectionState = BleConnectionState.Idle,
    val samplesWritten: Long = 0,
    val lastBpm: Int? = null,
    /**
     * Whether the last reading was a beat ([cc.tumtum.app.domain.OnSkinTracker]);
     * null before the first. A strap off the skin stays connected and keeps
     * talking (25/09), so connected alone says nothing about a heart.
     */
    val onSkin: Boolean? = null,
    val sensorBatteryPct: Int? = null,
    val deviceName: String? = null,
    /**
     * The strap is paired and the capture is on, but no beat has arrived for
     * [CaptureService.SILENCE_MS] (28/09). Connected or not: a strap out of
     * range, off the chest or asleep all look the same from here — no beat.
     */
    val sensorSilent: Boolean = false,
    /** When beats came back after a silence, for "Voltou." to stay a few seconds; null otherwise. */
    val backAtMs: Long? = null,
    /**
     * The service refused to record (28/09): "Ler sua batida" is not granted
     * for the signed-in account. The capture screen says so with the way to it.
     */
    val refusedReading: Boolean = false,
)

/**
 * A capture that ended without the person's "Encerrar" (28/09): why, the
 * night it left (null when nothing had been written), and when.
 */
data class ForcedStop(val reason: String, val nightId: Long?, val atMs: Long)

object CaptureBus {
    val status = MutableStateFlow(CaptureStatus())

    /** The last forced stop, until a screen has taken it to the night it left. */
    val forcedStop = MutableStateFlow<ForcedStop?>(null)
}
