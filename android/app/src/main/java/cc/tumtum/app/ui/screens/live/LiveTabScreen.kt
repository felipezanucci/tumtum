package cc.tumtum.app.ui.screens.live

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import cc.tumtum.app.R
import java.time.LocalDate
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import cc.tumtum.app.ui.components.TribeChip
import cc.tumtum.app.data.api.ServerEvents
import cc.tumtum.app.data.api.ServerEvent
import cc.tumtum.app.ui.components.Avatar
import cc.tumtum.app.ui.components.TTButton
import cc.tumtum.app.ui.components.TTButtonStyle
import cc.tumtum.app.ui.components.TTField
import cc.tumtum.app.ui.components.Wordmark
import cc.tumtum.app.domain.Skin
import cc.tumtum.app.ui.nav.Routes
import cc.tumtum.app.ui.nav.appContainer
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType
import kotlinx.coroutines.launch

/**
 * Aba AO VIVO: com evento ativo vai direto para a captura (a2, tela cheia);
 * sem evento, é o estado de espera/vazio (a5).
 */
@Composable
fun LiveTabScreen(nav: NavHostController) {
    val container = appContainer()
    val vm: LiveViewModel = viewModel { LiveViewModel(container) }
    val activeEvent by vm.activeEvent.collectAsStateWithLifecycle()
    val user by container.prefs.state.collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(activeEvent) {
        if (activeEvent != null) {
            nav.navigate(Routes.Capture) { launchSingleTop = true }
        }
    }

    val state = user ?: return
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
            Wordmark(width = 92.dp)
            cc.tumtum.app.ui.components.UserAvatar(
                state.account?.initials ?: "TT",
                Skin.BLACK,
                photoPath = state.avatarPath,
            )
        }
        // a5 — Vazio: convida a marcar o próximo evento, não a comprar nada.
        Column(
            Modifier.weight(1f).fillMaxWidth().padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
        ) {
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
                Spacer(Modifier.height(14.dp))
            }
            // O evento não é criado pelo participante: o operador marca (Configurações →
            // EXPERIMENTO) e a captura aparece aqui sozinha.
            Text(
                stringResource(R.string.empty_event_note),
                style = TTType.Footnote,
                color = TT.Gray45,
            )
        }
    }
}

/** What the sheet hands back: an event to start, typed or picked from the server's list (Etapa 3). */
data class NewEvent(
    val name: String,
    val venue: String,
    val eventType: String = "concert",
    val serverEventId: String? = null,
)

/**
 * Sheet mínima para marcar o evento — a janela de leitura (§7) precisa de um
 * início. (Superfície não desenhada no doc de telas; mantida mínima de propósito.)
 *
 * Since 2026-09-18 (Etapa 3) it also offers the server's events nearest to
 * today: picking one attaches the night to the timeline the pilot shares —
 * the thing that names a goal for everyone in the stadium — instead of a
 * private event with the same name. Typing still works; the kind decides
 * which timeline entries the server will accept.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventSheet(onDismiss: () -> Unit, onCreate: (NewEvent) -> Unit) {
    val container = appContainer()
    var name by remember { mutableStateOf("") }
    var venue by remember { mutableStateOf("") }
    var eventType by remember { mutableStateOf("concert") }
    var picked by remember { mutableStateOf<ServerEvent?>(null) }
    var serverEvents by remember { mutableStateOf<List<ServerEvent>?>(null) }
    var listFailed by remember { mutableStateOf(false) }

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
            Text(stringResource(R.string.event_new_title), style = TTType.TitleSmall, color = TT.Ink)
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
            Spacer(Modifier.height(26.dp))
            // Sempre rosa: um botão apagado a 40% lê como "nada para fazer". Sem
            // nome, o toque diz o que falta em vez de não responder.
            var needsName by remember { mutableStateOf(false) }
            if (needsName && name.isBlank()) {
                Text(stringResource(R.string.event_needs_name), style = TTType.BodySmall, color = TT.Ink)
                Spacer(Modifier.height(10.dp))
            }
            TTButton(
                stringResource(R.string.event_starts_now),
                TTButtonStyle.Rose,
                onClick = {
                    if (name.isBlank()) {
                        needsName = true
                    } else {
                        onCreate(NewEvent(name = name, venue = venue, eventType = eventType, serverEventId = picked?.id))
                    }
                },
            )
        }
    }
}
