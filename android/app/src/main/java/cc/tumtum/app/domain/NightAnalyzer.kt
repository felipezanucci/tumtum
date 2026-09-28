package cc.tumtum.app.domain

import java.time.Duration
import java.time.Instant

/**
 * Análise da noite (§7):
 *  - gap > 60s (10s numa cinta a 1 Hz) aparece como interrupção da linha; zero interpolação;
 *  - cobertura = tempo amostrado / duração da janela;
 *  - momentos = o detector do servidor, o mesmo número por número (28/09);
 *    o mais alto deles é o pico.
 */
object NightAnalyzer {

    const val GAP_THRESHOLD_SEC = 60L

    /** A source reading at least this often (a 1 Hz strap) is "dense". */
    const val DENSE_MAX_INTERVAL_SEC = 2

    /** A dense source that goes quiet for longer than this has a gap. */
    const val DENSE_GAP_SEC = 10L

    /**
     * What counts as a gap for these samples (25/09). A minute is right for a
     * watch that reads once a minute, and wrong for a strap that reads every
     * second: in b201 a strap taken off for fifty seconds was drawn as a
     * straight line across minutes nobody measured.
     */
    fun gapThresholdSec(samples: List<HrSample>): Long =
        if (medianIntervalSec(samples) in 1..DENSE_MAX_INTERVAL_SEC) DENSE_GAP_SEC else GAP_THRESHOLD_SEC

    fun gaps(
        samples: List<HrSample>,
        windowStart: Instant,
        windowEnd: Instant,
        thresholdSec: Long = gapThresholdSec(samples),
    ): List<Gap> {
        if (samples.isEmpty()) return listOf(Gap(windowStart, windowEnd))
        val sorted = samples.sortedBy { it.time }
        val out = mutableListOf<Gap>()
        if (Duration.between(windowStart, sorted.first().time).seconds > thresholdSec) {
            out += Gap(windowStart, sorted.first().time)
        }
        sorted.zipWithNext().forEach { (a, b) ->
            if (Duration.between(a.time, b.time).seconds > thresholdSec) out += Gap(a.time, b.time)
        }
        if (Duration.between(sorted.last().time, windowEnd).seconds > thresholdSec) {
            out += Gap(sorted.last().time, windowEnd)
        }
        return out
    }

    /** Cobertura em % — cada amostra "cobre" até o próximo ponto, limitado ao gap threshold. */
    fun coveragePct(samples: List<HrSample>, windowStart: Instant, windowEnd: Instant): Int {
        val total = Duration.between(windowStart, windowEnd).seconds
        if (total <= 0 || samples.isEmpty()) return 0
        val sorted = samples.sortedBy { it.time }
        val threshold = gapThresholdSec(sorted)
        var covered = 0L
        sorted.zipWithNext().forEach { (a, b) ->
            covered += Duration.between(a.time, b.time).seconds.coerceAtMost(threshold)
        }
        covered += threshold.coerceAtMost(total) // última amostra
        return ((covered * 100) / total).toInt().coerceIn(0, 100)
    }

    fun medianIntervalSec(samples: List<HrSample>): Int {
        if (samples.size < 2) return 0
        val deltas = samples.sortedBy { it.time }
            .zipWithNext { a, b -> Duration.between(a.time, b.time).seconds }
            .sorted()
        return deltas[deltas.size / 2].toInt().coerceAtLeast(1)
    }

    // ---- Moments: the server's detector, on the phone (28/09) ----
    //
    // Until 28/09 the phone had its own idea of a moment — the highest
    // readings at least ten minutes apart, whatever they were — and the
    // server had the one CLAUDE.md specifies. The same night then carried two
    // headlines: on a 17-second test the phone called 71 bpm over a 69
    // average a "moment", the server correctly found none, and the reveal
    // went from "84 A NOITE INTEIRA" to "SEM MOMENTOS" the instant the night
    // was saved. This is a port of `detect_peaks()` in
    // backend/app/services/peak_detection.py, number for number; change one,
    // change both (and CLAUDE.md, "Peak detection algorithm").

    const val MIN_SAMPLES = 10
    const val SMOOTH_WINDOW_SEC = 5.0
    const val BASELINE_WINDOW_SEC = 1200.0
    const val Z_ENTER = 2.0
    const val Z_EXIT = 1.0
    const val MIN_RISE_BPM = 10.0
    const val MIN_DURATION_SEC = 5
    const val MERGE_WINDOW_SEC = 30L
    const val MAX_MOMENTS = 20

    /**
     * Step 4 with its three guards, alone so a test can hold it: a very
     * steady stretch never divides by near-zero, a rise above 30 bpm is
     * always significant, and a rise a person would not feel (under 10 bpm)
     * never opens a moment — under 5 it is nothing at all.
     */
    fun zScore(deviation: Double, spread: Double): Double {
        var z = if (spread > 1.0) deviation / spread else if (deviation > 0) deviation / 10.0 else 0.0
        if (deviation > 30) z = maxOf(z, deviation / 15.0)
        if (deviation < MIN_RISE_BPM / 2.0) {
            z = 0.0
        } else if (deviation < MIN_RISE_BPM) {
            z = minOf(z, Z_EXIT)
        }
        return z
    }

