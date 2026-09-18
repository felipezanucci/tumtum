package cc.tumtum.app.ui.screens.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import cc.tumtum.app.data.api.TumtumApi
import kotlinx.coroutines.flow.first
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Row
import androidx.navigation.NavHostController
import cc.tumtum.app.R
import cc.tumtum.app.ui.components.BackArrow
import cc.tumtum.app.data.AvatarStore
import cc.tumtum.app.domain.Skin
import cc.tumtum.app.service.BatteryExemption
import cc.tumtum.app.service.CaptureService
import cc.tumtum.app.ui.screens.live.BatteryExemptionSheet
import cc.tumtum.app.ui.screens.live.CreateEventSheet
import cc.tumtum.app.ui.screens.live.NewEvent
import cc.tumtum.app.ui.components.UserAvatar
import cc.tumtum.app.ui.components.TTButton
import cc.tumtum.app.ui.components.TTButtonStyle
import cc.tumtum.app.ui.components.TTField
import cc.tumtum.app.ui.nav.Routes
import cc.tumtum.app.ui.screens.sources.SensorSection
import cc.tumtum.app.ui.nav.appContainer
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType
import kotlinx.coroutines.launch

/**
 * Configurações (§7): revogar leitura não quebra o app — noites ficam,
 * nova captura bloqueia com explicação. Apagar conta apaga tudo, avisado uma vez.
 */
