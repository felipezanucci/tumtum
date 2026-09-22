package cc.tumtum.app.ui.screens.live

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import cc.tumtum.app.R
import cc.tumtum.app.data.api.ServerEvent
import cc.tumtum.app.data.api.ServerEvents
import cc.tumtum.app.data.prefs.UpcomingEvent
import cc.tumtum.app.data.repo.registerEvent
import cc.tumtum.app.domain.EventTimes
import cc.tumtum.app.domain.NewEvent
import cc.tumtum.app.domain.Skin
import cc.tumtum.app.service.BatteryExemption
import cc.tumtum.app.service.CaptureService
import cc.tumtum.app.service.Reminders
import cc.tumtum.app.ui.Fmt
import cc.tumtum.app.ui.components.Badge
import cc.tumtum.app.ui.components.OutlineBadge
import cc.tumtum.app.ui.components.TTButton
import cc.tumtum.app.ui.components.TTButtonStyle
import cc.tumtum.app.ui.components.TTField
import cc.tumtum.app.ui.components.TribeChip
import cc.tumtum.app.ui.components.WheelDateField
import cc.tumtum.app.ui.components.WheelTimeField
import cc.tumtum.app.ui.components.Wordmark
import cc.tumtum.app.ui.nav.Routes
import cc.tumtum.app.ui.nav.appContainer
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.launch

/**
 * Aba AO VIVO: com evento ativo vai direto para a captura (a2, tela cheia);
 * sem evento, é o lugar do próximo evento marcado (§5.7) e, desde 21/09, da
 * lista dos eventos da TumTum. A regra do fundador: **o evento é da TumTum, o
 * fã nunca cria um.** O fã vê o que a TumTum cadastrou e ativa com um toque:
 * o que vem vira o PRÓXIMO (com o lembrete uma hora antes), o que está
 * rolando começa a captura. Nome, lugar, tipo, data e hora nunca são
 * perguntados a um fã.
 *
 * Quem cadastra é o operador, pelos dois atalhos que a chave "Cadastrar
 * eventos pelo celular" (Configurações → OPERADOR) acende no fim desta aba.
 * Data e horas são rodas, nunca texto.
 */
