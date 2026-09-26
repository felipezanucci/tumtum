package cc.tumtum.app.ui.nav

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cc.tumtum.app.AppContainer
import cc.tumtum.app.TumTumApp
import cc.tumtum.app.domain.Skin
import cc.tumtum.app.ui.components.TTBottomBar
import cc.tumtum.app.ui.components.TTTab
import cc.tumtum.app.ui.screens.account.CreateAccountScreen
import cc.tumtum.app.ui.screens.card.CardScreen
import cc.tumtum.app.ui.screens.choose.ChooseSkinScreen
import cc.tumtum.app.ui.screens.consent.ConsentScreen
import cc.tumtum.app.ui.screens.eventfeed.EventFeedScreen
import cc.tumtum.app.ui.screens.feed.FeedScreen
import cc.tumtum.app.ui.screens.gallery.GalleryScreen
import cc.tumtum.app.ui.screens.live.CaptureScreen
import cc.tumtum.app.ui.screens.live.LiveTabScreen
import cc.tumtum.app.ui.screens.login.LoginScreen
import cc.tumtum.app.ui.screens.onboarding.OnboardingScreen
import cc.tumtum.app.ui.screens.permission.PermissionScreen
import cc.tumtum.app.ui.screens.profile.PublicProfileScreen
import cc.tumtum.app.ui.screens.reveal.RevealScreen
import cc.tumtum.app.ui.screens.settings.SettingsScreen
import cc.tumtum.app.ui.screens.sources.SetupScreen
import cc.tumtum.app.ui.screens.sources.WatchSourcesScreen
import cc.tumtum.app.ui.screens.you.YouScreen
import androidx.compose.runtime.LaunchedEffect
import cc.tumtum.app.data.repo.consentGateNeeded
import kotlinx.coroutines.flow.first

object Routes {
    const val Onboarding = "onboarding"
    const val Account = "account"
    const val Login = "login"
    const val Permission = "permission"
    /**
     * The consent screen (26/09): before the Health Connect dialog, as the
     * gate after a sign-in, and whenever the server asks for one purpose.
     */
    const val Consent = "consent?focus={focus}&send={send}"
    const val SourcesSetup = "sources_setup"
    /** The same setup screen, opened on the strap search — Configurações' way in (25/09). */
    const val SensorSearch = "sources_setup/search"
    const val Feed = "feed"
    const val Live = "live"
    const val You = "you"
    const val Gallery = "gallery"
    const val Capture = "capture"
    const val EndNight = "end_night"
    /** The name rides along (#32): the header should never lose what the list already showed. */
    const val EventFeed = "event_feed/{eventId}?name={name}"
    const val Settings = "settings"
    const val Reveal = "reveal/{nightId}"
    const val Choose = "choose/{nightId}"
    const val Card = "card/{nightId}/{skin}"
    const val Profile = "profile/{handle}"

    fun reveal(nightId: Long) = "reveal/$nightId"

    /** [focus]: the purpose the app needs now; [sendNightId]: the night that goes up once `keep_night` is on. */
    fun consent(focus: String? = null, sendNightId: Long? = null): String {
        val args = buildList {
            focus?.let { add("focus=${Uri.encode(it)}") }
            sendNightId?.let { add("send=$it") }
        }
        return if (args.isEmpty()) "consent" else "consent?" + args.joinToString("&")
    }
    fun choose(nightId: Long) = "choose/$nightId"
    fun card(nightId: Long, skin: Skin) = "card/$nightId/${skin.name}"

    fun eventFeed(eventId: String, name: String? = null) =
        "event_feed/$eventId" + (name?.takeIf { it.isNotBlank() }?.let { "?name=${Uri.encode(it)}" } ?: "")
    fun profile(handle: String) = "profile/$handle"
}

@Composable
fun appContainer(): AppContainer =
    (LocalContext.current.applicationContext as TumTumApp).container

/** Rotas de aba — as únicas com a barra de 3 zonas visível (b7 inclusa). */
private val tabRoutes = setOf(Routes.Feed, Routes.Live, Routes.You, Routes.Gallery)

private fun tabFor(route: String?): TTTab = when (route) {
    Routes.Live -> TTTab.Live
    Routes.You, Routes.Gallery -> TTTab.You
    else -> TTTab.Feed
}

