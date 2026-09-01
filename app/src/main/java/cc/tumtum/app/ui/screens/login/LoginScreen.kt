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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import cc.tumtum.app.R
import cc.tumtum.app.data.prefs.Account
import cc.tumtum.app.ui.components.TTButton
import cc.tumtum.app.ui.components.TTButtonStyle
import cc.tumtum.app.ui.components.TTField
import cc.tumtum.app.ui.components.Wordmark
import cc.tumtum.app.ui.nav.Routes
import cc.tumtum.app.ui.nav.appContainer
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType
import kotlinx.coroutines.launch

/**
 * Entrar — para quem já tem conta. Sem backend ainda (§2 do handoff), a
 * "autenticação" abre a sessão local com o @ digitado, no mesmo espírito do
 * FakeSocialRepository; quando o backend existir, este é o único ponto a trocar.
 */
@Composable
fun LoginScreen(nav: NavHostController) {
    val container = appContainer()
    val scope = rememberCoroutineScope()
    var handle by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var saving by androidx.compose.runtime.remember { mutableStateOf(false) }

    val handleClean = handle.trim().removePrefix("@").lowercase()
    val valid = handleClean.length >= 3 && password.length >= 4

    Column(
        Modifier
            .fillMaxSize()
            .background(TT.Paper)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 28.dp, end = 28.dp, top = 22.dp, bottom = 30.dp),
    ) {
        Text(
            "←",
            style = TTType.ItemSub.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
            color = TT.Gray45,
            modifier = Modifier.clickable { nav.popBackStack() }.padding(4.dp),
        )
        Spacer(Modifier.height(24.dp))
        Wordmark(width = 92.dp)
        Spacer(Modifier.height(40.dp))
        Text(stringResource(R.string.login_title), style = TTType.Title, color = TT.Ink)
        Spacer(Modifier.height(6.dp))
        Text(stringResource(R.string.login_subtitle), style = TTType.Body, color = TT.Gray45)
        Spacer(Modifier.height(30.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TTField(
                stringResource(R.string.login_handle_label),
                handle,
                { handle = it },
                placeholder = "@voce",
                keyboardType = KeyboardType.Email,
            )
            TTField(stringResource(R.string.account_password_label), password, { password = it }, isPassword = true)
        }
        Spacer(Modifier.height(40.dp))
        TTButton(
            stringResource(R.string.login_cta),
            TTButtonStyle.Rose,
            enabled = valid && !saving,
            onClick = {
                saving = true
                scope.launch {
                    val email = handle.trim().takeIf { "@" in it && "." in it } ?: ""
                    container.prefs.createAccount(
                        Account(name = handleClean, username = handleClean, email = email, tribes = emptySet()),
                    )
                    nav.navigate(Routes.Permission)
                    saving = false
                }
            },
        )
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.login_note), style = TTType.Footnote, color = TT.Gray45)
    }
}
