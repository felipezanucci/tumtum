package cc.tumtum.app.export

import cc.tumtum.app.domain.HrSample
import cc.tumtum.app.domain.NightAnalyzer
import java.time.Duration
import java.time.Instant

/**
 * The points the card's line is drawn through (#61, 23/09).
 *
 * The card drew every raw 1 Hz sample, and at card scale beat-to-beat noise
 * is a jagged band — "a linha está muito grosseira". This averages the night
 * into about [TARGET_POINTS] points across the window, with three rules the
 * brand's chart rules demand:
 *
 *  - **a gap stays a gap** — no bucket, and no segment, spans minutes nobody
 *    measured;
 *  - **the peak is the peak** — the bucket holding the night's highest
 *    reading is drawn at that reading, so the marker sits on the line and the
 *    number on the card is not smoothed away;
 *  - nothing is invented: every point is a mean of real samples.
 *
 * Pure, so it is tested without a canvas.
 */
object CurvePath {
    data class Point(val time: Instant, val bpm: Float)

    const val TARGET_POINTS = 140

    /** Never average over less than this: at a short capture, detail is fine. */
    private const val MIN_BUCKET_MS = 5_000L

    fun segments(
        samples: List<HrSample>,
        windowStart: Instant,
        windowEnd: Instant,
        gapSec: Long = NightAnalyzer.gapThresholdSec(samples),
    ): List<List<Point>> {
        if (samples.isEmpty()) return emptyList()
        val sorted = samples.sortedBy { it.time }
        val totalMs = Duration.between(windowStart, windowEnd).toMillis().coerceAtLeast(1)
        val bucketMs = maxOf(MIN_BUCKET_MS, totalMs / TARGET_POINTS)
        val peak = sorted.maxBy { it.bpm }

        // Split where the capture broke, exactly as the gap marks are drawn.
        val runs = mutableListOf<MutableList<HrSample>>()
        for (s in sorted) {
            val last = runs.lastOrNull()?.lastOrNull()
            if (last == null || Duration.between(last.time, s.time).seconds > gapSec) {
                runs += mutableListOf(s)
            } else {
                runs.last() += s
            }
        }

        return runs.map { run ->
            run.groupBy { Duration.between(windowStart, it.time).toMillis().floorDiv(bucketMs) }
                .toSortedMap()
                .values
                .map { bucket ->
                    if (peak in bucket) {
                        Point(peak.time, peak.bpm.toFloat())
                    } else {
                        Point(
                            Instant.ofEpochMilli(bucket.map { it.time.toEpochMilli() }.average().toLong()),
                            bucket.map { it.bpm }.average().toFloat(),
                        )
                    }
                }
        }
    }
}
