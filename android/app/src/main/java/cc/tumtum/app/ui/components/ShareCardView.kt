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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import cc.tumtum.app.domain.HrSample
import cc.tumtum.app.domain.Skin
import cc.tumtum.app.export.CardFoot
import cc.tumtum.app.ui.theme.InstrumentSans
import cc.tumtum.app.ui.theme.TT
import java.time.Instant
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale

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
    photo: ImageBitmap? = null,
    /** The number as printed — "110+" when the person hid the exact bpm (26/09); null prints [bpm]. */
    bpmLabel: String? = null,
) {
    val bg = skinColor(skin)
    val fg = if (skin == Skin.BLACK) TT.Paper else TT.Ink
    val num = if (skin == Skin.BLACK) TT.Rose else TT.Ink
    val pad = width * 0.09f
    val w = width.value

    Box(
        modifier
            .width(width)
            .aspectRatio(9f / 16f)
            .background(bg)
            .let { if (skin == Skin.WHITE) it.border(1.dp, TT.Gray10) else it },
    ) {
    if (photo != null && skin == Skin.BLACK) {
        Image(photo, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize())
        Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.6f)))
    }
    Column(Modifier.matchParentSize().padding(pad)) {
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
            bpmLabel ?: "$bpm",
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
                // 11 px on the 1080 px card (A2): the preview scales with it.
                strokeWidth = maxOf(1.5f, w * 0.0102f).dp,
                markerRadius = maxOf(3f, w * 0.012f).dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height((w * 0.19f).dp)
                    .padding(top = (w * 0.05f).dp),
            )
        }
        // The foot, as CardRenderer draws it (A2, 22/09) — the same rule from
        // CardFoot decides whether the event shares the line or takes its own.
        val event = chip?.trim()?.takeIf { it.isNotEmpty() }
        val metaText: @Composable () -> Unit = {
            Text(
                meta,
                style = TextStyle(
                    fontFamily = InstrumentSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = maxOf(8f, w * 0.048f).sp,
                ),
                color = fg,
                maxLines = 1,
            )
        }
        val wordmark: @Composable () -> Unit = { Wordmark(width = (w * 0.185f).dp, onDark = skin == Skin.BLACK) }
        if (event != null && !CardFoot.ownRow(event)) {
            Row(
                Modifier.fillMaxWidth().padding(top = (w * 0.04f).dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    EventBox(event, skin, w, Modifier.weight(1f, fill = false))
                    Spacer(Modifier.width((w * 0.024f).dp))
                    metaText()
                }
                Spacer(Modifier.width((w * 0.028f).dp))
                wordmark()
            }
        } else {
            if (event != null) {
                // Its own row: two lines before a cut, as the renderer (#61).
                EventBox(event, skin, w, Modifier.padding(top = (w * 0.04f).dp), maxLines = 2)
            }
            Row(
                Modifier.fillMaxWidth().padding(top = (w * if (event != null) 0.022f else 0.04f).dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                metaText()
                wordmark()
            }
        }
    }
    }
}

/** The event's name in its box: acid with black type, or black on the skins acid would vanish into. */
@Composable
private fun EventBox(event: String, skin: Skin, w: Float, modifier: Modifier = Modifier, maxLines: Int = 1) {
    val dark = skin == Skin.YELLOW || skin == Skin.WHITE
    Text(
        event,
        style = TextStyle(
            fontFamily = InstrumentSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = maxOf(7f, w * 0.038f).sp,
            letterSpacing = 0.1.em,
        ),
        color = when (skin) {
            Skin.YELLOW -> TT.Acid
            Skin.WHITE -> TT.Paper
            else -> TT.Ink
        },
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .background(if (dark) TT.Ink else TT.Acid)
            .padding(horizontal = (w * 0.035f).dp, vertical = (w * 0.024f).dp),
    )
}