    /**
     * The night's moments, strongest first (magnitude = peak z × seconds).
     * The highest of them is the peak. Time-based, never index-based: a strap
     * at 1 Hz and a watch at one reading a minute go through the same code.
     */
    fun moments(samples: List<HrSample>, max: Int = MAX_MOMENTS): List<Moment> {
        val sorted = samples.sortedBy { it.time }
        if (sorted.size < MIN_SAMPLES) return emptyList()
        val n = sorted.size
        val secs = DoubleArray(n) { sorted[it].time.toEpochMilli() / 1000.0 }
        val bpms = DoubleArray(n) { sorted[it].bpm.toDouble() }

        // 1. Smooth — 2. and 3. baseline and spread, robust to what they surround
        val smoothed = slidingMean(secs, bpms, SMOOTH_WINDOW_SEC)
        val (baselines, spreads) = slidingMedianAndSpread(secs, smoothed, BASELINE_WINDOW_SEC)
        // 4. Elevation
        val z = DoubleArray(n) { i -> zScore(smoothed[i] - baselines[i], spreads[i]) }

        // 5. Regions with hysteresis — 6. drop the short ones — 7. extract
        val found = mutableListOf<Region>()
        var i = 0
        while (i < n) {
            if (z[i] > Z_ENTER) {
                val startIdx = i
                while (i < n && z[i] > Z_EXIT) i++
                val endIdx = i - 1
                val duration = maxOf(secs[endIdx] - secs[startIdx], 1.0).toInt()
                if (duration >= MIN_DURATION_SEC) {
                    var top = startIdx
                    var peakZ = z[startIdx]
                    for (k in startIdx..endIdx) {
                        if (bpms[k] > bpms[top]) top = k
                        if (z[k] > peakZ) peakZ = z[k]
                    }
                    found += Region(
                        at = sorted[top].time,
                        bpm = sorted[top].bpm,
                        durationSec = duration,
                        magnitude = peakZ * duration,
                        start = sorted[startIdx].time,
                        end = sorted[endIdx].time,
                    )
                }
            } else {
                i++
            }
        }

        // 8. Merge regions 30 s apart or closer: the stronger one's numbers, both bounds
        val merged = mutableListOf<Region>()
        for (r in found.sortedBy { it.at }) {
            val last = merged.lastOrNull()
            if (last != null && Duration.between(last.end, r.start).toMillis() <= MERGE_WINDOW_SEC * 1000) {
                val keep = if (r.magnitude > last.magnitude) r else last
                merged[merged.size - 1] = keep.copy(
                    start = minOf(r.start, last.start),
                    end = maxOf(r.end, last.end),
                )
            } else {
                merged += r
            }
        }

        // 9. Rank
        val ranked = merged.sortedByDescending { it.magnitude }.take(max)
        val peakBpm = ranked.maxOfOrNull { it.bpm } ?: return emptyList()
        return ranked.map { Moment(bpm = it.bpm, at = it.at, durationSec = it.durationSec, isPeak = it.bpm == peakBpm) }
    }

    private data class Region(
        val at: Instant,
        val bpm: Int,
        val durationSec: Int,
        val magnitude: Double,
        val start: Instant,
        val end: Instant,
    )

    /** Time-based moving average over a centred window. */
    private fun slidingMean(secs: DoubleArray, values: DoubleArray, windowSec: Double): DoubleArray {
        val n = values.size
        val half = windowSec / 2.0
        val out = DoubleArray(n)
        var left = 0
        var right = 0
        var total = 0.0
        for (i in 0 until n) {
            val center = secs[i]
            while (right < n && secs[right] <= center + half) { total += values[right]; right++ }
            while (left < right && secs[left] < center - half) { total -= values[left]; left++ }
            out[i] = if (right > left) total / (right - left) else values[i]
        }
        return out
    }

    /** Rolling median and IQR-based spread (IQR / 1.349) over a centred window kept sorted. */
    private fun slidingMedianAndSpread(
        secs: DoubleArray,
        values: DoubleArray,
        windowSec: Double,
    ): Pair<DoubleArray, DoubleArray> {
        val n = values.size
        val half = windowSec / 2.0
        val window = ArrayList<Double>()
        val medians = DoubleArray(n)
        val spreads = DoubleArray(n)
        var left = 0
        var right = 0
        for (i in 0 until n) {
            val center = secs[i]
            while (right < n && secs[right] <= center + half) {
                val at = window.binarySearch(values[right]).let { if (it < 0) -it - 1 else it }
                window.add(at, values[right])
                right++
            }
            while (left < right && secs[left] < center - half) {
                val at = window.binarySearch(values[left])
                if (at >= 0) window.removeAt(at)
                left++
            }
            if (window.isEmpty()) {
                medians[i] = values[i]
                spreads[i] = 0.0
                continue
            }
            medians[i] = quantile(window, 0.5)
            spreads[i] = if (window.size >= 4) (quantile(window, 0.75) - quantile(window, 0.25)) / 1.349 else 0.0
        }
        return medians to spreads
    }

    /** Linear-interpolated quantile of an already sorted list. */
    private fun quantile(sorted: List<Double>, q: Double): Double {
        val position = q * (sorted.size - 1)
        val lower = position.toInt()
        val upper = minOf(lower + 1, sorted.size - 1)
        val fraction = position - lower
        return sorted[lower] + (sorted[upper] - sorted[lower]) * fraction
    }
}
