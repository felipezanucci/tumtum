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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import cc.tumtum.app.R
import cc.tumtum.app.data.api.ServerEvent
import cc.tumtum.app.data.api.ServerEvents
import cc.tumtum.app.data.prefs.UpcomingEvent
import cc.tumtum.app.domain.EventTimes
import cc.tumtum.app.domain.Skin
import cc.tumtum.app.service.BatteryExemption
import cc.tumtum.app.service.CaptureService
import cc.tumtum.app.service.Reminders
import cc.tumtum.app.ui.Fmt
import cc.tumtum.app.ui.components.Badge
import cc.tumtum.app.ui.components.TTButton
import cc.tumtum.app.ui.components.TTButtonStyle
import cc.tumtum.app.ui.components.TTField
import cc.tumtum.app.ui.components.TribeChip
import cc.tumtum.app.ui.components.Wordmark
import cc.tumtum.app.ui.nav.Routes
import cc.tumtum.app.ui.nav.appContainer
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.launch

/**
 * Aba AO VIVO: com evento ativo vai direto para a captura (a2, tela cheia);
 * sem evento, é o estado de espera/vazio (a5) — e, desde 20/09, o lugar do
 * próximo evento marcado: o calendário é o gatilho (pesquisa de 19/09, §5.7).
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
    var showMarkSheet by remember { mutableStateOf(false) }
    var showPastSheet by remember { mutableStateOf(false) }
    var showBatteryGate by remember { mutableStateOf(false) }
    var pendingStart by remember { mutableStateOf<UpcomingEvent?>(null) }
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

    // "Começar agora" on the marked event: the same path the operator's
    // "Marcar evento" takes (battery gate, capture service), then the mark
    // and its reminder are cleared — the night has begun.
    fun startUpcoming(up: UpcomingEvent) {
        val paired = state.sensorPaired
        val address = state.bleAddress
        if (paired && !BatteryExemption.isExempt(context)) {
            pendingStart = up
            showBatteryGate = true
            return
        }
        scope.launch {
            val eventId = container.nights.startEvent(up.name, up.venue, up.eventType, up.serverEventId)
            if (paired && address != null) {
                container.prefs.setActiveCapture(eventId)
                CaptureService.start(context, eventId, address)
            }
            Reminders.cancelEvent(context)
            container.prefs.clearUpcoming()
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

        // a5 — Vazio: convida a marcar o próximo evento, não a comprar nada.
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            val up = state.upcoming
            if (up == null) {
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
                Spacer(Modifier.height(36.dp))
            } else {
                UpcomingCard(
                    up = up,
                    now = now,
                    notificationsOk = notificationsOk,
                    onStart = { startUpcoming(up) },
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
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, TT.Gray10, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(if (state.watchConnected) TT.Acid else TT.Gray25),
                )
                Text(
                    stringResource(if (state.watchConnected) R.string.empty_watch_ok else R.string.empty_no_watch),
                    style = TTType.BodySmall,
                    color = TT.Gray70,
                )
            }
            if (state.sensorPaired) {
                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, TT.Gray10, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(9.dp).clip(CircleShape).background(TT.Acid))
                    Text(
                        stringResource(R.string.empty_sensor_ok, state.bleName ?: ""),
                        style = TTType.BodySmall,
                        color = TT.Gray70,
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            if (!state.watchConnected && !state.sensorPaired) {
                TTButton(
                    stringResource(R.string.empty_connect),
                    TTButtonStyle.Rose,
                    onClick = { nav.navigate(Routes.Permission) },
                )
                Spacer(Modifier.height(10.dp))
            }
            if (up == null) {
                TTButton(
                    stringResource(R.string.upcoming_mark),
                    if (state.watchConnected || state.sensorPaired) TTButtonStyle.Rose else TTButtonStyle.Outline,
                    onClick = { showMarkSheet = true },
                )
                Spacer(Modifier.height(10.dp))
            }
            // §5.1 — the first night must not be weeks away: a night the watch
            // already recorded, brought in from its own history.
            if (state.watchConnected) {
                TTButton(
                    stringResource(R.string.past_bring),
                    TTButtonStyle.Outline,
                    onClick = { showPastSheet = true },
                )
            }
        }
    }

    if (showMarkSheet) {
        CreateEventSheet(
            mode = EventSheetMode.Upcoming,
            onDismiss = { showMarkSheet = false },
            onCreate = { spec ->
                showMarkSheet = false
                val startAt = spec.startAt ?: return@CreateEventSheet
                val next = UpcomingEvent(spec.name, spec.venue, spec.eventType, startAt, spec.serverEventId)
                scope.launch {
                    container.prefs.setUpcoming(next)
                    Reminders.scheduleEvent(
                        context, startAt,
                        context.getString(R.string.remind_event_title, next.name),
                        context.getString(R.string.remind_event_text),
                    )
                }
                askNotifications()
            },
        )
    }
    if (showPastSheet) {
        CreateEventSheet(
            mode = EventSheetMode.Past,
            onDismiss = { showPastSheet = false },
            onCreate = { spec ->
                showPastSheet = false
                val startAt = spec.startAt ?: return@CreateEventSheet
                val endAt = spec.endAt ?: return@CreateEventSheet
                scope.launch {
                    // The event is created already closed; the watch is asked
                    // over that window; the usual chooser and reveal follow.
                    val event = container.nights.createPastEvent(spec.name, spec.venue, spec.eventType, spec.serverEventId, startAt, endAt)
                    container.endNight.event = event
                    container.endNight.measurement = container.nights.measureSources(event, endAt)
                    nav.navigate(Routes.EndNight)
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
                pendingStart?.let { startUpcoming(it) }
                pendingStart = null
            },
        )
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

/** What the sheet hands back: an event to start, typed or picked from the server's list (Etapa 3). */
data class NewEvent(
    val name: String,
    val venue: String,
    val eventType: String = "concert",
    val serverEventId: String? = null,
    /** Set by the Upcoming and Past modes; null means "now". */
    val startAt: Instant? = null,
    /** Set by the Past mode only. */
    val endAt: Instant? = null,
)

