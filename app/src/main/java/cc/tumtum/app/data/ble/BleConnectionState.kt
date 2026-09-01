package cc.tumtum.app.data.ble

import java.util.UUID

object BleUuids {
    /** Heart Rate service — o único perfil que o produto fala (§1.5). */
    val HR_SERVICE: UUID = UUID.fromString("0000180D-0000-1000-8000-00805F9B34FB")
    val HR_MEASUREMENT: UUID = UUID.fromString("00002A37-0000-1000-8000-00805F9B34FB")
    val CCCD: UUID = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")
    val BATTERY_SERVICE: UUID = UUID.fromString("0000180F-0000-1000-8000-00805F9B34FB")
    val BATTERY_LEVEL: UUID = UUID.fromString("00002A19-0000-1000-8000-00805F9B34FB")
}

/** Estado visível da conexão (§10): buscando / conectando / conectado / reconectando. */
sealed class BleConnectionState {
    data object Idle : BleConnectionState()
    data object Scanning : BleConnectionState()
    data object Connecting : BleConnectionState()
    data class Connected(val deviceName: String) : BleConnectionState()
    data class Reconnecting(val attempt: Int) : BleConnectionState()
}

/** Dispositivo achado na varredura: nome + RSSI, tocar para parear (§10). */
data class BleDevice(val name: String, val address: String, val rssi: Int)

/** O que a camada BLE emite para quem persiste (o CaptureService). */
sealed class BleEvent {
    data class Sample(
        val measurement: HrMeasurement,
        val wallClockMs: Long,
        val elapsedRealtimeMs: Long,
    ) : BleEvent()

    data class Battery(val levelPct: Int) : BleEvent()

    data class Connection(
        val type: String,
        val detail: String,
        val rssi: Int? = null,
    ) : BleEvent()
}