@Composable
fun LiveTabScreen(nav: NavHostController) {
    val container = appContainer()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val vm: LiveViewModel = viewModel { LiveViewModel(container) }
    val activeEvent by vm.activeEvent.collectAsStateWithLifecycle()
    val now by vm.now.collectAsStateWithLifecycle()
    val user by container.prefs.state.collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(activeEvent) {
        if (activeEvent != null) {
            nav.navigate(Routes.Capture) { launchSingleTop = true }
        }
    }

    val state = user ?: return
    var sheetMode by remember { mutableStateOf<EventSheetMode?>(null) }
    var showBatteryGate by remember { mutableStateOf(false) }
    var pendingStart by remember { mutableStateOf<StartRequest?>(null) }
    // One line under the list for what a tap could not do, and why.
    var notice by remember { mutableStateOf<String?>(null) }
    // Re-read after the permission dialog closes, so the line under the card tells the truth.
    var notifTick by remember { mutableIntStateOf(0) }
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { notifTick++ }
    fun askNotifications() {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    val notificationsOk = remember(notifTick) { NotificationManagerCompat.from(context).areNotificationsEnabled() }

    // The battery exemption, read again every time the tab comes back: the
    // person may have changed it in Android's own settings. Said on the tab
    // (21/09) because the gate that asks for it is skipped, silently and
    // correctly, once it is granted — and a skipped step nobody announced
    // reads as a step the app forgot.
    var resumeTick by remember { mutableIntStateOf(0) }
    LifecycleResumeEffect(Unit) {
        resumeTick++
        onPauseOrDispose { }
    }
    val batteryExempt = remember(resumeTick) { BatteryExemption.isExempt(context) }

    // The server's events: asked for on every visit, and again on "Tentar de
    // novo". "Could not ask" is told apart from "nothing there".
    var serverEvents by remember { mutableStateOf<List<ServerEvent>?>(null) }
    var listFailed by remember { mutableStateOf(false) }
    var fetchTick by remember { mutableIntStateOf(0) }
    LaunchedEffect(fetchTick) {
        listFailed = false
        runCatching { container.api.listEvents() }
            .onSuccess { serverEvents = it }
            .onFailure { listFailed = true }
    }

    // Starting a capture on an event — the fan's "Começar agora" and the
    // operator's "Começa agora" take the same path: battery gate, then the
    // service. Only the marked event's own start clears the mark.
    fun startCapture(request: StartRequest) {
        val paired = state.sensorPaired
        val address = state.bleAddress
        if (paired && !BatteryExemption.isExempt(context)) {
            pendingStart = request
            showBatteryGate = true
            return
        }
        val up = request.event
        scope.launch {
            val eventId = container.nights.startEvent(up.name, up.venue, up.eventType, up.serverEventId)
            if (paired && address != null) {
                container.prefs.setActiveCapture(eventId)
                CaptureService.start(context, eventId, address)
            }
            if (request.clearsMark) {
                Reminders.cancelEvent(context)
                container.prefs.clearUpcoming()
            }
        }
    }

    fun markUpcoming(next: UpcomingEvent) {
        scope.launch {
            container.prefs.setUpcoming(next)
            Reminders.scheduleEvent(
                context, next.startAt,
                context.getString(R.string.remind_event_title, next.name),
                context.getString(R.string.remind_event_text),
            )
        }
        askNotifications()
    }

    /** The fan's one gesture: an event going on starts the capture, one still to come is marked. */
    fun activate(ev: ServerEvent) {
        notice = null
        val startAt = ev.startAt ?: return
        val up = UpcomingEvent(ev.name, ev.venue.orEmpty(), ev.eventType, startAt, ev.id)
        if (ev.isLiveAt(now)) {
            startCapture(StartRequest(up, clearsMark = state.upcoming?.serverEventId == ev.id))
        } else {
            markUpcoming(up)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(TT.Paper)
            .statusBarsPadding(),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Wordmark(width = 92.dp, modifier = Modifier.clickable { nav.navigate(Routes.Feed) { launchSingleTop = true } })
            cc.tumtum.app.ui.components.UserAvatar(
                state.account?.initials ?: "TT",
                Skin.BLACK,
                photoPath = state.avatarPath,
                modifier = Modifier.clickable {
                    state.account?.let { nav.navigate(Routes.profile(it.username)) }
                },
            )
        }

        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 24.dp),
        ) {
            val up = state.upcoming
            if (up == null) {
                // a5 — Vazio: o coração de folga, e a lista logo abaixo.
                Text(
                    stringResource(R.string.empty_title),
                    style = TTType.Shout.copy(fontSize = 34.sp, lineHeight = 35.sp),
                    color = TT.Ink,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    stringResource(R.string.empty_subtitle),
                    style = TTType.Body.copy(fontSize = 19.sp),
                    color = TT.Gray45,
                )
                Spacer(Modifier.height(28.dp))
            } else {
                UpcomingCard(
                    up = up,
                    now = now,
                    notificationsOk = notificationsOk,
                    onStart = { startCapture(StartRequest(up, clearsMark = true)) },
                    onUnmark = {
                        scope.launch {
                            Reminders.cancelEvent(context)
                            container.prefs.clearUpcoming()
                        }
                    },
                    onAllowNotifications = { askNotifications() },
                )
                Spacer(Modifier.height(24.dp))
            }

            StatusRow(
                ok = state.watchConnected,
                text = stringResource(if (state.watchConnected) R.string.empty_watch_ok else R.string.empty_no_watch),
            )
            if (state.sensorPaired) {
                Spacer(Modifier.height(10.dp))
                StatusRow(ok = true, text = stringResource(R.string.empty_sensor_ok, state.bleName ?: ""))
                Spacer(Modifier.height(10.dp))
                StatusRow(
                    ok = batteryExempt,
                    text = stringResource(if (batteryExempt) R.string.battery_row_ok else R.string.battery_row_pending),
                )
            }
            Spacer(Modifier.height(14.dp))
            if (!state.watchConnected && !state.sensorPaired) {
                TTButton(
                    stringResource(R.string.empty_connect),
                    TTButtonStyle.Rose,
                    onClick = { nav.navigate(Routes.Permission) },
                )
                Spacer(Modifier.height(24.dp))
            } else {
                Spacer(Modifier.height(14.dp))
            }

            // The events TumTum registered. An empty state is a claim: "none
            // yet" and "could not ask" are different sentences.
            Text(stringResource(R.string.events_section), style = TTType.Meta, color = TT.Gray70)
            Spacer(Modifier.height(10.dp))
            val events = serverEvents
            when {
                events == null && !listFailed -> {
                    Text(stringResource(R.string.events_loading), style = TTType.Footnote, color = TT.Gray45)
                }
                else -> {
                    if (listFailed) {
                        Text(stringResource(R.string.events_failed), style = TTType.BodySmall, color = TT.Ink)
                        Text(
                            stringResource(R.string.events_retry),
                            style = TTType.Meta,
                            color = TT.Ink,
                            modifier = Modifier.clickable { fetchTick++ }.padding(vertical = 8.dp),
                        )
                        Spacer(Modifier.height(6.dp))
                    }
                    val fan = ServerEvents.forFan(events.orEmpty(), now)
                    if (!listFailed && fan.isEmpty()) {
                        Text(stringResource(R.string.events_none), style = TTType.Footnote, color = TT.Gray45)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        fan.forEach { ev ->
                            EventRow(ev, now, marked = up?.serverEventId == ev.id) { activate(ev) }
                        }
                    }
                }
            }
            notice?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, style = TTType.BodySmall, color = TT.Ink)
            }

            // The operator's door, open only on the operator's phone.
            if (state.operatorEvents) {
                Spacer(Modifier.height(32.dp))
                Text(stringResource(R.string.settings_operator_section), style = TTType.Meta, color = TT.Gray70)
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.operator_events_hint), style = TTType.Footnote, color = TT.Gray45)
                Spacer(Modifier.height(12.dp))
                TTButton(stringResource(R.string.event_starts_now), TTButtonStyle.Outline, onClick = { sheetMode = EventSheetMode.Now })
                Spacer(Modifier.height(8.dp))
                TTButton(stringResource(R.string.upcoming_mark), TTButtonStyle.Outline, onClick = { sheetMode = EventSheetMode.Upcoming })
            }
        }
    }

    sheetMode?.let { mode ->
        CreateEventSheet(
            mode = mode,
            onDismiss = { sheetMode = null },
            onCreate = { spec ->
                sheetMode = null
                scope.launch {
                    // To the server first, so every fan's list has it; then this phone.
                    val registered = container.registerEvent(spec)
                    registered.error?.let { notice = context.getString(R.string.event_server_failed, it) }
                    val event = spec.copy(serverEventId = registered.serverEventId)
                    when (mode) {
                        EventSheetMode.Now -> startCapture(
                            StartRequest(
                                UpcomingEvent(event.name, event.venue, event.eventType, Instant.now(), event.serverEventId),
                                clearsMark = false,
                            ),
                        )
                        EventSheetMode.Upcoming -> event.startAt?.let { startAt ->
                            markUpcoming(UpcomingEvent(event.name, event.venue, event.eventType, startAt, event.serverEventId))
                        }
                    }
                    fetchTick++
                }
            },
        )
    }
    if (showBatteryGate) {
        BatteryExemptionSheet(
            onDismiss = {
                showBatteryGate = false
                pendingStart = null
            },
            onExempt = {
                showBatteryGate = false
                pendingStart?.let { startCapture(it) }
                pendingStart = null
            },
        )
    }
}

