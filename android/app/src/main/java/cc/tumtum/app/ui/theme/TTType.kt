package cc.tumtum.app.ui.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import cc.tumtum.app.R

/**
 * Tipografia §3 — Instrument Sans para toda a UI, pesos 400/500/600/700.
 * (Mutante Pop nunca entra na UI; os pôsteres de evento são assets.)
 */
@OptIn(ExperimentalTextApi::class)
val InstrumentSans = FontFamily(
    Font(
        R.font.instrument_sans_var,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400)),
    ),
    Font(
        R.font.instrument_sans_var,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500)),
    ),
    Font(
        R.font.instrument_sans_var,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600)),
    ),
    Font(
        R.font.instrument_sans_var,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700)),
    ),
    Font(
        R.font.instrument_sans_italic_var,
        weight = FontWeight.Normal,
        style = FontStyle.Italic,
        variationSettings = FontVariation.Settings(FontVariation.weight(400)),
    ),
)

private val NoPad = PlatformTextStyle(includeFontPadding = false)
private val Trim = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

private fun tt(base: TextStyle) = base.copy(
    fontFamily = InstrumentSans,
    platformStyle = NoPad,
    lineHeightStyle = Trim,
)

/** Papéis compostos (README §3 + tokens/typography.css). */
object TTType {
    /** Título de tela: 27–31px / 700 / tracking −0.02em. */
    val Title = tt(TextStyle(fontSize = 31.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.02).em, lineHeight = 34.sp))
    val TitleSmall = tt(TextStyle(fontSize = 27.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.02).em, lineHeight = 31.sp))

    /** Headline gritada do onboarding/galera. */
    val Shout = tt(TextStyle(fontSize = 44.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.03).em, lineHeight = 44.sp))
    val ShoutSmall = tt(TextStyle(fontSize = 29.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.025).em, lineHeight = 30.sp))

    /** Corpo: 15px / 1.5. */
    val Body = tt(TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Normal, lineHeight = 22.5.sp))
    val BodySmall = tt(TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Normal, lineHeight = 19.sp))
    val Footnote = tt(TextStyle(fontSize = 12.5.sp, fontWeight = FontWeight.Normal, lineHeight = 18.5.sp))

    /** Meta-label: 12px / 600 / tracking 0.08em / caixa alta. */
    val Meta = tt(TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.08.em))
    val MetaSmall = tt(TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.08.em))
    /** Rótulo de seção no preto (a2/a3): 11px / .2em. */
    val MetaWide = tt(TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.2.em))

    /** Label de botão: 16/600. */
    val Button = tt(TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold))

    /** Número herói: 64–120px / 700 / tracking −0.04em (o número é sempre número). */
    val Hero = tt(TextStyle(fontSize = 118.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.055).em, lineHeight = 92.sp))
    val HeroLive = tt(TextStyle(fontSize = 140.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.05).em, lineHeight = 112.sp))
    val HeroSmall = tt(TextStyle(fontSize = 64.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.04).em, lineHeight = 52.sp))

    val NumberLarge = tt(TextStyle(fontSize = 40.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.04).em))
    val NumberMedium = tt(TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold))
    val NumberRow = tt(TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold))

    val ItemTitle = tt(TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold))
    val ItemSub = tt(TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Normal))
    val CardShout = tt(TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, lineHeight = 14.sp))
}
