package cc.tumtum.app.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Build
import android.os.SystemClock
import cc.tumtum.app.domain.HrSource
import cc.tumtum.app.domain.SourceState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Fonte BLE ao vivo (§3): GATT padrão 0x180D, nada proprietário.
 *
 * Reconexão é comportamento normal, não bug (§3.2): backoff exponencial
 * 1s→30s, tentando enquanto a sessão estiver ativa, sempre com close()
 * no GATT anterior — vazamento de GATT esgota o limite do Android e a
 * reconexão passa a falhar silenciosamente depois de algumas horas.
 */
@SuppressLint("MissingPermission")
class BleHrSource(
    private val context: Context,
    private val events: (BleEvent) -> Unit,
) : HrSource {

    override val id: String = HrSource.ID_BLE

    private val _state = MutableStateFlow(SourceState.IDLE)
    override val state: StateFlow<SourceState> = _state

    val connection = MutableStateFlow<BleConnectionState>(BleConnectionState.Idle)

    var address: String? = null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val lock = Any()
    private var gatt: BluetoothGatt? = null
    @Volatile private var wanted = false
    @Volatile private var attempt = 0
    private var reconnectJob: Job? = null
    private var watchdogJob: Job? = null

    override suspend fun start(sessionId: Long) {
        val addr = address ?: return
        wanted = true
        attempt = 0
        _state.value = SourceState.STARTING
        connect(addr, autoConnect = false)
    }

    override suspend fun stop() {
        shutdown()
    }

    fun shutdown() {
        wanted = false
        reconnectJob?.cancel()
        watchdogJob?.cancel()
        closeGatt()
        connection.value = BleConnectionState.Idle
        _state.value = SourceState.STOPPED
        scope.cancel()
    }

    /** Bateria caindo explica queda de qualidade (§3.2) — lida na conexão e a cada 30min. */
    fun refreshBattery() {
        val g = synchronized(lock) { gatt } ?: return
        val ch = g.getService(BleUuids.BATTERY_SERVICE)?.getCharacteristic(BleUuids.BATTERY_LEVEL) ?: return
        runCatching { g.readCharacteristic(ch) }
    }

    private fun connect(addr: String, autoConnect: Boolean) {
        if (!wanted) return
        closeGatt()
        connection.value = if (attempt == 0) BleConnectionState.Connecting else BleConnectionState.Reconnecting(attempt)
        events(
            BleEvent.Connection(
                type = if (attempt == 0) "CONNECTING" else "RECONNECT_ATTEMPT",
                detail = "attempt=$attempt autoConnect=$autoConnect",
            ),
        )
        val adapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
        val device = try {
            adapter?.getRemoteDevice(addr)
        } catch (_: IllegalArgumentException) {
            null
        }
        if (adapter?.isEnabled != true || device == null) {
            events(BleEvent.Connection("DISCONNECTED", "bluetooth indisponível (adapter off ou endereço inválido)"))
            scheduleReconnect()
            return
        }
        val g = try {
            // autoConnect=false na primeira (mais rápido); true nas seguintes (mais persistente, §3.2).
            device.connectGatt(context, autoConnect, callback, BluetoothDevice.TRANSPORT_LE)
        } catch (_: SecurityException) {
            null
        }
        synchronized(lock) { gatt = g }
        if (g == null) {
            events(BleEvent.Connection("DISCONNECTED", "connectGatt falhou (permissão?)"))
            scheduleReconnect()
        } else {
            armWatchdog()
        }
    }

    /** Se nem o autoConnect resolver em 90s, fecha e recomeça — nunca fica pendurado mudo. */
    private fun armWatchdog() {
        watchdogJob?.cancel()
        watchdogJob = scope.launch {
            delay(90_000)
            if (wanted && connection.value !is BleConnectionState.Connected) {
                events(BleEvent.Connection("WATCHDOG", "90s sem conectar; reiniciando tentativa"))
                scheduleReconnect(immediate = true)
            }
        }
    }

    private fun scheduleReconnect(immediate: Boolean = false) {
        if (!wanted) return
        attempt += 1
        _state.value = SourceState.DEGRADED
        val delayMs = if (immediate) 0L else (1000L shl (attempt - 1).coerceAtMost(5)).coerceAtMost(30_000L)
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            delay(delayMs)
            address?.let { connect(it, autoConnect = true) }
        }
    }

    private fun closeGatt() {
        val g = synchronized(lock) { gatt.also { gatt = null } } ?: return
        runCatching { g.disconnect() }
        runCatching { g.close() }
    }

    private val callback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                attempt = 0
                watchdogJob?.cancel()
                val name = runCatching { g.device.name }.getOrNull() ?: g.device.address
                events(BleEvent.Connection("CONNECTED", "status=$status device=$name"))
                connection.value = BleConnectionState.Connected(name)
                _state.value = SourceState.ACTIVE
                runCatching { g.readRemoteRssi() }
                runCatching { g.discoverServices() }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                events(BleEvent.Connection("DISCONNECTED", "status=$status"))
                if (wanted) scheduleReconnect()
            }
        }

        override fun onReadRemoteRssi(g: BluetoothGatt, rssi: Int, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                events(BleEvent.Connection("RSSI", "rssi na conexão", rssi = rssi))
            }
        }

        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                events(BleEvent.Connection("DISCONNECTED", "discoverServices status=$status"))
                if (wanted) scheduleReconnect()
                return
            }
            val hr = g.getService(BleUuids.HR_SERVICE)?.getCharacteristic(BleUuids.HR_MEASUREMENT)
            if (hr == null) {
                events(BleEvent.Connection("DISCONNECTED", "serviço 0x180D sem characteristic 0x2A37"))
                if (wanted) scheduleReconnect()
                return
            }
            runCatching { g.setCharacteristicNotification(hr, true) }
            val cccd = hr.getDescriptor(BleUuids.CCCD)
            if (cccd != null) {
                if (Build.VERSION.SDK_INT >= 33) {
                    runCatching { g.writeDescriptor(cccd, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) }
                } else {
                    @Suppress("DEPRECATION")
                    cccd.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    @Suppress("DEPRECATION")
                    g.writeDescriptor(cccd)
                }
            }
            events(BleEvent.Connection("SERVICES_READY", "notificações 0x2A37 habilitadas"))
        }

        override fun onDescriptorWrite(g: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            // GATT é fila única: a leitura de bateria só entra depois do CCCD confirmar.
            if (descriptor.uuid == BleUuids.CCCD) refreshBattery()
        }

        // Android 13+ chama a variante nova; nas anteriores, só a legada.
        override fun onCharacteristicChanged(g: BluetoothGatt, ch: BluetoothGattCharacteristic, value: ByteArray) {
            handleChanged(ch, value)
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(g: BluetoothGatt, ch: BluetoothGattCharacteristic) {
            if (Build.VERSION.SDK_INT < 33) {
                @Suppress("DEPRECATION")
                handleChanged(ch, ch.value ?: return)
            }
        }

        override fun onCharacteristicRead(g: BluetoothGatt, ch: BluetoothGattCharacteristic, value: ByteArray, status: Int) {
            handleRead(ch, value, status)
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicRead(g: BluetoothGatt, ch: BluetoothGattCharacteristic, status: Int) {
            if (Build.VERSION.SDK_INT < 33) {
                @Suppress("DEPRECATION")
                handleRead(ch, ch.value ?: return, status)
            }
        }
    }

    private fun handleChanged(ch: BluetoothGattCharacteristic, value: ByteArray) {
        if (ch.uuid != BleUuids.HR_MEASUREMENT) return
        // Carimbo duplo no instante da chegada (§8).
        val wall = System.currentTimeMillis()
        val mono = SystemClock.elapsedRealtime()
        val measurement = HrMeasurementParser.parse(value) ?: return
        events(BleEvent.Sample(measurement, wall, mono))
    }

    private fun handleRead(ch: BluetoothGattCharacteristic, value: ByteArray, status: Int) {
        if (status != BluetoothGatt.GATT_SUCCESS) return
        if (ch.uuid == BleUuids.BATTERY_LEVEL && value.isNotEmpty()) {
            events(BleEvent.Battery(value[0].toInt() and 0xFF))
        }
    }
}
