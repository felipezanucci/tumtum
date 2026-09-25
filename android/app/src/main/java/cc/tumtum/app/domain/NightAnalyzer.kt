package cc.tumtum.app.domain

import java.time.Duration
import java.time.Instant

/**
 * Análise da noite (§7):
 *  - gap > 60s (10s numa cinta a 1 Hz) aparece como interrupção da linha; zero interpolação;
 *  - cobertura = tempo amostrado / duração da janela;
 *  - momentos = máximos locais destacados, o maior é o pico.
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

    /**
     * Momentos: máximos locais com separação mínima de 10min, ordenados por BPM.
     * O maior é o pico da noite.
     */
    fun moments(samples: List<HrSample>, max: Int = 8): List<Moment> {
        if (samples.isEmpty()) return emptyList()
        val sorted = samples.sortedBy { it.time }
        val minSeparation = Duration.ofMinutes(10)
        val picked = mutableListOf<HrSample>()
        for (s in sorted.sortedByDescending { it.bpm }) {
            if (picked.none { Duration.between(it.time, s.time).abs() < minSeparation }) {
                picked += s
                if (picked.size == max) break
            }
        }
        val peakBpm = picked.maxOf { it.bpm }
        return picked.sortedByDescending { it.bpm }.map { s ->
            // duração: quanto tempo as amostras vizinhas ficam a até 8 bpm do topo
            val nearby = sorted.filter {
                Duration.between(it.time, s.time).abs() <= Duration.ofSeconds(45) && it.bpm >= s.bpm - 8
            }
            val dur = if (nearby.size >= 2) {
                Duration.between(nearby.first().time, nearby.last().time).seconds.toInt().coerceAtLeast(2)
            } else 2
            Moment(bpm = s.bpm, at = s.time, durationSec = dur, isPeak = s.bpm == peakBpm)
        }
    }
}
