package cc.tumtum.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cc.tumtum.app.R
import cc.tumtum.app.domain.PublicProfile
import cc.tumtum.app.domain.Skin
import cc.tumtum.app.domain.SocialUser
import cc.tumtum.app.ui.components.Avatar
import cc.tumtum.app.ui.components.Badge
import cc.tumtum.app.ui.nav.Routes
import cc.tumtum.app.ui.nav.appContainer
import cc.tumtum.app.ui.screens.gallery.GalleryCover
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType

/** b8 — Perfil público: o que outra pessoa vê. */
@Composable
fun PublicProfileScreen(nav: NavHostController, handle: String) {
    val container = appContainer()
    val user by container.prefs.state.collectAsStateWithLifecycle(initialValue = null)
    val ownNights by container.nights.galleryNights().collectAsStateWithLifecycle(initialValue = emptyList())
    val allNights by container.nights.nights().collectAsStateWithLifecycle(initialValue = emptyList())

    val isMe = user?.account?.username == handle
    var followTick by remember { mutableIntStateOf(0) }
    val profile: PublicProfile? = if (isMe) {
        user?.account?.let { acc ->
            PublicProfile(
                user = SocialUser(
                    handle = acc.username,
                    displayName = acc.name,
                    initials = acc.initials,
                    avatarSkin = Skin.PINK,
                    city = "",
                    tribes = acc.tribes.toList(),
                ),
                nightCount = allNights.size,
                friendCount = 0,
                recordBpm = allNights.maxOfOrNull { it.peakBpm } ?: 0,
                publicNights = ownNights,
                followedByMe = false,
            )
        }
    } else {
        // followTick força releitura após o toggle no repositório fake.
        remember(handle, followTick) { container.social.profile(handle) }
    }
    val p = profile ?: return

    Column(
        Modifier
            .fillMaxSize()
            .background(TT.Paper)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding(),
    ) {
        // Cabeçalho preto
        Column(
            Modifier
                .fillMaxWidth()
                .background(TT.Ink)
                .statusBarsPadding()
                .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 24.dp),
        ) {
            Text(
                "←",
                style = TTType.MetaSmall,
                color = TT.Gray45,
                modifier = Modifier.clickable { nav.popBackStack() },
            )
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Avatar(p.user.initials, p.user.avatarSkin, size = 64.dp)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        p.user.displayName,
                        style = TTType.ItemTitle.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                        color = TT.Paper,
                    )
                    Text(
                        "@${p.user.handle}" + if (p.user.city.isNotBlank()) " · ${p.user.city}" else "",
                        style = TTType.BodySmall,
                        color = TT.Gray45,
                    )
                }
            }
            if (p.user.tribes.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    p.user.tribes.forEachIndexed { i, tribe ->
                        Badge(
                            tribe.uppercase(),
                            background = if (i % 2 == 0) TT.Acid else TT.Rose,
                            hPad = 9.dp,
                            vPad = 4.dp,
                        )
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                DarkStat(p.nightCount.toString(), stringResource(R.string.profile_nights), TT.Paper)
                DarkStat(p.friendCount.toString(), stringResource(R.string.profile_friends), TT.Paper)
                if (p.recordBpm > 0) {
                    DarkStat(p.recordBpm.toString(), stringResource(R.string.profile_record), TT.Rose)
                }
            }
            if (!isMe) {
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        stringResource(if (p.followedByMe) R.string.profile_following else R.string.profile_follow),
                        style = TTType.Button.copy(fontSize = 14.sp),
                        color = TT.Ink,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (p.followedByMe) TT.Acid else TT.Rose)
                            .clickable {
                                container.social.toggleFollow(p.user.handle)
                                followTick++
                            }
                            .padding(vertical = 12.dp),
                    )
                    Text(
                        stringResource(R.string.profile_share),
                        style = TTType.Button.copy(fontSize = 14.sp),
                        color = TT.Paper,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, TT.Ink600, RoundedCornerShape(12.dp))
                            .padding(vertical = 11.dp),
                    )
                }
            }
        }

        Text(
            stringResource(R.string.profile_public_nights),
            style = TTType.MetaSmall.copy(fontSize = 13.sp, letterSpacing = 0.06.em, fontWeight = FontWeight.Bold),
            color = TT.Ink,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 18.dp, bottom = 6.dp),
        )
        Column(
            Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            p.publicNights.chunked(3).forEach { rowNights ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowNights.forEach { night ->
                        Box(Modifier.weight(1f)) {
                            GalleryCover(night, compact = true) {
                                if (night.nightId > 0) nav.navigate(Routes.reveal(night.nightId))
                            }
                        }
                    }
                    repeat(3 - rowNights.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun DarkStat(value: String, label: String, valueColor: androidx.compose.ui.graphics.Color) {
    Column {
        Text(value, style = TTType.NumberMedium.copy(fontSize = 24.sp), color = valueColor)
        Text(label, style = TTType.MetaSmall.copy(fontSize = 10.5.sp), color = TT.Gray45)
    }
}
