package cc.tumtum.app.ui.screens.account

import android.util.Patterns
import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import androidx.compose.ui.unit.em
import androidx.navigation.NavHostController
import cc.tumtum.app.R
import cc.tumtum.app.data.repo.afterSignIn
import cc.tumtum.app.data.api.SignupCode
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Handles reservados no repositório fake — a "checagem de disponibilidade" local. */
private val TAKEN = setOf("mariana", "rodcosta", "jureis", "pbarros", "ltoledo", "tumtum")

private val TRIBES = listOf("SHOWS", "FUTEBOL", "FESTIVAIS")

/**
 * b2 — Criar conta. Leva menos que uma música.
 *
 * Since 2026-09-18 (Etapa 1) the account is created on the server first and
 * kept locally second: the @ and the tribes stay on the
 * phone, the e-mail, name and password become a real account with a token.
 * Without the server there is no account — the screen says so instead of
 * pretending.
 *
 * **Since 24/09 (#64) it takes two steps.** Test 7 made an account with
 * `teste@teste.com`, and Felipe's rule is that only a real address makes one.
 * No validator can know that, so the first step sends a 6-digit code to the
 * address and creates nothing; the second takes the code back, and only then
 * does the server create the account. Nothing on the phone changes before
 * that: a second account's profile takes over only with the confirmed code.
 *
 * **Since 25/09 a second account deletes nothing.** Each night belongs to
 * the account that recorded it; the previous person's are hidden while
 * another account is here and come back when theirs signs in. Creating an
 * account used to wipe every night on the phone — test 9's sealed nights
 * went that way, and their reveal alarms fired anyway.
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
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val user by container.prefs.state.collectAsStateWithLifecycle(initialValue = null)
    // A second account on the same phone (18/09): the profile is replaced and
    // the previous person's nights are hidden (25/09). Said before the tap.
    val replacing = user?.account

    // The second step (#64): the address the code went to, or null while the
    // form is showing. What the server answered, not what was typed.
    var codeSentTo by rememberSaveable { mutableStateOf<String?>(null) }
    var codeMinutes by rememberSaveable { mutableStateOf(15) }
    var code by rememberSaveable { mutableStateOf("") }
    var resendAt by rememberSaveable { mutableStateOf(0L) }
    var notice by remember { mutableStateOf<String?>(null) }

    val usernameClean = username.trim().lowercase()
    val usernameTaken = usernameClean in TAKEN
    val emailLooksWhole = Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val valid = name.isNotBlank() && usernameClean.length >= 3 && !usernameTaken &&
        emailLooksWhole && password.length >= 8

    /** Ask the server to mail a code: the first time, and on "Mandar outro código". */
    fun sendCode(again: Boolean) {
        saving = true
        error = null
        notice = null
        scope.launch {
            try {
                val started = container.api.signupStart(email = email.trim(), name = name.trim(), password = password)
                codeSentTo = started.email
                codeMinutes = started.expiresInMinutes
                code = ""
                resendAt = System.currentTimeMillis() + started.resendAfterSeconds * 1000L
                if (again) notice = context.getString(R.string.account_code_resent, started.email)
            } catch (e: Exception) {
                error = AuthErrors.messageFor(e, context)
            } finally {
                saving = false
            }
        }
    }

    // Back from the code step returns to the form, with everything still typed.
    BackHandler(enabled = codeSentTo != null && !saving) {
        codeSentTo = null
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
        Wordmark(width = 92.dp)
        Spacer(Modifier.height(40.dp))

        val sentTo = codeSentTo
        if (sentTo != null) {
            CodeStep(
                sentTo = sentTo,
                minutes = codeMinutes,
                replacingHandle = replacing?.username,
                code = code,
                onCode = { code = SignupCode.digits(it) },
                resendAt = resendAt,
                saving = saving,
                error = error,
                notice = notice,
                onDeclinedShort = { error = context.getString(R.string.account_code_short) },
                onDeclinedResend = { seconds ->
                    notice = null
                    error = context.getString(R.string.account_code_resend_wait, seconds)
                },
                onConfirm = {
                    saving = true
                    error = null
                    notice = null
                    scope.launch {
                        try {
                            container.api.signupConfirm(email = sentTo, code = code)
                            // Only now, with the account made, does the phone change.
                            val account = Account(name = name.trim(), username = usernameClean, email = sentTo, tribes = tribes)
                            if (replacing != null) {
                                // The previous person's profile, photo and sensor go;
                                // their nights stay on the phone, hidden, and come
                                // back when that account signs in (25/09). Until
                                // then a second account wiped every night here.
                                container.prefs.replaceAccount(account)
                            } else {
                                container.prefs.createAccount(account)
                            }
                            runCatching { container.api.me() }
                            container.afterSignIn()
                            nav.navigate(Routes.Permission)
                        } catch (e: Exception) {
                            error = AuthErrors.messageFor(e, context)
                        } finally {
                            saving = false
                        }
                    }
                },
                onResend = { sendCode(again = true) },
                onFixEmail = {
                    codeSentTo = null
                    error = null
                    notice = null
                },
            )
        } else {
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


            Spacer(Modifier.height(32.dp))
            // The tap sends a code and creates nothing yet (#64): said before it.
            Text(stringResource(R.string.account_code_explained), style = TTType.Footnote, color = TT.Gray45)
            Spacer(Modifier.height(10.dp))
            TTButton(
                if (saving) stringResource(R.string.auth_working) else stringResource(R.string.account_cta),
                TTButtonStyle.Rose,
                enabled = valid && !saving,
                onDeclined = {
                    if (!saving) {
                        error = context.getString(
                            when {
                                name.isBlank() -> R.string.form_missing_name
                                usernameClean.length < 3 -> R.string.form_short_username
                                usernameTaken -> R.string.form_username_taken
                                email.isBlank() -> R.string.form_missing_email
                                !email.contains("@") -> R.string.form_email_without_at
                                !emailLooksWhole -> R.string.form_email_incomplete
                                password.isEmpty() -> R.string.form_missing_password
                                else -> R.string.form_short_password
                            },
                        )
                    }
                },
                onClick = { sendCode(again = false) },
            )
            error?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, style = TTType.Body, color = TT.Rose)
            }
        }
    }
}

