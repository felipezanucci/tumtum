package cc.tumtum.app.domain

import java.time.Duration
import java.time.Instant

/**
 * Análise da noite (§7):
 *  - gap > 60s aparece como interrupção da linha; zero interpolação;
 *  - cobertura = tempo amostrado / duração da janela;
 *  - momentos = máximos locais destacados, o maior é o pico.
 */
object NightAnalyzer {

    const val GAP_THRESHOLD_SEC = 60L

    fun gaps(samples: List<HrSample>, windowStart: Instant, windowEnd: Instant): List<Gap> {
        if (samples.isEmpty()) return listOf(Gap(windowStart, windowEnd))
        val sorted = samples.sortedBy { it.time }
        val out = mutableListOf<Gap>()
        if (Duration.between(windowStart, sorted.first().time).seconds > GAP_THRESHOLD_SEC) {
            out += Gap(windowStart, sorted.first().time)
        }
        sorted.zipWithNext().forEach { (a, b) ->
            if (Duration.between(a.time, b.time).seconds > GAP_THRESHOLD_SEC) out += Gap(a.time, b.time)
        }
        if (Duration.between(sorted.last().time, windowEnd).seconds > GAP_THRESHOLD_SEC) {
            out += Gap(sorted.last().time, windowEnd)
        }
        return out
    }

    /** Cobertura em % — cada amostra "cobre" até o próximo ponto, limitado ao gap threshold. */
    fun coveragePct(samples: List<HrSample>, windowStart: Instant, windowEnd: Instant): Int {
        val total = Duration.between(windowStart, windowEnd).seconds
        if (total <= 0 || samples.isEmpty()) return 0
        val sorted = samples.sortedBy { it.time }
        var covered = 0L
        sorted.zipWithNext().forEach { (a, b) ->
            covered += Duration.between(a.time, b.time).seconds.coerceAtMost(GAP_THRESHOLD_SEC)
        }
        covered += GAP_THRESHOLD_SEC.coerceAtMost(total) // última amostra
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
