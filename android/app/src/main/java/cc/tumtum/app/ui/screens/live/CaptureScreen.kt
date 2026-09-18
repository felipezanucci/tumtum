package cc.tumtum.app.ui.screens.live

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import cc.tumtum.app.R
import cc.tumtum.app.data.ble.BleConnectionState
import cc.tumtum.app.service.CaptureBus
import cc.tumtum.app.service.CaptureService
import cc.tumtum.app.ui.Fmt
import cc.tumtum.app.ui.components.Badge
import cc.tumtum.app.ui.components.OutlineBadge
import cc.tumtum.app.ui.components.TTButton
import cc.tumtum.app.ui.components.TTButtonStyle
import cc.tumtum.app.ui.nav.Routes
import cc.tumtum.app.ui.nav.appContainer
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType
import java.time.Duration
import kotlinx.coroutines.delay

/**
 * a2 — Captura ao vivo. Fundo #0A0A0A, estado calmo, quase sem UI.
 * Com sensor BLE ativo, o número NÃO fica na tela (§10): ver o próprio número
 * muda o número. Fica atrás de um toque longo. O que fica visível é o estado
 * da conexão, o contador de amostras e a bateria do sensor.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CaptureScreen(nav: NavHostController) {
    val container = appContainer()
    val context = LocalContext.current
    val vm: LiveViewModel = viewModel { LiveViewModel(container) }
    val event by vm.activeEvent.collectAsStateWithLifecycle()
    val snapshot by vm.snapshot.collectAsStateWithLifecycle()
    val now by vm.now.collectAsStateWithLifecycle()
    val ending by vm.ending.collectAsStateWithLifecycle()
    val revoked by vm.permissionRevoked.collectAsStateWithLifecycle()
    val bus by CaptureBus.status.collectAsStateWithLifecycle()

    var bpmVisible by remember { mutableStateOf(false) }
    LaunchedEffect(bpmVisible) {
        if (bpmVisible) {
            delay(8_000)
            bpmVisible = false
        }
    }

    val e = event ?: return
    val bleActive = bus.active && bus.eventId == e.id

    Column(
        Modifier
            .fillMaxSize()
            .background(TT.Night)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 28.dp, end = 28.dp, top = 22.dp, bottom = 26.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(9.dp).clip(CircleShape).background(TT.Acid))
            Spacer(Modifier.size(9.dp))
            Text(stringResource(R.string.live_label), style = TTType.MetaWide, color = TT.Acid)
            Spacer(Modifier.weight(1f))
            if (bleActive) {
                val connLabel = when (bus.connection) {
                    is BleConnectionState.Connected -> "${(bus.deviceName ?: "SENSOR").uppercase()} · OK"
                    is BleConnectionState.Reconnecting -> stringResource(R.string.capture_reconnecting).uppercase()
                    else -> stringResource(R.string.capture_disconnected).uppercase()
                }
                Text(
                    connLabel,
                    style = TTType.MetaSmall.copy(letterSpacing = 0.06.em),
                    color = if (bus.connection is BleConnectionState.Connected) TT.Gray55 else TT.Rose,
                )
            } else {
                snapshot?.bestSourceLabel?.let { label ->
                    Text(
                        "${label.uppercase()} · ${snapshot?.coveragePct ?: 0}%",
                        style = TTType.MetaSmall.copy(letterSpacing = 0.06.em),
                        color = TT.Gray55,
                    )
                }
            }
        }
        Spacer(Modifier.height(36.dp))
        Text(e.name, style = TTType.ItemTitle.copy(fontSize = 16.sp), color = TT.Paper)
        Spacer(Modifier.height(3.dp))
        Text(
            listOfNotNull(e.venue.ifBlank { null }, stringResource(R.string.live_started_at, Fmt.hour(e.startAt)))
                .joinToString(" · "),
            style = TTType.BodySmall,
            color = TT.Gray45,
        )

        if (revoked && !bleActive) {
            // §7 — permissão revogada: nova captura bloqueada, com explicação honesta.
            Spacer(Modifier.height(48.dp))
            Text(stringResource(R.string.live_blocked_title), style = TTType.TitleSmall, color = TT.Paper)
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.live_blocked_body), style = TTType.Body, color = TT.Gray45)
            Spacer(Modifier.height(24.dp))
            TTButton(
                stringResource(R.string.live_blocked_cta),
                TTButtonStyle.OutlineOnDark,
                onClick = { nav.navigate(Routes.Permission) },
            )
        } else {
            Spacer(Modifier.height(48.dp))
            Text(stringResource(R.string.live_playing_for), style = TTType.MetaWide, color = TT.Gray55)
            Spacer(Modifier.height(8.dp))
            Text(
                Fmt.stopwatch(Duration.between(e.startAt, now)),
                style = TTType.HeroSmall,
                color = TT.Paper,
            )
            Spacer(Modifier.height(52.dp))
            if (bleActive) {
                // O número mora atrás de um toque longo (§10). Sem número na tela principal.
                Column(
                    Modifier.combinedClickable(
                        onClick = {},
                        onLongClick = { bpmVisible = true },
                    ),
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            if (bpmVisible) (bus.lastBpm?.toString() ?: "—") else "· · ·",
                            style = TTType.HeroLive,
                            color = TT.Rose,
                        )
                        if (bpmVisible) {
                            Spacer(Modifier.size(14.dp))
                            Text(
                                stringResource(R.string.live_bpm_now),
                                style = TTType.Body.copy(fontSize = 17.sp),
                                color = TT.Gray45,
                                modifier = Modifier.padding(bottom = 10.dp),
                            )
                        }
                    }
                    if (!bpmVisible) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.capture_hold_to_see),
                            style = TTType.Footnote,
                            color = TT.Gray55,
                        )
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        snapshot?.currentBpm?.toString() ?: "—",
                        style = TTType.HeroLive,
                        color = TT.Rose,
                    )
                    Spacer(Modifier.size(14.dp))
                    Text(
                        stringResource(R.string.live_bpm_now),
                        style = TTType.Body.copy(fontSize = 17.sp),
                        color = TT.Gray45,
                        modifier = Modifier.padding(bottom = 10.dp),
                    )
                }
            }
            Spacer(Modifier.height(26.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (bleActive) {
                    Badge(stringResource(R.string.capture_samples, bus.samplesWritten), hPad = 11.dp, vPad = 6.dp)
                    bus.sensorBatteryPct?.let { pct ->
                        OutlineBadge(
                            stringResource(R.string.capture_sensor_battery, pct),
                            borderColor = TT.Ink600,
                            contentColor = TT.Gray25,
                            hPad = 11.dp,
                            vPad = 6.dp,
                        )
                    }
                } else {
                    Badge(stringResource(R.string.live_moments, snapshot?.momentCount ?: 0), hPad = 11.dp, vPad = 6.dp)
                    snapshot?.peakBpm?.let { peak ->
                        OutlineBadge(
                            stringResource(R.string.live_peak_so_far, peak),
                            borderColor = TT.Ink600,
                            contentColor = TT.Gray25,
                            hPad = 11.dp,
                            vPad = 6.dp,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))
        Text(
            stringResource(R.string.live_hint),
            style = TTType.Body.copy(fontSize = 14.sp),
            color = TT.Gray55,
        )
        Spacer(Modifier.height(18.dp))
        TTButton(
            stringResource(R.string.live_end),
            TTButtonStyle.Acid,
            enabled = !ending,
            onClick = {
                // Encerra o serviço (grava offsets finais e limpa a sessão ativa) e mede as fontes.
                CaptureService.stop(context)
                vm.endNight {
                    nav.navigate(Routes.EndNight) {
                        popUpTo(Routes.Live) { inclusive = false }
                    }
                }
            },
        )
    }
}
