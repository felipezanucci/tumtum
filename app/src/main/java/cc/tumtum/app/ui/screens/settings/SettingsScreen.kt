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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Row
import androidx.navigation.NavHostController
import cc.tumtum.app.R
import cc.tumtum.app.data.AvatarStore
import cc.tumtum.app.domain.Skin
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

    val granted by produceState(initialValue = false) {
        value = container.health.hasPermission()
    }
    val user by container.prefs.state.collectAsStateWithLifecycle(initialValue = null)
    var participantDraft by remember { mutableStateOf("") }
    var nameDraft by remember { mutableStateOf("") }
    var draftsLoaded by remember { mutableStateOf(false) }
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
        Text(
            "←",
            style = TTType.ItemSub.copy(fontSize = 14.sp),
            color = TT.Gray45,
            modifier = Modifier.clickable { nav.popBackStack() }.padding(4.dp),
        )
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
            onValueChange = {
                nameDraft = it
                scope.launch { container.prefs.setName(it) }
            },
        )

        Spacer(Modifier.height(40.dp))
        // §10 — sensor BLE: parear/trocar/remover também depois do onboarding.
        SensorSection(prefs = container.prefs, bleName = user?.bleName)

        Spacer(Modifier.height(40.dp))
        Text(stringResource(R.string.settings_participant_section), style = TTType.Meta, color = TT.Gray70)
        Spacer(Modifier.height(10.dp))
        Text(stringResource(R.string.settings_participant_hint), style = TTType.Footnote, color = TT.Gray45)
        Spacer(Modifier.height(10.dp))
        TTField(
            label = stringResource(R.string.participant_label),
            value = participantDraft,
            onValueChange = {
                participantDraft = it
                scope.launch { container.prefs.setParticipantId(it) }
            },
            placeholder = "P01",
        )

        Spacer(Modifier.height(40.dp))
        Text(stringResource(R.string.settings_account_section), style = TTType.Meta, color = TT.Gray70)
        Spacer(Modifier.height(10.dp))
        Text(stringResource(R.string.settings_delete_warning), style = TTType.Footnote, color = TT.Gray45)
        Spacer(Modifier.height(14.dp))
        TTButton(
            stringResource(R.string.settings_delete),
            TTButtonStyle.Outline,
            onClick = { confirmDelete = true },
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
                Text(stringResource(R.string.settings_delete_warning), style = TTType.Body, color = TT.Gray70)
            },
            confirmButton = {
                Text(
                    stringResource(R.string.settings_delete_confirm),
                    style = TTType.Button.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                    color = TT.Ink,
                    modifier = Modifier
                        .clickable {
                            confirmDelete = false
                            scope.launch {
                                container.nights.wipeAll()
                                container.prefs.wipe()
                                nav.navigate(Routes.Onboarding) { popUpTo(0) { inclusive = true } }
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
