package cc.tumtum.app.export

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
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
import android.graphics.BitmapFactory
import android.graphics.RectF
import android.graphics.Shader
import android.net.Uri

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
    private const val SCRIM = 0x99000000.toInt()
    // How far above the block of type the sticker's gradient starts to darken.
    private const val GRADIENT_LEAD = 240f

    /**
     * @param sticker The card as a layer over someone's own video (item 43):
     *   the same drawing with no background of its own, so the video shows
     *   through, and a gradient under the block of type instead of the flat
     *   wash the photo card uses — see the note where it is drawn. Black skin
     *   only, the one whose surface a photo or a video can take.
     * @param bare No wash at all, only the shadows under the type. For the
     *   sticker handed to a Story editor, which the person moves wherever it
     *   reads. Tested 24/09: Snapchat draws a sticker at most 300 dp on a
     *   side, and there the wash showed as a dark box over the video however
     *   its edges were faded ("a máscara, o filtro errado"). Not for the video
     *   the card is burned into, where the card cannot be moved off a bright
     *   frame.
     * @param bpmLabel The number as the person chose to show it: exact, or
     *   rounded down to the ten with a plus ("110+"). Null prints the peak.
     */
    fun render(
        context: Context,
        night: Night,
        skin: Skin,
        title: String,
        meta: String,
        chip: String?,
        photo: Bitmap? = null,
        sticker: Boolean = false,
        bare: Boolean = false,
        bpmLabel: String? = null,
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
        // A sticker has no background of its own: the video runs behind it. Its
        // base is a gradient drawn later, once the block of type has been
        // measured, so it covers exactly what needs covering.
        if (!(sticker && skin == Skin.BLACK)) canvas.drawColor(bg)
        // A fan's own photo (§5.11): cover-scaled behind the black skin, under a
        // scrim dark enough for white text and the pink number to stay legible.
        if (photo != null && skin == Skin.BLACK && !sticker) {
            val scale = maxOf(W.toFloat() / photo.width, H.toFloat() / photo.height)
            val dw = photo.width * scale
            val dh = photo.height * scale
            val left = (W - dw) / 2f
            val top = (H - dh) / 2f
            canvas.drawBitmap(photo, null, RectF(left, top, left + dw, top + dh), Paint(Paint.FILTER_BITMAP_FLAG))
            canvas.drawColor(SCRIM)
        }
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
        val wordmarkW = 200
        val wordmarkH = (wordmarkW * 96f / 636f).toInt() // proporção do SVG oficial
        val metaTopPad = 43f

        // The foot (version A2, Felipe's pick, 22/09): the event in a box,
        // "bpm às 22h12" beside it, the wordmark to the right. It replaces the
        // acid chip that sat alone at the top of the card — "o quadradinho
        // amarelo… a gente tem que repensar" — so everything the card says
        // about *where* now sits in one line under the evidence. On the yellow
        // and white skins an acid box would vanish, so the box turns black.
        val event = chip?.trim()?.takeIf { it.isNotEmpty() }
        val boxColor = if (skin == Skin.YELLOW || skin == Skin.WHITE) INK else ACID
        val eventPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = semibold
            textSize = 41f
            letterSpacing = 0.1f
            color = when (skin) {
                Skin.YELLOW -> ACID
                Skin.WHITE -> PAPER
                else -> INK
            }
        }
        val boxH = 96f
        val boxPadX = 38f
        val ownRow = event != null && CardFoot.ownRow(event)
        // On its own row a long name gets two lines before it is cut (#61).
        val eventLineStep = 50f
        val ownRowLines = if (ownRow) {
            CardFoot.wrap(event!!, W - 2 * PAD - 2 * boxPadX) { eventPaint.measureText(it) }
        } else {
            emptyList()
        }
        val ownRowBoxH = boxH + eventLineStep * (ownRowLines.size - 1).coerceAtLeast(0)
        val rowGap = 24f
        val metaRowH = maxOf(60f, wordmarkH.toFloat())
        val footH = when {
            event == null -> metaRowH
            ownRow -> ownRowBoxH + rowGap + metaRowH
            else -> boxH
        }

        val blockH = titleH + numTopPad + numH + (if (hasCurve) curveTopPad + curveH else 0f) + metaTopPad + footH
        var y = H - PAD - blockH

        // The base under the type, for a sticker over someone's video (22/09).
        // It used to be the flat 60% wash the photo card uses, and over a video
        // that is an eraser — it darkens the whole frame to win legibility in
        // the one band where the type actually is. Felipe asked whether the
        // mask could go entirely; it cannot, and his own test video is the
        // reason: a white t-shirt sat directly behind the white meta line.
        // A gradient gives both. The video runs clean through the top half and
        // behind the acid chip — black on acid, legible over anything — and
        // darkens into a base beneath the block, which is anchored to the
        // bottom and measured just above, so this moves with it instead of
        // guessing at a fixed fraction of the height.
        if (sticker && skin == Skin.BLACK) {
            if (!bare) {
                val top = (y - GRADIENT_LEAD).coerceAtLeast(0f)
                val wash = Paint().apply {
                    shader = LinearGradient(
                        0f, top, 0f, H.toFloat(),
                        intArrayOf(0x00000000, 0x70000000, 0xE6000000.toInt()),
                        floatArrayOf(0f, 0.35f, 1f),
                        Shader.TileMode.CLAMP,
                    )
                }
                canvas.drawRect(0f, top, W.toFloat(), H.toFloat(), wash)
            }
            // The second net, for the stretch where the gradient is still
            // light: a shadow costs nothing and saves a bright frame. With no
            // wash at all it is the only net, so it is drawn heavier.
            listOf(titlePaint, numPaint, metaPaint).forEach {
                if (bare) {
                    it.setShadowLayer(18f, 0f, 3f, 0xE6000000.toInt())
                } else {
                    it.setShadowLayer(14f, 0f, 3f, 0xB3000000.toInt())
                }
            }
        }

        titleLines.forEach { line ->
            val fm = titlePaint.fontMetrics
            canvas.drawText(line, PAD, y - fm.ascent, titlePaint)
            y += titleLineH
        }

        y += numTopPad
        // Dígitos não têm descendente: a baseline no pé do bloco preenche a altura toda.
        // "110+" when the person hid the exact number (26/09); the bpm otherwise.
        canvas.drawText(bpmLabel ?: "${night.peakBpm}", PAD - 8f, y + numH - 10f, numPaint)
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
                // The dark edge exists for a bright frame behind the line; on
                // a flat skin it only thickened it (#61).
                outlined = (photo != null && skin == Skin.BLACK) || sticker,
            )
            y += curveH
        }

        y += metaTopPad
        val wordmark = ResourcesCompat.getDrawable(
            context.resources,
            if (skin == Skin.BLACK) R.drawable.wordmark_white else R.drawable.wordmark_black,
            null,
        )
        fun drawWordmark(centerY: Float) = wordmark?.let {
            val left = (W - PAD - wordmarkW).toInt()
            val top = (centerY - wordmarkH / 2f).toInt()
            it.setBounds(left, top, left + wordmarkW, top + wordmarkH)
            it.draw(canvas)
        }
        fun drawBox(maxWidth: Float): Float {
            val text = CardFoot.fit(event!!, maxWidth - 2 * boxPadX) { eventPaint.measureText(it) }
            val boxW = eventPaint.measureText(text) + 2 * boxPadX
            canvas.drawRect(PAD, y, PAD + boxW, y + boxH, Paint().apply { color = boxColor })
            val fm = eventPaint.fontMetrics
            canvas.drawText(text, PAD + boxPadX, y + boxH / 2f - (fm.ascent + fm.descent) / 2f, eventPaint)
            return boxW
        }
        val usable = W - 2 * PAD
        when {
            event == null -> {
                val fm = metaPaint.fontMetrics
                canvas.drawText(meta, PAD, y + metaRowH - fm.descent, metaPaint)
                drawWordmark(y + metaRowH - wordmarkH / 2f)
            }

            ownRow -> {
                val boxW = ownRowLines.maxOf { eventPaint.measureText(it) } + 2 * boxPadX
                canvas.drawRect(PAD, y, PAD + boxW, y + ownRowBoxH, Paint().apply { color = boxColor })
                val efm = eventPaint.fontMetrics
                ownRowLines.forEachIndexed { i, line ->
                    val center = y + boxH / 2f + i * eventLineStep
                    canvas.drawText(line, PAD + boxPadX, center - (efm.ascent + efm.descent) / 2f, eventPaint)
                }
                y += ownRowBoxH + rowGap
                val fm = metaPaint.fontMetrics
                canvas.drawText(meta, PAD, y + metaRowH - fm.descent, metaPaint)
                drawWordmark(y + metaRowH - wordmarkH / 2f)
            }

            else -> {
                val metaW = metaPaint.measureText(meta)
                val boxW = drawBox(usable - wordmarkW - 30f - metaW - 26f)
                val fm = metaPaint.fontMetrics
                canvas.drawText(meta, PAD + boxW + 26f, y + boxH / 2f - (fm.ascent + fm.descent) / 2f, metaPaint)
                drawWordmark(y + boxH / 2f)
            }
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
        outlined: Boolean,
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

        fun yOf(bpm: Float): Float =
            top + padTop + (1f - (bpm - lo) / span) * (height - padTop - padBottom)

        fun y(bpm: Int): Float = yOf(bpm.toFloat())

        // Finer and smoothed (#61, 23/09). A2 made it 11 px with a 17 px
        // outline, drawn through every raw 1 Hz sample — Felipe: "a linha
        // está muito grosseira". A stroke that heavy turns each beat-to-beat
        // wobble into a jagged band. Now the line follows [CurvePath]'s
        // averaged points (never across a gap, still through the true peak)
        // with curves between them, at 7.5 px, and outlined only when a
        // photo or a video sits behind it.
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 7.5f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            color = lineColor
        }
        val outlinePaint = Paint(linePaint).apply {
            strokeWidth = 12.5f
            color = 0x8C000000.toInt()
        }

        CurvePath.segments(sorted, windowStart, windowEnd).forEach { points ->
            val path = Path()
            val first = points.first()
            path.moveTo(x(first.time), yOf(first.bpm))
            if (points.size == 1) {
                path.lineTo(x(first.time) + 0.1f, yOf(first.bpm))
            }
            // Through each point's midpoint to the next: a smooth line that
            // still passes every averaged value closely, and the peak exactly.
            for (i in 1 until points.size) {
                val p = points[i - 1]
                val q = points[i]
                if (i == points.size - 1) {
                    path.quadTo(x(p.time), yOf(p.bpm), x(q.time), yOf(q.bpm))
                } else {
                    val mx = (x(p.time) + x(q.time)) / 2f
                    val my = (yOf(p.bpm) + yOf(q.bpm)) / 2f
                    path.quadTo(x(p.time), yOf(p.bpm), mx, my)
                }
            }
            if (outlined) canvas.drawPath(path, outlinePaint)
            canvas.drawPath(path, linePaint)
        }

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
        canvas.drawCircle(
            x(peak.time),
            y(peak.bpm),
            markerR,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x96000000.toInt() },
        )
        canvas.drawCircle(x(peak.time), y(peak.bpm), markerR - 3f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = markerColor })
    }

    /** A photo from the picker, decoded no larger than the card needs. */
    fun loadPhoto(context: Context, uri: Uri): Bitmap? = runCatching {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= W && bounds.outHeight / (sample * 2) >= H) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
    }.getOrNull()

    /** The PNG in the cache, where the FileProvider can serve it. */
    fun writePng(context: Context, bitmap: Bitmap, fileName: String): File {
        val dir = File(context.cacheDir, "cards").apply { mkdirs() }
        val file = File(dir, fileName)
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return file
    }

    /** Grava o PNG no cache e devolve o chooser com a imagem anexa. Nada sai sem o toque (§1). */
    fun shareIntent(context: Context, bitmap: Bitmap, fileName: String): Intent =
        shareFileIntent(context, writePng(context, bitmap, fileName), "image/png")

    /**
     * O chooser do sistema com um arquivo anexo. Um MP4 com o card gravado
     * dentro sai por aqui para Instagram, X, TikTok, Snap, WhatsApp ou a
     * galeria — um arquivo, não uma integração por rede (22/09).
     */
    fun shareFileIntent(context: Context, file: File, mime: String): Intent {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return Intent.createChooser(
            Intent(Intent.ACTION_SEND)
                .setType(mime)
                .putExtra(Intent.EXTRA_STREAM, uri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION),
            file.name,
        )
    }
}
