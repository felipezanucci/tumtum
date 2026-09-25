package cc.tumtum.app.ui.screens.live

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import cc.tumtum.app.R
import cc.tumtum.app.data.api.MarkKinds
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
import kotlinx.coroutines.launch
import cc.tumtum.app.data.repo.NightSync
import cc.tumtum.app.data.ble.SkinContact

/**
 * a2 — Captura ao vivo. Fundo #0A0A0A, estado calmo, quase sem UI.
 *
 * One screen, no scroll (21/09). This is used inside a show, in the dark,
 * by a thumb that has to find GOL and Encerrar without looking for them. On
 * the first phone the number fell under MARCAR O MOMENTO and the badges hid
 * behind a scroll nobody expected; the earlier fix (18/09) had split the
 * screen into a scrolling top and a fixed bottom, which only moved the
 * overflow. Now nothing scrolls: the three gaps between blocks share the
 * spare height, and on a short screen the two big numbers shrink instead of
 * pushing anything out.
 */
@Composable
fun CaptureScreen(nav: NavHostController) {
    val container = appContainer()
    val context = LocalContext.current
    val view = LocalView.current
    val vm: LiveViewModel = viewModel { LiveViewModel(container) }
    val event by vm.activeEvent.collectAsStateWithLifecycle()
    val snapshot by vm.snapshot.collectAsStateWithLifecycle()
    val now by vm.now.collectAsStateWithLifecycle()
    val ending by vm.ending.collectAsStateWithLifecycle()
    val revoked by vm.permissionRevoked.collectAsStateWithLifecycle()
    val bus by CaptureBus.status.collectAsStateWithLifecycle()
    val user by container.prefs.state.collectAsStateWithLifecycle(initialValue = null)
    val lastMark by vm.lastMark.collectAsStateWithLifecycle()

    val e = event ?: return
    val bleActive = bus.active && bus.eventId == e.id
    // What kind of night this is decides more than the mark buttons: a match
    // is not "tocando" and nobody at one is at a "show" (22/09).
    val sports = e.eventType == "sports"

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(TT.Night)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        // Two size tiers: a phone shorter than ~720dp of usable height gets
        // smaller numbers, never a scroll.
        val short = maxHeight < 720.dp
        val heroStyle = if (short) TTType.HeroLive.copy(fontSize = 104.sp, lineHeight = 84.sp) else TTType.HeroLive
        val clockStyle = if (short) {
            TTType.HeroSmall.copy(fontSize = 40.sp, lineHeight = 34.sp)
        } else {
            TTType.HeroSmall.copy(fontSize = 48.sp, lineHeight = 40.sp)
        }
        val gap = if (short) 10.dp else 16.dp

        Column(Modifier.fillMaxSize().padding(start = 28.dp, end = 28.dp, top = 18.dp, bottom = 22.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(9.dp).clip(CircleShape).background(TT.Acid))
                Spacer(Modifier.size(9.dp))
                Text(stringResource(R.string.live_label), style = TTType.MetaWide, color = TT.Acid)
                Spacer(Modifier.weight(1f))
                if (bleActive) {
                    val connLabel = when (bus.connection) {
                        is BleConnectionState.Connected ->
                            // Connected is not on the skin (25/09): the strap on a table
                            // is connected and measures nothing.
                            if (SkinContact.counts(bus.contactStatus)) {
                                "${(bus.deviceName ?: "SENSOR").uppercase()} · OK"
                            } else {
                                "${(bus.deviceName ?: "SENSOR").uppercase()} · ${stringResource(R.string.capture_no_contact)}"
                            }
                        is BleConnectionState.Reconnecting -> stringResource(R.string.capture_reconnecting).uppercase()
                        else -> stringResource(R.string.capture_disconnected).uppercase()
                    }
                    Text(
                        connLabel,
                        style = TTType.MetaSmall.copy(letterSpacing = 0.06.em),
                        color = if (bus.connection is BleConnectionState.Connected && SkinContact.counts(bus.contactStatus)) {
                            TT.Gray55
                        } else {
                            TT.Rose
                        },
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
            Spacer(Modifier.height(gap))
            Text(e.name, style = TTType.ItemTitle.copy(fontSize = 16.sp), color = TT.Paper, maxLines = 1)
            Spacer(Modifier.height(3.dp))
            Text(
                listOfNotNull(e.venue.ifBlank { null }, stringResource(R.string.live_started_at, Fmt.hour(e.startAt)))
                    .joinToString(" · "),
                style = TTType.BodySmall,
                color = TT.Gray45,
                maxLines = 1,
            )
            // An event the server never took is an event no fan can join, and
            // marks with nowhere to go. The app knew this on 22/09 and said it
            // on the tab that "Começa agora" leaves in the same instant, so
            // nobody read it. It belongs here, where the operator stays for the
            // next two hours — and it clears itself the moment the event
            // registers, because the row it reads is the live one.
            //
            // Since 25/09 it says why, and offers the one thing to do: on 25/09
            // it read "o servidor não aceitou" when the phone had in fact been
            // signed out, and nobody could tell which.
            if (e.serverEventId == null) {
                var why by remember(e.id) { mutableStateOf<String?>(null) }
                var trying by remember(e.id) { mutableStateOf(false) }
                val scope = rememberCoroutineScope()
                val signedIn = user?.session?.isLive(System.currentTimeMillis()) == true
                val reason = when {
                    !signedIn || why == NightSync.ERR_NO_SESSION -> stringResource(R.string.live_event_local_signed_out)
                    user?.isOperator != true || why == NightSync.ERR_NOT_OPERATOR ->
                        stringResource(R.string.live_event_local_not_operator)
                    why == null -> stringResource(R.string.live_event_local_unsent)
                    why == NightSync.ERR_OFFLINE -> stringResource(R.string.live_event_local_offline)
                    else -> stringResource(R.string.live_event_local_refused, why.orEmpty())
                }
                Spacer(Modifier.height(6.dp))
                Text(reason, style = TTType.BodySmall, color = TT.Acid, maxLines = 2)
                Text(
                    stringResource(if (trying) R.string.live_event_local_trying else R.string.live_event_local_retry),
                    style = TTType.MetaSmall,
                    color = TT.Paper,
                    modifier = Modifier
                        .clickable(enabled = !trying) {
                            trying = true
                            scope.launch {
                                why = container.sync.registerEventNow(e.id)
                                trying = false
                            }
                        }
                        .padding(vertical = 4.dp),
                )
            }

            if (revoked && !bleActive) {
                // §7 — permissão revogada: nova captura bloqueada, com explicação honesta.
                Spacer(Modifier.height(40.dp))
                Text(stringResource(R.string.live_blocked_title), style = TTType.TitleSmall, color = TT.Paper)
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.live_blocked_body), style = TTType.Body, color = TT.Gray45)
                Spacer(Modifier.height(24.dp))
                TTButton(
                    stringResource(R.string.live_blocked_cta),
                    TTButtonStyle.OutlineOnDark,
                    onClick = { nav.navigate(Routes.Permission) },
                )
                Spacer(Modifier.weight(1f))
            } else {
                Spacer(Modifier.weight(1f))
                Text(
                    stringResource(if (sports) R.string.live_playing_for_match else R.string.live_playing_for),
                    style = TTType.MetaWide,
                    color = TT.Gray55,
                )
                Spacer(Modifier.height(6.dp))
                Text(Fmt.stopwatch(Duration.between(e.startAt, now)), style = clockStyle, color = TT.Paper)
                Spacer(Modifier.weight(1f))
                // O número fica na tela (decisão de Felipe, 18/09, depois de dois
                // ensaios pedindo por ele). A regra §10 — ver o número muda o número —
                // fica registrada no log; a captura não muda por ele estar visível.
                val bpmNow = if (bleActive) {
                    bus.lastBpm?.takeIf { SkinContact.counts(bus.contactStatus) }
                } else {
                    snapshot?.currentBpm
                }
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(bpmNow?.toString() ?: "—", style = heroStyle, color = TT.Rose)
                    Spacer(Modifier.size(14.dp))
                    Text(
                        stringResource(R.string.live_bpm_now),
                        style = TTType.Body.copy(fontSize = 17.sp),
                        color = TT.Gray45,
                        modifier = Modifier.padding(bottom = 10.dp),
                    )
                }
                Spacer(Modifier.height(gap))
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
                Spacer(Modifier.weight(1f))
            }

            // Etapa 3 — marcar o momento. Only on the operator's phone (Configurações →
            // OPERADOR): three taps a person can find in the dark, the clock of the
            // tap is what names the moment later, for everyone on the same event.
            // Every tap is answered three ways — the button lights (rose for a
            // mark stored, acid for one already there), the phone buzzes (one
            // pattern each), and a line says what happened. On 21/09 a repeat
            // inside ten seconds changed only the line, and read as nothing.
            if (user?.marksOn == true) {
                val marks by container.nights.marksCount(e.id).collectAsStateWithLifecycle(initialValue = 0)
                val fb = lastMark
                var litTick by remember { mutableLongStateOf(-1L) }
                LaunchedEffect(fb?.tick) {
                    val f = fb ?: return@LaunchedEffect
                    litTick = f.tick
                    view.performHapticFeedback(
                        if (Build.VERSION.SDK_INT >= 30) {
                            if (f.repeated) HapticFeedbackConstants.REJECT else HapticFeedbackConstants.CONFIRM
                        } else {
                            HapticFeedbackConstants.LONG_PRESS
                        },
                    )
                    delay(1_200)
                    litTick = -1L
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.mark_label), style = TTType.MetaWide, color = TT.Gray55)
                    Text(
                        pluralStringResource(R.plurals.mark_count, marks, marks),
                        style = TTType.MetaSmall,
                        color = TT.Gray55,
                    )
                }
                Spacer(Modifier.height(10.dp))
                // What there is to mark depends on the night. A match (22/09)
                // gets its two anchors first — the whistle and the second
                // half, the only two instants everyone in the stadium knows
                // exactly — because they are what turn every API-Football
                // minute into a wall-clock time for everyone at the game. A
                // marked anchor shows its own clock on the button: the state
                // is said where the thumb already is.
                if (sports) {
                    val anchors by container.nights.anchors(e.id).collectAsStateWithLifecycle(initialValue = emptyMap())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            stringResource(R.string.mark_kickoff) to MarkKinds.KICKOFF,
                            stringResource(R.string.mark_second_half) to MarkKinds.SECOND_HALF,
                        ).forEach { (label, kind) ->
                            val at = anchors[kind]
                            MarkButton(
                                label = if (at != null) "$label · ${Fmt.hour(at)}" else label,
                                tapLabel = label,
                                kind = kind,
                                idle = if (at != null) TTButtonStyle.OutlineOnDark else TTButtonStyle.OutlineAcid,
                                fb = fb,
                                litTick = litTick,
                                onMark = vm::mark,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val kinds = if (sports) {
                        listOf(
                            stringResource(R.string.mark_goal) to MarkKinds.GOAL,
                            stringResource(R.string.mark_moment) to MarkKinds.MOMENT,
                        )
                    } else {
                        listOf(
                            stringResource(R.string.mark_song) to MarkKinds.SONG,
                            stringResource(R.string.mark_moment) to MarkKinds.MOMENT,
                        )
                    }
                    kinds.forEach { (label, kind) ->
                        MarkButton(
                            label = label,
                            tapLabel = label,
                            kind = kind,
                            idle = TTButtonStyle.OutlineOnDark,
                            fb = fb,
                            litTick = litTick,
                            onMark = vm::mark,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                // The line keeps its height with or without a mark, so the
                // buttons never jump under the thumb.
                Row(Modifier.fillMaxWidth().height(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (fb != null) {
                        Text(
                            stringResource(
                                when {
                                    fb.repeated && MarkKinds.isAnchor(fb.kind) -> R.string.mark_anchor_repeated
                                    fb.repeated -> R.string.mark_repeated
                                    else -> R.string.mark_stored
                                },
                                fb.label,
                                Fmt.hour(fb.at),
                            ),
                            style = TTType.BodySmall,
                            color = if (fb.repeated) TT.Acid else TT.Paper,
                            maxLines = 1,
                            modifier = Modifier.weight(1f),
                        )
                        if (!fb.repeated && fb.id != null) {
                            Spacer(Modifier.size(12.dp))
                            Text(
                                stringResource(R.string.mark_undo),
                                style = TTType.Meta,
                                color = TT.Rose,
                                modifier = Modifier.clickable { vm.undoLastMark() },
                            )
                        }
                    }
                }
                Spacer(Modifier.height(gap))
            }
            Text(
                stringResource(if (sports) R.string.live_hint_match else R.string.live_hint),
                style = TTType.Body.copy(fontSize = 14.sp),
                color = TT.Gray55,
                maxLines = 1,
            )
            Spacer(Modifier.height(12.dp))
            TTButton(
                stringResource(R.string.live_end),
                TTButtonStyle.Acid,
                enabled = !ending,
                onClick = {
                    // Encerra o serviço (grava offsets finais e limpa a sessão ativa) e mede as fontes.
                    CaptureService.stop(context)
                    vm.endNight(
                        onSaved = { nightId ->
                            nav.navigate(Routes.reveal(nightId)) {
                                popUpTo(Routes.Feed)
                            }
                        },
                        onChoose = {
                            nav.navigate(Routes.EndNight) {
                                popUpTo(Routes.Live) { inclusive = false }
                            }
                        },
                    )
                },
            )
        }
    }
}

/**
 * One mark button, answered three ways on a tap: it lights rose for a mark
 * stored, acid for one already there (see [LiveViewModel.mark]), and goes back
 * to [idle] after the haptic. [tapLabel] is what the mark is called on the
 * timeline; [label] is what the button shows, which for a marked anchor also
 * carries the clock of its tap.
 */
@Composable
private fun MarkButton(
    label: String,
    tapLabel: String,
    kind: String,
    idle: TTButtonStyle,
    fb: LiveViewModel.MarkFeedback?,
    litTick: Long,
    onMark: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lit = fb != null && fb.label == tapLabel && litTick == fb.tick
    TTButton(
        label,
        when {
            lit && fb!!.repeated -> TTButtonStyle.Acid
            lit -> TTButtonStyle.Rose
            else -> idle
        },
        onClick = { onMark(tapLabel, kind) },
        modifier = modifier,
    )
}