@Composable
fun TumTumRoot(
    startDestination: String,
    nav: NavHostController = rememberNavController(),
    openNightId: Long? = null,
    openLive: Boolean = false,
) {
    val backStack by nav.currentBackStackEntryAsState()
    val container = appContainer()
    // A tapped reminder lands on the night it named, or on AO VIVO — only once the person is in.
    // The consent gate (26/09): an account from before it — no birth date,
    // or no Terms agreed — passes it before anything else. Unasked (offline),
    // the person goes in; the server still refuses whatever needs a consent.
    LaunchedEffect(Unit) {
        if (startDestination != Routes.Feed) return@LaunchedEffect
        val session = container.prefs.state.first().session
        if (session?.isLive(System.currentTimeMillis()) == true && container.consentGateNeeded() == true) {
            nav.navigate(Routes.consent()) { popUpTo(Routes.Feed) { inclusive = true } }
        }
    }
    LaunchedEffect(openNightId, openLive) {
        if (startDestination != Routes.Feed) return@LaunchedEffect
        when {
            openNightId != null -> nav.navigate(Routes.reveal(openNightId))
            openLive -> nav.navigate(Routes.Live) { launchSingleTop = true }
        }
    }
    val route = backStack?.destination?.route

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.White,
        bottomBar = {
            if (route in tabRoutes) {
                TTBottomBar(
                    current = tabFor(route),
                    onSelect = { tab ->
                        val dest = when (tab) {
                            TTTab.Feed -> Routes.Feed
                            TTTab.Live -> Routes.Live
                            TTTab.You -> Routes.You
                        }
                        nav.navigate(dest) {
                            popUpTo(Routes.Feed) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = startDestination,
            modifier = Modifier.padding(padding),
        ) {
            // Entrada
            composable(Routes.Onboarding) { OnboardingScreen(nav) }
            composable(Routes.Account) { CreateAccountScreen(nav) }
            composable(Routes.Login) { LoginScreen(nav) }
            composable(Routes.Permission) { PermissionScreen(nav) }
            composable(
                Routes.Consent,
                arguments = listOf(
                    navArgument("focus") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("send") {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                ),
            ) { entry ->
                ConsentScreen(
                    nav,
                    focus = entry.arguments?.getString("focus"),
                    sendNightId = entry.arguments?.getLong("send")?.takeIf { it >= 0 },
                )
            }
            composable(Routes.SourcesSetup) { SetupScreen(nav) }
            composable(Routes.SensorSearch) { SetupScreen(nav, startSearching = true) }

            // Abas
            composable(Routes.Feed) { FeedScreen(nav) }
            composable(Routes.Live) { LiveTabScreen(nav) }
            composable(Routes.You) { YouScreen(nav) }
            composable(Routes.Gallery) { GalleryScreen(nav) }

            // Núcleo da noite
            composable(Routes.Capture) { CaptureScreen(nav) }
            composable(Routes.EndNight) { WatchSourcesScreen(nav) }
            composable(
                Routes.Reveal,
                arguments = listOf(navArgument("nightId") { type = NavType.LongType }),
            ) { entry ->
                RevealScreen(nav, nightId = entry.arguments!!.getLong("nightId"))
            }
            composable(
                Routes.Choose,
                arguments = listOf(navArgument("nightId") { type = NavType.LongType }),
            ) { entry ->
                ChooseSkinScreen(nav, nightId = entry.arguments!!.getLong("nightId"))
            }
            composable(
                Routes.Card,
                arguments = listOf(
                    navArgument("nightId") { type = NavType.LongType },
                    navArgument("skin") { type = NavType.StringType },
                ),
            ) { entry ->
                CardScreen(
                    nav,
                    nightId = entry.arguments!!.getLong("nightId"),
                    skin = Skin.valueOf(entry.arguments!!.getString("skin")!!),
                )
            }
            composable(
                Routes.EventFeed,
                arguments = listOf(
                    navArgument("eventId") { type = NavType.StringType },
                    navArgument("name") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                ),
            ) { entry ->
                EventFeedScreen(
                    nav,
                    eventId = entry.arguments!!.getString("eventId").orEmpty(),
                    eventName = entry.arguments?.getString("name"),
                )
            }
            composable(
                Routes.Profile,
                arguments = listOf(navArgument("handle") { type = NavType.StringType }),
            ) { entry ->
                PublicProfileScreen(nav, handle = entry.arguments!!.getString("handle")!!)
            }

            composable(Routes.Settings) { SettingsScreen(nav) }
        }
    }
}
