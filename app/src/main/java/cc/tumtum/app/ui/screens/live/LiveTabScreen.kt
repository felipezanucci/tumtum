package cc.tumtum.app.ui.screens.live

import androidx.compose.foundation.background
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
    val scope = rememberCoroutineScope()
    var showCreate by remember { mutableStateOf(false) }

    LaunchedEffect(activeEvent) {
        if (activeEvent != null) {
            nav.navigate(Routes.Capture) { launchSingleTop = true }
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    var pendingEvent by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showBatteryGate by remember { mutableStateOf(false) }

    val state = user ?: return

    // §6 — com sensor BLE pareado, a sessão só começa com a isenção de bateria concedida.
    fun createEvent(name: String, venue: String) {
        val paired = state.sensorPaired
        if (paired && !cc.tumtum.app.service.BatteryExemption.isExempt(context)) {
            pendingEvent = name to venue
            showBatteryGate = true
            return
        }
        scope.launch {
            val eventId = vm.startEvent(name, venue)
            val address = state.bleAddress
            if (paired && address != null) {
                cc.tumtum.app.service.CaptureService.start(context, eventId, address)
            }
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
            Wordmark(width = 92.dp)
            Avatar(state.account?.initials ?: "TT", Skin.BLACK)
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
            if (state.watchConnected) {
                TTButton(stringResource(R.string.empty_create_event), TTButtonStyle.Rose, onClick = { showCreate = true })
            } else {
                TTButton(
                    stringResource(R.string.empty_connect),
                    TTButtonStyle.Rose,
                    onClick = { nav.navigate(Routes.Permission) },
                )
                Spacer(Modifier.height(10.dp))
                TTButton(stringResource(R.string.empty_create_event), TTButtonStyle.Outline, onClick = { showCreate = true })
            }
        }
    }

    if (showCreate) {
        CreateEventSheet(
            onDismiss = { showCreate = false },
            onCreate = { name, venue ->
                showCreate = false
                createEvent(name, venue)
            },
        )
    }

    if (showBatteryGate) {
        BatteryExemptionSheet(
            onDismiss = {
                showBatteryGate = false
                pendingEvent = null
            },
            onExempt = {
                showBatteryGate = false
                pendingEvent?.let { (name, venue) -> createEvent(name, venue) }
                pendingEvent = null
            },
        )
    }
}

/**
 * Sheet mínima para marcar o evento — a janela de leitura (§7) precisa de um
 * início. (Superfície não desenhada no doc de telas; mantida mínima de propósito.)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventSheet(onDismiss: () -> Unit, onCreate: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var venue by remember { mutableStateOf("") }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = TT.Paper,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(start = 28.dp, end = 28.dp, bottom = 40.dp)) {
            Text(stringResource(R.string.event_new_title), style = TTType.TitleSmall, color = TT.Ink)
            Spacer(Modifier.height(24.dp))
            TTField(
                stringResource(R.string.event_name_label),
                name,
                { name = it },
                placeholder = stringResource(R.string.event_name_hint),
            )
            Spacer(Modifier.height(12.dp))
            TTField(
                stringResource(R.string.event_venue_label),
                venue,
                { venue = it },
                placeholder = stringResource(R.string.event_venue_hint),
            )
            Spacer(Modifier.height(26.dp))
            TTButton(
                stringResource(R.string.event_starts_now),
                TTButtonStyle.Rose,
                enabled = name.isNotBlank(),
                onClick = { onCreate(name, venue) },
            )
        }
    }
}
