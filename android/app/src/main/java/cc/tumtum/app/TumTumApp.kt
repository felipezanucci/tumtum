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
import cc.tumtum.app.data.api.TumtumApi
import cc.tumtum.app.data.health.HealthConnectSource
import cc.tumtum.app.data.prefs.UserPrefs
import cc.tumtum.app.data.repo.NightRepository
import cc.tumtum.app.data.repo.NightSync
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
    /** For the pieces that need a Context outside a screen (reminders). */
    val appContext: android.content.Context = app
    val prefs = UserPrefs(app)
    val api = TumtumApi(prefs)
    val health = HealthConnectSource(app)
    val db = TumTumDatabase.build(app)
    val nights = NightRepository(db, health)
    val sync = NightSync(db, api, prefs)
    val social: SocialRepository = SocialRepository(api)
    val endNight = EndNightCache()
    val exporter = SessionExporter(app, db, prefs)
}

class TumTumApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        repairLegacyHandle()
        resumeCaptureIfNeeded()
        // Etapa 2: a night that never reached the server tries again on every start.
        container.sync.retryPendingLater()
        // Reminders do not survive an update; set again from what the phone knows.
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            runCatching { cc.tumtum.app.service.Reminders.rescheduleAll(this@TumTumApp, container) }
        }
    }

    /**
     * Reparo único: builds antigos (≤ b9) deixavam o email inteiro virar @ e nome.
     * O @ é fixo daqui em diante — mas um @ com formato de email nunca foi um @.
     */
    private fun repairLegacyHandle() {
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            runCatching {
                val account = container.prefs.state.first().account ?: return@launch
                if ('@' !in account.username) return@launch
                val fixed = account.username.substringBefore('@').lowercase()
                    .filter { it.isLetterOrDigit() || it == '_' }
                    .ifBlank { return@launch }
                val fixedName = if ('@' in account.name) {
                    fixed.replaceFirstChar { it.uppercase() }
                } else {
                    account.name
                }
                container.prefs.setProfile(fixedName, fixed)
            }
        }
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
