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
import cc.tumtum.app.data.repo.Outcome
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
import cc.tumtum.app.data.api.ConsentSnapshot
import cc.tumtum.app.data.api.TumtumApi
import cc.tumtum.app.domain.Username
import cc.tumtum.app.ui.screens.account.AuthErrors
import cc.tumtum.app.ui.screens.account.UsernameField
import cc.tumtum.app.ui.screens.account.rememberUsernameChecker
import cc.tumtum.app.ui.screens.account.usernameDeclined
import cc.tumtum.app.domain.StopReason
import cc.tumtum.app.service.CaptureBus
import cc.tumtum.app.ui.screens.consent.consentOffNote
import kotlinx.coroutines.flow.first
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Row
import androidx.navigation.NavHostController
import cc.tumtum.app.R
import cc.tumtum.app.ui.components.BackArrow
import cc.tumtum.app.data.AvatarStore
import cc.tumtum.app.data.CardPhotoStore
import cc.tumtum.app.data.LocalFiles
import cc.tumtum.app.domain.ConsentText
import cc.tumtum.app.ui.screens.consent.ConsentRow
import cc.tumtum.app.ui.screens.consent.PolicyLinks
import cc.tumtum.app.ui.screens.consent.mailDpo
import cc.tumtum.app.ui.screens.consent.openLink
import cc.tumtum.app.data.prefs.SessionEnd
import cc.tumtum.app.service.Reminders
import cc.tumtum.app.ui.Fmt
import cc.tumtum.app.domain.Skin
import cc.tumtum.app.ui.components.OutlineBadge
import cc.tumtum.app.ui.components.UserAvatar
import androidx.compose.ui.Alignment
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
    // The password, asked again inside the dialog (26/09): an unlocked phone
    // on a table must not be enough to erase someone.
    var deletePassword by remember { mutableStateOf("") }

    val granted by produceState(initialValue = false) {
        value = container.health.hasPermission()
    }
    val user by container.prefs.state.collectAsStateWithLifecycle(initialValue = null)
    var nameDraft by remember { mutableStateOf("") }
    var draftsLoaded by remember { mutableStateOf(false) }
    var operatorOpen by remember { mutableStateOf(false) }
    // The @ just chosen here (28/09), so the save is said where it happened.
    var handleSaved by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(user) {
        if (!draftsLoaded && user != null) {
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

        // The profile is an account's (25/09): signed out, nobody's name,
        // photo or @ is shown here.
        if (user?.signedIn == true) {
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
            // The name saves itself (28/09, item 23): when the field lets go —
            // a tap elsewhere, or the keyboard's Done — and says so under it.
            // The big Salvar under one field was a second step nobody needed.
            var nameNote by remember { mutableStateOf<Int?>(null) }
            TTField(
                label = stringResource(R.string.account_name_label),
                value = nameDraft,
                onValueChange = {
                    nameDraft = it
                    nameNote = null
                },
                onCommit = {
                    val draft = nameDraft.trim()
                    when {
                        draft.isBlank() -> nameNote = R.string.form_missing_name
                        draft == (user?.account?.name ?: "").trim() -> Unit
                        // "Salvo." only once the account has it (28/09): it
                        // used to be said over a name kept on the phone alone.
                        else -> scope.launch {
                            nameNote = try {
                                val saved = container.api.setName(draft)
                                container.prefs.setName(saved)
                                nameDraft = saved
                                R.string.form_name_saved
                            } catch (e: TumtumApi.ApiException) {
                                if (e.code == 401) R.string.form_name_expired else R.string.form_name_failed
                            } catch (e: java.io.IOException) {
                                R.string.form_name_offline
                            }
                        }
                    }
                },
            )
            nameNote?.let {
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(it),
                    style = TTType.BodySmall,
                    color = if (it == R.string.form_name_saved) TT.Ink else TT.Rose,
                )
            }
            Spacer(Modifier.height(8.dp))
            // O @ é fixo: escolhido uma vez, não muda mais — said since 28/09
            // only over an @ the server holds. Until then it was said over
            // whatever the phone kept, and two accounts held @fezanu.
            val held = user?.account?.username
            if (held != null) {
                // One line (02/10): right after the save it says the @ is
                // yours and fixed; on every later visit, only that it is fixed.
                // Two lines saying the same thing read as a repeat.
                Text(
                    stringResource(
                        if (handleSaved == held) R.string.settings_handle_saved else R.string.settings_handle_fixed,
                        held,
                    ),
                    style = if (handleSaved == held) TTType.BodySmall else TTType.Footnote,
                    color = if (handleSaved == held) TT.Ink else TT.Gray45,
                )
            } else {
                // An account from before 28/09 whose @ the server does not
                // hold — none on this phone, or the one here was refused.
                ChooseUsername(
                    pending = user?.account?.pendingUsername,
                    refused = user?.account?.pendingRefused == true,
                    onSaved = { handleSaved = it },
                )
            }
        }

        Spacer(Modifier.height(40.dp))
        PrivacySection(signedIn = user?.session != null)

        Spacer(Modifier.height(40.dp))
        // §10 — sensor BLE: parear/trocar/remover também depois do onboarding.
        SensorSection(
            prefs = container.prefs,
            bleName = user?.bleName,
            onSearch = { nav.navigate(Routes.SensorSearch) },
        )

        Spacer(Modifier.height(40.dp))
        // Operador atrás de uma porta (§5.13 da pesquisa de 19/09): o fã não
        // precisa ler nada disto. Fecha a cada visita; quem opera toca uma vez.
        // O controle fica na linha do rótulo e o texto de apoio embaixo, nunca
        // ao lado: em 21/09 o MOSTRAR caiu no meio da frase ("Um fã não
        // precisa MOSTRAR mexer aqui") porque dividia a linha com o texto.
        DoorRow(
            title = stringResource(R.string.settings_operator_section),
            hint = stringResource(R.string.settings_participant_hint),
            control = stringResource(if (operatorOpen) R.string.settings_operator_hide else R.string.settings_operator_show),
            onClick = { operatorOpen = !operatorOpen },
        )
        if (operatorOpen) {
            Spacer(Modifier.height(4.dp))
            // The two switches that write to the event are the server's to grant
            // (item 52, 25/09): only an account the server calls an operator sees
            // them. A phone with them on under a fan's account "registered" an
            // event the server refused, and the capture said only "só neste celular".
            if (user?.isOperator == true) {
                // Modo operador: os toques GOL · MÚSICA · MOMENTO só aparecem no celular
                // de quem opera o teste. O fã nunca é convidado a fazer isso.
                val marksOn = user?.operatorMarks == true
                ToggleRow(
                    title = stringResource(R.string.settings_operator_marks),
                    hint = stringResource(R.string.settings_operator_marks_hint),
                    on = marksOn,
                    onToggle = { scope.launch { container.prefs.setOperatorMarks(!marksOn) } },
                )
                // Cadastro de evento pelo celular (21/09): o evento é da TumTum e o fã
                // só escolhe da lista. Quem cadastra é o operador, pelos atalhos que
                // esta chave acende na aba AO VIVO.
                val eventsOn = user?.operatorEvents == true
                ToggleRow(
                    title = stringResource(R.string.settings_operator_events),
                    hint = stringResource(R.string.settings_operator_events_hint),
                    on = eventsOn,
                    onToggle = { scope.launch { container.prefs.setOperatorEvents(!eventsOn) } },
                )
            } else {
                Text(
                    stringResource(R.string.settings_operator_only),
                    style = TTType.Footnote,
                    color = TT.Gray45,
                    modifier = Modifier.padding(vertical = 10.dp),
                )
            }
            // Trava da revela: com ela ligada, noites novas só abrem às 10h da manhã
            // seguinte — o cartão cego colhe a memória antes de qualquer dado.
            val lockOn = user?.revealLockEnabled == true
            ToggleRow(
                title = stringResource(R.string.settings_reveal_lock),
                hint = stringResource(R.string.settings_reveal_lock_hint),
                on = lockOn,
                onToggle = { scope.launch { container.prefs.setRevealLock(!lockOn) } },
            )
            // The experiment's participant code (P01…) went away at Felipe's
            // word on 25/09: "não faz sentido, a gente não vai precisar".
        }

        Spacer(Modifier.height(40.dp))
        Text(stringResource(R.string.settings_account_section), style = TTType.Meta, color = TT.Gray70)
        Spacer(Modifier.height(10.dp))
        // The server's side of the account (Etapa 1). Three states, each
        // said as it is: never signed in, signed in, or a token that has
        // died — the last one caught here rather than at the end of a night.
        val session = user?.session
        val sessionLive = session?.isLive(System.currentTimeMillis()) == true
        // How it ended, when the phone knows (25/09): a Sair and a renewal the
        // server refused read the same from outside, and the difference is
        // whether something is broken.
        val ended = user?.sessionEnded
        val endedAt = ended?.let { "${Fmt.date(it.at)} às ${Fmt.hour(it.at)}" }
        Text(
            when {
                sessionLive -> stringResource(R.string.settings_session_live, user?.account?.email.orEmpty())
                ended?.reason == SessionEnd.REFUSED -> stringResource(R.string.settings_session_refused_at, endedAt.orEmpty())
                session == null && ended?.reason == SessionEnd.SIGNED_OUT ->
                    stringResource(R.string.settings_session_signed_out_at, endedAt.orEmpty())
                session == null -> stringResource(R.string.settings_session_none)
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
            // Pink (#55, 23/09): signed out, entering is the one thing this
            // section is for, and a white outline read as secondary.
            TTButton(
                stringResource(R.string.settings_sign_in),
                TTButtonStyle.Rose,
                onClick = { nav.navigate(Routes.Login) },
            )
            Spacer(Modifier.height(10.dp))
            TTButton(
                stringResource(R.string.settings_create_other),
                TTButtonStyle.Outline,
                onClick = { nav.navigate(Routes.Account) },
            )
        }
        if (sessionLive) {
            Spacer(Modifier.height(32.dp))
            BlockedPeople()
        }
        // Signed out there is no account here to delete (25/09).
        if (session != null) {
            Spacer(Modifier.height(24.dp))
            Text(stringResource(R.string.settings_delete_warning), style = TTType.Footnote, color = TT.Gray45)
            Spacer(Modifier.height(14.dp))
            TTButton(
                stringResource(R.string.settings_delete),
                TTButtonStyle.Outline,
                onClick = {
                    deletePassword = ""
                    deleteError = null
                    confirmDelete = true
                },
            )
        }
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
                    if (user?.session != null) {
                        Spacer(Modifier.height(12.dp))
                        TTField(
                            label = stringResource(R.string.settings_delete_password),
                            value = deletePassword,
                            onValueChange = { deletePassword = it; deleteError = null },
                            isPassword = true,
                        )
                    }
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
                val signInFirstText = stringResource(R.string.settings_delete_needs_sign_in)
                val wrongPasswordText = stringResource(R.string.settings_delete_wrong_password)
                val needsPasswordText = stringResource(R.string.settings_delete_needs_password)
                Text(
                    stringResource(if (deleting) R.string.settings_delete_running else R.string.settings_delete_confirm),
                    style = TTType.Button.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                    color = if (deleting) TT.Gray45 else TT.Ink,
                    modifier = Modifier
                        .clickable(enabled = !deleting) {
                            if (user?.session != null && deletePassword.isEmpty()) {
                                deleteError = needsPasswordText
                                return@clickable
                            }
                            deleting = true
                            deleteError = null
                            scope.launch {
                                val before = container.prefs.state.first()
                                val session = before.session
                                val serverDone = if (session == null && before.lastUserId != null) {
                                    // Signed out, but an account was here (25/09): the
                                    // server was never asked, so nothing is deleted and
                                    // the screen does not say it was.
                                    deleteError = signInFirstText
                                    false
                                } else if (session == null) {
                                    true // never signed in: nothing on the server to delete
                                } else {
                                    runCatching { container.api.deleteAccount(deletePassword) }.fold(
                                        onSuccess = { true },
                                        onFailure = { e ->
                                            when {
                                                e is TumtumApi.ApiException && e.code == 404 -> true // already gone
                                                // 401 is a wrong password while the session still
                                                // lives, and an expired session once it does not.
                                                e is TumtumApi.ApiException && e.code == 401 -> {
                                                    val live = container.prefs.state.first().session
                                                        ?.isLive(System.currentTimeMillis()) == true
                                                    deleteError = if (live) wrongPasswordText else expiredText
                                                    false
                                                }
                                                e is TumtumApi.ApiException -> { deleteError = serverText.format(e.detail); false }
                                                else -> { deleteError = offlineText; false }
                                            }
                                        },
                                    )
                                }
                                deleting = false
                                if (serverDone) {
                                    confirmDelete = false
                                    // This account's nights, with their photos and
                                    // reveal alarms — not another account's (25/09).
                                    container.nights.deleteNightsOf(before.viewerId).forEach { gone ->
                                        CardPhotoStore.delete(gone.photoPath)
                                        Reminders.cancelReveal(context, gone.id)
                                    }
                                    // Every file the account left (26/09): the share cache,
                                    // export ZIPs, profile photos, photos behind cards.
                                    LocalFiles.wipeAccountFiles(context)
                                    deletePassword = ""
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

/**
 * Privacidade (LGPD remediation, 26/09): the seven consents, each a switch
 * that shows only what the server recorded — read on opening, changed one at
 * a time, and on a revocation the screen says what stops. Then the Terms and
 * the Policy, the person in charge of data, the one sentence about what
 * TumTum is not, and the way to download everything.
 *
 * Signed out, the switches are not shown (there is no account to read them
 * from) and the screen says so; a list that failed to load says that, never
 * a row of switches all off.
 */
@Composable
private fun PrivacySection(signedIn: Boolean) {
    val container = appContainer()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var snapshot by remember { mutableStateOf<ConsentSnapshot?>(null) }
    var loadFailed by remember { mutableStateOf(false) }
    var tick by remember { mutableStateOf(0) }
    var busy by remember { mutableStateOf<String?>(null) }
    // Answers to the last tap on a row: turned on just now, or a change that
    // did not go through. The off-state sentence is not here — it is the
    // switch's own, and stays for as long as the switch is off (28/09).
    var justOn by remember { mutableStateOf<Set<String>>(emptySet()) }
    var failures by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    // A capture this screen's "Ler sua batida" ended (28/09): said under that switch.
    val openedAt = remember { System.currentTimeMillis() }
    val forced by CaptureBus.forcedStop.collectAsStateWithLifecycle()

    LaunchedEffect(tick, signedIn) {
        snapshot = null
        loadFailed = false
        if (!signedIn) return@LaunchedEffect
        runCatching { container.api.getConsents() }
            .onSuccess { snapshot = it }
            .onFailure { loadFailed = true }
    }

    Text(stringResource(R.string.settings_privacy_section), style = TTType.Meta, color = TT.Gray70)
    Spacer(Modifier.height(6.dp))
    // Nothing waits for a Salvar here: each switch is sent the moment it moves.
    Text(stringResource(R.string.settings_privacy_instant), style = TTType.Footnote, color = TT.Gray45)
    Spacer(Modifier.height(10.dp))
    val current = snapshot?.let { ConsentText.startingSwitches(it.asMap()) }
    when {
        !signedIn -> Text(stringResource(R.string.settings_privacy_signed_out), style = TTType.Footnote, color = TT.Gray45)
        loadFailed -> {
            Text(stringResource(R.string.settings_privacy_failed), style = TTType.Footnote, color = TT.Gray45)
            Text(
                stringResource(R.string.events_retry),
                style = TTType.MetaSmall,
                color = TT.Ink,
                modifier = Modifier.clickable { tick++ }.padding(vertical = 8.dp),
            )
        }
        current == null -> Text(stringResource(R.string.consent_loading), style = TTType.Footnote, color = TT.Gray45)
        else -> ConsentText.PURPOSES.forEach { purpose ->
            val savingText = stringResource(R.string.settings_privacy_saving)
            val failedText = stringResource(R.string.settings_privacy_change_failed)
            val on = current[purpose] == true
            val stoppedCapture = purpose == ConsentText.READ_HEART_RATE && !on &&
                forced?.let { it.reason == StopReason.READING_REVOKED && it.atMs >= openedAt } == true
            // Said as what is true now: what stopped, for as long as the switch
            // is off; "Ligado." right after a grant; nothing under a key that
            // is simply on — and nothing claimed that did not happen.
            val note = when {
                busy == purpose -> savingText
                failures[purpose] != null -> failures[purpose]
                on && purpose in justOn -> stringResource(R.string.settings_privacy_granted)
                on -> null
                stoppedCapture -> stringResource(R.string.night_stopped_reading)
                else -> consentOffNote(purpose, snapshot?.consents?.firstOrNull { it.purpose == purpose })
            }
            ConsentRow(
                purpose = purpose,
                on = on,
                onDark = false,
                busy = busy != null,
                note = note,
                // The Terms are not a switch once accepted (02/10): the server
                // refuses the revocation too, and the row says how it is done.
                locked = purpose == ConsentText.TERMS && on,
                onToggle = { value ->
                    busy = purpose
                    failures = failures - purpose
                    justOn = justOn - purpose
                    scope.launch {
                        try {
                            val saved = container.api.putConsents(mapOf(purpose to value), ConsentText.MEANS_TAP)
                            snapshot = saved
                            if (saved.granted(purpose)) justOn = justOn + purpose
                        } catch (e: Exception) {
                            failures = failures + (purpose to failedText)
                        } finally {
                            busy = null
                        }
                    }
                },
            )
        }
    }

    Spacer(Modifier.height(14.dp))
    PolicyLinks(onDark = false)
    Spacer(Modifier.height(6.dp))
    Text(stringResource(R.string.settings_not_medical), style = TTType.Footnote, color = TT.Gray70)
    Spacer(Modifier.height(8.dp))
    Text(
        stringResource(R.string.settings_dpo, ConsentText.DPO_EMAIL, ConsentText.DPO_SUBJECT),
        style = TTType.Footnote,
        color = TT.Gray70,
        modifier = Modifier.clickable { mailDpo(context) }.padding(vertical = 4.dp),
    )
    Spacer(Modifier.height(14.dp))
    TTButton(
        stringResource(R.string.settings_download_data),
        TTButtonStyle.Outline,
        onClick = { openLink(context, ConsentText.DATA_URL) },
    )
    Spacer(Modifier.height(6.dp))
    Text(stringResource(R.string.settings_download_hint), style = TTType.Footnote, color = TT.Gray45)
}

/**
 * A section behind a door: the label and its control share a line, the hint
 * runs under both. The whole block is the touch target.
 */
@Composable
private fun DoorRow(title: String, hint: String, control: String, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = TTType.Meta, color = TT.Gray70, modifier = Modifier.weight(1f))
            OutlineBadge(control, borderColor = TT.Gray25, contentColor = TT.Ink, hPad = 10.dp, vPad = 6.dp)
        }
        Spacer(Modifier.height(4.dp))
        Text(hint, style = TTType.Footnote, color = TT.Gray45)
    }
}

/** An operator switch: title and LIGADA/DESLIGADA on one line, the explanation under them. */
@Composable
private fun ToggleRow(title: String, hint: String, on: Boolean, onToggle: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 10.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = TTType.Body, color = TT.Ink, modifier = Modifier.weight(1f))
            OutlineBadge(
                stringResource(if (on) R.string.settings_toggle_on else R.string.settings_toggle_off),
                borderColor = if (on) TT.Ink else TT.Gray25,
                contentColor = if (on) TT.Ink else TT.Gray45,
                hPad = 10.dp,
                vPad = 6.dp,
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(hint, style = TTType.Footnote, color = TT.Gray45)
    }
}

/**
 * Where a block from the feed is undone (#36, 22/09). Names only. A list that
 * failed to load says so rather than showing "Ninguém" — an empty state is a
 * claim, and "you blocked nobody" is not one this screen can make blind.
 */
@Composable
private fun BlockedPeople() {
    val container = appContainer()
    val scope = rememberCoroutineScope()
    var state by remember { mutableStateOf<Outcome<List<TumtumApi.BlockedPerson>>?>(null) }
    var tick by remember { mutableStateOf(0) }
    var removing by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(tick) { state = container.social.blocked() }

    Text(stringResource(R.string.settings_blocked_section), style = TTType.Meta, color = TT.Gray70)
    Spacer(Modifier.height(10.dp))
    when (val s = state) {
        null -> Unit
        is Outcome.Done ->
            if (s.value.isEmpty()) {
                Text(stringResource(R.string.settings_blocked_none), style = TTType.Footnote, color = TT.Gray45)
            } else {
                s.value.forEach { person ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(person.name, style = TTType.Body, color = TT.Ink, modifier = Modifier.weight(1f))
                        Text(
                            if (removing == person.id) "…" else stringResource(R.string.settings_unblock),
                            style = TTType.MetaSmall,
                            color = TT.Rose,
                            modifier = Modifier.clickable(enabled = removing == null) {
                                removing = person.id
                                scope.launch {
                                    container.social.unblock(person.id)
                                    removing = null
                                    tick++
                                }
                            },
                        )
                    }
                }
            }
        else -> Text(stringResource(R.string.settings_blocked_failed), style = TTType.Footnote, color = TT.Gray45)
    }
}

/**
 * "Escolhe seu @" (28/09): once, for an account the server holds no @ for —
 * made before the @ lived on the server, with none on this phone to claim or
 * one the server refused. The same live check as the sign-up; "Salvar @"
 * waits for the server's yes, and the server's refusal is said under the
 * field in its own words. Once saved the field goes for good: a set @ is
 * never editable.
 */
@Composable
private fun ChooseUsername(pending: String?, refused: Boolean, onSaved: (String) -> Unit) {
    val container = appContainer()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // The refused name stays in the field, so the line under it says why.
    var draft by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(pending.orEmpty()) }
    val checker = rememberUsernameChecker(draft)
    val status = Username.status(draft, checker.check)
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Spacer(Modifier.height(18.dp))
    Text(stringResource(R.string.settings_handle_choose_title), style = TTType.ItemTitle, color = TT.Ink)
    Spacer(Modifier.height(6.dp))
    Text(stringResource(R.string.settings_handle_choose_body), style = TTType.Footnote, color = TT.Gray45)
    if (refused && pending != null) {
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.settings_handle_refused, pending), style = TTType.BodySmall, color = TT.Ink)
    }
    Spacer(Modifier.height(12.dp))
    UsernameField(
        value = draft,
        onValueChange = {
            if (it != draft) error = null
            draft = it
        },
        checker = checker,
    )
    Spacer(Modifier.height(14.dp))
    TTButton(
        if (saving) stringResource(R.string.auth_working) else stringResource(R.string.settings_handle_save),
        TTButtonStyle.Rose,
        enabled = status == Username.Status.Available && !saving,
        onDeclined = { if (!saving) error = usernameDeclined(status, checker, context) },
        onClick = {
            val name = draft
            saving = true
            error = null
            scope.launch {
                try {
                    val held = container.api.setUsername(name)
                    onSaved(held)
                } catch (e: TumtumApi.ApiException) {
                    error = when {
                        (e.code == 409 || e.code == 422) && Username.isAboutUsername(e.detail) -> {
                            checker.refused(name, e.detail)
                            e.detail
                        }
                        e.code == 401 -> context.getString(R.string.settings_session_expired)
                        else -> AuthErrors.messageFor(e, context)
                    }
                } catch (e: java.io.IOException) {
                    error = context.getString(R.string.settings_handle_offline)
                } catch (e: Exception) {
                    error = AuthErrors.messageFor(e, context)
                } finally {
                    saving = false
                }
            }
        },
    )
    error?.let {
        Spacer(Modifier.height(10.dp))
        Text(it, style = TTType.Body, color = TT.Rose)
    }
}