/** A capture about to start, and whether it is the marked event's own start. */
private data class StartRequest(val event: UpcomingEvent, val clearsMark: Boolean)

/** One line of state with a dot: acid when the thing is in place, grey when it is not. */
@Composable
private fun StatusRow(ok: Boolean, text: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, TT.Gray10, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(9.dp).clip(CircleShape).background(if (ok) TT.Acid else TT.Gray25))
        Text(text, style = TTType.BodySmall, color = TT.Gray70)
    }
}

/**
 * One of TumTum's events, as a fan sees it: when, what, where, and what a tap
 * does — AGORA for one going on, MARCAR for one still to come, MARCADO for
 * the one already chosen.
 */
@Composable
private fun EventRow(ev: ServerEvent, now: Instant, marked: Boolean, onClick: () -> Unit) {
    val startAt = ev.startAt ?: return
    val shape = RoundedCornerShape(12.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, if (marked) TT.Ink else TT.Gray10, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text("${Fmt.date(startAt)} · ${Fmt.hour(startAt)}", style = TTType.MetaSmall, color = TT.Gray55)
            Spacer(Modifier.height(2.dp))
            Text(ev.name, style = TTType.ItemTitle.copy(fontSize = 15.sp), color = TT.Ink, maxLines = 1)
            ev.venue?.let { Text(it, style = TTType.BodySmall, color = TT.Gray45, maxLines = 1) }
        }
        when {
            ev.isLiveAt(now) -> Badge(stringResource(R.string.events_badge_now))
            marked -> Badge(stringResource(R.string.events_badge_marked))
            else -> OutlineBadge(stringResource(R.string.events_badge_mark), borderColor = TT.Gray25, contentColor = TT.Ink)
        }
    }
}

