package cc.tumtum.app.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cc.tumtum.app.R
import cc.tumtum.app.ui.components.BackArrow
import cc.tumtum.app.data.prefs.Account
import cc.tumtum.app.ui.components.TTButton
import cc.tumtum.app.ui.components.TTButtonStyle
import cc.tumtum.app.ui.components.TTField
import cc.tumtum.app.ui.components.Wordmark
import cc.tumtum.app.ui.nav.Routes
import cc.tumtum.app.ui.nav.appContainer
import cc.tumtum.app.ui.screens.account.AuthErrors
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType
import kotlinx.coroutines.launch

/**
 * Entrar — para quem já tem conta.
 *
 * Until 2026-09-18 this opened a local session with whatever @ was typed.
 * Now it is the real thing: e-mail and password against the server, a token
 * that knows its own expiry, and the local profile filled from what the
 * server says. The @ stays what this phone already had; a phone that never
 * had one gets the e-mail's prefix, editable nowhere yet — the @ is fixed by
 * design, and the server does not know it (one-app-plan, "later").
 */
@Composable
fun LoginScreen(nav: NavHostController) {
    val container = appContainer()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val user by container.prefs.state.collectAsStateWithLifecycle(initialValue = null)

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val valid = email.contains("@") && password.isNotEmpty()

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
        Spacer(Modifier.height(24.dp))
        Wordmark(width = 92.dp)
        Spacer(Modifier.height(40.dp))
        Text(stringResource(R.string.login_title), style = TTType.Title, color = TT.Ink)
        Spacer(Modifier.height(6.dp))
        Text(stringResource(R.string.login_subtitle), style = TTType.Body, color = TT.Gray45)
        Spacer(Modifier.height(30.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TTField(
                stringResource(R.string.login_email_label),
                email,
                { email = it },
                placeholder = stringResource(R.string.account_email_hint),
                keyboardType = KeyboardType.Email,
            )
            TTField(stringResource(R.string.account_password_label), password, { password = it }, isPassword = true)
        }
        Spacer(Modifier.height(40.dp))
        TTButton(
            if (saving) stringResource(R.string.auth_working) else stringResource(R.string.login_cta),
            TTButtonStyle.Rose,
            enabled = valid && !saving,
            onClick = {
                saving = true
                error = null
                scope.launch {
                    try {
                        val cleanEmail = email.trim()
                        container.api.login(cleanEmail, password)
                        // The name is the server's; the @ and the tribes are this phone's.
                        val me = runCatching { container.api.me() }.getOrNull()
                        val existing = user?.account
                        container.prefs.createAccount(
                            Account(
                                name = me?.name?.ifBlank { null } ?: existing?.name ?: AuthErrors.handleFrom(cleanEmail)
                                    .replaceFirstChar { it.uppercase() },
                                username = existing?.username ?: AuthErrors.handleFrom(cleanEmail),
                                email = me?.email ?: cleanEmail,
                                tribes = existing?.tribes ?: emptySet(),
                            ),
                        )
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
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.login_forgot), style = TTType.Footnote, color = TT.Gray45)
    }
}
