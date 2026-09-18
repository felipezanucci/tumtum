package cc.tumtum.app.export

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import cc.tumtum.app.R
import cc.tumtum.app.domain.HrSample
import cc.tumtum.app.domain.Night
import cc.tumtum.app.domain.NightAnalyzer
import cc.tumtum.app.domain.Skin
import java.io.File
import java.time.Duration
import java.time.Instant

/**
 * O card 9:16 como imagem (1080×1920, Story) — o mesmo layout do ShareCardView,
 * desenhado com Canvas para sair do aparelho pelo share sheet com a imagem anexa.
 * Regras do manual: número sempre preto, exceto no preto (rosa); texto preto
 * sobre rosa/amarelo; buraco de captura desenhado como buraco, nunca inventado.
 */
object CardRenderer {

    private const val W = 1080
    private const val H = 1920
    private const val PAD = 97f // 0.09 × W, como no componente

    private const val INK = 0xFF000000.toInt()
    private const val PAPER = 0xFFFFFFFF.toInt()
    private const val ROSE = 0xFFFF6F91.toInt()
    private const val ACID = 0xFFEFFF00.toInt()
    private const val GRAY25 = 0xFFB4B4B4.toInt()
    private const val GRAY70 = 0xFF4A4A4A.toInt()
    private const val GRAY10 = 0xFFE6E6E6.toInt()

    fun render(
        context: Context,
        night: Night,
        skin: Skin,
        title: String,
        meta: String,
        chip: String?,
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bg = when (skin) {
            Skin.PINK -> ROSE
            Skin.BLACK -> INK
            Skin.YELLOW -> ACID
            Skin.WHITE -> PAPER
        }
        val fg = if (skin == Skin.BLACK) PAPER else INK
        val num = if (skin == Skin.BLACK) ROSE else INK
        canvas.drawColor(bg)
        if (skin == Skin.WHITE) {
            val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 4f
                color = GRAY10
            }
            canvas.drawRect(2f, 2f, W - 2f, H - 2f, border)
        }

        val base = ResourcesCompat.getFont(context, R.font.instrument_sans_var) ?: Typeface.SANS_SERIF
        val bold = Typeface.create(base, 700, false)
        val semibold = Typeface.create(base, 600, false)

