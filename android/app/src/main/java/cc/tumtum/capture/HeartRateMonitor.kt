package cc.tumtum.capture

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import java.util.UUID
import kotlin.math.min

/**
 * A connection to one heart rate sensor, with reconnection.
 *
 * A BLE link will drop during a six-hour event — a crowd, RF noise, its owner
 * walking to the bar with the phone left on a table. Reconnecting is the normal
 * case, not the exception, so a drop costs samples rather than the night.
 *
 * The behaviour here is the web client's, carried over unchanged: eight fast
 * attempts with exponential backoff, then a slow steady retry that never gives
 * up. The web version originally stopped after those eight — about two minutes
 * — which ended a capture on the first trip away from the phone.
 */
class HeartRateMonitor(
    private val context: Context,
    private val onReading: (HeartRateMeasurement) -> Unit,
    private val onState: (State, String?) -> Unit,
) {

    enum class State { IDLE, CONNECTING, CONNECTED, RECONNECTING, DISCONNECTED, ERROR }

    private val handler = Handler(Looper.getMainLooper())
    private var gatt: BluetoothGatt? = null
    private var device: BluetoothDevice? = null
    private var reconnectAttempts = 0
    private var stopped = false

    var state: State = State.IDLE
        private set

    val deviceName: String?
        @SuppressLint("MissingPermission")
        get() = try {
            device?.name
        } catch (_: SecurityException) {
            null
        }

    @SuppressLint("MissingPermission")
    fun connect(target: BluetoothDevice) {
        stopped = false
        device = target
        setState(State.CONNECTING, null)
        openStream()
    }

    @SuppressLint("MissingPermission")
    fun stop() {
        stopped = true
        handler.removeCallbacksAndMessages(null)
        try {
            gatt?.disconnect()
            gatt?.close()
        } catch (_: SecurityException) {
            // Permission revoked mid-capture. Nothing left to close cleanly.
        }
        gatt = null
        setState(State.DISCONNECTED, null)
    }

    @SuppressLint("MissingPermission")
    private fun openStream() {
        val target = device ?: return
        try {
            // autoConnect=false connects faster; reconnection is handled here
            // rather than left to the stack, so that it can be observed.
            gatt = target.connectGatt(context, false, callback, BluetoothDevice.TRANSPORT_LE)
        } catch (e: SecurityException) {
            setState(State.ERROR, "Sem permissão de Bluetooth")
        }
    }

    private fun setState(next: State, detail: String?) {
        state = next
        handler.post { onState(next, detail) }
    }

    private fun scheduleReconnect() {
        if (stopped) return
        setState(State.RECONNECTING, null)

        // Back off quickly for the first few failures — a stumble, a pocket, a
        // moment of interference — then settle into a slow steady retry.
        // Scanning costs battery, and battery is what ends a long night.
        val delay = if (reconnectAttempts < FAST_ATTEMPTS) {
            min(1000L shl reconnectAttempts, 15_000L)
        } else {
            SLOW_RECONNECT_MS
        }
        reconnectAttempts += 1

        handler.postDelayed({
            if (!stopped) openStream()
        }, delay)
    }

    private val callback = object : BluetoothGattCallback() {

        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> try {
                    g.discoverServices()
                } catch (_: SecurityException) {
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    try {
                        g.close()
                    } catch (_: SecurityException) {
                    }
                    gatt = null
                    if (!stopped) scheduleReconnect()
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
            val characteristic = g
                .getService(HEART_RATE_SERVICE)
                ?.getCharacteristic(HEART_RATE_MEASUREMENT)

            if (characteristic == null) {
                // The sensor connected but does not speak the standard profile.
                // Retrying will not change that, so say so instead of looping.
                stopped = true
                setState(State.ERROR, "Este sensor não expõe batimento no padrão Bluetooth")
                return
            }

            try {
                g.setCharacteristicNotification(characteristic, true)
                val descriptor = characteristic.getDescriptor(CLIENT_CONFIG)
                if (descriptor != null) {
                    // Subscribing is two steps: tell the stack, then tell the
                    // sensor by writing its configuration descriptor. Skipping
                    // the second is the classic reason notifications never come.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        g.writeDescriptor(
                            descriptor,
                            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE,
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        @Suppress("DEPRECATION")
                        g.writeDescriptor(descriptor)
                    }
                }
            } catch (_: SecurityException) {
                setState(State.ERROR, "Sem permissão de Bluetooth")
                return
            }

            reconnectAttempts = 0
            setState(State.CONNECTED, null)
        }

        override fun onCharacteristicChanged(
            g: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
        ) {
            deliver(value)
        }

        @Deprecated("Called on Android 12 and below; the newer overload carries the value.")
        @Suppress("DEPRECATION")
        override fun onCharacteristicChanged(
            g: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
        ) {
            characteristic.value?.let(::deliver)
        }
    }

    private fun deliver(value: ByteArray) {
        val measurement = HeartRateParser.parse(value) ?: return
        handler.post { onReading(measurement) }
    }

    companion object {
        /** Bluetooth SIG assigned numbers. */
        val HEART_RATE_SERVICE: UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
        val HEART_RATE_MEASUREMENT: UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
        private val CLIENT_CONFIG: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

        private const val FAST_ATTEMPTS = 8
        private const val SLOW_RECONNECT_MS = 30_000L
    }
}
