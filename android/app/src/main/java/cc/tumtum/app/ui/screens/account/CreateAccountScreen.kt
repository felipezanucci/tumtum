package cc.tumtum.app.ui.screens.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.navigation.NavHostController
import cc.tumtum.app.R
import cc.tumtum.app.data.prefs.Account
import cc.tumtum.app.ui.components.TTButton
import cc.tumtum.app.ui.components.TTButtonStyle
import cc.tumtum.app.ui.components.TTField
import cc.tumtum.app.ui.components.TribeChip
import cc.tumtum.app.ui.components.Wordmark
import cc.tumtum.app.ui.nav.Routes
import cc.tumtum.app.ui.nav.appContainer
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType
import kotlinx.coroutines.launch

/** Handles reservados no repositório fake — a "checagem de disponibilidade" local. */
private val TAKEN = setOf("mariana", "rodcosta", "jureis", "pbarros", "ltoledo", "tumtum")

private val TRIBES = listOf("SHOWS", "FUTEBOL", "FESTIVAIS")

/**
 * b2 — Criar conta. Leva menos que uma música.
 *
 * Since 2026-09-18 (Etapa 1) the account is created on the server first and
 * kept locally second: the @, the tribes and the participant id stay on the
 * phone, the e-mail, name and password become a real account with a token.
 * Without the server there is no account — the screen says so instead of
 * pretending.
 */
@Composable
fun CreateAccountScreen(nav: NavHostController) {
    val container = appContainer()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var name by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var tribes by rememberSaveable { mutableStateOf(setOf<String>()) }
    var participant by rememberSaveable { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val user by container.prefs.state.collectAsStateWithLifecycle(initialValue = null)
    // A second account on the same phone (18/09): the profile is replaced and
    // the nights recorded here go with the old one. Said before the tap.
    val replacing = user?.account

    val usernameClean = username.trim().lowercase()
    val usernameTaken = usernameClean in TAKEN
    val valid = name.isNotBlank() && usernameClean.length >= 3 && !usernameTaken &&
        email.contains("@") && password.length >= 8

    Column(
        Modifier
            .fillMaxSize()
            .background(TT.Paper)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .navigationBarsPadding()
            .padding(start = 28.dp, end = 28.dp, top = 22.dp, bottom = 30.dp),
    ) {
        Wordmark(width = 92.dp)
        Spacer(Modifier.height(40.dp))
        Text(stringResource(R.string.account_title), style = TTType.Title, color = TT.Ink)
        Spacer(Modifier.height(6.dp))
        Text(stringResource(R.string.account_subtitle), style = TTType.Body, color = TT.Gray45)
        replacing?.let {
            Spacer(Modifier.height(14.dp))
            Text(
                stringResource(R.string.account_replaces, it.username),
                style = TTType.BodySmall,
                color = TT.Ink,
            )
        }
        Spacer(Modifier.height(30.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TTField(stringResource(R.string.account_name_label), name, { name = it })
            TTField(
                stringResource(R.string.account_username_label),
                username,
                { username = it.filter { c -> c.isLetterOrDigit() || c == '_' } },
                trailing = {
                    if (usernameClean.length >= 3) {
                        Text(
                            text = if (usernameTaken) {
                                stringResource(R.string.account_username_taken)
                            } else {
                                stringResource(R.string.account_username_free)
                            },
                            style = TTType.MetaSmall,
                            color = if (usernameTaken) TT.Gray45 else TT.Ink,
                        )
                    }
                },
            )
            TTField(
                stringResource(R.string.account_email_label),
                email,
                { email = it },
                placeholder = stringResource(R.string.account_email_hint),
                keyboardType = KeyboardType.Email,
            )
            TTField(stringResource(R.string.account_password_label), password, { password = it }, isPassword = true)
        }

        Spacer(Modifier.height(22.dp))
        Row {
            Text(stringResource(R.string.account_tribes_label), style = TTType.Meta, color = TT.Gray70)
            Spacer(Modifier.padding(2.dp))
            Text(stringResource(R.string.account_tribes_optional), style = TTType.Meta.copy(letterSpacing = 0.em), color = TT.Gray45)
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TRIBES.forEach { tribe ->
                TribeChip(
                    tribe,
                    selected = tribe in tribes,
                    onToggle = { tribes = if (tribe in tribes) tribes - tribe else tribes + tribe },
                )
            }
        }

        Spacer(Modifier.height(22.dp))
        // §9 — identificador do participante do experimento (P01…P18). Opcional fora dele.
        TTField(
            stringResource(R.string.participant_label),
            participant,
            { participant = it },
            placeholder = "P01",
        )
        Spacer(Modifier.height(6.dp))
        Text(stringResource(R.string.participant_hint), style = TTType.Footnote, color = TT.Gray45)

        Spacer(Modifier.height(40.dp))
        TTButton(
            if (saving) stringResource(R.string.auth_working) else stringResource(R.string.account_cta),
            TTButtonStyle.Rose,
            enabled = valid && !saving,
            onClick = {
                saving = true
                error = null
                scope.launch {
                    try {
                        container.api.register(email = email.trim(), name = name.trim(), password = password)
                        if (replacing != null) container.nights.wipeAll()
                        container.prefs.createAccount(
                            Account(name = name.trim(), username = usernameClean, email = email.trim(), tribes = tribes),
                        )
                        container.prefs.setParticipantId(participant)
                        nav.navigate(Routes.Permission)
                    } catch (e: Exception) {
                        error = AuthErrors.messageFor(e, context)
                    } finally {
                        saving = false
                    }
                }
            },
        )
        error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, style = TTType.Body, color = TT.Rose)
        }
    }
}
