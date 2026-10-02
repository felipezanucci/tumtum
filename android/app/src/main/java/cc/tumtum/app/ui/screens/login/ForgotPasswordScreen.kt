package cc.tumtum.app.ui.screens.login

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cc.tumtum.app.R
import cc.tumtum.app.data.api.SignupCode
import cc.tumtum.app.ui.components.BackArrow
import cc.tumtum.app.ui.components.TTButton
import cc.tumtum.app.ui.components.TTButtonStyle
import cc.tumtum.app.ui.components.TTField
import cc.tumtum.app.ui.components.Wordmark
import cc.tumtum.app.ui.nav.appContainer
import cc.tumtum.app.ui.screens.account.AuthErrors
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** The server's shortest password, said before the server has to. */
private const val MIN_PASSWORD = 6

/** Seconds before another code may be asked for, as on the sign-up. */
private const val RESEND_AFTER_SECONDS = 60

/**
 * "Esqueci a senha", in the app (02/10).
 *
 * Until that day the sign-in screen said "dá para criar outra em
 * tumtum.cc/login" and nothing else: no button, and a browser to leave for.
 * Felipe, locked out of his own account on b227: *"essa experiência é
 * péssima"*. Now two steps, on the sign-up's pattern — the address, then the
 * six digits from the mail with the new password — and the person is signed
 * in at the end, landing where a sign-in lands ([completeSignIn]).
 *
 * The first step's answer is the same whether or not the address has an
 * account (the server admits nothing), so the second step says "se esse
 * e-mail tiver conta", never "mandamos".
 */
@Composable
fun ForgotPasswordScreen(nav: NavHostController, initialEmail: String) {
    val container = appContainer()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val user by container.prefs.state.collectAsStateWithLifecycle(initialValue = null)

    var email by rememberSaveable { mutableStateOf(initialEmail) }
    var sentTo by rememberSaveable { mutableStateOf<String?>(null) }
    var code by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var resendAt by rememberSaveable { mutableStateOf(0L) }
    var working by remember { mutableStateOf(false) }
    var done by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var notice by remember { mutableStateOf<String?>(null) }

    fun ask(again: Boolean) {
        val address = email.trim()
        working = true
        error = null
        notice = null
        scope.launch {
            try {
                container.api.forgotPassword(address)
                sentTo = address
                code = ""
                resendAt = System.currentTimeMillis() + RESEND_AFTER_SECONDS * 1000L
                if (again) notice = context.getString(R.string.forgot_resent, address)
            } catch (e: Exception) {
                error = AuthErrors.messageFor(e, context)
            } finally {
                working = false
            }
        }
    }

    // Back from the code step returns to the address, still typed.
    BackHandler(enabled = sentTo != null && !working) {
        sentTo = null
        error = null
        notice = null
    }

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
        BackArrow(onClick = {
            if (sentTo != null && !working) {
                sentTo = null
                error = null
                notice = null
            } else {
                nav.popBackStack()
            }
        })
        Spacer(Modifier.height(24.dp))
        Wordmark(width = 92.dp)
        Spacer(Modifier.height(40.dp))

        val address = sentTo
        if (address == null) {
            Text(stringResource(R.string.forgot_title), style = TTType.Title, color = TT.Ink)
            Spacer(Modifier.height(6.dp))
            Text(stringResource(R.string.forgot_body), style = TTType.Body, color = TT.Gray45)
            Spacer(Modifier.height(30.dp))
            TTField(
                stringResource(R.string.login_email_label),
                email,
                { email = it; error = null },
                placeholder = stringResource(R.string.account_email_hint),
                keyboardType = KeyboardType.Email,
            )
            Spacer(Modifier.height(28.dp))
            TTButton(
                if (working) stringResource(R.string.auth_working) else stringResource(R.string.forgot_send),
                TTButtonStyle.Rose,
                enabled = email.contains("@") && !working,
                onDeclined = {
                    if (!working) {
                        error = context.getString(
                            if (email.isBlank()) R.string.form_missing_email else R.string.form_email_without_at,
                        )
                    }
                },
                onClick = { ask(again = false) },
            )
            error?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, style = TTType.Body, color = TT.Rose)
            }
        } else {
            // The wait before another code, counted down where the button is.
            var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
            LaunchedEffect(resendAt) {
                now = System.currentTimeMillis()
                while (now < resendAt) {
                    delay(1_000)
                    now = System.currentTimeMillis()
                }
            }
            val waitSeconds = SignupCode.secondsUntil(resendAt, now)
            val passwordOk = password.length >= MIN_PASSWORD

            Text(stringResource(R.string.account_code_title), style = TTType.Title, color = TT.Ink)
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.forgot_code_body, address), style = TTType.Body, color = TT.Ink)
            Spacer(Modifier.height(26.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TTField(
                    stringResource(R.string.account_code_label),
                    code,
                    { code = SignupCode.digits(it); error = null },
                    placeholder = "000000",
                    keyboardType = KeyboardType.NumberPassword,
                )
                TTField(
                    stringResource(R.string.forgot_new_password_label),
                    password,
                    { password = it; error = null },
                    isPassword = true,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.account_code_spam), style = TTType.Footnote, color = TT.Gray45)
            Spacer(Modifier.height(28.dp))
            TTButton(
                when {
                    done -> stringResource(R.string.forgot_done)
                    working -> stringResource(R.string.auth_working)
                    else -> stringResource(R.string.forgot_cta)
                },
                TTButtonStyle.Rose,
                enabled = SignupCode.isComplete(code) && passwordOk && !working,
                onDeclined = {
                    if (!working) {
                        error = context.getString(
                            if (!SignupCode.isComplete(code)) R.string.account_code_short else R.string.forgot_password_short,
                        )
                    }
                },
                onClick = {
                    working = true
                    error = null
                    notice = null
                    scope.launch {
                        try {
                            container.api.resetPasswordWithCode(address, code, password)
                            // Said on the button while the profile is filled
                            // and the next screen chosen: the password changed.
                            done = true
                            completeSignIn(container, nav, onboarded = user?.onboarded == true, typedEmail = address)
                        } catch (e: Exception) {
                            done = false
                            error = AuthErrors.messageFor(e, context)
                        } finally {
                            working = false
                        }
                    }
                },
            )
            error?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, style = TTType.Body, color = TT.Rose)
            }
            notice?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, style = TTType.Body, color = TT.Ink)
            }
            Spacer(Modifier.height(18.dp))
            TTButton(
                if (waitSeconds > 0) {
                    stringResource(R.string.account_code_resend_in, waitSeconds)
                } else {
                    stringResource(R.string.account_code_resend)
                },
                TTButtonStyle.Outline,
                enabled = waitSeconds == 0 && !working,
                onDeclined = {
                    if (!working && waitSeconds > 0) {
                        notice = null
                        error = context.getString(R.string.account_code_resend_wait, waitSeconds)
                    }
                },
                onClick = { ask(again = true) },
            )
            Spacer(Modifier.height(10.dp))
            TTButton(
                stringResource(R.string.account_code_fix_email),
                TTButtonStyle.Outline,
                enabled = !working,
                onClick = {
                    sentTo = null
                    error = null
                    notice = null
                },
            )
        }
    }
}
