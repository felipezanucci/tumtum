package cc.tumtum.capture

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A night, drawn.
 *
 * Deliberately a plain BPM-over-time line and nothing else. The brand manual
 * allows a real heart-rate chart only where it is the person's own data and
 * the chart is product information — and forbids anything that reads as
 * clinical: no zones, no risk colours, no normal ranges. This answers "when
 * did that happen?", never "what does it mean?".
 *
 * Drawn on a Canvas rather than pulled from a chart library because the APK
 * carries no runtime dependencies, and one polyline does not justify breaking
 * that.
 */
class HRCurveView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private var points: List<HrPoint> = emptyList()
    private var peaks: List<Peak> = emptyList()

    private val density = resources.displayMetrics.density
    private fun dp(value: Float) = value * density

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(2f)
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        color = resources.getColor(R.color.tumtum_lime, null)
    }

    private val peakPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = resources.getColor(R.color.tumtum_white, null)
    }

    private val peakRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(2f)
        color = resources.getColor(R.color.tumtum_lime, null)
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = dp(11f)
        color = resources.getColor(R.color.tumtum_muted, null)
    }

    private val path = Path()

    fun show(points: List<HrPoint>, peaks: List<Peak>) {
        this.points = points
        this.peaks = peaks
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (points.size < 2) return

        // Room for the bpm labels on the left and the clock along the bottom.
        val left = dp(34f)
        val right = width - dp(8f)
        val top = dp(12f)
        val bottom = height - dp(20f)
        if (right <= left || bottom <= top) return

        val firstTime = points.first().timeMillis
        val lastTime = points.last().timeMillis
        val span = (lastTime - firstTime).toFloat()
        if (span <= 0f) return

        var lowest = points.minOf { it.bpm }
        var highest = points.maxOf { it.bpm }
        // A perfectly flat stretch would divide by zero and, worse, draw a
        // line hugging one edge as if it meant something.
        if (highest == lowest) {
            highest += 1
            lowest -= 1
        }
        val range = (highest - lowest).toFloat()

        fun xFor(timeMillis: Long) = left + (timeMillis - firstTime) / span * (right - left)
        fun yFor(bpm: Int) = bottom - (bpm - lowest) / range * (bottom - top)

        path.reset()
        points.forEachIndexed { index, point ->
            val x = xFor(point.timeMillis)
            val y = yFor(point.bpm)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        canvas.drawPath(path, linePaint)

        // The two numbers that frame the night, on the axis they belong to.
        canvas.drawText(highest.toString(), dp(2f), yFor(highest) + dp(4f), labelPaint)
        canvas.drawText(lowest.toString(), dp(2f), yFor(lowest) + dp(4f), labelPaint)

        // When it started and when it ended, in the phone's own timezone.
        canvas.drawText(CLOCK.format(Date(firstTime)), left, height - dp(4f), labelPaint)
        val endLabel = CLOCK.format(Date(lastTime))
        canvas.drawText(endLabel, right - labelPaint.measureText(endLabel), height - dp(4f), labelPaint)

        for (peak in peaks) {
            val at = peak.timestampMillis ?: continue
            // A peak outside the drawn window has no honest position on it.
            if (at < firstTime || at > lastTime) continue
            val x = xFor(at)
            val y = yFor(peak.bpm.coerceIn(lowest, highest))
            canvas.drawCircle(x, y, dp(4f), peakPaint)
            canvas.drawCircle(x, y, dp(7f), peakRingPaint)
        }
    }

    companion object {
        private val CLOCK = SimpleDateFormat("HH:mm", Locale("pt", "BR"))
    }
}
