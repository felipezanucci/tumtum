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
import cc.tumtum.app.ui.components.BackArrow
import cc.tumtum.app.data.repo.NightLookup
import cc.tumtum.app.data.repo.NightSync
import cc.tumtum.app.domain.UploadState
import cc.tumtum.app.domain.RevealLock
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import cc.tumtum.app.data.repo.SyncPhase
import cc.tumtum.app.ui.components.revealWhen
import androidx.core.app.NotificationManagerCompat

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
    val lookup by container.nights.lookup(nightId).collectAsStateWithLifecycle(initialValue = null)
    val night = (lookup as? NightLookup.Found)?.night
    val user by container.prefs.state.collectAsStateWithLifecycle(initialValue = null)
    val showsExport = user?.showsExport == true
    val home = {
        nav.navigate(Routes.Feed) {
            popUpTo(nav.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }

    val progress = remember { Animatable(0f) }
    val peaksAlpha = remember { Animatable(0f) }
    LaunchedEffect(night?.id) {
        if (night != null) {
            // Etapa 2: opening a night that never reached the server is a natural moment to try again.
            if (night?.uploadState != UploadState.ANALYSED) container.sync.uploadLater(night!!.id)
            progress.snapTo(0f)
            peaksAlpha.snapTo(0f)
            progress.animateTo(1f, tween(durationMillis = 1_200, easing = FastOutSlowInEasing))
            peaksAlpha.animateTo(1f, tween(durationMillis = 350))
        }
    }

    // A night that is not here, or not this account's, is said — never a
    // blank screen (25/09: a "Sua noite abriu." for a deleted night opened one).
    when (lookup) {
        null -> return
        NightLookup.Missing -> {
            NightNotHere(R.string.reveal_missing_title, R.string.reveal_missing_body, onHome = home)
            return
        }
        NightLookup.OtherAccount -> {
            NightNotHere(R.string.reveal_other_title, R.string.reveal_other_body, onHome = home)
            return
        }
        is NightLookup.Found -> Unit
    }
    val n = night ?: return

    // A trava da revela: antes das 10h a curva não existe para quem olha (protocolo).
    var lockTick by remember { mutableStateOf(0) }
    val locked = remember(lockTick, n.revealAt) { RevealLock.isLocked(n.revealAt) }
    LaunchedEffect(n.revealAt) {
        val at = n.revealAt ?: return@LaunchedEffect
        val waitMs = java.time.Duration.between(java.time.Instant.now(), at).toMillis()
        if (waitMs > 0) {
            kotlinx.coroutines.delay(waitMs + 500)
            lockTick++
        }
    }
    if (locked) {
        LockedNightView(
            night = n,
            exporting = exporting,
            onBack = { nav.popBackStack() },
            onHome = home,
            onExport = if (!showsExport) null else fun() {
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
        return
    }

    // Dois blocos: o de cima rola, o botão de compartilhar é fixo embaixo. No
    // ensaio de 18/09 ele ficou no segundo scroll e ninguém sabia que existia.
    Column(
        Modifier
            .fillMaxSize()
            .background(TT.Night)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 28.dp, end = 28.dp, top = 22.dp, bottom = 26.dp),
    ) {
      Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.reveal_label), style = TTType.MetaWide, color = TT.Acid)
            BackArrow(onClick = { nav.popBackStack() }, onDark = true)
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(n.eventName, style = TTType.ItemSub.copy(fontSize = 14.sp), color = TT.Paper)
            Text(" · ${Fmt.date(n.date)}", style = TTType.ItemSub.copy(fontSize = 14.sp), color = TT.Gray55)
        }
        Spacer(Modifier.height(22.dp))
        Text(
            // A night with no moment gets its own line (25/09): "Aí veio isso"
            // over a flat minute pointed at nothing.
            stringResource(if (n.moments.isEmpty()) R.string.reveal_calm_title else R.string.reveal_default_title),
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
            // The axis ends where the night ended (25/09): the peak's time sat
            // here in yellow, at the far right, under a dot at the far left —
            // "15H19 … 15H19" read as a night that began and ended at once.
            // The peak's time is already said next to its number.
            Text(Fmt.hour(n.endAt).uppercase(), style = TTType.MetaSmall.copy(fontSize = 10.sp), color = TT.Gray55)
        }
        Spacer(Modifier.height(14.dp))
        // Where the night stands with the server — the first thing under the
        // curve, not the last thing under the moments. On 21/09 the two steps
        // ran behind the curve's own 1.2 s animation (the block was inside the
        // faded peaks column), finished in under a second, and the result sat
        // below the fold: the person saw nothing and read "nothing happened",
        // then a GOL that named no moment. The steps now stay on screen long
        // enough to be read, and the state line sits where the eye already is.
        SyncStatus(n, container.sync)
        Spacer(Modifier.height(14.dp))

        // Picos — revelados depois da curva.
        Column(Modifier.alpha(peaksAlpha.value)) {
            DividerDark()
            n.moments.take(3).forEach { moment ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "${moment.bpm}",
                        style = TTType.NumberRow,
                        color = TT.Paper,
                        modifier = Modifier.width(52.dp),
                    )
                    Column(Modifier.weight(1f)) {
                        // The cause, when the event's timeline measured one — and
                        // nothing when it did not. **The fan is never asked** (Felipe,
                        // 22/09): "Toca pra dizer o que tava rolando" was the last
                        // door left after the guess chips went, and the rule covers
                        // both. A moment arrives named or stays nameless.
                        moment.label?.let {
                            Text(it, style = TTType.BodySmall, color = TT.Paper)
                        }
                        Text(
                            stringResource(R.string.reveal_moment_meta, Fmt.hour(moment.at), moment.durationSec),
                            style = TTType.BodySmall,
                            color = TT.Gray45,
                        )
                    }
                    if (moment.isPeak) {
                        Badge(stringResource(R.string.reveal_biggest), hPad = 7.dp, vPad = 3.dp)
                    }
                }
                DividerDark()
            }
            // "Sua noite × a galera" (card 04) fica fora até existir uma amostra
            // coletiva real: a tela por trás dela mostra 3.412 pessoas inventadas
            // pelo mockup, e um número inventado na mão de um fã é uma mentira.
        }

        // §9 — extração manual, à prova de 2h da manhã: ZIP → share sheet, sem rede.
        // Only on the operator's account or a protocol phone (25/09): a fan
        // never needed a button for raw data.
        if (showsExport) {
            Spacer(Modifier.height(18.dp))
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
        Spacer(Modifier.height(14.dp))
        TTButton(
            stringResource(R.string.reveal_share),
            TTButtonStyle.Rose,
            onClick = { nav.navigate(Routes.choose(n.id)) },
        )
    }
}

