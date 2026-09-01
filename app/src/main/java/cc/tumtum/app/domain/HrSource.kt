package cc.tumtum.app.domain

import kotlinx.coroutines.flow.StateFlow

/** Estado grosso de uma fonte de FC — o detalhe fino de BLE vive em BleConnectionState. */
enum class SourceState { IDLE, STARTING, ACTIVE, DEGRADED, STOPPED }

/**
 * A abstração comum das fontes de FC (§2): Health Connect (lote, retroativo)
 * e o sensor BLE ao vivo coexistem sob ela e alimentam o mesmo pipeline.
 */
interface HrSource {
    val id: String
    val state: StateFlow<SourceState>

    /** Começa a acompanhar a sessão (o evento). Para o Health Connect é só marcação. */
    suspend fun start(sessionId: Long)

    suspend fun stop()

    companion object {
        const val ID_HEALTH_CONNECT = "health_connect"
        const val ID_BLE = "live_ble"
    }
}
