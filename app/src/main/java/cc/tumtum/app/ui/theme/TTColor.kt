package cc.tumtum.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Fundação visual §3 — paleta institucional travada.
 * Regras rígidas (manual):
 *  - Número grande de BPM é sempre preto; sobre fundo preto é branco/rosa. Nunca rosa fora do preto.
 *  - Rosa e ácido são acento, não tapete. Máximo 2 cores de fundo por fluxo.
 *  - Nunca texto branco sobre rosa ou amarelo.
 */
object TT {
    val Ink = Color(0xFF000000)
    val Paper = Color(0xFFFFFFFF)
    val Rose = Color(0xFFFF6F91)
    val Acid = Color(0xFFEFFF00)
    val Night = Color(0xFF0A0A0A)

    // Rampa neutra (design system tokens/colors.css)
    val Gray70 = Color(0xFF4A4A4A)   // texto secundário
    val Gray55 = Color(0xFF6F6F6F)   // meta em fundo claro
    val Gray45 = Color(0xFF8A8A8A)   // texto terciário, placeholder
    val Gray25 = Color(0xFFB4B4B4)   // borda de campo em repouso
    val Gray10 = Color(0xFFE6E6E6)   // divisor, borda suave
    val Ink800 = Color(0xFF141414)
    val Ink700 = Color(0xFF1E1E1E)   // divisor no preto
    val Ink600 = Color(0xFF2E2E2E)   // borda no preto

    // Dataviz (tokens): vocabulário completo, sem zonas nem cores de risco.
    // No preto: linha rosa, marcador amarelo. No branco: linha preta, marcador rosa
    // (amarelo sobre branco some — 1,11:1).
    val DataLineOnDark = Rose
    val DataMarkerOnDark = Acid
    val DataLineOnLight = Ink
    val DataMarkerOnLight = Rose
    val DataSecondary = Gray25
    val DataGap = Gray70
}
