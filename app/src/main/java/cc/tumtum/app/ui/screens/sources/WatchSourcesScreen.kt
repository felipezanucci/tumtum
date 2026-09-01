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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cc.tumtum.app.R
import cc.tumtum.app.data.repo.SourceMeasurement
import cc.tumtum.app.domain.WatchSource
import cc.tumtum.app.ui.components.Badge
import cc.tumtum.app.ui.components.TTButton
import cc.tumtum.app.ui.components.TTButtonStyle
import cc.tumtum.app.ui.nav.Routes
import cc.tumtum.app.ui.nav.appContainer
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.launch

/**
 * b4 — Trazer do meu relógio. Densidade real por fonte na janela (§7):
 * a decisão aparece, nunca é tomada escondida. Fonte sem dado fica cinza.
 *
 * setupMode: mede as últimas 24h (ainda não há evento).
 * fim de noite: usa a medição da janela real feita ao encerrar (EndNightCache).
 */
@Composable
fun WatchSourcesScreen(nav: NavHostController, setupMode: Boolean) {
    val container = appContainer()
    val scope = rememberCoroutineScope()

    val measurement by produceState<SourceMeasurement?>(initialValue = if (setupMode) null else container.endNight.measurement) {
        if (setupMode) {
            val end = Instant.now()
            val start = end.minus(Duration.ofHours(24))
            val bySource = container.health.readWindowBySource(start, end)
            value = SourceMeasurement(start, end, bySource, container.health.sourceDensities(bySource, start, end))
        }
    }

    var selected by remember { mutableStateOf<String?>(null) }
    var noData by remember { mutableStateOf(false) }
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
        Text(
            "←",
            style = TTType.ItemSub.copy(fontSize = 14.sp),
            color = TT.Gray45,
            modifier = Modifier.clickable { nav.popBackStack() }.padding(4.dp),
        )
        Spacer(Modifier.height(34.dp))
        Text(stringResource(R.string.sources_title), style = TTType.TitleSmall, color = TT.Ink)
        Spacer(Modifier.height(6.dp))
        Text(stringResource(R.string.sources_subtitle), style = TTType.Body, color = TT.Gray45)
        Spacer(Modifier.height(26.dp))

        if (setupMode) {
            // §10 — sensor BLE ao vivo: parear aqui, lembrar o endereço, reconectar sozinho.
            val user by container.prefs.state.collectAsStateWithLifecycle(initialValue = null)
            SensorSection(prefs = container.prefs, bleName = user?.bleName)
            Spacer(Modifier.height(26.dp))
        }

        if (m != null && sources.none { it.hasData }) {
            // Erro honesto — sem piada, sem "ops".
            Text(stringResource(R.string.sources_empty), style = TTType.Body, color = TT.Ink)
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            sources.forEach { source ->
                SourceCard(
                    source = source,
                    selected = source.packageName == selectedPkg,
                    setupMode = setupMode,
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
        // No setup, um botão morto confunde: o CTA só aparece quando há fonte utilizável.
        if (!setupMode || selectedSource?.hasData == true) {
        TTButton(
            text = selectedSource?.let { stringResource(R.string.sources_use, it.label) }
                ?: stringResource(R.string.sources_title),
            style = TTButtonStyle.Rose,
            enabled = selectedSource?.hasData == true,
            onClick = {
                val src = selectedSource ?: return@TTButton
                scope.launch {
                    container.prefs.setSource(src.packageName, src.label)
                    if (setupMode) {
                        container.prefs.setOnboarded()
                        nav.navigate(Routes.Feed) { popUpTo(0) { inclusive = true } }
                    } else {
                        val event = container.endNight.event ?: return@launch
                        val meas = container.endNight.measurement ?: return@launch
                        val nightId = container.nights.saveNight(event, meas, src.packageName)
                        if (nightId == null) {
                            noData = true
                        } else {
                            container.endNight.clear()
                            nav.navigate(Routes.reveal(nightId)) {
                                popUpTo(Routes.Feed)
                            }
                        }
                    }
                }
            },
        )
        }
        if (setupMode) {
            Spacer(Modifier.height(10.dp))
            // Sempre dá para seguir e resolver depois — sensor pareado conta, e o
            // caminho de volta mora em Configurações e no vazio (a5).
            TTButton(
                stringResource(R.string.sources_skip),
                TTButtonStyle.Outline,
                onClick = {
                    scope.launch {
                        container.prefs.setOnboarded()
                        nav.navigate(Routes.Feed) { popUpTo(0) { inclusive = true } }
                    }
                },
            )
        }
    }
}

@Composable
private fun SourceCard(source: WatchSource, selected: Boolean, setupMode: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    val borderMod = if (selected && source.hasData) {
        Modifier.border(2.dp, TT.Ink, shape)
    } else {
        Modifier.border(1.dp, TT.Gray10, shape)
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
                color = if (source.hasData) TT.Ink else TT.Gray25,
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
                    .background(TT.Gray10),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(source.coveragePct / 100f)
                        .height(8.dp)
                        .background(if (source.isBest) TT.Ink else TT.Gray45),
                )
            }
            val interval = if (source.medianIntervalSec >= 60) {
                stringResource(R.string.sources_interval_min, source.medianIntervalSec / 60)
            } else {
                stringResource(R.string.sources_interval_s, source.medianIntervalSec)
            }
            Text(
                stringResource(
                    if (setupMode) R.string.sources_coverage_24h else R.string.sources_coverage,
                    source.coveragePct,
                    interval,
                ),
                style = TTType.Footnote,
                color = TT.Gray70,
            )
        }
    }
}
