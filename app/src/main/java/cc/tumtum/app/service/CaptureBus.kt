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
    val contactStatus: Int? = null,
    val sensorBatteryPct: Int? = null,
    val deviceName: String? = null,
)

object CaptureBus {
    val status = MutableStateFlow(CaptureStatus())
}
