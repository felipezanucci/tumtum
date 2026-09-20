package cc.tumtum.app

import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cc.tumtum.app.ui.nav.Routes
import cc.tumtum.app.ui.nav.TumTumRoot
import cc.tumtum.app.ui.splash.SplashOverlay
import cc.tumtum.app.ui.theme.TumTumTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Só no cold start (§4). Voltar de background/recriação não mostra splash.
        val showSplash = !splashShownThisProcess && savedInstanceState == null
        val reduceMotion = Settings.Global.getFloat(
            contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) == 0f

        // A reminder tapped (Reminders): open that night, or the AO VIVO tab.
        val openNightId = intent?.getLongExtra(EXTRA_OPEN_NIGHT_ID, -1L)?.takeIf { it >= 0 }
        val openLive = intent?.getBooleanExtra(EXTRA_OPEN_LIVE, false) == true
        setContent {
            TumTumTheme {
                AppRoot(showSplash = showSplash, reduceMotion = reduceMotion, openNightId = openNightId, openLive = openLive)
            }
        }
    }

    companion object {
        const val EXTRA_OPEN_NIGHT_ID = "open_night_id"
        const val EXTRA_OPEN_LIVE = "open_live"
        private var splashShownThisProcess = false

        internal fun markSplashShown() {
            splashShownThisProcess = true
        }
    }
}

@Composable
private fun AppRoot(showSplash: Boolean, reduceMotion: Boolean, openNightId: Long?, openLive: Boolean) {
    val container = (androidx.compose.ui.platform.LocalContext.current.applicationContext as TumTumApp).container
    val userState by container.prefs.state.collectAsStateWithLifecycle(initialValue = null)
    var splashVisible by remember { mutableStateOf(showSplash) }

    Box(Modifier.fillMaxSize()) {
        // O FEED já está montado atrás (3b): quando a camada rosa sai, é corte seco.
        userState?.let { state ->
            TumTumRoot(
                startDestination = if (state.onboarded) Routes.Feed else Routes.Onboarding,
                openNightId = openNightId,
                openLive = openLive,
            )
        }
        if (splashVisible) {
            SplashOverlay(
                reduceMotion = reduceMotion,
                sessionResolved = userState != null,
                onFinished = {
                    splashVisible = false
                    MainActivity.markSplashShown()
                },
            )
        }
    }
}
