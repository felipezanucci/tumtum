package cc.tumtum.app.ui.screens.live

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import cc.tumtum.app.R
import cc.tumtum.app.service.BatteryExemption
import cc.tumtum.app.ui.components.TTButton
import cc.tumtum.app.ui.components.TTButtonStyle
import cc.tumtum.app.ui.theme.TT
import cc.tumtum.app.ui.theme.TTType

/**
 * §6 — a sessão não começa sem a isenção de otimização de bateria.
 * Melhor recusar a captura do que produzir dado incompleto sem ninguém perceber.
 *
 * Tela de permissão, então a marca fica quieta e diz exatamente o que fazer:
 * qual permissão é, qual botão apertar aqui (o rosa) e qual apertar na janela
 * do Android (OK). Ao voltar do diálogo do sistema a folha confere sozinha —
 * se a isenção foi dada, segue; se não, diz que não foi, em vez de ficar muda.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryExemptionSheet(onDismiss: () -> Unit, onExempt: () -> Unit) {
    val context = LocalContext.current
    val steps = remember { BatteryExemption.manufacturerSteps() }
    var asked by remember { mutableStateOf(false) }
    var notYet by remember { mutableStateOf(false) }

    // Volta do diálogo do Android: confere sem pedir mais um toque.
    LifecycleResumeEffect(asked) {
        if (asked && BatteryExemption.isExempt(context)) onExempt()
        onPauseOrDispose { }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = TT.Paper,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(start = 28.dp, end = 28.dp, bottom = 40.dp)) {
            Text(stringResource(R.string.battery_title), style = TTType.TitleSmall, color = TT.Ink)
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.battery_body), style = TTType.Body, color = TT.Gray70)
            Spacer(Modifier.height(20.dp))
            Text(stringResource(R.string.battery_steps_label), style = TTType.Meta, color = TT.Gray55)
            Spacer(Modifier.height(8.dp))
            Step(1, stringResource(R.string.battery_step_1))
            Step(2, stringResource(R.string.battery_step_2))
            Step(3, stringResource(R.string.battery_step_3))
            if (steps != null) {
                Spacer(Modifier.height(12.dp))
                Text(steps, style = TTType.Footnote, color = TT.Gray70)
            }
            Spacer(Modifier.height(24.dp))
            TTButton(
                stringResource(R.string.battery_allow),
                TTButtonStyle.Rose,
                onClick = {
                    notYet = false
                    asked = true
                    runCatching { context.startActivity(BatteryExemption.requestIntent(context)) }
                },
            )
            Spacer(Modifier.height(10.dp))
            TTButton(
                stringResource(R.string.battery_check),
                TTButtonStyle.Outline,
                onClick = { if (BatteryExemption.isExempt(context)) onExempt() else notYet = true },
            )
            if (notYet) {
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.battery_not_yet), style = TTType.BodySmall, color = TT.Ink)
            }
        }
    }
}

@Composable
private fun Step(number: Int, text: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text("$number.", style = TTType.Body, color = TT.Ink)
        Spacer(Modifier.width(8.dp))
        Text(text, style = TTType.Body, color = TT.Ink)
    }
}
