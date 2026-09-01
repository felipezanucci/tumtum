package cc.tumtum.app.ui.screens.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cc.tumtum.app.R
import cc.tumtum.app.domain.Skin
import cc.tumtum.app.ui.components.Avatar
import cc.tumtum.app.ui.components.MomentCard
import cc.tumtum.app.ui.components.Wordmark
import cc.tumtum.app.ui.nav.Routes
import cc.tumtum.app.ui.nav.appContainer
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType

/**
 * b5 — Feed dos seus amigos: cards compartilhados + uma frase.
 * Nada de post solto; reação única SENTI TB.
 */
@Composable
fun FeedScreen(nav: NavHostController) {
    val container = appContainer()
    val feed by container.social.friendsFeed.collectAsStateWithLifecycle()
    val upcoming by container.social.upcoming.collectAsStateWithLifecycle()
    val user by container.prefs.state.collectAsStateWithLifecycle(initialValue = null)

    Column(Modifier.fillMaxSize().background(TT.Paper).statusBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Wordmark(width = 92.dp)
            cc.tumtum.app.ui.components.UserAvatar(
                user?.account?.initials ?: "TT",
                Skin.BLACK,
                photoPath = user?.avatarPath,
                modifier = Modifier.clickable {
                    user?.account?.let { nav.navigate(Routes.profile(it.username)) }
                },
            )
        }
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            upcoming?.let { up ->
                item(key = "upcoming") {
                    // Banner ácido do evento de hoje — CTA leva à captura (AO VIVO).
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(TT.Acid)
                            .clickable { nav.navigate(Routes.EventFeed) }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                stringResource(R.string.feed_tonight, up.eventName),
                                style = TTType.ItemSub.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                                color = TT.Ink,
                            )
                            Text(
                                stringResource(R.string.feed_confirmed, up.friendsConfirmed),
                                style = TTType.ItemSub.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                                color = TT.Ink.copy(alpha = 0.65f),
                            )
                        }
                        Text(
                            stringResource(R.string.feed_capture),
                            style = TTType.MetaSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                            color = TT.Acid,
                            modifier = Modifier
                                .background(TT.Ink)
                                .clickable { nav.navigate(Routes.Live) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                        )
                    }
                }
            }
            items(feed, key = { it.id }) { moment ->
                MomentCard(
                    moment = moment,
                    onToggleSenti = { container.social.toggleSenti(moment.id) },
                    onOpenProfile = { nav.navigate(Routes.profile(moment.user.handle)) },
                )
            }
        }
    }
}
