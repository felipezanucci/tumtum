package cc.tumtum.app.ui.screens.choose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cc.tumtum.app.R
import cc.tumtum.app.domain.Skin
import cc.tumtum.app.ui.Fmt
import cc.tumtum.app.ui.components.ShareCardView
import cc.tumtum.app.ui.nav.Routes
import cc.tumtum.app.ui.nav.appContainer
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType

/** Escolha (UI kit do core loop): um momento, qual pele? A forma fica, a pele muda. */
@Composable
fun ChooseSkinScreen(nav: NavHostController, nightId: Long) {
    val container = appContainer()
    val night by container.nights.night(nightId).collectAsStateWithLifecycle(initialValue = null)
    val n = night ?: return

    val skins = listOf(
        Skin.PINK to stringResource(R.string.skin_pink),
        Skin.BLACK to stringResource(R.string.skin_black),
        Skin.YELLOW to stringResource(R.string.skin_yellow),
        Skin.WHITE to stringResource(R.string.skin_white),
    )

    Column(
        Modifier
            .fillMaxSize()
            .background(TT.Paper)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 26.dp, end = 26.dp, top = 22.dp, bottom = 24.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.choose_label), style = TTType.MetaSmall, color = TT.Gray45)
            Text(
                "←",
                style = TTType.ItemSub.copy(fontSize = 14.sp),
                color = TT.Gray45,
                modifier = Modifier.clickable { nav.popBackStack() },
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            stringResource(R.string.choose_title),
            style = TTType.ShoutSmall.copy(fontSize = 24.sp, lineHeight = 25.sp),
            color = TT.Ink,
        )
        Spacer(Modifier.height(6.dp))
        Text(stringResource(R.string.choose_subtitle), style = TTType.BodySmall, color = TT.Gray45)
        Spacer(Modifier.height(20.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(skins) { (skin, label) ->
                Column(
                    Modifier.clickable { nav.navigate(Routes.card(n.id, skin)) },
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    ShareCardView(
                        skin = skin,
                        title = stringResource(R.string.reveal_default_title),
                        bpm = n.peakBpm,
                        meta = stringResource(R.string.reveal_bpm) + " " + stringResource(R.string.reveal_at, Fmt.hour(n.peakAt)),
                        width = 150.dp,
                    )
                    Text(
                        label,
                        style = TTType.MetaSmall.copy(fontSize = 10.sp, letterSpacing = 0.1.em),
                        color = TT.Gray70,
                    )
                }
            }
        }
    }
}