        // Chip ácido no topo (evento · hora)
        if (chip != null) {
            val chipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = semibold
                textSize = 41f
                letterSpacing = 0.14f
                color = INK
            }
            val tw = chipPaint.measureText(chip)
            val chipBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ACID }
            val chipH = 41f + 40f
            canvas.drawRect(PAD, PAD, PAD + tw + 80f, PAD + chipH, chipBg)
            val fm = chipPaint.fontMetrics
            canvas.drawText(chip, PAD + 40f, PAD + chipH / 2f - (fm.ascent + fm.descent) / 2f, chipPaint)
        }

        // Bloco ancorado embaixo: título → número → curva → meta + wordmark
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = bold
            textSize = 67f
            color = fg
        }
        val titleLines = title.split("\n")
        val titleLineH = 71f
        val titleH = titleLines.size * titleLineH

        val numPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = bold
            textSize = 389f
            letterSpacing = -0.05f
            color = num
        }
        val numTopPad = 49f
        val numH = 350f

        val hasCurve = skin == Skin.BLACK && night.samples.isNotEmpty()
        val curveTopPad = 54f
        val curveH = if (hasCurve) 205f else 0f

        val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = semibold
            textSize = 52f
            color = fg
        }
        val wordmarkW = 259
        val wordmarkH = (wordmarkW * 96f / 636f).toInt() // proporção do SVG oficial
        val metaTopPad = 43f
        val metaRowH = maxOf(60f, wordmarkH.toFloat())

        val blockH = titleH + numTopPad + numH + (if (hasCurve) curveTopPad + curveH else 0f) + metaTopPad + metaRowH
        var y = H - PAD - blockH

        titleLines.forEach { line ->
            val fm = titlePaint.fontMetrics
            canvas.drawText(line, PAD, y - fm.ascent, titlePaint)
            y += titleLineH
        }

        y += numTopPad
        // Dígitos não têm descendente: a baseline no pé do bloco preenche a altura toda.
        canvas.drawText("${night.peakBpm}", PAD - 8f, y + numH - 10f, numPaint)
        y += numH

        if (hasCurve) {
            y += curveTopPad
            drawCurve(
                canvas = canvas,
                samples = night.samples,
                windowStart = night.startAt,
                windowEnd = night.endAt,
                left = PAD,
                top = y,
                width = W - 2 * PAD,
                height = curveH,
                lineColor = ROSE,
                markerColor = ACID,
                gapColor = GRAY70,
            )
            y += curveH
        }

        y += metaTopPad
        val rowBottom = y + metaRowH
        run {
            val fm = metaPaint.fontMetrics
            canvas.drawText(meta, PAD, rowBottom - fm.descent, metaPaint)
        }
        val wordmark = ResourcesCompat.getDrawable(
            context.resources,
            if (skin == Skin.BLACK) R.drawable.wordmark_white else R.drawable.wordmark_black,
            null,
        )
        wordmark?.let {
            val left = (W - PAD - wordmarkW).toInt()
            val top = (rowBottom - wordmarkH).toInt()
            it.setBounds(left, top, left + wordmarkW, top + wordmarkH)
            it.draw(canvas)
        }

        return bitmap
    }

    /** A curva com as regras da BpmCurve: segmentos quebram no gap; traço pontilhado na base. */
    private fun drawCurve(
        canvas: Canvas,
        samples: List<HrSample>,
        windowStart: Instant,
        windowEnd: Instant,
        left: Float,
        top: Float,
        width: Float,
        height: Float,
        lineColor: Int,
        markerColor: Int,
        gapColor: Int,
    ) {
        val sorted = samples.sortedBy { it.time }
        val lo = (sorted.minOf { it.bpm } - 6).coerceAtLeast(30)
        val hi = sorted.maxOf { it.bpm } + 6
        val span = (hi - lo).coerceAtLeast(1)
        val totalMs = Duration.between(windowStart, windowEnd).toMillis().coerceAtLeast(1)
        val markerR = 15f
        val padTop = markerR + 2f
        val padBottom = 12f

        fun x(t: Instant): Float =
            left + (Duration.between(windowStart, t).toMillis().toFloat() / totalMs) * width

        fun y(bpm: Int): Float =
            top + padTop + (1f - (bpm - lo).toFloat() / span) * (height - padTop - padBottom)

        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 8f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            color = lineColor
        }

        var path: Path? = null
        var prev: HrSample? = null
        fun flush() {
            path?.let { canvas.drawPath(it, linePaint) }
            path = null
        }
        for (s in sorted) {
            val p = prev
            if (p != null && Duration.between(p.time, s.time).seconds > NightAnalyzer.GAP_THRESHOLD_SEC) flush()
            val cur = path
            if (cur == null) {
                path = Path().apply { moveTo(x(s.time), y(s.bpm)) }
            } else {
                cur.lineTo(x(s.time), y(s.bpm))
            }
            prev = s
        }
        flush()

        val gapPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 7f
            color = gapColor
            pathEffect = DashPathEffect(floatArrayOf(20f, 35f), 0f)
        }
        val gapY = top + height - 4f
        NightAnalyzer.gaps(sorted, windowStart, windowEnd).forEach { g ->
            canvas.drawLine(x(g.start), gapY, x(g.end), gapY, gapPaint)
        }

        val peak = sorted.maxBy { it.bpm }
        canvas.drawCircle(x(peak.time), y(peak.bpm), markerR, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = markerColor })
    }

    /** Grava o PNG no cache e devolve o chooser com a imagem anexa. Nada sai sem o toque (§1). */
    fun shareIntent(context: Context, bitmap: Bitmap, fileName: String): Intent {
        val dir = File(context.cacheDir, "cards").apply { mkdirs() }
        val file = File(dir, fileName)
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return Intent.createChooser(
            Intent(Intent.ACTION_SEND)
                .setType("image/png")
                .putExtra(Intent.EXTRA_STREAM, uri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION),
            fileName,
        )
    }
}
