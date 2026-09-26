package cc.tumtum.app.ui.screens.consent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cc.tumtum.app.R
import cc.tumtum.app.domain.ConsentText
import cc.tumtum.app.ui.components.TTButton
import cc.tumtum.app.ui.components.TTButtonStyle
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType
import cc.tumtum.app.ui.theme.TumTumTheme

/**
 * What Health Connect opens when someone asks, from its own settings, why
 * TumTum wants their heart rate (`ACTION_SHOW_PERMISSIONS_RATIONALE` up to
 * Android 13, `VIEW_PERMISSION_USAGE` from 14). Until 26/09 both pointed at
 * MainActivity, which ignored them: the person asked "why?" and got the feed.
 *
 * A plain page, on purpose — no account, no navigation, nothing to tap but
 * the policy: the same explanation the consent screen gives, the list of
 * purposes, and the link.
 */
class ConsentRationaleActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TumTumTheme {
                val context = LocalContext.current
                Column(
                    Modifier
                        .fillMaxSize()
                        .background(TT.Night)
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(start = 28.dp, end = 28.dp, top = 34.dp, bottom = 30.dp),
                ) {
                    Text(stringResource(R.string.consent_label), style = TTType.MetaWide, color = TT.Gray45)
                    Spacer(Modifier.height(14.dp))
                    Text(stringResource(R.string.rationale_title), style = TTType.TitleSmall, color = TT.Paper)
                    Spacer(Modifier.height(18.dp))
                    ConsentIntro(onDark = true)
                    Spacer(Modifier.height(22.dp))
                    Text(stringResource(R.string.rationale_purposes), style = TTType.Meta, color = TT.Gray45)
                    Spacer(Modifier.height(8.dp))
                    ConsentText.PURPOSES.forEach { purpose ->
                        ConsentCopy.of(purpose)?.let { copy ->
                            Spacer(Modifier.height(10.dp))
                            Text(stringResource(copy.title), style = TTType.ItemTitle, color = TT.Paper)
                            Spacer(Modifier.height(2.dp))
                            Text(stringResource(copy.body), style = TTType.BodySmall, color = TT.Gray45)
                        }
                    }
                    Spacer(Modifier.height(22.dp))
                    Text(stringResource(R.string.rationale_where), style = TTType.BodySmall, color = TT.Gray45)
                    Spacer(Modifier.height(18.dp))
                    TTButton(
                        stringResource(R.string.rationale_policy),
                        TTButtonStyle.Rose,
                        onClick = { openLink(context, ConsentText.PRIVACY_URL) },
                    )
                    Spacer(Modifier.height(10.dp))
                    TTButton(
                        stringResource(R.string.rationale_close),
                        TTButtonStyle.OutlineOnDark,
                        onClick = { finish() },
                    )
                }
            }
        }
    }
}
