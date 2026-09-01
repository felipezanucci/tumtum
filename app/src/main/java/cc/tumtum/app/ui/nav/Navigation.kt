package cc.tumtum.app.ui.nav

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
import cc.tumtum.app.ui.screens.crowd.CrowdScreen
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
import cc.tumtum.app.ui.screens.sources.WatchSourcesScreen
import cc.tumtum.app.ui.screens.you.YouScreen

object Routes {
    const val Onboarding = "onboarding"
    const val Account = "account"
    const val Login = "login"
    const val Permission = "permission"
    const val SourcesSetup = "sources_setup"
    const val Feed = "feed"
    const val Live = "live"
    const val You = "you"
    const val Gallery = "gallery"
    const val Capture = "capture"
    const val EndNight = "end_night"
    const val EventFeed = "event_feed"
    const val Settings = "settings"
    const val Reveal = "reveal/{nightId}"
    const val Choose = "choose/{nightId}"
    const val Card = "card/{nightId}/{skin}"
    const val Crowd = "crowd/{nightId}"
    const val Profile = "profile/{handle}"

    fun reveal(nightId: Long) = "reveal/$nightId"
    fun choose(nightId: Long) = "choose/$nightId"
    fun card(nightId: Long, skin: Skin) = "card/$nightId/${skin.name}"
    fun crowd(nightId: Long) = "crowd/$nightId"
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
fun TumTumRoot(startDestination: String, nav: NavHostController = rememberNavController()) {
    val backStack by nav.currentBackStackEntryAsState()
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
            composable(Routes.SourcesSetup) { WatchSourcesScreen(nav, setupMode = true) }

            // Abas
            composable(Routes.Feed) { FeedScreen(nav) }
            composable(Routes.Live) { LiveTabScreen(nav) }
            composable(Routes.You) { YouScreen(nav) }
            composable(Routes.Gallery) { GalleryScreen(nav) }

            // Núcleo da noite
            composable(Routes.Capture) { CaptureScreen(nav) }
            composable(Routes.EndNight) { WatchSourcesScreen(nav, setupMode = false) }
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
                Routes.Crowd,
                arguments = listOf(navArgument("nightId") { type = NavType.LongType }),
            ) { entry ->
                CrowdScreen(nav, nightId = entry.arguments!!.getLong("nightId"))
            }

            // Social
            composable(Routes.EventFeed) { EventFeedScreen(nav) }
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
