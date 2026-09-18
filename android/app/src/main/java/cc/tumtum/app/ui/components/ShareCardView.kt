package cc.tumtum.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import cc.tumtum.app.domain.HrSample
import cc.tumtum.app.domain.Skin
import cc.tumtum.app.ui.theme.InstrumentSans
import cc.tumtum.app.ui.theme.TT
import java.time.Instant

/**
 * O card 9:16 — a unidade social do TumTum (componente ShareCard do design system).
 * Número sempre preto; no preto, rosa. Nunca rosa fora do preto.
 */
@Composable
fun ShareCardView(
    skin: Skin,
    title: String,
    bpm: Int,
    meta: String,
    width: Dp,
    modifier: Modifier = Modifier,
    chip: String? = null,
    curveSamples: List<HrSample>? = null,
    curveWindow: Pair<Instant, Instant>? = null,
) {
    val bg = skinColor(skin)
    val fg = if (skin == Skin.BLACK) TT.Paper else TT.Ink
    val num = if (skin == Skin.BLACK) TT.Rose else TT.Ink
    val pad = width * 0.09f
    val w = width.value

    Column(
        modifier
            .width(width)
            .aspectRatio(9f / 16f)
            .background(bg)
            .let { if (skin == Skin.WHITE) it.border(1.dp, TT.Gray10) else it }
            .padding(pad),
    ) {
        if (chip != null) {
            Text(
                chip,
                style = TextStyle(
                    fontFamily = InstrumentSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = maxOf(8f, w * 0.038f).sp,
                    letterSpacing = 0.14.em,
                ),
                color = TT.Ink,
                modifier = Modifier.background(TT.Acid).padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
        Spacer(Modifier.weight(1f))
        Text(
            title,
            style = TextStyle(
                fontFamily = InstrumentSans,
                fontWeight = FontWeight.Bold,
                fontSize = (w * 0.062f).sp,
                lineHeight = (w * 0.062f * 1.06f).sp,
            ),
            color = fg,
        )
        Text(
            "$bpm",
            style = TextStyle(
                fontFamily = InstrumentSans,
                fontWeight = FontWeight.Bold,
                fontSize = (w * 0.36f).sp,
                lineHeight = (w * 0.36f * 0.9f).sp,
                letterSpacing = (-0.05).em,
            ),
            color = num,
            modifier = Modifier.padding(top = (w * 0.045f).dp),
        )
        if (curveSamples != null && curveWindow != null && curveSamples.isNotEmpty()) {
            BpmCurve(
                samples = curveSamples,
                windowStart = curveWindow.first,
                windowEnd = curveWindow.second,
                lineColor = if (skin == Skin.BLACK) TT.DataLineOnDark else TT.DataLineOnLight,
                markerColor = if (skin == Skin.BLACK) TT.DataMarkerOnDark else TT.DataMarkerOnLight,
                gapColor = if (skin == Skin.BLACK) TT.DataGap else TT.Gray25,
                strokeWidth = 1.5.dp,
                markerRadius = 3.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height((w * 0.19f).dp)
                    .padding(top = (w * 0.05f).dp),
            )
        }
        Row(
            Modifier.fillMaxWidth().padding(top = (w * 0.04f).dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                meta,
                style = TextStyle(
                    fontFamily = InstrumentSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = maxOf(10f, w * 0.048f).sp,
                ),
                color = fg,
            )
            Wordmark(width = (w * 0.24f).dp, onDark = skin == Skin.BLACK)
        }
    }
}