@Composable
fun SettingsScreen(nav: NavHostController) {
    val container = appContainer()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var confirmDelete by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    var deleteError by remember { mutableStateOf<String?>(null) }

    val granted by produceState(initialValue = false) {
        value = container.health.hasPermission()
    }
    val user by container.prefs.state.collectAsStateWithLifecycle(initialValue = null)
    var participantDraft by remember { mutableStateOf("") }
    var nameDraft by remember { mutableStateOf("") }
    var draftsLoaded by remember { mutableStateOf(false) }
    var showCreateEvent by remember { mutableStateOf(false) }
    var showBatteryGate by remember { mutableStateOf(false) }
    var pendingEvent by remember { mutableStateOf<NewEvent?>(null) }

    // §6 — com sensor pareado, a sessão só começa com a isenção de bateria concedida.
    fun createEvent(spec: NewEvent) {
        val paired = user?.sensorPaired == true
        val address = user?.bleAddress
        if (paired && !BatteryExemption.isExempt(context)) {
            pendingEvent = spec
            showBatteryGate = true
            return
        }
        scope.launch {
            val eventId = container.nights.startEvent(spec.name, spec.venue, spec.eventType, spec.serverEventId)
            if (paired && address != null) {
                container.prefs.setActiveCapture(eventId)
                CaptureService.start(context, eventId, address)
            }
            nav.navigate(Routes.Live) { launchSingleTop = true }
        }
    }
    LaunchedEffect(user) {
        if (!draftsLoaded && user != null) {
            participantDraft = user?.participantId ?: ""
            nameDraft = user?.account?.name ?: ""
            draftsLoaded = true
        }
    }

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
        Text(stringResource(R.string.settings_title), style = TTType.Title, color = TT.Ink)

        Spacer(Modifier.height(36.dp))
        Text(stringResource(R.string.settings_hc_section), style = TTType.Meta, color = TT.Gray70)
        Spacer(Modifier.height(10.dp))
        Text(
            stringResource(if (granted) R.string.settings_hc_granted else R.string.settings_hc_revoked),
            style = TTType.Body,
            color = if (granted) TT.Ink else TT.Gray70,
        )
        Spacer(Modifier.height(14.dp))
        TTButton(
            stringResource(R.string.settings_hc_manage),
            TTButtonStyle.Outline,
            onClick = {
                // Tela do sistema do Health Connect; revogação acontece lá.
                runCatching {
                    context.startActivity(Intent("androidx.health.ACTION_HEALTH_CONNECT_SETTINGS"))
                }
            },
        )

        Spacer(Modifier.height(40.dp))
        Text(stringResource(R.string.settings_profile_section), style = TTType.Meta, color = TT.Gray70)
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            val photoScope = rememberCoroutineScope()
            val picker = rememberLauncherForActivityResult(
                ActivityResultContracts.PickVisualMedia(),
            ) { uri ->
                if (uri != null) {
                    photoScope.launch {
                        AvatarStore.import(context, uri, user?.avatarPath)?.let {
                            container.prefs.setAvatarPath(it)
                        }
                    }
                }
            }
            UserAvatar(
                user?.account?.initials ?: "TT",
                Skin.PINK,
                photoPath = user?.avatarPath,
                size = 48.dp,
            )
            Spacer(Modifier.padding(6.dp))
            Text(
                stringResource(R.string.settings_photo_change),
                style = TTType.Meta.copy(fontSize = 12.sp),
                color = TT.Ink,
                modifier = Modifier
                    .clickable {
                        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                    .padding(6.dp),
            )
        }
        Spacer(Modifier.height(14.dp))
        TTField(
            label = stringResource(R.string.account_name_label),
            value = nameDraft,
            onValueChange = { nameDraft = it },
        )
        Spacer(Modifier.height(8.dp))
        // O @ é fixo: escolhido uma vez na criação da conta, não muda mais.
        Text(
            stringResource(R.string.settings_handle_fixed, user?.account?.username ?: ""),
            style = TTType.Footnote,
            color = TT.Gray45,
        )
        Spacer(Modifier.height(14.dp))
        TTButton(
            stringResource(R.string.profile_save),
            TTButtonStyle.Ink,
            enabled = nameDraft.isNotBlank() && nameDraft.trim() != (user?.account?.name ?: ""),
            onClick = { scope.launch { container.prefs.setName(nameDraft) } },
        )

        Spacer(Modifier.height(40.dp))
        // §10 — sensor BLE: parear/trocar/remover também depois do onboarding.
        SensorSection(prefs = container.prefs, bleName = user?.bleName)

        Spacer(Modifier.height(40.dp))
        Text(stringResource(R.string.settings_participant_section), style = TTType.Meta, color = TT.Gray70)
        Spacer(Modifier.height(10.dp))
        Text(stringResource(R.string.settings_participant_hint), style = TTType.Footnote, color = TT.Gray45)
        Spacer(Modifier.height(4.dp))
        // Modo operador: os toques GOL · MÚSICA · MOMENTO só aparecem no celular
        // de quem opera o teste. O fã nunca é convidado a fazer isso.
        val marksOn = user?.operatorMarks == true
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { scope.launch { container.prefs.setOperatorMarks(!marksOn) } }
                .padding(vertical = 10.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.settings_operator_marks), style = TTType.Body, color = TT.Ink)
                Text(stringResource(R.string.settings_operator_marks_hint), style = TTType.Footnote, color = TT.Gray45)
            }
            Text(
                stringResource(if (marksOn) R.string.settings_toggle_on else R.string.settings_toggle_off),
                style = TTType.Meta,
                color = if (marksOn) TT.Ink else TT.Gray45,
            )
        }
        // Trava da revela: com ela ligada, noites novas só abrem às 10h da manhã
        // seguinte — o cartão cego colhe a memória antes de qualquer dado.
        val lockOn = user?.revealLockEnabled == true
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { scope.launch { container.prefs.setRevealLock(!lockOn) } }
                .padding(vertical = 10.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.settings_reveal_lock), style = TTType.Body, color = TT.Ink)
                Text(stringResource(R.string.settings_reveal_lock_hint), style = TTType.Footnote, color = TT.Gray45)
            }
            Text(
                stringResource(if (lockOn) R.string.settings_toggle_on else R.string.settings_toggle_off),
                style = TTType.Meta,
                color = if (lockOn) TT.Ink else TT.Gray45,
            )
        }
        Spacer(Modifier.height(14.dp))
        TTField(
            label = stringResource(R.string.participant_label),
            value = participantDraft,
            onValueChange = {
                participantDraft = it
                scope.launch { container.prefs.setParticipantId(it) }
            },
            placeholder = "P01",
        )
        Spacer(Modifier.height(4.dp))
        Text(stringResource(R.string.participant_hint), style = TTType.Footnote, color = TT.Gray45)
        Spacer(Modifier.height(14.dp))
        // O participante não cria evento (a5): o operador marca aqui e a captura
        // aparece sozinha na aba AO VIVO de quem está com o aparelho.
        TTButton(
            stringResource(R.string.settings_create_event),
            TTButtonStyle.Outline,
            onClick = { showCreateEvent = true },
        )

        Spacer(Modifier.height(40.dp))
        Text(stringResource(R.string.settings_account_section), style = TTType.Meta, color = TT.Gray70)
        Spacer(Modifier.height(10.dp))
        // The server's side of the account (Etapa 1). Three states, each
        // said as it is: never signed in, signed in, or a token that has
        // died — the last one caught here rather than at the end of a night.
        val session = user?.session
        val sessionLive = session?.isLive(System.currentTimeMillis()) == true
        Text(
            when {
                session == null -> stringResource(R.string.settings_session_none)
                sessionLive -> stringResource(R.string.settings_session_live, user?.account?.email.orEmpty())
                else -> stringResource(R.string.settings_session_expired)
            },
            style = TTType.Footnote,
            color = TT.Gray45,
        )
        Spacer(Modifier.height(14.dp))
        if (sessionLive) {
            TTButton(
                stringResource(R.string.settings_sign_out),
                TTButtonStyle.Outline,
                onClick = { scope.launch { container.api.signOut() } },
            )
        } else {
            TTButton(
                stringResource(R.string.settings_sign_in),
                TTButtonStyle.Outline,
                onClick = { nav.navigate(Routes.Login) },
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(stringResource(R.string.settings_delete_warning), style = TTType.Footnote, color = TT.Gray45)
        Spacer(Modifier.height(14.dp))
        TTButton(
            stringResource(R.string.settings_delete),
            TTButtonStyle.Outline,
            onClick = { confirmDelete = true },
        )
    }

    if (showCreateEvent) {
        CreateEventSheet(
            onDismiss = { showCreateEvent = false },
            onCreate = { spec ->
                showCreateEvent = false
                createEvent(spec)
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
                pendingEvent?.let { createEvent(it) }
                pendingEvent = null
            },
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            containerColor = TT.Paper,
            title = {
                Text(
                    stringResource(R.string.settings_delete),
                    style = TTType.TitleSmall.copy(fontSize = 22.sp),
                    color = TT.Ink,
                )
            },
            text = {
                Column {
                    Text(stringResource(R.string.settings_delete_warning), style = TTType.Body, color = TT.Gray70)
                    deleteError?.let {
                        Spacer(Modifier.height(10.dp))
                        Text(it, style = TTType.BodySmall, color = TT.Ink)
                    }
                }
            },
            confirmButton = {
                // The server first, the phone second. If the server did not
                // delete, nothing local goes either: the privacy page promises
                // the account is gone, and a wiped phone over a live account
                // would be the app lying about its own state (item 32).
                val offlineText = stringResource(R.string.settings_delete_failed_offline)
                val expiredText = stringResource(R.string.settings_delete_failed_expired)
                val serverText = stringResource(R.string.settings_delete_failed_server)
                Text(
                    stringResource(if (deleting) R.string.settings_delete_running else R.string.settings_delete_confirm),
                    style = TTType.Button.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                    color = if (deleting) TT.Gray45 else TT.Ink,
                    modifier = Modifier
                        .clickable(enabled = !deleting) {
                            deleting = true
                            deleteError = null
                            scope.launch {
                                val session = container.prefs.state.first().session
                                val serverDone = if (session == null) {
                                    true // never signed in: nothing on the server to delete
                                } else {
                                    runCatching { container.api.deleteAccount() }.fold(
                                        onSuccess = { true },
                                        onFailure = { e ->
                                            when {
                                                e is TumtumApi.ApiException && e.code == 404 -> true // already gone
                                                e is TumtumApi.ApiException && e.code == 401 -> { deleteError = expiredText; false }
                                                e is TumtumApi.ApiException -> { deleteError = serverText.format(e.detail); false }
                                                else -> { deleteError = offlineText; false }
                                            }
                                        },
                                    )
                                }
                                deleting = false
                                if (serverDone) {
                                    confirmDelete = false
                                    container.nights.wipeAll()
                                    container.prefs.wipe()
                                    nav.navigate(Routes.Onboarding) { popUpTo(0) { inclusive = true } }
                                }
                            }
                        }
                        .padding(12.dp),
                )
            },
            dismissButton = {
                Text(
                    stringResource(R.string.settings_delete_cancel),
                    style = TTType.Button.copy(fontSize = 14.sp),
                    color = TT.Gray45,
                    modifier = Modifier.clickable { confirmDelete = false }.padding(12.dp),
                )
            },
        )
    }
}
