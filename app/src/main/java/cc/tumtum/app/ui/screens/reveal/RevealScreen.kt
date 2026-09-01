package cc.tumtum.app.ui.screens.reveal

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cc.tumtum.app.R
import cc.tumtum.app.ui.Fmt
import cc.tumtum.app.ui.components.Badge
import cc.tumtum.app.ui.components.BpmCurve
import cc.tumtum.app.ui.components.TTButton
import cc.tumtum.app.ui.components.TTButtonStyle
import cc.tumtum.app.ui.nav.Routes
import cc.tumtum.app.ui.nav.appContainer
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType
import java.time.Duration
import kotlinx.coroutines.launch

/**
 * a3 — A noite, a revela. O momento de maior impacto do produto:
 * a curva desenha da esquerda para a direita em 1,2s e só então os picos aparecem.
 * Fundo preto; linha rosa; marcador amarelo; buraco visível, nunca interpolado.
 */
@Composable
fun RevealScreen(nav: NavHostController, nightId: Long) {
    val container = appContainer()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var exporting by remember { mutableStateOf(false) }
    val night by container.nights.night(nightId).collectAsStateWithLifecycle(initialValue = null)

    val progress = remember { Animatable(0f) }
    val peaksAlpha = remember { Animatable(0f) }
    LaunchedEffect(night?.id) {
        if (night != null) {
            progress.snapTo(0f)
            peaksAlpha.snapTo(0f)
            progress.animateTo(1f, tween(durationMillis = 1_200, easing = FastOutSlowInEasing))
            peaksAlpha.animateTo(1f, tween(durationMillis = 350))
        }
    }

    val n = night ?: return
    Column(
        Modifier
            .fillMaxSize()
            .background(TT.Night)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 28.dp, end = 28.dp, top = 22.dp, bottom = 26.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.reveal_label), style = TTType.MetaWide, color = TT.Acid)
            Text(
                "←",
                style = TTType.ItemSub.copy(fontSize = 14.sp),
                color = TT.Gray45,
                modifier = Modifier.clickable { nav.popBackStack() },
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(n.eventName, style = TTType.ItemSub.copy(fontSize = 14.sp), color = TT.Paper)
            Text(" · ${Fmt.date(n.date)}", style = TTType.ItemSub.copy(fontSize = 14.sp), color = TT.Gray55)
        }
        Spacer(Modifier.height(22.dp))
        Text(
            stringResource(R.string.reveal_default_title),
            style = TTType.ShoutSmall.copy(fontSize = 23.sp, lineHeight = 24.5.sp),
            color = TT.Paper,
        )
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text("${n.peakBpm}", style = TTType.Hero, color = TT.Rose)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.padding(bottom = 8.dp)) {
                Text(stringResource(R.string.reveal_bpm), style = TTType.ItemTitle.copy(fontSize = 17.sp), color = TT.Paper)
                Text(
                    stringResource(R.string.reveal_at, Fmt.hour(n.peakAt)),
                    style = TTType.ItemSub.copy(fontSize = 14.sp),
                    color = TT.Gray45,
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        BpmCurve(
            samples = n.samples,
            windowStart = n.startAt,
            windowEnd = n.endAt,
            lineColor = TT.DataLineOnDark,
            markerColor = TT.DataMarkerOnDark,
            gapColor = TT.DataGap,
            progress = progress.value,
            modifier = Modifier.fillMaxWidth().height(118.dp),
        )
        Spacer(Modifier.height(7.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(Fmt.hour(n.startAt).uppercase(), style = TTType.MetaSmall.copy(fontSize = 10.sp), color = TT.Gray55)
            val biggestGapMin = n.gaps.maxOfOrNull { Duration.between(it.start, it.end).toMinutes() } ?: 0
            if (biggestGapMin >= 1) {
                Text(
                    stringResource(R.string.reveal_gap, biggestGapMin.toInt()),
                    style = TTType.MetaSmall.copy(fontSize = 10.sp),
                    color = TT.Gray70,
                )
            }
            Text(Fmt.hour(n.peakAt).uppercase(), style = TTType.MetaSmall.copy(fontSize = 10.sp), color = TT.Acid)
        }
        Spacer(Modifier.height(18.dp))

        // Picos — revelados depois da curva.
        Column(Modifier.alpha(peaksAlpha.value)) {
            DividerDark()
            n.moments.take(3).forEach { moment ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "${moment.bpm}",
                        style = TTType.NumberRow,
                        color = TT.Paper,
                        modifier = Modifier.width(52.dp),
                    )
                    Text(
                        stringResource(R.string.reveal_moment_meta, Fmt.hour(moment.at), moment.durationSec),
                        style = TTType.BodySmall,
                        color = TT.Gray45,
                        modifier = Modifier.weight(1f),
                    )
                    if (moment.isPeak) {
                        Badge(stringResource(R.string.reveal_biggest), hPad = 7.dp, vPad = 3.dp)
                    }
                }
                DividerDark()
            }
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.crowd_you) + " × " + stringResource(R.string.crowd_them),
                style = TTType.MetaSmall,
                color = TT.Gray55,
                modifier = Modifier
                    .clickable { nav.navigate(Routes.crowd(n.id)) }
                    .padding(vertical = 6.dp),
            )
        }

        Spacer(Modifier.height(24.dp))
        TTButton(
            stringResource(R.string.reveal_share),
            TTButtonStyle.Rose,
            onClick = { nav.navigate(Routes.choose(n.id)) },
        )
        Spacer(Modifier.height(10.dp))
        // §9 — extração manual, à prova de 2h da manhã: ZIP → share sheet, sem rede.
        TTButton(
            if (exporting) stringResource(R.string.export_running) else stringResource(R.string.export_session),
            TTButtonStyle.OutlineOnDark,
            enabled = !exporting,
            onClick = {
                exporting = true
                scope.launch {
                    runCatching {
                        val zip = container.exporter.exportNight(n.id)
                        context.startActivity(container.exporter.shareIntent(zip))
                    }
                    exporting = false
                }
            },
        )
    }
}

@Composable
private fun DividerDark() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(TT.Ink700))
}
