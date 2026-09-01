package cc.tumtum.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cc.tumtum.app.domain.HrSample
import cc.tumtum.app.domain.NightAnalyzer
import java.time.Duration
import java.time.Instant

/**
 * A curva da noite. Regras (§7 + manual):
 *  - gap > 60s = interrupção da linha, com traço pontilhado na base. Zero interpolação.
 *  - no preto: linha rosa, marcador amarelo; no branco: linha preta, marcador rosa.
 *  - `progress` desenha da esquerda para a direita (a revela usa 0→1 em 1,2s);
 *    o marcador do pico só aparece quando a linha chega nele.
 */
@Composable
fun BpmCurve(
    samples: List<HrSample>,
    windowStart: Instant,
    windowEnd: Instant,
    lineColor: Color,
    markerColor: Color,
    modifier: Modifier = Modifier,
    secondarySamples: List<HrSample>? = null,
    secondaryColor: Color = Color.Unspecified,
    gapColor: Color = Color.Unspecified,
    strokeWidth: Dp = 2.dp,
    markerRadius: Dp = 4.5.dp,
    progress: Float = 1f,
    showMarker: Boolean = true,
) {
    Canvas(modifier) {
        val all = if (secondarySamples.isNullOrEmpty()) samples else samples + secondarySamples
        if (all.isEmpty()) return@Canvas
        val lo = (all.minOf { it.bpm } - 6).coerceAtLeast(30)
        val hi = (all.maxOf { it.bpm } + 6)
        val span = (hi - lo).coerceAtLeast(1)
        val totalMs = Duration.between(windowStart, windowEnd).toMillis().coerceAtLeast(1)
        val padTop = markerRadius.toPx() + 1f
        val padBottom = 6f

        fun x(t: Instant): Float =
            (Duration.between(windowStart, t).toMillis().toFloat() / totalMs) * size.width

        fun y(bpm: Int): Float =
            padTop + (1f - (bpm - lo).toFloat() / span) * (size.height - padTop - padBottom)

        fun segments(list: List<HrSample>): List<List<Offset>> {
            val sorted = list.sortedBy { it.time }
            val segs = mutableListOf<MutableList<Offset>>()
            var cur = mutableListOf<Offset>()
            var prev: HrSample? = null
            for (s in sorted) {
                val p = prev
                if (p != null && Duration.between(p.time, s.time).seconds > NightAnalyzer.GAP_THRESHOLD_SEC) {
                    if (cur.size >= 1) segs += cur
                    cur = mutableListOf()
                }
                cur += Offset(x(s.time), y(s.bpm))
                prev = s
            }
            if (cur.isNotEmpty()) segs += cur
            return segs
        }

        fun DrawScope.drawSeries(segs: List<List<Offset>>, color: Color, width: Float) {
            segs.forEach { seg ->
                if (seg.size == 1) {
                    drawCircle(color, radius = width / 2f, center = seg[0])
                } else {
                    val path = Path().apply {
                        moveTo(seg[0].x, seg[0].y)
                        for (i in 1 until seg.size) lineTo(seg[i].x, seg[i].y)
                    }
                    drawPath(path, color, style = Stroke(width, cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
            }
        }

        val clipRight = size.width * progress.coerceIn(0f, 1f)
        clipRect(right = clipRight) {
            secondarySamples?.takeIf { it.isNotEmpty() }?.let {
                drawSeries(segments(it), secondaryColor, strokeWidth.toPx() * 0.9f)
            }
            drawSeries(segments(samples), lineColor, strokeWidth.toPx())

            // Buracos: traço pontilhado na base, nunca linha inventada.
            if (gapColor.isSpecified && samples.isNotEmpty()) {
                val gapY = size.height - 2f
                NightAnalyzer.gaps(samples, windowStart, windowEnd).forEach { g ->
                    drawLine(
                        gapColor,
                        Offset(x(g.start), gapY),
                        Offset(x(g.end), gapY),
                        strokeWidth = 1.5f.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f.dp.toPx(), 7f.dp.toPx())),
                    )
                }
            }
        }

        if (showMarker && samples.isNotEmpty()) {
            val peak = samples.maxBy { it.bpm }
            val px = x(peak.time)
            if (px <= clipRight) {
                drawCircle(markerColor, radius = markerRadius.toPx(), center = Offset(px, y(peak.bpm)))
            }
        }
    }
}
