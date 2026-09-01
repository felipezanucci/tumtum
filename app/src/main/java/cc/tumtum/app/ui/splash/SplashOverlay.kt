package cc.tumtum.app.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import cc.tumtum.app.ui.components.Wordmark
import cc.tumtum.app.ui.theme.TT
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * §4 — o app abre batendo. Fundo #FF6F91, wordmark preto a 46% da largura,
 * centro óptico. Duas frases de tum-tum e acabou; corte seco, sem fade.
 *
 *  0–120ms   rosa já pintado (windowBackground do theme de splash)
 *  120–460   entrada: opacidade 0→1, escala 0,82 → 1,045 → 1,00
 *  460–1410  frase 1 (950ms): 1,00 → 1,06 (66) → 1,00 (170) → 1,035 (247) → 1,00 (360)
 *  1410–2360 frase 2 — a mesma, uma vez só
 *  saída     no fim da frase 2; se a sessão não resolveu, rosa parado até resolver
 *
 * A batida só existe em escala. Com "reduzir animação": estático, saída em 800ms.
 */
@Composable
fun SplashOverlay(
    reduceMotion: Boolean,
    sessionResolved: Boolean,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val resolved by rememberUpdatedState(sessionResolved)
    val entry = remember { Animatable(if (reduceMotion) 1f else 0f) } // progresso da entrada (alpha)
    val scale = remember { Animatable(if (reduceMotion) 1f else 0.82f) }

    LaunchedEffect(Unit) {
        if (reduceMotion) {
            delay(800)
            snapshotFlow { resolved }.first { it }
            onFinished()
            return@LaunchedEffect
        }
        delay(120)
        // Entrada com leve overshoot — cubic-bezier(.2,.9,.25,1)
        val entryEasing = CubicBezierEasing(0.2f, 0.9f, 0.25f, 1f)
        coroutineScope {
            launch { entry.animateTo(1f, keyframes { durationMillis = 200 }) }
            launch {
                scale.animateTo(
                    1f,
                    keyframes {
                        durationMillis = 340
                        0.82f at 0
                        1.045f at 190 using entryEasing
                        1f at 340 using entryEasing
                    },
                )
            }
        }
        // Duas frases de batida — nunca loop infinito.
        repeat(2) {
            scale.animateTo(
                1f,
                keyframes {
                    durationMillis = 950
                    1f at 0
                    1.06f at 66 using FastOutSlowInEasing
                    1f at 170
                    1.035f at 247
                    1f at 360
                    1f at 950
                },
            )
        }
        // Se a sessão ainda não resolveu, o rosa fica parado — sem batida, sem reinício.
        snapshotFlow { resolved }.first { it }
        onFinished() // corte seco: quem remove a camada é o chamador, sem fade
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(TT.Rose),
        contentAlignment = Alignment.Center,
    ) {
        Wordmark(
            width = maxWidth * 0.46f,
            onDark = false,
            modifier = Modifier.graphicsLayer {
                alpha = entry.value
                scaleX = scale.value
                scaleY = scale.value
            },
        )
    }
}
