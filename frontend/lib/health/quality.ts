/**
 * Heart rate data quality analysis.
 *
 * Tumtum's peak detection (see CLAUDE.md) applies a 5-second moving average and
 * a 60-second rolling baseline before scoring z-scores. That pipeline only works
 * if the sensor samples continuously and densely enough:
 *
 * - A 5s moving average needs several readings per 5s window.
 * - A 60s rolling standard deviation needs enough points per window to be stable.
 * - Peak regions shorter than 5s are discarded as noise, so a sensor that reports
 *   once per minute can never produce a detectable peak.
 *
 * This module turns an imported file into a verdict against those requirements.
 * It is the measurement instrument we use to evaluate a candidate device.
 */

import type { HRSample } from './import-parsers'

/** Ideal cadence for peak detection — one reading every 5 seconds. */
export const TARGET_INTERVAL_SECONDS = 5

/** Above this median cadence, peak detection becomes unreliable. */
export const ACCEPTABLE_INTERVAL_SECONDS = 10

/** Above this median cadence, peak detection is not viable at all. */
export const MAX_USABLE_INTERVAL_SECONDS = 30

/**
 * Minimum silence that counts as a gap. For slow-but-steady devices the
 * threshold adapts upward to three times their own cadence, so that a device
 * sampling every 60s is judged on cadence rather than reported as 100% gaps.
 */
export const GAP_THRESHOLD_SECONDS = 30

export type QualityVerdict = 'good' | 'marginal' | 'insufficient'

export interface QualityReport {
  sampleCount: number
  startTime: string
  endTime: string
  durationSeconds: number
  medianIntervalSeconds: number
  p90IntervalSeconds: number
  gapCount: number
  longestGapSeconds: number
  /** Silence above which an interval was counted as a gap, in seconds. */
  gapThresholdSeconds: number
  /** Fraction of the session duration actually covered by readings (0–1). */
  coverage: number
  avgBpm: number
  minBpm: number
  maxBpm: number
  verdict: QualityVerdict
  /** Human-readable findings, in pt-BR, for the evaluation report. */
  notes: string[]
}

function percentile(sortedValues: number[], fraction: number): number {
  if (sortedValues.length === 0) return 0
  const index = Math.min(
    sortedValues.length - 1,
    Math.max(0, Math.ceil(fraction * sortedValues.length) - 1),
  )
  return sortedValues[index]
}

/**
 * Analyse sampling cadence, gaps and BPM range for a set of imported samples.
 *
 * Requires at least two samples; callers should surface a parse error before
 * reaching this point when a file yields fewer.
 */