/**
 * Three honest states, never a blend: the two real steps while they run (and
 * for a moment after, both marked OK, so a sync faster than a glance is
 * still seen to have happened), then the line that says which moments these
 * are and why — the server's, the phone's, or the phone's because the server
 * could not be reached — with the one thing to do about it.
 */
@Composable
private fun SyncStatus(n: cc.tumtum.app.domain.Night, sync: NightSync) {
    val uploading by sync.uploading.collectAsStateWithLifecycle()
    val phases by sync.phase.collectAsStateWithLifecycle()
    val phase = phases[n.id]
    var shown by remember { mutableStateOf<SyncPhase?>(null) }
    var holding by remember { mutableStateOf(false) }
    LaunchedEffect(phase) {
        if (phase != null) {
            shown = phase
            holding = false
        } else if (shown != null) {
            holding = true
            kotlinx.coroutines.delay(1_800)
            holding = false
            shown = null
        }
    }
    when {
        phase != null -> SyncSteps(phase, n.samples.size, finished = false)
        holding -> SyncSteps(shown ?: SyncPhase.ANALYSING, n.samples.size, finished = true)
        n.id in uploading -> SyncSteps(SyncPhase.SENDING, n.samples.size, finished = false)
        else -> {
            val analysed = n.uploadState == UploadState.ANALYSED
            val failed = n.uploadError != null && n.uploadError != NightSync.ERR_NO_SESSION
            Text(
                when {
                    // "Momentos encontrados" over an empty list claimed what did not happen (25/09).
                    analysed && n.moments.isEmpty() -> stringResource(R.string.sync_server_none)
                    analysed -> stringResource(R.string.sync_server_moments)
                    n.uploadError == NightSync.ERR_NO_SESSION -> stringResource(R.string.sync_no_account)
                    n.uploadError == NightSync.ERR_EXPIRED -> stringResource(R.string.sync_failed_expired)
                    n.uploadError == NightSync.ERR_OFFLINE -> stringResource(R.string.sync_failed_offline)
                    n.uploadError != null -> stringResource(R.string.sync_failed_server, n.uploadError.orEmpty())
                    else -> stringResource(R.string.sync_local_pending)
                },
                style = TTType.MetaSmall,
                color = when {
                    analysed -> TT.Paper
                    failed -> TT.Rose
                    else -> TT.Gray55
                },
            )
            if (!analysed && n.uploadError != NightSync.ERR_NO_SESSION) {
                Text(
                    stringResource(R.string.sync_retry),
                    style = TTType.MetaSmall,
                    color = TT.Acid,
                    modifier = Modifier.clickable { sync.uploadLater(n.id) }.padding(vertical = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun SyncSteps(phase: SyncPhase, sampleCount: Int, finished: Boolean) {
    Column(Modifier.padding(bottom = 2.dp)) {
        SyncStep(
            stringResource(R.string.sync_step_send, Fmt.thousands(sampleCount)),
            done = finished || phase != SyncPhase.SENDING,
            active = !finished && phase == SyncPhase.SENDING,
        )
        SyncStep(
            stringResource(R.string.sync_step_analyse),
            done = finished,
            active = !finished && phase == SyncPhase.ANALYSING,
        )
    }
}

@Composable
private fun SyncStep(label: String, done: Boolean, active: Boolean) {
    Row(Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (active) TT.Acid else if (done) TT.Gray55 else TT.Ink700),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            if (done) "$label · ${stringResource(R.string.sync_step_done)}" else label,
            style = TTType.MetaSmall,
            color = if (active) TT.Acid else if (done) TT.Gray55 else TT.Gray45,
        )
    }
}

@Composable
private fun DividerDark() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(TT.Ink700))
}

/**
 * The night is not on this phone, or belongs to another account (25/09).
 * Said plainly, with the way home — never the blank screen a notification
 * for a deleted night used to open.
 */
@Composable
private fun NightNotHere(title: Int, body: Int, onHome: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(TT.Night)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 28.dp, end = 28.dp, top = 22.dp, bottom = 26.dp),
    ) {
        Text(stringResource(R.string.reveal_label), style = TTType.MetaWide, color = TT.Acid)
        Spacer(Modifier.weight(1f))
        Text(
            stringResource(title),
            style = TTType.ShoutSmall.copy(fontSize = 27.sp, lineHeight = 29.sp),
            color = TT.Paper,
        )
        Spacer(Modifier.height(14.dp))
        Text(stringResource(body), style = TTType.Body, color = TT.Gray45)
        Spacer(Modifier.weight(1f))
        TTButton(stringResource(R.string.reveal_home), TTButtonStyle.Rose, onClick = onHome)
    }
}

