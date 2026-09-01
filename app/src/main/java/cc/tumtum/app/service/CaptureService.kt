package cc.tumtum.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import cc.tumtum.app.MainActivity
import cc.tumtum.app.R
import cc.tumtum.app.TumTumApp
import cc.tumtum.app.data.ble.BleConnectionState
import cc.tumtum.app.data.ble.BleEvent
import cc.tumtum.app.data.ble.BleHrSource
import cc.tumtum.app.data.ble.HrMeasurementParser
import cc.tumtum.app.data.db.BleSampleEntity
import cc.tumtum.app.data.db.ConnectionEventEntity
import cc.tumtum.app.data.db.MotionEntity
import cc.tumtum.app.data.db.RrIntervalEntity
import cc.tumtum.app.domain.MotionAggregator
import java.util.concurrent.Executors
import kotlin.math.sqrt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * §4 — a parte que decide o teste. Se o serviço morrer às 23h40, a noite se perde.
 *
 *  - Foreground `connectedDevice`, startForeground() antes de qualquer coisa.
 *  - Cada amostra vai para o Room no instante em que chega — nada de buffer (§4.2).
 *  - WakeLock parcial durante a sessão; participantes levam power bank (protocolo).
 *  - START_STICKY + estado em DataStore: morte de processo e reboot retomam (§4.3).
 */
class CaptureService : Service() {

    private val container get() = (application as TumTumApp).container

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** Fila única: escrita em disco na ordem de chegada dos pacotes. */
    private val persistDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    private var ble: BleHrSource? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var sensorManager: SensorManager? = null
    private var motionListener: SensorEventListener? = null
    private val motion = MotionAggregator()

    private var eventId: Long = -1
    private var sampleCount: Long = 0
    private var attached = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Sempre, inclusive nos caminhos de erro: ForegroundServiceDidNotStartInTimeException mata o app (§4.1).
        startForegroundCompat()

