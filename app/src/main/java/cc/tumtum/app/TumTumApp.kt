package cc.tumtum.app

import android.app.Application
import cc.tumtum.app.data.db.TumTumDatabase
import cc.tumtum.app.service.CaptureBus
import cc.tumtum.app.service.CaptureService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import cc.tumtum.app.data.health.HealthConnectSource
import cc.tumtum.app.data.prefs.UserPrefs
import cc.tumtum.app.data.repo.FakeSocialRepository
import cc.tumtum.app.data.repo.NightRepository
import cc.tumtum.app.data.repo.SocialRepository
import cc.tumtum.app.data.repo.SourceMeasurement
import cc.tumtum.app.domain.EventSession
import cc.tumtum.app.export.SessionExporter

/**
 * Estado transitório entre "Encerrar a noite" (a2) → escolha de fonte (b4) → revela (a3).
 * Vive no processo; se o processo morrer no meio, o evento fica aberto e o fluxo recomeça.
 */
class EndNightCache {
    var event: EventSession? = null
    var measurement: SourceMeasurement? = null

    fun clear() {
        event = null
        measurement = null
    }
}

/** DI manual e enxuto — sem framework até precisar de um. */
class AppContainer(app: Application) {
    val prefs = UserPrefs(app)
    val health = HealthConnectSource(app)
    val db = TumTumDatabase.build(app)
    val nights = NightRepository(db, health)
    val social: SocialRepository = FakeSocialRepository()
    val endNight = EndNightCache()
    val exporter = SessionExporter(app, db, prefs)
}

class TumTumApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        resumeCaptureIfNeeded()
    }

    /**
     * §4.3 — se há sessão ativa gravada e o serviço não está de pé, retoma.
     * Best effort: em background o Android 12+ pode recusar o start (o sticky
     * restart e o retorno ao app cobrem esses casos); com a isenção de bateria
     * concedida (§6), o start em background é permitido.
     */
    private fun resumeCaptureIfNeeded() {
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            runCatching {
                val state = container.prefs.state.first()
                val eventId = state.activeCaptureEventId ?: return@launch
                val address = state.bleAddress ?: return@launch
                if (CaptureBus.status.value.active) return@launch
                val event = container.db.eventDao().byId(eventId)
                if (event != null && event.endAt == null) {
                    CaptureService.start(this@TumTumApp, eventId, address, restartReason = "PROCESS_RESTART")
                }
            }
        }
    }
}
