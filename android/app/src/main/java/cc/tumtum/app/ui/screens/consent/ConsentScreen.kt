package cc.tumtum.app.ui.screens.consent

import androidx.compose.foundation.background
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cc.tumtum.app.R
import cc.tumtum.app.data.api.TumtumApi
import cc.tumtum.app.domain.BirthDate
import cc.tumtum.app.domain.ConsentText
import cc.tumtum.app.ui.components.BackArrow
import cc.tumtum.app.ui.components.TTButton
import cc.tumtum.app.ui.components.TTButtonStyle
import cc.tumtum.app.ui.components.WheelBirthDateField
import cc.tumtum.app.ui.nav.Routes
import cc.tumtum.app.ui.nav.appContainer
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType
import java.io.IOException
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Where the screen stands with the server: it never shows a switch it did not read. */
private sealed interface ConsentLoad {
    data object Loading : ConsentLoad
    data object Ready : ConsentLoad
    data object SignedOut : ConsentLoad
    data class Failed(val offline: Boolean) : ConsentLoad
}

/**
 * O consentimento (LGPD remediation, 26/09) — the screen before the Health
 * Connect dialog, and the gate after a sign-in that lacks the Terms or a
 * birth date.
 *
 * Quiet and careful, as every consent screen: black canvas, body type, no
 * joke. It says what the data is, the window, how long things are kept, that
 * TumTum is not a medical device, that nobody else gets it, and where to take
 * it back — then one switch per purpose. The two the core loop needs are
 * explained as such and still each wait for their own tap; the five optional
 * ones start off and stay off unless the person turns them on.
 *
 * The switches start from what the server recorded, never from a guess: if
 * the server cannot be asked, the screen says so and offers to try again —
 * sending switches it never read would revoke what the person had granted.
 *
 * @param focus a purpose the app needs right now (the server answered
 *   `consent_required`, or a button asked for it): that row comes first,
 *   framed, and "Continuar" returns to where the person was.
 * @param sendNightId a night waiting for `keep_night`: once it is on, the
 *   night goes up without a second tap.
 */
