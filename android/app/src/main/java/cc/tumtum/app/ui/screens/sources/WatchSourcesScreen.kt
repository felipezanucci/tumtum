package cc.tumtum.app.ui.screens.sources

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import cc.tumtum.app.R
import cc.tumtum.app.ui.components.BackArrow
import cc.tumtum.app.data.repo.saveEndedNight
import cc.tumtum.app.domain.WatchSource
import cc.tumtum.app.ui.components.Badge
import cc.tumtum.app.ui.components.TTButton
import cc.tumtum.app.ui.components.TTButtonStyle
import cc.tumtum.app.ui.nav.Routes
import cc.tumtum.app.ui.nav.appContainer
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType
import kotlinx.coroutines.launch

/**
 * b4 — Trazer do meu relógio, at the end of a night. Densidade real por fonte
 * na janela (§7): a decisão aparece, nunca é tomada escondida. Fonte sem dado
 * fica cinza. Usa a medição da janela real feita ao encerrar (EndNightCache).
 *
 * Until 25/09 this screen was also the setup step, run on the last 24 hours,
 * and it said after-a-night things where there was no night. Setup is
 * [SetupScreen] now.
 */
@Composable
fun WatchSourcesScreen(nav: NavHostController) {
    val container = appContainer()
    val scope = rememberCoroutineScope()

    val measurement = container.endNight.measurement

    var selected by remember { mutableStateOf<String?>(null) }
    var noData by remember { mutableStateOf(false) }
    var pickHint by remember { mutableStateOf(false) }
    val m = measurement
    val sources = m?.sources.orEmpty()
    val selectedPkg = selected ?: sources.firstOrNull { it.isBest }?.packageName
    val selectedSource = sources.firstOrNull { it.packageName == selectedPkg }

    Column(
        Modifier
            .fillMaxSize()
            .background(TT.Paper)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 28.dp, end = 28.dp, top = 22.dp, bottom = 30.dp),
    ) {
        BackArrow(onClick = { nav.popBackStack() })
        Spacer(Modifier.height(34.dp))
        val nothingRecorded = m != null && sources.none { it.hasData }
        if (nothingRecorded) {
            // Fim de noite sem uma batida em fonte nenhuma: dizer isso, não
            // oferecer uma escolha entre fontes vazias.
            Text(stringResource(R.string.sources_none_title), style = TTType.TitleSmall, color = TT.Ink)
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.sources_none_body), style = TTType.Body, color = TT.Gray70)
            Spacer(Modifier.height(30.dp))
            TTButton(
                stringResource(R.string.sources_none_back),
                TTButtonStyle.Ink,
                onClick = {
                    container.endNight.clear()
                    nav.navigate(Routes.Feed) { popUpTo(Routes.Feed) { inclusive = true } }
                },
            )
            return@Column
        }
        Text(stringResource(R.string.sources_title), style = TTType.TitleSmall, color = TT.Ink)
        Spacer(Modifier.height(6.dp))
        Text(stringResource(R.string.sources_subtitle), style = TTType.Body, color = TT.Gray45)
        Spacer(Modifier.height(26.dp))

        if (m != null && sources.none { it.hasData }) {
            // Erro honesto — sem piada, sem "ops".
            Text(stringResource(R.string.sources_empty), style = TTType.Body, color = TT.Ink)
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            sources.forEach { source ->
                SourceCard(
                    source = source,
                    selected = source.packageName == selectedPkg,
                    setupMode = false,
                    onClick = { if (source.hasData) selected = source.packageName },
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.sources_footnote), style = TTType.Footnote, color = TT.Gray45)
        if (noData) {
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.sources_empty), style = TTType.Footnote, color = TT.Gray70)
        }

        Spacer(Modifier.height(30.dp))
        TTButton(
            text = selectedSource?.let { stringResource(R.string.sources_use, it.label) }
                ?: stringResource(R.string.sources_title),
            style = TTButtonStyle.Rose,
            enabled = selectedSource?.hasData == true,
            onDeclined = { pickHint = true },
            onClick = {
                val src = selectedSource ?: return@TTButton
                scope.launch {
                    container.prefs.setSource(src.packageName, src.label)
                    val event = container.endNight.event ?: return@launch
                    val meas = container.endNight.measurement ?: return@launch
                    val nightId = container.saveEndedNight(event, meas, src.packageName)
                    if (nightId == null) {
                        noData = true
                    } else {
                        nav.navigate(Routes.reveal(nightId)) {
                            popUpTo(Routes.Feed)
                        }
                    }
                }
            },
        )
        if (pickHint && selectedSource?.hasData != true) {
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.sources_pick_one), style = TTType.BodySmall, color = TT.Rose)
        }
    }
}

@Composable
internal fun SourceCard(
    source: WatchSource,
    selected: Boolean,
    setupMode: Boolean,
    onClick: () -> Unit,
    onDark: Boolean = false,
) {
    val shape = RoundedCornerShape(12.dp)
    val borderMod = if (selected && source.hasData) {
        Modifier.border(2.dp, if (onDark) TT.Paper else TT.Ink, shape)
    } else {
        Modifier.border(1.dp, if (onDark) TT.Ink600 else TT.Gray10, shape)
    }
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .then(borderMod)
            .clickable(enabled = source.hasData, onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                source.label,
                style = TTType.ItemTitle.copy(fontSize = 16.sp),
                color = if (!source.hasData) TT.Gray25 else if (onDark) TT.Paper else TT.Ink,
            )
            if (source.isBest && source.hasData) {
                Badge(stringResource(R.string.sources_best))
            } else if (!source.hasData) {
                Text(stringResource(R.string.sources_no_data), style = TTType.Footnote, color = TT.Gray25)
            }
        }
        if (source.hasData) {
            // Barra de densidade: trilho cinza, preenchimento preto (melhor) ou cinza.
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (onDark) TT.Ink700 else TT.Gray10),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(source.coveragePct / 100f)
                        .height(8.dp)
                        // On black the line is Pink, as in every chart on a dark surface.
                        .background(
                            when {
                                !source.isBest -> TT.Gray45
                                onDark -> TT.DataLineOnDark
                                else -> TT.Ink
                            },
                        ),
                )
            }
            val interval = if (source.medianIntervalSec >= 60) {
                stringResource(R.string.sources_interval_min, source.medianIntervalSec / 60)
            } else {
                stringResource(R.string.sources_interval_s, source.medianIntervalSec)
            }
            Text(
                stringResource(
                    if (setupMode) R.string.sources_coverage_last_hour else R.string.sources_coverage,
                    source.coveragePct,
                    interval,
                ),
                style = TTType.Footnote,
                color = if (onDark) TT.Gray45 else TT.Gray70,
            )
        }
    }
}