/** Which question the sheet asks: an event starting now, the next one, or a night that already happened. */
enum class EventSheetMode { Now, Upcoming, Past }

/**
 * Sheet mínima para marcar o evento — a janela de leitura (§7) precisa de um
 * início. (Superfície não desenhada no doc de telas; mantida mínima de propósito.)
 *
 * Since 2026-09-18 (Etapa 3) it also offers the server's events nearest to
 * today: picking one attaches the night to the timeline the pilot shares —
 * the thing that names a goal for everyone in the stadium — instead of a
 * private event with the same name. Typing still works; the kind decides
 * which timeline entries the server will accept.
 *
 * Since 2026-09-20 it has three modes: [EventSheetMode.Now] (the operator's
 * "Começa agora"), [EventSheetMode.Upcoming] (date and time, the calendar as
 * the trigger) and [EventSheetMode.Past] (date, start and end — a night the
 * watch already holds). The rules for the typed times live in [EventTimes].
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
    var dateText by remember {
        mutableStateOf(if (mode == EventSheetMode.Past) EventTimes.yesterday(Instant.now()) else EventTimes.today(Instant.now()))
    }
    var startText by remember { mutableStateOf("") }
    var endText by remember { mutableStateOf("") }
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
                        EventSheetMode.Past -> R.string.past_bring
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
                TTField(
                    stringResource(R.string.event_date_label),
                    dateText,
                    { dateText = it; timeError = null },
                    placeholder = stringResource(R.string.event_date_hint),
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TTField(
                        stringResource(if (mode == EventSheetMode.Past) R.string.event_start_label else R.string.event_time_label),
                        startText,
                        { startText = it; timeError = null },
                        modifier = Modifier.weight(1f),
                        placeholder = stringResource(R.string.event_time_hint),
                    )
                    if (mode == EventSheetMode.Past) {
                        TTField(
                            stringResource(R.string.event_end_label),
                            endText,
                            { endText = it; timeError = null },
                            modifier = Modifier.weight(1f),
                            placeholder = "01:30",
                        )
                    }
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
                            EventTimes.Reason.UNPARSEABLE -> R.string.event_bad_time
                            EventTimes.Reason.IN_PAST -> R.string.event_in_past
                            EventTimes.Reason.NOT_PAST -> R.string.event_not_past
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
                        EventSheetMode.Past -> R.string.event_fetch
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
                        EventSheetMode.Upcoming -> EventTimes.upcoming(dateText, startText, Instant.now())
                        EventSheetMode.Past -> EventTimes.past(dateText, startText, endText, Instant.now())
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
