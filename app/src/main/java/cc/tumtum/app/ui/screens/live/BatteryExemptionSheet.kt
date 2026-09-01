package cc.tumtum.app.ui.screens.live

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cc.tumtum.app.R
import cc.tumtum.app.service.BatteryExemption
import cc.tumtum.app.ui.components.TTButton
import cc.tumtum.app.ui.components.TTButtonStyle
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType

/**
 * §6 — a sessão não começa sem a isenção de otimização de bateria.
 * Melhor recusar a captura do que produzir dado incompleto sem ninguém perceber.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryExemptionSheet(onDismiss: () -> Unit, onExempt: () -> Unit) {
    val context = LocalContext.current
    val steps = remember { BatteryExemption.manufacturerSteps() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = TT.Paper,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(start = 28.dp, end = 28.dp, bottom = 40.dp)) {
            Text(stringResource(R.string.battery_title), style = TTType.TitleSmall, color = TT.Ink)
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.battery_body), style = TTType.Body, color = TT.Gray70)
            if (steps != null) {
                Spacer(Modifier.height(12.dp))
                Text(steps, style = TTType.Footnote, color = TT.Gray70)
            }
            Spacer(Modifier.height(24.dp))
            TTButton(
                stringResource(R.string.battery_allow),
                TTButtonStyle.Ink,
                onClick = {
                    runCatching { context.startActivity(BatteryExemption.requestIntent(context)) }
                },
            )
            Spacer(Modifier.height(10.dp))
            TTButton(
                stringResource(R.string.battery_check),
                TTButtonStyle.Outline,
                onClick = { if (BatteryExemption.isExempt(context)) onExempt() },
            )
        }
    }
}