/** The next marked event: what, when, how far, and the one reminder the product allows itself. */
@Composable
private fun UpcomingCard(
    up: UpcomingEvent,
    now: Instant,
    notificationsOk: Boolean,
    onStart: () -> Unit,
    onUnmark: () -> Unit,
    onAllowNotifications: () -> Unit,
) {
    val left = Duration.between(now, up.startAt)
    val countdown = when {
        left.isNegative -> stringResource(R.string.upcoming_started)
        left.toDays() >= 2 -> stringResource(R.string.upcoming_in_days, left.toDays().toInt())
        left.toDays() == 1L -> stringResource(R.string.upcoming_tomorrow)
        left.toHours() >= 1 -> stringResource(R.string.upcoming_in_hours, left.toHours().toInt())
        else -> stringResource(R.string.upcoming_in_minutes, left.toMinutes().toInt().coerceAtLeast(1))
    }
    val shape = RoundedCornerShape(12.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, TT.Ink, shape)
            .padding(16.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.upcoming_label), style = TTType.MetaSmall, color = TT.Gray55)
            Badge(countdown)
        }
        Spacer(Modifier.height(8.dp))
        Text(up.name, style = TTType.ItemTitle, color = TT.Ink)
        val where = if (up.venue.isNotBlank()) " · ${up.venue}" else ""
        Text("${Fmt.date(up.startAt)} · ${Fmt.hour(up.startAt)}$where", style = TTType.BodySmall, color = TT.Gray45)
        Spacer(Modifier.height(8.dp))
        if (notificationsOk) {
            Text(stringResource(R.string.upcoming_reminder_ok), style = TTType.Footnote, color = TT.Gray45)
        } else {
            Text(stringResource(R.string.upcoming_reminder_blocked), style = TTType.Footnote, color = TT.Gray45)
            Text(
                stringResource(R.string.upcoming_reminder_allow),
                style = TTType.Meta,
                color = TT.Ink,
                modifier = Modifier.clickable(onClick = onAllowNotifications).padding(vertical = 6.dp),
            )
        }
        Spacer(Modifier.height(12.dp))
        TTButton(stringResource(R.string.upcoming_start), TTButtonStyle.Rose, onClick = onStart)
        Text(
            stringResource(R.string.upcoming_unmark),
            style = TTType.Meta,
            color = TT.Gray45,
            modifier = Modifier.clickable(onClick = onUnmark).padding(top = 10.dp, bottom = 4.dp),
        )
    }
}

