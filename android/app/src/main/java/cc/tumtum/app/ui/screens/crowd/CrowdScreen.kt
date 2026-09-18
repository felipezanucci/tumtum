package cc.tumtum.app.ui.screens.crowd

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cc.tumtum.app.R
import cc.tumtum.app.domain.HrSample
import cc.tumtum.app.ui.Fmt
import cc.tumtum.app.ui.components.Badge
import cc.tumtum.app.ui.components.BpmCurve
import cc.tumtum.app.ui.components.OutlineBadge
import cc.tumtum.app.ui.components.PlateText
import cc.tumtum.app.ui.nav.appContainer
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * a4 — A galera. Compara a sua noite com o agregado do evento.
 * Diz exatamente o que mede: pico de cada corpo, nunca ranking de BPM.
 * (Agregado vem do repositório fake até o backend existir.)
 */
@Composable
fun CrowdScreen(nav: NavHostController, nightId: Long) {
    val container = appContainer()
    val night by container.nights.night(nightId).collectAsStateWithLifecycle(initialValue = null)
    val n = night ?: return
    val stats = remember(n.eventName) { container.social.crowdStats(n.eventName) }

    // Curva do agregado (fake): versão amortecida da própria noite — trocável junto com o backend.
    val cohort = remember(n.id) {
        if (n.samples.isEmpty()) emptyList()
        else {
            val avg = n.samples.map { it.bpm }.average()
            n.samples.mapIndexed { i, s ->
                val t = i.toFloat() / n.samples.size
                HrSample(s.time, (avg + (s.bpm - avg) * 0.45 + 6 * sin(t * 9.0)).roundToInt())
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(TT.Paper)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 18.dp, bottom = 26.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                stringResource(R.string.crowd_label, n.eventName.uppercase()),
                style = TTType.MetaWide,
                color = TT.Gray55,
            )
            Text(
                "←",
                style = TTType.ItemSub.copy(fontSize = 14.sp),
                color = TT.Gray45,
                modifier = Modifier.clickable { nav.popBackStack() },
            )
        }
        Spacer(Modifier.height(18.dp))
        Text(stringResource(R.string.crowd_headline), style = TTType.ShoutSmall, color = TT.Ink)
        Spacer(Modifier.height(8.dp))
        PlateText(
            stringResource(R.string.crowd_plate),
            background = TT.Rose,
            contentColor = TT.Ink,
            style = TTType.ItemTitle.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
        )
        Spacer(Modifier.height(26.dp))
        Text(
            "${stats.cohortPct}%",
            style = TTType.Hero.copy(fontSize = 96.sp, lineHeight = 78.sp),
            color = TT.Ink,
        )
        Text(
            buildAnnotatedString {
                val people = Fmt.thousands(stats.sharedCount)
                val full = stringResource(
                    R.string.crowd_body,
                    people,
                    stats.cohortPct,
                    stats.windowStartLabel,
                    stats.windowEndLabel,
                )
                val bold = "$people pessoas"
                val idx = full.indexOf(bold)
                if (idx >= 0) {
                    append(full.substring(0, idx))
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = TT.Ink)) { append(bold) }
                    append(full.substring(idx + bold.length))
                } else {
                    append(full)
                }
            },
            style = TTType.BodySmall.copy(fontSize = 13.5.sp),
            color = TT.Gray70,
            modifier = Modifier.padding(top = 10.dp).width(300.dp),
        )
        Spacer(Modifier.height(24.dp))
        BpmCurve(
            samples = n.samples,
            windowStart = n.startAt,
            windowEnd = n.endAt,
            lineColor = TT.DataLineOnLight,
            markerColor = TT.DataMarkerOnLight,
            secondarySamples = cohort,
            secondaryColor = TT.DataSecondary,
            gapColor = TT.Gray25,
            modifier = Modifier.fillMaxWidth().height(96.dp),
        )
        Spacer(Modifier.height(7.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            LegendItem(stringResource(R.string.crowd_you), TT.Ink)
            LegendItem(stringResource(R.string.crowd_them), TT.DataSecondary)
        }
        Spacer(Modifier.height(22.dp))
        DividerLight()
        stats.peaks.forEach { peak ->
            Row(
                Modifier.fillMaxWidth().padding(vertical = 13.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    peak.timeLabel,
                    style = TTType.BodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = TT.Ink,
                    modifier = Modifier.width(52.dp),
                )
                Text(peak.label, style = TTType.BodySmall, color = TT.Gray70, modifier = Modifier.weight(1f))
                if (peak.highlight) {
                    Badge(stringResource(R.string.crowd_people, Fmt.thousands(peak.people)), hPad = 7.dp, vPad = 3.dp)
                } else {
                    OutlineBadge(Fmt.thousands(peak.people), hPad = 7.dp, vPad = 3.dp)
                }
            }
            DividerLight()
        }
        Spacer(Modifier.height(14.dp))
        Text(
            stringResource(R.string.crowd_footnote),
            style = TTType.MetaSmall.copy(letterSpacing = 0.sp, fontWeight = FontWeight.Normal, fontSize = 11.sp, lineHeight = 16.5.sp),
            color = TT.Gray45,
        )
    }
}

@Composable
private fun LegendItem(label: String, color: androidx.compose.ui.graphics.Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = TTType.MetaSmall.copy(fontSize = 10.sp), color = TT.Gray55)
        Box(Modifier.size(width = 14.dp, height = 3.dp).background(color))
    }
}

@Composable
private fun DividerLight() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(TT.Gray10))
}