/**
 * A noite lacrada: nada de curva, nada de número. A memória da pessoa é colhida
 * no papel antes de qualquer dado; a revela abre sozinha às 10h.
 *
 * Its one clear action is **Voltar pro início**, in Pink (Felipe, 25/09): the
 * only way out used to be the small arrow, and the only button an export a
 * fan had no use for. The export is still here for the operator and on a
 * protocol phone, quiet, under the way home.
 */
@Composable
private fun LockedNightView(
    night: cc.tumtum.app.domain.Night,
    exporting: Boolean,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onExport: (() -> Unit)?,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(TT.Night)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 28.dp, end = 28.dp, top = 22.dp, bottom = 26.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.reveal_label), style = TTType.MetaWide, color = TT.Acid)
            BackArrow(onClick = onBack, onDark = true)
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(night.eventName, style = TTType.ItemSub.copy(fontSize = 14.sp), color = TT.Paper)
            Text(" · ${Fmt.date(night.date)}", style = TTType.ItemSub.copy(fontSize = 14.sp), color = TT.Gray55)
        }
        Spacer(Modifier.weight(1f))
        Text(
            stringResource(R.string.locked_title),
            style = TTType.ShoutSmall.copy(fontSize = 27.sp, lineHeight = 29.sp),
            color = TT.Paper,
        )
        Spacer(Modifier.height(14.dp))
        Text(
            stringResource(
                // The promise is made only when Android will let it be kept.
                if (NotificationManagerCompat.from(LocalContext.current).areNotificationsEnabled()) R.string.locked_body_notify else R.string.locked_body,
                night.revealAt?.let { revealWhen(it) }.orEmpty(),
            ),
            style = TTType.Body,
            color = TT.Gray45,
        )
        Spacer(Modifier.weight(1f))
        TTButton(stringResource(R.string.reveal_home), TTButtonStyle.Rose, onClick = onHome)
        if (onExport != null) {
            Spacer(Modifier.height(10.dp))
            TTButton(
                if (exporting) stringResource(R.string.export_running) else stringResource(R.string.locked_export),
                TTButtonStyle.OutlineOnDark,
                enabled = !exporting,
                onClick = onExport,
            )
        }
    }
}
