package cc.tumtum.app.ui.screens.card

import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cc.tumtum.app.R
import cc.tumtum.app.domain.Skin
import cc.tumtum.app.ui.Fmt
import cc.tumtum.app.ui.components.ShareCardView
import cc.tumtum.app.ui.components.TTButton
import cc.tumtum.app.ui.components.TTButtonStyle
import cc.tumtum.app.ui.nav.appContainer
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType
import kotlinx.coroutines.launch

/**
 * Seu card (UI kit do core loop). Compartilhar é sempre ativo:
 * nada sai daqui sem o "Postar no feed".
 */
@Composable
fun CardScreen(nav: NavHostController, nightId: Long, skin: Skin) {
    val container = appContainer()
    val scope = rememberCoroutineScope()
    val night by container.nights.night(nightId).collectAsStateWithLifecycle(initialValue = null)
    val user by container.prefs.state.collectAsStateWithLifecycle(initialValue = null)
    var posted by remember { mutableStateOf(false) }
    val n = night ?: return

    Column(
        Modifier
            .fillMaxSize()
            .background(TT.Night)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 26.dp, end = 26.dp, top = 22.dp, bottom = 24.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "←",
                style = TTType.ItemSub.copy(fontSize = 14.sp),
                color = TT.Gray45,
                modifier = Modifier.clickable { nav.popBackStack() },
            )
            Spacer(Modifier.weight(1f))
            Text(stringResource(R.string.card_label), style = TTType.MetaSmall, color = TT.Acid)
        }
        Box(Modifier.weight(1f).fillMaxWidth().padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
            ShareCardView(
                skin = skin,
                title = stringResource(R.string.reveal_default_title),
                bpm = n.peakBpm,
                meta = stringResource(R.string.reveal_bpm) + " " + stringResource(R.string.reveal_at, Fmt.hour(n.peakAt)),
                chip = "${n.eventName.uppercase()} · ${Fmt.hour(n.peakAt).uppercase()}",
                width = 214.dp,
                curveSamples = if (skin == Skin.BLACK) n.samples else null,
                curveWindow = if (skin == Skin.BLACK) n.startAt to n.endAt else null,
            )
        }
        if (posted) {
            Text(
                stringResource(R.string.card_posted),
                style = TTType.ItemSub.copy(fontSize = 14.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                color = TT.Acid,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(18.dp),
            )
        } else {
            TTButton(
                stringResource(R.string.card_post),
                TTButtonStyle.Rose,
                onClick = {
                    val account = user?.account ?: return@TTButton
                    scope.launch {
                        container.nights.publish(n.id, skin)
                        container.social.postOwnMoment(n, skin, account)
                        posted = true
                    }
                },
            )
        }
        Spacer(Modifier.height(2.dp))
    }
}
