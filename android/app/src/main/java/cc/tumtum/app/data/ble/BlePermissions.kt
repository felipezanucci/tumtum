package cc.tumtum.app.data.ble

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * §5 — Android 12+ usa BLUETOOTH_SCAN/CONNECT; Android 11 e anteriores caem
 * no caminho legado com localização. Participantes com aparelho antigo existem.
 */
object BlePermissions {

    fun required(): Array<String> =
        if (Build.VERSION.SDK_INT >= 31) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }

    /** Notificação é a única janela do participante para a sessão (§4.2). */
    fun withNotifications(): Array<String> =
        if (Build.VERSION.SDK_INT >= 33) {
            required() + Manifest.permission.POST_NOTIFICATIONS
        } else {
            required()
        }

    fun granted(context: Context): Boolean = required().all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
}
