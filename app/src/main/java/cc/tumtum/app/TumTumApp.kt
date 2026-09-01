package cc.tumtum.app

import android.app.Application
import cc.tumtum.app.data.db.TumTumDatabase
import cc.tumtum.app.data.health.HealthConnectSource
import cc.tumtum.app.data.prefs.UserPrefs
import cc.tumtum.app.data.repo.FakeSocialRepository
import cc.tumtum.app.data.repo.NightRepository
import cc.tumtum.app.data.repo.SocialRepository
import cc.tumtum.app.data.repo.SourceMeasurement
import cc.tumtum.app.domain.EventSession

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
}

class TumTumApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