@Composable
fun ConsentScreen(nav: NavHostController, focus: String?, sendNightId: Long?) {
    val container = appContainer()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var load by remember { mutableStateOf<ConsentLoad>(ConsentLoad.Loading) }
    var tick by remember { mutableStateOf(0) }
    var switches by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
    var needsBirth by remember { mutableStateOf(false) }
    var birth by remember { mutableStateOf<LocalDate?>(null) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val focusKey = focus?.takeIf { it in ConsentText.PURPOSES }

    LaunchedEffect(tick) {
        load = ConsentLoad.Loading
        if (container.prefs.state.first().session == null) {
            load = ConsentLoad.SignedOut
            return@LaunchedEffect
        }
        load = try {
            val me = container.api.me()
            val consents = container.api.getConsents()
            needsBirth = me.birthDate == null
            switches = ConsentText.startingSwitches(consents.asMap())
            ConsentLoad.Ready
        } catch (e: TumtumApi.ApiException) {
            if (e.code == 401) ConsentLoad.SignedOut else ConsentLoad.Failed(offline = false)
        } catch (e: IOException) {
            ConsentLoad.Failed(offline = true)
        }
    }

    /** Where "Continuar" leads, once the server has the choices. */
    suspend fun after(granted: Map<String, Boolean>) {
        if (sendNightId != null && granted[ConsentText.KEEP_NIGHT] == true) {
            container.sync.sendAfterConsent(sendNightId)
        }
        val wantsReading = granted[ConsentText.READ_HEART_RATE] == true &&
            container.health.isAvailable && !container.health.hasPermission()
        when {
            // The first pass (after an account, a first sign-in, or the gate):
            // the Health Connect dialog next only if reading was turned on.
            focusKey == null -> if (wantsReading) {
                nav.navigate(Routes.Permission)
            } else {
                container.prefs.setOnboarded()
                nav.navigate(Routes.Feed) { popUpTo(0) { inclusive = true } }
            }
            // Opened on the way to connecting a watch: with reading on, the
            // way continues to the permission screen and the setup after it.
            focusKey == ConsentText.READ_HEART_RATE && granted[ConsentText.READ_HEART_RATE] == true -> {
                nav.popBackStack()
                nav.navigate(Routes.Permission)
            }
            else -> if (!nav.popBackStack()) nav.navigate(Routes.Feed)
        }
    }

    fun proceed() {
        error = null
        val chosenBirth = birth
        if (needsBirth) {
            if (chosenBirth == null) {
                error = context.getString(R.string.consent_birth_missing)
                return
            }
            if (!BirthDate.isAdult(chosenBirth, LocalDate.now())) {
                error = context.getString(R.string.age_under_18)
                return
            }
        }
        // An account exists only with the Terms agreed; the first pass cannot end without them.
        if (focusKey == null && switches[ConsentText.TERMS] != true) {
            error = context.getString(R.string.consent_terms_needed)
            return
        }
        saving = true
        scope.launch {
            try {
                if (needsBirth && chosenBirth != null) {
                    container.api.patchBirthDate(chosenBirth)
                    needsBirth = false
                }
                val saved = container.api.putConsents(switches, ConsentText.MEANS_TAP)
                val granted = saved.asMap()
                switches = ConsentText.startingSwitches(granted)
                after(granted)
            } catch (e: TumtumApi.ApiException) {
                error = when {
                    e.code == 401 -> context.getString(R.string.consent_failed_expired)
                    // The server's own sentence (under 18, a date already set…),
                    // unless it is a validation list rather than a sentence.
                    e.code == 422 && !e.detail.trimStart().startsWith("[") && !e.detail.trimStart().startsWith("{") -> e.detail
                    else -> context.getString(R.string.consent_failed_server, e.detail)
                }
            } catch (e: IOException) {
                error = context.getString(R.string.consent_failed_offline)
            } finally {
                saving = false
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(TT.Night)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 28.dp, end = 28.dp, top = 22.dp, bottom = 30.dp),
    ) {
        if (focusKey != null) {
            BackArrow(onClick = { nav.popBackStack() }, onDark = true)
            Spacer(Modifier.height(18.dp))
        }
        Text(stringResource(R.string.consent_label), style = TTType.MetaWide, color = TT.Gray45)
        Spacer(Modifier.height(14.dp))
        Text(stringResource(R.string.consent_title), style = TTType.TitleSmall, color = TT.Paper)
        Spacer(Modifier.height(18.dp))
        ConsentIntro(onDark = true)
        Spacer(Modifier.height(26.dp))

        when (val l = load) {
            ConsentLoad.Loading -> Text(stringResource(R.string.consent_loading), style = TTType.BodySmall, color = TT.Gray45)
            ConsentLoad.SignedOut -> {
                Text(stringResource(R.string.consent_signed_out), style = TTType.Body, color = TT.Paper)
                Spacer(Modifier.height(16.dp))
                TTButton(stringResource(R.string.settings_sign_in), TTButtonStyle.Rose, onClick = { nav.navigate(Routes.Login) })
            }
            is ConsentLoad.Failed -> {
                Text(
                    stringResource(if (l.offline) R.string.consent_load_offline else R.string.consent_load_failed),
                    style = TTType.Body,
                    color = TT.Paper,
                )
                Spacer(Modifier.height(16.dp))
                TTButton(stringResource(R.string.events_retry), TTButtonStyle.Rose, onClick = { tick++ })
            }
            ConsentLoad.Ready -> {
                if (needsBirth) {
                    WheelBirthDateField(
                        label = stringResource(R.string.consent_birth_label),
                        value = birth,
                        onChange = { birth = it; error = null },
                        onDark = true,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(stringResource(R.string.consent_birth_note), style = TTType.Footnote, color = TT.Gray45)
                    Spacer(Modifier.height(26.dp))
                }

                fun toggle(purpose: String, value: Boolean) {
                    switches = switches + (purpose to value)
                    error = null
                }

                if (focusKey != null) {
                    Text(stringResource(R.string.consent_section_focus), style = TTType.Meta, color = TT.Acid)
                    Spacer(Modifier.height(6.dp))
                    ConsentRow(
                        purpose = focusKey,
                        on = switches[focusKey] == true,
                        onToggle = { toggle(focusKey, it) },
                        onDark = true,
                        focused = true,
                    )
                    Spacer(Modifier.height(22.dp))
                }

                Text(stringResource(R.string.consent_section_core), style = TTType.Meta, color = TT.Gray45)
                Spacer(Modifier.height(4.dp))
                ConsentText.PURPOSES.filter { it in ConsentText.CORE && it != focusKey }.forEach { p ->
                    ConsentRow(purpose = p, on = switches[p] == true, onToggle = { toggle(p, it) }, onDark = true)
                }
                Spacer(Modifier.height(18.dp))
                Text(stringResource(R.string.consent_section_optional), style = TTType.Meta, color = TT.Gray45)
                Spacer(Modifier.height(4.dp))
                ConsentText.PURPOSES.filter { ConsentText.isOptional(it) && it != focusKey }.forEach { p ->
                    ConsentRow(purpose = p, on = switches[p] == true, onToggle = { toggle(p, it) }, onDark = true)
                }

                Spacer(Modifier.height(14.dp))
                PolicyLinks(onDark = true)
                Text(
                    stringResource(R.string.consent_version, ConsentText.VERSION),
                    style = TTType.Footnote,
                    color = TT.Gray55,
                )
                Spacer(Modifier.height(24.dp))
                TTButton(
                    stringResource(if (saving) R.string.consent_saving else R.string.consent_continue),
                    TTButtonStyle.Rose,
                    enabled = !saving,
                    onClick = { proceed() },
                )
                error?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(it, style = TTType.Body, color = TT.Rose)
                }
            }
        }
    }
}
