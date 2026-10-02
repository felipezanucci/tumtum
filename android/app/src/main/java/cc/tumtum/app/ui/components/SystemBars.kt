package cc.tumtum.app.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * The icons of the system bars follow the screen (02/10). The app draws edge
 * to edge, so the status bar and the navigation bar sit on the screen's own
 * canvas — but their icons were always the dark ones, chosen once for the
 * light theme, and on the black screens (the night, the card, the feed's
 * head) the bars read as pale bands cutting the screen in three (Felipe,
 * b227: "vamos com a barra preta"). A dark screen calls this with
 * `lightIcons = true`; the previous choice comes back when it leaves.
 */
@Composable
fun SystemBars(lightIcons: Boolean, lightNavigationIcons: Boolean = lightIcons) {
    val view = LocalView.current
    if (view.isInEditMode) return
    DisposableEffect(lightIcons, lightNavigationIcons) {
        val window = view.context.activity()?.window ?: return@DisposableEffect onDispose {}
        val controller = WindowCompat.getInsetsController(window, view)
        val wasLightStatus = controller.isAppearanceLightStatusBars
        val wasLightNavigation = controller.isAppearanceLightNavigationBars
        controller.isAppearanceLightStatusBars = !lightIcons
        controller.isAppearanceLightNavigationBars = !lightNavigationIcons
        onDispose {
            controller.isAppearanceLightStatusBars = wasLightStatus
            controller.isAppearanceLightNavigationBars = wasLightNavigation
        }
    }
}

private tailrec fun Context.activity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.activity()
    else -> null
}