        when (intent?.action) {
            ACTION_STOP -> {
                finishSession()
                return START_NOT_STICKY
            }
            ACTION_START -> {
                val id = intent.getLongExtra(EXTRA_EVENT_ID, -1)
                val address = intent.getStringExtra(EXTRA_ADDRESS)
                val restart = intent.getStringExtra(EXTRA_RESTART_REASON)
                if (id > 0 && address != null) {
                    scope.launch { attach(id, address, restart) }
                } else {
                    stopSelfQuietly()
                }
            }
            // Intent null = o sistema reiniciou o serviço sticky depois de matar o processo.
            else -> scope.launch { restoreFromPrefs() }
        }
        return START_STICKY
    }

    private suspend fun restoreFromPrefs() {
        val state = container.prefs.state.first()
        val id = state.activeCaptureEventId
        val address = state.bleAddress
        val event = id?.let { container.db.eventDao().byId(it) }
        if (id == null || address == null || event == null || event.endAt != null) {
            stopSelfQuietly()
            return
        }
        attach(id, address, restartReason = "PROCESS_RESTART")
    }

    private suspend fun attach(id: Long, address: String, restartReason: String?) {
        if (attached && eventId == id) return
        if (attached) detachSources()
        attached = true
        eventId = id
        container.prefs.setActiveCapture(id)

        val wall = System.currentTimeMillis()
        val mono = SystemClock.elapsedRealtime()
        // Offset dos relógios no início (§8) — permite reconstruir desvio de NTP na análise.
        container.db.eventDao().setClockOffsetStart(id, wall - mono)
        recordConnection(if (restartReason != null) restartReason else "SESSION_START", "serviço em primeiro plano ativo")

        sampleCount = container.db.captureDao().sampleCount(id)
        CaptureBus.status.value = CaptureStatus(
            active = true,
            eventId = id,
            samplesWritten = sampleCount,
            deviceName = container.prefs.state.first().bleName,
        )

        acquireWakeLock()
        startMotion()
        startBle(address)
        startTickers()
    }

    private fun startBle(address: String) {
        val source = BleHrSource(this) { event -> onBleEvent(event) }
        source.address = address
        ble = source
        scope.launch {
            source.start(eventId)
            source.connection.collect { conn ->
                CaptureBus.status.value = CaptureBus.status.value.copy(connection = conn)
                updateNotification()
            }
        }
    }

    private fun onBleEvent(event: BleEvent) {
        val id = eventId
        if (id <= 0) return
        scope.launch(persistDispatcher) {
            when (event) {
                is BleEvent.Sample -> {
                    val m = event.measurement
                    // Amostra crua, imediatamente em disco (§1.4, §4.2).
                    container.db.captureDao().insertSample(
                        BleSampleEntity(
                            eventId = id,
                            wallClockMs = event.wallClockMs,
                            elapsedRealtimeMs = event.elapsedRealtimeMs,
                            bpm = m.bpm,
                            contactStatus = m.contactStatus,
                        ),
                    )
                    if (m.rrIntervalsMs.isNotEmpty()) {
                        container.db.captureDao().insertRr(
                            m.rrIntervalsMs.map {
                                RrIntervalEntity(
                                    eventId = id,
                                    wallClockMs = event.wallClockMs,
                                    elapsedRealtimeMs = event.elapsedRealtimeMs,
                                    rrMs = it,
                                )
                            },
                        )
                    }
                    sampleCount += 1
                    CaptureBus.status.value = CaptureBus.status.value.copy(
                        samplesWritten = sampleCount,
                        lastBpm = m.bpm,
                        contactStatus = m.contactStatus,
                    )
                }
                is BleEvent.Battery -> {
                    recordConnection("BATTERY", "nível ${event.levelPct}%")
                    CaptureBus.status.value = CaptureBus.status.value.copy(sensorBatteryPct = event.levelPct)
                }
                is BleEvent.Connection -> recordConnection(event.type, event.detail, event.rssi)
            }
        }
    }

    private fun startMotion() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accel = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: return
        val listener = object : SensorEventListener {
            override fun onSensorChanged(e: SensorEvent) {
                val mag = sqrt(
                    (e.values[0] * e.values[0] + e.values[1] * e.values[1] + e.values[2] * e.values[2]).toDouble(),
                ) - SensorManager.GRAVITY_EARTH
                val window = motion.add(SystemClock.elapsedRealtime(), System.currentTimeMillis(), mag)
                if (window != null) {
                    val id = eventId
                    if (id > 0) {
                        scope.launch(persistDispatcher) {
                            container.db.captureDao().insertMotion(
                                MotionEntity(
                                    eventId = id,
                                    wallClockMs = window.wallClockMs,
                                    elapsedRealtimeMs = window.elapsedRealtimeMs,
                                    magMean = window.magMean,
                                    magStd = window.magStd,
                                ),
                            )
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        motionListener = listener
        sensorManager?.registerListener(listener, accel, SensorManager.SENSOR_DELAY_GAME)
    }

    private fun startTickers() {
        // Notificação: única forma do participante saber que está funcionando sem abrir o app (§4.2).
        scope.launch {
            while (attached) {
                updateNotification()
                delay(15_000)
            }
        }
        // Bateria do sensor a cada 30min (§3.2).
        scope.launch {
            while (attached) {
                delay(30 * 60_000L)
                ble?.refreshBattery()
            }
        }
    }

    private fun acquireWakeLock() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "tumtum:capture").apply {
            setReferenceCounted(false)
            acquire(12 * 60 * 60 * 1000L) // teto de 12h — maior que qualquer janela de evento
        }
    }

    private fun finishSession() {
        val wasAttached = attached
        val id = eventId
        val lastWindow = motion.flush()
        detachSources()
        CaptureBus.status.value = CaptureStatus()
        if (!wasAttached || id <= 0) {
            stopSelfQuietly()
            return
        }
        // As escritas finais entram na MESMA fila das amostras: nada é perdido nem reordenado.
        scope.launch {
            kotlinx.coroutines.withContext(persistDispatcher) {
                val wall = System.currentTimeMillis()
                val mono = SystemClock.elapsedRealtime()
                container.db.eventDao().setClockOffsetEnd(id, wall - mono)
                lastWindow?.let { w ->
                    container.db.captureDao().insertMotion(
                        MotionEntity(
                            eventId = id,
                            wallClockMs = w.wallClockMs,
                            elapsedRealtimeMs = w.elapsedRealtimeMs,
                            magMean = w.magMean,
                            magStd = w.magStd,
                        ),
                    )
                }
                container.db.captureDao().insertConnectionEvent(
                    ConnectionEventEntity(eventId = id, wallClockMs = wall, elapsedRealtimeMs = mono, type = "SESSION_STOP", detail = "encerrado pelo usuário"),
                )
                container.prefs.clearActiveCapture()
            }
            kotlinx.coroutines.withContext(Dispatchers.Main) { stopSelfQuietly() }
        }
    }

    private fun detachSources() {
        attached = false
        ble?.shutdown()
        ble = null
        motionListener?.let { sensorManager?.unregisterListener(it) }
        motionListener = null
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
    }

    private fun stopSelfQuietly() {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        detachSources()
        scope.cancel()
        persistDispatcher.close()
        super.onDestroy()
    }

    // ---- Notificação ----

    private fun createChannel() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, getString(R.string.capture_channel), NotificationManager.IMPORTANCE_LOW),
        )
    }

    private fun startForegroundCompat() {
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE,
        )
    }

    private fun updateNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun buildNotification(): android.app.Notification {
        val status = CaptureBus.status.value
        val connected = status.connection is BleConnectionState.Connected
        val text = if (!status.active) {
            getString(R.string.capture_notif_starting)
        } else {
            buildString {
                append(
                    when (status.connection) {
                        is BleConnectionState.Connected -> getString(R.string.capture_notif_connected)
                        is BleConnectionState.Reconnecting -> getString(R.string.capture_notif_reconnecting)
                        else -> getString(R.string.capture_notif_disconnected)
                    },
                )
                if (connected && status.lastBpm != null && status.contactStatus != HrMeasurementParser.CONTACT_NOT_DETECTED) {
                    append(" · ${status.lastBpm} bpm")
                }
                append(" · ${status.samplesWritten} ").append(getString(R.string.capture_notif_samples))
                status.sensorBatteryPct?.let { append(" · 🔋$it%") }
            }
        }
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.capture_notif_title))
            .setContentText(text)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(contentIntent)
            .build()
    }

    private suspend fun recordConnection(type: String, detail: String, rssi: Int? = null) {
        container.db.captureDao().insertConnectionEvent(
            ConnectionEventEntity(
                eventId = eventId,
                wallClockMs = System.currentTimeMillis(),
                elapsedRealtimeMs = SystemClock.elapsedRealtime(),
                type = type,
                detail = detail,
                rssi = rssi,
            ),
        )
    }

    companion object {
        private const val CHANNEL_ID = "capture"
        private const val NOTIFICATION_ID = 1001
        private const val ACTION_START = "cc.tumtum.app.capture.START"
        private const val ACTION_STOP = "cc.tumtum.app.capture.STOP"
        private const val EXTRA_EVENT_ID = "eventId"
        private const val EXTRA_ADDRESS = "address"
        private const val EXTRA_RESTART_REASON = "restartReason"

        /** Best effort: em background o Android 12+ pode recusar — quem chama decide o fallback. */
        fun start(context: Context, eventId: Long, address: String, restartReason: String? = null): Boolean =
            runCatching {
                val intent = Intent(context, CaptureService::class.java)
                    .setAction(ACTION_START)
                    .putExtra(EXTRA_EVENT_ID, eventId)
                    .putExtra(EXTRA_ADDRESS, address)
                    .putExtra(EXTRA_RESTART_REASON, restartReason)
                ContextCompat.startForegroundService(context, intent)
            }.isSuccess

        fun stop(context: Context) {
            runCatching {
                ContextCompat.startForegroundService(
                    context,
                    Intent(context, CaptureService::class.java).setAction(ACTION_STOP),
                )
            }
        }
    }
}
