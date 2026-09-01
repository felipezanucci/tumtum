package cc.tumtum.app.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Varredura filtrada pelo service Heart Rate (0x180D, §3.2). Emite cada achado;
 * a UI agrega por endereço e mostra nome + RSSI. Permissões já checadas por quem chama.
 */
class BleScanner(private val context: Context) {

    fun isBluetoothOn(): Boolean {
        val adapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
        return adapter?.isEnabled == true
    }

    @SuppressLint("MissingPermission")
    fun scan(): Flow<BleDevice> = callbackFlow {
        val adapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
        val scanner = adapter?.bluetoothLeScanner
        if (adapter?.isEnabled != true || scanner == null) {
            close()
            return@callbackFlow
        }
        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val name = try {
                    result.scanRecord?.deviceName ?: result.device.name
                } catch (_: SecurityException) {
                    null
                }
                trySend(BleDevice(name = name ?: "Sensor sem nome", address = result.device.address, rssi = result.rssi))
            }

            override fun onBatchScanResults(results: List<ScanResult>) {
                results.forEach { onScanResult(0, it) }
            }
        }
        val filters = listOf(ScanFilter.Builder().setServiceUuid(ParcelUuid(BleUuids.HR_SERVICE)).build())
        val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        try {
            scanner.startScan(filters, settings, callback)
        } catch (e: SecurityException) {
            close(e)
            return@callbackFlow
        }
        awaitClose {
            try {
                scanner.stopScan(callback)
            } catch (_: SecurityException) {
            }
        }
    }
}