export function analyseQuality(samples: HRSample[]): QualityReport {
  if (samples.length < 2) {
    throw new Error('São necessárias ao menos duas leituras para analisar a qualidade.')
  }

  const times = samples.map((sample) => Date.parse(sample.time))
  const bpms = samples.map((sample) => sample.bpm)

  const durationSeconds = (times[times.length - 1] - times[0]) / 1000

  const intervals: number[] = []
  for (let i = 1; i < times.length; i += 1) {
    intervals.push((times[i] - times[i - 1]) / 1000)
  }

  const sortedIntervals = [...intervals].sort((a, b) => a - b)
  const medianIntervalSeconds = percentile(sortedIntervals, 0.5)
  const p90IntervalSeconds = percentile(sortedIntervals, 0.9)

  // Judge dropouts against the device's own rhythm: a steady 60s sampler has no
  // dropouts, it is simply too slow — which the cadence verdict reports separately.
  const gapThresholdSeconds = Math.max(GAP_THRESHOLD_SECONDS, medianIntervalSeconds * 3)

  let gapCount = 0
  let longestGapSeconds = 0
  let coveredSeconds = 0

  for (const intervalSeconds of intervals) {
    if (intervalSeconds > gapThresholdSeconds) {
      gapCount += 1
    } else {
      coveredSeconds += intervalSeconds
    }
    longestGapSeconds = Math.max(longestGapSeconds, intervalSeconds)
  }

  const coverage = durationSeconds > 0 ? Math.min(coveredSeconds / durationSeconds, 1) : 0

  const avgBpm = Math.round(bpms.reduce((sum, bpm) => sum + bpm, 0) / bpms.length)

  let verdict: QualityVerdict
  if (medianIntervalSeconds <= ACCEPTABLE_INTERVAL_SECONDS && coverage >= 0.8) {
    verdict = 'good'
  } else if (medianIntervalSeconds <= MAX_USABLE_INTERVAL_SECONDS && coverage >= 0.5) {
    verdict = 'marginal'
  } else {
    verdict = 'insufficient'
  }

  const notes: string[] = []

  if (medianIntervalSeconds <= TARGET_INTERVAL_SECONDS) {
    notes.push(
      `Cadência ideal: uma leitura a cada ${medianIntervalSeconds.toFixed(1)}s ` +
        `(alvo do Tumtum: ${TARGET_INTERVAL_SECONDS}s).`,
    )
  } else if (medianIntervalSeconds <= ACCEPTABLE_INTERVAL_SECONDS) {
    notes.push(
      `Cadência aceitável: uma leitura a cada ${medianIntervalSeconds.toFixed(1)}s. ` +
        `O ideal para a detecção de picos é ${TARGET_INTERVAL_SECONDS}s.`,
    )
  } else if (medianIntervalSeconds <= MAX_USABLE_INTERVAL_SECONDS) {
    notes.push(
      `Cadência baixa: uma leitura a cada ${medianIntervalSeconds.toFixed(1)}s. ` +
        'A média móvel de 5s perde resolução e picos curtos podem sumir.',
    )
  } else {
    notes.push(
      `Cadência insuficiente: uma leitura a cada ${medianIntervalSeconds.toFixed(1)}s. ` +
        `Picos com menos de ${TARGET_INTERVAL_SECONDS}s são descartados como ruído, ` +
        'então a detecção de picos não funciona com esse dispositivo.',
    )
  }

  if (p90IntervalSeconds > medianIntervalSeconds * 3 && p90IntervalSeconds > gapThresholdSeconds) {
    notes.push(
      `Cadência irregular: 10% dos intervalos passam de ${p90IntervalSeconds.toFixed(0)}s, ` +
        `contra uma mediana de ${medianIntervalSeconds.toFixed(1)}s.`,
    )
  }

  if (gapCount > 0) {
    notes.push(
      `${gapCount} interrupção(ões) acima de ${gapThresholdSeconds.toFixed(0)}s. ` +
        `A maior tem ${formatDuration(longestGapSeconds)}.`,
    )
  }

  if (coverage < 0.8) {
    notes.push(
      `Cobertura de ${(coverage * 100).toFixed(0)}% do período — ` +
        'boa parte da sessão ficou sem medição.',
    )
  }

  if (durationSeconds < 600) {
    notes.push(
      'Amostra curta (menos de 10 minutos). Para avaliar o dispositivo de verdade, ' +
        'grave ao menos 30 minutos contínuos.',
    )
  }

  return {
    sampleCount: samples.length,
    startTime: samples[0].time,
    endTime: samples[samples.length - 1].time,
    durationSeconds,
    medianIntervalSeconds,
    p90IntervalSeconds,
    gapCount,
    longestGapSeconds,
    gapThresholdSeconds,
    coverage,
    avgBpm,
    minBpm: Math.min(...bpms),
    maxBpm: Math.max(...bpms),
    verdict,
    notes,
  }
}

/** Format a duration in seconds as "1h 23min", "23min 10s" or "45s". */
export function formatDuration(seconds: number): string {
  const total = Math.round(seconds)
  const hours = Math.floor(total / 3600)
  const minutes = Math.floor((total % 3600) / 60)
  const remainingSeconds = total % 60

  if (hours > 0) return `${hours}h ${minutes}min`
  if (minutes > 0) return `${minutes}min ${remainingSeconds}s`
  return `${remainingSeconds}s`
}

export const VERDICT_LABELS: Record<QualityVerdict, string> = {
  good: 'Aprovado',
  marginal: 'Limítrofe',
  insufficient: 'Reprovado',
}

export const VERDICT_DESCRIPTIONS: Record<QualityVerdict, string> = {
  good: 'Os dados atendem ao que a detecção de picos do Tumtum precisa.',
  marginal: 'Dá para gerar uma curva, mas a detecção de picos fica menos confiável.',
  insufficient: 'Os dados não sustentam a detecção de picos do Tumtum.',
}