/**
 * The second step (#64): the code from the e-mail. Quiet and careful, like
 * every account screen — it says where the code went, how long it lives, what
 * to do when it does not arrive, and how to fix the address, and it never
 * leaves a declined tap unexplained.
 */
@Composable
private fun CodeStep(
    sentTo: String,
    minutes: Int,
    replacingHandle: String?,
    code: String,
    onCode: (String) -> Unit,
    resendAt: Long,
    saving: Boolean,
    error: String?,
    notice: String?,
    onDeclinedShort: () -> Unit,
    onDeclinedResend: (Int) -> Unit,
    onConfirm: () -> Unit,
    onResend: () -> Unit,
    onFixEmail: () -> Unit,
) {
    // The wait before another code, counted down from the server's own number.
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(resendAt) {
        now = System.currentTimeMillis()
        while (now < resendAt) {
            delay(1_000)
            now = System.currentTimeMillis()
        }
    }
    val waitSeconds = SignupCode.secondsUntil(resendAt, now)

    Text(stringResource(R.string.account_code_title), style = TTType.Title, color = TT.Ink)
    Spacer(Modifier.height(10.dp))
    Text(stringResource(R.string.account_code_body, sentTo, minutes), style = TTType.Body, color = TT.Ink)
    replacingHandle?.let {
        Spacer(Modifier.height(14.dp))
        Text(stringResource(R.string.account_replaces, it), style = TTType.BodySmall, color = TT.Ink)
    }
    Spacer(Modifier.height(26.dp))
    TTField(
        stringResource(R.string.account_code_label),
        code,
        onCode,
        placeholder = "000000",
        keyboardType = KeyboardType.NumberPassword,
    )
    Spacer(Modifier.height(8.dp))
    Text(stringResource(R.string.account_code_spam), style = TTType.Footnote, color = TT.Gray45)

    Spacer(Modifier.height(28.dp))
    TTButton(
        if (saving) stringResource(R.string.auth_working) else stringResource(R.string.account_code_cta),
        TTButtonStyle.Rose,
        enabled = SignupCode.isComplete(code) && !saving,
        onDeclined = { if (!saving) onDeclinedShort() },
        onClick = onConfirm,
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
        enabled = waitSeconds == 0 && !saving,
        onDeclined = { if (!saving && waitSeconds > 0) onDeclinedResend(waitSeconds) },
        onClick = onResend,
    )
    Spacer(Modifier.height(10.dp))
    TTButton(
        stringResource(R.string.account_code_fix_email),
        TTButtonStyle.Outline,
        enabled = !saving,
        onClick = onFixEmail,
    )
}
