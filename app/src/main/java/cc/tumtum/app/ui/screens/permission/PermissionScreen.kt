package cc.tumtum.app.ui.screens.permission

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.PermissionController
import androidx.navigation.NavHostController
import cc.tumtum.app.R
import cc.tumtum.app.data.ble.BlePermissions
import cc.tumtum.app.ui.components.TTButton
import cc.tumtum.app.ui.components.TTButtonStyle
import cc.tumtum.app.ui.nav.Routes
import cc.tumtum.app.ui.nav.appContainer
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType
import kotlinx.coroutines.launch

/**
 * b3 — Permissão: a tela quieta. Botão preto, sem rosa, sem piada.
 * O pedido de permissão do Health Connect só acontece aqui, nunca antes.
 */
@Composable
fun PermissionScreen(nav: NavHostController) {
    val container = appContainer()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val hcAvailable = remember { container.health.isAvailable }
    var blePermitted by remember { mutableStateOf(BlePermissions.granted(context)) }

    val launcher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract(),
    ) { granted ->
        if (granted.containsAll(container.health.permissions)) {
            nav.navigate(Routes.SourcesSetup)
        }
        // Negou no diálogo do sistema: fica na tela quieta, sem insistir.
    }
    val bleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { blePermitted = BlePermissions.granted(context) }

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
        Spacer(Modifier.height(34.dp))
        Text(stringResource(R.string.perm_title), style = TTType.TitleSmall, color = TT.Ink)
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.perm_body), style = TTType.Body, color = TT.Gray70)
        Spacer(Modifier.height(26.dp))

        Divider()
        PermissionRow(stringResource(R.string.perm_do_1), does = true)
        Divider()
        PermissionRow(stringResource(R.string.perm_do_2), does = true)
        Divider()
        PermissionRow(stringResource(R.string.perm_dont_1), does = false)
        Divider()
        PermissionRow(stringResource(R.string.perm_dont_2), does = false)

        Spacer(Modifier.height(24.dp))
        // §5 — o bloco BLE, na mesma tela quieta: sensor no corpo é opcional e é outra permissão.
        Text(stringResource(R.string.perm_ble_section), style = TTType.Meta, color = TT.Gray70)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.perm_ble_body), style = TTType.BodySmall, color = TT.Gray70)
        Spacer(Modifier.height(12.dp))
        if (blePermitted) {
            Text(stringResource(R.string.perm_ble_granted), style = TTType.BodySmall, color = TT.Ink)
        } else {
            TTButton(
                stringResource(R.string.perm_ble_allow),
                TTButtonStyle.Outline,
                onClick = { bleLauncher.launch(BlePermissions.withNotifications()) },
            )
        }

        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.perm_footnote), style = TTType.Footnote, color = TT.Gray45)
        if (!hcAvailable) {
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.perm_hc_missing), style = TTType.Footnote, color = TT.Gray70)
        }

        Spacer(Modifier.height(30.dp))
        TTButton(
            stringResource(R.string.perm_allow),
            TTButtonStyle.Ink,
            enabled = hcAvailable,
            onClick = { launcher.launch(container.health.permissions) },
        )
        Spacer(Modifier.height(10.dp))
        // "Agora não" segue para o app; o vazio (a5) reconvida sem insistência.
        TTButton(
            stringResource(R.string.perm_not_now),
            TTButtonStyle.Outline,
            onClick = {
                scope.launch {
                    container.prefs.setOnboarded()
                    nav.navigate(Routes.Feed) { popUpTo(0) { inclusive = true } }
                }
            },
        )
    }
}

@Composable
private fun PermissionRow(text: String, does: Boolean) {
    Row(Modifier.fillMaxWidth().padding(vertical = 14.dp)) {
        Text(
            if (does) "✓" else "✕",
            style = TTType.BodySmall.copy(fontWeight = FontWeight.Bold),
            color = if (does) TT.Ink else TT.Gray45,
            modifier = Modifier.width(18.dp),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text,
            style = TTType.BodySmall.copy(fontSize = 14.sp),
            color = if (does) TT.Ink else TT.Gray70,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun Divider() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(TT.Gray10))
}