/** Which question the operator's sheet asks: an event starting now, or one still to come. */
enum class EventSheetMode { Now, Upcoming }

/**
 * A folha do operador (21/09): como a TumTum cadastra um evento pelo celular.
 * Superfície de operador, nunca de fã — o fã escolhe da lista em AO VIVO.
 *
 * Since 2026-09-18 (Etapa 3) it also offers the server's events nearest to
 * today: picking one attaches the night to the timeline the pilot shares —
 * the thing that names a goal for everyone in the stadium — instead of a
 * private event with the same name. Typing a name still works; the kind
 * decides which timeline entries the server will accept.
 *
 * Two modes: [EventSheetMode.Now] ("Começa agora", for something starting as
 * it is registered) and [EventSheetMode.Upcoming] (date, start and end — the
 * calendar as the trigger). "Trazer uma noite que já passou" was the third
 * and was cut on 22/09. Date and times are wheels, never text; what a rolled
 * time can still get wrong lives in [EventTimes].
 *
 * The end matters as much as the start: it is the window a fan's AGORA badge
 * lives in, and without one the app calls a two-hour match live for five
 * hours — the same class of false claim as the rest.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventSheet(
    onDismiss: () -> Unit,
    onCreate: (NewEvent) -> Unit,
    mode: EventSheetMode = EventSheetMode.Now,
) {
    val container = appContainer()
    var name by remember { mutableStateOf("") }
    var venue by remember { mutableStateOf("") }
    var eventType by remember { mutableStateOf("concert") }
    var picked by remember { mutableStateOf<ServerEvent?>(null) }
    var serverEvents by remember { mutableStateOf<List<ServerEvent>?>(null) }
    var listFailed by remember { mutableStateOf(false) }
    var day by remember { mutableStateOf(EventTimes.today(Instant.now())) }
    var start by remember { mutableStateOf(LocalTime.of(21, 0)) }
    var end by remember { mutableStateOf(LocalTime.of(23, 30)) }
    var timeError by remember { mutableStateOf<EventTimes.Reason?>(null) }

    // The list is asked for once, when the sheet opens. "Could not ask" is
    // told apart from "nothing there": an empty state is a claim.
    LaunchedEffect(Unit) {
        runCatching { container.api.listEvents() }
            .onSuccess { serverEvents = ServerEvents.nearest(it, LocalDate.now()) }
            .onFailure { listFailed = true; serverEvents = emptyList() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = TT.Paper,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 28.dp, end = 28.dp, bottom = 40.dp),
        ) {
            Text(
                stringResource(
                    when (mode) {
                        EventSheetMode.Now -> R.string.event_new_title
                        EventSheetMode.Upcoming -> R.string.upcoming_mark
                    },
                ),
                style = TTType.TitleSmall,
                color = TT.Ink,
            )
            Spacer(Modifier.height(18.dp))
            Text(stringResource(R.string.event_pick_title), style = TTType.Meta, color = TT.Gray70)
            Spacer(Modifier.height(8.dp))
            when {
                serverEvents == null -> Text(stringResource(R.string.event_pick_loading), style = TTType.Footnote, color = TT.Gray45)
                listFailed -> Text(stringResource(R.string.event_pick_offline), style = TTType.Footnote, color = TT.Gray45)
                serverEvents!!.isEmpty() -> Text(stringResource(R.string.event_pick_none), style = TTType.Footnote, color = TT.Gray45)
                else -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    serverEvents!!.forEach { ev ->
                        val selected = picked?.id == ev.id
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, if (selected) TT.Ink else TT.Gray10, RoundedCornerShape(10.dp))
                                .clickable {
                                    picked = if (selected) null else ev
                                    if (!selected) {
                                        name = ev.name
                                        venue = ev.venue.orEmpty()
                                        eventType = ev.eventType
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(Modifier.size(9.dp).clip(CircleShape).background(if (selected) TT.Acid else TT.Gray25))
                            Text(ev.label, style = TTType.BodySmall, color = TT.Ink, modifier = Modifier.weight(1f))
                            ev.venue?.let { Text(it, style = TTType.MetaSmall, color = TT.Gray45) }
                        }
                    }
                }
            }
            Spacer(Modifier.height(22.dp))
            Text(stringResource(R.string.event_or_type), style = TTType.Meta, color = TT.Gray70)
            Spacer(Modifier.height(8.dp))
            TTField(
                stringResource(R.string.event_name_label),
                name,
                { name = it; picked = null },
                placeholder = stringResource(R.string.event_name_hint),
            )
            Spacer(Modifier.height(12.dp))
            TTField(
                stringResource(R.string.event_venue_label),
                venue,
                { venue = it; picked = null },
                placeholder = stringResource(R.string.event_venue_hint),
            )
            Spacer(Modifier.height(14.dp))
            Text(stringResource(R.string.event_type_label), style = TTType.Meta, color = TT.Gray70)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "concert" to stringResource(R.string.event_type_concert),
                    "sports" to stringResource(R.string.event_type_sports),
                    "festival" to stringResource(R.string.event_type_festival),
                ).forEach { (kind, label) ->
                    TribeChip(label, selected = eventType == kind, onToggle = { eventType = kind; picked = null })
                }
            }
            if (mode != EventSheetMode.Now) {
                Spacer(Modifier.height(14.dp))
                WheelDateField(stringResource(R.string.event_date_label), day, { day = it; timeError = null })
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    WheelTimeField(
                        stringResource(R.string.event_start_label),
                        start,
                        { start = it; timeError = null },
                        modifier = Modifier.weight(1f),
                    )
                    WheelTimeField(
                        stringResource(R.string.event_end_label),
                        end,
                        { end = it; timeError = null },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Spacer(Modifier.height(26.dp))
            // Sempre rosa: um botão apagado a 40% lê como "nada para fazer". Sem
            // nome, o toque diz o que falta em vez de não responder.
            var needsName by remember { mutableStateOf(false) }
            if (needsName && name.isBlank()) {
                Text(stringResource(R.string.event_needs_name), style = TTType.BodySmall, color = TT.Ink)
                Spacer(Modifier.height(10.dp))
            }
            timeError?.let { reason ->
                Text(
                    stringResource(
                        when (reason) {
                            EventTimes.Reason.IN_PAST -> R.string.event_in_past
                            EventTimes.Reason.TOO_LONG -> R.string.event_too_long
                        },
                    ),
                    style = TTType.BodySmall,
                    color = TT.Ink,
                )
                Spacer(Modifier.height(10.dp))
            }
            TTButton(
                stringResource(
                    when (mode) {
                        EventSheetMode.Now -> R.string.event_starts_now
                        EventSheetMode.Upcoming -> R.string.event_mark
                    },
                ),
                TTButtonStyle.Rose,
                onClick = {
                    if (name.isBlank()) {
                        needsName = true
                        return@TTButton
                    }
                    val times: EventTimes.Result? = when (mode) {
                        EventSheetMode.Now -> null
                        EventSheetMode.Upcoming -> EventTimes.upcoming(day, start, end, Instant.now())
                    }
                    when (times) {
                        null -> onCreate(NewEvent(name = name, venue = venue, eventType = eventType, serverEventId = picked?.id))
                        is EventTimes.Result.Error -> timeError = times.reason
                        is EventTimes.Result.Ok -> onCreate(
                            NewEvent(
                                name = name, venue = venue, eventType = eventType, serverEventId = picked?.id,
                                startAt = times.startAt, endAt = times.endAt,
                            ),
                        )
                    }
                },
            )
        }
    }
}
