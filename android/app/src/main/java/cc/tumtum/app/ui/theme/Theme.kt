package cc.tumtum.app.ui.theme

import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.TextStyle

/**
 * O app não é Material — o Material3 entra só como andaime (sheets, ripples).
 * Todo desenho de superfície, tipo e cor vem dos tokens TT/TTType.
 * Tema único claro: as telas de show (a2/a3) pintam o próprio fundo #0A0A0A/preto.
 */
private val Scheme = lightColorScheme(
    primary = TT.Ink,
    onPrimary = TT.Paper,
    secondary = TT.Rose,
    onSecondary = TT.Ink,
    tertiary = TT.Acid,
    onTertiary = TT.Ink,
    background = TT.Paper,
    onBackground = TT.Ink,
    surface = TT.Paper,
    onSurface = TT.Ink,
    surfaceVariant = TT.Paper,
    onSurfaceVariant = TT.Gray70,
    outline = TT.Gray25,
    outlineVariant = TT.Gray10,
    error = TT.Ink,
    onError = TT.Paper,
)

@Composable
fun TumTumTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = Scheme,
        typography = androidx.compose.material3.Typography(
            bodyLarge = TTType.Body,
            bodyMedium = TTType.Body,
            labelLarge = TTType.Button,
        ),
    ) {
        CompositionLocalProvider(
            LocalTextSelectionColors provides TextSelectionColors(
                handleColor = TT.Rose,
                backgroundColor = TT.Rose.copy(alpha = 0.35f),
            ),
            content = content,
        )
    }
}

/** Estilo base para campos de texto sem chrome do Material. */
val FieldTextStyle: TextStyle
    @Composable get() = TTType.Body.copy(color = TT.Ink)
