import { describe, it, expect } from 'vitest'
import { parseHRFile } from './import-parsers'

const POLAR_CSV = `Name,Sport,Date,Start time,Duration,Total distance (km),Average heart rate (bpm),Average speed (km/h),Max heart rate (bpm)
Felipe Zanucci,Other indoor,29-08-2026,22:00:00,05:58:12,0,112,0,187

Sample rate,Time,HR (bpm),Speed (km/h),Pace (min/km),Cadence,Altitude (m),Stride length (m),Distances (m),Temperatures (C),Power (W)
1,00:00:00,85,,,,,,,,
1,00:00:01,86,,,,,,,,
1,00:00:02,88,,,,,,,,
1,01:30:00,187,,,,,,,,
`

describe('Polar Flow CSV', () => {
  it('is read, anchored to the session start in the preamble', () => {
    const r = parseHRFile('polar.csv', POLAR_CSV)
    expect(r.samples).toHaveLength(4)
    expect(r.samples.map((s) => s.bpm)).toEqual([85, 86, 88, 187])

    // 29 August 2026, 22:00 local, plus each row's elapsed offset.
    const first = new Date(r.samples[0].time)
    expect(first.getFullYear()).toBe(2026)
    expect(first.getMonth()).toBe(7) // August
    expect(first.getDate()).toBe(29)
    expect(first.getHours()).toBe(22)
    expect(first.getMinutes()).toBe(0)

    const last = new Date(r.samples[3].time)
    expect(last.getDate()).toBe(29)
    expect(last.getHours()).toBe(23) // 22:00 + 1h30
    expect(last.getMinutes()).toBe(30)

    expect(r.warnings.join(' ')).toContain('contados a partir do início')
  })

  it('warns when the date could be read either way round', () => {
    const ambiguous = POLAR_CSV.replace('29-08-2026', '05-08-2026')
    const r = parseHRFile('polar.csv', ambiguous)
    expect(r.samples.length).toBe(4)
    expect(r.warnings.join(' ')).toContain('dia/mês ou mês/dia')
  })

  it('does not warn about the date when it is unambiguous', () => {
    const r = parseHRFile('polar.csv', POLAR_CSV)
    expect(r.warnings.join(' ')).not.toContain('dia/mês ou mês/dia')
  })
})

describe('formats that already worked', () => {
  it('reads a plain CSV with absolute timestamps', () => {
    const csv = `time,bpm
2026-08-29T22:00:00Z,85
2026-08-29T22:00:01Z,90
2026-08-29T22:00:02Z,95
`
    const r = parseHRFile('plain.csv', csv)
    expect(r.samples).toHaveLength(3)
    expect(r.samples[0].bpm).toBe(85)
    expect(r.warnings.join(' ')).not.toContain('contados a partir do início')
  })

  it('reads a Samsung Health export with its metadata line', () => {
    const csv = `com.samsung.health.heart_rate,1,com.samsung.shealth
com.samsung.health.heart_rate.start_time,com.samsung.health.heart_rate.heart_rate
2026-08-29 22:00:00,85
2026-08-29 22:00:10,92
`
    const r = parseHRFile('samsung.csv', csv)
    expect(r.format).toBe('samsung_health_csv')
    expect(r.samples).toHaveLength(2)
  })

  it('still refuses a file with no readings at all', () => {
    expect(() => parseHRFile('empty.csv', 'time,bpm\n,\n,\n')).toThrow()
  })

  it('reports the columns it looked at when it finds nothing', () => {
    try {
      parseHRFile('nope.csv', 'alpha,beta\n1,2\n')
      throw new Error('should have thrown')
    } catch (e) {
      expect((e as Error).message).toContain('alpha')
    }
  })

  it('prefers the header that yields more readings, not the first that matches', () => {
    // The summary block matches on labels but has one row of totals under it.
    const csv = `Start time,Average heart rate (bpm)
2026-08-29T22:00:00Z,112

Time,HR (bpm)
2026-08-29T22:00:00Z,85
2026-08-29T22:00:01Z,86
2026-08-29T22:00:02Z,88
`
    const r = parseHRFile('two-headers.csv', csv)
    expect(r.samples).toHaveLength(3)
    expect(r.samples.map((s) => s.bpm)).toEqual([85, 86, 88])
  })
})
